# Séance 4 — Plusieurs clients, une base, des failles

**Objectif** : votre serveur tient plusieurs clients en même temps, sauvegarde dans une base H2, et résiste aux attaques classiques.

**Cours utile** : support de la séance 4, §1.3 (threads virtuels), §1.4 à §1.7 (race condition et corrections), §1.8 (TOCTOU), §2 (JDBC), §2.3 (identifiants hors du code), §2.5 (`PreparedStatement`), §2.6 (`ResultSet`), §2.7 (injection SQL), §3.1 (mots de passe).

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
try (var clients = Executors.newVirtualThreadPerTaskExecutor()) {
    while (true) {
        Socket client = serverSocket.accept();
        clients.submit(() -> serve(client));
    }
}
```

Relancez `LoadTest block` : il passe.

## 2. 🟢 🔒 La race condition (45 min)

Maintenant que plusieurs clients s'exécutent **en même temps**, ils touchent tous le même `Store`. Lancez :

```bash
java tools/LoadTest.java incr
```

50 clients font chacun 2 000 `INCR` sur la même clé. Résultat attendu : 100 000. Vous obtenez **moins**. Des incréments sont perdus : `INCR` lit, ajoute, écrit — et deux clients lisent la même valeur avant que l'un ait écrit (support S4, §1.4).

```bash
java tools/LoadTest.java lock
```

Pire : 50 clients font `SET verrou x NX`, et **plusieurs** reçoivent `OK`. Or `NX` veut dire « seulement si la clé n'existe pas ». Deux clients croient tous les deux détenir le verrou. C'est un **TOCTOU** : *time of check to time of use* (support S4, §1.8). Sur un vrai système, deux processus croient chacun être seuls à traiter un paiement.

**La correction.** Le vrai Redis exécute toutes les commandes sur **un seul thread** : elles ne se chevauchent jamais. Faites pareil, le plus simplement du monde : rendez `CommandProcessor.execute` `synchronized`. Une commande entière s'exécute d'un bloc.

Relancez `incr` et `lock` : exactement 100 000, et un seul `OK`.

🔵 Alternative plus fine, sans verrou global : `ConcurrentHashMap` et ses opérations atomiques `compute` / `putIfAbsent` (support S4, §1.7). Plus rapide, mais il faut raisonner commande par commande.

## 3. 🟢 🔒 Déni de service : la longueur annoncée (30 min)

Souvenez-vous de la séance 3 : on ne fait pas confiance à une longueur envoyée par le client. Testez l'autre bout du problème :

```bash
java tools/LoadTest.java bomb
```

Le client annonce une chaîne de **2 Go** en 30 octets : `*1\r\n$2000000000\r\n`. Si votre `RespReader` fait `new byte[2_000_000_000]`, le serveur réserve 2 Go d'un coup. Quelques clients comme celui-là, et le serveur tombe (`OutOfMemoryError`). C'est un déni de service.

Bornez, dans `RespReader` :

- la taille d'une chaîne : 512 Ko suffisent largement ;
- le nombre d'arguments d'une commande : 1 024 ;
- la longueur d'une ligne inline.

Au-delà, une `ProtocolException` (comme en séance 3). Comparez le message avec Valkey.

Relancez `bomb` : le serveur refuse tout de suite et reste debout.

## 4. 🟢 🔒 Persistance en base, et injection SQL (1 h 15)

On remplace le fichier par une base **H2**. L'enseignant vous donne `H2SnapshotStore.java` : vous le placez dans `miniredis/persistence/` et lancez le serveur avec `--persistence h2`.

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
4. vérifiez les dégâts : un nouveau `SAVE` échoue, parce que la table n'existe plus.

### c) La correction

- Réécrivez `save()` avec des `PreparedStatement` et des `?` (support S4, §2.5). La valeur n'est **jamais** mélangée au texte SQL : une apostrophe, un `DROP TABLE`, tout redevient une donnée inoffensive.
- Écrivez `load()` avec un `ResultSet` (support S4, §2.6) : le `TODO` est à vous.
- Vérifiez : `SET plat "l'été"`, `SAVE`, redémarrage, `GET plat` répond `"l'été"`. Et votre valeur d'attaque, maintenant, est juste stockée telle quelle.

🔵 🔒 **Les identifiants.** Le code fourni contient `"sa"` et un mot de passe en clair. Un secret dans le code source finit dans Git, donc partout (support S4, §2.3). Lisez l'URL, l'utilisateur et le mot de passe depuis des **variables d'environnement** (`System.getenv`), avec une valeur par défaut raisonnable.

## 5. 🟣 🔒 Les bonus sécurité

- **`AUTH`.** Si la variable d'environnement `MINIREDIS_PASSWORD` est définie, le serveur exige `AUTH motdepasse` avant toute autre commande, et répond `NOAUTH Authentication required.` sinon. Comparez le mot de passe avec `MessageDigest.isEqual`, **pas** avec `equals` : une comparaison qui s'arrête au premier caractère faux laisse deviner le mot de passe en mesurant le temps de réponse (support S4, §3.1).
- **`CONFIG SET dir`.** C'est l'attaque historique des Redis exposés : l'attaquant change le dossier de sauvegarde, puis écrit un fichier où il veut (une clé SSH, une tâche cron). Implémentez `CONFIG GET`, mais **refusez** `CONFIG SET dir` à l'exécution. Pour `dbfilename`, n'acceptez qu'un nom de fichier simple, jamais un chemin (pas de `/`, pas de `..`).

---

## Avant de partir (jalon 2)

- [ ] `java tools/LoadTest.java all` : tout passe.
- [ ] `mvn test` passe.
- [ ] `Compare` sur `s1` à `s4` : sections 🟢 toutes `✓`.
- [ ] L'injection SQL est corrigée, la base recharge après redémarrage.
- [ ] Vous savez expliquer une de vos corrections de sécurité à l'oral.
- [ ] Tout est commité et poussé.
