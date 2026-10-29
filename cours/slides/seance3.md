---
marp: true
theme: foodfast
paginate: true
title: "Java — Séance 3 : Rendre l'application intelligente et robuste"
---

<!-- _class: lead -->

# Séance 3

## Rendre l'application intelligente et robuste

Lambdas · Streams · Exceptions · Journalisation 🔒

<!--
Durée : 1 min
Durée totale prévue : environ 73 min de cours, le reste de la séance est consacré à la pratique.
Message : en S2, on a construit le modèle FoodFast. Aujourd'hui, on le fait parler (statistiques) et on lui apprend à échouer proprement.
-->

---

## Objectifs de la séance

- **AA5** : transformer et agréger des données avec les lambdas et l'API Stream
- **AA6** : gérer les erreurs avec des exceptions métier, sans masquer les erreurs ni divulguer d'information
- **Plan** : 1. Lambdas · 2. Streams · 3. Exceptions et journalisation

> Le modèle de la séance 2 sait **stocker**. Aujourd'hui, il apprend à **répondre** et à **échouer proprement**.

<!--
Durée : 2 min
Message : deux acquis évalués. Les streams servent dans toutes les applications métier ; les exceptions sont le premier endroit où l'on crée (ou évite) des failles.
Question à la salle : qui a déjà vu une page d'erreur avec une pile d'appels Java sur un site en production ?
-->

---

<!-- _class: section -->

# 1. Des interfaces aux lambdas

<!--
Durée : 0,5 min
-->

---

## 🟢 Point de départ : `Discount`

```java
interface Discount {
    BigDecimal apply(BigDecimal total);   // UNE seule méthode abstraite
}

// Nouvelle promo "happy hour" : une classe entière pour une ligne utile
class HappyHourDiscount implements Discount {
    @Override
    public BigDecimal apply(BigDecimal total) {
        return total.subtract(new BigDecimal("2.00"))
                    .max(BigDecimal.ZERO);
    }
}
```

> `Discount` ne décrit pas un objet, mais un **comportement**.

<div class="support">📖 Support §1.1 et §1.2 — Discount, une classe pour une ligne</div>

<!--
Durée : 2 min
Message : partir de ce qu'ils connaissent (S2). Une interface à une seule méthode, c'est "une fonction déguisée en objet".
Question à la salle : le marketing invente 20 promos par an. Combien de classes ?
-->

---

## 🟢 De la classe à la lambda

<div class="cols"><div>

**Classe anonyme** (avant Java 8)

```java
Discount happyHour = new Discount() {
    @Override
    public BigDecimal apply(
            BigDecimal t) {
        return t.subtract(TWO)
                .max(BigDecimal.ZERO);
    }
};
```

</div><div>

**Lambda** (Java 8 et +)

```java
Discount happyHour =
    t -> t.subtract(TWO)
          .max(BigDecimal.ZERO);
```

- On ne garde que **paramètres → corps**
- Le compilateur déduit le type de `t` et la méthode `apply`

</div></div>

<div class="support">📖 Support §1.3 et §1.4 — Classe anonyme, lambda (TWO = 2.00 €)</div>

<!--
Durée : 2 min
Message : la lambda EST un objet de type Discount. On peut la ranger dans une variable, une List<Discount>, la passer en paramètre : c'est le polymorphisme de S2.
La classe anonyme n'est montrée que pour faire le lien ; elle est détaillée dans le support.
Quand garder un record ? Quand la règle a des paramètres qui font son identité (PercentageDiscount(10)).
-->

---

## 🟢 Syntaxe d'une lambda

```java
// Aucun paramètre
Supplier<String> newId = () -> "order-" + UUID.randomUUID();
// Un paramètre : parenthèses facultatives
Discount none = t -> t;
// Deux paramètres
Comparator<BigDecimal> byAmount = (a, b) -> a.compareTo(b);
// Plusieurs instructions : accolades et return
Function<BigDecimal, String> label = t -> {
    if (t.compareTo(new BigDecimal("50")) >= 0) return "grosse";
    return "standard";
};
```

> Une lambda implémente une **interface fonctionnelle** : une interface avec **une seule** méthode abstraite.

<div class="support">📖 Support §1.5 — Syntaxe et capture de variables</div>

