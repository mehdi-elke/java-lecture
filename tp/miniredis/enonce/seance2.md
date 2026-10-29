# Séance 2 — Du `switch` aux objets

**Objectif** : réorganiser le code en objets, sans rien casser, puis ajouter les listes et l'expiration des clés.

**Cours utile** : support de la séance 2, §2.4 (`record`), §3.7 (`Instant`, `Duration`, `Clock`), §4.3 (`abstract` et polymorphisme), §4.8 (le patron de méthode), §5 (interfaces, `instanceof`, `sealed`), §6.5 (`Map`), §6.9 (copie défensive), §6.12 (`Deque`), §7 (`Optional`).

**Rendu** : jalon 1 à la fin de la séance (dépôt Git), évalué par des tests cachés et par `Compare` sur `s1.txt` et `s2.txt`.

---

## Le problème

Votre `CommandProcessor` fait maintenant plusieurs centaines de lignes : un `switch` géant et quinze méthodes privées. Chaque méthode recommence la même vérification du nombre d'arguments. Et il faut maintenant ajouter des listes : `GET` sur une liste doit répondre une erreur, `LLEN` sur une chaîne aussi. Avec une `Map<String, String>`, impossible.

## Le filet de sécurité

Vos tests de la séance 1 appellent tous `processor.execute(List.of(...))`. **Cette signature ne change pas.** Pendant toute la réorganisation, lancez `mvn test` après chaque étape : si un test casse, vous savez exactement quelle étape l'a cassé.

## Signatures imposées

Respectez-les à la lettre : les tests de correction et le code fourni en séance 4 en dépendent.

```java
package miniredis.command;

/** Une commande : un nom, et ce qu'elle fait sur le Store. */
public interface Command {
    String name();                                      // en majuscules : "GET"
    Reply execute(List<String> args, Store store);      // args : sans le nom de la commande
}
```

```java
package miniredis.command;

public abstract class AbstractCommand implements Command {
    protected AbstractCommand(String name, int minArgs, int maxArgs) { ... }
    public final String name() { ... }
    /** Vérifie le nombre d'arguments, puis appelle doExecute. */
    public final Reply execute(List<String> args, Store store) { ... }
    protected abstract Reply doExecute(List<String> args, Store store);
}
```

```java
package miniredis.store;

public sealed interface Value permits StringValue, ListValue {
    String type();                                      // "string", "list" : ce que répond TYPE
}
public record StringValue(String value) implements Value { ... }
public final class ListValue implements Value {
    public void pushLeft(String item);
    public void pushRight(String item);
    public Optional<String> popLeft();
    public Optional<String> popRight();
    public int size();
    public List<String> range(long start, long stop);  // règles de LRANGE, renvoie une copie
    public List<String> items();                        // tous les éléments, copie
}
```

```java
package miniredis.store;

public final class Store {
    public Store();                         // horloge système : Clock.systemUTC()
    public Store(Clock clock);              // horloge choisie : pour les tests
    public Optional<Value> get(String key);
    public void put(String key, Value value);
    public boolean remove(String key);      // true si la clé existait
    public boolean exists(String key);
    public List<String> keys();             // copie
    public int size();
    public void clear();
    public Instant now();
    public boolean expireAt(String key, Instant when);   // false si la clé n'existe pas
    public boolean persist(String key);                  // true si une expiration a été retirée
    public Optional<Instant> expiration(String key);
}
```

## 1. 🟢 Réorganiser (1 h)

Avancez par petites étapes, `mvn test` à chaque fois :

1. Créez `Store` avec une `Map<String, Value>` et `StringValue`. Faites-le utiliser par `CommandProcessor` à la place de la `Map<String, String>`.
2. Créez `Command` et `AbstractCommand`. La vérification du nombre d'arguments est écrite **une seule fois**, dans `execute`, qui est `final` : une sous-classe ne peut pas l'oublier ni la contourner. C'est le patron de méthode (support S2, §4.8).
3. Transformez `GET` en une classe `GetCommand extends AbstractCommand`. Dans `CommandProcessor`, remplacez son `case` par un registre :
   ```java
   private final Map<String, Command> commands = new HashMap<>();
   ```
   `execute` cherche la commande dans la `Map`. Inconnue : l'erreur `unknown command` habituelle.
4. Une fois que `GET` marche, faites de même pour les autres commandes, une par une.

