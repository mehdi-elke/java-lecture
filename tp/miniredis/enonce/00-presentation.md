# TP Mini-Redis — Présentation

## Le projet

En quatre séances, vous écrivez en Java un **serveur clé-valeur compatible avec Redis**.
À la fin, le vrai client `valkey-cli` se connecte à votre serveur et ne voit pas la différence.

```
$ valkey-cli -p 6380
127.0.0.1:6380> SET nom Ada
OK
127.0.0.1:6380> GET nom
"Ada"
127.0.0.1:6380> INCR visites
(integer) 1
```

Redis et son fork libre Valkey servent de cache, de file de messages ou de base de données dans une grande partie des applications web. Vous allez comprendre comment un tel serveur fonctionne de l'intérieur, et pourquoi certains ont été piratés.

## La règle unique : « fais comme Valkey »

Il n'y a pas de cahier des charges à interpréter. **La spécification, c'est le comportement du vrai Valkey.**

Vous ne savez pas ce que doit répondre `INCR` sur une chaîne qui n'est pas un nombre ? Ne demandez pas à l'enseignant : demandez à Valkey.

```
$ valkey-cli -p 6379
127.0.0.1:6379> SET mot abc
OK
127.0.0.1:6379> INCR mot
(error) ERR value is not an integer or out of range
```

Votre serveur doit répondre **exactement** la même chose, au caractère près.

## Ce qui est fourni

Le projet `starter/` contient :

| Fichier | Rôle |
|---|---|
| `protocol/Reply.java` | Les 6 types de réponse du protocole (`Simple`, `Error`, `Int`, `Bulk`, `Nil`, `Array`) |
| `protocol/RespReader.java` | Lit les commandes envoyées par le client |
| `protocol/RespWriter.java` | Encode et envoie les réponses |
| `server/Server.java` | La boucle réseau : accepte un client et lui répond |
| `CommandProcessor.java` | **Votre point de départ** : exécute les commandes. `SET` et `GET` sont donnés en exemple |
| `tools/Compare.java` | Compare votre serveur au vrai Valkey |
| `tools/LoadTest.java` | Met votre serveur à l'épreuve (séance 4) |
| `tests/s1.txt` … `s4.txt` | Les commandes testées à chaque séance |

Vous n'avez pas à écrire le protocole réseau. Lisez quand même `RespTest.java` : il montre les octets réellement échangés.

## Installation

Il vous faut :

- **JDK 25** : `java -version` doit afficher `25`.
- **Maven** : `mvn -v`. Les IDE (IntelliJ, VS Code) l'embarquent.
- **Docker** : pour lancer le Valkey de référence et le client `valkey-cli`.

Sans Docker, demandez à l'enseignant l'adresse du Valkey de référence de la salle, et utilisez `nc` (ou `telnet`) comme client.

## Les quatre commandes à connaître

**1. Lancer le Valkey de référence** (port 6379), une fois par séance :

```bash
docker run -d --rm --name ref-valkey -p 6379:6379 valkey/valkey:8
```

**2. Lancer votre serveur** (port 6380) : bouton ▶ de l'IDE sur `Server.main`, ou

```bash
mvn compile exec:java
```

**3. Parler à un serveur** avec `valkey-cli` :

```bash
# votre serveur
docker exec -it ref-valkey valkey-cli -h host.docker.internal -p 6380
# le serveur de référence
docker exec -it ref-valkey valkey-cli
```

Sous Linux, remplacez `host.docker.internal` par l'adresse IP de votre machine, ou lancez le conteneur avec `--network host`.
Sans `valkey-cli`, `nc localhost 6380` permet de taper des commandes à la main : `PING` puis Entrée.

**4. Comparer votre serveur à Valkey** :

```bash
java tools/Compare.java tests/s1.txt
```

```
# 🟢 PING et ECHO
  ✓ PING                             PONG
  ✗ ECHO salut                       attendu : "salut"
                                     obtenu  : (error) ERR unknown command 'ECHO', with args beginning with: 'salut'
...
21 identiques, 54 différents
```

Chaque ligne `✗` est une tâche. Votre objectif : `0 différents`.
Pour tester un cas qui n'est pas dans le fichier, écrivez votre propre fichier de commandes : le format est expliqué en tête de `Compare.java`.

## Les niveaux

Chaque énoncé propose trois niveaux. Avancez à votre rythme.

- 🟢 **Socle** : ce que tout le monde doit faire. C'est la base de la note.
- 🔵 **Approfondissement** : pour aller plus loin, une fois le socle terminé.
- 🟣 **Bonus** : pour ceux qui ont déjà de l'expérience.
- 🔒 **Sécurité** : les exercices de sécurité, marqués à chaque niveau.

## Les règles du jeu

- **Des tests JUnit** : chaque commande que vous écrivez a au moins un test dans `CommandProcessorTest`. Un test s'écrit sans réseau : `run("INCR", "compteur")`.
- **Les signatures imposées** dans les énoncés doivent être respectées à la lettre. Le code fourni plus tard, et les tests de correction, en dépendent.
- **Git** : un commit au moins à chaque tâche terminée. Le dépôt est rendu à la fin des séances 2 et 4.
- **L'IA est autorisée**, mais vous êtes évalués sans elle : vous devrez expliquer et modifier votre code à l'oral, en 3 minutes. Cherchez 15 minutes seul avant de demander, demandez une explication plutôt que du code, et laissez Valkey et vos tests trancher : l'IA oublie souvent les cas limites.

## Notation

Elle repose sur :

- des QCM courts en début de séances 2, 3 et 4 ;
- un rendu avant la séance 2 ([consigne](rendu-seance1.md)), puis deux jalons en fin de séances 2 et 4 (dépôt Git évalué par des tests cachés et par `Compare`) ;
- **deux passages de 3 minutes** avec l'enseignant, en séances 2 et 4 : expliquer puis modifier votre propre code, sans IA.

## Le plan

| Séance | Vous écrivez | Notions |
|---|---|---|
| [1](seance1.md) | Les premières commandes : `PING`, `INCR`, `APPEND`, `MGET`… | `switch`, `String`, méthodes, JUnit |
| [2](seance2.md) | Une architecture objet, les listes, l'expiration des clés | interfaces, héritage, `record`, `sealed`, collections, `Clock` |
| [3](seance3.md) | Un serveur robuste, la sauvegarde sur disque | exceptions, streams, fichiers, logs |
| [4](seance4.md) | Plusieurs clients en même temps, une base H2, les failles | threads, JDBC, injection SQL, déni de service |
