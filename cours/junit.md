---
title: "Java - Les Tests Unitaires avec JUnit"
description: "Apprendre à écrire des tests unitaires robustes et pertinents"
author: "Mehdi Elketroussi"
theme: gaia
size: 16:9
paginate: true
marp: true
---

# Les Tests Unitaires avec JUnit

Notre filet de sécurité pour un code de qualité.

---

## Agenda

1.  **Le "Pourquoi" :** Pourquoi les tests ne sont pas une option.
2.  **Anatomie d'un Bon Test :** Les qualités indispensables.
3.  **La Structure Universelle :** *Given, When, Then*.
4.  **Mise en Place :** Intégrer JUnit 5 avec Maven.
5.  **Étude de Cas :** Tester une fonction de A à Z.
6.  **Synthèse :** Quoi tester (et ne pas tester).
7.  **La Couverture de Code :** Mesurer l'efficacité de nos tests.

---

## 1. Le "Pourquoi" des Tests

Écrire du code sans tests, c'est naviguer sans boussole. Les tests sont essentiels pour :

- **La Confiance :** S'assurer que le code fait *précisément* ce qu'on attend de lui.
- **La Non-Régression :** Garantir que les modifications futures ne "cassent" pas les fonctionnalités existantes. C'est le principal filet de sécurité d'un projet.
- **La Documentation Vivante :** Un test bien écrit est la meilleure documentation possible. Il montre comment une méthode est censée se comporter avec des exemples concrets.

---

## 2. Anatomie d'un Bon Test (Principes F.I.R.S.T.)

Un test unitaire efficace respecte 5 principes clés :

- **F**ast (Rapide) : Il s'exécute en millisecondes. Une suite de milliers de tests ne doit prendre que quelques secondes.
- **I**ndependent (Indépendant) : Chaque test est autonome. Son succès ou son échec ne doit jamais dépendre d'un autre test.
- **R**epeatable (Reproductible) : Il doit donner le même résultat à chaque exécution, quel que soit l'environnement (votre machine, un serveur, etc.).
- **S**elf-Validating (Auto-validé) : Le test conclut lui-même à un succès ou un échec. Personne ne devrait avoir à lire un fichier de log pour savoir si ça a marché.
- **T**imely (Opportun) : Le test est écrit au bon moment, c'est-à-dire juste avant le code qu'il valide (principe du TDD).

---

## 3. La Structure Universelle : Given, When, Then

C'est la structure qui rend un test lisible et compréhensible par tous. On la nomme aussi **Arrange, Act, Assert (AAA)**.

- **Given (Étant donné / Arrange)**
  On prépare le contexte : création des objets, initialisation des variables. C'est la mise en place.

- **When (Quand / Act)**
  On exécute l'action unique que l'on souhaite tester. C'est le cœur du test.

- **Then (Alors / Assert)**
  On vérifie que le résultat est celui attendu. C'est l'étape de l'**assertion**.

Un test qui suit cette structure raconte une histoire claire.

---

## 4. Mise en Place : JUnit 5 et Maven

Pour utiliser JUnit, on doit l'ajouter comme dépendance dans le fichier `pom.xml` de notre projet Maven.

```xml
<dependencies>
    <!-- L'API pour écrire les tests (@Test, assertEquals, ...) -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-api</artifactId>
        <version>5.10.0</version> <!-- Toujours préférer la dernière version stable -->
        <scope>test</scope> <!-- Crucial: la dépendance n'est utilisée que pour les tests -->
    </dependency>
    
    <!-- Le moteur qui exécute les tests -->
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-engine</artifactId>
        <version>5.10.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

Par convention, Maven cherche les fichiers de test dans le dossier `src/test/java`.

---

## 5. Étude de Cas : Tester la méthode `isLeapYear`

Rappel de la logique : *une année est bissextile si elle est divisible par 4, sauf si elle est divisible par 100 mais pas par 400.*

Comment s'assurer que notre implémentation est correcte ? En testant tous les scénarios !

**Le code à tester (dans `src/main/java`) :**
```java
public class FoodFastUtils {
    public static boolean isLeapYear(int year) {
        if (year <= 0) {
            throw new IllegalArgumentException("L'année doit être positive.");
        }
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
    }
}
```

---

### a) Le Cas Nominal

On teste le comportement le plus courant et attendu.

**Le test (dans `src/test/java`) :**
```java
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

class FoodFastUtilsTest {
    @Test
    void une_annee_divisible_par_4_mais_pas_100_est_bissextile() {
        // Given (Étant donné)
        int year = 2024;

        // When (Quand)
        boolean result = FoodFastUtils.isLeapYear(year);

        // Then (Alors)
        assertTrue(result, "L'année 2024 est divisible par 4, elle devrait être bissextile.");
    }
}
```
> **Bonne pratique :** Le nom du test doit décrire ce qu'il fait. `test1` est un mauvais nom. `une_annee_divisible_par_4_est_bissextile` est un excellent nom.

---

### b) Les Cas aux Limites (Edge Cases)

C'est là que les bugs se cachent. On teste les frontières de la logique.

```java
// ... dans FoodFastUtilsTest.java

