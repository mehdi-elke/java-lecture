---
title: "Java - Séance 3"
description: "Logique Applicative et Robustesse"
author: Mehdi Elketroussi
theme: gaia
size: 16:9
paginate: true
marp: true
---

<!-- Notes présentateur:
Durée totale: 2h-2h20
Stratégie: Cette séance est dense mais essentielle. Elle introduit la programmation fonctionnelle et la gestion d'erreurs professionnelle.
Focus: Montrer la puissance des lambdas/streams avec le pattern ❌ → ✅. Insister sur les exceptions comme outil de robustesse, pas comme problème.
-->

# Séance 3 : Logique Applicative et Robustesse

**Objectif :** Manipuler des collections de manière moderne et rendre l'application robuste face aux erreurs.

**Plan de la séance :**

**Partie 1 : Programmation Fonctionnelle et Manipulation de Données** (70-80 min)
- Rappel : Interfaces et Polymorphisme
- Lambdas : Fonctions Anonymes
- Interfaces Fonctionnelles
- L'API Stream
- Method References

---

**Partie 2 : Gestion des Erreurs** (50-60 min)
- Try-Catch-Finally
- Exceptions Vérifiées vs Non-Vérifiées
- Créer ses Exceptions Personnalisées
- Best Practices

---

<!-- Notes présentateur:
Durée: 5min
Stratégie: Rappel rapide mais essentiel pour comprendre les interfaces fonctionnelles.
Focus: Les étudiants doivent comprendre qu'une interface = contrat, et que le polymorphisme permet de traiter différents objets de la même manière.
-->

# Partie 1 : Programmation Fonctionnelle

## Rappel : Interfaces et Polymorphisme

---

## Rappel : Qu'est-ce qu'une Interface ?

Une **interface** définit un **contrat** : un ensemble de méthodes qu'une classe s'engage à implémenter.

```java
public interface Drawable {
    void draw();
    double getArea();
}
```

Une classe qui implémente cette interface **doit** fournir le code de toutes ses méthodes.

```java
public class Circle implements Drawable {
    private double radius;

    @Override
    public void draw() {
        System.out.println("Dessin d'un cercle de rayon " + radius);
    }

    @Override
    public double getArea() {
        return Math.PI * radius * radius;
    }
}
```

---

## Le Polymorphisme en Action

Le polymorphisme permet de traiter différents objets via leur **interface commune**.

```java
public class Rectangle implements Drawable {
    private double width, height;

    @Override
    public void draw() {
        System.out.println("Dessin d'un rectangle " + width + "x" + height);
    }

    @Override
    public double getArea() {
        return width * height;
    }
}
```

**Utilisation polymorphique :**
```java
List<Drawable> shapes = List.of(
    new Circle(5.0),
    new Rectangle(10.0, 20.0)
);

for (Drawable shape : shapes) {
    shape.draw(); // Polymorphisme : appel de la bonne méthode
    System.out.println("Aire: " + shape.getArea());
}
```

---

## Pourquoi ce Rappel ?

Les **interfaces fonctionnelles** sont des interfaces spéciales avec **une seule méthode abstraite**.

Elles permettent d'utiliser des **lambdas** (fonctions anonymes) pour fournir une implémentation de cette méthode de manière très concise.

**Exemples d'interfaces fonctionnelles :**
- `Comparator<T>` → méthode `compare(T, T)`
- `Runnable` → méthode `run()`
- `Predicate<T>` → méthode `test(T)` *(que nous allons découvrir)*

Le polymorphisme s'applique aussi aux lambdas : une lambda peut être traitée comme n'importe quelle implémentation de l'interface.

---

<!-- Notes présentateur:
Durée: 15min
Stratégie: Montrer l'évolution historique (classe anonyme → lambda) pour que les étudiants comprennent le "pourquoi" des lambdas.
Focus: Pattern ❌ → ✅ systématique. Insister sur la concision et la lisibilité.
Piège à éviter: Ne pas surcharger avec trop de syntaxes différentes. Se concentrer sur (params) -> expression.
-->

## 1. Lambdas : Fonctions Anonymes

---

## Avant Java 8 : Le Problème

Imaginons que nous voulons trier une liste de `String` par ordre alphabétique.

**❌ Avant Java 8 (Classe Anonyme) :**
```java
List<String> names = new ArrayList<>(List.of("Charlie", "Alice", "Bob"));

Collections.sort(names, new Comparator<String>() {
    @Override
    public int compare(String s1, String s2) {
        return s1.compareTo(s2);
    }
});

System.out.println(names); // [Alice, Bob, Charlie]
```

**Problème :** Beaucoup de code verbeux pour une logique simple (comparer deux chaînes).

---

## Avec Java 8 : Les Lambdas

**✅ Avec une Lambda :**
```java
List<String> names = new ArrayList<>(List.of("Charlie", "Alice", "Bob"));

Collections.sort(names, (s1, s2) -> s1.compareTo(s2));

System.out.println(names); // [Alice, Bob, Charlie]
```
---
**Encore plus concis (avec method reference) :**
```java
Collections.sort(names, String::compareTo);
```
---
**Qu'est-ce qui a changé ?**
- Pas de déclaration de classe anonyme.
- Le compilateur **déduit** que nous implémentons `Comparator<String>`.
- La syntaxe `(params) -> expression` est une **expression lambda**.

---

## Syntaxe d'une Lambda

**Forme générale :**
```java
(paramètres) -> { corps de la méthode }
```
---
**Exemples :**

```java
// Aucun paramètre
() -> System.out.println("Hello")

// Un seul paramètre (les parenthèses sont optionnelles)
x -> x * x

// Plusieurs paramètres
(a, b) -> a + b

// Corps avec plusieurs instructions (nécessite des accolades)
(x, y) -> {
    int sum = x + y;
    return sum * 2;
}
```
---
**Règle :** Le compilateur déduit les types des paramètres à partir du contexte.

---

## Lambda = Implémentation d'Interface Fonctionnelle

Une lambda est toujours associée à une **interface fonctionnelle** : une interface avec **une seule méthode abstraite**.

**Exemple avec `Runnable` :**

**❌ Avant (Classe Anonyme) :**
```java
Runnable task = new Runnable() {
    @Override
    public void run() {
        System.out.println("Tâche exécutée");
    }
};
task.run();
```
---
**✅ Avec Lambda :**
```java
Runnable task = () -> System.out.println("Tâche exécutée");
task.run();
```

Le compilateur sait que `Runnable` a une méthode `run()`, donc la lambda implémente cette méthode.

---

## 🔗 Lien avec FoodFast

**Question pour vous :** Dans le TP FoodFast, vous devrez implémenter :
```java
List<Order> findOrdersByStatus(OrderStatus status)
```
---

**Réflexion :**
- Comment pourriez-vous utiliser une **lambda** pour filtrer les commandes ?
- Quelle serait la condition que chaque commande doit vérifier ?
- Indice : Vous aurez besoin d'une interface qui teste une condition (vrai/faux) sur chaque `Order`.

