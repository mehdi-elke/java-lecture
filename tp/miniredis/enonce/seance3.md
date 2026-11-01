# Séance 3 — Robuste et persistant

**Objectif** : votre serveur ne tombe plus, journalise proprement, et sauvegarde ses données sur disque.

**Cours utile** : support de la séance 3, §2 (streams), §3 (exceptions), §3.9 (envelopper une exception), §4.1 (`System.Logger`), §4.2 (CWE-209), §4.4 (injection dans les logs). §5 (fichiers : `Path`, `Files`, flux, écriture atomique, `@TempDir`).

---

## 1. 🟢 🔒 Faire tomber le serveur avec 12 octets (30 min)

Lancez votre serveur, puis dans un terminal :

```bash
printf '*1\r\n$abc\r\n' | nc localhost 6380
```

Votre serveur **plante**. Regardez la console : une `NumberFormatException`. Un client a annoncé une longueur, `abc`, qui n'est pas un nombre, et `RespReader` a fait confiance à `Integer.parseInt`.

Comparez avec Valkey (port 6379) : il répond une erreur et **reste debout**.

```
-ERR Protocol error: invalid bulk length
```

À faire :

1. Créez `miniredis.protocol.ProtocolException extends IOException`, avec un constructeur `ProtocolException(String message)`.
2. Dans `RespReader`, toute longueur lue doit être validée. Le **message** de l'exception est la fin du message de Valkey, **sans** `Protocol error: ` :

   | Ce que le client envoie | Message de la `ProtocolException` |
   |---|---|
   | une longueur de chaîne invalide : `$abc` | `invalid bulk length` |
   | un nombre d'arguments invalide : `*abc` | `invalid multibulk length` |
   | autre chose que `$` avant un argument : `*1\r\n+x` | `expected '$', got '+'` |

3. Dans `Server`, attrapez la `ProtocolException` : écrivez `new Reply.Error("ERR Protocol error: " + e.getMessage())` au client, puis fermez **cette** connexion. Le serveur, lui, continue.

Vérifiez : relancez la commande `nc`, puis confirmez que `valkey-cli` se connecte toujours.

> Gardez cette idée pour la séance 4 : on ne fait **jamais** confiance à une longueur annoncée par un client.

## 2. 🟢 Des exceptions propres (45 min)

Depuis la séance 1, vos commandes renvoient des `Reply.Error`. C'est mélangé : la logique métier et la mise en forme de l'erreur sont dans la même ligne. On va séparer.

1. Créez `miniredis.CommandException extends RuntimeException`, avec un constructeur `CommandException(String message)`. Son message **est** le message d'erreur Redis (`ERR value is not an integer or out of range`).
2. `miniredis.WrongTypeException`, créée en séance 2, hérite maintenant de `CommandException`.
3. Vos commandes **lancent** ces exceptions au lieu de renvoyer des `Reply.Error`. `Integers.parseStrict` aussi : elle lance une `CommandException` au lieu d'une `NumberFormatException`.
4. Dans `CommandProcessor.execute`, **un seul** `try/catch` les transforme en `Reply.Error`. C'est la « traduction à la frontière » (support S3, §3).

🔒 **Ne pas fuiter (CWE-209, support S3, §4.2).** Dans ce `try/catch`, attrapez aussi `RuntimeException` (les bugs : `NullPointerException`, index hors limites…). Le client ne doit **jamais** recevoir le détail d'un bug interne : répondez-lui `ERR internal error`, et écrivez la vraie exception, avec sa pile, dans les logs du serveur. Un message d'erreur trop bavard renseigne un attaquant.

Pour le tester, il faut une commande qui plante. La plus simple : un `SAVE` (§4) branché sur un `SnapshotStore` de test dont `save` lance une `IllegalStateException`. Le client doit recevoir `ERR internal error`, et rien d'autre.

## 3. 🟢 Streams (30 min)

Réécrivez avec l'API Stream (support S3, §2). C'est plus court et plus lisible.

