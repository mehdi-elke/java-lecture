# Séance 1 — Premières commandes

**Objectif** : à la fin de la séance, `java tools/Compare.java tests/s1.txt` affiche le moins de `✗` possible dans les sections 🟢.

**Vous travaillez dans** : `CommandProcessor.java` et `CommandProcessorTest.java`. Seules exceptions : deux petites classes utilitaires en 🔵 et 🟣 (§5 et §7), dont le nom est imposé.

**Cours utile** : support de la séance 1, §3.2 (`switch` en expression), §3.5 (méthodes `static`), §3.8 (lire une `List` et une `Map`), §4.6 (`Long.parseLong`), §4.7 (UTF-8), §5 (Maven et JUnit).

---

## 0. 🟢 Prise en main (25 min)

1. Ouvrez votre dossier `miniredis` (la copie de `tp/miniredis/starter`, voir la [présentation](00-presentation.md)) dans votre IDE comme projet Maven.
2. Lancez `mvn test` : les tests fournis passent. Les lignes `WARNING: ... sun.misc.Unsafe ...` affichées par Maven sont normales avec Java 25 : ce ne sont pas des erreurs. Seul `BUILD FAILURE` en est une.
3. Mettez le projet sous Git tout de suite : dans IntelliJ, **VCS → Enable Version Control Integration → Git**, puis un premier commit « Projet de départ » (⌘K ou Ctrl+K). En terminal : `git init`, `git add .`, `git commit -m "Projet de départ"`. Vous commiterez ensuite après chaque commande.
4. Lancez votre serveur : ▶ sur `Server.main` (voir la [présentation](00-presentation.md), « Les trois commandes à connaître »).
5. Dans le terminal d'IntelliJ, connectez-vous à votre serveur avec `java tools/Cli.java`, tapez `SET nom Ada` puis `GET nom`. Regardez la console de votre serveur : chaque commande reçue s'y affiche.
6. Tapez `PING`. Votre serveur répond une erreur : `PING` n'existe pas encore. Quittez `Cli` avec `quit`.
7. Lancez `java tools/Compare.java tests/s1.txt` et lisez le résultat. Le message « Valkey non utilisé » est normal : `Compare` lit les réponses de Valkey enregistrées dans `tests/s1.expected`.

Lisez ensuite `CommandProcessor.java` en entier. Repérez :

- le `switch` qui choisit la méthode selon le nom de la commande ;
- `set(...)` et `get(...)` : le modèle à suivre pour chaque nouvelle commande ;
- `wrongNumberOfArguments(...)` : l'erreur à renvoyer quand le nombre d'arguments est faux.

Les données sont dans une `Map<String, String>` : la clé Redis, et sa valeur.

## Méthode pour chaque commande

1. **Cherchez ce que répond Valkey**, cas normaux et cas d'erreur : la [spécification](specification.md), ou `tests/s1.expected` (la commande, `=>`, la réponse exacte). Si vous avez installé Valkey (facultatif) : demandez-lui.
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

Pour savoir quel type de `Reply` utiliser, regardez comment la réponse s'affiche (dans `Cli`, `Compare`, la spécification ou `valkey-cli`, c'est le même affichage) :

| Affiché | Vous renvoyez |
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

Écrivez cette méthode, dans une nouvelle classe (signature imposée) :

```java
package miniredis.util;

public final class Integers {
    /** L'entier écrit dans text, ou une exception s'il n'est pas écrit de façon canonique. */
    public static long parseStrict(String text) { ... }
}
```

Elle n'accepte que l'écriture « canonique » d'un entier : pas de `+`, pas de zéro inutile en tête, pas d'espace, pas de `-0`, rien qui dépasse un `long`. Sinon, elle **lance une `NumberFormatException`**, que vos commandes transforment en `ERR value is not an integer or out of range`. Les cas sont dans la section 🔵 de `tests/s1.txt`.

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

Écrivez le filtrage **à la main**, sans expression régulière, dans cette classe (signature imposée) :

```java
package miniredis.util;

public final class Glob {
    /** true si text correspond au motif pattern. */
    public static boolean matches(String pattern, String text) { ... }
}
```

Indice : une méthode récursive est la solution la plus courte pour `*`.

Allez plus loin avec `[^a]` (tout sauf a) et `\*` (une vraie étoile), en comparant avec Valkey.

---

## Avant de partir

- [ ] `mvn test` passe.
- [ ] `java tools/Compare.java tests/s1.txt` : les sections 🟢 sont toutes `✓`.
- [ ] Un test JUnit par commande.
- [ ] Tout est commité.

Ce qui est attendu pour le rendu, et comment mettre votre projet sur GitHub : [rendu de la séance 1](rendu-seance1.md).
