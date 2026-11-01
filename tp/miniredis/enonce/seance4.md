# Séance 4 — Plusieurs clients, une base, des failles

**Objectif** : votre serveur tient plusieurs clients en même temps, sauvegarde dans une base H2, et résiste aux attaques classiques.

**Cours utile** : support de la séance 3, §5.5 (chemins) ; support de la séance 4, §1.3 (threads virtuels), §1.4 à §1.7 (race condition et corrections), §1.8 (TOCTOU), §2 (JDBC), §2.3 (identifiants hors du code), §2.5 (`PreparedStatement`), §2.6 (`ResultSet`), §2.7 (injection SQL), §3.1 (mots de passe).

**Rendu** : jalon 2 à la fin de la séance. Prévoyez 3 minutes pour expliquer une de vos corrections de sécurité à l'oral.

---

## 1. 🟢 Plusieurs clients à la fois (30 min)

Votre serveur ne sert **qu'un client à la fois** : le deuxième attend que le premier parte. Vérifiez-le :

```bash
java tools/LoadTest.java block
```

Un client qui se connecte sans rien dire bloque tous les autres. Le test échoue (ou traîne).

Dans `Server`, au lieu de traiter le client dans la boucle d'acceptation, confiez chaque connexion à un **thread virtuel** (support S4, §1.3) :

```java
try (ServerSocket serverSocket = new ServerSocket(port);
     var clients = Executors.newVirtualThreadPerTaskExecutor()) {
    while (true) {
        Socket client = serverSocket.accept();
        clients.submit(() -> serve(client, processor));
    }
}
```

```java
/** Sert un client jusqu'à sa déconnexion, dans son propre thread virtuel. */
private static void serve(Socket client, CommandProcessor processor) {
    try (client) {                         // la socket est fermée à la fin, quoi qu'il arrive
        handle(client, processor);         // la méthode du starter, inchangée
    } catch (IOException e) {
        IO.println("- connexion interrompue : " + e.getMessage());   // ou votre LOG de la séance 3
    }
}
```

Avant, le `try (Socket client = ...)` de la boucle fermait la socket. Maintenant, la boucle repart tout de suite : c'est `serve` qui ferme, dans le thread du client. Et une `IOException` ne remonte plus jusqu'à `main` : un client qui coupe brutalement ne doit pas arrêter le serveur.

Relancez `LoadTest block` : il passe.

## 2. 🟢 🔒 La race condition (40 min)

Maintenant que plusieurs clients s'exécutent **en même temps**, ils touchent tous le même `Store`. Lancez :

```bash
java tools/LoadTest.java incr
```

50 clients font chacun 2 000 `INCR` sur la même clé. Résultat attendu : 100 000. Vous obtenez **moins**. Des incréments sont perdus : `INCR` lit, ajoute, écrit — et deux clients lisent la même valeur avant que l'un ait écrit (support S4, §1.4).

```bash
java tools/LoadTest.java lock
```

Pire : 50 clients font en même temps `SET verrou x NX` (votre commande de la séance 3), 200 fois de suite, et à certains tours **plusieurs** reçoivent `OK`. Or `NX` veut dire « seulement si la clé n'existe pas ». Deux clients croient tous les deux détenir le verrou. C'est un **TOCTOU** : *time of check to time of use* (support S4, §1.8). Sur un vrai système, deux processus croient chacun être seuls à traiter un paiement.

**La correction.** Le vrai Redis exécute toutes les commandes sur **un seul thread** : elles ne se chevauchent jamais. Faites pareil, le plus simplement du monde : rendez `CommandProcessor.execute` `synchronized`. Une commande entière s'exécute d'un bloc.

Relancez `incr` et `lock` : exactement 100 000, et un seul `OK` à chaque tour.

🔵 Alternative plus fine, sans verrou global : `ConcurrentHashMap` et ses opérations atomiques `compute` / `putIfAbsent` (support S4, §1.7). Plus rapide, mais il faut raisonner commande par commande.

## 3. 🟢 🔒 Déni de service : la longueur annoncée (30 min)

Souvenez-vous de la séance 3 : on ne fait pas confiance à une longueur envoyée par le client. Testez l'autre bout du problème :

```bash
java tools/LoadTest.java bomb
```

