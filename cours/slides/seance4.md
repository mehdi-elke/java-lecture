---
marp: true
theme: foodfast
paginate: true
title: "Java — Séance 4 : Préparer l'application au monde réel"
---

<!-- _class: lead -->

# Séance 4

## Préparer FoodFast au monde réel

Concurrence · JDBC · le bug et la faille

<!--
Durée : 1 min
Message : jusqu'ici FoodFast vivait dans un monde idéal (un seul utilisateur, aucune donnée qui survit). Aujourd'hui, le monde réel : plusieurs clients en même temps, et une vraie base de données. À chaque fois, on regarde le bug ET la faille de sécurité qu'il ouvre.
-->

---

## Objectifs de la séance

**AA7** : identifier et corriger une **race condition** et une **injection SQL**.

1. **Concurrence** (≈ 30 min) : lancer des tâches en parallèle, voir des données se perdre, corriger
2. 🔒 **TOCTOU** : un code promo utilisé 200 fois
3. **JDBC** (≈ 30 min) : lire et écrire des commandes dans une base H2
4. 🔒 **Injection SQL** : vider la base clients avec une apostrophe

<div class="support">📖 Support — introduction</div>

<!--
Durée : 2 min
Message : AA7 est évalué. Deux bugs, deux failles, toutes démontrées en direct.
Rappel : tout le code complet, compilé, est dans le support. Ici on ne montre que l'essentiel.
-->

---

## Vendredi, 19 h 55

Des centaines de clients valident leur panier **dans la même seconde**.

Pour chaque commande, ≈ 300 ms d'**attente** : restaurant, base, e-mail.

| Traitement | 1 000 commandes |
|---|---|
| Une par une | plus de **5 minutes** |
| En parallèle | moins d'**une seconde** |

> Plusieurs threads, c'est plus rapide. Mais ils **partagent la mémoire**.

<div class="support">📖 Support §1.1 — Pourquoi la concurrence ?</div>

<!--
Durée : 2 min
Question à la salle : pendant ces 300 ms, que fait le processeur ? (Rien : il attend le réseau.)
Message : la concurrence n'est pas un luxe, c'est le quotidien de tout serveur. Et le prix à payer, c'est la mémoire partagée.
-->

---

<!-- _class: section -->

# Partie 1

## Concurrence : le bug et la faille

<!--
Durée : 0,5 min
-->

---

## 🟢 Lancer des tâches : `ExecutorService`

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int i = 1; i <= 10_000; i++) {
        int n = i;
        executor.submit(() -> notifyRestaurant(n)); // 100 ms chacune
    }
} // close() ATTEND la fin de toutes les tâches

IO.println("10 000 restaurants prévenus");     // en ≈ 0,2 s
```

- Un `ExecutorService` gère les threads à votre place
- **try-with-resources** (S3) : plus de `shutdown()` oublié
- `submit` renvoie un `Future` : `get()` donne le résultat ou l'erreur

<div class="support">📖 Support §1.2 — ExecutorService</div>

<!--
Durée : 3 min
Démo : NotifyRestaurants.java du support (10 000 tâches de 100 ms, terminées en ≈ 0,2 s).
Message : depuis Java 19, ExecutorService est AutoCloseable. Le close() attend la fin : c'est notre point de synchronisation, jamais un sleep.
Piège à signaler : une exception dans une tâche dont personne ne lit le Future est perdue en silence (S3 : ne jamais avaler une exception).
-->

---

## 🟢 Les threads virtuels, en une slide

| | Thread plateforme | Thread **virtuel** (Java 21) |
|---|---|---|
| Géré par | le système | la JVM |
| Coût | ≈ 1 Mo | quelques centaines d'octets |
| Combien ? | quelques milliers | des **millions** |
| Pour | calcul intensif | tâches qui **attendent** (réseau, base) |

- Pendant une attente, la JVM « décroche » le thread virtuel
- On ne les met **pas en pool** : un par tâche
- On limite la ressource rare (connexions à la base), pas les threads

<div class="support">📖 Support §1.3 — Les threads virtuels</div>

<!--
Durée : 2 min
Message : un thread virtuel par requête, c'est le modèle moderne d'un serveur Java.
Pour le calcul pur (chiffrement, compression), un pool fixe de la taille du nombre de cœurs reste le bon choix.
Précision pour les avancés : depuis le JDK 24 (JEP 491), synchronized ne bloque plus le thread porteur ; l'ancien conseil « remplacer synchronized par ReentrantLock » est obsolète.
-->

---

## 🤔 Prédisez la sortie

```java
class OrderCounter {
    private int count = 0;
    void increment() { count++; }
    int get() { return count; }
}
var counter = new OrderCounter();
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    for (int t = 0; t < 1_000; t++) {
        executor.submit(() -> {
            for (int i = 0; i < 1_000; i++) counter.increment();
        });
    }
}
IO.println(counter.get()); // 1 000 tâches × 1 000 incréments = ?
```

<!--
Durée : 2 min
Question à la salle : quelle valeur ? Faire voter : 1 000 000 / moins / plus / ça dépend.
Ne pas donner la réponse : on lance la démo sur la slide suivante.
-->

---

## 🟢 Démo : les commandes disparaissent

```
Commandes comptées : 174491 (attendu : 1000000)
Commandes comptées : 247164 (attendu : 1000000)
Commandes comptées : 152176 (attendu : 1000000)
```

`count++`, c'est **trois** étapes : lire, ajouter 1, écrire.

```
Thread A                    Thread B
lit count (41)
                            lit count (41)