<!--
Durée : 2 min
Message : la forme la plus courante est "x -> expression". Les accolades sont rares en pratique : si la lambda dépasse 3 lignes, faites-en une méthode.
Mentionner @FunctionalInterface (facultatif) : le compilateur refuse une 2e méthode abstraite.
-->

---

## 🤔 Est-ce que ça compile ?

```java
int pendingCount = 0;
orders.forEach(o -> {
    if (o.getStatus() == OrderStatus.PENDING) {
        pendingCount++;
    }
});
IO.println(pendingCount);
```

<!--
Durée : 1,5 min
Question à la salle : vote à main levée. Compile / ne compile pas / compile mais affiche 0.
Laisser 30 secondes de réflexion avant de passer à la slide suivante.
-->

---

## Réponse : non, une lambda ne modifie pas une variable locale

`error: local variables referenced from a lambda expression must be final or effectively final`

- Une lambda peut **lire** une variable locale *effectivement finale*, pas la **modifier**
- Elle peut s'exécuter plus tard, ou sur un autre thread (séance 4)

```java
// ✅ Pas de bricolage (int[], AtomicInteger) : on laisse l'API compter
long pendingCount = orders.stream()
        .filter(o -> o.getStatus() == OrderStatus.PENDING)
        .count();                                         // 1
```

<div class="support">📖 Support §1.5 — Capture de variables</div>

<!--
Durée : 1,5 min
Message : la règle n'est pas un caprice du compilateur, elle protège contre des résultats imprévisibles. Et la bonne solution est déclarative : c'est l'introduction parfaite aux streams.
-->

---

## 🟢 Les interfaces fonctionnelles du JDK

| Interface | Méthode | Rôle | Exemple FoodFast |
|---|---|---|---|
| `Predicate<T>` | `boolean test(T)` | tester | commande en attente ? |
| `Function<T, R>` | `R apply(T)` | transformer | commande → total |
| `Consumer<T>` | `void accept(T)` | agir | notifier le client |
| `Supplier<T>` | `T get()` | fournir | nouvel identifiant |
| `Comparator<T>` | `int compare(T, T)` | comparer | trier par total |

> Inutile d'inventer vos interfaces : `java.util.function` en fournit une quarantaine.

<div class="support">📖 Support §1.6 — Les interfaces fonctionnelles du JDK</div>

<!--
Durée : 2 min
Message : lire Function<Order, BigDecimal> comme "une fonction qui prend une Order et rend un BigDecimal". Les <T> sont les génériques vus en S2 (List<Dish>).
Remarque : Discount est en fait un UnaryOperator<BigDecimal>, mais notre nom dit ce qu'il signifie dans le métier.
-->

---

## 🟢 Les interfaces fonctionnelles sur FoodFast

```java
Predicate<Order> isPending = o -> o.getStatus() == OrderStatus.PENDING;
Function<Order, BigDecimal> toTotal = o -> o.getTotal();
Consumer<Order> notify =
        o -> IO.println("Notification envoyée pour " + o.getStatus());
Comparator<Order> byTotal =
        (a, b) -> a.getTotal().compareTo(b.getTotal());

List<Order> orders = new ArrayList<>(platform.getOrders());
orders.sort(byTotal);                 // List.sort attend un Comparator
Order cheapest = orders.getFirst();
IO.println(toTotal.apply(cheapest));  // 9.80
IO.println(isPending.test(cheapest)); // true
```

<div class="support">📖 Support §1.6 — Exemple complet et composition (and, negate, thenComparing)</div>

<!--
Durée : 2 min
Message : ces objets se passent en paramètre. List.sort, que vous connaissez, attend un Comparator : avant Java 8, il fallait une classe anonyme.
Démo possible : jshell, créer un Predicate et appeler test.
-->

---

## 🟢 Références de méthodes : `Integer::sum`, enfin expliqué

```java
// Séance 2, Order.addDish : merge(clé, valeur, fusion(ancienne, nouvelle))
items.merge(dish, quantity, (oldQty, qty) -> oldQty + qty);
items.merge(dish, quantity, (a, b) -> Integer.sum(a, b));
items.merge(dish, quantity, Integer::sum);          // les 3 sont égales
```

| Référence | Lambda équivalente |
|---|---|
| `Order::getTotal` | `o -> o.getTotal()` |
| `BigDecimal::add` | `(a, b) -> a.add(b)` |
| `platform::getOrderById` | `id -> platform.getOrderById(id)` |

