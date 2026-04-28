# TP : FoodFast

## Introduction

Bienvenue dans l'équipe de développement de FoodFast ! Ce projet sera notre fil rouge tout au long du semestre. Nous allons construire, étape par étape, un prototype de système de livraison de repas, en appliquant à chaque séance les concepts vus en cours. L'objectif est d'avoir un projet qui évolue, gagne en complexité et en robustesse au fil de votre apprentissage.

---

## Partie 1 : Initialisation du Projet et Algorithmes de Base (Séance 1)

*Objectif : Se familiariser avec l'environnement Java, la syntaxe de base et les tests unitaires à travers des tâches simples présentées comme des "utilitaires" pour le projet FoodFast.*

### Question 1 : "Hello, FoodFast!"

Créez une classe `Application` avec une méthode `main` qui affiche "Bienvenue chez FoodFast !" ainsi que les arguments passés en ligne de commande.

*Concepts-clés : `public static void main`, `System.out.println`, compilation `javac` et exécution `java`.*

### Question 2 : "Utilitaires de Logique Métier" (avec TDD)

Créez une classe `FoodFastUtils` qui contiendra des méthodes `static` utiles pour notre logique métier.

**a) Planificateur de livraison (FizzBuzz) :** Créez une méthode `static String deliveryPlanner(int n)` qui retourne "Fizz" pour les multiples de 3, "Buzz" pour les multiples de 5, et "FizzBuzz" pour les multiples des deux. Cet "algorithme" nous aidera à planifier nos lots de livraison. **Écrivez les tests JUnit avant d'écrire le code (TDD).**

**b) Calcul de promotions (Année Bissextile) :** Créez une méthode `static boolean isLeapYear(int year)` qui détermine si une année est bissextile, pour nos futures campagnes promotionnelles. **Écrivez les tests JUnit d'abord.**

*Concepts-clés : Conditions `if`/`else`, opérateurs logiques et arithmétiques, TDD avec JUnit.*

### Question 3 : "Manipulation de Données"

Toujours dans `FoodFastUtils`, ajoutez les méthodes suivantes :

**a) Outils d'analyse :** Une méthode `static int sumUpTo(int n)` qui utilise une boucle pour calculer la somme de tous les entiers de 1 à n.

**b) Anonymisation :** Une méthode `static String anonymize(String text)` qui inverse la chaîne de caractères passée en paramètre pour anonymiser un ID.

*Concepts-clés : Boucles `for`/`while`, manipulation de `String`.*

---

## Partie 2 : Modélisation Orientée Objet (Séance 2)

*Objectif : Appliquer les principes de la POO pour construire le cœur du système.*

### Question 4 : "Le Cœur du Système : Les Objets Métier"

Modélisez les objets principaux de FoodFast. **Pour chaque classe, implémentez un constructeur, `equals()`, `hashCode()` et `toString()`, et écrivez les tests JUnit associés.**

1.  **`enum DishSize { SMALL, MEDIUM, LARGE }`**
2.  **`class Dish`** : `String name`, `BigDecimal price`, `DishSize size`.
3.  **`class Customer`** : `String id`, `String name`, `String address`.
4.  **`enum OrderStatus { PENDING, IN_PREPARATION, COMPLETED, CANCELLED }`**
5.  **`class Order`** :
    *   `String id` (généré avec `UUID.randomUUID().toString()`).
    *   `OrderStatus status` (initialisé à `PENDING`).
    *   `Map<Dish, Integer> dishes` (pour stocker les plats et leur quantité).
    *   `Customer customer`.
    *   `LocalDateTime orderDate`.
    *   Ajoutez une méthode `BigDecimal calculateTotalPrice()`.

*Concepts-clés : POO, Encapsulation, `enum`, `Map`, `BigDecimal`, `LocalDateTime`, `UUID`.*

### Question 5 : "La Plateforme de Livraison"

1.  Créez la classe `DeliveryPlatform`.
2.  Donnez-lui une `Map<String, Order>` pour stocker les commandes en cours.
3.  Implémentez les méthodes suivantes :
    *   `void placeOrder(Order order)`
    *   `Optional<Order> findOrderById(String orderId)`

*Concepts-clés : `List`, `Map`, `Optional`.*

---

## Partie 3 : Logique Applicative et Robustesse (Séance 3)

*Objectif : Manipuler des collections avec des fonctionnalités modernes de Java et rendre l'application robuste.*

