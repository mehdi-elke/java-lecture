---
title: "Java - Séance 4"
description: "Multithreading et Accès aux Données"
author: Mehdi Elketroussi
theme: gaia
size: 16:9
paginate: true
marp: true
---

<!-- Notes présentateur:
Durée totale du contenu: 2h (contenu riche pour apprentissage autonome)
Durée du parcours en cours: 1h25-1h30 (slides marquées [ESSENTIELLE])
Stratégie: Les étudiants ont une petite présentation à faire, donc on suit uniquement les slides ESSENTIELLES.
         Les slides BONUS et RAPIDE sont dans le support pour l'apprentissage autonome.
Focus: Concurrence (race conditions + ExecutorService), I/O moderne (Files API), JDBC (PreparedStatement)
IMPORTANT: try-with-resources a été vu en Séance 3, donc rappel RAPIDE uniquement
-->

# Séance 4 : Multithreading et Accès aux Données

**Objectif :** Comprendre la programmation concurrente, gérer les ressources système et persister les données de manière professionnelle.

---

<!-- [ESSENTIELLE] À PRÉSENTER (2min) -->

## Plan de la Séance

**Partie 1 : Concurrence et Parallélisation** (contenu: 70-80 min, cours: 45-50 min)
- Introduction : Pourquoi la Concurrence ?
- Les Threads
- Race Conditions et Synchronisation
- ExecutorService et Pools de Threads
- Focus Sécurité : TOCTOU

---

**Partie 2 : I/O et Sérialisation** (contenu: 20-25 min, cours: 15 min)
- Rappel : try-with-resources
- API I/O Moderne
- Sérialisation d'Objets

---

**Partie 3 : Persistance avec JDBC** (contenu: 35-40 min, cours: 20-25 min)
- Introduction à JDBC
- Connexion à une Base de Données
- PreparedStatement
- Focus Sécurité : Injection SQL

<!--
NOTES PRÉSENTATEUR :
- Système de marquage des slides :
  [ESSENTIELLE] = À présenter en cours (1h30 total)
  [RAPIDE] = Mentionner si temps disponible
  [BONUS] = À sauter, lecture autonome pour les étudiants
-->

---

<!-- PARTIE 1 : CONCURRENCE -->

# Partie 1 : Concurrence et Parallélisation

<!--
NOTES PRÉSENTATEUR - Concurrence (45-50min en cours) :
- Objectif : Comprendre pourquoi la concurrence est essentielle et comment éviter les race conditions
- Slides ESSENTIELLES : Introduction, Threads, Race Condition, synchronized, ExecutorService
- Slides à SAUTER : Future/Callable, détails synchronisation avancée
- Message clé : "Les applications modernes sont multi-utilisateurs, donc concurrentes"
- PIÈGE à anticiper : "Pourquoi mon code ne plante pas toujours ?" → nature imprévisible des race conditions
-->

---

<!-- [ESSENTIELLE] À PRÉSENTER (5min) -->

## 1.1 Introduction : Pourquoi la Concurrence ?

La **concurrence** est la capacité d'un programme à exécuter plusieurs tâches "en même temps".

**Dans le monde réel :**
- Un serveur web traite des milliers de requêtes simultanées
- Une application mobile télécharge des données sans bloquer l'interface
- Un restaurant ne sert pas qu'un seul client à la fois

---

**Sans concurrence :**
```java
// Traiter les commandes une par une
processOrder(order1); // Bloque tout pendant 5 secondes
processOrder(order2); // Doit attendre que order1 soit finie
processOrder(order3); // Doit attendre que order2 soit finie
```

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

**Avec concurrence :**
```java
// Traiter plusieurs commandes en parallèle
Thread t1 = new Thread(() -> processOrder(order1));
Thread t2 = new Thread(() -> processOrder(order2));
Thread t3 = new Thread(() -> processOrder(order3));

t1.start(); // Démarre immédiatement
t2.start(); // Démarre immédiatement
t3.start(); // Démarre immédiatement
// Les 3 commandes sont traitées en parallèle !
```

**Application FoodFast :** Plusieurs restaurants créent des commandes simultanément. La plateforme doit gérer cette concurrence sans perdre de données.

---

<!-- [ESSENTIELLE] À PRÉSENTER (8min) -->

## 1.2 Les Threads : Fondamentaux

Un **Thread** est la plus petite unité d'exécution. Plusieurs threads peuvent s'exécuter en parallèle dans un même programme et **partagent la même mémoire**.

**Créer un Thread avec `Runnable` (interface fonctionnelle) :**

---