écrit 42
                            écrit 42   ← l'incrément de A est perdu
```

<div class="support">📖 Support §1.4 — Race condition</div>

<!--
Durée : 3 min
Démo live : java RaceDemo.java (support §1.4), lancer 3 fois. Le résultat change à chaque fois, et on perd la moitié des commandes ou plus.
Message : c'est une race condition. Le résultat dépend de l'ordre d'exécution, que personne ne contrôle.
Piège : avec 10 threads et 10 incréments, ça « marche » souvent. Un test qui passe ne prouve rien. L'ancienne démo du cours (1 000 threads, 1 incrément chacun, puis sleep) affichait toujours 1000 : elle ne démontrait rien.
Si la machine n'a qu'un cœur, la démo peut afficher 1000000 : utiliser le résultat pré-enregistré ci-dessus.
-->

---

## 🟢 Correction 1 : `AtomicInteger`

```java
class OrderCounter {
    private final AtomicInteger count = new AtomicInteger();

    void increment() {
        count.incrementAndGet(); // lire + ajouter + écrire : indivisible
    }

    int get() {
        return count.get();
    }
}
```

```
Commandes comptées : 1000000 (attendu : 1000000)
```

> Une **seule** variable partagée → un type atomique.

<div class="support">📖 Support §1.5 — AtomicInteger</div>

<!--
Durée : 2 min
Démo : AtomicCounterDemo.java, toujours 1000000.
Message : le processeur fournit une instruction atomique (compare-and-swap). Autres méthodes : addAndGet, updateAndGet, compareAndSet.
Limite : dès que deux variables doivent rester cohérentes entre elles, deux AtomicInteger ne suffisent plus.
-->

---

## 🟢 Correction 2 : `synchronized`

```java
class SalesStats {
    private int orderCount = 0;
    private BigDecimal revenue = BigDecimal.ZERO;

    synchronized void record(BigDecimal orderTotal) {
        orderCount++;                        // les deux champs
        revenue = revenue.add(orderTotal);   // changent ensemble
    }

    synchronized String summary() {          // la lecture aussi !
        return orderCount + " commandes, " + revenue + " €";
    }
}
```

<div class="support">📖 Support §1.6 — synchronized</div>

<!--
Durée : 3 min
Message : synchronized prend le verrou de l'objet. Un seul thread à la fois dans les méthodes synchronized de CET objet.
Question à la salle : que se passe-t-il si summary() n'est pas synchronized ? (On peut lire orderCount à jour et revenue pas encore : un état incohérent.)
Règle : tous les accès à l'état partagé, lectures comprises, passent par le même verrou.
Piège pour les avancés : volatile ne rend pas count++ atomique.
-->

---

## 🟢 Quel outil pour quel besoin ?

| Besoin | Outil |
|---|---|
| Un compteur, une variable | `AtomicInteger`, `AtomicLong` |
| Plusieurs variables cohérentes | `synchronized` |
| Une `Map` partagée | `ConcurrentHashMap` |
| Un objet qui ne change jamais | **rien** : `record`, `String`, `BigDecimal` |

> La meilleure protection contre les race conditions : l'**immuabilité**.

<div class="support">📖 Support §1.6 — tableau récapitulatif</div>

<!--
Durée : 1 min
Message : les records de S2 (Dish) sont immuables, on peut les partager entre mille threads sans aucune précaution. C'est pour ça qu'on a tant insisté sur l'immuabilité.
-->

---

## 🟢 `ConcurrentHashMap` : thread-safe… appel par appel

`DeliveryPlatform` (S2) stocke ses commandes dans une `HashMap` : **pas** faite pour plusieurs threads.

```java
// S4 : la plateforme devient thread-safe
private final Map<String, Order> orders = new ConcurrentHashMap<>();