<div class="support">📖 Support §1.7 — Les quatre formes de références de méthodes</div>

<!--
Durée : 2 min
Message : Integer possède une méthode statique sum(int, int). Quand une lambda ne fait qu'appeler une méthode existante, on peut écrire Classe::méthode.
Piège à signaler : BigDecimal::add, le 1er paramètre devient l'objet sur lequel on appelle add.
Règle : préférer la référence seulement si elle est plus lisible.
-->

---

<!-- _class: section -->

# 2. L'API Stream

<!--
Durée : 0,5 min
-->

---

## 🟢 Boucle ou stream ?

<div class="cols"><div>

**Impératif** : *comment* faire

```java
List<BigDecimal> totals =
        new ArrayList<>();
for (Order o : orders) {
    if (o.getStatus()
            != CANCELLED) {
        totals.add(o.getTotal());
    }
}
totals.sort(
        Comparator.reverseOrder());
```

</div><div>

**Stream** : *quoi* obtenir

```java
List<BigDecimal> totals =
    orders.stream()
        .filter(o -> o.getStatus()
                != CANCELLED)
        .map(Order::getTotal)
        .sorted(Comparator
                .reverseOrder())
        .toList();
```

</div></div>

> Le stream se lit comme la question : « les totaux des commandes non annulées, du plus grand au plus petit ».

<div class="support">📖 Support §2.1 — Pourquoi les streams</div>

<!--
Durée : 2 min
Message : la boucle n'est pas interdite (elle reste plus claire avec des break ou beaucoup de branches). Le stream brille pour filtrer, transformer, agréger.
Résultat des deux : [37.50, 30.50, 23.80, 17.40, 9.80].
-->

---

## 🟢 Anatomie d'un pipeline

```text
orders.stream()        source
    .filter(...)       intermédiaire  ┐  rendent un Stream,
    .map(...)          intermédiaire  ┘  ne calculent rien encore
    .toList()          terminale      →  déclenche le calcul
```

- Un stream **ne stocke rien** : c'est un tuyau, pas un récipient
- Il **ne modifie jamais** sa source (`filter` ne retire rien de la `Map`)
- **Une seule** opération terminale, à la fin

<div class="support">📖 Support §2.2 — Anatomie d'un pipeline</div>

<!--
Durée : 2 min
Message : source, opérations intermédiaires paresseuses, opération terminale. Retenir ce schéma : il explique tous les pièges qu'on verra.
Sources possibles : collection.stream(), Stream.of(...), Files.lines(...).
-->

---

## 🟢 `filter`, `map`, `toList`

```java
// Dans DeliveryPlatform
public List<Order> findOrdersByStatus(OrderStatus status) {
    return orders.values().stream()
                 .filter(o -> o.getStatus() == status)
                 .toList();
}

// Les clients dont une commande est en route
List<String> names = platform.getOrders().stream()
        .filter(o -> o.getStatus() == OrderStatus.IN_DELIVERY)
        .map(o -> o.getCustomer().getName())
        .toList();                                    // [Bob]
```

<div class="support">📖 Support §2.3 — filter, map, toList</div>

<!--
Durée : 2,5 min
Message : filter prend un Predicate, map prend une Function. On retrouve les interfaces de la partie 1.
map change le type des éléments : Stream<Order> devient Stream<String>.
toList() (Java 16) remplace collect(Collectors.toList()) que vous verrez dans du code ancien.
-->

---

## 🟢 `sorted` et `limit` : le top 3 des commandes

```java
List<Order> top3 = orders.stream()
        .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
        .sorted(Comparator.comparing(Order::getTotal).reversed())
        .limit(3)
        .toList();
// Bob : 37.50 · Alice : 30.50 · Bob : 23.80
```

- `Comparator.comparing(clé)` fabrique le comparateur, `.reversed()` l'inverse
- `BigDecimal` est trié avec `compareTo` : correct pour un montant (S2)

<div class="support">📖 Support §2.4 — sorted, limit, Comparator</div>

<!--
Durée : 2 min
Question à la salle : comment obtenir les 2 commandes les plus récentes ? (comparing(Order::getCreatedAt).reversed(), limit(2))
-->

---

