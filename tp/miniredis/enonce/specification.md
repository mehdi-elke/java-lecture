# TP Mini-Redis — Spécification des commandes

**La référence reste Valkey** : en cas de doute, c'est sa réponse qui compte. Ce document en est la version écrite, pour travailler sans Valkey ou pour vérifier une règle d'un coup d'œil.

Toutes les réponses ci-dessous ont été **obtenues sur Valkey 8** et sont recopiées à l'identique. Les mêmes se trouvent dans `tests/s1.expected` à `tests/s4.expected`, que `Compare` utilise automatiquement quand Valkey n'est pas joignable.

---

## Règles générales

**Lire les réponses.** Elles sont écrites comme les affiche `valkey-cli` (et `Compare`) :

| Affichage | Type de réponse (`Reply`) | Exemple |
|---|---|---|
| `OK`, `PONG` | `Simple` : un texte court | `SET` réussi |
| `(error) ERR …` | `Error` : le message, au caractère près | mauvais nombre d'arguments |
| `(integer) 3` | `Int` : un nombre entier | `EXISTS`, `INCR` |
| `"bonjour"` | `Bulk` : une valeur | `GET`, `ECHO` |
| `(nil)` | `Nil` : pas de valeur | `GET` sur une clé absente |
| `["a", (nil), "b"]` | `Array` : une liste de réponses | `MGET`, `LRANGE` |

**Nom des commandes** : insensible à la casse. `set`, `SET` et `Set` sont la même commande. Les **clés** et les **valeurs**, elles, sont sensibles à la casse.

**Mauvais nombre d'arguments** : le nom de la commande en **minuscules**, entre apostrophes.

```text
GET
=> (error) ERR wrong number of arguments for 'get' command
```

**Commande inconnue** : le nom tel que tapé, puis chaque argument entre apostrophes **suivi d'un espace** (y compris le dernier).

```text
FOO bar baz
=> (error) ERR unknown command 'FOO', with args beginning with: 'bar' 'baz' 
```

**Entiers** : un argument entier invalide donne toujours la même erreur.

```text
=> (error) ERR value is not an integer or out of range
```

Valkey est **strict** (🔵) : il refuse `+5`, `007`, `" 5"` (espace), `5x`, `-0` et tout nombre qui ne tient pas dans un `long`. Il accepte `-3`.

**Une clé n'a qu'un type** (séance 2) : une commande appliquée à une clé du mauvais type répond :

```text
=> (error) WRONGTYPE Operation against a key holding the wrong kind of value
```

---

## Séance 1 — Chaînes