- `KEYS` : `store.keys().stream().filter(...).sorted().toList()`.
- `MGET` : un `.map(...)` sur la liste des clés.
- `DBSIZE` (aucun argument) : le nombre de clés.
- `INFO` (arguments ignorés) : une réponse `Bulk`, sur le modèle de Valkey. Les lignes se terminent par `\r\n`. La section `# Types` compte les clés par type, avec `Collectors.groupingBy` (support S3, §2.7) :

  ```text
  # Server
  redis_version:7.2.4
  server_name:mini-redis

  # Keyspace
  db0:keys=3,expires=1,avg_ttl=0

  # Types
  list:1
  string:2
  ```

  `db0:...` donne le nombre de clés et le nombre de clés avec expiration. Base vide : pas de ligne `db0`. Les types sont triés par ordre alphabétique, sans ligne pour un type absent.

## 4. 🟢 Sauvegarder sur disque (1 h)

Aujourd'hui, un redémarrage efface tout. On ajoute `SAVE`, et le rechargement au démarrage.

**Fourni** : copiez ce fichier tel quel dans `src/main/java/miniredis/store/Snapshot.java`.

```java
package miniredis.store;

import java.time.Instant;
import java.util.Map;

/** L'état complet de la base à un instant : les données et les dates d'expiration. */
public record Snapshot(Map<String, Value> data, Map<String, Instant> expirations) {

    public Snapshot {
        data = Map.copyOf(data);
        expirations = Map.copyOf(expirations);
    }

    public static Snapshot empty() {
        return new Snapshot(Map.of(), Map.of());
    }
}
```

Signatures imposées :

```java
package miniredis.persistence;

public interface SnapshotStore {
    void save(Snapshot snapshot) throws IOException;
    Snapshot load() throws IOException;           // Snapshot.empty() s'il n'y a rien

    /** Pas de persistance : save ne fait rien, load rend Snapshot.empty(). Pour les tests. */
    static SnapshotStore none() { ... }
}

public final class FileSnapshotStore implements SnapshotStore {
    public FileSnapshotStore(Path file);
}
```

```java
// À ajouter à Store (miniredis.store)
public Snapshot snapshot();                 // une COPIE de tout le contenu, sans les clés expirées
public void restore(Snapshot snapshot);     // remplace tout le contenu ; ignore les clés déjà expirées

// À ajouter à CommandProcessor (miniredis)
public CommandProcessor(Store store, SnapshotStore snapshots);   // SAVE écrit dans snapshots
```

Les deux constructeurs de la séance 2 restent : ils utilisent `SnapshotStore.none()`.

`snapshot()` doit rendre une **copie** des listes et des ensembles : sinon, un `RPUSH` arrivé pendant la sauvegarde modifierait ce qu'on est en train d'écrire (support S2, §6.9).

**Format du fichier : les commandes qui recréent les données.** Plutôt qu'inventer un format, écrivez la suite des commandes qui, rejouées, reconstruisent la base — c'est l'idée de l'AOF de Redis :

```
SET nom Ada
RPUSH liste a b c
PEXPIREAT session 1798000000000
```

Chaque commande est encodée en RESP, avec le `RespWriter` **déjà fourni** : une commande, c'est un `Reply.Array` de `Reply.Bulk`. Vous la relisez avec `RespReader`. Conséquence agréable : une clé qui contient un espace ou un retour à la ligne est gérée toute seule, sans rien échapper à la main.

Les règles du fichier :
- **Où** : `data/dump.resp`, relatif au dossier de lancement (la racine du projet). Ce dossier est déjà dans le `.gitignore`.
- **Quoi** : une commande par clé : `SET` pour une chaîne, `RPUSH` pour une liste, `SADD` pour un ensemble. Puis un `PEXPIREAT clé date` par clé qui expire, où `date` est l'instant d'expiration en **millisecondes depuis le 1er janvier 1970** : `instant.toEpochMilli()`, et `Instant.ofEpochMilli(...)` pour relire.
- **Relire** : `load()` lit les commandes une à une avec `RespReader` et reconstruit elle-même le `Snapshot` avec un `switch` sur le nom. Elle ne passe **pas** par `CommandProcessor` : `PEXPIREAT` n'a pas besoin d'exister comme commande.
- **Pas de fichier** : `load()` rend `Snapshot.empty()`. C'est le cas au premier démarrage.

