# Séance 1 — Premières commandes

**Objectif** : à la fin de la séance, `java tools/Compare.java tests/s1.txt` affiche le moins de `✗` possible dans les sections 🟢.

**Vous travaillez dans** : `CommandProcessor.java` et `CommandProcessorTest.java`. Rien d'autre.

**Cours utile** : support de la séance 1, §3.2 (`switch` en expression), §3.5 (méthodes `static`), §3.8 (lire une `List` et une `Map`), §4.6 (`Long.parseLong`), §4.7 (UTF-8), §5 (Maven et JUnit).

---

## 0. 🟢 Prise en main (25 min)

1. Ouvrez le dossier `starter/` dans votre IDE comme projet Maven.
2. Lancez `mvn test` : les tests fournis passent.
3. Lancez le Valkey de référence et votre serveur (voir la [présentation](00-presentation.md)).
4. Connectez-vous à votre serveur avec `valkey-cli`, tapez `SET nom Ada` puis `GET nom`. Regardez la console de votre serveur : chaque commande reçue s'y affiche.
5. Tapez `PING`. Votre serveur répond une erreur : `PING` n'existe pas encore.
6. Lancez `java tools/Compare.java tests/s1.txt` et lisez le résultat.

Lisez ensuite `CommandProcessor.java` en entier. Repérez :

- le `switch` qui choisit la méthode selon le nom de la commande ;
- `set(...)` et `get(...)` : le modèle à suivre pour chaque nouvelle commande ;
- `wrongNumberOfArguments(...)` : l'erreur à renvoyer quand le nombre d'arguments est faux.

Les données sont dans une `Map<String, String>` : la clé Redis, et sa valeur.

## Méthode pour chaque commande

1. **Demandez à Valkey** ce qu'il répond, cas normaux et cas d'erreur (`valkey-cli` sur le port 6379).
2. **Écrivez le test** dans `CommandProcessorTest`, avec la réponse exacte de Valkey :
   ```java
   @Test
   void echoReturnsItsArgument() {
       assertEquals(new Reply.Bulk("salut"), run("ECHO", "salut"));
   }
   ```
3. **Ajoutez un `case`** dans le `switch`, et une méthode privée.
4. `mvn test`, puis relancez le serveur et `Compare`.
5. Commit.

Pour savoir quel type de `Reply` utiliser, regardez comment `valkey-cli` affiche la réponse :

| `valkey-cli` affiche | Vous renvoyez |
|---|---|
| `PONG` ou `OK` (sans guillemets) | `new Reply.Simple("PONG")` ou `Reply.OK` |
| `"salut"` (avec guillemets) | `new Reply.Bulk("salut")` |
| `(integer) 3` | `new Reply.Int(3)` |
| `(nil)` | `Reply.NIL` |
| `(error) ERR ...` | `new Reply.Error("ERR ...")` |
| `1) "a"` `2) "b"` | `new Reply.Array(List.of(...))` |

## 1. 🟢 `PING` et `ECHO` (15 min)

- `PING` répond `PONG`. `PING message` répond le message.
- `ECHO message` répond le message.
- Testez aussi les mauvais nombres d'arguments : `PING a b`, `ECHO`, `ECHO a b`.

## 2. 🟢 `DEL` et `EXISTS` (20 min)

- `EXISTS a b c` répond le nombre de clés qui existent.
- `DEL a b c` supprime les clés et répond le nombre de clés **réellement** supprimées.
- Piège : demandez à Valkey ce que répond `EXISTS a a` quand `a` existe, puis `DEL a a`. Ce n'est pas la même logique.

## 3. 🟢 `STRLEN`, `APPEND`, `MGET` (25 min)

- `STRLEN clé` : la longueur de la valeur. Et pour une clé absente ?
- `APPEND clé suffixe` : ajoute à la fin et répond la nouvelle longueur. Si la clé n'existe pas, elle est créée.
- `MGET k1 k2 k3` : un tableau avec une réponse par clé, `(nil)` pour une clé absente.

## 4. 🟢 `INCR`, `DECR`, `INCRBY`, `DECRBY` (40 min)

- `INCR compteur` ajoute 1 et répond la nouvelle valeur. Une clé absente vaut 0.
- `INCRBY compteur 10` ajoute 10. `DECR` et `DECRBY` retirent.
- Les valeurs sont stockées comme des chaînes : après `INCR compteur`, `GET compteur` répond `"1"`.
- Erreurs à reproduire : une valeur qui n'est pas un nombre (`SET mot abc` puis `INCR mot`), un pas qui n'est pas un nombre (`INCRBY compteur abc`).

🔒 **Le débordement.** Un `long` va jusqu'à 9 223 372 036 854 775 807. Essayez sur Valkey :

```
SET max 9223372036854775807
INCR max
```

Valkey refuse. Que fait votre code ? En Java, `Long.MAX_VALUE + 1` donne un nombre **négatif**, sans aucune erreur (support S1, §2.6). Un compteur de crédits ou de stock qui passe silencieusement de « très grand » à « très négatif » est une faille classique. Trouvez la méthode de `Math` qui lève une exception au lieu de déborder, et répondez comme Valkey.

Évitez d'écrire quatre fois le même code : les quatre commandes peuvent partager une seule méthode.

## 5. 🔵 Lire un entier aussi strictement que Valkey (30 min) 🔒

`Long.parseLong` accepte `"+5"` et `"007"`. Valkey les refuse :

```
INCRBY compteur +5     → (error) ERR value is not an integer or out of range
INCRBY compteur 007    → (error) ERR value is not an integer or out of range
```

Écrivez une méthode `static long parseStrict(String text)` qui n'accepte que l'écriture « canonique » d'un entier : pas de `+`, pas de zéro inutile en tête, pas d'espace, pas de `-0`. Les cas sont dans la section 🔵 de `tests/s1.txt`.

Pourquoi c'est de la sécurité : deux composants qui ne lisent pas une entrée de la même façon (votre code et un pare-feu applicatif, par exemple) laissent passer ce que l'un des deux aurait dû bloquer. **On valide une entrée selon une règle stricte et explicite, pas selon ce que la bibliothèque tolère.**

Testez avec un test paramétré (support S1, §5.7).

## 6. 🔵 Octets ou caractères ? (15 min)

```
SET accent é
STRLEN accent
```

Valkey répond 2, pas 1 : Redis compte des **octets**, et `é` occupe 2 octets en UTF-8 (support S1, §4.7). Corrigez `STRLEN` et `APPEND`.

## 7. 🟣 `KEYS motif` (bonus)

`KEYS user:*` renvoie toutes les clés qui commencent par `user:`. Le motif accepte :

- `*` : n'importe quelle suite de caractères, même vide ;
- `?` : exactement un caractère ;
- `[abc]` : un caractère parmi a, b, c ;
- `[a-z]` : un caractère de a à z.

Écrivez le filtrage **à la main**, sans expression régulière. Indice : une méthode récursive est la solution la plus courte pour `*`.

Allez plus loin avec `[^a]` (tout sauf a) et `\*` (une vraie étoile), en comparant avec Valkey.

---

## Avant de partir

- [ ] `mvn test` passe.
- [ ] `java tools/Compare.java tests/s1.txt` : les sections 🟢 sont toutes `✓`.
- [ ] Un test JUnit par commande.
- [ ] Tout est commité.