```java
// L'interface Runnable définit une tâche à exécuter
Runnable task = () -> {
    System.out.println("Je m'exécute dans un thread séparé !");
    System.out.println("Thread: " + Thread.currentThread().getName());
};

// Créer et démarrer le thread
Thread thread = new Thread(task);
thread.start(); // Lance l'exécution en parallèle

System.out.println("Thread principal: " + Thread.currentThread().getName());
```

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

**Résultat (ordre non déterministe) :**
```
Thread principal: main
Je m'exécute dans un thread séparé !
Thread: Thread-0
```

**Point Clé :** L'ordre d'exécution des threads n'est **pas garanti**. C'est le système d'exploitation qui décide.

---

<!-- [BONUS] À sauter en cours -->

## 1.2.1 Créer un Thread Personnalisé (Approche Héritée)

**Ancienne méthode (moins recommandée) :** Hériter de `Thread`

```java
public class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("Thread personnalisé : " + getName());
    }
}

// Utilisation
MyThread t = new MyThread();
t.start();
```

---

**Pourquoi préférer `Runnable` ?**
- Java ne permet qu'un seul héritage (limitation)
- `Runnable` sépare la tâche de son exécution (meilleure conception)
- Compatible avec les lambdas (code plus concis)

---

<!-- [ESSENTIELLE] À PRÉSENTER (15min) -->

## 1.3 Le Problème : Race Conditions

Lorsque plusieurs threads **accèdent et modifient** une donnée partagée en même temps, des résultats **incorrects et imprévisibles** peuvent survenir. C'est une **race condition**.

**Exemple : Un Compteur Partagé**

```java
public class Counter {
    private int count = 0;

    public void increment() {
        // Cette opération SEMBLE atomique, mais ne l'est PAS :
        count++; // 1. Lire count  2. Ajouter 1  3. Écrire count
    }

    public int getCount() {
        return count;
    }
}
```

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

**Code de test qui "plante" :**

```java
Counter counter = new Counter();

// Créer 1000 threads qui incrémentent le compteur
for (int i = 0; i < 1000; i++) {
    new Thread(() -> counter.increment()).start();
}

try {
    Thread.sleep(2000); // Attendre que tous les threads finissent
} catch (InterruptedException e) {
    Thread.currentThread().interrupt();
}

System.out.println("Valeur finale : " + counter.getCount());
// Résultat attendu : 1000
// Résultat réel : 987, 993, 1000... (IMPRÉVISIBLE !)
```

**Pourquoi ?** Plusieurs threads lisent et écrivent `count` en même temps, certains incréments sont "écrasés".

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

### Visualisation de la Race Condition

```plaintext
count = 0

Thread 1                Thread 2
--------                --------
Lire count (0)
                        Lire count (0)
Calculer 0 + 1 = 1
                        Calculer 0 + 1 = 1
Écrire count = 1
                        Écrire count = 1  ← ÉCRASE la valeur !

Résultat : count = 1 (au lieu de 2)
```
---

**Un incrément a été perdu !** Avec 1000 threads, on perd des dizaines d'incréments.

---

<!-- [ESSENTIELLE] À PRÉSENTER (8min) -->

## 1.4 Solution : La Synchronisation avec `synchronized`

Pour résoudre les race conditions, on doit garantir l'**exclusion mutuelle** : une section critique du code ne doit être exécutée que par **un seul thread à la fois**.

**Java utilise le mot-clé `synchronized` :**

---

```java
public class Counter {
    private int count = 0;

    // Le "verrou" (monitor) de l'objet Counter est acquis
    // AVANT d'exécuter la méthode, et relâché APRÈS.
    public synchronized void increment() {
        count++; // Maintenant atomique dans ce contexte
    }

    public synchronized int getCount() {
        return count; // Lecture aussi protégée
    }
}
```

**Résultat :** Le compteur atteint toujours 1000, même avec 1000 threads !

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

### Visualisation : Le Verrou `synchronized`

Imaginez `synchronized` comme une **porte avec une seule clé** (le "verrou") pour accéder à une pièce (la "section critique").

<style scoped>
pre {
  font-size: 0.65em;
  text-align: left;
}
</style>

```plaintext
                        +--------------------+
                        | Section Critique   |
                        |   count++          |
                        | [VERROU/MONITOR]   |
                        +---------^----------+
                                  |
                         +--------+---------+
Thread 1 (a la clé) ---->|      PORTE       |
                         +------------------+
                                  |
                         <--X-- Thread 2 (bloqué, attend)
                                  |
                         <--X-- Thread 3 (bloqué, attend)
```

**Un seul thread peut modifier `count` à la fois**, garantissant la cohérence.

---

<!-- [BONUS] À sauter en cours -->

## 1.4.1 Synchronisation sur un Bloc (Contrôle Fin)

Au lieu de synchroniser toute la méthode, on peut synchroniser **un bloc spécifique** :

