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

Vous ne savez pas ce que doit répondre `INCR` sur une chaîne qui n'est pas un nombre ? Ne demandez pas à l'enseignant : cherchez ce que répond Valkey.

```
$ valkey-cli -p 6379
127.0.0.1:6379> SET mot abc
OK
127.0.0.1:6379> INCR mot
(error) ERR value is not an integer or out of range
```

Votre serveur doit répondre **exactement** la même chose, au caractère près.

Inutile d'installer Valkey pour le savoir : la [spécification écrite](specification.md) liste chaque commande, ses réponses et ses pièges, recopiés de Valkey, et `tests/s1.expected` donne la réponse exacte de Valkey à chaque commande testée (la commande, `=>`, la réponse).

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
| `tools/Cli.java` | Un mini `valkey-cli`, pour parler à votre serveur sans rien installer |
| `tools/LoadTest.java` | Met votre serveur à l'épreuve (séance 4) |
| `tools/PubSubCompare.java` | Compare votre Pub/Sub à Valkey, avec plusieurs clients ([défi](defi-pubsub.md)) |
| `tests/s1.txt` … `s4.txt` | Les commandes testées à chaque séance |
| `tests/s1.expected` … `s4.expected` | Les réponses de Valkey à ces commandes, enregistrées |
| `tests/pubsub.txt`, `pubsub.expected` | Le scénario du défi Pub/Sub, et les réponses de Valkey |

Vous n'avez pas à écrire le protocole réseau. Lisez quand même `RespTest.java` : il montre les octets réellement échangés.

## Récupérer le projet

1. Ouvrez [github.com/mehdi-elke/java-lecture](https://github.com/mehdi-elke/java-lecture), bouton vert **Code** → **Download ZIP**, puis décompressez l'archive.
2. Copiez le dossier `tp/miniredis/starter` à l'endroit où vous rangez vos projets, et renommez-le `miniredis`.
3. Dans IntelliJ : **File → Open**, choisissez le dossier `miniredis`. IntelliJ reconnaît le `pom.xml` et l'ouvre comme projet Maven.

Ce dossier est **votre** projet pour les quatre séances : c'est lui que vous mettrez sur GitHub ([rendu de la séance 1](rendu-seance1.md)).

## Installation

Il vous faut :

- **JDK 25** : `java -version` doit afficher `25`.
- **Maven** : `mvn -v`. Les IDE (IntelliJ, VS Code) l'embarquent.

C'est tout : les réponses de Valkey sont enregistrées dans le projet (`tests/s1.expected` … `s4.expected`), et un client est fourni (`tools/Cli.java`). Le vrai Valkey est **facultatif** (voir « 🔵 Option : le vrai Valkey » plus bas).

## Les trois commandes à connaître

Lancez-les depuis le dossier de votre projet `miniredis`, par exemple dans le terminal d'IntelliJ (**View → Tool Windows → Terminal**, ou ⌥F12 / Alt+F12).

**1. Lancer votre serveur** (port 6380) : bouton ▶ de l'IDE sur `Server.main`, ou

```bash
mvn compile exec:java
```

Après chaque modification du code, arrêtez le serveur (■) et relancez-le.

Jusqu'à la séance 4, votre serveur ne sert **qu'un client à la fois** : quittez `Cli` avant de lancer `Compare`, sinon `Compare` attend et affiche `(pas de réponse après 3 s)`.

**2. Parler à votre serveur** avec le client fourni, dans un second terminal :

```bash
java tools/Cli.java                              # mode interactif sur votre serveur (localhost:6380)
java tools/Cli.java SET plat "pâtes fraîches"    # une seule commande
```

```
localhost:6380> SET nom Ada
OK
localhost:6380> GET nom
"Ada"
```

Pour quitter : `quit` ou Ctrl+D. Les guillemets, `\"` et `\r\n` à l'intérieur des guillemets, et les accents sont envoyés correctement. `Cli` affiche les réponses comme le vrai `valkey-cli`.

`nc localhost 6380` sert seulement à envoyer des octets bruts (séance 3) : il ne comprend pas les guillemets, et les réponses s'affichent brutes (`+OK`, `$3` puis la valeur).

**3. Comparer votre serveur à Valkey** :

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

`Compare` envoie chaque commande de `tests/s1.txt` à votre serveur et compare la réponse à celle de Valkey, enregistrée dans `tests/s1.expected`. Il signale d'abord que Valkey n'est pas lancé : c'est normal.

Chaque ligne `✗` est une tâche. Votre objectif : `0 différents`.
Un `✗` en entraîne parfois d'autres plus bas (une clé mal remplie fausse les commandes suivantes) : corrigez toujours le premier d'abord. Si votre serveur n'est pas lancé, `Compare` le dit et s'arrête.

Pour un cas qui n'est pas dans `s1.txt` (une ligne que vous y ajoutez est ignorée, faute de réponse enregistrée) : testez-le dans `CommandProcessorTest`, avec la réponse de la [spécification](specification.md).

## 🔵 Option : le vrai Valkey

Facultatif. Utile pour interroger Valkey sur un cas absent de la spécification, ou pour voir le vrai `valkey-cli` parler à votre serveur.

**Avec Docker** :

```bash
docker run -d --rm --name ref-valkey -p 6379:6379 valkey/valkey:8   # Valkey sur le port 6379
docker exec -it ref-valkey valkey-cli                                # parler à Valkey
docker exec -it ref-valkey valkey-cli -h host.docker.internal -p 6380   # parler à votre serveur
docker stop ref-valkey                                               # l'arrêter
```

`valkey-cli` n'existe que dans le conteneur : taper `valkey-cli` directement dans votre terminal donne `command not found`. Sous Linux, remplacez `host.docker.internal` par l'adresse IP de votre machine, ou lancez le conteneur avec `--network host`.

**Sans Docker** : `brew install valkey` sur macOS, `sudo apt install valkey` sur Linux si votre distribution le propose, ou dans WSL sous Windows. Lancez `valkey-server` (port 6379), puis `valkey-cli` et `valkey-cli -p 6380` fonctionnent directement.

`java tools/Cli.java --server localhost:6379` parle aussi à Valkey.

Quand Valkey écoute sur le port 6379, `Compare` l'interroge en direct au lieu de lire `tests/s1.expected`. Le résultat est le même, mais vous pouvez alors écrire votre propre fichier de commandes : le format est expliqué en tête de `Compare.java`.

## Les niveaux

Chaque énoncé propose trois niveaux. Avancez à votre rythme.

- 🟢 **Socle** : ce que tout le monde doit faire. C'est la base de la note.
- 🔵 **Approfondissement** : pour aller plus loin, une fois le socle terminé.
- 🟣 **Bonus** : pour ceux qui ont déjà de l'expérience. En plus des bonus de chaque séance, un **[défi long, Pub/Sub](defi-pubsub.md)** court sur les séances 2 à 4.
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