| Commande | Effet | Réponse |
|---|---|---|
| `SET clé valeur` | enregistre la valeur (remplace l'ancienne) | `OK` |
| `GET clé` | lit la valeur | `"valeur"`, ou `(nil)` si absente |
| `PING` | teste la connexion | `PONG` |
| `PING message` | renvoie le message | `"message"` |
| `ECHO message` | renvoie le message | `"message"` |
| `EXISTS k1 k2 …` | compte les clés présentes ; **une clé répétée compte à chaque fois** | `(integer) n` |
| `DEL k1 k2 …` | supprime ; compte les clés **réellement** supprimées | `(integer) n` |
| `STRLEN clé` | longueur de la valeur, en **octets** (🔵 `é` vaut 2) | `(integer) n`, `0` si absente |
| `APPEND clé suffixe` | ajoute à la fin ; crée la clé si absente | `(integer)` nouvelle longueur |
| `MGET k1 k2 …` | lit plusieurs clés | un tableau, `(nil)` pour chaque clé absente |
| `INCR clé` / `DECR clé` | ajoute / retire 1 ; une clé absente vaut 0 | `(integer)` nouvelle valeur |
| `INCRBY clé n` / `DECRBY clé n` | ajoute / retire `n` | `(integer)` nouvelle valeur |
| 🟣 `KEYS motif` | les clés qui correspondent au motif | un tableau (ordre libre) |

Nombre d'arguments : `PING` en accepte 0 ou 1 ; `ECHO`, `GET`, `STRLEN`, `INCR`, `DECR`, `KEYS` exactement 1 ; `SET`, `APPEND`, `INCRBY`, `DECRBY` exactement 2 ; `EXISTS`, `DEL`, `MGET` au moins 1.

**Les pièges, avec les réponses de Valkey** :

```text
SET a 1
SET b 2
EXISTS a a absente b
=> (integer) 3                  a compte deux fois
DEL a absente
=> (integer) 1                  seule a existait
DEL a
=> (integer) 0                  a n'existe plus

INCRBY compteur 10              (compteur valait 1)
=> (integer) 11
GET compteur
=> "8"                          après DECRBY compteur 3 : la valeur est stockée en texte

SET mot abc
INCR mot
=> (error) ERR value is not an integer or out of range
INCRBY compteur abc
=> (error) ERR value is not an integer or out of range
```

🔒 **Débordement** : une valeur qui sortirait d'un `long` est refusée, la clé ne change pas.

```text
SET max 9223372036854775807
INCR max
=> (error) ERR increment or decrement would overflow
SET min -9223372036854775808
DECR min
=> (error) ERR increment or decrement would overflow
```

🟣 Un seul cas a un message différent : `DECRBY` d'un pas égal à `Long.MIN_VALUE`. Valkey calcule `DECRBY n` comme `INCRBY -n`, et `-(-9223372036854775808)` n'existe pas dans un `long`.

```text
SET x 5
DECRBY x -9223372036854775808
=> (error) ERR decrement would overflow
```

🔵 **Octets** :

```text
SET accent é
STRLEN accent
=> (integer) 2
APPEND accent té
=> (integer) 5
GET accent
=> "été"
```

🟣 **Motifs de `KEYS`** : `*` = n'importe quelle suite de caractères (même vide), `?` = exactement un caractère, `[abc]` = un caractère parmi ceux-là.

```text
(clés : user:1, user:2, user:10, admin)
KEYS user:*       => ["user:1", "user:10", "user:2"]
KEYS user:?       => ["user:1", "user:2"]
KEYS *1*          => ["user:1", "user:10"]
KEYS [au]*        => ["admin", "user:1", "user:10", "user:2"]
KEYS user:[12]    => ["user:1", "user:2"]
KEYS nothing*     => []
```

---

## Séance 2 — Listes, types, expiration, ensembles

| Commande | Effet | Réponse |
|---|---|---|
| `RPUSH clé v1 v2 …` | ajoute à droite (à la fin) | `(integer)` nouvelle longueur |
| `LPUSH clé v1 v2 …` | ajoute à gauche, **un par un** : `LPUSH l z y` donne `y, z, …` | `(integer)` nouvelle longueur |
| `LRANGE clé début fin` | les éléments de `début` à `fin` **inclus** | un tableau, `[]` si absente |
| `LLEN clé` | longueur | `(integer) n`, `0` si absente |
| `LPOP clé` / `RPOP clé` | retire et renvoie le premier / le dernier | `"valeur"`, `(nil)` si absente |
| `TYPE clé` | le type | `string`, `list`, `set` ou `none` |
| 🔵 `EXPIRE clé secondes` | fixe une durée de vie | `(integer) 1`, ou `0` si la clé est absente |
| 🔵 `TTL clé` | secondes restantes | `(integer) n` ; `-1` sans expiration ; `-2` si absente |
| 🔵 `PERSIST clé` | retire l'expiration | `(integer) 1`, ou `0` s'il n'y en avait pas |
| 🔵 `SET clé valeur EX secondes` | `SET` avec durée de vie | `OK` |
| 🟣 `SADD clé m1 m2 …` | ajoute à l'ensemble (sans doublon) | `(integer)` nombre de membres **nouveaux** |
| 🟣 `SMEMBERS clé` | tous les membres | un tableau (ordre libre) |
| 🟣 `SISMEMBER clé membre` | appartenance | `(integer) 1` ou `0` |
| 🟣 `SCARD clé` | nombre de membres | `(integer) n`, `0` si absente |

Nombre d'arguments : `LLEN`, `LPOP`, `RPOP`, `TYPE`, `TTL`, `PERSIST`, `SMEMBERS`, `SCARD` exactement 1 ; `EXPIRE`, `SISMEMBER` exactement 2 ; `LRANGE` exactement 3 ; `RPUSH`, `LPUSH`, `SADD` au moins 2 (la clé et une valeur). Valkey accepte un compte facultatif pour `LPOP`/`RPOP` (`LPOP clé 3`) : il n'est pas demandé.

**Indices de `LRANGE`** : `0` est le premier, `-1` le dernier, `-2` l'avant-dernier. Un indice trop grand est ramené au bout de la liste ; si `début` dépasse `fin`, la réponse est vide.

```text
(liste : y, z, a, b, c)
LRANGE liste 0 -1      => ["y", "z", "a", "b", "c"]
LRANGE liste 1 2       => ["z", "a"]
LRANGE liste -2 -1     => ["b", "c"]
LRANGE liste 0 100     => ["y", "z", "a", "b", "c"]
LRANGE liste 5 10      => []
LRANGE liste -100 1    => ["y", "z"]
LRANGE liste 3 1       => []
LRANGE liste 0 x       => (error) ERR value is not an integer or out of range
```

**Une liste vidée disparaît** : après le dernier `LPOP`, `EXISTS` répond `0` et `TYPE` répond `none`.

**Types** : `GET`, `INCR`, `APPEND`, `STRLEN` sur une liste, ou `RPUSH`, `LLEN`, `LRANGE` sur une chaîne, répondent `WRONGTYPE`. Deux exceptions :
- `MGET` ne lève jamais `WRONGTYPE` : une clé d'un autre type donne `(nil)`.
- `SET` remplace n'importe quelle clé, quel que soit son type : `TYPE` répond ensuite `string`.

🔵 **Expiration** :

```text
SET session abc
TTL session                  => (integer) -1
EXPIRE session 100           => (integer) 1
TTL session                  => (integer) 100
SET session2 abc EX 100
SET session2 nouvelle        => OK            un SET retire l'expiration
TTL session2                 => (integer) -1
SET session3 abc EX 0        => (error) ERR invalid expire time in 'set' command
SET session3 abc EX x        => (error) ERR value is not an integer or out of range
SET session3 abc FOO         => (error) ERR syntax error
EXPIRE session -1            => (integer) 1   une durée négative supprime la clé
EXISTS session               => (integer) 0
```

Une clé expirée se comporte **exactement** comme une clé absente : `GET` → `(nil)`, `EXISTS` → `0`, `TTL` → `-2`, et elle n'apparaît ni dans `KEYS` ni dans `DBSIZE`.

`TTL` arrondit à la seconde **la plus proche** : juste après `EXPIRE session 100`, il reste 99,998 s, et `TTL` répond `100`, pas `99`.

---

## Séance 3 — Parcourir et sauvegarder

| Commande | Effet | Réponse |
|---|---|---|
| `DBSIZE` | nombre de clés (sans argument) | `(integer) n` |
| `KEYS motif` | voir séance 1 ; doit aussi trouver les listes | un tableau |
| `SAVE` | écrit les données sur le disque (sans argument) | `OK` |
| `INFO` | des informations sur le serveur (arguments ignorés) | un texte (`Bulk`) |

`MGET` sur un mélange de chaînes, de listes et de clés absentes : `(nil)` pour tout ce qui n'est pas une chaîne.

```text
MGET user:1 absente user:liste user:2
=> ["a", (nil), (nil), "b"]
```

Le rechargement des données se vérifie en arrêtant puis en relançant votre serveur : les clés doivent toujours être là.

`DBSIZE x` et `SAVE x` : `ERR wrong number of arguments for 'dbsize' command` (resp. `'save'`).

**`INFO`** : le format est imposé par l'énoncé de la séance 3 (§3), car Valkey en affiche des centaines de lignes. Seule la ligne `# Keyspace` est identique à Valkey :

```text
DBSIZE                     => (integer) 1
INFO keyspace              => (extrait) db0:keys=1,expires=0,avg_ttl=0
```

**Erreurs du serveur** (pas de Valkey pour ces cas : ce sont les messages imposés par l'énoncé) :

| Situation | Réponse |
|---|---|
| bug interne (une `RuntimeException` dans une commande) | `(error) ERR internal error` |
| `SAVE` qui échoue (disque plein, dossier interdit…) | `(error) ERR saving failed, see server logs` |

**Erreurs de protocole** : le serveur répond, puis **ferme la connexion**. Elles ne se testent pas avec `valkey-cli`, qui envoie toujours des commandes bien formées : envoyez les octets vous-même (`printf '*abc\r\n' | nc localhost 6380`, ou un test JUnit sur `RespReader`).

| Octets reçus | Réponse |
|---|---|
| `*abc\r\n` | `-ERR Protocol error: invalid multibulk length` |
| `*1\r\n$abc\r\n` | `-ERR Protocol error: invalid bulk length` |
| `*1\r\n+PING\r\n` | `-ERR Protocol error: expected '$', got '+'` |

---

## Séance 4 — `SET` conditionnel

| Commande | Effet | Réponse |
|---|---|---|
| `SET clé valeur NX` | seulement si la clé **n'existe pas** | `OK`, ou `(nil)` si elle existait (rien n'est modifié) |
| `SET clé valeur XX` | seulement si la clé **existe** | `OK`, ou `(nil)` si elle n'existait pas |
| `SETNX clé valeur` | comme `SET … NX` | `(integer) 1` si écrit, `0` sinon |

Les options se combinent dans n'importe quel ordre (`EX 100 NX` ou `NX EX 100`), mais `NX` et `XX` ensemble sont refusés :

```text
SET verrou a NX          => OK
SET verrou b NX          => (nil)
GET verrou               => "a"
SET autre c XX           => (nil)
SET verrou d NX XX       => (error) ERR syntax error
SET nouveau e NX EX 100  => OK
TTL nouveau              => (integer) 100
SETNX verrou f           => (integer) 0
SETNX libre f            => (integer) 1
SETNX verrou             => (error) ERR wrong number of arguments for 'setnx' command
```

## Séance 4 — Limites, `AUTH`, `CONFIG`

**Limites du protocole** : les limites sont celles de l'énoncé (bien plus basses que celles de Valkey) ; les messages sont ceux de Valkey. Le serveur répond, puis ferme la connexion.

| Dépassement | Réponse |
|---|---|
| chaîne annoncée de plus de 512 Ko (`$2000000000`) | `-ERR Protocol error: invalid bulk length` |
| plus de 1 024 arguments annoncés (`*1000000`) | `-ERR Protocol error: invalid multibulk length` |
| ligne inline de plus de 64 Ko sans fin de ligne | `-ERR Protocol error: too big inline request` |

🟣 🔒 **`AUTH`** (si `MINIREDIS_PASSWORD` vaut `secret`) :

```text
GET x                    => (error) NOAUTH Authentication required.
AUTH faux                => (error) WRONGPASS invalid username-password pair or user is disabled.
AUTH secret              => OK
AUTH default secret      => OK               « default » est le nom de l'utilisateur unique
AUTH inconnu secret      => (error) WRONGPASS invalid username-password pair or user is disabled.
AUTH                     => (error) ERR wrong number of arguments for 'auth' command
AUTH a b c               => (error) ERR syntax error
```

Sans mot de passe configuré, `AUTH secret` répond :

```text
(error) ERR AUTH <password> called without any password configured for the default user. Are you sure your configuration is correct?
```

🟣 🔒 **`CONFIG`** : `CONFIG GET` répond un tableau `[nom, valeur]`, ou `[]` pour un paramètre inconnu.

```text
CONFIG GET dir                    => ["dir", "/chemin/absolu/vers/data"]
CONFIG GET dbfilename             => ["dbfilename", "dump.resp"]
CONFIG GET inconnu                => []
CONFIG GET                        => (error) ERR wrong number of arguments for 'config|get' command
CONFIG SET dir /tmp               => (error) ERR CONFIG SET failed (possibly related to argument 'dir') - can't set protected config
CONFIG SET dbfilename ../x        => (error) ERR CONFIG SET failed (possibly related to argument 'dbfilename') - dbfilename can't be a path, just a filename
CONFIG SET dbfilename autre.resp  => OK
CONFIG SET inconnu 1              => (error) ERR Unknown option or number of arguments for CONFIG SET - 'inconnu'
CONFIG FOO bar                    => (error) ERR unknown subcommand 'FOO'. Try CONFIG HELP.
```

Ici, **le TP s'écarte de Valkey 8**, qui refuse aussi `CONFIG SET dbfilename` (paramètre protégé) : nous l'acceptons pour un nom de fichier simple, afin que vous écriviez la vérification de chemin (support S3, §5.5). Le message pour un chemin est celui des anciennes versions de Redis.

---

## Une réponse qui n'est pas ici ?

1. Si vous avez Valkey : demandez-lui (`valkey-cli`).
2. Sinon, cherchez la commande dans `tests/sN.expected` : chaque ligne `=> …` est la réponse exacte de Valkey à la commande au-dessus.

Avec `nc` plutôt que `valkey-cli` : les guillemets marchent (`SET "a b" "c d"`), mais les réponses arrivent brutes (`+OK`, `$3`, `c d`), et les accents dépendent de l'encodage du terminal. Pour vérifier une réponse au caractère près, préférez un test JUnit.
3. Sinon, la documentation officielle : [valkey.io/commands](https://valkey.io/commands/). Elle décrit chaque commande et ses réponses.
