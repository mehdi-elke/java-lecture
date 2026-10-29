---
marp: true
theme: foodfast
paginate: true
title: "Java — Séance 2 : De la donnée à l'objet"
---

<!-- _class: lead -->

# Séance 2 — De la donnée à l'objet

## Construire le modèle FoodFast

Classes · `enum` · `record` · `BigDecimal` · héritage · interfaces · collections · `Optional`

<!--
Durée : 0,5 min
Message : aujourd'hui, on passe des tableaux et des méthodes static de la S1 à de vrais objets métier.
À la fin de la séance, le modèle FoodFast complet existe : Dish, Order, Customer, Discount, DeliveryPlatform.
-->

---

## Objectifs de la séance

À la fin, vous savez :

- **AA2** — modéliser un domaine avec des classes, des `record` et des `enum`, avec encapsulation et invariants
- **AA3** — utiliser l'héritage, les interfaces et le polymorphisme
- **AA4** — choisir la bonne collection (`List`, `Set`, `Map`) et expliquer `equals`/`hashCode`

> Fil rouge : on construit, pas à pas, le modèle de FoodFast.

<div class="support">📖 Le support détaillé reprend chaque slide avec du code complet et compilé</div>

<!--
Durée : 1 min
Message : sept parties, dans un ordre précis, car chaque notion s'appuie sur la précédente :
classe → enum/record → types riches → héritage → interfaces → collections → Optional.
Annoncer : séance dense. Ce qui n'est pas indispensable est dans le support.
-->

---

<!-- _class: section -->

# 1. Classe et objet

<!--
Durée : 0,5 min
-->

---

## 🟢 Pourquoi des classes ?

```java
// S1 : un menu = des tableaux parallèles
String[] names    = {"Margherita", "Burger"};
int[] pricesCents = {1250, 990};
String[] sizes    = {"MEDIUM", "LARGE"};

// Supprimer le plat n° 1 ? Il faut toucher 3 tableaux.
// En oublier un = données incohérentes, sans erreur.
```

> Une **classe** regroupe les données **et** les règles d'un concept métier.
> Un **objet** est un exemplaire de cette classe : *cette* Margherita à 12,50 €.

<div class="support">📖 Support §1.1 — Pourquoi des classes</div>

<!--
Durée : 2 min
Question à la salle : en S1, comment avez-vous stocké un plat avec son nom et son prix ?
Message : le tableau parallèle est fragile, la classe est la réponse.
-->

---

## 🟢 Champs, constructeur, `this`

```java
class Dish {
    String name;          // champ : propre à chaque objet
    int priceInCents;     // comme en S1, pour l'instant

    Dish(String name, int priceInCents) {   // constructeur
        this.name = name;                    // this = l'objet créé
        this.priceInCents = priceInCents;
    }
}

Dish pizza  = new Dish("Margherita", 1250);
Dish burger = new Dish("Burger", 990);
IO.println(pizza.name);   // Margherita
```

<div class="support">📖 Support §1.2 — Champs, constructeur, this</div>

<!--
Durée : 2,5 min
Message : le constructeur porte le nom de la classe et n'a pas de type de retour.
this.name désigne le champ, name le paramètre.
Deux new = deux objets distincts en mémoire (rappel du schéma pile/tas de S1).
-->

---

## 🟢🔒 Encapsulation : un objet invalide ne doit pas exister

<div class="cols"><div>

```java
// ❌ champs accessibles
class Dish {
    String name;
    int priceInCents;
    // ...
}

pizza.priceInCents = -500;
// compile, et personne
// ne s'en rend compte
```

</div><div>

```java
// ✅ private + vérification
class Dish {
    private final String name;
    private final int priceInCents;

    Dish(String name, int price) {
        if (price < 0) throw new
          IllegalArgumentException(
            "Prix négatif");
        // ... affectations
    }
}
```

</div></div>

<div class="support">📖 Support §1.3 — Encapsulation et invariants</div>