public void placeOrder(Order order) {
    if (orders.putIfAbsent(order.getId(), order) != null) {
        throw new IllegalStateException("Commande déjà enregistrée");
    }
}
```

<div class="support">📖 Support §1.7 — DeliveryPlatform thread-safe</div>

<!--
Durée : 2 min
Message : chaque appel (get, put, remove) est sûr, sans verrou global.
putIfAbsent : vérifie l'absence ET insère, en une seule opération atomique. Jamais d'écrasement silencieux.
Attention au titre : « appel par appel ». La slide suivante montre pourquoi c'est important.
-->

---

## 🤔 Prédisez la sortie

```java
Map<Dish, Integer> unsafe = new ConcurrentHashMap<>();
Map<Dish, Integer> safe = new ConcurrentHashMap<>();
// 1 000 tâches virtuelles, chacune exécute 100 fois :
int current = unsafe.getOrDefault(pizza, 0);
unsafe.put(pizza, current + 1);

safe.merge(pizza, 1, Integer::sum);
// ...
IO.println(unsafe.get(pizza) + " / " + safe.get(pizza));
```

Les deux maps sont des `ConcurrentHashMap`. Même résultat ?

<div class="support">📖 Support §1.7 — DishPopularity.java</div>

<!--
Durée : 2 min
Question à la salle : les deux maps sont thread-safe, donc 100000 / 100000 ?
Réponse (démo DishPopularity.java) : « 71066 / 100000 » environ. get puis put = deux appels séparés : un autre thread écrit entre les deux.
Message : thread-safe ne veut pas dire atomique. Une SUITE d'appels n'est pas atomique.
Rappel S2 : Dish est un record, son equals/hashCode en fait une bonne clé.
-->

---

## 🟢 Les opérations atomiques de `ConcurrentHashMap`

| Méthode | Atomiquement… | FoodFast |
|---|---|---|
| `merge(k, 1, Integer::sum)` | ajoute ou crée | compter les plats |
| `putIfAbsent(k, v)` | insère si absent | enregistrer une commande |
| `compute(k, f)` | recalcule la valeur | décrémenter un stock |
| `remove(k)` | supprime et **renvoie** | consommer un code promo |

> Une suite `get` → `if` → `put` sur une donnée partagée est **suspecte**.

<div class="support">📖 Support §1.7 — tableau complet</div>

<!--
Durée : 2 min
Message : ces méthodes font la lecture, le test et l'écriture en une seule étape.
Piège : ConcurrentHashMap refuse null (clé et valeur).
Transition : « get → if → put »… c'est exactement la forme d'une faille de sécurité célèbre.
-->

---

## 🔒 TOCTOU : « vérifier puis agir »

**T**ime **O**f **C**heck to **T**ime **O**f **U**se (CWE-367)

1. Le code **vérifie** une condition : « ce code promo est-il valide ? »
2. Un autre thread change la situation
3. Le code **agit** comme si la condition était encore vraie

Une race condition **exploitable** : un attaquant envoie 200 requêtes identiques en même temps.

<div class="support">📖 Support §1.8 — TOCTOU</div>

<!--
Durée : 2 min
Message : c'est une vraie attaque sur les sites marchands : coupons, cartes cadeaux, virements, votes. Des outils comme Burp Suite envoient des rafales de requêtes simultanées pour l'exploiter.
-->

---

## 🔒 Le code promo `BIENVENUE10` (usage unique)

```java
private final Map<String, BigDecimal> unusedCodes =
        new ConcurrentHashMap<>();

