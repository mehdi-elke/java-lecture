---
title: Java
description: Support du cours Java débutant
author: Mehdi Elketroussi
keywords: java, maven, build
theme: gaia
size: 16:9
paginate: true
marp: true
---

# Maven

<style>
    .small-text {
        font-size: 12px;
    }

img[alt~="center"] {
  display: block;
  margin: 0 auto;
}
</style>

<!-- @TODO: Style défini sur cette slide afin d'être utilisable dans la présentation, à voir comment externaliser et créer un theme personnel.-->



---
## Qu'est-ce que Maven ?

Maven est un outil de gestion de projet et d'automatisation de la construction pour les projets Java.

 Il est principalement utilisé pour simplifier le processus de construction et de gestion des dépendances, permettant aux développeurs de se concentrer sur le développement de fonctionnalités plutôt que sur les tâches répétitives de configuration.

---

## Pourquoi Utiliser Maven ?

### 1. Gestion des Dépendances
Maven automatise la gestion des bibliothèques et des dépendances nécessaires à votre projet. Grâce à son dépôt central, vous pouvez facilement spécifier les bibliothèques que vous souhaitez utiliser et Maven les téléchargera pour vous.

### 2. Standardisation
Maven impose une structure de projet cohérente qui facilite la compréhension et la maintenance du code. Cela permet aux nouveaux développeurs de s'intégrer rapidement dans le projet.

---

### 3. Automatisation
Maven automatise les tâches courantes, telles que la compilation, le test et le déploiement, réduisant ainsi le risque d'erreurs humaines.

### 4. Intégration Continue
Maven s'intègre facilement avec des outils d'intégration continue comme Jenkins, facilitant le déploiement et les tests automatiques des applications.

---

### 5. Reproductibilité
Maven garantit que votre projet peut être reconstruit de manière cohérente sur différentes machines grâce à la gestion des versions et des dépendances.

---

## Structure d'un Projet Maven

Un projet Maven suit généralement une structure standardisée, qui facilite l'organisation du code source et des ressources :
```console
mon-projet/
├── pom.xml                  # Fichier de configuration du projet
├── src/
│   ├── main/
│   │   ├── java/            # Code source Java
│   │   └── resources/       # Ressources non-Java (configurations, fichiers, etc.)
│   └── test/
│       ├── java/            # Tests unitaires Java
│       └── resources/       # Ressources pour les tests
└── target/                  # Répertoire généré contenant les artefacts construits
```
---
## Détails des Composants

- **`pom.xml`** : Le fichier de configuration principal qui contient des informations sur le projet, ses dépendances, et ses plugins.

- **`src/`** : Le répertoire principal contenant le code source et les ressources.

  - **`main/`** : Contient le code source et les ressources de l'application.

    - **`java/`** : Emplacement pour les fichiers Java de l'application.
    - **`resources/`** : Contient des fichiers de ressources (fichiers de configuration, images, etc.) utilisés par l'application.
---

  - **`test/`** : Contient les tests unitaires et les ressources associées.

    - **`java/`** : Emplacement pour les fichiers Java de tests.
    - **`resources/`** : Contient des fichiers de ressources utilisés pour les tests.

- **`target/`** : Répertoire généré lors de la construction du projet, contenant les artefacts construits (JAR, WAR, etc.).
---
### Le fichier `pom.xml`

Le fichier `pom.xml` (Project Object Model) est le cœur de tout projet Maven. Il contient des informations sur le projet, sa version, ses dépendances et les plugins nécessaires.

---

#### Exemple de `pom.xml`

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>mon-projet</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>junit</groupId>
            <artifactId>junit</artifactId>
            <version>5.8.2</version>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>3.12.0</version>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.8.1</version>
                <configuration>
                    <source>11</source>
                    <target>11</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```
---
### Éléments Clés du pom.xml

- groupId : Identifiant unique pour le groupe d’artefacts.
- artifactId : Identifiant unique pour l’artefact (votre projet).
- version : Version actuelle de votre projet.
- dependencies : Liste des dépendances nécessaires pour le projet
- build : Configuration des plugins utilisés lors de la construction.
---
### Commandes Maven Courantes

- mvn clean : Supprime le répertoire target (où les artefacts sont stockés) pour une construction propre.
- mvn compile : Compile le code source du projet.
- mvn test : Exécute les tests unitaires.
- mvn package : Crée un fichier JAR ou WAR à partir du code compilé.
- mvn install : Installe le package dans le dépôt local pour une utilisation par d’autres projets.
---
### Plugins Maven

Maven prend en charge une large gamme de plugins qui ajoutent des fonctionnalités à votre projet. Quelques plugins courants incluent :

- maven-surefire-plugin : Exécute les tests unitaires.
-	maven-jar-plugin : Crée des fichiers JAR.
- maven-deploy-plugin : Déploie des artefacts vers un serveur distant.
---

Maven est un outil essentiel pour la gestion des projets Java, offrant une gestion efficace des dépendances, une structure standardisée, et une automatisation des processus de construction et de déploiement. Son utilisation permet de simplifier le développement, d’améliorer la productivité et de garantir la qualité du code.


