# TP ShortLink — Présentation

## Le projet

Vous écrivez **ShortLink**, un raccourcisseur de liens, comme bit.ly. On lui donne une URL longue :

```text
shortlink> add https://foodfast.fr/restaurants/paris/pizzeria-da-mario/menu?promo=AUTOMNE
Lien créé : 4c92
```

Il répond un **code court**. Ce code redonne l'URL, et compte les clics :

```text
shortlink> open 4c92
https://foodfast.fr/restaurants/paris/pizzeria-da-mario/menu?promo=AUTOMNE
shortlink> clicks 4c92
1
```

Le sujet tient en une phrase, et c'est voulu. Il n'y a pas de règle métier à interpréter : chaque comportement attendu est écrit dans un **test fourni**. Votre travail est de faire passer ces tests, puis d'en écrire à vous.

Au fil des séances, ShortLink devient une vraie application :

| Séance | Vous écrivez | Notions |
|---|---|---|
| [1](seance1.md) | Valider les codes et les URL, fabriquer les codes, la console | `String`, boucles, `switch`, méthodes, `Map`, JUnit |
| 2 | Une architecture objet : des liens qui expirent, un dépôt de liens interchangeable | classes, interfaces, `record`, `enum`, collections, `Optional`, `Clock` |
| 3 | Des erreurs propres, des statistiques, la sauvegarde sur disque | exceptions, streams, fichiers, logs |
| 4 | Un vrai serveur web qui redirige, plusieurs clients, une base de données | threads, HTTP, JDBC, injection SQL, déni de service |

Les énoncés des séances 2 à 4 arrivent avant chaque séance.

## Et Mini-Redis ?

ShortLink est le TP du module. Mini-Redis est un **parcours autonome 🟣**, pour ceux qui ont déjà de l'expérience. Les deux couvrent **les mêmes notions** et sont **évalués de la même façon**. Ils diffèrent par la manière de travailler :

| | ShortLink | Mini-Redis |
|---|---|---|
| Ce qu'on vise | faire passer des tests fournis | se comporter exactement comme un vrai serveur (Valkey) |
| Guidage | pas à pas, avec le début du code à chaque exercice | des objectifs, à vous de trouver comment |
| Installation | Java et Maven | Java et Maven (Docker facultatif) |
| Pour qui | tout le monde | vous programmez déjà à l'aise et voulez chercher seuls |

Vous ne savez pas lequel choisir ? Prenez ShortLink. Un TP terminé et bien compris vaut mieux qu'un TP ambitieux à moitié fait. Le choix se fait en séance 1, et il est définitif.

## Récupérer le projet

1. Sur [github.com/mehdi-elke/java-lecture](https://github.com/mehdi-elke/java-lecture) : **Code → Download ZIP**.
2. Dans le ZIP, copiez le dossier `tp/shortlink/starter` où vous voulez, et renommez-le `shortlink`.
3. Ouvrez ce dossier dans IntelliJ (**File → Open**) : il est reconnu comme projet Maven.

## Installation

Java 25 et Maven, comme pour le cours (support de la séance 1, §0). Rien d'autre.

Vérifiez dans un terminal, dans le dossier du projet :

```bash
mvn test
```

Vous devez voir `BUILD SUCCESS` et `Tests run: 41, Failures: 0, Errors: 0, Skipped: 40`. Les 40 tests « skipped » sont ceux des exercices : vous les activerez un par un.

## Ce qui est fourni

| Fichier | Rôle |
|---|---|
| `Main.java` | La console : lit ce que vous tapez, affiche la réponse. **Fourni, rien à changer.** |
| `Shell.java` | Exécute les commandes. `add` est écrite, pour servir de modèle : vous écrivez les autres |
| `Codes.java` | Les codes courts : méthodes à écrire |
| `Urls.java` | Les URL : méthodes à écrire |
| `src/test/java/shortlink/*Test.java` | Un fichier de tests par exercice, désactivé au départ |

Lancer la console : bouton ▶ à côté de `main` dans `Main.java`, ou `mvn -q compile exec:java` dans un terminal.

## La méthode, à chaque exercice

1. Ouvrez le fichier de test de l'exercice, et **supprimez la ligne `@Disabled(...)`**.
2. Lancez ce test (▶ à côté du nom de la classe) : tout est **rouge**. C'est normal, la méthode n'est pas écrite.
3. Écrivez la méthode, et relancez jusqu'à ce que tout soit **vert**.
4. **Ajoutez au moins un test à vous** dans le même fichier, à l'endroit marqué. Un test qui vérifie un cas auquel les tests fournis ne pensent pas.
5. `mvn test`, puis un **commit**.

Ne modifiez pas les tests fournis pour les faire passer : c'est votre code qui doit changer. Les tests de correction sont les mêmes, plus quelques autres.

## Les niveaux

- 🟢 **Socle** : ce que tout le monde doit faire. C'est la base de la note.
- 🔵 **Approfondissement** : une fois le socle terminé.
- 🟣 **Bonus** : pour ceux qui ont de l'avance.
- 🔒 **Sécurité** : les points de sécurité, marqués à chaque niveau.

## Les règles du jeu

- **Les signatures** (noms de classes, de méthodes, paramètres) sont imposées : les tests et la suite du TP en dépendent.
- **Git** : un commit au moins par exercice terminé. Le dépôt est rendu avant la séance 2, puis à la fin des séances 2 et 4.
- **L'IA est autorisée**, mais vous êtes évalués sans elle : vous devrez expliquer et modifier votre code à l'oral, en 3 minutes. Cherchez 15 minutes seul avant de demander, et demandez une explication plutôt que du code.

## Notation

La même que pour Mini-Redis :

- des QCM courts en début de séances 2, 3 et 4 ;
- un rendu avant la séance 2 ([consigne](rendu-seance1.md)), puis deux jalons en fin de séances 2 et 4 (dépôt Git évalué par des tests de correction) ;
- **deux passages de 3 minutes** avec l'enseignant, en séances 2 et 4 : expliquer puis modifier votre propre code, sans IA.