```java
public class Counter {
    private int count = 0;
    private final Object lock = new Object(); // Verrou explicite

    public void increment() {
        // Code non critique ici (peut être parallèle)

        synchronized (lock) { // Section critique
            count++;
        }

        // Code non critique ici (peut être parallèle)
    }
}
```

---

**Avantage :** Meilleure performance si seule une petite partie de la méthode doit être protégée.

---

<!-- [RAPIDE] Mentionner si temps (5min) -->

## 1.5 Collections Concurrentes

Modifier une collection standard (comme `HashMap` ou `ArrayList`) depuis plusieurs threads n'est **pas sûr** et peut :
- Lever une `ConcurrentModificationException`
- Corrompre silencieusement les données

---

**Java fournit des alternatives *thread-safe* :**

```java
// HashMap standard : NON thread-safe
Map<String, Integer> map = new HashMap<>();

// ConcurrentHashMap : thread-safe
Map<String, Integer> safeMap = new ConcurrentHashMap<>();
```

---

<!-- [RAPIDE] Mentionner si temps (suite) -->

**Autres collections concurrentes :**

| Collection Standard | Alternative Thread-Safe | Usage |
|---------------------|------------------------|-------|
| `ArrayList` | `CopyOnWriteArrayList` | Lectures fréquentes, écritures rares |
| `HashMap` | `ConcurrentHashMap` | Accès concurrent sans blocage global |
| `LinkedList` | `ConcurrentLinkedQueue` | File d'attente concurrente |

---

**Application FoodFast :**
```java
// Dans DeliveryPlatform, remplacer :
// private Map<String, Order> orders = new HashMap<>();

// Par :
private Map<String, Order> orders = new ConcurrentHashMap<>();
```

---

<!-- [ESSENTIELLE] À PRÉSENTER (12min) -->

## 1.6 ExecutorService : Gérer les Threads Professionnellement

Créer des threads manuellement (`new Thread()`) est :
- **Coûteux** : chaque thread consomme de la mémoire (~1 MB)
- **Non géré** : qui s'occupe de fermer les threads ?
- **Risqué** : créer trop de threads peut crasher l'application

**Solution :** `ExecutorService` et les **pools de threads**

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

### Qu'est-ce qu'un Pool de Threads ?

Un pool réutilise un **nombre fixe de threads** pour exécuter de nombreuses tâches.

```plaintext
Tâches en attente         Pool de Threads (4 threads)
----------------          --------------------------
[Tâche 1]                 Thread-1 → [Tâche 1]
[Tâche 2]                 Thread-2 → [Tâche 2]
[Tâche 3]        →        Thread-3 → [Tâche 3]
[Tâche 4]                 Thread-4 → [Tâche 4]
[Tâche 5] (attend...)
[Tâche 6] (attend...)
```
---

**Avantages :**
- Limite le nombre de threads actifs (évite la surcharge)
- Réutilise les threads (meilleure performance)
- Gestion automatique du cycle de vie

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

### Exemple Complet avec ExecutorService

```java
import java.util.concurrent.*;

public class ExecutorExample {
    public static void main(String[] args) {
        // Créer un pool avec 4 threads
        ExecutorService executor = Executors.newFixedThreadPool(4);

        // Définir une tâche
        Runnable task = () -> {
            String name = Thread.currentThread().getName();
            System.out.println("Tâche exécutée par " + name);
        };
```

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

```java
        // Soumettre 10 tâches au pool (seules 4 s'exécutent en parallèle)
        for (int i = 0; i < 10; i++) {
            executor.submit(task);
        }

        // CRUCIAL : arrêter l'executor pour libérer les ressources
        executor.shutdown();
    }
}
```

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

**Résultat :**
```
Tâche exécutée par pool-1-thread-1
Tâche exécutée par pool-1-thread-2
Tâche exécutée par pool-1-thread-3
Tâche exécutée par pool-1-thread-4
Tâche exécutée par pool-1-thread-1  ← Thread réutilisé !
Tâche exécutée par pool-1-thread-2
...
```

**Point Clé :** `shutdown()` est **obligatoire** pour éviter que les threads du pool ne tournent indéfiniment (fuite de ressources).

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

### Application FoodFast

**Contexte :** Simuler plusieurs restaurants qui créent des commandes simultanément.

--- 

```java
 executor = Executors.newFixedThreadPool(10);
DeliveryPlatform platform = new DeliveryPlatform();

// Simuler 100 commandes créées en parallèle
for (int i = 0; i < 100; i++) {
    final int orderNum = i;
    executor.submit(() -> {
        Order order = createRandomOrder(orderNum);
        platform.placeOrder(order);
    });
}

executor.shutdown(); // Ne plus accepter de nouvelles tâches
```
---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