// ❌ TOCTOU
BigDecimal redeem(String code) throws InterruptedException {
    BigDecimal amount = unusedCodes.get(code);  // CHECK
    if (amount == null) throw new InvalidPromoCodeException();
    saveDiscount(code, amount);                 // ≈ 5 ms en base
    unusedCodes.remove(code);                   // USE
    return amount;
}
```

<div class="support">📖 Support §1.8 — PromoCodeRace.java</div>

<!--
Durée : 2 min
Question à la salle : la map est une ConcurrentHashMap. Où est le problème ?
Laisser chercher : entre le get et le remove, 199 autres requêtes passent le get.
-->

---

## 🔒 Démo : 200 requêtes simultanées

```
Code BIENVENUE10 accepté 200 fois, refusé 0 fois
```

**2 000 €** de remise au lieu de 10 €.

- Chaque appel à la map est thread-safe…
- … mais la **séquence** `get` → `remove` ne l'est pas
- Les 200 threads passent le `get` avant le premier `remove`

<div class="support">📖 Support §1.8 — PromoCodeRace.java</div>

<!--
Durée : 2 min
Démo live : java PromoCodeRace.java. 200 sur 200 à chaque fois.
Honnêteté : le sleep de 5 ms (simulation de l'écriture en base) élargit la fenêtre pour que la démo soit reproductible. Sans lui, la faille existe toujours, elle est plus rare. Et un attaquant élargit la fenêtre lui-même en chargeant le serveur.
-->

---

## 🔒 La correction : vérifier ET agir en une étape

```java
// ✅ remove renvoie la valeur ET supprime l'entrée, atomiquement
BigDecimal redeem(String code) throws InterruptedException {
    BigDecimal amount = unusedCodes.remove(code); // CHECK + USE
    if (amount == null) throw new InvalidPromoCodeException();
    saveDiscount(code, amount);    // seul le gagnant arrive ici
    return amount;
}
```

```
remove atomique : accepté 1 fois sur 200
```

<div class="support">📖 Support §1.8 — PromoCodeSafe.java</div>

<!--
Durée : 2 min
Démo live : java PromoCodeSafe.java. 1 sur 200, à chaque fois.
Message : un seul thread peut recevoir la valeur ; tous les autres reçoivent null. Aucun verrou, et les perdants échouent tout de suite.
Même idée pour la dernière portion de tiramisu : stock.compute(...) (support §1.9).
-->

---

## 🔒 Et avec un verrou ?

<div class="cols"><div>

❌ **Ne protège pas**

```java
synchronized void deleteAll(
        User user) {
    if (user.isAdmin()) {
        orders.clear();
    }
}
// un autre thread retire les
// droits SANS prendre ce verrou
```

</div><div>

✅ **Protège**

```java
synchronized BigDecimal redeem(
        String code) {
    // CHECK… et USE ici
}
synchronized void addCode(...)
// TOUS les accès : CE verrou
```

</div></div>

<div class="support">📖 Support §1.8 — Pourquoi l'ancienne correction était fausse</div>

<!--
Durée : 2 min
Message : un verrou ne protège que contre les threads qui prennent le MÊME verrou. Deux conditions : (1) il couvre la vérification et l'action, (2) tous les codes qui modifient la donnée vérifiée le prennent.
Le code de gauche figurait dans l'ancienne version de ce cours : le verrou est celui de la plateforme, le thread qui modifie user ne le prend jamais.
Si on ne contrôle pas tous les accès : opération atomique sur la donnée elle-même. Le verrou sérialise aussi toutes les requêtes : goulot d'étranglement.
-->

---

## Concurrence : ce qu'il faut retenir

- `ExecutorService` en **try-with-resources**, threads **virtuels** pour les attentes
- `count++` n'est pas atomique ; un test qui passe ne prouve rien
- `AtomicInteger`, `synchronized` (**même verrou pour lire et écrire**)
- `ConcurrentHashMap` : `merge`, `compute`, `putIfAbsent`, `remove`
- 🔒 TOCTOU : vérification et action en **une seule** opération atomique

> Repérez les `if` qui testent un état partagé avant de le modifier.

<div class="support">📖 Support §1.9 à §1.13 — stock, deadlocks, fichiers, scoped values</div>

<!--
Durée : 1 min
Pour les avancés, dans le support : deadlocks (démo avec détection), TOCTOU sur les fichiers (CREATE_NEW), scoped values (final en Java 25). La concurrence structurée est encore en preview en Java 25 : on ne l'enseigne pas.
-->

---

<!-- _class: section -->

# Partie 2

## JDBC : le bug et la faille

<!--
Durée : 0,5 min
-->

---

## 🟢 JDBC et H2

Une `Map` en mémoire : **un redémarrage, et tout est perdu**.

```
Votre code ──► API JDBC (java.sql)
                  ▼
            Driver (.jar : H2, PostgreSQL…)
                  ▼
            Base de données
