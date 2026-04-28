---
title: "Java - Séance 2"
description: "De la Donnée à l'Objet - Le Cœur de FoodFast"
author: "Mehdi Elketroussi"
theme: gaia
size: 16:9
paginate: true
marp: true
---

# Séance 2 : De la Donnée à l'Objet

**Le Cœur de FoodFast**

**Objectif :** Apprendre les types et structures essentiels de Java, puis les utiliser pour modéliser les objets métier de FoodFast.

---

## Plan de la Séance

**Partie 1 : Les Outils pour Modéliser**
1. L'API Collections Framework
2. Les `enum` - Constantes typées
3. Types Riches du JDK (`BigDecimal`, `LocalDateTime`, `UUID`)
4. `Optional` - Éviter `null`

---

**Partie 2 : POO - Construire les Objets Métier**
5. Révision : Classe, Objet, Constructeur
6. Encapsulation
7. **Interfaces, Classes Abstraites et Polymorphisme** 
8. Le Contrat Object : `toString`, `equals`, `hashCode`

<!--
NOTES PRÉSENTATEUR :
- Timing total : 2h05-2h30 (ajout POO avancée : +15-20min)
- Insister sur le fait que les outils viennent AVANT la POO (inversion par rapport au plan initial)
- Rappeler que les étudiants auront les supports pour approfondir
- Focus : ce dont ils ont besoin pour le TP (Questions 4 et 5)
- Section 7 (POO avancée) : indispensable pour comprendre Lambdas en Séance 3
-->

---

<!-- PARTIE 1 : LES OUTILS -->

# Partie 1 : Les Outils pour Modéliser

<!--
NOTES PRÉSENTATEUR :
- Partie 1 : ~60-70min
- Objectif : Donner les outils AVANT la POO
- Stratégie : Vue d'ensemble Collections (20min), enum (12min), Types riches (25min), Optional (5min)
- Message clé : "Ces outils existent pour résoudre des problèmes réels"
-->

---

## 1. L'API Collections Framework

### Pourquoi les Collections ?

<!--
NOTES PRÉSENTATEUR - Collections Framework (25-30min) :
- Commencer par le problème : tableaux fixes vs besoins dynamiques
- Montrer la hiérarchie rapidement (slide suivant)
- Focus sur ArrayList et HashMap (99% des cas)
- Annoncer : "Détails dans les supports, on voit l'essentiel pour le TP"
- PIÈGE à anticiper : "Pourquoi mon objet ne se retrouve pas dans la Map ?" → equals/hashCode (Partie 2)
-->

---

### Le Problème Sans Collections

```java
// ❌ Sans collections : impossible de gérer un nombre variable d'éléments
String student1 = "Alice";
String student2 = "Bob";
String student3 = "Charlie";
// Et si on a 100 étudiants ? 1000 ?

// ❌ Avec un tableau : taille fixe
String[] students = new String[3]; // Que faire si on veut ajouter un 4ème étudiant ?
students[0] = student1;
students[1] = student2;
students[2] = student3;
```

---

### La Solution : Collections

```java
// ✅ Avec collections : simple et flexible
List<String> students = new ArrayList<>();
students.add("Alice");
students.add("Bob");
students.add("Charlie");
students.add("David"); // ✅ Peut grandir indéfiniment !

System.out.println("Nombre d'étudiants : " + students.size()); // 4
String first = students.get(0); // Accès par index : "Alice"
```

**Le Java Collections Framework (JCF)** = ensemble d'interfaces et de classes pour stocker et manipuler des groupes d'objets.

---

### La Hiérarchie des Collections

```
Iterable<T>
  └─ Collection<T>
       ├─ List<T>      → ArrayList, LinkedList
       ├─ Set<T>       → HashSet, TreeSet
       └─ Queue<T>     → ArrayDeque

Map<K,V> (à part)    → HashMap, TreeMap
```

**Important :**
- `Collection<T>` : interface de base pour `List`, `Set`, `Queue`
- `Map<K,V>` : **n'est PAS une Collection** (structure clé-valeur)

<!--
NOTES PRÉSENTATEUR :
- Ne pas s'attarder sur ce schéma (2min max)
- Juste montrer qu'il y a une structure logique
- Insister : Map est À PART (question fréquente)
- Transition : "On va voir List, Set, Map rapidement, puis Queue très vite"
-->

---

## List - La Séquence Ordonnée

<!--
NOTES PRÉSENTATEUR - List (7min) :
- Caractéristiques principales : ORDRE + DOUBLONS + INDEX
- Toujours recommander ArrayList (99% des cas)
- LinkedList : mention rapide, "uniquement si besoin spécifique"
- Montrer l'exemple de code, insister sur les doublons autorisés
-->

### Caractéristiques
- **Ordonnée** : conserve l'ordre d'insertion
- **Doublons autorisés** : peut contenir plusieurs fois le même élément
- **Accès par index** : `get(0)`, `get(1)`, ...

---

### Implémentations de List

| Implémentation | Structure interne | Quand l'utiliser ? |
|---|---|---|
| **`ArrayList`** ✅ | Tableau dynamique | **LE CHOIX PAR DÉFAUT** (99% des cas). Rapide pour la lecture. |
| `LinkedList` | Liste doublement chaînée | Nombreux ajouts/suppressions en début/fin de liste. |

**Règle simple :** Utilisez toujours `ArrayList`, sauf besoin très spécifique.

---

### Exemple : ArrayList

```java
// Création
List<String> books = new ArrayList<>();

// Ajout d'éléments
books.add("1984");
books.add("Le Petit Prince");
books.add("1984"); // ✅ Doublon OK (plusieurs exemplaires du même livre)

// Taille
System.out.println(books.size()); // 3

// Accès par index
String first = books.get(0); // "1984"
String second = books.get(1); // "Le Petit Prince"

// Parcours
for (String book : books) {
    System.out.println(book);
}

// Modification
books.set(0, "Fahrenheit 451"); // Remplace l'élément à l'index 0

// Suppression
books.remove("1984"); // Supprime la première occurrence
```

---

### 🔗 Lien avec FoodFast

**Dans le TP, vous utiliserez `List` pour :**
- Stocker des résultats de recherche (Question 6 - Séance 3)
- `List<Order> findOrdersByCustomer(...)`

**Question :** Pourquoi utiliser `List` plutôt qu'un tableau ?
**Réponse :** Taille dynamique, méthodes utiles (`add`, `remove`, `contains`)

---

## Set - L'Unicité Garantie

### Caractéristiques
- **Aucun doublon** : unicité garantie
- **Pas d'ordre** (sauf `TreeSet`)
- **Pas d'accès par index**

---

### Implémentations de Set

| Implémentation | Ordre | Quand l'utiliser ? |
|---|---|---|
| **`HashSet`** ✅ | Aucun | **LE CHOIX PAR DÉFAUT**. Très rapide (O(1)). |
| `TreeSet` | Trié | Besoin d'un ordre naturel. Plus lent (O(log n)). |

---

### Exemple : HashSet

```java
// Création
Set<String> cities = new HashSet<>();

// Ajout d'éléments
cities.add("Paris");
cities.add("Lyon");
cities.add("Paris"); // ❌ Ignoré (doublon)

// Taille
System.out.println(cities.size()); // 2 (pas 3 !)

// Test d'appartenance
if (cities.contains("Paris")) {
    System.out.println("Paris est dans le Set");
}
```

---

### ⚠️ Piège : Set avec des Objets Personnalisés

```java
Set<Student> uniqueStudents = new HashSet<>();

Student alice1 = new Student("12345", "Alice");
uniqueStudents.add(alice1);

Student alice2 = new Student("12345", "Alice"); // Même numéro étudiant
uniqueStudents.add(alice2); // Est-ce un doublon ?

System.out.println(uniqueStudents.size()); // ???
```

**Réponse :** Dépend de si `Student` implémente `equals()` et `hashCode()` !
→ On verra ça dans la Partie 2.

---

### 🔗 Lien avec FoodFast

**Dans le TP, vous pourriez utiliser `Set` pour :**
- Garantir l'unicité des clients ayant commandé aujourd'hui
- `Set<Customer>` (évite les doublons automatiquement)

**Question :** Pourquoi `Set` plutôt que `List` ?
**Réponse :** Garantie d'unicité (pas de doublon possible)

---

## Map - Le Dictionnaire (Clé → Valeur)

<!--
NOTES PRÉSENTATEUR - Map (7min) : SECTION CRITIQUE pour le TP !
- C'est LE concept le plus important pour Questions 4 et 5
- Insister : Map<Dish, Integer> dans Order + Map<String, Order> dans DeliveryPlatform
- Montrer les 2 exemples : String→Objet ET Objet→Quantité
- ANNONCER le piège equals/hashCode (slide dédié à la fin)
- Prendre le temps sur les méthodes essentielles (put, get, containsKey)
-->