```java
try {
    // Attendre max 1 minute que toutes les tâches se terminent
    if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
        executor.shutdownNow(); // Forcer l'arrêt si timeout
    }
} catch (InterruptedException e) {
    executor.shutdownNow();
    Thread.currentThread().interrupt();
}

System.out.println("Commandes créées : " + platform.getOrderCount());
```

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

**À vous de jouer :** Rendez `DeliveryPlatform` thread-safe (Question 8 du TP).

---

<!-- [BONUS] À sauter en cours -->

## 1.6.1 Future et Callable : Récupérer un Résultat

`Runnable` ne peut pas retourner de résultat. Pour cela, on utilise `Callable<T>`.

```java
ExecutorService executor = Executors.newFixedThreadPool(2);

// Callable retourne un résultat de type Integer
Callable<Integer> calculation = () -> {
    Thread.sleep(1000); // Simule un calcul long
    return 42;
};

// submit() retourne un Future<Integer>
Future<Integer> future = executor.submit(calculation);

System.out.println("Calcul en cours...");

// Bloque jusqu'à ce que le résultat soit disponible
Integer result = future.get();
System.out.println("Résultat : " + result); // 42

executor.shutdown();
```

---

<!-- [BONUS] À sauter en cours -->

**`Future<T>` : Méthodes Principales**

| Méthode | Description |
|---------|-------------|
| `get()` | Bloque et attend le résultat |
| `get(timeout, unit)` | Attend avec timeout |
| `isDone()` | Vérifie si la tâche est terminée |
| `cancel(mayInterrupt)` | Annule la tâche |

**Usage :** Exécuter des calculs lourds en parallèle et récupérer les résultats quand ils sont prêts.

---

<!-- [RAPIDE] Mentionner si temps (5min) -->

## 1.7 Focus Sécurité : TOCTOU (Time-of-Check to Time-of-Use)

Une **race condition** peut devenir une **faille de sécurité grave**.

**Scénario Vulnérable :**

```java
// Faille TOCTOU
public void deleteAllOrders(User user) {
    if (user.isAdmin()) { // ← CHECK (vérification)
        // DANGER : Si un autre thread retire les droits admin ICI...
        this.orders.clear(); // ← USE (utilisation)
    }
}
```
---

**Que se passe-t-il ?**
1. Thread 1 vérifie `user.isAdmin()` → `true`
2. **Thread 2 retire les droits admin à `user`** (entre CHECK et USE)
3. Thread 1 exécute `orders.clear()` → **action non autorisée exécutée !**

---

<!-- [RAPIDE] Mentionner si temps (suite) -->

**Solution : Rendre l'Opération Atomique**

```java
public synchronized void deleteAllOrders(User user) {
    // Le synchronized garantit que CHECK et USE sont atomiques
    if (user.isAdmin()) {
        this.orders.clear(); // Personne ne peut modifier user entre temps
    }
}
```

---

<!-- [RAPIDE] Mentionner si temps (suite) -->

**Exemple FoodFast : Gestion de Stock Thread-Safe**

```java
public class Dish {
    private final AtomicInteger stock = new AtomicInteger(100);

    // Opération atomique : décrémenter seulement si stock suffisant
    public boolean tryDecrementStock(int quantity) {
        while (true) {
            int current = stock.get();
            if (current < quantity) return false; // Stock insuffisant
            // compareAndSet : atomique, réussit seulement si personne n'a modifié
            if (stock.compareAndSet(current, current - quantity)) {
                return true; // Succès
            }
            // Un autre thread a modifié stock entre temps, réessayer
        }
    }
}
```

---

<!-- [RAPIDE] Mentionner si temps (suite) -->

**Utilisation dans DeliveryPlatform :**

```java
// Dans DeliveryPlatform
public void placeOrder(Dish dish, int quantity) {
    if (dish.tryDecrementStock(quantity)) {
        createOrder(dish, quantity);
    } else {
        throw new InsufficientStockException("Stock insuffisant");
    }
}
```

**Avec `AtomicInteger` et `compareAndSet`, l'opération CHECK-USE devient atomique !**

---

<!-- PARTIE 2 : I/O ET SÉRIALISATION -->

# Partie 2 : I/O et Sérialisation

<!--
NOTES PRÉSENTATEUR - I/O (15min en cours) :
- Objectif : Manipuler les fichiers avec l'API moderne et comprendre la sérialisation
- IMPORTANT : try-with-resources a été vu en Séance 3, donc RAPPEL RAPIDE uniquement
- Slides ESSENTIELLES : Rappel try-with-resources, API Files
- Slides à SAUTER : Sérialisation détails, Path Traversal
- Message clé : "L'API moderne Files est plus simple que les anciennes classes I/O"
- PIÈGE à anticiper : "Pourquoi Path et pas String ?" → Type-safety et opérations riches
-->