<!--
Durée : 3 min
Message : un invariant est une règle toujours vraie (« un prix n'est jamais négatif »).
On la vérifie UNE fois, dans le constructeur ; private + final empêche de la casser ensuite.
throw new IllegalArgumentException : retenez juste « je refuse cette valeur ». Les exceptions en détail : S3.
-->

---

## 🟢 Des méthodes métier, pas des setters

<div class="cols"><div>

```java
// ❌ setters générés
order.setDishCount(-3);
order.setConfirmed(false);
// n'importe qui « dé-confirme »
// une commande
```

</div><div>

```java
// ✅ ce que le métier permet
class Order {
  private int dishCount;
  private boolean confirmed;

  void addDish(int quantity) {
    if (quantity <= 0) throw new
      IllegalArgumentException(
        "Quantité : " + quantity);
    dishCount += quantity;
  }
  void confirm() { confirmed = true; }
}
```

</div></div>

> Un getter si quelqu'un en a besoin. Un setter presque jamais.

<div class="support">📖 Support §1.4 — Méthodes métier plutôt que setters</div>

<!--
Durée : 2 min
Message : ne générez pas getters/setters à la chaîne avec l'IDE. Demandez-vous : quelles opérations le métier autorise-t-il ?
Chaque méthode est un point de contrôle où l'on vérifie les règles.
-->

---

## 🟢 `static` contre instance

```java
class Order {
    static final int MAX_QUANTITY = 20;  // un seul, partagé
    private int dishCount;               // un par commande

    void addDish(int quantity) {         // agit sur this
        if (quantity > MAX_QUANTITY) { /* ... refuser */ }
        dishCount += quantity;
    }
}

Order a = new Order();
Order b = new Order();
a.addDish(3);              // a : 3 plats, b : toujours 0
IO.println(Order.MAX_QUANTITY);   // appelé sur la classe
```

<div class="support">📖 Support §1.5 — static contre instance</div>

<!--
Durée : 2 min
Message : en S1, toutes vos méthodes étaient static : elles ne connaissaient que leurs paramètres.
Une méthode d'instance a accès à this, donc aux champs de l'objet.
Règle : static pour ce qui ne dépend d'aucun objet (constante, utilitaire).
-->

---

<!-- _class: section -->

# 2. `enum` et `record`

<!--
Durée : 0,5 min
-->

---

## 🟢 `enum` : un ensemble fermé de valeurs

<div class="cols"><div>

```java
// ❌ String : tout passe
String status = "PENDNG";
// faute de frappe,
// découverte en prod
```

</div><div>

```java
// ✅ enum : le compilateur
// connaît les valeurs
enum DishSize {
    SMALL, MEDIUM, LARGE
}

enum OrderStatus {
    PENDING, CONFIRMED,
    PREPARING, IN_DELIVERY,
    DELIVERED, CANCELLED
}
```

</div></div>

`OrderStatus.PENDNG` → **erreur de compilation**. `OrderStatus.values()` liste les valeurs.

<div class="support">📖 Support §2.1 — enum</div>

<!--
Durée : 2 min
Message : dès qu'une donnée a un nombre fini de valeurs connues, c'est un enum.
On compare avec == (une seule instance par valeur).
Piège (support) : ne jamais stocker ordinal() en base.
-->

---

## 🟢 `switch` sur un `enum`

```java
static String label(OrderStatus status) {
    return switch (status) {
        case PENDING     -> "En attente";
        case CONFIRMED   -> "Confirmée";
        case PREPARING   -> "En préparation";
        case IN_DELIVERY -> "En livraison";
        case DELIVERED   -> "Livrée";
        case CANCELLED   -> "Annulée";
    };
}
```

> Pas de `default` : si l'on ajoute `REFUNDED` à l'enum, ce `switch` **ne compile plus**. C'est voulu.

<div class="support">📖 Support §2.2 — switch sur un enum</div>

<!--
Durée : 2 min
Rappel S1 : le switch en expression (flèche, pas de break).
Message : un switch exhaustif sur un enum est vérifié par le compilateur. Un default masquerait l'oubli.
-->

---

## 🟢 `record` : une valeur en une ligne

```java
record Dish(String name, int priceInCents, DishSize size) {
    Dish {                                  // constructeur compact
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Nom vide");
        if (priceInCents < 0)
            throw new IllegalArgumentException("Prix négatif");
    }
}

Dish pizza = new Dish("Margherita", 1250, DishSize.MEDIUM);
pizza.name();    // "Margherita" : accesseur, sans get
IO.println(pizza);  // Dish[name=Margherita, priceInCents=1250, ...]
```

Généré : champs `private final`, constructeur, accesseurs, `equals`, `hashCode`, `toString`.

<div class="support">📖 Support §2.4 — record · §2.5 — Constructeur compact</div>

<!--
Durée : 3 min
Message : un record = une valeur immuable. 30 lignes de classe remplacées par une.
Le constructeur compact ne répète pas les paramètres : il valide, puis Java affecte les champs.
Quand choisir un record : une valeur (Dish, montant, adresse). Une classe : une entité qui change d'état (Order).
-->

---

<!-- _class: section -->

# 3. Types riches

<!--
Durée : 0,5 min
-->

---

## 🤔 Prédisez la sortie

```java
double a = 0.1 + 0.2;
IO.println(a);
IO.println(a == 0.3);
```

Que s'affiche-t-il ?

<!--
Durée : 1 min
Question à la salle : votez à main levée. 0.3 / true ? autre chose ?
Démo : taper l'exemple dans jshell AVANT de passer à la slide suivante.
-->

---

## Réponse : `0.30000000000000004` et `false`

- Un `double` est stocké en **binaire** : `0.1` n'y a pas de valeur exacte
- En S1, vous avez contourné le problème avec des **centimes en `int`**
- Mais : 10 % de remise sur 12,45 € ? Quel arrondi ? Et l'affichage ?
- `BigDecimal` : un nombre **décimal exact**, avec un arrondi **choisi**

> Un montant, c'est toujours un `BigDecimal`. Jamais un `double`.

<div class="support">📖 Support §3.1 — Le problème du double</div>

<!--
Durée : 2 min
Message : ce n'est pas un bug de Java, c'est la norme IEEE 754 (même résultat en Python, JS, C).
Les centimes en int de la S1 étaient une bonne idée, BigDecimal la généralise.
-->

---

## 🟢 `BigDecimal` : créer et calculer

```java
BigDecimal price = new BigDecimal("12.50");   // ✅ depuis une String
BigDecimal wrong = new BigDecimal(0.1);       // ❌ hérite de l'erreur
// wrong = 0.1000000000000000055511151231257827...

BigDecimal total = price.multiply(BigDecimal.valueOf(2));  // 25.00

total.add(new BigDecimal("2.90"));            // ❌ résultat perdu
total = total.add(new BigDecimal("2.90"));    // ✅ 27.90
```

> `BigDecimal` est **immuable**, comme `String` : chaque opération renvoie un **nouvel** objet.

<div class="support">📖 Support §3.2 — BigDecimal : créer et calculer</div>

<!--
Durée : 2,5 min
Message : deux règles. 1) Construire depuis une String (ou BigDecimal.valueOf pour un entier).
2) Récupérer le résultat de chaque opération.
Question à la salle : que vaut total après la ligne ❌ ? (25.00)
-->