## 🟢 Répondre à une question : `count`, `anyMatch`, `findFirst`

```java
long pending = orders.stream()
        .filter(o -> o.getStatus() == OrderStatus.PENDING)
        .count();                                         // 1

BigDecimal limit = new BigDecimal("35");
boolean anyBig = orders.stream()
        .anyMatch(o -> o.getTotal().compareTo(limit) > 0); // true

Optional<Order> preparing = orders.stream()
        .filter(o -> o.getStatus() == OrderStatus.PREPARING)
        .findFirst();                                     // vide
```

<div class="support">📖 Support §2.5 — count, anyMatch, allMatch, noneMatch, findFirst</div>

<!--
Durée : 2 min
Message : anyMatch, allMatch, noneMatch, findFirst s'arrêtent dès que la réponse est connue (court-circuit).
findFirst rend un Optional : on retrouve S2.
-->

---

## 🟢 Chiffre d'affaires : `reduce` avec `BigDecimal`

```java
BigDecimal revenue = orders.stream()
        .filter(o -> o.getStatus() == OrderStatus.DELIVERED)
        .map(Order::getTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);       // 71.70
```

```text
ZERO.add(30.50)  = 30.50
30.50.add(17.40) = 47.90
47.90.add(23.80) = 71.70
```

- Pas de `sum()` pour `BigDecimal`, et **jamais** de `double` pour l'argent
- `BigDecimal.ZERO` : point de départ, et résultat si le flux est vide

<div class="support">📖 Support §2.6 — reduce avec BigDecimal</div>

<!--
Durée : 2 min
Message : reduce(identité, accumulateur). L'accumulateur combine le résultat partiel et l'élément suivant. BigDecimal est immuable : add rend un nouveau BigDecimal, c'est exactement ce que reduce attend.
Question à la salle : que rend ce code s'il n'y a aucune commande livrée ? (0)
-->

---

## 🟢 Commandes par statut : `groupingBy`

```java
Map<OrderStatus, Long> countByStatus = orders.stream()
        .collect(Collectors.groupingBy(
                Order::getStatus,          // la clé de chaque groupe
                Collectors.counting()));   // le calcul dans chaque groupe

countByStatus.get(OrderStatus.DELIVERED);  // 3
countByStatus.get(OrderStatus.PREPARING);  // null : pas de groupe vide
```

- Autres calculs par groupe : `toList()` (par défaut), `summingInt`, `reducing`

> L'ordre d'une `Map` issue de `groupingBy` n'est **pas garanti** : ne testez jamais son affichage.

<div class="support">📖 Support §2.7 — groupingBy, counting, EnumMap</div>

<!--
Durée : 2,5 min
Message : c'est le GROUP BY de SQL. Le classifieur donne la clé, le collecteur aval dit quoi faire de chaque groupe.
Variante dans le support : CA par client avec reducing, et EnumMap pour un ordre stable.
-->

---

## 🔵 Plats les plus commandés : `flatMap`

```java
Map<String, Integer> qtyByDish = orders.stream()
        .filter(o -> o.getStatus() != OrderStatus.CANCELLED)
        .flatMap(o -> o.getItems().entrySet().stream()) // à plat
        .collect(Collectors.groupingBy(
                e -> e.getKey().name(),
                Collectors.summingInt(Map.Entry::getValue)));

qtyByDish.entrySet().stream()
        .sorted(Map.Entry.<String, Integer>comparingByValue()
                         .reversed())
        .limit(3)
        .forEach(e -> IO.println(e.getKey() + " x" + e.getValue()));
// Margherita x5 · Burger x3 · Tiramisu x2
```

<div class="support">📖 Support §2.9 — flatMap ; §2.10 — toMap et les collisions de clés</div>

<!--
Durée : 2 min
Niveau approfondissement : ne pas exiger, montrer que tout se combine.
Message : map transforme 1 élément en 1 élément ; flatMap transforme 1 commande en un flux de lignes (plat, quantité), puis met tous ces flux bout à bout.
-->

---

## 🤔 Prédisez la sortie

```java
List<String> statuses = List.of("PENDING", "DELIVERED", "CANCELLED");

Stream<String> s = statuses.stream()
        .filter(st -> {
            IO.println("filtre " + st);
            return st.startsWith("D");
        });
IO.println("pipeline construit");

IO.println(s.toList());
```