---

<!-- [ESSENTIELLE] À PRÉSENTER (2min) -->

## 2.1 Rappel : try-with-resources

**Vu en Séance 3**, le mécanisme `try-with-resources` permet de fermer automatiquement les ressources.

**Rappel rapide :**

```java
// Les ressources (FileReader, Connection, etc.) sont automatiquement fermées
try (FileInputStream input = new FileInputStream("data.txt")) {
    int data = input.read();
    // Traiter les données...
} // input.close() appelé automatiquement, même en cas d'exception
```

---

**Pourquoi c'est important ici ?**
- Nous allons utiliser `try-with-resources` pour manipuler des fichiers avec l'API moderne
- C'est la bonne pratique pour toutes les opérations I/O et JDBC

**Si besoin de réviser les détails, voir Séance 3.**

---

<!-- [ESSENTIELLE] À PRÉSENTER (8min) -->

## 2.2 API I/O Moderne : `java.nio.file`

L'API moderne pour les fichiers est **`java.nio.file`** (introduite en Java 7).

**Classes principales :**
- `Path` : représente un chemin de fichier
- `Paths` : fabrique de `Path` (deprecated depuis Java 11, utiliser `Path.of()`)
- `Files` : méthodes utilitaires pour manipuler les fichiers

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

### Exemples avec Files

```java
import java.nio.file.*;

// Lire tout le contenu d'un fichier en une ligne
Path path = Path.of("commandes.txt");
String content = Files.readString(path);

// Écrire dans un fichier (écrase le contenu existant)
Files.writeString(path, "Nouvelle commande #123");

// Vérifier si un fichier existe
if (Files.exists(path)) {
    System.out.println("Le fichier existe");
}
```

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

```java
// Lister tous les fichiers d'un répertoire
try (var stream = Files.list(Path.of("."))) {
    stream.forEach(System.out::println);
}
```

**Avantage :** Code concis et moderne.

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

### Application FoodFast

Vous pourriez utiliser l'API `Files` pour :
- Sauvegarder des logs de commandes dans un fichier
- Charger une configuration de la plateforme
- Exporter l'état des commandes pour analyse

---

**Exemple conceptuel :**
```java
// Exporter toutes les commandes dans un fichier
Path exportPath = Path.of("exports/orders.txt");
List<String> orderLines = orders.values().stream()
    .map(order -> order.getId() + "," + order.getStatus())
    .collect(Collectors.toList());

Files.write(exportPath, orderLines);
```

**À vous de jouer :** Implémentez vos propres fonctionnalités I/O selon vos besoins !

---

<!-- [RAPIDE] Mentionner si temps (5min) -->

## 2.3 Sérialisation : Introduction

La **sérialisation** permet de transformer un objet en flux de bytes pour :
- Le sauvegarder dans un fichier
- Le transmettre sur le réseau
- Le stocker en cache

---

**Pour rendre une classe sérialisable :**

```java
import java.io.Serializable;

public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private BigDecimal totalPrice;
    // ...
}
```

---

<!-- [BONUS] À sauter en cours -->

## 2.3.1 Sérialisation : Exemple Complet

**Sauvegarder une liste d'objets :**

```java
import java.io.*;
import java.util.*;

// Liste de commandes (IDs)
List<String> orderIds = new ArrayList<>();
orderIds.add("order-123");
orderIds.add("order-456");
orderIds.add("order-789");

try (ObjectOutputStream oos = new ObjectOutputStream(
        new FileOutputStream("orders.ser"))) {
    oos.writeObject(orderIds);
}
```

**Charger la liste :**

```java
try (ObjectInputStream ois = new ObjectInputStream(
        new FileInputStream("orders.ser"))) {
    @SuppressWarnings("unchecked")
    List<String> loadedOrders = (List<String>) ois.readObject();
    System.out.println("Commandes chargées : " + loadedOrders.size());
}
```

---

<!-- [BONUS] À sauter en cours -->

### Le Mot-Clé `transient`

Certains champs ne doivent **pas** être sérialisés (ex: mots de passe, connexions DB) :

```java
public class User implements Serializable {
    private String username;
    private transient String password; // Ne sera PAS sauvegardé
    private transient Connection dbConnection; // Non sérialisable
}
```
---
**Application FoodFast (TP Bonus Question 11) :**
```java
public class DeliveryPlatform implements Serializable {
    private Map<String, Order> orders; // Sérialisé
    private transient ExecutorService executor; // Non sérialisé
}
```

---

<!-- [BONUS] À sauter en cours -->