---

## 🟢 `equals` contre `compareTo`

```java
BigDecimal a = new BigDecimal("12.5");
BigDecimal b = new BigDecimal("12.50");
a.equals(b);          // false : compare valeur ET échelle
a.compareTo(b) == 0;  // true  : compare la valeur seulement
```

D'où la version finale de `Dish` : on **normalise** le prix.

```java
record Dish(String name, BigDecimal price, DishSize size) {
    Dish {
        // ... mêmes vérifications (nom, null, prix >= 0)
        price = price.setScale(2, RoundingMode.HALF_UP);
    }
}
```

<div class="support">📖 Support §3.3 — equals contre compareTo</div>

<!--
Durée : 3 min
Message : pour comparer deux montants : compareTo. Toujours.
Le record utilise equals sur chaque champ : sans setScale, Dish(12.5) ≠ Dish(12.50). On le reverra avec les Map.
Dans un constructeur compact, on peut réaffecter le paramètre avant que Java remplisse le champ.
-->

---

## 🟢 `LocalDateTime` et `UUID`

```java
LocalDateTime createdAt = LocalDateTime.now();
LocalDateTime deadline  = createdAt.plusMinutes(45);
boolean late = LocalDateTime.now().isAfter(deadline);

UUID uuid = UUID.randomUUID();     // 122 bits aléatoires
String orderId = "order-" + uuid;  // order-3f2b8c1e-...-4...
```

- Dates : `java.time`, immuable. Oubliez `Date` et `Calendar`
- `UUID` : identifiant unique, sans compteur central