À la fin, le `switch` a disparu. Ajouter une commande, c'est écrire une classe et l'enregistrer.

Question à vous poser : `INCR`, `DECR`, `INCRBY` et `DECRBY` ont-elles besoin de quatre classes ?

## 2. 🟢 Les listes (1 h)

`ListValue` stocke ses éléments dans un `ArrayDeque<String>` : ajout et retrait en temps constant aux deux bouts (support S2, §6.12).

| Commande | Réponse |
|---|---|
| `RPUSH clé a b c` | ajoute à droite, répond la nouvelle longueur. Crée la liste si besoin |
| `LPUSH clé a b c` | ajoute à gauche. Demandez à Valkey dans quel ordre ! |
| `LPOP clé`, `RPOP clé` | retire et répond l'élément, `(nil)` si la clé n'existe pas |
| `LLEN clé` | la longueur, 0 si la clé n'existe pas |
| `LRANGE clé début fin` | les éléments de début à fin **inclus** |
| `TYPE clé` | `string`, `list` ou `none` |

`LRANGE` accepte des index négatifs : `-1` est le dernier élément, `-2` l'avant-dernier. `LRANGE liste 0 -1` renvoie donc toute la liste. Testez sur Valkey les index hors limites (`0 100`, `-100 1`, `5 10`, `3 1`) avant d'écrire `range`.

Règle Redis : **une liste vidée disparaît.** Après le dernier `LPOP`, `EXISTS` répond 0.

## 3. 🟢 `WRONGTYPE` (20 min)

```
RPUSH liste a
GET liste
(error) WRONGTYPE Operation against a key holding the wrong kind of value
```

Toutes les commandes de chaîne doivent refuser une liste, et inversement. Pour tester le type, utilisez `instanceof` avec motif (support S2, §5.6) :

```java
if (value instanceof StringValue s) { ... s.value() ... }
```

Pour ne pas répéter ce test dans quinze classes, où le placer ?

🔒 **Copie défensive.** `range` et `items` renvoient des **copies**. Si `ListValue` renvoyait son `ArrayDeque` interne, n'importe quel code pourrait modifier la liste sans passer par ses méthodes (support S2, §6.9). Écrivez un test qui le vérifie.

## 4. 🔵 L'expiration des clés (45 min)

```
SET session abc
EXPIRE session 100      → (integer) 1
TTL session             → (integer) 100
PERSIST session         → (integer) 1
TTL session             → (integer) -1
SET code 1234 EX 60     → OK
```

- `EXPIRE clé secondes`, `TTL clé`, `PERSIST clé`, et l'option `SET clé valeur EX secondes`.
- `TTL` répond -1 sans expiration, -2 si la clé n'existe pas.
- Le `Store` garde une deuxième `Map<String, Instant>` : la date d'expiration de chaque clé.
- Pas de thread qui nettoie en arrière-plan : une clé expirée est supprimée **au moment où on la consulte**. C'est ce que fait Redis.
- Un `SET` sans `EX` retire l'expiration. Un `APPEND` ou un `RPUSH` la conservent. Vérifiez sur Valkey.

**Tester sans attendre.** Un test qui fait `Thread.sleep(100_000)` est inacceptable. Le `Store` reçoit un `Clock` (support S2, §3.7) : en production, l'horloge système ; dans les tests, une horloge que **vous** faites avancer. Écrivez cette horloge de test : une petite classe qui hérite de `Clock`.

Testez aussi sur Valkey : `EXPIRE session -1`, `SET k v EX 0`, `SET k v EX abc`, `SET k v FOO`.

## 5. 🟣 Les ensembles (bonus)

`SADD`, `SMEMBERS`, `SISMEMBER`, `SCARD`, avec une classe `SetValue`.

Ajoutez `SetValue` dans le `permits` de `Value`. Si vous avez écrit des `switch` sur `Value`, le compilateur vous montre maintenant chaque endroit à compléter : c'est l'intérêt de `sealed` (support S2, §5.7).

---

## Avant de partir (jalon 1)

- [ ] `mvn test` passe, **y compris tous les tests de la séance 1**.
- [ ] `java tools/Compare.java tests/s1.txt` et `tests/s2.txt` : les sections 🟢 sont toutes `✓`.
- [ ] Plus de `switch` géant : une classe par commande.
- [ ] Tout est commité et poussé.