```

H2 **en mémoire** : zéro installation, une dépendance Maven.

```
jdbc:h2:mem:foodfast;DB_CLOSE_DELAY=-1
```

<div class="support">📖 Support §2.1 — JDBC et H2</div>

<!--
Durée : 2 min
Message : JDBC est l'API standard depuis 1997 ; JPA, Hibernate, Spring Data reposent dessus. Votre code ne dépend que de java.sql : changer de base, c'est changer de driver et d'URL.
DB_CLOSE_DELAY=-1 : la base reste en vie tant que la JVM tourne.
-->

---

## 🟢 Se connecter : trois objets, trois `close()`

```java
String url = "jdbc:h2:mem:foodfast;DB_CLOSE_DELAY=-1";
String user = System.getenv().getOrDefault("FOODFAST_DB_USER", "sa");
String password =
        System.getenv().getOrDefault("FOODFAST_DB_PASSWORD", "");

try (Connection conn = DriverManager.getConnection(url, user, password);
     PreparedStatement ps = conn.prepareStatement("SELECT H2VERSION()");
     ResultSet rs = ps.executeQuery()) {
    rs.next();
    IO.println("Connecté à H2 " + rs.getString(1));
} // rs, ps, puis conn sont fermés
```

<div class="support">📖 Support §2.2 — Connection et try-with-resources</div>

<!--
Durée : 2 min
Démo : H2Hello.java.
Message : Connection, PreparedStatement, ResultSet sont AutoCloseable : TOUJOURS dans un try-with-resources. Une connexion non fermée est une fuite : la base n'en accepte qu'un nombre limité.
Remarquez les identifiants : lus dans l'environnement. On y revient dans quelques slides.
-->

---

## 🟢 Le schéma : SQL lisible avec un text block

```java
private static final String CREATE_TABLE = """
        CREATE TABLE IF NOT EXISTS orders (
            id            VARCHAR(64)   PRIMARY KEY,
            customer_name VARCHAR(100)  NOT NULL,
            total         DECIMAL(10,2) NOT NULL,
            status        VARCHAR(20)   NOT NULL,
            created_at    TIMESTAMP     NOT NULL
        )
        """;
```

- `BigDecimal` ↔ `DECIMAL`, jamais `DOUBLE` (S2)
- `enum` ↔ `VARCHAR` (son `name()`), `LocalDateTime` ↔ `TIMESTAMP`

<div class="support">📖 Support §2.4 — Text blocks et types</div>

<!--
Durée : 2 min
Message : le text block (Java 15) évite les concaténations ligne à ligne et l'espace oublié en fin de ligne.
Piège : un text block est une simple String. Y coller une valeur avec + ou formatted() crée exactement une injection SQL.
-->

---

## 🟢 Écrire : `PreparedStatement`

```java
private static final String INSERT = """
        INSERT INTO orders (id, customer_name, total, status, created_at)
        VALUES (?, ?, ?, ?, ?)
        """;
void save(Order order) throws SQLException {
    try (Connection conn = connect();
         PreparedStatement ps = conn.prepareStatement(INSERT)) {
        ps.setString(1, order.getId());           // positions dès 1
        ps.setString(2, order.getCustomer().getName());
        ps.setBigDecimal(3, order.getTotal());
        ps.setString(4, order.getStatus().name());
        ps.setObject(5, order.getCreatedAt());    // LocalDateTime
        ps.executeUpdate();
    } // ...
```

<div class="support">📖 Support §2.5 — PreparedStatement</div>

<!--
Durée : 3 min
Message : un ? par valeur, puis setXxx(position, valeur). Les positions commencent à 1, pas 0 !
executeUpdate renvoie le nombre de lignes modifiées.
Le code est dans une classe OrderRepository : la seule classe qui connaît le SQL.
-->

---

## 🟢 Lire : `ResultSet` → objet

```java
Optional<OrderSummary> findById(String id) throws SQLException {
    try (Connection conn = connect();
         PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID)) {
        ps.setString(1, id);
        try (ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) return Optional.empty(); // aucune ligne
            return Optional.of(new OrderSummary(
                    rs.getString("id"),
                    rs.getString("customer_name"),
                    rs.getBigDecimal("total"),
                    OrderStatus.valueOf(rs.getString("status")),
                    rs.getObject("created_at", LocalDateTime.class)));
        }
    } // ...