> 🔒 Un identifiant impossible à deviner **n'est pas** un contrôle d'accès.

<div class="support">📖 Support §3.5 — LocalDateTime · §3.6 — UUID</div>

<!--
Durée : 2 min
Message : java.time suit les mêmes règles que BigDecimal : immuable, plusMinutes renvoie un nouvel objet.
🔒 Pour la cyber : un id séquentiel (order-41, order-42) permet de deviner les commandes des autres (IDOR, OWASP A01).
Un UUID réduit le risque, mais le serveur doit TOUJOURS vérifier que la commande appartient à l'utilisateur.
-->

---

<!-- _class: section -->

# 4. Héritage

<!--
Durée : 0,5 min
-->

---

## 🟢 Pourquoi l'héritage ?

Un client et un livreur ont tous les deux un `id`, un `name`, un `email`.

```
             Object          ← racine de toutes les classes
               │
         User (abstract)     ← id, name, email, role()
          ┌────┴─────┐
      Customer    Courier    ← + address  /  rien de plus
```

> `Customer` **est un** `User`. On écrit le commun une fois, dans `User`.

<div class="support">📖 Support §4.1 — Pourquoi l'héritage</div>

<!--
Durée : 1 min
Message : sans héritage, on copie-colle les trois champs, les getters et les validations dans chaque classe.
Toute classe hérite de Object, même sans extends : c'est de là que viennent toString, equals, hashCode.
-->

---

## 🟢 La classe mère abstraite `User`

```java
abstract class User {
    private final UUID id = UUID.randomUUID();
    private final String name;
    private final String email;

    protected User(String name, String email) {
        this.name = Objects.requireNonNull(name);
        this.email = Objects.requireNonNull(email);
    }
    public String getName() { return name; }
    // ... getId(), getEmail()
    public abstract String role();   // pas de corps
}
```

<div class="support">📖 Support §4.2 — extends, super, protected</div>

<!--
Durée : 2 min
Message : abstract class = on ne peut pas faire new User(...). Un « utilisateur » tout court n'existe pas.
abstract role() = chaque sous-classe DOIT la fournir.
protected : le constructeur n'est appelé que par les sous-classes.
Objects.requireNonNull : refuse null (même idée que IllegalArgumentException).
-->

---

## 🟢 `extends`, `super(...)`, `@Override`

```java
class Customer extends User {
    private final String address;

    Customer(String name, String email, String address) {
        super(name, email);       // construit d'abord la partie User
        this.address = Objects.requireNonNull(address);
    }
    @Override public String role() { return "CLIENT"; }
}

User u = new Customer("Alice", "alice@example.com", "10 rue Haute");
u.getName();   // "Alice"  : hérité de User
u.role();      // "CLIENT" : la version de Customer
```

<div class="support">📖 Support §4.3 — @Override et abstract · §4.4 — Object</div>

<!--
Durée : 3 min
Message : super(...) appelle le constructeur de User ; les champs private de User restent inaccessibles à Customer.
@Override : le compilateur vérifie qu'on redéfinit bien une méthode existante (faute de frappe = erreur).
Variable de type User, objet Customer : c'est la méthode de l'OBJET qui s'exécute. C'est le polymorphisme.
Courier : même schéma, sans adresse (support).
-->

---

## 🟢 Composition plutôt qu'héritage

<div class="cols"><div>

```java
// ❌ « une commande EST
//    un client » ?
class Order extends Customer {
    // hérite de address,
    // email, role()...
}
```

</div><div>

```java
// ✅ une commande A un client
class Order {
    private final Customer
        customer;
    // ...
}
```

</div></div>

- `extends` seulement si « **est un** » est vrai, et le reste
- Sinon : un champ (« **a un** »). C'est la composition
- Java : **une seule** classe mère par classe

<div class="support">📖 Support §4.5 — Composition plutôt qu'héritage</div>

<!--
Durée : 2 min
Question à la salle : Courier extends Customer pour réutiliser le nom et l'email ? (non : un livreur n'est pas un client)
Message : l'héritage crée un couplage fort. En cas de doute, composition.
-->

---

<!-- _class: section -->

# 5. Interfaces et polymorphisme

<!--
Durée : 0,5 min
-->

---

## 🟢 L'interface `Discount`