### Caractéristiques
- Associe une **clé unique** à une **valeur**
- Recherche ultra-rapide par clé
- **Pas une Collection** (n'hérite pas de `Collection`)

---

### Implémentations de Map

| Implémentation | Ordre des clés | Quand l'utiliser ? |
|---|---|---|
| **`HashMap`** ✅ | Aucun | **LE CHOIX PAR DÉFAUT**. Très rapide (O(1)). |
| `TreeMap` | Clés triées | Besoin de parcourir les clés dans l'ordre. Plus lent (O(log n)). |

---

### Exemple 1 : Map String → String (Annuaire)

```java
// Annuaire téléphonique : Nom → Numéro
Map<String, String> phoneBook = new HashMap<>();

// Ajout
phoneBook.put("Alice", "06-12-34-56-78");
phoneBook.put("Bob", "06-98-76-54-32");
phoneBook.put("Charlie", "07-11-22-33-44");

// Recherche par clé (ultra-rapide O(1))
String alicePhone = phoneBook.get("Alice"); // "06-12-34-56-78"

// Test d'existence
if (phoneBook.containsKey("David")) {
    System.out.println("Numéro trouvé");
} else {
    System.out.println("Contact introuvable"); // ✅ Ce cas
}

// Taille
System.out.println("Nombre de contacts : " + phoneBook.size()); // 3
```

---

### Exemple 2 : Map String → Integer (Inventaire)

```java
// Inventaire : Produit → Quantité en stock
Map<String, Integer> inventory = new HashMap<>();

// Ajout
inventory.put("Laptop", 15);
inventory.put("Mouse", 50);
inventory.put("Keyboard", 30);

// Récupération
int laptopStock = inventory.get("Laptop"); // 15

// Modification (mise à jour de stock)
inventory.put("Laptop", 12); // Maintenant 12 laptops

// Incrémentation
int currentStock = inventory.get("Mouse");
inventory.put("Mouse", currentStock + 10); // 50 + 10 = 60
```

---

### Méthodes Essentielles de Map

```java
Map<String, String> phoneBook = new HashMap<>();

// Ajout / Modification
phoneBook.put("Alice", "06-12-34-56-78");

// Récupération
String phone = phoneBook.get("Alice"); // null si la clé n'existe pas

// Test d'existence
boolean exists = phoneBook.containsKey("Alice"); // true

// Suppression
phoneBook.remove("Alice");

// Taille
int size = phoneBook.size();
```

---

### Parcours de Map

```java
Map<String, String> phoneBook = new HashMap<>();

// Parcours des clés
for (String name : phoneBook.keySet()) {
    System.out.println(name);
}

// Parcours des valeurs
for (String phone : phoneBook.values()) {
    System.out.println(phone);
}

// Parcours des paires clé-valeur
for (Map.Entry<String, String> entry : phoneBook.entrySet()) {
    String name = entry.getKey();
    String phoneNumber = entry.getValue();
    System.out.println(name + " : " + phoneNumber);
}
```

---

### 🔗 Lien avec FoodFast

**Dans le TP, vous utiliserez `Map` de 2 façons :**

**Question 4 - Dans `Order` :**
- **Besoin :** Stocker les plats d'une commande avec leur quantité
- **Type de Map :** `Map<???, Integer>` (Objet → Quantité)
- **Méthodes utiles :** `put()`, `entrySet()` pour le calcul du prix total

**Question 5 - Dans `DeliveryPlatform` :**
- **Besoin :** Retrouver rapidement une commande par son ID
- **Type de Map :** `Map<String, ???>` (ID → Objet)
- **Méthodes utiles :** `put()`, `get()`, `containsKey()`

**Question :** Quel type d'objet mettre en clé dans `Order` ?
**Indice :** Pensez à ce que vous voulez compter (quantité de quoi ?)

---

### ⚠️ Piège Critique : equals() et hashCode()

<!--
NOTES PRÉSENTATEUR - PIÈGE CRITIQUE (3min) :
- RALENTIR ICI, c'est le piège #1 quand on utilise des objets comme clés !
- Montrer le code qui semble marcher mais retourne null
- Expliquer simplement : "HashMap cherche au mauvais endroit sans hashCode()"
- TEASER : "On va résoudre ça dans la Partie 2 avec equals/hashCode"
- Objectif : qu'ils comprennent POURQUOI c'est nécessaire, pas juste "c'est une règle"
-->

```java
// ❌ PROBLÈME : Sans equals/hashCode dans Book
class Book {
    private String title;
    private String isbn;
    // Pas de equals() ni hashCode() implémentés !
}

Map<Book, Integer> library = new HashMap<>();

Book book1 = new Book("1984", "978-0451524935");
library.put(book1, 3); // 3 exemplaires disponibles

Book book2 = new Book("1984", "978-0451524935"); // Même livre !
System.out.println(library.get(book2)); // null ❌ (devrait être 3 !)

// Pourquoi ? HashMap utilise hashCode() pour trouver l'élément !
// Si book1 et book2 ont des hashCode différents, HashMap ne les retrouve pas.
```

**✅ SOLUTION :** Implémenter `equals()` et `hashCode()` dans `Book`.
→ On verra ça dans la Partie 2 !

---

## Queue/Deque - Files d'Attente

### Caractéristiques
- **Queue** : FIFO (First-In, First-Out)
- **Deque** : Double-ended (pile OU file)

### Implémentation
| Implémentation | Quand l'utiliser ? |
|---|---|
| **`ArrayDeque`** ✅ | File d'attente ou pile |

---

### Exemple : Queue (FIFO)

```java
// File d'attente de tâches à traiter
Queue<String> taskQueue = new ArrayDeque<>();

// Ajouter à la fin
taskQueue.offer("Envoyer email");
taskQueue.offer("Générer rapport");
taskQueue.offer("Sauvegarder données");

// Retirer du début (FIFO - First In, First Out)
String next = taskQueue.poll(); // "Envoyer email" (premier ajouté)
String next2 = taskQueue.poll(); // "Générer rapport"

// Voir le premier sans le retirer
String peek = taskQueue.peek(); // "Sauvegarder données" (toujours dans la queue)
```

---

### Exemple : Deque comme Pile (LIFO)

```java
// Pile (Last-In, First-Out)
Deque<String> stack = new ArrayDeque<>();

// Empiler
stack.push("A");
stack.push("B");
stack.push("C");

// Dépiler
String top = stack.pop(); // "C" (dernier ajouté)
String next = stack.pop(); // "B"
```

---

## Récapitulatif : Quelle Collection Choisir ?

| Besoin | Interface | Implémentation |
|---|---|---|
| Liste ordonnée avec doublons | `List` | **`ArrayList`** ✅ |
| Garantir l'unicité | `Set` | **`HashSet`** ✅ |
| Associer clé → valeur | `Map` | **`HashMap`** ✅ |
| File d'attente (FIFO) | `Queue` | **`ArrayDeque`** ✅ |
| Pile (LIFO) | `Deque` | **`ArrayDeque`** ✅ |

### 🔗 Lien avec FoodFast

**Dans le TP, les Collections seront utilisées pour :**
- **`Map`** → Stocker plats avec quantités (Question 4) + Retrouver commandes par ID (Question 5)
- **`List`** → Stocker résultats de recherche (Question 6 - Séance 3)
- **`Queue`** → Optionnel : File d'attente des livraisons en cours (Bonus)

---

### Pour aller plus loin (dans les supports)

**Autres implémentations (non couvertes aujourd'hui) :**
- `LinkedList` vs `ArrayList` : différences de performances
- `LinkedHashSet` / `LinkedHashMap` : ordre d'insertion
- `TreeSet` / `TreeMap` : tri automatique avec `Comparable` / `Comparator`
- Thread-safety : `ConcurrentHashMap` (Séance 4)

**En Séance 3 :**
- Manipulation avancée avec l'API Stream
- `filter()`, `map()`, `collect()` sur les collections

---

## 2. Les enum - Constantes Typées

<!--
NOTES PRÉSENTATEUR - enum (12-15min) :
- Pattern Problème→Solution très clair ici
- Montrer le DANGER des int/String (pas de sécurité)
- enum = type sûr à la compilation
- Exemples FoodFast : DishSize, OrderStatus (Questions 4 du TP)
- Switch expression moderne (Java 12+) : montrer la syntaxe élégante
-->

### Le Problème avec les Constantes Classiques

---

### ❌ Mauvaise Approche : int ou String

```java
// Exemple : Priorité d'une tâche avec des int
public static final int PRIORITY_LOW = 1;
public static final int PRIORITY_MEDIUM = 2;
public static final int PRIORITY_HIGH = 3;

public void setPriority(int priority) {
    this.priority = priority;
}

// Rien n'empêche :
setPriority(999); // ❌ Valeur invalide acceptée !
setPriority(-1);  // ❌ Encore pire
```

---

```java
// Exemple : Statut d'une tâche avec des String
public static final String STATUS_TODO = "TODO";
public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
public static final String STATUS_DONE = "DONE";

public void setStatus(String status) {
    this.status = status;
}

// Rien n'empêche :
setStatus("EN_COURS"); // ❌ Typo
setStatus("todo");     // ❌ Minuscule
setStatus(null);       // ❌ null
```

**Problèmes :**
- Aucune sécurité à la compilation
- Facile de se tromper
- Pas d'autocomplétion dans l'IDE

---

### ✅ Solution : enum

```java
public enum Priority {
    LOW,
    MEDIUM,
    HIGH
}
```

**Avantages :**
- **Type sûr** : impossible de passer une valeur invalide
- **Autocomplétion** dans l'IDE
- **Contrôle à la compilation**

---

### Utilisation des enum

```java
// Déclaration
Priority priority = Priority.MEDIUM;

// ✅ Type sûr à la compilation
public void setPriority(Priority priority) {
    this.priority = priority;
}

// Impossible de faire :
// setPriority(999); // ❌ Erreur de compilation
// setPriority("MEDIUM"); // ❌ Erreur de compilation

// ✅ Seules les valeurs valides sont acceptées
setPriority(Priority.LOW);    // ✅
setPriority(Priority.MEDIUM); // ✅
setPriority(Priority.HIGH);   // ✅
```

---

### Switch avec enum

```java
Priority priority = Priority.MEDIUM;

// Style moderne (Java 12+)
String description = switch (priority) {
    case LOW -> "Basse priorité";
    case MEDIUM -> "Priorité moyenne";
    case HIGH -> "Haute priorité";
};

System.out.println(description); // "Priorité moyenne"

// Style classique
switch (priority) {
    case LOW:
        System.out.println("Basse");
        break;
    case MEDIUM:
        System.out.println("Moyenne");
        break;
    case HIGH:
        System.out.println("Haute");
        break;
}
```

---

### Méthodes Utiles des enum

```java
// Toutes les valeurs
Priority[] allPriorities = Priority.values();
for (Priority p : allPriorities) {
    System.out.println(p); // LOW, MEDIUM, HIGH
}

// Nom en String
String name = Priority.MEDIUM.name(); // "MEDIUM"

// Position (ordinal)
int position = Priority.MEDIUM.ordinal(); // 1 (commence à 0)

// Comparaison (utiliser == avec les enum)
if (priority == Priority.HIGH) {
    System.out.println("Tâche urgente !");
}

// Parsing (String → enum)
Priority parsed = Priority.valueOf("HIGH"); // Priority.HIGH
```

---

### 🔗 Lien avec FoodFast

**Dans le TP Question 4, vous devrez créer 2 enum :**

**1. Taille des plats (`DishSize`) :**
- Valeurs possibles : SMALL, MEDIUM, LARGE
- Utilisé dans la classe `Dish`

**2. Statut des commandes (`OrderStatus`) :**
- Valeurs possibles : ?
- Pensez aux différentes étapes d'une commande (en attente, en préparation, etc.)
- Utilisé dans la classe `Order`

**Question :** Quelles valeurs mettre dans `OrderStatus` ?
**Indice :** Une commande peut être en attente, en préparation, terminée ou annulée

---

## 3. Types Riches du JDK

<!--
NOTES PRÉSENTATEUR - Types Riches (20-25min) :
- Section ESSENTIELLE pour le TP
- BigDecimal (10min) : MONTRER le bug du double en live si possible !
- LocalDateTime (7min) : Comparaison avec ancienne API Date
- UUID (5min) : Génération d'IDs uniques
- Message : "Ces types existent pour des raisons TECHNIQUES précises"
-->

### A. BigDecimal - La Monnaie

<!--
NOTES PRÉSENTATEUR - BigDecimal (10-12min) : SECTION CRITIQUE !
- Démarrer avec le BUG : 0.1 + 0.2 != 0.3 (montrer en live si possible)
- Expliquer simplement : binaire vs décimal
- Règles d'or : String, immutable, compareTo
- Erreur classique : oublier de récupérer le résultat (immutabilité)
- Cas d'usage : calculateTotalPrice() dans Order
-->

---

### ❌ Le Bug Catastrophique du `double`

```java
// Test simple
double price = 0.1 + 0.2;
System.out.println(price); // 0.30000000000000004 ❌

// Catastrophe en production !
double total = 0.0;
for (int i = 0; i < 100; i++) {
    total += 0.01; // Ajouter 1 centime 100 fois
}
System.out.println(total); // 0.9999999999999999 au lieu de 1.0 ❌

// Conséquence réelle
double price1 = 10.0;
double price2 = 9.70;
double discount = 0.30;

if (price1 - discount == price2) {
    System.out.println("Prix égaux");
} else {
    System.out.println("Prix différents"); // ❌ Affiché à cause de l'imprécision !
}
```

**Raison :** Les nombres flottants (`float`, `double`) sont stockés en **binaire**, certains nombres décimaux ne peuvent pas être représentés exactement.

---

### ✅ Solution : BigDecimal

```java
// ✅ Création (ATTENTION : toujours String !)
BigDecimal price1 = new BigDecimal("19.99"); // ✅ Précis
BigDecimal price2 = new BigDecimal("8.50");  // ✅ Précis

BigDecimal wrong = new BigDecimal(0.1); // ❌ ERREUR : Imprécis !
// Utilise la représentation binaire de 0.1, qui est déjà imprécise

// ✅ Toujours utiliser String ou méthode valueOf
BigDecimal correct = BigDecimal.valueOf(0.1); // ✅ OK
```

**Règle d'or #1 :** Toujours créer un `BigDecimal` avec un `String`, jamais avec un `double`.

---

### Opérations avec BigDecimal

```java
BigDecimal price1 = new BigDecimal("19.99");
BigDecimal price2 = new BigDecimal("8.50");

// ⚠️ BigDecimal est IMMUTABLE (comme String)
// Les opérations retournent un NOUVEAU BigDecimal

// Addition
BigDecimal total = price1.add(price2); // 28.49

// Soustraction
BigDecimal diff = price1.subtract(price2); // 11.49

// Multiplication
BigDecimal vat = total.multiply(new BigDecimal("0.20")); // TVA 20%

// Division (ATTENTION : arrondi obligatoire !)
BigDecimal avgPrice = total.divide(new BigDecimal("2"), RoundingMode.HALF_UP);
```

**Règle d'or #2 :** `BigDecimal` est **immutable**. N'oubliez pas de récupérer le résultat !

---

### ❌ Erreur Classique : Oublier l'immutabilité

```java
BigDecimal total = new BigDecimal("100.00");

// ❌ ERREUR : on ne récupère pas le résultat
total.add(new BigDecimal("10.00"));
System.out.println(total); // 100.00 (inchangé !)

// ✅ CORRECT : récupérer le résultat
total = total.add(new BigDecimal("10.00"));
System.out.println(total); // 110.00 ✅
```

---

### Comparaison avec BigDecimal

```java
BigDecimal price1 = new BigDecimal("12.99");
BigDecimal price2 = new BigDecimal("8.50");

// ❌ ERREUR : Ne PAS utiliser equals() pour comparer des valeurs
// (equals() compare aussi l'échelle : 2.0 != 2.00)

// ✅ CORRECT : Utiliser compareTo()
int comparison = price1.compareTo(price2);

if (comparison > 0) {
    System.out.println("price1 est plus cher"); // ✅ Ce cas
} else if (comparison < 0) {
    System.out.println("price2 est plus cher");
} else {
    System.out.println("Prix égaux");
}

// Raccourcis
if (price1.compareTo(price2) > 0) { ... } // price1 > price2
```

---

### Arrondi et Conversion

```java
BigDecimal price = new BigDecimal("19.999");

// Arrondi à 2 décimales
BigDecimal rounded = price.setScale(2, RoundingMode.HALF_UP); // 20.00

// Modes d'arrondi courants
RoundingMode.HALF_UP   // Arrondi mathématique (2.5 → 3)
RoundingMode.HALF_DOWN // 2.5 → 2
RoundingMode.UP        // Toujours vers le haut
RoundingMode.DOWN      // Toujours vers le bas (troncature)

// Conversion en double (uniquement pour affichage !)
double asDouble = price.doubleValue();
System.out.println("Prix : " + asDouble + " €");
```

**⚠️ Attention :** Ne jamais reconvertir un `double` en `BigDecimal` pour des calculs !

---

### 🔗 Lien avec FoodFast

**Dans le TP Question 4, vous utiliserez `BigDecimal` pour :**

**1. Dans la classe `Dish` :**
- Attribut `price` de type `BigDecimal` (jamais `double` !)
- Validation : le prix doit être positif (`compareTo(BigDecimal.ZERO) > 0`)

**2. Dans la classe `Order` :**
- Méthode `calculateTotalPrice()` qui retourne un `BigDecimal`
- **Algorithme :** Pour chaque plat dans la Map :
  - Récupérer le prix du plat
  - Multiplier par la quantité
  - Ajouter au total

**Question :** Comment accumuler un total avec `BigDecimal` ?
**Indice :** Commencez avec `BigDecimal.ZERO` et utilisez `.add()`

---

### Résumé : BigDecimal

**Règles d'or :**
1. ✅ Toujours créer avec `String` : `new BigDecimal("19.99")`
2. ✅ `BigDecimal` est **immutable** (comme `String`)
3. ✅ Comparer avec `compareTo()`, pas `equals()`
4. ✅ Spécifier le mode d'arrondi avec `divide()` et `setScale()`
5. ❌ Ne JAMAIS utiliser `double` pour la monnaie

<!--
NOTES PRÉSENTATEUR :
- Insister sur ces 5 règles, elles reviendront dans les erreurs du TP !
- Faire répéter par les étudiants : "String, immutable, compareTo"
-->

---

### B. LocalDateTime - Les Dates

<!--
NOTES PRÉSENTATEUR - LocalDateTime (5-7min) :
- Rapide comparaison avec java.util.Date (deprecated)
- java.time = moderne, immutable, claire
- Pour le TP : LocalDateTime.now() dans Order
- Mention rapide : LocalDate, LocalTime, ZonedDateTime (Séance 4)
- Ne pas s'attarder, pas le concept le plus critique
-->

---

### Pourquoi LocalDateTime ?

**L'ancienne API (`java.util.Date`) était terrible :**
- Mutable (dangereux)
- Mal conçue (année commence à 1900, mois de 0 à 11)
- Thread-unsafe
- Deprecated

**`java.time` (Java 8+) :**
- **Immutable** : thread-safe, pas de modification accidentelle
- API claire et moderne
- Types spécialisés : `LocalDate`, `LocalTime`, `LocalDateTime`, `ZonedDateTime`

---

### Création de LocalDateTime

```java
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

// Date et heure actuelles
LocalDateTime now = LocalDateTime.now();
System.out.println(now); // 2025-01-20T14:30:00

// Date et heure spécifiques
LocalDateTime specific = LocalDateTime.of(2025, 1, 20, 14, 30);
// 20 janvier 2025, 14h30

// Avec Month (plus lisible)
LocalDateTime readable = LocalDateTime.of(2025, Month.JANUARY, 20, 14, 30);
```

---

### Manipulation de LocalDateTime

```java
LocalDateTime now = LocalDateTime.now();

// ⚠️ LocalDateTime est IMMUTABLE
// Les opérations retournent un NOUVEAU LocalDateTime

// Ajouter
LocalDateTime tomorrow = now.plusDays(1);
LocalDateTime nextWeek = now.plusWeeks(1);
LocalDateTime nextMonth = now.plusMonths(1);
LocalDateTime nextYear = now.plusYears(1);
LocalDateTime inTwoHours = now.plusHours(2);

// Soustraire
LocalDateTime yesterday = now.minusDays(1);
LocalDateTime lastWeek = now.minusWeeks(1);
```

---

### Comparaison de LocalDateTime

```java
LocalDateTime date1 = LocalDateTime.of(2025, 1, 20, 14, 0);
LocalDateTime date2 = LocalDateTime.of(2025, 1, 21, 14, 0);

// Comparaisons
if (date1.isBefore(date2)) {
    System.out.println("date1 est avant date2"); // ✅
}

if (date2.isAfter(date1)) {
    System.out.println("date2 est après date1"); // ✅
}

if (date1.isEqual(date1)) {
    System.out.println("Dates égales"); // ✅
}

// compareTo (comme BigDecimal)
int comparison = date1.compareTo(date2);
if (comparison < 0) {
    System.out.println("date1 est avant date2");
}
```

---

### Formatage de LocalDateTime

```java
LocalDateTime now = LocalDateTime.now();

// Format par défaut
System.out.println(now); // 2025-01-20T14:30:00

// Format personnalisé
DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
String formatted = now.format(formatter);
System.out.println(formatted); // 20/01/2025 14:30

// Formats courants
DateTimeFormatter.ISO_LOCAL_DATE_TIME // 2025-01-20T14:30:00
DateTimeFormatter.ISO_LOCAL_DATE      // 2025-01-20
DateTimeFormatter.ISO_LOCAL_TIME      // 14:30:00

// Parsing (String → LocalDateTime)
String text = "20/01/2025 14:30";
LocalDateTime parsed = LocalDateTime.parse(text, formatter);
```

---

### 🔗 Lien avec FoodFast

**Dans le TP Question 4, vous utiliserez `LocalDateTime` pour :**

**Dans la classe `Order` :**
- Attribut `orderDate` de type `LocalDateTime`
- Initialisé dans le constructeur avec `LocalDateTime.now()`
- Permet de savoir quand une commande a été créée

**Bonus (optionnel) :**
- Méthode pour vérifier si une commande est ancienne (> 1 heure)
- Utiliser `.minusHours()` et `.isBefore()`

---

### Autres Types de java.time

```java
// Juste la date (sans heure)
LocalDate date = LocalDate.now(); // 2025-01-20
LocalDate birthday = LocalDate.of(1990, 5, 15);

// Juste l'heure (sans date)
LocalTime time = LocalTime.now(); // 14:30:00
LocalTime openingTime = LocalTime.of(9, 0); // 09:00

// Date + heure + fuseau horaire (pour plus tard)
ZonedDateTime zoned = ZonedDateTime.now(); // 2025-01-20T14:30:00+01:00[Europe/Paris]
```

**Pour le TP :** Utilisez `LocalDateTime` uniquement.
`ZonedDateTime` sera vu en Séance 4 (concurrence).

---

### C. UUID - Identifiants Uniques

---

### Pourquoi UUID ?

**Besoin :** Générer des identifiants uniques pour les commandes.

**Alternatives :**
- ❌ Auto-incrément en base de données (1, 2, 3, ...) :
  - Nécessite la base de données
  - Prédictible (faille de sécurité)
  - Problème en distribué (collisions)

- ✅ UUID (Universally Unique IDentifier) :
  - Génération côté application (pas besoin de la DB)
  - 128 bits = 2^128 possibilités (pratiquement aucune collision)
  - Distribué (plusieurs serveurs peuvent générer en parallèle)

---

### Génération d'UUID

```java
import java.util.UUID;

// Génération aléatoire (version 4)
UUID uuid = UUID.randomUUID();
System.out.println(uuid);
// Exemple : 550e8400-e29b-41d4-a716-446655440000

// En String (pour stocker)
String uuidString = UUID.randomUUID().toString();
System.out.println(uuidString);
// Exemple : "a3bb189e-8bf9-3888-9912-ace4e6543002"

// Parsing (String → UUID)
UUID parsed = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
```

---

### 🔗 Lien avec FoodFast

**Dans le TP Question 4, vous utiliserez `UUID` pour :**

**Dans la classe `Order` :**
- Attribut `id` de type `String`
- Généré dans le constructeur avec `UUID.randomUUID().toString()`
- Garantit que chaque commande a un identifiant unique

**Pourquoi stocker en `String` et pas en `UUID` ?**
- Plus simple à manipuler (pas de conversion)
- Facile à utiliser comme clé de `Map<String, Order>`
- Format standard pour les APIs et la base de données

**Question :** Pourquoi UUID et pas un simple compteur (1, 2, 3...) ?
**Réponse :** UUID fonctionne en distribué (plusieurs serveurs) et n'est pas prédictible

---

## 4. Optional - Éviter `null`

<!--
NOTES PRÉSENTATEUR - Optional (5-8min) :
- Peut être rapide si le temps manque
- Citer Tony Hoare : "erreur à un milliard de dollars"
- Pour le TP : findOrderById() retourne Optional<Order>
- Règle importante : UNIQUEMENT comme type de retour (pas paramètre, pas attribut)
- Si temps : montrer les 4 styles d'utilisation
-->

---

### Le Problème de `null`

```java
// Méthode qui peut ne rien trouver
public User findUserById(String id) {
    return users.get(id); // Peut retourner null
}

// Utilisation
User user = findUserById("user-123");
System.out.println(user.getName()); // NullPointerException si user == null ❌

// Obligé de vérifier partout
if (user != null) {
    String email = user.getEmail();
    if (email != null) {
        System.out.println(email);
    }
}
```

**Tony Hoare** (créateur de `null`) l'a appelé sa **"erreur à un milliard de dollars"**.

---

### Solution : Optional

```java
import java.util.Optional;

// Retour de méthode
public Optional<User> findUserById(String id) {
    User user = users.get(id);
    return Optional.ofNullable(user); // Encapsule null de manière sûre
}
```

**`Optional<T>` :** Un conteneur qui peut contenir une valeur de type `T` ou être vide.

---

### Utilisation de Optional

```java
Optional<User> maybeUser = findUserById("user-123");

// Style 1 : Test classique
if (maybeUser.isPresent()) {
    User user = maybeUser.get();
    System.out.println(user.getName());
}

// Style 2 : Valeur par défaut
User user = maybeUser.orElse(defaultUser);

// Style 3 : Exception si absent
User user = maybeUser.orElseThrow(() ->
    new UserNotFoundException("Utilisateur introuvable"));

// Style 4 : Fonctionnel (lambda)
maybeUser.ifPresent(user ->
    System.out.println("Utilisateur trouvé : " + user.getName())
);
```

---

### Création d'Optional

```java
// Avec une valeur non-null
Optional<String> opt1 = Optional.of("Hello");

// Avec une valeur potentiellement null
String value = getValue(); // Peut être null
Optional<String> opt2 = Optional.ofNullable(value);

// Optional vide
Optional<String> opt3 = Optional.empty();
```

---

### 🔗 Lien avec FoodFast

**Dans le TP Question 5, vous utiliserez `Optional` pour :**

**Dans la classe `DeliveryPlatform` :**
- Méthode `findOrderById(String orderId)` qui retourne `Optional<Order>`
- Pourquoi Optional ? La commande peut ne pas exister dans la Map

**Implémentation :**
```java
public Optional<Order> findOrderById(String orderId) {
    Order order = orders.get(orderId); // Peut être null
    return Optional.ofNullable(order); // ✅ Encapsule null
}
```

**Utilisation côté appelant :**
- Utiliser `.isPresent()` + `.get()` pour vérifier et récupérer
- Ou `.orElseThrow()` pour lancer une exception si absent

**Question :** Pourquoi ne pas retourner `null` directement ?
**Réponse :** `Optional` force l'appelant à gérer le cas "pas trouvé"

---

### ⚠️ Règles d'utilisation de Optional

**✅ À FAIRE :**
- Utiliser comme **type de retour** de méthode
- Pour indiquer qu'une valeur peut être absente

**❌ À NE PAS FAIRE :**
- Utiliser comme **paramètre** de méthode
- Utiliser comme **attribut** de classe
- Utiliser dans les **collections** (`List<Optional<T>>` ❌)

```java
// ❌ MAUVAIS
public void setCustomer(Optional<Customer> customer) { ... }
private Optional<String> name; // Attribut

// ✅ BON
public Optional<Customer> findCustomer(String id) { ... }
```

---

<!-- PARTIE 2 : POO -->

# Partie 2 : POO - Construire les Objets Métier

<!--
NOTES PRÉSENTATEUR - Partie 2 (~65-80min) :
- Timing : Révision (8min), Encapsulation (15min), POO Avancée (15-20min), Contrat Object (30-35min)
- Objectif : Maintenant qu'on a les outils, on construit les objets
- Message : "C'est grâce aux outils de la Partie 1 que la POO devient puissante"
- Section POO Avancée (7) : CRITIQUE pour comprendre List, Map, et Lambdas (Séance 3)
- Section Contrat Object (8) : 30min minimum, c'est LE concept critique !
-->

---

## 5. Révision : Classe, Objet, Constructeur

<!--
NOTES PRÉSENTATEUR - Révision (8-10min) :
- Rapide, déjà vu en Séance 1
- Juste rappeler : classe = modèle, objet = instance, new = création
- Montrer le PROBLÈME de l'accès direct → transition vers encapsulation
- Ne pas s'attarder, ils connaissent déjà
-->

---

### Rappel Séance 1

**Classe :** Modèle / Plan
**Objet :** Instance concrète en mémoire
**`new` :** Création d'un objet

```java
// Classe = modèle
public class Dish {
    String name;
    BigDecimal price;
    DishSize size;
}

// Objets = instances
Dish pizza = new Dish();    // Objet 1
Dish burger = new Dish();   // Objet 2
Dish salad = new Dish();    // Objet 3
```

---

### Le Constructeur

```java
public class Dish {
    String name;
    BigDecimal price;
    DishSize size;

    // Constructeur : même nom que la classe, pas de type de retour
    public Dish(String name, BigDecimal price, DishSize size) {
        this.name = name;
        this.price = price;
        this.size = size;
    }
}

// Utilisation
Dish pizza = new Dish(
    "Pizza Margherita",
    new BigDecimal("12.99"),
    DishSize.MEDIUM
);
```

**`this` :** Référence à l'objet courant (pour distinguer paramètre et attribut).

---

### Le Problème : Accès Direct aux Champs

```java
public class Dish {
    String name;
    BigDecimal price;
    DishSize size;

    public Dish(String name, BigDecimal price, DishSize size) {
        this.name = name;
        this.price = price;
        this.size = size;
    }
}

// Utilisation
Dish pizza = new Dish("Pizza", new BigDecimal("12.99"), DishSize.MEDIUM);

// ❌ PROBLÈME : Accès direct sans contrôle
pizza.price = new BigDecimal("-100"); // Prix négatif !
pizza.size = null;                     // Taille null !
pizza.name = "";                       // Nom vide !
```

**On ne contrôle rien !** → D'où l'**encapsulation**.

---

## 6. Encapsulation

<!--
NOTES PRÉSENTATEUR - Encapsulation (12-15min) :
- Concept fondamental de la POO
- Montrer le DANGER de l'accès direct (slide précédent)
- private + getters/setters = contrôle
- Validation dans constructeur ET setters
- Immutabilité : aller plus loin (final + pas de setters)
- Pour FoodFast : Dish et Customer doivent être immutables
-->

---

### Le Principe d'Encapsulation

**Définition :** Cacher les détails internes d'une classe et exposer une API publique contrôlée.

**Objectifs :**
1. **Protéger les données** : empêcher les modifications invalides
2. **Contrôler l'accès** : valider les données avant modification
3. **Garantir la cohérence** : maintenir l'objet dans un état valide

---

### Modificateurs d'Accès

| Modificateur | Visible depuis... |
|---|---|
| `private` | La classe elle-même uniquement |
| (package) | Le même package |
| `protected` | Package + sous-classes |
| `public` | Partout |

**Règle par défaut :**
- Attributs : `private` ✅
- Méthodes : `public` ✅
- Classe : `public` ✅

---

### ✅ Solution : private + Getters/Setters

```java
public class Dish {
    // ✅ Attributs privés
    private String name;
    private BigDecimal price;
    private DishSize size;

    // Constructeur avec validation
    public Dish(String name, BigDecimal price, DishSize size) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Le nom ne peut pas être vide");
        }
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le prix doit être positif");
        }
        if (size == null) {
            throw new IllegalArgumentException("La taille ne peut pas être null");
        }

        this.name = name;
        this.price = price;
        this.size = size;
    }

    // Suite →
```

---

```java
    // Getters (lecture seule)
    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public DishSize getSize() {
        return size;
    }

    // Setter avec validation
    public void setPrice(BigDecimal price) {
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Le prix doit être positif");
        }
        this.price = price;
    }
}
```

---

### Utilisation

```java
Dish pizza = new Dish("Pizza", new BigDecimal("12.99"), DishSize.MEDIUM);

// ✅ Lecture via getters
String name = pizza.getName();
BigDecimal price = pizza.getPrice();

// ✅ Modification contrôlée via setter
pizza.setPrice(new BigDecimal("14.99")); // OK

// ❌ Impossible d'accéder directement
// pizza.price = new BigDecimal("-100"); // Erreur de compilation

// ❌ Validation dans le setter
try {
    pizza.setPrice(new BigDecimal("-10")); // Exception
} catch (IllegalArgumentException e) {
    System.out.println(e.getMessage()); // "Le prix doit être positif"
}
```

---

### Immutabilité : Aller Plus Loin

**Objet immutable :** Ne peut pas être modifié après création.

```java
public class Dish {
    // ✅ Attributs final
    private final String name;
    private final BigDecimal price;
    private final DishSize size;

    public Dish(String name, BigDecimal price, DishSize size) {
        // Validation
        this.name = name;
        this.price = price;
        this.size = size;
    }

    // ✅ Uniquement des getters (pas de setters)
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public DishSize getSize() { return size; }
}
```

---

**Avantages de l'immutabilité :**
- **Thread-safe** : pas de problème de concurrence (Séance 4)
- **Plus sûr** : pas de modification accidentelle
- **Peut être clé de Map** : hashCode ne change jamais

**Pour FoodFast :** Privilégiez l'immutabilité pour `Dish`, `Customer`.

---

## 7. Interfaces, Classes Abstraites et Polymorphisme

<!--
NOTES PRÉSENTATEUR - POO Avancée (15-20min) :
- Section AJOUTÉE pour combler la lacune POO
- Indispensable pour comprendre List, Map, Lambdas (Séance 3)
- Ordre : Interface (8min), Polymorphisme (5min), Classe abstraite (5min)
- Message clé : "Vous utilisez déjà des interfaces ! List, Map, Set..."
- Transition naturelle : Object est une classe (héritage) → Contrat Object
-->

---

### A. Interfaces - Le Contrat

<!--
NOTES PRÉSENTATEUR - Interface (8min) :
- Définition : contrat que les classes s'engagent à respecter
- Syntaxe : interface + implements
- Exemple concret : List (interface) vs ArrayList (implémentation)
- Pourquoi ? Flexibilité, changement d'implémentation sans casser le code
-->

---

#### Qu'est-ce qu'une Interface ?

**Définition :** Une interface est un **contrat** qui définit un ensemble de méthodes que les classes doivent implémenter.

```java
// Interface = contrat
public interface Drawable {
    void draw();
    double calculateArea();
}
```

**Caractéristiques :**
- Contient des **signatures de méthodes** (pas d'implémentation)
- Une classe **implémente** une interface avec `implements`
- Une classe peut implémenter **plusieurs interfaces**

---

#### Exemple : Interface Drawable

```java
// Interface
public interface Drawable {
    void draw();
    double calculateArea();
}
```

---

#### Implémentations de Drawable

```java
// Implémentation dans Circle
public class Circle implements Drawable {
    private double radius;

    public Circle(double radius) {
        this.radius = radius;
    }

    @Override
    public void draw() {
        System.out.println("Dessin d'un cercle de rayon " + radius);
    }

    @Override
    public double calculateArea() {
        return Math.PI * radius * radius;
    }
}
```

---

```java
// Implémentation dans Rectangle
public class Rectangle implements Drawable {
    private double width;
    private double height;

    @Override
    public void draw() {
        System.out.println("Dessin d'un rectangle " + width + "x" + height);
    }

    @Override
    public double calculateArea() {
        return width * height;
    }
}
```

---

#### Pourquoi des Interfaces ?

**❌ Sans interface : Code rigide**
```java
public class ShapeRenderer {
    public void renderCircle(Circle circle) {
        circle.draw(); // ❌ Uniquement Circle
    }

    public void renderRectangle(Rectangle rect) {
        rect.draw(); // ❌ Uniquement Rectangle
    }
}
```

**✅ Avec interface : Code flexible**
```java
public class ShapeRenderer {
    public void render(Drawable shape) { // ✅ N'importe quel Drawable
        shape.draw();
        System.out.println("Aire : " + shape.calculateArea());
    }
}

// Utilisation
renderer.render(circle);    // Circle implements Drawable
renderer.render(rectangle); // Rectangle implements Drawable aussi
renderer.render(triangle);  // Triangle implements Drawable aussi
```

**Avantages :**
- **Flexibilité** : changer l'implémentation sans casser le code
- **Testabilité** : créer des mocks facilement
- **Découplage** : le code dépend du contrat, pas de l'implémentation

---

#### Interfaces dans le JDK : List, Set, Map

```java
// ✅ Vous utilisez déjà des interfaces !

// List est une INTERFACE
List<String> books = new ArrayList<>(); // ArrayList IMPLÉMENTE List

// On peut changer l'implémentation facilement
List<String> books = new LinkedList<>(); // ✅ Fonctionne aussi !

// Pourquoi ? Parce que ArrayList ET LinkedList implémentent List
```

**Hiérarchie :**
```
Interface List<T>
  ├─ class ArrayList<T> implements List<T>
  └─ class LinkedList<T> implements List<T>

Interface Map<K,V>
  ├─ class HashMap<K,V> implements Map<K,V>
  └─ class TreeMap<K,V> implements Map<K,V>
```

---

#### Syntaxe Complète

```java
// Interface (Java 8+)
public interface Resizable {
    // Méthode abstraite (obligatoire à implémenter)
    void resize(double factor);

    // Méthode par défaut (optionnelle, implémentation fournie)
    default void reset() {
        System.out.println("Reset to default size");
    }

    // Constante (public static final implicite)
    double MAX_SCALE = 10.0;
}

// Implémentation
public class Circle implements Drawable, Resizable {
    private double radius;

    @Override
    public void draw() { ... }

    @Override
    public double calculateArea() { ... }

    @Override
    public void resize(double factor) {
        this.radius *= factor;
    }

    // reset() héritée de Resizable (pas besoin de l'implémenter)
}
```

---

### 🔗 Lien avec FoodFast

**Dans le TP, vous pourriez créer une interface (Bonus avancé) :**

**Exemple : Interface `MenuItem`**
- Méthodes communes à `Dish` et d'autres types d'items du menu
- Méthodes à définir : `String getName()`, `BigDecimal getPrice()`

**Exemple : Interface `Payable`**
- Pour les objets qui peuvent être payés (`Order`, etc.)
- Méthode : `BigDecimal calculateAmount()`

**Question :** Quel est l'avantage de créer une interface `MenuItem` ?
**Réponse :** Pouvoir traiter tous les items du menu de manière uniforme

---

### B. Polymorphisme - Une Forme, Plusieurs Comportements

<!--
NOTES PRÉSENTATEUR - Polymorphisme (5-7min) :
- Concept clé : "Une référence de type parent peut pointer vers un objet enfant"
- Expliquer : List<Dish> = new ArrayList<>() (POURQUOI ça marche)
- Montrer le bénéfice : changer d'implémentation sans tout recoder
- Lien avec Séance 3 : Lambdas = implémentation d'interface fonctionnelle
-->

---

#### Qu'est-ce que le Polymorphisme ?

**Définition :** La capacité d'une référence de type parent (interface ou classe) à pointer vers un objet de type enfant (implémentation ou sous-classe).

```java
// Type de la variable : List (interface)
// Type de l'objet : ArrayList (implémentation)
List<String> books = new ArrayList<>();
```

**Polymorphisme = "Plusieurs formes"**
- La référence `books` est de type `List`
- L'objet pointé est un `ArrayList`
- ✅ Fonctionne car `ArrayList implements List`

---

#### Exemple Concret : Flexibilité

```java
public class Library {
    // ✅ Déclarer avec l'interface (type le plus général)
    private List<String> books = new ArrayList<>();

    public void addBook(String book) {
        books.add(book);
    }

    public List<String> getBooks() {
        return books;
    }
}

// Demain, on peut changer l'implémentation facilement
private List<String> books = new LinkedList<>(); // ✅ Rien d'autre à changer !
```

**Règle d'or :** Déclarez avec l'**interface**, instanciez avec l'**implémentation**.
```java
List<String> books = new ArrayList<>();     // ✅ BON
ArrayList<String> books = new ArrayList<>(); // ❌ Trop spécifique
```

---

#### Polymorphisme avec Méthodes

```java
public class ShapeProcessor {
    // Accepte N'IMPORTE QUEL objet qui implémente Drawable
    public void process(Drawable shape) {
        shape.draw();
        double area = shape.calculateArea();
        System.out.println("Aire : " + area);
    }
}

// Utilisation
ShapeProcessor processor = new ShapeProcessor();

Circle circle = new Circle(5.0);
processor.process(circle); // ✅ Circle implements Drawable

Rectangle rect = new Rectangle(10, 5);
processor.process(rect); // ✅ Rectangle implements Drawable aussi
```

**Avantage :** Le même code fonctionne avec **différents types** d'objets.

---

#### Polymorphisme dans les Collections

```java
// Stocker différents types de formes
List<Drawable> shapes = new ArrayList<>();

// Tous implémentent Drawable
shapes.add(new Circle(5.0));
shapes.add(new Rectangle(10, 5));
shapes.add(new Triangle(3, 4, 5));

// Traiter tous les éléments de manière uniforme
double totalArea = 0.0;
for (Drawable shape : shapes) {
    shape.draw();
    totalArea += shape.calculateArea(); // Polymorphisme !
}

System.out.println("Aire totale : " + totalArea);
```

**Chaque objet appelle SA propre implémentation de `draw()` et `calculateArea()`.**

---

### 🔗 Lien avec FoodFast

**Dans le TP, vous utilisez déjà le polymorphisme :**

**Exemple 1 : Collections**
```java
List<Order> orders = new ArrayList<>();  // ✅ Interface = Implémentation
Map<String, Order> ordersById = new HashMap<>();  // ✅ Interface = Implémentation
```

**Exemple 2 : Optional**
- `Optional<Order> findOrderById(String id)`
- Permet de retourner soit un `Order` soit "vide" (polymorphisme de l'absence)

**Question :** Pourquoi écrire `List<Order>` et pas `ArrayList<Order>` ?
**Réponse :** Flexibilité ! On peut changer `ArrayList` en `LinkedList` facilement

---

### C. Classes Abstraites - Entre Interface et Classe

<!--
NOTES PRÉSENTATEUR - Classe Abstraite (5min) :
- Moins critique pour le TP, mais concept important
- Différence avec interface : peut avoir implémentation partielle
- Exemple : classe Payment avec méthodes communes
- Quand l'utiliser : code commun à partager entre sous-classes
- Rapide, ne pas trop détailler
-->

---

#### Qu'est-ce qu'une Classe Abstraite ?

**Définition :** Une classe qui ne peut pas être instanciée directement et qui peut contenir des méthodes abstraites (sans implémentation) ET des méthodes concrètes (avec implémentation).

**Entre interface et classe concrète :**
- Comme une **interface** : peut avoir des méthodes abstraites
- Comme une **classe** : peut avoir des attributs et des méthodes implémentées

```java
// Classe abstraite
public abstract class Animal {
    protected String name;
    protected int age;

    public Animal(String name, int age) {
        this.name = name;
        this.age = age;
    }

    // Méthode abstraite (à implémenter dans les sous-classes)
    public abstract void makeSound();

    // Méthode concrète (commune à toutes les sous-classes)
    public void displayInfo() {
        System.out.println(name + " a " + age + " ans");
    }
}
```

---

#### Exemple : Hiérarchie d'Animaux

```java
// Classe abstraite
public abstract class Animal {
    protected String name;
    protected int age;

    public Animal(String name, int age) {
        this.name = name;
        this.age = age;
    }

    // Méthode abstraite
    public abstract void makeSound();

    // Méthode concrète
    public void displayInfo() {
        System.out.println(name + " a " + age + " ans");
    }
}
```

---

#### Sous-classes Concrètes

```java
// Sous-classe Dog
public class Dog extends Animal {
    public Dog(String name, int age) {
        super(name, age);
    }

    @Override
    public void makeSound() {
        System.out.println(name + " fait: Woof! Woof!");
    }
}

// Sous-classe Cat
public class Cat extends Animal {
    public Cat(String name, int age) {
        super(name, age);
    }

    @Override
    public void makeSound() {
        System.out.println(name + " fait: Meow!");
    }
}
```

---

#### Utilisation

```java
// ❌ Impossible d'instancier une classe abstraite
Animal animal = new Animal("Max", 5); // ERREUR de compilation

// ✅ Instancier une sous-classe concrète
Animal dog = new Dog("Rex", 3);
Animal cat = new Cat("Mimi", 2);

// Polymorphisme : même type (Animal), comportements différents
dog.displayInfo(); // Rex a 3 ans
dog.makeSound();   // Rex fait: Woof! Woof!

cat.displayInfo(); // Mimi a 2 ans
cat.makeSound();   // Mimi fait: Meow!
```

---

#### Interface vs Classe Abstraite : Quand utiliser quoi ?

| Critère | Interface | Classe Abstraite |
|---------|-----------|------------------|
| **Héritage multiple** | ✅ Une classe peut implémenter plusieurs interfaces | ❌ Une classe ne peut étendre qu'une seule classe |
| **État (attributs)** | ❌ Pas d'attributs d'instance | ✅ Peut avoir des attributs |
| **Implémentation** | ❌ Pas d'implémentation (sauf `default`) | ✅ Peut avoir des méthodes implémentées |
| **Constructeur** | ❌ Pas de constructeur | ✅ Peut avoir un constructeur |
| **Quand l'utiliser** | Définir un **contrat** | Partager du **code commun** |

**Règle simple :**
- **Interface** : "peut faire" (Drawable, Comparable, Serializable)
- **Classe abstraite** : "est un" avec code partagé (Animal, Vehicle, Shape)

---

### 🔗 Lien avec FoodFast

**Les classes abstraites ne sont PAS requises pour le TP de base.**

**Bonus avancé (optionnel) :**
- Créer une classe abstraite `MenuItem` avec:
  - Attributs communs : `name`, `basePrice`
  - Méthode abstraite : `BigDecimal getPrice()`
  - Méthode concrète : `String getName()`

- Sous-classes possibles :
  - `Dish extends MenuItem` (prix variable selon size)
  - `Drink extends MenuItem` (prix variable selon température)

**Question :** Quel est l'avantage d'une classe abstraite `MenuItem` ?
**Réponse :** Partager le code commun (name, getName()) entre différents types d'items

---

### Résumé : POO Avancée

**Interface :**
- ✅ Contrat (méthodes à implémenter)
- ✅ Héritage multiple
- ✅ Flexibilité maximale
- Exemple : `List`, `Map`, `Drawable`

**Classe Abstraite :**
- ✅ Code commun partagé
- ✅ Peut avoir attributs et constructeur
- ❌ Héritage simple uniquement
- Exemple : `Animal`, `MenuItem`

**Polymorphisme :**
- ✅ Référence parent → objet enfant
- ✅ Flexibilité, réutilisabilité
- Exemple : `List<String> = new ArrayList<>()`

<!--
NOTES PRÉSENTATEUR :
- Faire le lien avec Partie 1 : List, Map, Set sont des INTERFACES
- Transition : "Maintenant qu'on comprend l'héritage, parlons de Object (la classe mère)"
- Ces concepts seront essentiels pour Séance 3 (Lambdas = interface fonctionnelle)
-->

---

## 8. Le Contrat Object : toString, equals, hashCode

<!--
NOTES PRÉSENTATEUR - Contrat Object (30-35min) : SECTION LA PLUS IMPORTANTE !
- C'est le cœur de la séance, prendre le temps nécessaire
- Ordre : toString (5min), equals (10min), hashCode (12min), démo complète (5min)
- MONTRER le schéma HashMap avec buckets (slide dédié)
- Faire le lien avec le piège Map de la Partie 1
- Objectif : qu'ils comprennent POURQUOI, pas juste copier-coller du code
- Insister : TOUJOURS implémenter equals ET hashCode ENSEMBLE
-->

---

### Pourquoi Ce Contrat ?

<!--
NOTES PRÉSENTATEUR :
- Toutes les classes héritent de Object
- Par défaut, ces méthodes ne font pas ce qu'on veut
- Si Map/Set → OBLIGATOIRE d'implémenter equals/hashCode
-->

Toutes les classes Java héritent de `Object` :

```java
public class Object {
    public String toString() { ... }
    public boolean equals(Object obj) { ... }
    public int hashCode() { ... }
}
```

**Par défaut, ces méthodes ne font pas ce qu'on veut.**

**Si vous mettez vos objets dans des collections (`Map`, `Set`), vous DEVEZ les implémenter !**

---

### A. toString() - Pour le Débogage

<!--
NOTES PRÉSENTATEUR - toString (5min) :
- Le plus simple des 3
- Par défaut : NomClasse@AdresseMémoire (inutile)
- Override pour affichage lisible
- Utilité : debug, logs, println()
- Pas critique pour le TP mais bonne pratique
-->

---

#### Par Défaut (Hérité de Object)

```java
Person person = new Person("Alice", 25);
System.out.println(person);
// Person@2a139a55 ❌ Pas lisible !
```

**Format :** `NomClasse@AdresseMémoire` (inutile pour nous)

---

#### ✅ Implémentation Personnalisée

```java
public class Person {
    private final String name;
    private final int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return "Person{" +
               "name='" + name + '\'' +
               ", age=" + age +
               '}';
    }
}
```

---

#### Résultat

```java
Person person = new Person("Alice", 25);
System.out.println(person);
// Person{name='Alice', age=25} ✅ Lisible !

// Utile pour le débogage
List<Person> people = List.of(new Person("Alice", 25), new Person("Bob", 30));
System.out.println(people);
// [Person{name='Alice', age=25}, Person{name='Bob', age=30}]
```

**Utilité :**
- Débogage
- Logs
- Affichage console

---

### B. equals() - Comparer par Valeur

<!--
NOTES PRÉSENTATEUR - equals (10-12min) :
- Problème : == compare les RÉFÉRENCES, pas le CONTENU
- Montrer le schéma mémoire (2 objets différents en mémoire)
- Solution : override equals() pour comparer les champs
- Pattern en 3 étapes : même référence ? null/type différent ? comparaison champs
- Utiliser Objects.equals() pour gérer null automatiquement
- Mentionner le contrat (réflexif, symétrique, transitif) mais ne pas insister
-->

---

#### Le Problème du `==`

```java
Person alice1 = new Person("Alice", 25);
Person alice2 = new Person("Alice", 25);

System.out.println(alice1 == alice2); // false ❌

// Pourquoi ? == compare les RÉFÉRENCES mémoire, pas le CONTENU !
```

```
Mémoire :
alice1 ──> [Objet Person en @1234]  name="Alice", age=25
alice2 ──> [Objet Person en @5678]  name="Alice", age=25

alice1 == alice2  →  @1234 == @5678  →  false
```

**On veut comparer le CONTENU, pas les références !**

---

#### ✅ Solution : equals()

```java
public class Person {
    private final String name;
    private final int age;

    @Override
    public boolean equals(Object obj) {
        // 1. Vérifier si c'est la même référence (optimisation)
        if (this == obj) return true;

        // 2. Vérifier si obj est null ou d'un autre type
        if (obj == null || getClass() != obj.getClass()) return false;

        // 3. Cast et comparaison des champs
        Person person = (Person) obj;
        return age == person.age &&
               Objects.equals(name, person.name);
    }
}
```

**`Objects.equals(a, b)` :** Méthode helper qui gère `null` automatiquement.

---

#### Résultat

```java
Person alice1 = new Person("Alice", 25);
Person alice2 = new Person("Alice", 25);

System.out.println(alice1.equals(alice2)); // true ✅

// Maintenant on compare le contenu !
```

---

#### Le Contrat de equals()

Pour être correct, `equals()` doit respecter :

1. **Réflexif :** `a.equals(a)` = `true`
2. **Symétrique :** si `a.equals(b)` alors `b.equals(a)`
3. **Transitif :** si `a.equals(b)` et `b.equals(c)` alors `a.equals(c)`
4. **Cohérent :** appels multiples donnent le même résultat
5. **Null :** `a.equals(null)` = `false`

Notre implémentation respecte ce contrat ✅

---

### C. hashCode() - Pour les Collections

<!--
NOTES PRÉSENTATEUR - hashCode (10-12min) : SECTION CRITIQUE !
- C'est LE concept qui bloque tout le monde dans le TP
- Reprendre l'exemple du piège Map de la Partie 1
- MONTRER le schéma HashMap avec buckets (slide dédié)
- Expliquer : HashMap utilise hashCode() pour trouver le "bucket"
- Règle d'or : si a.equals(b) alors a.hashCode() == b.hashCode()
- Utiliser Objects.hash() avec les MÊMES champs que equals()
- Insister : TOUJOURS implémenter les 2 ensemble, JAMAIS l'un sans l'autre
-->

---

#### Le Problème Sans hashCode()

```java
// Implémenté equals() mais PAS hashCode()
public class Person {
    @Override
    public boolean equals(Object obj) { ... } // ✅ Implémenté

    // ❌ hashCode() pas implémenté (utilise celui de Object)
}

// Test
Map<Person, String> phoneBook = new HashMap<>();

Person alice1 = new Person("Alice", 25);
phoneBook.put(alice1, "06-12-34-56-78");

Person alice2 = new Person("Alice", 25); // Même personne !
System.out.println(phoneBook.get(alice2)); // null ❌ (devrait être le numéro !)
```

**Pourquoi ?** `HashMap` utilise `hashCode()` pour trouver l'élément !

---

#### Comment HashMap Fonctionne

<!--
NOTES PRÉSENTATEUR :
- RALENTIR ici, c'est le schéma le plus important du cours !
- Dessiner au tableau si possible pour bien montrer le concept
- Expliquer : hashCode → bucket → equals
- Sans bon hashCode, on cherche au mauvais endroit
- Avec bon hashCode, on trouve le bon bucket, puis equals() compare
-->

```
HashMap interne :
┌─────────────────────────┐
│ Bucket 0:  []           │
│ Bucket 1:  []           │
│ Bucket 2:  [pizza1 -> 5]│  ← hashCode(pizza1) % taille = 2
│ Bucket 3:  []           │
│ ...                     │
└─────────────────────────┘

Recherche de pizza2 :
1. Calcul hashCode(pizza2) → 9876
2. Bucket = 9876 % taille = 7 (différent de 2 !)
3. Bucket 7 est vide → retourne null ❌
```

**HashMap ne trouve pas pizza2 car il cherche au mauvais endroit !**

---

#### Le Contrat equals/hashCode

> **Règle d'or :** Si deux objets sont égaux selon `equals()`, ils **DOIVENT** avoir le même `hashCode()`.

```
Si a.equals(b) → a.hashCode() == b.hashCode()
```

**⚠️ L'inverse n'est pas obligatoire :**
- Deux objets différents peuvent avoir le même hashCode (collision)
- Mais deux objets égaux doivent avoir le même hashCode

---

#### ✅ Implémentation de hashCode()

```java
public class Person {
    private final String name;
    private final int age;

    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }
}
```

**`Objects.hash(...)` :** Méthode helper qui combine les hashCodes de tous les champs.

**⚠️ Important :** Utiliser les **mêmes champs** dans `equals()` et `hashCode()` !

---

#### Résultat

```java
// Maintenant avec equals() ET hashCode()
public class Person {
    @Override
    public boolean equals(Object obj) { ... } // ✅

    @Override
    public int hashCode() { ... } // ✅
}

// Test
Map<Person, String> phoneBook = new HashMap<>();

Person alice1 = new Person("Alice", 25);
phoneBook.put(alice1, "06-12-34-56-78");

Person alice2 = new Person("Alice", 25);
System.out.println(phoneBook.get(alice2)); // "06-12-34-56-78" ✅ Ça marche !
```

---

#### Pourquoi Ça Marche Maintenant ?

```
HashMap interne :
┌─────────────────────────────────────┐
│ Bucket 0:  []                       │
│ Bucket 1:  []                       │
│ Bucket 2:  [alice1 -> "06-12-..."] │  ← hashCode(alice1) = 1234 → Bucket 2
│ Bucket 3:  []                       │
│ ...                                 │
└─────────────────────────────────────┘

Recherche de alice2 :
1. Calcul hashCode(alice2) → 1234 (pareil que alice1 !)
2. Bucket = 1234 % taille = 2 ✅
3. Bucket 2 : compare avec equals() → alice1.equals(alice2) = true ✅
4. Retourne "06-12-34-56-78" ✅
```

---

### Démonstration Complète

```java
public class Person {
    private final String name;
    private final int age;

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return "Person{name='" + name + "', age=" + age + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Person person = (Person) obj;
        return age == person.age && Objects.equals(name, person.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, age);
    }
}
```

---

### Test Complet

```java
public static void main(String[] args) {
    Person alice = new Person("Alice", 25);

    // Test toString
    System.out.println(alice);
    // Person{name='Alice', age=25} ✅

    // Test equals
    Person alice2 = new Person("Alice", 25);
    System.out.println(alice.equals(alice2)); // true ✅

    // Test dans Map (hashCode)
    Map<Person, String> phoneBook = new HashMap<>();
    phoneBook.put(alice, "06-12-34-56-78");
    System.out.println(phoneBook.get(alice2)); // "06-12-34-56-78" ✅

    // Test dans Set
    Set<Person> people = new HashSet<>();
    people.add(alice);
    people.add(alice2); // Ignoré (doublon)
    System.out.println(people.size()); // 1 ✅
}
```

---

### 🔗 Lien avec FoodFast

**Dans le TP Question 4, vous DEVEZ implémenter equals/hashCode dans `Dish` :**

**Pourquoi ?**
- `Order` utilise `Map<Dish, Integer>` pour stocker les plats et quantités
- Sans `equals()` + `hashCode()`, impossible de retrouver un plat dans la Map !

**Ce qu'il faut implémenter :**
```java
public class Dish {
    @Override
    public String toString() { ... }  // Utile pour debug

    @Override
    public boolean equals(Object obj) {
        // Comparer: name, price, size
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, price, size);
    }
}
```

**Question :** Quels champs comparer dans `equals()` pour `Dish` ?
**Réponse :** name, price, size (tous les attributs qui définissent l'identité du plat)

---

### Résumé : Le Contrat Object

**Règles absolues :**

1. ✅ **TOUJOURS** implémenter `equals()` et `hashCode()` **ensemble**
   - Ne JAMAIS implémenter l'un sans l'autre

2. ✅ Utiliser les **mêmes champs** dans les deux méthodes

3. ✅ Utiliser les méthodes helper :
   - `Objects.equals()` pour `equals()`
   - `Objects.hash()` pour `hashCode()`

4. ✅ Implémenter `toString()` pour le débogage

---

**Sans equals/hashCode :**
- ❌ `Map` ne retrouve pas vos objets
- ❌ `Set` stocke des doublons
- ❌ `contains()` ne fonctionne pas

**Avec equals/hashCode :**
- ✅ `Map` fonctionne correctement
- ✅ `Set` garantit l'unicité
- ✅ `contains()` fonctionne

---

## Conclusion : Vous Êtes Prêts pour le TP !

<!--
NOTES PRÉSENTATEUR - Conclusion (3min) :
- Récapitulatif rapide des 2 parties
- Faire le lien : Partie 1 (outils) → Partie 2 (POO)
- Rappeler les Questions 4 et 5 du TP
- Annoncer Séance 3 : Streams + Exceptions
- Laisser du temps pour les questions
- Message final : "Vous avez tout ce qu'il faut pour réussir le TP !"
-->

---

### Ce qu'on a vu aujourd'hui

**Partie 1 : Les Outils**
✅ Collections : `List`, `Set`, `Map`, `Queue`
✅ `enum` : constantes typées et sûres
✅ Types riches : `BigDecimal`, `LocalDateTime`, `UUID`
✅ `Optional` : éviter `null`

**Partie 2 : POO**
✅ Encapsulation : `private` + getters/setters
✅ **Interfaces, Classes Abstraites et Polymorphisme** ⭐
✅ Contrat Object : `toString`, `equals`, `hashCode`

---

### Ce que vous pouvez faire maintenant

**→ TP Partie 2 : Questions 4 et 5**

**Question 4 :** Créer les classes métier avec :
- `enum DishSize`, `enum OrderStatus`
- `class Dish` avec `BigDecimal`, constructeur, getters, `equals`, `hashCode`, `toString`
- `class Customer` avec constructeur, getters, `equals`, `hashCode`, `toString`
- `class Order` avec `UUID`, `LocalDateTime`, `Map<Dish, Integer>`, `calculateTotalPrice()`

**Question 5 :** Créer `DeliveryPlatform` avec :
- `Map<String, Order>` pour stocker les commandes
- `placeOrder(Order order)`
- `Optional<Order> findOrderById(String orderId)`

---