```

<div class="support">📖 Support §2.6 — OrderRepository complet</div>

<!--
Durée : 3 min
Démo : OrderRepositoryDemo.java (save puis findById, puis findById sur un identifiant inconnu → Optional.empty).
Message : rs.next() avance d'une ligne et renvoie false s'il n'y en a plus. On transforme chaque ligne en objet : c'est le mapping.
Optional (S2) : « pas trouvé » n'est pas une erreur technique. L'appelant décide : orElseThrow(() -> new OrderNotFoundException(id)) (S3).
OrderSummary est un record : une vue immuable de ce qui est stocké.
-->

---

## 🔒 Injection SQL : la recherche par client

```java
// ❌ La saisie de l'utilisateur est collée dans le SQL
List<String> findByCustomerUnsafe(Connection conn, String name)
        throws SQLException {
    String sql = "SELECT id, customer_name, total FROM orders"
            + " WHERE customer_name = '" + name + "'";
    try (Statement st = conn.createStatement();
         ResultSet rs = st.executeQuery(sql)) {
        return readRows(rs);
    }
}
```

Recherche « Alice » → `[order-1 (Alice, 31.90 €)]`

<div class="support">📖 Support §2.7 — SqlInjectionDemo.java</div>

<!--
Durée : 2 min
Message : ça marche parfaitement… avec des clients honnêtes. CWE-89, catégorie Injection du Top 10 OWASP.
Transition : et si le client tape autre chose qu'un nom ?
-->

---

## 🤔 Prédisez la sortie

L'utilisateur saisit comme nom de client :

```
' OR '1'='1
```

```java
findByCustomerUnsafe(conn, "' OR '1'='1");
```

La table contient les commandes d'Alice, Bob et Chloé.

**Que renvoie la recherche ?**

<!--
Durée : 2 min
Question à la salle : une erreur SQL ? Une liste vide ? Autre chose ?
Faire écrire la requête SQL générée au tableau AVANT la démo.
-->

---

## 🔒 Démo : toutes les commandes de tous les clients

```
SQL exécuté : SELECT id, customer_name, total FROM orders
              WHERE customer_name = '' OR '1'='1'
[order-1 (Alice, 31.90 €), order-2 (Bob, 18.00 €),
 order-3 (Chloé, 42.50 €)]