```java
// ❌ avant : un if/else par type de remise, modifié à chaque ajout
// if (type.equals("PERCENT")) ... else if (type.equals("FIXED")) ...

interface Discount {
    BigDecimal apply(BigDecimal total);   // contrat : quoi, pas comment
}

record PercentageDiscount(int percent) implements Discount {
    public BigDecimal apply(BigDecimal total) {
        return total.multiply(BigDecimal.valueOf(100 - percent))
                    .divide(BigDecimal.valueOf(100), 2,
                            RoundingMode.HALF_UP);
    }
}
```

<div class="support">📖 Support §5.1 — Le problème · §5.2 — Interface et implémentations</div>

<!--
Durée : 3 min
Message : le code qui UTILISE une remise ne devrait pas connaître toutes les remises.
Une interface = un contrat (des signatures, sans état). implements = « je respecte ce contrat ».
Les méthodes d'une interface sont public : l'implémentation doit l'être aussi.
Un record peut implémenter une interface.
-->

---

## 🟢 Polymorphisme

```java
record FixedDiscount(BigDecimal amount) implements Discount {
    public BigDecimal apply(BigDecimal total) {
        return total.subtract(amount).max(BigDecimal.ZERO);
    }
}

Discount[] promos = { new PercentageDiscount(10),
                      new FixedDiscount(new BigDecimal("5.00")) };
BigDecimal total = new BigDecimal("42.00");
for (Discount d : promos) {
    IO.println(d.apply(total));   // 37.80 puis 37.00
}
```

Nouvelle remise = nouvelle classe. La boucle **ne change pas**.

<div class="support">📖 Support §5.3 — Polymorphisme</div>

<!--
Durée : 2,5 min
Message : une variable Discount, plusieurs comportements : c'est le polymorphisme.
Le code appelant dépend de l'interface, pas des implémentations.
max(ZERO) : une remise ne rend jamais le total négatif (invariant).
Lien S3 : cette interface à une seule méthode pourra s'écrire en une ligne avec une lambda.
-->

---

## 🔵 Interface ou classe abstraite ?

| | Interface | Classe abstraite |
|---|---|---|
| Représente | une **capacité** (« sait appliquer ») | une **famille** (« est un utilisateur ») |
| Champs d'instance | non | oui |
| Constructeur | non | oui |
| Combien par classe | plusieurs | **une seule** |
| FoodFast | `Discount` | `User` |

> Par défaut : une interface. Une classe abstraite si l'on partage de l'**état**.

<div class="support">📖 Support §5.4 — Interface ou classe abstraite</div>

<!--
Durée : 2 min
Message : User partage des champs (id, name, email) → classe abstraite.
Discount ne partage aucun état → interface.
Méthodes default et sealed interface : support (🔵 / 🟣).
-->

---

<!-- _class: section -->

# 6. Collections

<!--
Durée : 0,5 min
-->

---

## 🟢 `List`, `Set`, `Map` sont des interfaces

```java
List<Dish> menu = new ArrayList<>();       // ordonnée, doublons OK
Set<String> cuisines = new HashSet<>();    // pas de doublon
Map<String, Order> orders = new HashMap<>();  // clé → valeur
```

- À gauche l'**interface** (le contrat), à droite l'**implémentation**
- `<Dish>` : le **type des éléments**, vérifié à la compilation
- `<>` : Java déduit le type. Pas de primitif : `List<Integer>`

> Comme `Discount d = new FixedDiscount(...)`. Vous connaissez déjà le principe.

<div class="support">📖 Support §6.1 — Interfaces · §6.2 — Génériques</div>

<!--
Durée : 2 min
Message : c'est pour ça que les interfaces viennent avant les collections.
Déclarer List et non ArrayList : on peut changer d'implémentation sans toucher au reste du code.
Question à la salle : pour la liste des statuts déjà pris par une commande, quelle collection ?
-->

---

## 🟢 `List` : le menu

```java
List<Dish> menu = new ArrayList<>();
menu.add(pizza);
menu.add(burger);

menu.size();        // 2
menu.get(0);        // pizza
menu.getFirst();    // pizza   (Java 21)
menu.getLast();     // burger  (Java 21)

for (Dish d : menu) { IO.println(d.name()); }

List<DishSize> sizes = List.of(DishSize.SMALL, DishSize.LARGE);
sizes.add(DishSize.MEDIUM);   // ❌ UnsupportedOperationException
```