## 2.4 Focus Sécurité : Path Traversal

C'est une vulnérabilité où un attaquant manipule un chemin de fichier pour accéder à des fichiers en dehors du répertoire autorisé.

**Attaque :**
```java
String userInput = "../../etc/passwd"; // Entrée malveillante
Path path = Path.of("/var/www/uploads/" + userInput);
// Résultat : /var/www/uploads/../../etc/passwd = /etc/passwd !!!
String content = Files.readString(path); // Accès non autorisé !
```

---

<!-- [BONUS] À sauter en cours -->

**Prévention : Valider et Normaliser**

```java
Path baseDir = Path.of("/var/www/uploads");
String userInput = "../../etc/passwd"; // Tentative d'attaque

// Résoudre le chemin relatif
Path userPath = baseDir.resolve(userInput);

// Normaliser (résout les "..")
Path normalizedPath = userPath.normalize().toAbsolutePath();

// Vérifier que le chemin reste dans le répertoire autorisé
if (!normalizedPath.startsWith(baseDir.toAbsolutePath())) {
    throw new SecurityException("Accès interdit : " + userInput);
}

// Maintenant sûr de lire le fichier
String content = Files.readString(normalizedPath);
```

**Règle d'or :** TOUJOURS valider les chemins construits à partir d'entrées utilisateur.

---

<!-- PARTIE 3 : JDBC -->

# Partie 3 : Persistance avec JDBC

<!--
NOTES PRÉSENTATEUR - JDBC (20-25min en cours) :
- Objectif : Comprendre comment sauvegarder des données dans une base de données et se protéger des injections SQL
- Slides ESSENTIELLES : Introduction, Connexion, PreparedStatement, Injection SQL
- Slides à SAUTER : Architecture détaillée, configuration Maven
- Message clé : "PreparedStatement n'est pas qu'une bonne pratique, c'est une protection de sécurité critique"
- PIÈGE à anticiper : "Pourquoi pas juste concaténer une String ?" → démonstration d'injection SQL
-->

---

<!-- [RAPIDE] Mentionner si temps (3min) -->

## 3.1 Introduction : Pourquoi JDBC ?

Jusqu'ici, nos données sont **en mémoire** :
- Elles disparaissent au redémarrage de l'application
- Impossible de les partager entre plusieurs instances
- Pas de recherche avancée (index, jointures)

**Solution :** Sauvegarder les données dans une **base de données relationnelle** (PostgreSQL, MySQL, H2...).

**JDBC (Java Database Connectivity)** est l'API standard pour interagir avec les bases de données.

**Application FoodFast :** Les commandes doivent survivre à un redémarrage de la plateforme.

---

<!-- [BONUS] À sauter en cours -->

## 3.1.1 Architecture JDBC

```plaintext
+-----------------+
| Application     |
| Java            |
+-----------------+
        |
        | JDBC API (java.sql.*)
        v
+-----------------+
| DriverManager   |  ← Gère les connexions
+-----------------+
        |
        | Driver JDBC (ex: postgresql.jar)
        v
+-----------------+
| Base de Données |  ← PostgreSQL, MySQL, H2...
| (PostgreSQL)    |
+-----------------+
```
---

**Dépendance Maven pour PostgreSQL :**
```xml
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.6.0</version>
</dependency>
```

---

<!-- [ESSENTIELLE] À PRÉSENTER (5min) -->

## 3.2 Connexion à une Base de Données

**Les 4 étapes JDBC :**
1. **Connect** : Établir une connexion
2. **Prepare** : Préparer une requête SQL
3. **Execute** : Exécuter la requête
4. **Close** : Fermer les ressources (avec try-with-resources)

---

**Exemple : Se Connecter à PostgreSQL**

```java
import java.sql.*;

public class DatabaseConnection {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/foodfast";
        String user = "postgres";
        String password = "secret";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Connexion réussie !");
        } catch (SQLException e) {
            System.err.println("Erreur de connexion : " + e.getMessage());
        }
    }
}
```

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

**Anatomie de l'URL JDBC :**

```
jdbc:postgresql://localhost:5432/foodfast
 ^      ^           ^         ^      ^
 |      |           |         |      +-- Nom de la base de données
 |      |           |         +--------- Port (5432 par défaut pour PostgreSQL)
 |      |           +------------------- Hôte (localhost = machine locale)
 |      +------------------------------- Type de base (postgresql, mysql, h2...)
 +-------------------------------------- Protocole JDBC
```

**Point Clé :** `Connection` implémente `AutoCloseable`, donc on utilise try-with-resources.