Le client annonce une chaîne de **2 Go** en 30 octets : `*1\r\n$2000000000\r\n`. Si votre `RespReader` fait `new byte[2_000_000_000]`, le serveur tente de réserver 2 Go d'un coup. Selon la mémoire de la JVM, il lève une `OutOfMemoryError`, ou réussit et attend 2 Go qui n'arriveront jamais. Quelques clients comme celui-là, et le serveur n'a plus de mémoire pour personne. C'est un déni de service.

Bornez, dans `RespReader` :

- la taille d'une chaîne : 512 Ko suffisent largement ;
- le nombre d'arguments d'une commande : 1 024 ;
- la longueur d'une ligne inline.

Au-delà, une `ProtocolException` (comme en séance 3). Ces limites sont les nôtres, bien plus basses que celles de Valkey ; les messages, eux, sont ceux de Valkey :

| Dépassement | Message de la `ProtocolException` |
|---|---|
| chaîne annoncée de plus de 512 Ko (`$2000000000`) | `invalid bulk length` |
| plus de 1 024 arguments annoncés (`*1000000`) | `invalid multibulk length` |
| ligne inline de plus de 64 Ko sans fin de ligne | `too big inline request` |

**Testez la borne avant d'allouer** : la vérification se fait sur le nombre lu, avant `new byte[...]`. Pour la ligne inline, lisez octet par octet et arrêtez-vous dès que la limite est dépassée : un client qui envoie des `a` sans fin ne doit pas remplir la mémoire.

Relancez `bomb` : le serveur refuse tout de suite et reste debout.

Attention à votre sauvegarde : `FileSnapshotStore.load()` relit le fichier avec ce même `RespReader`. Une liste de plus de 1 024 éléments, ou une valeur de plus de 512 Ko, ne se rechargerait plus. 🔵 Le fichier vient de votre serveur, pas d'un client : ajoutez un second constructeur `RespReader(InputStream in, int maxBulk, int maxArgs)`, et passez des limites larges dans `load()`.

## 4. 🟢 🔒 Persistance en base, et injection SQL (50 min)

On remplace le fichier par une base **H2**. L'enseignant vous donne `H2SnapshotStore.java` **en début de séance** : placez-le dans `src/main/java/miniredis/persistence/`. Il n'utilise que les signatures imposées (`Snapshot`, `Value`, `StringValue`, `ListValue`, `SnapshotStore`) : il compile tel quel dans votre projet. La dépendance H2 est déjà dans le `pom.xml`.

Pour choisir le stockage au lancement, `Server.main` lit ses options. Remplacez le début de `main` :

```java
public static void main(String[] args) throws IOException {
    int port = Integer.parseInt(option(args, "--port", "6380"));
    SnapshotStore snapshots = switch (option(args, "--persistence", "file")) {
        case "file" -> new FileSnapshotStore(Path.of("data", "dump.resp"));
        case "h2" -> H2SnapshotStore.fromEnvironment();
        default -> throw new IllegalArgumentException("--persistence : file ou h2");
    };
    // ... la suite ne change pas : store.restore(snapshots.load()), etc.
}

/** La valeur qui suit name dans args, par exemple "h2" pour "--persistence h2". */
private static String option(String[] args, String name, String defaultValue) {
    for (int i = 0; i < args.length - 1; i++) {
        if (args[i].equals(name)) return args[i + 1];
    }
    return defaultValue;
}
```

Lancement : `mvn compile exec:java -Dexec.args="--persistence h2"`. Dans IntelliJ : **Run → Edit Configurations…**, champ **Program arguments** : `--persistence h2`.

Ce code marche... en apparence.

### a) La valeur qui casse tout

```
SET plat "l'été"
SAVE
```