*(Nous verrons la réponse avec l'API Stream !)*

---

<!-- Notes présentateur:
Durée: 12min
Stratégie: Présenter les 4 interfaces principales (Predicate, Function, Consumer, Supplier) avec des exemples concrets.
Focus: Montrer que ces interfaces sont des "outils" pour manipuler des données. Insister sur Predicate et Function (les plus utilisées avec Streams).
Piège à éviter: Ne pas présenter toutes les variantes (BiPredicate, BiFunction, etc.). Se limiter aux 4 principales.
-->

## 2. Interfaces Fonctionnelles du JDK

Java fournit un ensemble d'interfaces fonctionnelles prêtes à l'emploi dans le package `java.util.function`.

---

## Les 4 Interfaces Fonctionnelles Essentielles

| Interface | Méthode | Description | Exemple d'usage |
|-----------|---------|-------------|-----------------|
| `Predicate<T>` | `boolean test(T t)` | Évalue une condition (vrai/faux) | Filtrer des éléments |
| `Function<T, R>` | `R apply(T t)` | Transforme un objet `T` en objet `R` | Transformer des données |
| `Consumer<T>` | `void accept(T t)` | Effectue une action sur un objet | Afficher, logger |
| `Supplier<T>` | `T get()` | Fournit un objet sans paramètre | Générer des valeurs |

**Note :** Il existe des variantes (`BiPredicate`, `BiFunction`, etc.) que nous verrons si nécessaire.

---

## `Predicate<T>` : Tester une Condition

**Définition :**
```java
@FunctionalInterface
public interface Predicate<T> {
    boolean test(T t);
}
```
---
**Exemple : Filtrer des nombres pairs**

**❌ Sans Lambda (Classe Anonyme) :**
```java
Predicate<Integer> isEven = new Predicate<Integer>() {
    @Override
    public boolean test(Integer n) {
        return n % 2 == 0;
    }
};

System.out.println(isEven.test(4)); // true
System.out.println(isEven.test(5)); // false
```

---

## `Predicate<T>` : Avec Lambda

**✅ Avec Lambda :**
```java
Predicate<Integer> isEven = n -> n % 2 == 0;

System.out.println(isEven.test(4)); // true
System.out.println(isEven.test(5)); // false
```
---
**Autre exemple : Chaînes non vides**
```java
Predicate<String> notEmpty = s -> !s.isEmpty();

System.out.println(notEmpty.test("Hello")); // true
System.out.println(notEmpty.test(""));      // false
```

**Usage typique :** Utilisé avec `stream.filter()` pour ne garder que les éléments qui satisfont une condition.

---

## `Function<T, R>` : Transformer des Données

**Définition :**
```java
@FunctionalInterface
public interface Function<T, R> {
    R apply(T t);
}
```
---
**Exemple : Transformer une chaîne en sa longueur**

**❌ Sans Lambda :**
```java
Function<String, Integer> length = new Function<String, Integer>() {
    @Override
    public Integer apply(String s) {
        return s.length();
    }
};

System.out.println(length.apply("Hello")); // 5
```

---

## `Function<T, R>` : Avec Lambda

**✅ Avec Lambda :**
```java
Function<String, Integer> length = s -> s.length();

System.out.println(length.apply("Hello")); // 5
```
---
**Autre exemple : Majuscules**
```java
Function<String, String> toUpper = s -> s.toUpperCase();

System.out.println(toUpper.apply("hello")); // HELLO
```

**Usage typique :** Utilisé avec `stream.map()` pour transformer chaque élément d'un stream.

---

## `Consumer<T>` : Effectuer une Action

**Définition :**
```java
@FunctionalInterface
public interface Consumer<T> {
    void accept(T t);
}
```
---
**Exemple : Afficher un élément**

**✅ Avec Lambda :**
```java
Consumer<String> print = s -> System.out.println(s);

print.accept("Hello"); // Affiche "Hello"
```
---
**Autre exemple : Logger**
```java
Consumer<String> logger = s -> System.err.println("[LOG] " + s);

logger.accept("Démarrage de l'application");
```

**Usage typique :** Utilisé avec `stream.forEach()` pour effectuer une action sur chaque élément.

---

## `Supplier<T>` : Fournir une Valeur

**Définition :**
```java
@FunctionalInterface
public interface Supplier<T> {
    T get();
}
```
---
**Exemple : Générer un nombre aléatoire**

**✅ Avec Lambda :**
```java
Supplier<Double> randomValue = () -> Math.random();

System.out.println(randomValue.get()); // 0.123456789...
System.out.println(randomValue.get()); // 0.987654321...
```
---
**Autre exemple : Fournir une valeur par défaut**
```java
Supplier<String> defaultName = () -> "Anonyme";

System.out.println(defaultName.get()); // Anonyme
```

**Usage typique :** Utilisé avec `Optional.orElseGet()` pour fournir une valeur par défaut de manière *lazy* (calculée uniquement si nécessaire).

---

## 🔗 Lien avec FoodFast

**Question pour vous :**

Dans le TP, vous devrez implémenter :
```java
List<Order> findOrdersByCustomer(Customer customer)
```
---
**Réflexion :**
- Quelle **interface fonctionnelle** pourriez-vous utiliser pour tester si une commande appartient à un client ?
- Quelle serait la condition à vérifier ?
- Comment écrire cette condition sous forme de lambda ?

**Indice :** Une commande appartient à un client si `order.getCustomer().equals(customer)`.

---

<!-- Notes présentateur:
Durée: 35min (section la plus longue)
Stratégie: Progression graduelle. Commencer par créer un Stream, puis filter, map, collect. Ensuite les opérations avancées.
Focus: Montrer le pipeline (source → opérations intermédiaires → opération terminale). Insister sur le fait que les opérations intermédiaires sont *lazy*.
Piège à éviter: Ne pas oublier l'opération terminale (sinon rien ne se passe). Montrer l'erreur explicitement.
-->

## 3. L'API Stream : Manipuler des Collections de Manière Déclarative

---

## Le Problème : Code Impératif vs Déclaratif

**Scénario :** À partir d'une liste de nombres, extraire les nombres pairs et les doubler.

**❌ Approche Impérative (Avant Java 8) :**
```java
List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6);
List<Integer> result = new ArrayList<>();

for (Integer n : numbers) {
    if (n % 2 == 0) { // Garder les pairs
        result.add(n * 2); // Doubler
    }
}

System.out.println(result); // [4, 8, 12]
```
---
**Problème :** On se concentre sur **comment faire** (boucle, condition, ajout), pas sur **quoi faire**.

---

## La Solution : API Stream

**✅ Approche Déclarative (Avec Streams) :**
```java
List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6);

List<Integer> result = numbers.stream()       // Source
    .filter(n -> n % 2 == 0)                  // Op. intermédiaire : garder pairs
    .map(n -> n * 2)                          // Op. intermédiaire : doubler
    .collect(Collectors.toList());            // Op. terminale : collecter

System.out.println(result); // [4, 8, 12]
```
---
**Avantages :**
- **Lisible :** On décrit **quoi faire** (filtrer, transformer, collecter).
- **Concis :** Moins de code verbeux.
- **Composable :** Facile d'ajouter des opérations.

---

## Qu'est-ce qu'un Stream ?

Un **Stream** n'est **pas** une structure de données. C'est un **flux de données** sur lequel on applique des opérations.

**Anatomie d'un Pipeline Stream :**

```
Source → Opérations Intermédiaires → Opération Terminale
```
---
1. **Source** : Une collection (`List`, `Set`), un tableau, ou un générateur.
2. **Opérations Intermédiaires** : `filter()`, `map()`, `sorted()`, etc. Elles sont **lazy** (exécutées uniquement si une opération terminale est présente).
3. **Opération Terminale** : `collect()`, `forEach()`, `count()`, etc. Déclenche le traitement.

**Exemple simple :**
```java
List<String> names = List.of("Alice", "Bob", "Charlie");

names.stream()                        // Source
    .filter(s -> s.length() > 3)      // Op. intermédiaire
    .forEach(System.out::println);    // Op. terminale
// Affiche: Alice, Charlie
```

---

## Créer un Stream

**À partir d'une Collection :**
```java
List<String> list = List.of("A", "B", "C");
Stream<String> stream1 = list.stream();
```
---
**À partir d'un tableau :**
```java
String[] array = {"A", "B", "C"};
Stream<String> stream2 = Arrays.stream(array);
```
---
**Stream vide :**
```java
Stream<String> emptyStream = Stream.empty();
```
---
**Stream avec des éléments :**
```java
Stream<String> stream3 = Stream.of("A", "B", "C");
```

---

## `filter()` : Filtrer les Éléments

**Signature :**
```java
Stream<T> filter(Predicate<T> predicate)
```

**Usage :** Garde uniquement les éléments pour lesquels le `Predicate` retourne `true`.

---

**Exemple : Garder les chaînes non vides**
```java
List<String> words = List.of("Alice", "", "Bob", "Charlie", "");

List<String> nonEmpty = words.stream()
    .filter(s -> !s.isEmpty())
    .collect(Collectors.toList());

System.out.println(nonEmpty); // [Alice, Bob, Charlie]
```
---

**Exemple : Garder les nombres > 10**
```java
List<Integer> numbers = List.of(5, 12, 8, 20, 3);

List<Integer> largeNumbers = numbers.stream()
    .filter(n -> n > 10)
    .collect(Collectors.toList());

System.out.println(largeNumbers); // [12, 20]
```

---

## `map()` : Transformer les Éléments

**Signature :**
```java
<R> Stream<R> map(Function<T, R> mapper)
```

**Usage :** Transforme chaque élément du stream en appliquant la fonction.

---

**Exemple : Transformer des chaînes en leur longueur**
```java
List<String> words = List.of("Alice", "Bob", "Charlie");

List<Integer> lengths = words.stream()
    .map(s -> s.length())
    .collect(Collectors.toList());

System.out.println(lengths); // [5, 3, 7]
```
---

**Exemple : Mettre en majuscules**
```java
List<String> upper = words.stream()
    .map(s -> s.toUpperCase())
    .collect(Collectors.toList());

System.out.println(upper); // [ALICE, BOB, CHARLIE]
```

---

## Combiner `filter()` et `map()`

**Exemple : Garder les chaînes non vides et calculer leur longueur**

```java
List<String> words = List.of("Alice", "", "Bob", "Charlie", "");

List<Integer> lengths = words.stream()
    .filter(s -> !s.isEmpty())       // Garde les non vides
    .map(s -> s.length())             // Transforme en longueur
    .collect(Collectors.toList());

System.out.println(lengths); // [5, 3, 7]
```
---

**Ordre important :** Filtrer avant de mapper pour ne pas transformer des éléments qu'on va jeter.

---

## `collect()` : L'Opération Terminale Essentielle

**Signature :**
```java
<R, A> R collect(Collector<T, A, R> collector)
```

**Usage :** Transforme le stream en une structure de données (List, Set, Map, etc.).

La classe `Collectors` fournit des méthodes factory très utiles.

---

## `Collectors.toList()` et `Collectors.toSet()`

**Collecter dans une List :**
```java
List<String> list = stream.collect(Collectors.toList());
```
---
**Collecter dans un Set (supprime les doublons) :**
```java
List<Integer> numbers = List.of(1, 2, 2, 3, 3, 3);

Set<Integer> uniqueNumbers = numbers.stream()
    .collect(Collectors.toSet());

System.out.println(uniqueNumbers); // [1, 2, 3]
```

---

## `Collectors.joining()` : Joindre des Chaînes

**Joindre avec un séparateur :**
```java
List<String> words = List.of("Alice", "Bob", "Charlie");

String joined = words.stream()
    .collect(Collectors.joining(", "));

System.out.println(joined); // Alice, Bob, Charlie
```
---
**Avec préfixe et suffixe :**
```java
String withBrackets = words.stream()
    .collect(Collectors.joining(", ", "[", "]"));

System.out.println(withBrackets); // [Alice, Bob, Charlie]
```

---

## `Collectors.toMap()` : Créer une Map

**Signature :**
```java
Collector<T, ?, Map<K, U>> toMap(
    Function<T, K> keyMapper,
    Function<T, U> valueMapper
)
```
---
**Exemple : Créer une Map nom → longueur**
```java
List<String> words = List.of("Alice", "Bob", "Charlie");

Map<String, Integer> nameToLength = words.stream()
    .collect(Collectors.toMap(
        s -> s,              // Clé : le nom lui-même
        s -> s.length()      // Valeur : la longueur
    ));

System.out.println(nameToLength);
// {Alice=5, Bob=3, Charlie=7}
```
---
**Autre exemple : Map nom → majuscules**
```java
Map<String, String> nameToUpper = words.stream()
    .collect(Collectors.toMap(
        s -> s,
        s -> s.toUpperCase()
    ));

System.out.println(nameToUpper);
// {Alice=ALICE, Bob=BOB, Charlie=CHARLIE}
```

---

## `sorted()` : Trier le Stream

**Tri naturel :**
```java
List<Integer> numbers = List.of(5, 2, 8, 1, 9);

List<Integer> sorted = numbers.stream()
    .sorted()
    .collect(Collectors.toList());

System.out.println(sorted); // [1, 2, 5, 8, 9]
```
---
**Tri avec un Comparator :**
```java
List<String> words = List.of("Alice", "Bob", "Charlie");

List<String> sortedByLength = words.stream()
    .sorted((s1, s2) -> Integer.compare(s1.length(), s2.length()))
    .collect(Collectors.toList());

System.out.println(sortedByLength); // [Bob, Alice, Charlie]
```
---
**Tri inversé :**
```java
List<Integer> reversed = numbers.stream()
    .sorted(Comparator.reverseOrder())
    .collect(Collectors.toList());

System.out.println(reversed); // [9, 8, 5, 2, 1]
```

---

## `distinct()` : Supprimer les Doublons

```java
List<Integer> numbers = List.of(1, 2, 2, 3, 3, 3, 4);

List<Integer> unique = numbers.stream()
    .distinct()
    .collect(Collectors.toList());

System.out.println(unique); // [1, 2, 3, 4]
```

**Note :** Utilise `equals()` pour comparer les éléments.

---

## `limit()` et `skip()` : Contrôler le Nombre d'Éléments

**`limit(n)` : Garde les `n` premiers éléments**
```java
List<Integer> numbers = List.of(1, 2, 3, 4, 5);

List<Integer> first3 = numbers.stream()
    .limit(3)
    .collect(Collectors.toList());

System.out.println(first3); // [1, 2, 3]
```
---
**`skip(n)` : Saute les `n` premiers éléments**
```java
List<Integer> afterFirst2 = numbers.stream()
    .skip(2)
    .collect(Collectors.toList());

System.out.println(afterFirst2); // [3, 4, 5]
```
---
**Combiner les deux :**
```java
List<Integer> middle = numbers.stream()
    .skip(1)   // Saute le 1er
    .limit(3)  // Garde 3 éléments
    .collect(Collectors.toList());

System.out.println(middle); // [2, 3, 4]
```

---

## `forEach()` : Effectuer une Action

**Signature :**
```java
void forEach(Consumer<T> action)
```
---
**Exemple : Afficher chaque élément**
```java
List<String> words = List.of("Alice", "Bob", "Charlie");

words.stream()
    .forEach(s -> System.out.println(s));
// Affiche:
// Alice
// Bob
// Charlie
```
---
**Avec method reference :**
```java
words.stream().forEach(System.out::println);
```

**Note :** `forEach` est une **opération terminale**. Après son exécution, le stream est consommé.

---

## `count()` : Compter les Éléments

```java
List<String> words = List.of("Alice", "", "Bob", "Charlie", "");

long count = words.stream()
    .filter(s -> !s.isEmpty())
    .count();

System.out.println(count); // 3
```
---
**Note :** `count()` est une opération terminale qui retourne un `long`.

---

## `anyMatch()`, `allMatch()`, `noneMatch()`

**`anyMatch(Predicate)` : Au moins un élément satisfait la condition**
```java
List<Integer> numbers = List.of(1, 2, 3, 4, 5);

boolean hasEven = numbers.stream()
    .anyMatch(n -> n % 2 == 0);

System.out.println(hasEven); // true
```
---
**`allMatch(Predicate)` : Tous les éléments satisfont la condition**
```java
boolean allPositive = numbers.stream()
    .allMatch(n -> n > 0);

System.out.println(allPositive); // true
```
---
**`noneMatch(Predicate)` : Aucun élément ne satisfait la condition**
```java
boolean noNegative = numbers.stream()
    .noneMatch(n -> n < 0);

System.out.println(noNegative); // true
```

---

## `findFirst()` et `findAny()`

**`findFirst()` : Retourne le premier élément (dans un `Optional`)**
```java
List<String> words = List.of("Alice", "Bob", "Charlie");

Optional<String> first = words.stream()
    .filter(s -> s.startsWith("B"))
    .findFirst();

first.ifPresent(System.out::println); // Bob
```
---
**`findAny()` : Retourne n'importe quel élément (utile avec les streams parallèles)**
```java
Optional<String> any = words.stream()
    .filter(s -> s.length() > 3)
    .findAny();

any.ifPresent(System.out::println); // Alice (ou Charlie)
```

**Note :** Retournent un `Optional` car le stream peut être vide.

---

## Piège : Oublier l'Opération Terminale

**❌ Erreur Courante :**
```java
List<String> words = List.of("Alice", "Bob", "Charlie");

words.stream()
    .filter(s -> s.length() > 3)
    .map(s -> s.toUpperCase());
// Rien ne se passe ! Aucune opération terminale.
```
---
**✅ Solution :**
```java
words.stream()
    .filter(s -> s.length() > 3)
    .map(s -> s.toUpperCase())
    .forEach(System.out::println); // Op. terminale
// Affiche: ALICE, CHARLIE
```

**Rappel :** Les opérations intermédiaires sont **lazy**. Sans opération terminale, elles ne sont jamais exécutées.

---

## Exemple Complet : Pipeline Stream

**Scénario :** À partir d'une liste de phrases, extraire les mots uniques de plus de 3 lettres, les mettre en majuscules, et les trier.

```java
List<String> sentences = List.of(
    "Java est un langage",
    "Les streams sont puissants",
    "Java et Python"
);

List<String> result = sentences.stream()
    .flatMap(sentence -> Arrays.stream(sentence.split(" "))) // Séparer en mots
    .filter(word -> word.length() > 3)                       // Mots > 3 lettres
    .map(String::toUpperCase)                                // Majuscules
    .distinct()                                              // Supprimer doublons
    .sorted()                                                // Trier
    .collect(Collectors.toList());

System.out.println(result);
// [JAVA, LANGAGE, PUISSANTS, PYTHON, SONT, STREAMS]
```
---

**Note :** `flatMap()` transforme chaque phrase en stream de mots, puis aplatit tous les streams en un seul.

---

## 🔗 Lien avec FoodFast

**Question 6 du TP :**

Vous devez implémenter :
```java
List<Order> findOrdersByStatus(OrderStatus status)
```
---

**Réflexion :**
- Quelle méthode de Stream utiliseriez-vous pour **filtrer** les commandes ?
- Quelle serait la condition (Predicate) à appliquer ?
- Comment **collecter** les résultats dans une `List<Order>` ?

**Indice :** Vous avez maintenant tous les outils nécessaires pour résoudre cette question !

---

**Structure attendue :**
```java
return orders.values().stream()
    .filter(/* condition sur le status */)
    .collect(/* collecter dans une liste */);
```

---

<!-- Notes présentateur:
Durée: 7min
Stratégie: Montrer que les method references sont un raccourci pour les lambdas qui appellent une méthode existante.
Focus: Présenter les 4 types principaux (static, instance, instance sur objet particulier, constructeur).
Piège à éviter: Ne pas surcharger. Se limiter aux cas les plus courants.
-->

## 4. Method References : Un Raccourci pour les Lambdas

---

## Le Problème : Lambdas Répétitives

Parfois, une lambda ne fait qu'appeler une méthode existante.

**Exemple :**
```java
List<String> words = List.of("Alice", "Bob", "Charlie");

words.stream()
    .map(s -> s.toUpperCase())
    .forEach(s -> System.out.println(s));
```
---

**Observation :**
- `s -> s.toUpperCase()` appelle juste `toUpperCase()` sur `s`.
- `s -> System.out.println(s)` appelle juste `println()` avec `s`.

**Java fournit un raccourci : les method references.**

---

## Method References : La Solution

**✅ Avec Method References :**
```java
words.stream()
    .map(String::toUpperCase)
    .forEach(System.out::println);
```
---

**Syntaxe :** `Classe::méthode` ou `objet::méthode`

**Avantages :**
- Plus concis.
- Intention plus claire (on voit directement quelle méthode est appelée).

---

## Les 4 Types de Method References

**1. Méthode statique : `Classe::méthodeStatique`**
```java
List<String> numbers = List.of("1", "2", "3");

List<Integer> ints = numbers.stream()
    .map(Integer::parseInt) // Équivalent à s -> Integer.parseInt(s)
    .collect(Collectors.toList());

System.out.println(ints); // [1, 2, 3]
```

---

## Les 4 Types de Method References (suite)

**2. Méthode d'instance sur un objet arbitraire : `Classe::méthodeInstance`**
```java
List<String> words = List.of("Alice", "Bob", "Charlie");

words.stream()
    .map(String::toUpperCase) // Équivalent à s -> s.toUpperCase()
    .forEach(System.out::println);
// ALICE, BOB, CHARLIE
```
---
**3. Méthode d'instance sur un objet particulier : `objet::méthodeInstance`**
```java
List<String> words = List.of("Alice", "Bob", "Charlie");

words.forEach(System.out::println); // Équivalent à s -> System.out.println(s)
```
---
**4. Constructeur : `Classe::new`**
```java
List<String> words = List.of("Alice", "Bob", "Charlie");

List<StringBuilder> builders = words.stream()
    .map(StringBuilder::new) // Équivalent à s -> new StringBuilder(s)
    .collect(Collectors.toList());
```

---

## Quand Utiliser les Method References ?

**✅ Utilisez une method reference quand :**
- La lambda ne fait qu'appeler une méthode existante.
- Le code devient plus lisible.

**❌ Restez avec une lambda quand :**
- Vous avez une logique supplémentaire.
- La method reference rend le code moins clair.
---
**Exemple où la lambda est préférable :**
```java
// Lambda claire
words.stream().filter(s -> s.length() > 3)

// Method reference moins claire (nécessiterait une méthode dédiée)
// Pas de gain de lisibilité
```

---

## 🔗 Lien avec FoodFast

**Réflexion :**

Dans votre méthode `findOrdersByCustomer()`, si vous avez une lambda comme :
```java
.forEach(order -> System.out.println(order))
```
---
**Question :** Comment pourriez-vous la réécrire avec une method reference ?

**Réponse :** `System.out::println`

---

<!-- Notes présentateur:
Durée: 5min (BONUS, pour les étudiants avancés)
Stratégie: Montrer rapidement les collectors avancés sans trop de détails. Dire que c'est pour aller plus loin.
Focus: groupingBy et partitioningBy sont les plus utiles.
Piège à éviter: Ne pas rentrer dans les détails. Juste montrer l'existence et un exemple simple.
-->

## Bonus : Collectors Avancés

---

## `Collectors.groupingBy()` : Grouper par Clé

**Exemple : Grouper des mots par leur longueur**
```java
List<String> words = List.of("Alice", "Bob", "Charlie", "Dan");

Map<Integer, List<String>> byLength = words.stream()
    .collect(Collectors.groupingBy(s -> s.length()));

System.out.println(byLength);
// {3=[Bob, Dan], 5=[Alice], 7=[Charlie]}
```
---
**Usage :** Créer des groupes basés sur une propriété commune.

---

## `Collectors.partitioningBy()` : Partitionner en Deux Groupes

**Exemple : Séparer les mots courts et longs**
```java
List<String> words = List.of("Alice", "Bob", "Charlie", "Dan");

Map<Boolean, List<String>> partitioned = words.stream()
    .collect(Collectors.partitioningBy(s -> s.length() > 3));

System.out.println(partitioned);
// {false=[Bob, Dan], true=[Alice, Charlie]}
```
---
**Usage :** Séparer en deux groupes selon une condition (vrai/faux).

---

## `Collectors.counting()` : Compter les Éléments

**Exemple : Compter les mots par longueur**
```java
Map<Integer, Long> countByLength = words.stream()
    .collect(Collectors.groupingBy(
        s -> s.length(),
        Collectors.counting()
    ));

System.out.println(countByLength);
// {3=2, 5=1, 7=1}
```

---

## Pourquoi ces Collectors sont Puissants ?

**Réponse :** Ils permettent de transformer des structures de données complexes en une seule ligne de code, de manière lisible et déclarative.

**Dans le monde réel :**
- `groupingBy()` : Grouper des transactions par catégorie, des logs par niveau de sévérité, etc.
- `partitioningBy()` : Séparer des utilisateurs actifs/inactifs, des commandes livrées/non livrées, etc.
- `counting()` : Statistiques sur des données.

---

<!-- Notes présentateur:
Durée: 3-5min
Stratégie: Avertissement rapide sur les parallel streams. Ne pas entrer dans les détails de la concurrence (Séance 4).
Focus: Dire que c'est puissant mais dangereux si mal utilisé.
Piège à éviter: Ne pas promettre des gains de performance miraculeux. Dire que ça dépend du contexte.
-->

## Avertissement : Parallel Streams

---

## Parallel Streams : Puissance et Danger

Java permet de paralléliser un stream en appelant `.parallel()` :

```java
List<Integer> numbers = List.of(1, 2, 3, 4, 5, 6, 7, 8);

int sum = numbers.parallelStream()
    .filter(n -> n % 2 == 0)
    .mapToInt(n -> n * 2)
    .sum();

System.out.println(sum); // 40
```
---
**Avantages :**
- Peut accélérer le traitement sur de grandes collections avec des CPU multi-cœurs.

**Dangers :**
- **Thread-safety :** Les opérations doivent être **sans effets de bord** et **thread-safe**.
- **Overhead :** Pour des petites collections, le coût de parallélisation peut être supérieur au gain.

**Règle :** N'utilisez pas `parallel()` par défaut. Mesurez d'abord les performances.

---

<!-- Notes présentateur:
Durée: 50-60min (Partie 2 complète)
Stratégie: Montrer que les exceptions ne sont pas un "problème" mais un outil pour rendre le code robuste.
Focus: Try-catch, checked vs unchecked, exceptions personnalisées, best practices.
Piège à éviter: Ne pas donner l'impression que les exceptions sont "mauvaises". Elles sont essentielles.
-->

# Partie 2 : Gestion des Erreurs et Robustesse

---

<!-- Notes présentateur:
Durée: 5min
Stratégie: Introduction rapide pour expliquer pourquoi les exceptions existent.
Focus: Les erreurs font partie de la vie d'une application. Il faut les gérer de manière professionnelle.
-->

## Introduction : Pourquoi Gérer les Erreurs ?

**Scénario :** Vous développez une application de livraison de repas.

**Problèmes potentiels :**
- Un fichier de configuration est introuvable.
- Une connexion réseau échoue.
- Un utilisateur saisit une valeur invalide.
- Une base de données est indisponible.
---
**Sans gestion d'erreurs :** L'application plante (`NullPointerException`, `IOException`, etc.).

**Avec gestion d'erreurs :** L'application détecte le problème, informe l'utilisateur, et continue de fonctionner ou se ferme proprement.

**Objectif :** Rendre l'application **robuste** face aux erreurs.

---

<!-- Notes présentateur:
Durée: 15min
Stratégie: Montrer try-catch-finally avec des exemples concrets. Insister sur le fait que finally est TOUJOURS exécuté.
Focus: Pattern ❌ → ✅ pour montrer le problème sans try-catch.
-->

## 1. Try-Catch-Finally : Le Mécanisme de Base

---

## Le Problème : Code Sans Gestion d'Erreur

**❌ Code Fragile :**
```java
public int divide(int a, int b) {
    return a / b; // Que se passe-t-il si b == 0 ?
}

public static void main(String[] args) {
    int result = divide(10, 0);
    System.out.println(result);
}
```
---
**Résultat :**
```
Exception in thread "main" java.lang.ArithmeticException: / by zero
```

**Problème :** L'application plante. Aucune récupération possible.

---

## La Solution : Try-Catch

**✅ Code Robuste :**
```java
public int divide(int a, int b) {
    try {
        return a / b;
    } catch (ArithmeticException e) {
        System.err.println("Erreur : Division par zéro.");
        return 0; // Valeur par défaut
    }
}

public static void main(String[] args) {
    int result = divide(10, 0);
    System.out.println("Résultat : " + result);
}
```
---
**Résultat :**
```
Erreur : Division par zéro.
Résultat : 0
```

**Avantage :** L'application ne plante pas. L'erreur est gérée.

---

## Anatomie du Try-Catch

```java
try {
    // Code susceptible de lever une exception
} catch (TypeException e) {
    // Traitement de l'exception
}
```
---
**Fonctionnement :**
1. Le code dans le bloc `try` est exécuté.
2. Si une exception est levée, le flux d'exécution saute au bloc `catch` correspondant.
3. Si aucune exception n'est levée, le bloc `catch` est ignoré.

**Note :** Vous pouvez avoir plusieurs blocs `catch` pour différents types d'exceptions.

---

## Plusieurs Blocs Catch

**Exemple :**
```java
try {
    String text = null;
    System.out.println(text.length()); // NullPointerException
    int result = 10 / 0;                // ArithmeticException
} catch (NullPointerException e) {
    System.err.println("Erreur : Référence nulle.");
} catch (ArithmeticException e) {
    System.err.println("Erreur : Division par zéro.");
}
```
---
**Ordre important :** Les exceptions plus **spécifiques** doivent être attrapées avant les plus **générales**.

**❌ Erreur de Compilation :**
```java
try {
    // ...
} catch (Exception e) {         // Trop général
    // ...
} catch (ArithmeticException e) { // Jamais atteint (erreur de compilation)
    // ...
}
```

---

## Le Bloc Finally : Toujours Exécuté

Le bloc `finally` contient du code qui sera **toujours** exécuté, qu'une exception soit levée ou non.

**Usage typique :** Libérer des ressources (fermer un fichier, une connexion, etc.).

---
```java
FileReader reader = null;
try {
    reader = new FileReader("config.txt");
    // Lire le fichier...
} catch (FileNotFoundException e) {
    System.err.println("Fichier introuvable.");
} finally {
    if (reader != null) {
        try {
            reader.close(); // Toujours fermer le fichier
        } catch (IOException e) {
            System.err.println("Erreur lors de la fermeture.");
        }
    }
}
```

**Note :** Ce code est verbeux. Nous verrons une solution plus élégante avec `try-with-resources`.

---

## Try-With-Resources : La Solution Élégante

**✅ Avec Try-With-Resources :**
```java
try (FileReader reader = new FileReader("config.txt")) {
    // Lire le fichier...
} catch (FileNotFoundException e) {
    System.err.println("Fichier introuvable.");
} catch (IOException e) {
    System.err.println("Erreur de lecture.");
}
// Le fichier est automatiquement fermé, même en cas d'exception
```
---
**Avantages :**
- Plus concis.
- Fermeture automatique des ressources.
- Fonctionne avec toute classe qui implémente `AutoCloseable` ou `Closeable`.

**Exemples de ressources :** `FileReader`, `BufferedReader`, `Connection` (JDBC), `PreparedStatement`, etc.

---

## 🔗 Lien avec FoodFast

**Question 7 du TP :**

Vous devez créer une méthode `prepare(Order order)` qui peut lancer une `OrderPreparationException`.

**Réflexion :**
- Où placeriez-vous le bloc `try-catch` pour gérer cette exception ?
- Que feriez-vous en cas d'erreur (passer la commande en statut `CANCELLED` ?) ?
- Comment afficher un message d'erreur approprié ?
---
**Structure attendue :**
```java
try {
    restaurant.prepare(order);
} catch (OrderPreparationException e) {
    // Gérer l'erreur
}
```

---

<!-- Notes présentateur:
Durée: 10min
Stratégie: Bien expliquer la différence entre checked et unchecked. Montrer des exemples concrets.
Focus: Checked = erreurs prévisibles (IO, réseau). Unchecked = erreurs de programmation (NPE, IAE).
-->

## 2. Exceptions Vérifiées vs Non-Vérifiées

---

## Hiérarchie des Exceptions en Java

```
Throwable
├── Error (erreurs système, ne pas attraper)
│   └── OutOfMemoryError, StackOverflowError, etc.
└── Exception
    ├── RuntimeException (unchecked)
    │   ├── NullPointerException
    │   ├── IllegalArgumentException
    │   ├── ArithmeticException
    │   └── ...
    └── IOException, SQLException, etc. (checked)
```
---
**Deux catégories d'exceptions :**
1. **Checked Exceptions** : Héritent de `Exception` (mais pas de `RuntimeException`).
2. **Unchecked Exceptions** : Héritent de `RuntimeException`.

---

## Checked Exceptions : Gestion Obligatoire

**Définition :** Exceptions que le compilateur **oblige** à gérer (avec `try-catch` ou en les propageant avec `throws`).

**Exemples :** `IOException`, `SQLException`, `FileNotFoundException`, etc.

**Raison :** Représentent des erreurs **prévisibles** et **récupérables** (fichier manquant, réseau indisponible, etc.).

---

**Exemple :**
```java
public String readFile(String path) throws IOException {
    return Files.readString(Paths.get(path));
}
```

**Le compilateur force la gestion :**
```java
// ❌ Erreur de compilation si on ne gère pas l'IOException
public void main() {
    String content = readFile("config.txt"); // Erreur !
}
```

---

## Checked Exceptions : Gestion ou Propagation

**Option 1 : Gérer avec try-catch**
```java
public void main() {
    try {
        String content = readFile("config.txt");
        System.out.println(content);
    } catch (IOException e) {
        System.err.println("Erreur de lecture : " + e.getMessage());
    }
}
```
---
**Option 2 : Propager avec throws**
```java
public void main() throws IOException {
    String content = readFile("config.txt");
    System.out.println(content);
}
```

**Note :** Propager l'exception = la responsabilité de gestion est déléguée à l'appelant.

---

## Unchecked Exceptions : Gestion Optionnelle

**Définition :** Exceptions que le compilateur ne **force pas** à gérer.

**Exemples :** `NullPointerException`, `IllegalArgumentException`, `ArithmeticException`, etc.

**Raison :** Représentent des erreurs de **programmation** (bugs). Elles doivent être **corrigées** dans le code, pas gérées.

---
**Exemple :**
```java
public int divide(int a, int b) {
    return a / b; // Peut lever ArithmeticException si b == 0
}
```
---
**Pas d'obligation de gestion :**
```java
public void main() {
    int result = divide(10, 0); // Pas d'erreur de compilation
    // Mais exception au runtime si b == 0
}
```

---

## Checked vs Unchecked : Quand Utiliser Quoi ?

**Utilisez une Checked Exception quand :**
- L'erreur est **prévisible** et **récupérable**.
- L'appelant peut faire quelque chose de sensé pour gérer l'erreur.
- Exemples : fichier manquant, réseau indisponible, format de données invalide.

---
**Utilisez une Unchecked Exception quand :**
- L'erreur est due à un **bug** (erreur de programmation).
- L'appelant ne peut rien faire pour gérer l'erreur (il doit corriger le code).
- Exemples : `null` passé où il ne devrait pas l'être, index hors limites, division par zéro.

**Règle générale :** Les Checked Exceptions forcent l'appelant à penser à l'erreur. Les Unchecked Exceptions signalent un bug à corriger.

---

## `throw` vs `throws` : Quelle Différence ?

**`throw` : Lancer une Exception**
```java
public void setAge(int age) {
    if (age < 0) {
        throw new IllegalArgumentException("L'âge ne peut être négatif.");
    }
    this.age = age;
}
```
---
**`throws` : Déclarer qu'une Méthode Peut Lancer une Exception**
```java
public String readFile(String path) throws IOException {
    return Files.readString(Paths.get(path));
}
```

**Résumé :**
- **`throw`** : Mot-clé pour **lancer** une instance d'exception.
- **`throws`** : Mot-clé dans la signature d'une méthode pour **déclarer** qu'elle peut lancer une exception (obligatoire pour les Checked Exceptions).

---

<!-- Notes présentateur:
Durée: 10min
Stratégie: Montrer comment créer une exception personnalisée et pourquoi c'est utile.
Focus: Les exceptions personnalisées permettent de représenter des erreurs métier spécifiques.
-->

## 3. Créer ses Exceptions Personnalisées

---

## Pourquoi des Exceptions Personnalisées ?

Les exceptions du JDK (`IOException`, `IllegalArgumentException`, etc.) sont **génériques**.

**Problème :** Elles ne représentent pas toujours précisément l'erreur métier.

---

**Solution :** Créer des exceptions **personnalisées** pour votre domaine.

---

**Avantages :**
- **Clarté :** L'exception a un nom explicite (ex: `InsufficientBalanceException`).
- **Traçabilité :** Facile de distinguer les erreurs métier des erreurs techniques.
- **Robustesse :** L'appelant peut gérer spécifiquement cette erreur.

---

## Créer une Exception Vérifiée (Checked)

**Étape 1 : Hériter de `Exception`**

```java
public class InsufficientBalanceException extends Exception {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
```
---
**Étape 2 : Utiliser l'Exception**

```java
public class BankAccount {
    private double balance;

    public void withdraw(double amount) throws InsufficientBalanceException {
        if (amount > balance) {
            throw new InsufficientBalanceException(
                "Solde insuffisant. Disponible : " + balance
            );
        }
        balance -= amount;
    }
}
```

---

## Créer une Exception Vérifiée : Utilisation

**Étape 3 : Gérer l'Exception**

```java
public static void main(String[] args) {
    BankAccount account = new BankAccount(100.0);

    try {
        account.withdraw(150.0);
    } catch (InsufficientBalanceException e) {
        System.err.println("Erreur : " + e.getMessage());
    }
}
```
---
**Résultat :**
```
Erreur : Solde insuffisant. Disponible : 100.0
```

**Avantage :** Le code appelant est **obligé** de gérer cette erreur (c'est une Checked Exception).

---

## Créer une Exception Non-Vérifiée (Unchecked)

**Étape 1 : Hériter de `RuntimeException`**

```java
public class InvalidAgeException extends RuntimeException {
    public InvalidAgeException(String message) {
        super(message);
    }
}
```
---
**Étape 2 : Utiliser l'Exception**

```java
public class Person {
    private int age;

    public void setAge(int age) {
        if (age < 0 || age > 150) {
            throw new InvalidAgeException("Âge invalide : " + age);
        }
        this.age = age;
    }
}
```

**Note :** Pas besoin de `throws` dans la signature (Unchecked Exception).

---

## Créer une Exception Non-Vérifiée : Utilisation

**Étape 3 : Utiliser la Méthode**

```java
public static void main(String[] args) {
    Person person = new Person();

    try {
        person.setAge(-5);
    } catch (InvalidAgeException e) {
        System.err.println("Erreur : " + e.getMessage());
    }
}
```
---
**Résultat :**
```
Erreur : Âge invalide : -5
```

**Note :** La gestion est **optionnelle** (Unchecked Exception). Ici, on choisit de la gérer pour afficher un message propre.

---

## Checked ou Unchecked : Comment Choisir ?

**Votre exception personnalisée doit être Checked si :**
- L'erreur est **récupérable** et l'appelant peut faire quelque chose de sensé.
- Exemples : `InsufficientBalanceException`, `FileAlreadyExistsException`.

---
**Votre exception personnalisée doit être Unchecked si :**
- L'erreur est due à un **bug** ou une **violation de contrat**.
- Exemples : `InvalidAgeException`, `IllegalStateException`.

**Règle moderne :** Beaucoup de développeurs préfèrent les Unchecked Exceptions pour éviter de polluer les signatures de méthodes avec `throws`. À vous de juger selon le contexte.

---

## 🔗 Lien avec FoodFast

**Question 7 du TP :**

Vous devez créer une exception `OrderPreparationException`.

**Réflexion :**
- Devrait-elle être Checked ou Unchecked ?
- Quel message d'erreur fournirez-vous ?
- Comment l'utiliser dans la méthode `prepare(Order order)` ?

**Indice :** Une erreur de préparation est **récupérable** (on peut annuler la commande). Une Checked Exception serait donc appropriée.

---

**Structure attendue :**
```java
public class OrderPreparationException extends Exception {
    public OrderPreparationException(String message) {
        super(message);
    }
}
```

---

<!-- Notes présentateur:
Durée: 10min
Stratégie: Donner des règles concrètes pour bien gérer les exceptions.
Focus: Ne pas avaler les exceptions, logger proprement, ne pas utiliser les exceptions pour le flux normal.
-->

## 4. Best Practices : Bien Gérer les Exceptions

---

## Best Practice 1 : Ne Jamais Avaler les Exceptions

**❌ Mauvaise Pratique :**
```java
try {
    // Code susceptible de lever une exception
} catch (Exception e) {
    // Rien du tout
}
```
---
**Problème :** L'exception est **ignorée**. Impossible de détecter le problème.

**✅ Bonne Pratique :**
```java
try {
    // Code susceptible de lever une exception
} catch (Exception e) {
    System.err.println("Erreur : " + e.getMessage());
    e.printStackTrace(); // Affiche la pile d'appels
}
```
---
**Encore mieux (avec un logger) :**
```java
try {
    // Code susceptible de lever une exception
} catch (Exception e) {
    logger.error("Erreur lors du traitement", e);
}
```

---

## Best Practice 2 : Logger les Exceptions

**❌ Mauvaise Pratique :**
```java
catch (IOException e) {
    System.out.println("Erreur"); // Message trop vague
}
```
---
**✅ Bonne Pratique :**
```java
catch (IOException e) {
    logger.error("Erreur lors de la lecture du fichier {}", filePath, e);
}
```
---
**Avantages :**
- Message **contextualisé** (quel fichier ?).
- La **pile d'appels** est loggée (pour le débogage).
- Le log peut être redirigé vers un fichier ou un service de monitoring.

---

## Best Practice 3 : Ne Pas Utiliser les Exceptions pour le Flux Normal

**❌ Mauvaise Pratique :**
```java
public Integer findById(String id) {
    if (!map.containsKey(id)) {
        throw new NotFoundException("ID introuvable");
    }
    return map.get(id);
}
---
// Utilisation
try {
    Integer value = findById("123");
} catch (NotFoundException e) {
    // Cas "normal" : l'ID n'existe pas
}
```
---
**Problème :** Les exceptions sont **coûteuses** (création de la pile d'appels). Elles ne doivent pas être utilisées pour le flux normal de l'application.

---

## Best Practice 3 : Utiliser `Optional` pour l'Absence

**✅ Bonne Pratique :**
```java
public Optional<Integer> findById(String id) {
    return Optional.ofNullable(map.get(id));
}

// Utilisation
Optional<Integer> value = findById("123");
if (value.isPresent()) {
    System.out.println(value.get());
} else {
    System.out.println("ID introuvable");
}
```

**Règle :** Utilisez `Optional` pour représenter l'**absence de valeur**. Réservez les exceptions pour les **erreurs exceptionnelles**.

---

## Best Practice 4 : Attraper les Exceptions Spécifiques

**❌ Mauvaise Pratique :**
```java
try {
    // Code complexe
} catch (Exception e) {
    // Attrape TOUTES les exceptions (même les bugs)
}
```
---
**Problème :** Masque les bugs et rend le débogage difficile.

**✅ Bonne Pratique :**
```java
try {
    // Code complexe
} catch (IOException e) {
    logger.error("Erreur d'IO", e);
} catch (SQLException e) {
    logger.error("Erreur SQL", e);
}
```

**Règle :** N'attrapez que les exceptions que vous pouvez **réellement gérer**. Laissez les autres se propager.

---

## Best Practice 5 : Documenter les Exceptions avec `@throws`

**✅ Bonne Pratique :**
```java
/**
 * Retire un montant du compte.
 *
 * @param amount le montant à retirer
 * @throws InsufficientBalanceException si le solde est insuffisant
 * @throws IllegalArgumentException si le montant est négatif
 */
public void withdraw(double amount) throws InsufficientBalanceException {
    if (amount < 0) {
        throw new IllegalArgumentException("Le montant ne peut être négatif");
    }
    if (amount > balance) {
        throw new InsufficientBalanceException("Solde insuffisant");
    }
    balance -= amount;
}
```
---
**Avantage :** Les développeurs qui utilisent votre méthode savent **quelles exceptions** peuvent être levées et **pourquoi**.

---

## Best Practice 6 : Ne Pas Exposer les Détails d'Implémentation

**❌ Mauvaise Pratique :**
```java
public Order findOrder(String id) throws SQLException {
    // Laisse fuiter SQLException (détail d'implémentation)
}
```

**Problème :** Si vous changez de base de données (par ex. vers MongoDB), l'exception change aussi. Cela casse le contrat de votre API.

---
**✅ Bonne Pratique :**
```java
public Order findOrder(String id) throws OrderNotFoundException {
    try {
        // Code JDBC
    } catch (SQLException e) {
        throw new OrderNotFoundException("Commande introuvable", e);
    }
}
```

**Règle :** Encapsulez les exceptions techniques dans des exceptions métier.

---

## 🔗 Lien avec FoodFast

**Réflexion :**

Dans votre méthode `placeOrder()`, vous appelez `restaurant.prepare(order)` qui peut lancer une `OrderPreparationException`.

**Questions :**
- Comment allez-vous logger l'erreur ?
- Allez-vous relancer l'exception ou la gérer localement ?
- Comment informer l'utilisateur qu'une commande n'a pas pu être préparée ?

---

**Structure attendue :**
```java
try {
    restaurant.prepare(order);
    logger.info("Commande {} préparée avec succès", order.getId());
} catch (OrderPreparationException e) {
    logger.error("Erreur de préparation pour la commande {}", order.getId(), e);
    order.setStatus(OrderStatus.CANCELLED);
}
```

---

<!-- Notes présentateur:
Durée: 2min
Stratégie: Résumé rapide des deux parties.
-->

## Conclusion : Ce que Nous Avons Vu Aujourd'hui

**Partie 1 : Programmation Fonctionnelle**
- **Lambdas** : Fonctions anonymes pour un code plus concis.
- **Interfaces Fonctionnelles** : `Predicate`, `Function`, `Consumer`, `Supplier`.
- **API Stream** : Manipulation déclarative des collections (`filter`, `map`, `collect`, etc.).
- **Method References** : Raccourcis pour les lambdas.

---

**Partie 2 : Gestion des Erreurs**
- **Try-Catch-Finally** : Mécanisme de base pour gérer les exceptions.
- **Checked vs Unchecked** : Savoir quand utiliser quel type d'exception.
- **Exceptions Personnalisées** : Représenter les erreurs métier.
- **Best Practices** : Logger, ne pas avaler les exceptions, utiliser `Optional`, etc.

---

## Prochaine Étape : Le TP FoodFast

**Questions 6 et 7 :**

**Question 6 :** Implémentez les méthodes de recherche dans `DeliveryPlatform` en utilisant **l'API Stream** et des **lambdas**.

**Question 7 :** Créez l'exception `OrderPreparationException` et gérez-la dans `placeOrder()` avec un bloc **try-catch**.

**Objectif :** Appliquer directement les concepts vus aujourd'hui !

---

## Questions ?

N'hésitez pas à demander des clarifications ou des exemples supplémentaires.

**Bon courage pour le TP !**