### Question 6 : "Recherche Avancée de Commandes"

Dans `DeliveryPlatform`, ajoutez les méthodes de recherche suivantes en utilisant **l'API Stream et des lambdas**.

1.  `List<Order> findOrdersByCustomer(Customer customer)`
2.  `List<Order> findOrdersByStatus(OrderStatus status)`

*Concepts-clés : API Stream, `filter`, `collect`, Lambdas, `Predicate`.*

### Question 7 : "Gestion des Erreurs de Préparation"

1.  Créez une exception personnalisée `OrderPreparationException`.
2.  Dans une classe `Restaurant`, créez une méthode `prepare(Order order)` qui simule une préparation. Cette méthode aura 20% de chances de lancer votre `OrderPreparationException`.
3.  Dans `DeliveryPlatform`, lors de l'ajout d'une commande, appelez cette méthode et utilisez un bloc `try-catch` pour gérer l'erreur (par exemple, en passant la commande au statut `CANCELLED` et en affichant un message).

*Concepts-clés : Exceptions, `try-catch-finally`, exceptions personnalisées.*

---

## Partie 4 : Concurrence et Persistance (Séance 4)

*Objectif : Préparer l'application à un environnement multi-utilisateurs et persister les données.*

### Question 8 : "Montée en Charge (Concurrence)"

1.  Rendez `DeliveryPlatform` *thread-safe*. Utilisez une `ConcurrentHashMap` pour stocker les commandes.
2.  Dans votre `main`, créez un `ExecutorService` avec plusieurs threads pour simuler plusieurs restaurants qui passent des commandes en même temps.
3.  Assurez-vous que les méthodes qui modifient l'état de la plateforme (comme `placeOrder`) sont correctement synchronisées pour éviter les *race conditions*.
4.  **Discussion Sécurité :** Expliquez comment une *race condition* peut devenir une faille de sécurité de type **TOCTOU** (Time-of-check to Time-of-use).

*Concepts-clés : Concurrence, `ExecutorService`, `ConcurrentHashMap`, `synchronized`.*

### Question 9 : "Persistance en Base de Données (JDBC)"

1.  Configurez une connexion JDBC à une base de données PostgreSQL.
2.  Créez une méthode dans `DeliveryPlatform` qui sauvegarde une commande dans une table `orders`.
3.  **Utilisez systématiquement `PreparedStatement`** pour construire vos requêtes.
4.  **Discussion Sécurité :** Expliquez, avec un exemple de code, comment `PreparedStatement` prévient les **injections SQL** par rapport à une concaténation de `String`.

*Concepts-clés : JDBC, `Connection`, `PreparedStatement`, `try-with-resources`.*

---

## Partie 5 (Bonus) : Architecture Avancée

*Objectif : Pour ceux qui ont terminé, refactoriser l'application vers une architecture événementielle, plus souple et évolutive.*

### Question 10 : "Journalisation des Événements (Pattern Singleton)"

Créez une classe `Logger` qui utilise le pattern **Singleton** pour tracer tous les événements importants (création de commande, erreur, etc.). **Discutez en commentaire les limites de ce pattern et les alternatives modernes (comme l'injection de dépendances).**

### Question 11 : "Sauvegarde des Commandes (I/O & Sérialisation)"

1.  Rendez vos classes de modèle `Serializable`.
2.  Dans `DeliveryPlatform`, ajoutez les méthodes `savePlatformState(String filePath)` et `loadPlatformState(String filePath)` qui utilisent `ObjectOutputStream` et `ObjectInputStream`.
3.  **Discussion Sécurité :**
    *   **Path Traversal :** Comment un attaquant pourrait-il exploiter le paramètre `filePath` ? Comment s'en protéger ?
    *   **Désérialisation Non Sécurisée :** Expliquez l'attaque par **collision de hash** qui peut mener à un Déni de Service.

### Question 12 : "Architecture Événementielle (EventBus)"

1.  **Conception :** Créez un `EventBus` (en Singleton), une interface `Event`, et des événements concrets comme `OrderPlacedEvent`.
2.  **Refactoring :** Modifiez `DeliveryPlatform` pour qu'elle publie des événements au lieu d'appeler d'autres services directement.
3.  **Abonnement :** Transformez vos services (Logger, NotificationService, etc.) en `Subscriber` qui s'abonnent et réagissent aux événements.

*Concepts-clés : Design Patterns, Découplage, Architecture événementielle.*