`SAVE` échoue. Regardez les logs : l'apostrophe de `l'été` a cassé la requête SQL. Le code construit le SQL en **collant** la valeur dans le texte de la requête (support S4, §2.7). L'apostrophe ferme la chaîne SQL trop tôt.

### b) De la panne à l'attaque

Si une apostrophe casse une requête par accident, un attaquant peut la casser **exprès**. Une valeur bien choisie, suivie de `SAVE`, peut exécuter un deuxième ordre SQL : par exemple détruire la table où tout est stocké. C'est l'injection SQL, la même faille que les Redis/Valkey réellement compromis sur Internet.

À vous de trouver la valeur. Méthode :
1. regardez dans le code exactement comment la valeur est insérée dans la requête `INSERT` ;
2. construisez une valeur qui ferme proprement cet `INSERT`, puis en ajoute un second, destructeur ;
3. envoyez-la avec `SET`, puis `SAVE` ;
4. vérifiez les dégâts : le `SAVE` suivant échoue (`Table "ENTRIES" not found` dans les logs), parce que la table n'existe plus.

Pour envoyer une valeur pleine d'apostrophes et de guillemets, utilisez `valkey-cli` ou `java tools/Cli.java` : entre guillemets, `\"` est un guillemet dans la valeur.

### c) La correction

- Réécrivez `save()` avec des `PreparedStatement` et des `?` (support S4, §2.5). La valeur n'est **jamais** mélangée au texte SQL : une apostrophe, un `DROP TABLE`, tout redevient une donnée inoffensive.
- Vérifiez : `SET plat "l'été"`, `SAVE` répond `OK`. Et votre valeur d'attaque, maintenant, est juste stockée telle quelle.
- 🔵 Écrivez `load()` avec un `ResultSet` (support S4, §2.6) : le `TODO` est à vous. Vérifiez : redémarrage, `GET plat` répond `"l'été"`.

🔵 🔒 **Les identifiants.** Le code fourni contient `"sa"` et un mot de passe en clair. Un secret dans le code source finit dans Git, donc partout (support S4, §2.3). Lisez l'URL, l'utilisateur et le mot de passe depuis les **variables d'environnement** `MINIREDIS_JDBC_URL`, `MINIREDIS_DB_USER` et `MINIREDIS_DB_PASSWORD` (`System.getenv`). Sans variable : l'URL actuelle, l'utilisateur `sa`, et un mot de passe **vide** (jamais un vrai secret par défaut dans le code).

Attention : H2 enregistre l'utilisateur et le mot de passe **dans le fichier de la base**, à sa création. Si vous changez de mot de passe, la base existante le refuse (`Wrong user name or password`) : supprimez `data/miniredis.mv.db`, elle sera recréée.

## 5. 🟣 🔒 Les bonus sécurité

- **`AUTH`.** Si la variable d'environnement `MINIREDIS_PASSWORD` est définie, le serveur exige `AUTH motdepasse` avant toute autre commande, et répond `NOAUTH Authentication required.` sinon. Comparez le mot de passe avec `MessageDigest.isEqual`, **pas** avec `equals` : une comparaison qui s'arrête au premier caractère faux laisse deviner le mot de passe en mesurant le temps de réponse (support S4, §3.1).

  Être authentifié est un état **de la connexion**, pas du serveur : un client authentifié ne doit pas ouvrir la porte aux autres. Il ne peut donc pas vivre dans `CommandProcessor`, partagé par tous et dont `execute` ne change pas. Gérez `AUTH` dans `Server`, dans la boucle de **chaque** client : une variable locale `authenticated`, et `AUTH` traitée avant d'appeler `processor.execute`. La vérification du mot de passe, elle, mérite sa propre petite classe (par exemple `Authentication`), testable sans socket. Les réponses de Valkey sont dans la [spécification](specification.md).
- **`CONFIG SET dir`.** C'est l'attaque historique des Redis exposés : l'attaquant change le dossier de sauvegarde, puis écrit un fichier où il veut (une clé SSH, une tâche cron). Implémentez `CONFIG GET dir` et `CONFIG GET dbfilename`, mais **refusez** `CONFIG SET dir` à l'exécution. Pour `CONFIG SET dbfilename`, n'acceptez qu'un nom de fichier simple, jamais un chemin (pas de `/`, pas de `..` : support S3, §5.5). Formats et messages : dans la spécification.

---

## Avant de partir (jalon 2)

- [ ] `java tools/LoadTest.java all` : tout passe.
- [ ] `mvn test` passe.
- [ ] `Compare` sur `s1` à `s4` : sections 🟢 toutes `✓`.
- [ ] L'injection SQL est corrigée (et 🔵 la base recharge après redémarrage).
- [ ] Vous savez expliquer une de vos corrections de sécurité à l'oral.
- [ ] Tout est commité et poussé.