<!--
Durée : 2 min
Question à la salle : quelle est la première ligne affichée ?
Démo : exécuter dans jshell après le vote, puis ajouter un deuxième s.toList() pour montrer l'IllegalStateException.
-->

---

## Réponse : un stream est paresseux

```text
pipeline construit
filtre PENDING
filtre DELIVERED
filtre CANCELLED
[DELIVERED]
```

- Les opérations intermédiaires **décrivent**, la terminale **exécute**
- Sans opération terminale, rien ne se passe, sans erreur ni avertissement
- Un stream ne sert **qu'une fois** : un second `s.toList()` lève une `IllegalStateException`

<div class="support">📖 Support §2.8 — Les trois pièges des streams</div>

<!--
Durée : 1,5 min
Message : ne jamais stocker un Stream dans un attribut. Stocker la collection, appeler stream() à chaque fois.
-->

---

## 🟢 Pièges : effets de bord et liste non modifiable

<div class="cols"><div>

❌ Remplir une liste externe

```java
List<Order> pending =
        new ArrayList<>();
orders.stream()
    .filter(o -> o.getStatus()
            == PENDING)
    .forEach(pending::add);
```

</div><div>

✅ Laisser le stream construire

```java
List<Order> pending =
    orders.stream()
        .filter(o -> o.getStatus()
                == PENDING)
        .toList();
```

</div></div>

- `toList()` rend une liste **non modifiable** : `add` lève `UnsupportedOperationException`
- Besoin de la modifier ? `new ArrayList<>(stream.toList())`

<div class="support">📖 Support §2.8 et §2.3 ; §2.11 — streams parallèles</div>

<!--
Durée : 2 min
Message : une lambda de filter/map doit être une fonction pure. L'effet de bord devient un vrai bug avec parallelStream() : éléments perdus, exception aléatoire (détails dans le support §2.11).
La liste non modifiable est un avantage : c'est la copie défensive de S2, gratuitement.
-->

---

<!-- _class: section -->

# 3. Exceptions et journalisation

<!--
Durée : 0,5 min
Transition : jusqu'ici, tout se passait bien. Que se passe-t-il quand la commande n'existe pas, que le fichier du menu est absent, qu'on veut livrer une commande annulée ?
-->

---

## 🟢 La hiérarchie des exceptions

```text
Throwable
├── Error                  grave (JVM) : on ne l'attrape pas
└── Exception              CHECKED : le compilateur oblige à la traiter
    ├── IOException, SQLException…         (monde extérieur)
    └── RuntimeException   UNCHECKED : aucune obligation
        ├── IllegalArgumentException       (mauvais paramètre)
        │   └── NumberFormatException
        ├── IllegalStateException          (mauvais moment)
        └── OrderNotFoundException         (la nôtre)
```

- **Checked** : `throws IOException` dans la signature, ou `try`/`catch`
- **Unchecked** : bug ou contrat violé, on corrige l'appelant

<div class="support">📖 Support §3.1 et §3.2 — Pourquoi des exceptions, checked ou unchecked</div>