```

- La première `'` **ferme** la chaîne SQL
- `OR '1'='1'` devient du **code** : vrai pour toutes les lignes
- Variantes : `admin' --` (contourne un login), `UNION SELECT` (lit d'autres tables)

<div class="support">📖 Support §2.7 — Injection SQL</div>

<!--
Durée : 2 min
Démo live : java SqlInjectionDemo.java.
Message : une donnée a changé la STRUCTURE de la requête. Selon les droits du compte, l'attaquant peut aussi modifier ou détruire des données.
-->

---

## 🔒 La correction : `PreparedStatement`

```java
// ✅ Structure figée ; la saisie n'est qu'une valeur
String sql =
    "SELECT id, customer_name, total FROM orders WHERE customer_name = ?";
try (PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setString(1, name);
    try (ResultSet rs = ps.executeQuery()) {
        return readRows(rs);
    }
}
```

Même saisie `' OR '1'='1` → `[]` (aucun client ne s'appelle ainsi).

> La requête est analysée **avant** de connaître la valeur : la valeur ne devient **jamais** du SQL.

<div class="support">📖 Support §2.7 — Pourquoi PreparedStatement protège</div>

<!--
Durée : 2 min
Message : ce n'est pas de l'« échappement » : la base reçoit la structure, puis la valeur séparément. Elle cherche un client qui s'appelle littéralement ' OR '1'='1.
Pièges : (1) les filtres maison (remplacer les apostrophes, interdire OR) sont contournés ; (2) « cette valeur ne vient pas de l'utilisateur » : un en-tête, un champ caché, une donnée relue en base peuvent être contrôlés par un attaquant.
-->

---

## 🔒 Les limites du `?`

Un `?` remplace une **valeur**, jamais un nom de colonne. `ORDER BY ?` ne trie pas.

```java
// ✅ Liste blanche : la saisie CHOISIT, elle n'est jamais copiée
String sortColumn(String userChoice) {
    return switch (userChoice) {
        case "date" -> "created_at";
        case "total" -> "total";
        default -> throw new IllegalArgumentException("Tri non autorisé");
    };
}
String sql = "SELECT id FROM orders ORDER BY " + sortColumn(choice);
```

<div class="support">📖 Support §2.8 — Liste blanche</div>

<!--
Durée : 2 min
Message : la concaténation est ici sûre parce qu'on ne concatène QUE des constantes du programme. La saisie « total; DROP TABLE orders » lève une IllegalArgumentException.
Rappel S2 : switch en expression.
-->

---

## 🔒 Les secrets hors du code

<div class="cols"><div>

❌ **Codé en dur**

```java
String password = "secret";
```

- finit dans Git, pour toujours
- des robots scannent GitHub
- fuite → **changer** le secret

</div><div>

✅ **Lu dans l'environnement**

```java
String password = System
    .getenv("FOODFAST_DB_PASSWORD");
```

- `.env` dans `.gitignore`
- gestionnaire de secrets en prod
- en prod : **pas** de valeur par défaut

</div></div>

<div class="support">📖 Support §2.3 — Identifiants hors du code</div>

<!--
Durée : 2 min
Message : l'ancienne version de ce cours contenait password = "secret". Supprimer la ligne au commit suivant ne sert à rien : l'historique Git la garde.
La valeur par défaut "" des démos n'a de sens que pour H2 en mémoire. En production, si la variable manque, l'application refuse de démarrer.
Piège (support) : le toString() d'un record affiche tous ses champs, donc le mot de passe dans les logs. Redéfinir toString().
-->

---

## 🔒 Le moindre privilège

```java
// Au déploiement, compte administrateur :
st.execute("GRANT SELECT, INSERT ON orders TO foodfast_app");

// L'application se connecte avec foodfast_app :
st.execute("DROP TABLE orders");
```

```
INSERT autorisé
DROP TABLE refusé (code 90096)
```

> Si une injection passe quand même, les dégâts sont limités aux droits du compte.

<div class="support">📖 Support §2.3 — LeastPrivilege.java</div>

<!--
Durée : 2 min
Démo possible : LeastPrivilege.java (le mot de passe du compte applicatif est généré avec SecureRandom, jamais écrit dans le code).
Message : défense en profondeur. PreparedStatement empêche l'injection ; le moindre privilège limite les dégâts si un jour une injection passe quand même.
Question à la salle : quels droits pour le compte qui affiche le menu ? (SELECT sur les plats, rien d'autre.)
-->

---

## Ce qu'il faut retenir

- 🟢 `ExecutorService` en try-with-resources ; threads virtuels pour les attentes
- 🟢 Race condition → `AtomicInteger`, `synchronized`, `ConcurrentHashMap.merge`
- 🔒 TOCTOU → vérifier **et** agir en une seule opération atomique
- 🟢 JDBC : `Connection`, `PreparedStatement`, `ResultSet` en try-with-resources
- 🔒 Injection SQL → **toujours** des `?` ; liste blanche pour les colonnes
- 🔒 Secrets dans l'environnement ; compte au moindre privilège

<!--
Durée : 2 min
Question à la salle : pour chaque ligne, quel est le bug et quelle est la faille ?
AA7 : savoir les reconnaître dans un code qu'on vous donne, et les corriger.
-->

---

## Pour aller plus loin (support)

| § | Sujet | Niveau |
|---|---|---|
| 1.9 – 1.11 | Stock et `compute`, deadlocks, TOCTOU fichiers | 🔒 🔵 |
| 1.12 | Scoped values (Java 25) | 🟣 |
| 2.9 | Transactions : `commit` / `rollback` | 🔵 |
| 3.1 | Hacher des mots de passe : PBKDF2 | 🔵 🔒 |
| 3.2 | API KDF (Java 25) : pourquoi HKDF ≠ mot de passe | 🟣 🔒 |
| 3.3 | Désérialisation Java : chaînes de gadgets | 🔒 |

<div class="support">📖 Support §1.9 à §3.3</div>

<!--
Durée : 1 min
Message : tous les exemples du support sont complets et compilés. Les profils cybersécurité : lisez au moins les §3.1 à §3.3.
-->