<div class="support">📖 Support §6.3 — List</div>

<!--
Durée : 2 min
Message : ArrayList grandit toute seule, contrairement au tableau de S1.
List.of crée une liste NON modifiable : parfait pour une valeur fixe, ou pour renvoyer une liste sans risque.
-->

---

## 🟢 `Map` : les plats d'une commande

```java
Map<Dish, Integer> items = new HashMap<>();

items.merge(pizza, 2, Integer::sum);   // pizza → 2
items.merge(pizza, 1, Integer::sum);   // pizza → 3
items.getOrDefault(burger, 0);         // 0, pas null

BigDecimal total = BigDecimal.ZERO;
for (Map.Entry<Dish, Integer> e : items.entrySet()) {
    BigDecimal qty = BigDecimal.valueOf(e.getValue());
    total = total.add(e.getKey().price().multiply(qty));
}
```

`Integer::sum` : « additionne l'ancienne et la nouvelle quantité ». Syntaxe vue en S3.

<div class="support">📖 Support §6.5 — Map</div>

<!--
Durée : 3 min
Message : merge = « crée l'entrée si absente, sinon combine ». Fini le get + test null + put.
getOrDefault évite le null (et la NullPointerException du int qui le reçoit).
Ici Dish est une CLÉ de Map : retenez-le pour la slide suivante.
-->

---

## 🤔 Prédisez la sortie

```java
class PlainDish {               // une classe, PAS un record
    private final String name;
    PlainDish(String name) { this.name = name; }
}

Set<PlainDish> favorites = new HashSet<>();
favorites.add(new PlainDish("Margherita"));

IO.println(favorites.contains(new PlainDish("Margherita")));
```

`true` ou `false` ?

<!--
Durée : 1,5 min
Question à la salle : votez. Puis démo live : lancer le code.
-->

---

## Réponse : `false`. Il faut `equals` et `hashCode`

```java
@Override public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof PlainDish other)) return false;
    return name.equals(other.name);
}
@Override public int hashCode() {
    return Objects.hash(name);   // mêmes champs que equals
}
```

- Hérités d'`Object` : égalité d'**identité** (même objet en mémoire)
- `HashSet` cherche d'abord par `hashCode`, puis confirme par `equals`
- Contrat : `a.equals(b)` ⇒ `a.hashCode() == b.hashCode()`

> Un `record` les génère pour vous, sur **tous** ses champs.

<div class="support">📖 Support §6.6 — Le contrat equals / hashCode / toString</div>

<!--
Durée : 3 min
Message : le HashSet range l'objet dans un « tiroir » choisi par hashCode. Deux objets égaux dans deux tiroirs différents : on ne retrouve jamais le premier.
toString d'Object affiche PlainDish@1b6d3586 : nom de classe + hashCode d'identité en hexa (pas une adresse mémoire).
-->

---

## 🔒 `equals` incorrect = données perdues

<div class="cols"><div>

```java
// ❌ Dish sans setScale
items.merge(new Dish("Pizza",
    new BigDecimal("12.5"), M),
    1, Integer::sum);
items.merge(new Dish("Pizza",
    new BigDecimal("12.50"), M),
    1, Integer::sum);
// 2 lignes « Pizza » x1
```

</div><div>

```java
// ✅ constructeur compact
record Dish(String name,
        BigDecimal price,
        DishSize size) {
  Dish {
    // ... vérifications
    price = price.setScale(
      2, RoundingMode.HALF_UP);
  }
}
// 1 ligne « Pizza » x2
```

</div></div>

> Ne modifiez **jamais** un champ utilisé par `hashCode` d'une clé déjà dans une `Map`.

<div class="support">📖 Support §6.7 — record et BigDecimal · §6.8 — Clés mutables</div>

<!--
Durée : 2 min
Message : un equals faux ne plante pas : il produit des doublons silencieux, des recherches qui échouent, des stocks faux.
M = DishSize.MEDIUM (raccourci pour la slide).
Clé mutable : on la range dans un tiroir, on change le champ, elle n'est plus jamais retrouvée. D'où des clés immuables (record).
-->

---

## 🔒 Copie défensive : `getItems()`

<div class="cols"><div>