**ATTENTION :** Ne JAMAIS hardcoder les credentials dans le code (utiliser des variables d'environnement ou un fichier de config).

---

<!-- [ESSENTIELLE] À PRÉSENTER (10min) -->

## 3.3 Exécuter des Requêtes : `PreparedStatement`

**`PreparedStatement`** est l'outil pour exécuter des requêtes SQL avec des paramètres.

**Pourquoi pas juste une `String` ?** → Faille de sécurité (injection SQL, voir slide suivante)

---

**Syntaxe : Utiliser des placeholders `?`**

```java
String sql = "INSERT INTO users (id, name, email) VALUES (?, ?, ?)";
//                                                          ^  ^  ^
//                                                          |  |  |
//                                    Placeholders ---------+--+--+
```

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

### Exemple INSERT : Insérer un Utilisateur

```java
String url = "jdbc:postgresql://localhost:5432/foodfast";

try (Connection conn = DriverManager.getConnection(url, "user", "pass");
     PreparedStatement ps = conn.prepareStatement(
         "INSERT INTO users (id, name, email) VALUES (?, ?, ?)")) {

    // Lier les valeurs aux placeholders
    ps.setString(1, "user-123");        // 1er ? (id)
    ps.setString(2, "Alice");           // 2ème ? (name)
    ps.setString(3, "alice@example.com"); // 3ème ? (email)

    // Exécuter l'insertion
    int rowsInserted = ps.executeUpdate();
    System.out.println(rowsInserted + " ligne insérée");

} catch (SQLException e) {
    System.err.println("Erreur SQL : " + e.getMessage());
}
```

---

**Important :** Les index commencent à **1** (pas 0).

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

### Exemple SELECT : Récupérer des Données

```java
String sql = "SELECT id, name, email FROM users WHERE name = ?";

try (Connection conn = DriverManager.getConnection(url, "user", "pass");
     PreparedStatement ps = conn.prepareStatement(sql)) {

    ps.setString(1, "Alice"); // Paramètre de recherche

    // Exécuter la requête et obtenir les résultats
    try (ResultSet rs = ps.executeQuery()) {
```

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

```java
        while (rs.next()) { // Parcourir les lignes
            String id = rs.getString("id");
            String name = rs.getString("name");
            String email = rs.getString("email");

            System.out.println(id + " - " + name + " - " + email);
        }
    }
}
```

----

**`ResultSet`** : curseur sur les résultats. On parcourt avec `next()`.

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

### Application FoodFast : Sauvegarder une Commande

**Schéma de table SQL :**
```sql
CREATE TABLE orders (
    id VARCHAR(255) PRIMARY KEY,
    customer_name VARCHAR(255),
    total_price DECIMAL(10, 2),
    status VARCHAR(50),
    order_date TIMESTAMP
);
```

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

**Code Java (TP Question 9) :**
```java
String sql = "INSERT INTO orders (id, customer_name, total_price, status, order_date) "
           + "VALUES (?, ?, ?, ?, ?)";

try (PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setString(1, order.getId());
    ps.setString(2, order.getCustomer().getName());
    ps.setBigDecimal(3, order.calculateTotalPrice());
    ps.setString(4, order.getStatus().name());
    ps.setTimestamp(5, Timestamp.valueOf(order.getOrderDate()));

    ps.executeUpdate();
}
```

---

<!-- [ESSENTIELLE] À PRÉSENTER (7min) -->

## 3.4 Focus Sécurité : Injection SQL

L'**injection SQL** est l'une des failles de sécurité les plus dangereuses (OWASP Top 10).

**Principe :** Un attaquant injecte du code SQL malveillant via une entrée utilisateur.

---

**Code VULNÉRABLE (concaténation de String) :**

```java
String userInput = "alice'; DROP TABLE users; --";
String sql = "SELECT * FROM users WHERE name = '" + userInput + "'";
// Résultat : SELECT * FROM users WHERE name = 'alice'; DROP TABLE users; --'
//                                                        ^^^^^^^^^^^^^^^^^^^
//                                                        CODE SQL MALVEILLANT !

Statement stmt = conn.createStatement();
stmt.executeQuery(sql); // La table users est SUPPRIMÉE !
```

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

### Démonstration de l'Attaque

**Scénario : Connexion utilisateur**

```java
// Formulaire de login
String username = request.getParameter("username"); // Entrée utilisateur
String password = request.getParameter("password");

// CODE VULNÉRABLE
String sql = "SELECT * FROM users WHERE username = '" + username
           + "' AND password = '" + password + "'";
```

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

**Attaque classique :**
- Username : `admin' --`
- Password : `anything`

**Requête générée :**
```sql
SELECT * FROM users
WHERE username = 'admin' --' AND password = 'anything'
                         ^^^
                         Commentaire SQL : tout le reste est ignoré !
```

**Résultat :** L'attaquant se connecte sans connaître le mot de passe !

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

### Pourquoi `PreparedStatement` Protège ?

`PreparedStatement` **sépare la structure SQL des données**.

---
**Code SÉCURISÉ :**


```java
String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

try (PreparedStatement ps = conn.prepareStatement(sql)) {
    ps.setString(1, "admin' OR '1'='1"); // Traité comme une DONNÉE
    ps.setString(2, "anything");

    ResultSet rs = ps.executeQuery();
    // La requête cherche un utilisateur dont le nom est littéralement
    // "admin' OR '1'='1" (chaîne complète), pas d'injection possible !
}
```
---
**Mécanisme :**
- Le SQL est **compilé** par la base de données AVANT d'insérer les paramètres
- Les paramètres sont transmis séparément et **échappés automatiquement**
- Impossible d'injecter du code SQL

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

### Visualisation : String vs PreparedStatement

```plaintext
CONCATÉNATION (VULNÉRABLE)
------------------------------
Application → "SELECT * FROM users WHERE id = '" + userInput + "'"
              ↓
Base de Données reçoit :
              "SELECT * FROM users WHERE id = 'alice'; DROP TABLE users; --'"
              → Interprète DROP TABLE comme du SQL → TABLE SUPPRIMÉE !


PREPAREDSTATEMENT (SÉCURISÉ)
--------------------------------
Application → "SELECT * FROM users WHERE id = ?"
              ↓ (compilation du SQL)
Base de Données → Structure SQL enregistrée
              ↓
Application → Envoie "alice'; DROP TABLE users; --" comme DONNÉE
              ↓
Base de Données → Cherche id = "alice'; DROP TABLE users; --"
              → Traité comme une simple chaîne, pas de code SQL exécuté
```

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

### Règle d'Or

**TOUJOURS utiliser `PreparedStatement` pour les requêtes avec des paramètres.**

**JAMAIS concaténer des Strings pour construire du SQL.**

---

```java
// INTERDIT
String sql = "SELECT * FROM orders WHERE id = '" + orderId + "'";

// OBLIGATOIRE
String sql = "SELECT * FROM orders WHERE id = ?";
PreparedStatement ps = conn.prepareStatement(sql);
ps.setString(1, orderId);
```

---

**Même si vous faites confiance à la source des données, utilisez `PreparedStatement` :**
- Protection contre les bugs futurs
- Meilleure performance (requête pré-compilée)
- Bonne pratique professionnelle

---

<!-- [ESSENTIELLE] À PRÉSENTER (5min) -->

## Conclusion : Bilan du Module Java

### Ce que Vous Avez Appris en 4 Séances

**Séance 1 :** Syntaxe de base, structures de contrôle, TDD avec JUnit
**Séance 2 :** POO, Collections, Types riches, Interfaces et Polymorphisme
**Séance 3 :** Programmation fonctionnelle (Lambdas, Streams), Gestion des exceptions
**Séance 4 :** Concurrence, Gestion des ressources (I/O), Persistance (JDBC)

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

### Compétences Acquises

**Vous êtes maintenant capable de :**
- Écrire des programmes Java robustes et testés
- Modéliser des problèmes métier avec la POO
- Manipuler des collections de données de manière moderne (Streams)
- Gérer les erreurs et les cas limites (Exceptions)
- Créer des applications multi-threads sécurisées
- Interagir avec des bases de données de manière sécurisée
- Appliquer les bonnes pratiques de sécurité (TOCTOU, Injection SQL, Path Traversal)

**Ces compétences sont le socle de tout développement Java professionnel.**

---

<!-- [ESSENTIELLE] À PRÉSENTER (suite) -->

### Prochaines Étapes

**Pour aller plus loin :**
- **Maven/Gradle** : gestion de dépendances et build
- **Spring Framework** : développement d'applications web et microservices
- **Tests avancés** : Mockito, tests d'intégration
- **Design Patterns** : Singleton, Factory, Observer, Strategy...
- **Architecture** : Clean Architecture, Hexagonal Architecture
- **ORM (Hibernate/JPA)** : alternative à JDBC pour la persistance

**Le projet FoodFast est votre base pour explorer ces concepts !**

---

<!-- [ESSENTIELLE] À PRÉSENTER -->

# Questions ?

**Merci pour votre attention !**

**Rendez-vous pour le TP : Partie 4 - Concurrence et Persistance**

<!--
NOTES PRÉSENTATEUR - Conclusion :
- Récapituler les 4 séances rapidement
- Insister sur la progression : syntaxe → POO → fonctionnel → monde réel
- Encourager les étudiants à explorer le TP Bonus (Questions 10-12)
- Rappeler les ressources : documentation officielle Java, Stack Overflow, supports de cours
-->