1. Écrivez `FileSnapshotStore`. Testez-la avec un `@TempDir` (support S3, §5.6) : sauvegarder un `Snapshot`, le relire, comparer.
2. La commande `SAVE` (aucun argument) appelle `save(store.snapshot())` et répond `OK`.
3. Au démarrage, `Server` recharge la sauvegarde. Dans `main`, remplacez `new CommandProcessor()` par :

   ```java
   SnapshotStore snapshots = new FileSnapshotStore(Path.of("data", "dump.resp"));
   Store store = new Store();
   store.restore(snapshots.load());
   CommandProcessor processor = new CommandProcessor(store, snapshots);
   ```

Testez : `SET`, `RPUSH`, `SAVE`, arrêtez le serveur, relancez-le, `GET` et `LRANGE`. Les données sont là.

🔒 Si `SAVE` échoue (disque plein…), le client reçoit `ERR saving failed, see server logs`, et le détail va dans les logs. Jamais le chemin complet du fichier ni la trace Java au client.

## 5. 🔵 Écriture atomique (20 min)

Que se passe-t-il si le serveur est tué **pendant** l'écriture du fichier ? La sauvegarde est à moitié écrite, donc illisible, et l'ancienne est perdue.

La parade : écrire dans un fichier temporaire **du même dossier**, puis le **renommer** sur le fichier final. Le renommage est atomique : `Files.move(tmp, fichier, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)`. À tout instant, le fichier final est soit l'ancienne version complète, soit la nouvelle, jamais un mélange (support S3, §5.4).

## 6. 🔵 🔒 Journaliser sans se faire piéger (30 min)

Remplacez vos `IO.println` par un `System.Logger` (support S3, §4.1). Choisissez le niveau : connexions et démarrage en `INFO`, chaque commande reçue en `DEBUG` (sinon, les 100 000 `INCR` de la séance 4 noient la console), erreurs internes en `ERROR` avec l'exception.

Puis l'injection dans les logs (CWE-117, support S3, §4.4). Un client crée cette clé :

```
SET "compte\r\n+ client 10.0.0.1 authentifié" 1
```

Si vous journalisez le nom de la clé tel quel, le `\r\n` crée une **fausse ligne** dans vos logs. Un attaquant fabrique ainsi de fausses traces. Écrivez une petite méthode qui neutralise les caractères de contrôle (`\r`, `\n`…) avant d'écrire dans les logs, et tronque les valeurs trop longues (signature imposée) :

```java
package miniredis.util;

public final class LogSanitizer {
    public static final int MAX_LENGTH = 200;
    /** text sans aucun caractère de contrôle, et d'au plus MAX_LENGTH caractères (+ « … » si tronqué). */
    public static String clean(String text) { ... }
}
```

Par quoi remplacer `\r` ? Le plus utile pour qui lit les logs : le texte visible `\r` (deux caractères). Le support (§4.4) remplace par `_` : les deux conviennent.

## 7. 🟣 Des commandes en une ligne (bonus)

Pour les commandes très simples (`PING`, `ECHO`), une classe entière est lourde. Ajoutez une fabrique :

```java
Command.of("PING", 0, 1, (args, store) -> args.isEmpty()
        ? new Reply.Simple("PONG")
        : new Reply.Bulk(args.getFirst()));
```

C'est une lambda qui joue le rôle de `doExecute` (support S3, §1).

---

## Avant de partir

- [ ] `nc` avec une longueur invalide : le serveur répond une erreur et survit.
- [ ] `mvn test` passe (ajoutez des tests pour la persistance, avec un `@TempDir`, et pour `ERR internal error`).
- [ ] `Compare` sur `s1`, `s2`, `s3` : sections 🟢 toutes `✓`.
- [ ] Les données survivent à un redémarrage.
- [ ] Tout est commité.