<!--
Durée : 2 min
Message : une exception remonte la pile d'appels jusqu'à quelqu'un qui sait la traiter. Personne ? Le programme s'arrête avec un message clair. C'est le fail fast des constructeurs de S2 (Dish refuse un prix négatif).
throw (l'instruction) contre throws (la déclaration).
Exemple checked : Files.readString(path) ne compile pas sans throws IOException ou try/catch.
-->

---

## 🟢 `try` / `catch` / `finally`

```java
try {
    var request = new DeliveryRequest(Integer.parseInt(qtyText),
                                      LocalDateTime.parse(dateText));
    return "OK : " + request;
} catch (NumberFormatException | DateTimeParseException e) {
    return "Saisie mal formée, vérifiez la quantité et la date.";
} catch (IllegalArgumentException e) {     // APRÈS sa sous-classe
    return "Saisie refusée : la quantité doit être positive.";
} finally {
    IO.println("(saisie traitée)");         // toujours exécuté
}
```

> On attrape là où l'on **sait quoi faire** de l'erreur, du **plus spécifique** au plus général.

<div class="support">📖 Support §3.3 — try/catch/finally, multi-catch, pièges</div>

<!--
Durée : 2,5 min
Message : ici on est à la frontière (une saisie utilisateur) : c'est le bon endroit pour attraper et refuser avec un message clair.
Pièges : NumberFormatException est une IllegalArgumentException, l'ordre inverse ne compile pas. catch (Exception e) attrape aussi les bugs. Jamais de return dans finally.
-->

---

## 🟢 try-with-resources

```java
static List<Dish> loadMenu(Path file) throws IOException {
    try (Stream<String> lines = Files.lines(file)) {
        return lines.filter(l -> !l.isBlank())
                    .map(l -> parseLine(l))
                    .toList();
    }   // lines.close() est appelé automatiquement, même en cas d'erreur
}
```

- Pour tout `AutoCloseable` : fichier, connexion réseau, connexion JDBC (S4)
- Plusieurs ressources : fermées dans l'ordre **inverse** d'ouverture
- `IOException` est checked : `loadMenu` la déclare, l'appelant décidera

<div class="support">📖 Support §3.4 — try-with-resources</div>

<!--
Durée : 2,5 min
Message : Files.lines rend un Stream adossé à un fichier ouvert : un des rares streams qu'il faut fermer. Lien direct avec la partie 2.
Avant Java 7 : finally + close() + null check + exception de close... 15 lignes, souvent fausses.
-->

---

## 🤔 Prédisez la sortie

```java
class Ticket implements AutoCloseable {
    Ticket() { IO.println("ouverture"); }
    public void close() { IO.println("fermeture"); }
}

try (Ticket t = new Ticket()) {
    IO.println("préparation");
    throw new IllegalStateException("four en panne");
} catch (IllegalStateException e) {
    IO.println("catch : " + e.getMessage());
} finally {
    IO.println("finally");
}
```

<!--
Durée : 1,5 min
Question à la salle : "fermeture" apparaît-elle avant ou après "catch" ?
-->

---

## Réponse : la ressource est fermée avant le `catch`

```text
ouverture
préparation
fermeture
catch : four en panne
finally
```

> Quand on traite l'erreur, la ressource est **déjà libérée**.

<div class="support">📖 Support §3.4 — Ordre de fermeture, exceptions supprimées 🔵</div>

<!--
Durée : 1 min
Approfondissement : si close() lève aussi une exception, elle est attachée à la première (getSuppressed()), pas perdue.
-->

---

## 🟢 Exception métier : `OrderNotFoundException`

```java
class OrderNotFoundException extends RuntimeException {
    private final String orderId;
    OrderNotFoundException(String orderId) {
        super("Commande introuvable : " + orderId);
        this.orderId = orderId;
    }
    public String getOrderId() { return orderId; }
}
```

**Pourquoi unchecked ?**
- L'appelant ne peut pas réparer sur place : l'erreur remonte à la frontière
- Il pouvait l'éviter avec `findOrderById` : c'est un **contrat violé**
- Une checked ne passerait pas dans `map(platform::getOrderById)`

<div class="support">📖 Support §3.5 — Exception métier, checked ou unchecked</div>

<!--
Durée : 2,5 min
Message : un nom métier, un message pour le développeur (pas pour l'utilisateur, on y revient en 🔒), et une donnée utile (l'identifiant).
Checked serait justifiée si l'appelant devait prévoir un plan B et pouvait le mettre en œuvre lui-même (réessayer une I/O).
Question à la salle : pourquoi pas IllegalArgumentException("not found") ? (le type ne dit rien du problème métier)
-->

---

## 🟢 `getOrderById` ou `findOrderById` ?

```java
/** Recherche : l'absence est un résultat normal. */
public Optional<Order> findOrderById(String id) {
    return Optional.ofNullable(orders.get(id));
}

/** Accès : la commande DOIT exister. */
public Order getOrderById(String id) {
    return findOrderById(id)
            .orElseThrow(() -> new OrderNotFoundException(id));
}
```

- Barre de recherche du client → `findOrderById` : « aucun résultat »
- Le livreur valide **sa** livraison → `getOrderById` : sinon, anomalie

<div class="support">📖 Support §3.6 — Lever ou renvoyer un Optional</div>

<!--
Durée : 2,5 min
Message clé : l'absence est-elle attendue ? Oui → Optional. Non → exception.
Pièges : findOrderById(id).get() donne NoSuchElementException("No value present") sans dire quelle commande ; renvoyer null ; lever une exception pour un cas normal.
orElseThrow(Supplier) : la lambda fabrique l'exception seulement si l'Optional est vide.
-->

---

## 🟢 Transition interdite : `IllegalStateException`

```java
public void moveTo(OrderStatus next) {
    if (!status.canMoveTo(next)) {          // règle portée par l'enum
        throw new IllegalStateException(
                "Transition interdite : " + status + " -> " + next);
    }
    // ...
    status = next;                          // modifié APRÈS vérification
}

order.moveTo(OrderStatus.CANCELLED);
order.moveTo(OrderStatus.IN_DELIVERY);
// IllegalStateException: Transition interdite : CANCELLED -> IN_DELIVERY
```

- `IllegalArgumentException` : **le paramètre** est invalide
- `IllegalStateException` : **l'objet** n'est pas dans le bon état

<div class="support">📖 Support §3.7 — Valider les transitions</div>

<!--
Durée : 2 min
Message : canMoveTo est un switch exhaustif sur l'enum : un nouveau statut oublié = erreur de compilation.
On vérifie avant de modifier : si la transition est refusée, la commande reste cohérente (fail-safe).
Sécurité : un utilisateur qui rejoue une requête "livrer" sur une commande annulée est arrêté ici.
-->

---

## 🟢 Ne jamais avaler une exception

<div class="cols"><div>

```java
// ❌ Le catch vide
catch (IllegalStateException e) {
}

// ❌ La valeur magique
catch (NumberFormatException e) {
    return 0;          // ou null
}

// ❌ La console, et on continue
catch (IOException e) {
    e.printStackTrace();
}
```

</div><div>

```java
// ✅ 1. Ne pas attraper
// ✅ 2. Valider en amont
// ✅ 3. Traiter vraiment
//       (refuser, annuler)
// ✅ 4. Envelopper, avec la cause
catch (NumberFormatException e) {
    throw new IllegalArgumentException(
            "Pourcentage invalide", e);
}
```

</div></div>

> Une erreur masquée devient une **donnée fausse**, découverte trop tard, loin de sa cause.

<div class="support">📖 Support §3.8 — Ne jamais avaler ; §3.9 — Envelopper sans perdre la cause</div>

<!--
Durée : 2,5 min
Message : "abc" devenu 0 % de remise, personne ne le saura. Le null renvoyé provoquera une NullPointerException trois méthodes plus loin.
🔒 Un catch vide efface aussi les traces d'une attaque (accès refusé, signature invalide) : OWASP Top 10, Security Logging and Monitoring Failures ; SEI CERT ERR00-J.
-->

---

## 🟢 Journaliser : `System.Logger`, pas `printStackTrace`

```java
private static final Logger LOG =
        System.getLogger(DeliveryService.class.getName());

void markDelivered(String orderId) {
    Order order = platform.getOrderById(orderId);
    try {
        order.moveTo(OrderStatus.DELIVERED);
        LOG.log(Level.INFO, "Commande {0} livrée", order.getId());
    } catch (IllegalStateException e) {
        LOG.log(Level.WARNING, "Livraison refusée : " + order.getId(), e);
        throw e;                  // on journalise ET on relance
    }
}
```

<div class="support">📖 Support §4.1 — System.Logger, niveaux, pièges</div>

<!--
Durée : 2,5 min
Message : un logger date le message, donne un niveau (ERROR, WARNING, INFO, DEBUG) et la classe d'origine, vers une destination configurable. Inclus dans le JDK, sans dépendance.
L'exception en DERNIER argument : la pile part dans les logs, pas à l'utilisateur.
Piège : journaliser puis relancer à chaque couche = 5 fois la même erreur. Journaliser une fois.
En entreprise : SLF4J + Logback, même principe.
-->

---

## 🔒 Fuite d'information par les messages d'erreur (CWE-209)

<div class="cols"><div>

❌ Le détail technique part chez le client

```java
} catch (RuntimeException e) {
    return "500 " + e;
}
```

```text
500 java.lang.IllegalState…
Connexion refusée : jdbc:
postgresql://db-prod-02:5432
/foodfast (user=ff_admin)
```

</div><div>

✅ Détail dans les logs, référence au client

```java
} catch (RuntimeException e) {
    String ref = UUID.randomUUID()
                     .toString();
    LOG.log(Level.ERROR,
            "Incident " + ref, e);
    return "500 Une erreur est "
         + "survenue. Réf. " + ref;
}
```

</div></div>

<div class="support">📖 Support §4.2 — CWE-209, réponses d'erreur</div>

<!--
Durée : 3 min
Démo : lancer ErrorResponses.java (support §4.2) et lire à voix haute ce que l'attaquant vient d'apprendre : type de base, serveur, port, nom de base, compte.
Message : jamais de e.getMessage(), e.toString() ni de pile dans une réponse utilisateur. La référence fait le lien entre le client, le support et les logs.
Bonus : des messages différents pour "inexistante" et "appartient à un autre client" permettent d'énumérer les identifiants.
-->

---

## 🔒 Ce qu'on ne met pas dans les logs

```java
// ❌ Données personnelles : les logs sont copiés, archivés, partagés
LOG.log(Level.INFO, "Livraison pour " + c.getEmail()
        + ", " + c.getAddress());

// ✅ Un identifiant suffit pour enquêter
LOG.log(Level.INFO, "Livraison de {0} pour le client {1}",
        order.getId(), c.getId());
```

- Jamais : mots de passe, jetons, numéros de carte, données de santé
- Attention au `toString()` d'un `record` : il affiche **tous** ses champs
- RGPD : principe de minimisation des données

<div class="support">📖 Support §4.3 — Données personnelles dans les logs</div>

<!--
Durée : 2 min
Message : les logs sont rarement protégés comme une base de données (outils tiers, prestataires, archives). Un mot de passe erroné journalisé est souvent le vrai mot de passe avec une faute de frappe.
-->

---

## 🔒 Injection dans les logs (CWE-117)

```java
// L'identifiant vient de l'URL : l'attaquant y glisse un \n
String orderId = "order-42\nINFO: Remboursement de 500 EUR valide";
LOG.log(Level.WARNING, "Commande introuvable : " + orderId);  // ❌
```

```text
WARNING: Commande introuvable : order-42
INFO: Remboursement de 500 EUR valide           <- fausse ligne !
```

```java
static String forLog(String untrusted) {                      // ✅
    return untrusted.replaceAll("\\p{Cntrl}", "_");
}
```

<div class="support">📖 Support §4.4 — Injection dans les logs, Log4Shell</div>

<!--
Durée : 2 min
Démo : lancer LogInjection.java (support §4.4).
Message : une entrée utilisateur journalisée est une entrée non fiable. Neutraliser les caractères de contrôle, limiter la longueur, et surtout valider le format à la frontière (un identifiant de commande a un format connu).
Lien : Log4Shell (2021), une bibliothèque de logs qui interprétait le contenu des messages.
-->

---

## Ce qu'il faut retenir

- **Lambda** : implémentation concise d'une interface à une méthode ; `Integer::sum`, `Order::getTotal`
- **Stream** : source → intermédiaires paresseuses → **une** terminale ; `toList()` non modifiable
- Montants : `reduce(BigDecimal.ZERO, BigDecimal::add)` ; groupes : `groupingBy` + `counting`
- `OrderNotFoundException` (unchecked) si la commande **doit** exister, sinon `Optional`
- **Jamais** de `catch` vide, de `return 0` ou `null` par défaut, de `printStackTrace`
- 🔒 Détail dans les logs, message neutre à l'utilisateur ; ni données personnelles ni `\n` bruts

<!--
Durée : 2 min
Question à la salle : pour chaque ligne, un étudiant donne un exemple FoodFast.
-->

---

## Pour aller plus loin

- 🔵 Support §2.9 à §2.11 : `flatMap`, `toMap` et collisions de clés, streams parallèles
- 🟣 Support §2.12 : Gatherers (JDK 24), lots de livraisons avec `windowFixed`
- 🟣 Support §3.10 et §3.11 : exceptions checked dans les lambdas, variable `_`
- Annexe A du support : le modèle FoodFast complet de la séance
- dev.java · OWASP Logging et Error Handling Cheat Sheets · SEI CERT (ERR00-J, ERR01-J)

> Séance 4 : concurrence et JDBC, sous l'angle « le bug et la faille ».

<!--
Durée : 1 min
-->