```java
// ❌ on rend la Map interne
Map<Dish, Integer> getItems() {
    return items;
}

// attaque, après confirm() :
order.getItems().put(pizza, -5);
// quantité négative, total faux
```

</div><div>

```java
// ✅ une vue non modifiable
Map<Dish, Integer> getItems() {
    return Collections
        .unmodifiableMap(items);
}

order.getItems().put(pizza, -5);
// UnsupportedOperationException
```

</div></div>

> `private` ne suffit pas si l'on **rend** l'objet interne modifiable.

<div class="support">📖 Support §6.9 — Copie défensive</div>

<!--
Durée : 2,5 min
Message : addDish vérifie la quantité et le statut. Rendre la Map interne permet de contourner ces deux règles.
Pour la cyber : c'est une faille d'encapsulation, classique (CERT OBJ05-J).
Variante : List.copyOf / Map.copyOf, une copie figée (support).
-->

---

<!-- _class: section -->

# 7. `Optional`

<!--
Durée : 0,5 min
-->

---

## 🟢 `Optional` : « peut-être absent », dans la signature

```java
class DeliveryPlatform {
    private final Map<String, Order> orders = new HashMap<>();

    public Optional<Order> findOrderById(String id) {
        return Optional.ofNullable(orders.get(id));
    }
}

Optional<Order> found = platform.findOrderById("order-42");
if (found.isEmpty()) { IO.println("Commande inconnue"); }

Order order = platform.findOrderById(id).orElseThrow();
```

<div class="support">📖 Support §7.1 — Pourquoi · §7.2 — Utiliser Optional</div>

<!--
Durée : 3 min
Message : Map.get renvoie null si la clé est absente. Rien dans la signature ne le dit : NullPointerException plus loin.
Optional<Order> force l'appelant à traiter l'absence.
orElseThrow() : « je sais qu'elle existe ; sinon, erreur immédiate et claire ».
-->

---

## 🟢 Les pièges d'`Optional`

<div class="cols"><div>

```java
// ❌ get() sans vérifier :
//    la NPE devient une autre
//    exception, rien de gagné
opt.get();

// ❌ en champ ou en paramètre
private Optional<String> phone;
void send(Optional<String> to)

// ❌ renvoyer null quand le
//    retour est un Optional
return null;
```

</div><div>

```java
// ✅ dire ce qu'on veut
opt.orElseThrow();
opt.orElse(defaultOrder);
opt.isPresent();

// ✅ Optional : uniquement
//    en type de RETOUR

// ✅ absent = vide
return Optional.empty();
```

</div></div>

<div class="support">📖 Support §7.3 — Les pièges</div>

<!--
Durée : 2 min
Message : Optional est fait pour les retours de méthodes de recherche. Pas pour les champs, ni les paramètres, ni les collections (renvoyez une liste vide).
En S3, avec les lambdas : map, ifPresentOrElse.
-->

---

## Ce qu'il faut retenir

- **Classe** : champs `private`, invariants vérifiés dans le constructeur, méthodes métier
- **`enum`** pour les valeurs fermées, **`record`** pour les valeurs immuables
- **Montants** : `BigDecimal` depuis une `String`, comparés avec `compareTo`
- **Héritage** si « est un » ; sinon **composition**. **Interface** pour un contrat
- **Collections** : `List`/`Set`/`Map` déclarées par leur interface ; `equals` **et** `hashCode`
- **`Optional`** en retour d'une recherche ; 🔒 ne jamais exposer une collection interne

<!--
Durée : 2 min
Question à la salle : pourquoi Dish normalise-t-il son prix avec setScale(2) ?
-->

---

## Pour aller plus loin

Dans le support détaillé :

- 🔵 Arrondis `HALF_UP` / `HALF_EVEN`, enum avec champs, méthodes `default`
- 🔵 `LinkedHashMap`, `TreeMap`, `ArrayDeque` et leurs complexités
- 🟣 Écrire ses propres génériques
- 🟣 `sealed interface Discount` et `switch` avec motifs
- 🟣 Valider avant `super(...)` (JEP 513)

<div class="support">📖 Support §8 — Le modèle complet · §10 — Ressources</div>

<!--
Durée : 0,5 min
Message : le modèle complet de la séance, compilé, est au §8 du support.
-->
