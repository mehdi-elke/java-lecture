# Séance 3 — Robuste et persistant

**Objectif** : votre serveur ne tombe plus, journalise proprement, et sauvegarde ses données sur disque.

**Cours utile** : support de la séance 3, §2 (streams), §3 (exceptions), §3.9 (envelopper une exception), §4.1 (`System.Logger`), §4.2 (CWE-209), §4.4 (injection dans les logs). Lecture et écriture de fichiers (`Path`, `Files`, `Files.move` atomique) : voir la partie fichiers du support S3.

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

1. Créez `protocol/ProtocolException extends IOException`.
2. Dans `RespReader`, toute longueur lue doit être validée : si ce n'est pas un nombre, lancez une `ProtocolException` avec le message de Valkey.
3. Dans `Server`, attrapez la `ProtocolException` : écrivez `-ERR Protocol error: ...` au client, puis fermez **cette** connexion. Le serveur, lui, continue.

Vérifiez : relancez la commande `nc`, puis confirmez que `valkey-cli` se connecte toujours.

> Gardez cette idée pour la séance 4 : on ne fait **jamais** confiance à une longueur annoncée par un client.

## 2. 🟢 Des exceptions propres (45 min)

Depuis la séance 1, vos commandes renvoient des `Reply.Error`. C'est mélangé : la logique métier et la mise en forme de l'erreur sont dans la même ligne. On va séparer.

1. Créez `CommandException extends RuntimeException`. Son message **est** le message d'erreur Redis (`ERR value is not an integer or out of range`).
2. Créez `WrongTypeException extends CommandException` pour l'erreur `WRONGTYPE`.
3. Vos commandes **lancent** ces exceptions au lieu de renvoyer des `Reply.Error`.
4. Dans `CommandProcessor.execute`, **un seul** `try/catch` les transforme en `Reply.Error`. C'est la « traduction à la frontière » (support S3, §3).

🔒 **Ne pas fuiter (CWE-209, support S3, §4.2).** Dans ce `try/catch`, attrapez aussi `RuntimeException` (les bugs : `NullPointerException`, index hors limites…). Le client ne doit **jamais** recevoir le détail d'un bug interne : répondez-lui `ERR internal error`, et écrivez la vraie exception, avec sa pile, dans les logs du serveur. Un message d'erreur trop bavard renseigne un attaquant.

## 3. 🟢 Streams (30 min)

Réécrivez avec l'API Stream (support S3, §2). C'est plus court et plus lisible.

- `KEYS` : `store.keys().stream().filter(...).sorted().toList()`.
- `MGET` : un `.map(...)` sur la liste des clés.
- `DBSIZE` : le nombre de clés.
- `INFO` : ajoutez une section qui compte les clés par type, avec `Collectors.groupingBy` (support S3, §2.7).

## 4. 🟢 Sauvegarder sur disque (1 h)

Aujourd'hui, un redémarrage efface tout. On ajoute `SAVE`, et le rechargement au démarrage.

Signature imposée :

```java
package miniredis.persistence;

public interface SnapshotStore {
    void save(Snapshot snapshot) throws IOException;
    Snapshot load() throws IOException;           // Snapshot.empty() s'il n'y a rien
}
```

`Snapshot` est fourni : un `record` qui contient les données et les expirations.

**Format du fichier : les commandes qui recréent les données.** Plutôt qu'inventer un format, écrivez la suite des commandes qui, rejouées, reconstruisent la base — c'est l'idée de l'AOF de Redis :

```
SET nom Ada
RPUSH liste a b c
PEXPIREAT session 1798000000000
```

Chaque commande est encodée en RESP, avec le `RespWriter` **déjà fourni**. Vous le relisez avec `RespReader`. Conséquence agréable : une clé qui contient un espace ou un retour à la ligne est gérée toute seule, sans rien échapper à la main.

1. Écrivez `FileSnapshotStore implements SnapshotStore`.
2. La commande `SAVE` appelle `save(store.snapshot())` et répond `OK`.
3. Au démarrage, `Server` appelle `load()` et remplit le `Store`.

Testez : `SET`, `RPUSH`, `SAVE`, arrêtez le serveur, relancez-le, `GET` et `LRANGE`. Les données sont là.

🔒 Si `SAVE` échoue (disque plein…), le client reçoit `ERR ...` court, et le détail va dans les logs. Jamais le chemin complet du fichier ni la trace Java au client.

## 5. 🔵 Écriture atomique (20 min)

Que se passe-t-il si le serveur est tué **pendant** l'écriture du fichier ? La sauvegarde est à moitié écrite, donc illisible, et l'ancienne est perdue.

La parade : écrire dans un fichier temporaire, puis le **renommer** sur le fichier final. Le renommage est atomique : `Files.move(tmp, fichier, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)`. À tout instant, le fichier final est soit l'ancienne version complète, soit la nouvelle, jamais un mélange.

## 6. 🔵 🔒 Journaliser sans se faire piéger (30 min)

Remplacez vos `IO.println` par un `System.Logger` (support S3, §4.1).

Puis l'injection dans les logs (CWE-117, support S3, §4.4). Un client crée cette clé :

```
SET "compte\r\n+ client 10.0.0.1 authentifié" 1
```

Si vous journalisez le nom de la clé tel quel, le `\r\n` crée une **fausse ligne** dans vos logs. Un attaquant fabrique ainsi de fausses traces. Écrivez une petite méthode qui neutralise les caractères de contrôle (`\r`, `\n`…) avant d'écrire dans les logs, et tronque les valeurs trop longues.

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
- [ ] `mvn test` passe (ajoutez des tests pour la persistance, avec un `@TempDir`).
- [ ] `Compare` sur `s1`, `s2`, `s3` : sections 🟢 toutes `✓`.
- [ ] Les données survivent à un redémarrage.
- [ ] Tout est commité.