@Test
void une_annee_divisible_par_100_mais_pas_400_nest_pas_bissextile() {
    // Given: Un siècle "normal"
    int year = 1900;
    
    // When
    boolean result = FoodFastUtils.isLeapYear(year);
    
    // Then
    assertFalse(result, "1900 est un siècle non bissextile.");
}

@Test
void une_annee_divisible_par_400_est_bissextile() {
    // Given: Un siècle "spécial"
    int year = 2000;
    
    // When
    boolean result = FoodFastUtils.isLeapYear(year);
    
    // Then
    assertTrue(result, "2000 est un siècle bissextile.");
}
```
---

### c) Les Cas d'Erreur (Exceptions)

Que doit faire notre méthode si on lui donne une entrée invalide, comme une année négative ? Elle devrait échouer de manière prévisible, par exemple en levant une `IllegalArgumentException`.

```java
// ... dans FoodFastUtilsTest.java

@Test
void une_annee_negative_doit_lever_une_exception() {
    // Given
    int invalidYear = -4;

    // When & Then
    // On affirme que l'exécution de isLeapYear(-4) DOIT lancer
    // une exception de type IllegalArgumentException.
    // Le test passe si l'exception est levée. Il échoue sinon.
    assertThrows(IllegalArgumentException.class, () -> {
        FoodFastUtils.isLeapYear(invalidYear);
    });
}
```
---

## 6. Synthèse : Quoi Tester ?

**À TESTER SANS HÉSITER :**
- ✅ **La logique métier :** `if/else`, calculs, conditions complexes.
- ✅ **Les cas aux limites :** `0`, `-1`, `null`, listes vides, chaînes de caractères vides. C'est la source n°1 des bugs.
- ✅ **Les cas d'erreur :** La gestion des exceptions est une fonctionnalité en soi.
- ✅ **Les algorithmes** que vous écrivez.

**À NE PAS TESTER (en général) :**
- ❌ **Les librairies externes et le JDK :** Ne testez pas que `java.util.ArrayList` fonctionne. Faites confiance à l'équipe de Java.
- ❌ **Les getters et setters triviaux :** Si une méthode se contente de retourner un champ sans aucune logique, la tester n'apporte rien.
- ❌ **Le code que vous ne possédez pas** ou que vous ne pouvez pas modifier.

---

## 7. La Couverture de Code : Mesurer l'Efficacité de nos Tests

La **couverture de code** est une métrique qui indique la proportion de votre code source qui est exécutée lorsque vos tests sont lancés.

- **Objectif :** Identifier les parties de votre code qui ne sont pas testées.
- **Attention :** Une couverture élevée n'est pas une garantie de qualité des tests ! Un code peut être couvert à 100% mais les tests peuvent être mauvais (ex: ils ne vérifient rien).

---

### a) Types de Couverture

Plusieurs types de couverture existent, les plus courants sont :

- **Couverture de Lignes :** Le pourcentage de lignes de code exécutées.
- **Couverture de Branches (ou Décisions) :** Le pourcentage de branches `if/else`, `switch`, `boucles` qui ont été parcourues. C'est plus précis que la couverture de lignes.

---

### b) Outils et Intégration Maven (JaCoCo)

Pour mesurer la couverture de code en Java, l'outil le plus populaire est **JaCoCo** (Java Code Coverage). Il s'intègre facilement à Maven.

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version> <!-- Vérifier la dernière version -->
            <executions>
                <execution>
                    <goals>
                        <goal>prepare-agent</goal>
                    </goals>
                </execution>
                <execution>
                    <id>report</id>
                    <phase>test</phase>
                    <goals>
                        <goal>report</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```
Après un `mvn clean install`, un rapport HTML détaillé sera généré dans `target/site/jacoco/index.html`.

---

### c) Interpréter la Couverture : Avantages et Limites

**Avantages :**
- **Identification des Zones Mortes :** Révèle les parties du code jamais atteintes par les tests (et donc potentiellement inutilisées ou non testées).
- **Aide à la Prise de Décision :** Guide les efforts de test. Si une logique critique n'est pas couverte, il faut écrire des tests.

**Limites :**
- **Pas un Indicateur de Qualité :** Une couverture à 100% ne signifie pas que le code est bien testé. Les tests peuvent passer sans faire de vérifications pertinentes.
- **Coût :** Viser 100% de couverture de branches peut être très coûteux et peu rentable pour une valeur ajoutée marginale sur certains codes.

---

## Conclusion

- Les tests unitaires sont un **investissement**, pas une perte de temps.
- Ils vous forcent à écrire du code plus **simple**, plus **modulaire** et de **meilleure qualité**.
- La structure **Given/When/Then** rend vos tests clairs et utiles comme documentation.
- Pensez toujours aux **cas limites** et aux **cas d'erreur**, pas seulement au "happy path".
- La **couverture de code** est un outil utile pour *identifier les lacunes*, mais ne remplace jamais la **qualité** des assertions.