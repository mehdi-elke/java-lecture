---
marp: true
theme: foodfast
paginate: true
title: "Java — Séance 1 : Les bases du langage"
---

<!-- _class: lead -->
<!-- _paginate: false -->

# Java — Séance 1
## Les bases du langage

Du premier `void main()` au premier test JUnit

<!--
Durée : 1 min
Accueil. Présentez le fil rouge du module : FoodFast, une plateforme de livraison de repas.
Rappelez que le support détaillé est partagé : les slides ne contiennent pas tout.
-->

---

## Le module : 4 séances de 4 heures

| Séance | Thème |
|---|---|
| **1 — aujourd'hui** | Les bases du langage |
| 2 — dans un mois | Du code aux objets |
| 3 | Robuste et persistant |
| 4 | Plusieurs clients, SQL et sécurité 🔒 |

Chaque séance : **cours court** entrecoupé d'**ateliers** 🛠️ sur votre machine, puis **TP en classe**.
Le TP, fil rouge des 4 séances : écrire **votre propre serveur compatible Redis**.

<!--
Durée : 1 min (présentation personnelle comprise)
Se présenter en une phrase : parcours, ce qu'on fait au quotidien avec Java.
-->

---

## Comment vous êtes évalués

**Plusieurs notes courtes, pas d'examen final.** On note surtout ce que vous savez **expliquer**.

| Quand | Quoi |
|---|---|
| Fin de séance 1 | QCM **diagnostique**, non noté |
| Avant la séance 2 | TP rendu sur GitHub : fait, et `mvn test` passe |
| Séances 2, 3, 4 | QCM de 10 min sur la séance précédente |
| Séance 2 | **3 min** avec moi : expliquer et modifier votre code du TP |
| Fin des séances 2 et 4 | TP rendu sur GitHub, comparé à Valkey |
| Séance 4 | **3 min** avec moi : expliquer une correction de sécurité |

<!--
Durée : 2 min
Poids proposés (prive/tp-miniredis/README.md §5, à valider avant d'annoncer des chiffres) : rendu S1 5 %, vérification S2 10 %, QCM 3 × 10 %, jalon S2 20 %, jalon S4 25 %, oral S4 10 %.
Message : le code rendu compte peu en lui-même. Ce qui compte, c'est ce que vous savez en faire devant moi.
-->

---

## Les règles du jeu

**Vous êtes adultes, je vous traite en adultes.**
- Vous gérez votre rythme : 🟢 pour tous, 🔵 🟣 pour aller plus loin.
- Vous êtes autonomes : vous cherchez, puis vous demandez **quand vous bloquez**.

**L'IA est autorisée. Mais :**
- **Vous êtes évalués sans elle** : QCM, et 3 minutes pour expliquer et modifier **votre** code.
- **Cherchez 15 min seul** avant de demander. Demandez une **explication**, pas du code.
- **Valkey et vos tests ont le dernier mot**, pas l'IA. Elle oublie souvent les cas limites.
- **Pas de code que vous ne savez pas expliquer** dans votre dépôt.

<!--
Durée : 2 min, puis questions
« Je ne surveille pas vos écrans. Je vous demanderai simplement d'expliquer et de modifier votre code : si c'est l'IA qui a appris, ce n'est pas vous. »
Pourquoi chercher seul d'abord : c'est quand on bloque qu'on apprend. Une réponse immédiate donne un code qui marche, pas la compréhension.
Pour les profils cyber : relire du code qu'on n'a pas écrit et y trouver la faille, c'est le métier.
Les supports complets sont partagés à la fin de la séance.
Terminer par : « Est-ce que c'est clair pour tout le monde ? Des questions avant de commencer ? » Attendre vraiment la réponse.
-->

---

## Objectifs de la séance

**AA1** — Écrire, compiler et exécuter un programme Java, puis le construire et le tester avec Maven et JUnit.

0. 🛠️ Préparer son poste : Java 25, IntelliJ, débogueur
1. Du code source à la JVM : votre premier programme
2. Variables, types et opérateurs
3. Conditions, boucles, tableaux et méthodes
4. Chaînes de caractères et validation des entrées 🔒
5. Maven et un premier test JUnit

<div class="support">📖 Support détaillé — Séance 1</div>

<!--
Durée : 2 min
Message : à la fin de la séance, vous savez écrire une petite fonction métier ET la tester.
Pour ceux qui connaissent déjà Java : le support contient une section 🟣 « Défis » (Luhn, mot de passe, César…). Vous pouvez l'attaquer dès maintenant.
Question à la salle : qui a déjà écrit du Java ? du C ? du Python ? (ajuster le rythme)
-->

---

<!-- _class: section -->

# 0. 🛠️ Préparer son poste

---

## 🛠️ Installer Java 25 et Maven avec SDKMAN

```text
$ curl -s "https://get.sdkman.io" | bash
$ sdk list java                 → repérer Temurin 25, colonne Identifier
$ sdk install java 25.0.5.0+8-tem
$ sdk install maven
$ java -version                 → openjdk version "25…"
$ mvn -v                        → … Java version: 25…
```

- macOS, Linux et **WSL**. Sans droits administrateur.
- Windows sans WSL : IntelliJ télécharge le JDK (*Download JDK…*).
- `sdk use java …` change de version dans ce terminal seulement.

<div class="support">📖 Support §0.1 et §0.2 — Installer Java et Maven</div>

<!--
Durée : 5 min (si les postes ne sont pas prêts)
OPTIONNEL — variable d'ajustement. Sauter si l'installation a été demandée avant la séance ; vérifier seulement java -version et mvn -v à main levée.
Démo : sdk list java, montrer la colonne Identifier. Le numéro change à chaque mise à jour.
🔒 curl | bash : on fait confiance au site. Le dire aux profils cyber, sans s'y attarder.
-->

---

## 🛠️ IntelliJ : un premier projet en 2 minutes

| *New Project* | Valeur |
|---|---|
| Build system | **Maven** |
| JDK | **25** (sinon *Add JDK from disk* ou *Download JDK*) |
| Add sample code | coché |
| GroupId | `fr.foodfast` |

- Code dans `src/main/java`, tests dans `src/test/java`.
- ▶ dans la marge à côté de `main` : *Run*. Sortie en bas.
- Taper `main` + Tab, `sout` + Tab.

<div class="support">📖 Support §0.3 — IntelliJ IDEA</div>

<!--
Durée : 5 min
OPTIONNEL — variable d'ajustement. Démo live : créer le projet, lancer Main, montrer la fenêtre Run.
Montrer Alt+Entrée sur une erreur soulignée en rouge (§0.5).
-->

---

## 🛠️ Le débogueur : voir le programme s'exécuter

```java
static int totalCents(int[] pricesCents) {
    int total = 0;                              // ● point d'arrêt
    for (int i = 1; i < pricesCents.length; i++) {
        total = total + pricesCents[i];
    }
    return total;                    // {1250, 450, 800} → 1250 ?!
}
```

1. Clic dans la marge : **point d'arrêt**. 2. ▶ → **Debug**.
3. **F8** : ligne suivante. 4. Regarder `i` et `total`. 5. **F9** : reprendre.

<div class="support">📖 Support §0.4 — Le débogueur</div>

<!--
Durée : 5 min
OPTIONNEL — variable d'ajustement. Mais fortement conseillé : c'est l'outil qui débloque les débutants en TP.
Démo live : poser le point d'arrêt, F8, faire constater à la salle que i commence à 1.
Question à la salle : où est le bug ? (réponse : i = 1, la case 0 est oubliée)
-->

---

<!-- _class: section -->

# 1. Du code source à l'exécution

---

## Le chemin d'un programme Java

```text
  Hello.java  ──javac──→  Hello.class  ──java──→   JVM
  (source,                (bytecode,              (exécute sur
   lisible)                portable)               Windows, Linux, macOS)
```

- **`javac`** : le compilateur. Il vérifie le code et produit du **bytecode**.
- **Bytecode** : un langage intermédiaire, le même sur toutes les machines.
- **JVM** (*Java Virtual Machine*) : exécute le bytecode. Elle l'interprète, puis compile à la volée (**JIT**) les parties souvent utilisées.
- La JVM gère la mémoire : le **ramasse-miettes** (*GC*) libère les objets inutilisés.
- On installe le **JDK 25** (LTS) : `javac`, `java`, `jshell`…

<div class="support">📖 Support §1.1 — JDK, bytecode et JVM</div>

<!--
Durée : 2 min
Message : « compilé une fois, exécuté partout ». Java n'est ni purement compilé (comme C) ni purement interprété (comme Python).
JDK = Java Development Kit ; Java 25 est une LTS (Long-Term Support), comme 17 et 21. Vérification en direct : java -version doit afficher 25. jshell : console interactive (support §1.5).
Pour les profils cyber : pas d'arithmétique de pointeurs, mémoire gérée, bytecode vérifié au chargement. Toute une classe de failles C (buffer overflow) disparaît.
-->

---

## Votre premier programme (Java 25)

```java
// Fichier Hello.java
void main() {
    IO.println("Bienvenue chez FoodFast !");
}
```

```text
$ javac Hello.java     → produit Hello.class
$ java Hello           → Bienvenue chez FoodFast !
$ java Hello.java      → raccourci : compile en mémoire et exécute
```

- **Compact source file** (JEP 512, final en Java 25) : pas de classe à écrire.
- `IO.println` affiche une ligne. `IO` est une classe de `java.lang` : aucun import.

<div class="support">📖 Support §1.2 — Le compact source file</div>

<!--
Durée : 3 min
Démo live : écrire Hello.java dans un éditeur, javac, ls (montrer le .class), java Hello, puis java Hello.java.
Faire volontairement une erreur (oublier le ;) pour montrer un message du compilateur : apprendre à LIRE l'erreur (fichier:ligne).
-->

---

## La forme classique : une classe et `main`

```java
public class FoodFastApp {
    public static void main(String[] args) {
        System.out.println("Bienvenue chez FoodFast !");
    }
}
```

| Mot | Sens |
|---|---|
| `public class FoodFastApp` | une classe nommée, dans `FoodFastApp.java` |
| `static` | méthode de la classe, appelable sans objet |
| `void` | ne renvoie rien |
| `String[] args` | arguments de la ligne de commande |

<div class="support">📖 Support §1.3 — La forme classique</div>

<!--
Durée : 2,5 min
Message : c'est la forme que vous verrez dans 99 % du code existant, des tutoriels et des projets. Le nom du fichier DOIT être celui de la classe publique.
System.out.println et IO.println font la même chose ici.
-->

---

## Pourquoi une classe nommée dans un projet ?

<div class="cols"><div>

**Compact source file**

- classe **implicite**, sans nom
- aucun autre code ne peut l'appeler
- donc **impossible à tester** avec JUnit

</div><div>

**Classe nommée**

- `OrderCalculator.totalCents(...)`
- appelable depuis une autre classe
- **testable**, rangée dans un projet Maven

</div></div>

> Le *compact source file* sert à apprendre et à écrire des scripts. Dès qu'on veut tester, on écrit une **classe nommée**.

<div class="support">📖 Support §1.4 — Pourquoi la forme classe</div>

<!--
Durée : 2 min
Transition : aujourd'hui, les exemples sont en compact source file. À la fin de la séance, on les range dans une classe pour les tester avec Maven.
-->

---

## 🛠️ Lire une erreur : où, puis quoi

```text
Hello.java:2: error: cannot find symbol
    IO.printn("Bienvenue chez FoodFast !");
      ^
  symbol:   method printn(String)
  location: class IO
```

- **Où** : fichier `Hello.java`, ligne `2`, colonne du `^`.
- **Quoi** : `cannot find symbol`, un nom inconnu : `printn`.
- Corriger la **première** erreur, puis recompiler.

<div class="support">📖 Support annexe B — Les erreurs les plus fréquentes</div>

<!--
Durée : 2 min
OPTIONNEL — variable d'ajustement. Utile si beaucoup de débutants. cannot find symbol est l'erreur la plus fréquente : faute de frappe, variable hors de portée, import manquant.
L'annexe B donne les 12 erreurs de compilation et les 6 erreurs d'exécution les plus courantes, avec la correction.
-->

---

## 🛠️ À vous — Partie 1 (15 à 20 min)

1. **Compiler à la main** `Hello.java`, puis provoquer trois erreurs et lire les messages.
2. **Dans IntelliJ** : une classe `Welcome` qui accueille le restaurant passé en argument.
3. 🔵 `javap -c Hello` : lire le bytecode.

> Le but n'est pas d'avoir juste : c'est d'avoir **tapé, lancé et lu**.

<div class="support">📖 Support §1.6 — À vous</div>

<!--
Durée : 15 à 20 min
OPTIONNEL — variable d'ajustement. À placer ici, ou à garder pour le début du TP selon le rythme.
Passer dans les rangs : vérifier que chacun a bien un java -version en 25 et un programme qui tourne dans IntelliJ.
Les avancés : partie 6 du support (§6.7 Java moderne, défis).
-->

---

<!-- _class: section -->

# 2. Variables, types et opérateurs

---

## Les types primitifs utiles

| Type | Contient | Exemple FoodFast |
|---|---|---|
| `int` | entier sur 32 bits (±2,1 milliards) | `int quantity = 3;` |
| `long` | entier sur 64 bits | `long orderCount = 12_000_000_000L;` |
| `double` | nombre à virgule (approché) | `double rating = 4.5;` |
| `boolean` | `true` ou `false` | `boolean delivered = false;` |
| `char` | un caractère Unicode | `char size = 'M';` |

> 💶 Montants **en centimes** dans un `int` : `int priceCents = 1250;` (12,50 €). Pas de `double` pour l'argent ; `BigDecimal` arrive en séance 2.

<div class="support">📖 Support §2.1 — Types primitifs</div>

<!--
Durée : 2,5 min
Existent aussi : byte, short, float (rares dans du code métier).
Question à la salle : pourquoi pas de double pour l'argent ? (réponse : 0.1 + 0.2 = 0.30000000000000004, on le démontre en S2)
Une variable se déclare avec un type, et Java vérifie ce type à la compilation : c'est le typage statique.
-->

---

## Types références : `String` et `null`

```java
int quantity = 3;              // primitif : la variable contient 3
String dish = "Margherita";    // référence : la variable désigne
                               // un objet String en mémoire
String coupon = null;          // null : ne désigne aucun objet
IO.println(dish.length());     // 10 : un objet a des méthodes
IO.println(coupon.length());   // ❌ NullPointerException
```

- Primitif : minuscule (`int`), pas de méthode, jamais `null`.
- Référence : majuscule (`String`), des méthodes, peut valoir `null`.

<div class="support">📖 Support §2.2 — Types références et null</div>

<!--
Durée : 2 min
Message : appeler une méthode sur null = NullPointerException, l'erreur la plus fréquente en Java.
Dessiner au tableau la pile (variables locales) et le tas (objets) : le schéma est dans le support.
-->

---

## `var` : laisser le compilateur déduire le type

```java
var quantity = 3;              // int
var dish = "Margherita";       // String
quantity = "trois";            // ❌ ne compile pas : quantity est un int
var coupon;                    // ❌ ne compile pas : pas de valeur
var nothing = null;            // ❌ ne compile pas : type inconnu
```

- `var` **n'est pas** un type dynamique : le type est fixé à la compilation.
- Réservé aux **variables locales** initialisées dans la même instruction.
- À utiliser quand le type est évident à la lecture.

<div class="support">📖 Support §2.3 — var</div>

<!--
Durée : 2 min
Pour les étudiants venant de Python ou JavaScript : Java reste typé statiquement, var évite seulement de répéter le type.
-->

---

## Les opérateurs du quotidien

| Catégorie | Opérateurs | Exemple |
|---|---|---|
| Arithmétique | `+ - * / %` | `total = price * quantity;` |
| Affectation | `= += -= ++ --` | `total += fee;` `count++;` |
| Comparaison | `== != < <= > >=` | `quantity <= 20` |
| Logique | `&& \|\| !` | `qty >= 1 && qty <= 20` |
| Conversion | `(type)` | `(double) cents / 100` |

> `&&` et `||` sont **paresseux** : `s != null && s.isBlank()` ne lève jamais de `NullPointerException`.

<div class="support">📖 Support §2.4 — Opérateurs</div>

<!--
Durée : 2 min
Règle de priorité à retenir : * / % avant + -. Dans le doute : des parenthèses.
% (modulo) : reste de la division. Utile pour pair/impair, FizzBuzz, les centimes (cents % 100).
-->

---

## 🤔 Prédisez la sortie

```java
IO.println(7 / 2);
IO.println(7 % 2);
IO.println((double) 7 / 2);
IO.println("Total : " + 1 + 2);
IO.println(1 + 2 + " €");
```

<!--
Durée : 2,5 min
Laisser 1 minute de réflexion, vote à main levée, puis démo dans jshell.
Réponses : 3 (division entière entre deux int), 1, 3.5, "Total : 12" (évaluation de gauche à droite : String + 1 → "Total : 1", puis + 2), "3 €".
Message : la division entière TRONQUE. Et dans un ticket, mettez des parenthèses : "Total : " + (1 + 2).
-->

---

## 🤔 Prédisez la sortie : `==` sur des `String`

```java
String code = "PROMO10";
String typed = "promo10".toUpperCase();

IO.println(typed);
IO.println(typed == code);
IO.println(typed.equals(code));
```

<!--
Durée : 2 min
Réponses : PROMO10, false, true.
Explication : == compare les RÉFÉRENCES (est-ce le même objet en mémoire ?). toUpperCase crée un nouvel objet. equals compare le CONTENU.
Piège : == « marche parfois » avec des littéraux (le compilateur réutilise le même objet). C'est encore pire : le bug n'apparaît qu'en production, avec une saisie utilisateur.
-->

---

## `==` contre `equals`

<div class="cols"><div>

❌ Compare les **références**

```java
if (typed == code) {
    applyDiscount();
}
```

</div><div>

✅ Compare le **contenu**

```java
if (code.equals(typed)) {
    applyDiscount();
}
```

</div></div>

- `==` pour les primitifs (`int`, `char`, `boolean`…).
- `equals` pour les objets (`String`…). `equalsIgnoreCase` ignore la casse.
- Constante à gauche : `"PROMO10".equals(typed)` ne plante pas si `typed` vaut `null`.

<div class="support">📖 Support §2.5 — == contre equals</div>

<!--
Durée : 2 min
Message : pour les objets, toujours equals. On reverra equals en détail en séance 2 (pour nos propres classes).
-->

---

## 🔒 Prédisez la sortie : le débordement d'entier

```java
int priceCents = 1250;           // une pizza à 12,50 €
int quantity = 2_000_000;        // saisie malveillante
int total = priceCents * quantity;
IO.println(total);
```

- Un `int` va de −2 147 483 648 à **2 147 483 647**.
- Au-delà, Java ne signale **rien** : le calcul « fait le tour ».

<!--
Durée : 2 min
Réponse : -1794967296. Un total NÉGATIF.
Question à la salle : que se passe-t-il si le système de paiement accepte ce total ? (remboursement au lieu d'un débit, commande gratuite…)
Cas réels : CWE-190 (Integer Overflow), classique des failles de paniers e-commerce.
-->

---

## 🔒 Débordement : détecter au lieu de subir

<div class="cols"><div>

❌ Débordement silencieux

```java
int total = price * qty;
total = total + fee;
```

</div><div>

✅ Exception si ça déborde

```java
int total = Math.multiplyExact(
        price, qty);
total = Math.addExact(total, fee);
```

</div></div>

- `Math.addExact`, `subtractExact`, `multiplyExact` lèvent une `ArithmeticException`.
- Le programme s'arrête au lieu de continuer avec un montant faux.
- Mieux encore : **valider la quantité** avant le calcul (§4.3).

<div class="support">📖 Support §2.6 — Débordement d'entier</div>

<!--
Durée : 2 min
Démo live dans jshell : Math.multiplyExact(1250, 2_000_000) → ArithmeticException: integer overflow.
Message : un arrêt franc vaut mieux qu’un résultat faux. On verra en partie 4 comment intercepter l’exception avec un try / catch minimal (détails en séance 3).
-->

---

## 🛠️ À vous — Partie 2 (20 à 30 min)

1. **jshell** : prédire puis vérifier `7 / 2`, `"Total : " + 1 + 2`, `0.1 + 0.2`…
2. **Une ligne de ticket** : `2 x Margherita = 25,00 €`, avec `formatEuros(int cents)`.
3. **Au débogueur** : regarder un compteur `int` devenir négatif, puis `Math.addExact`.

> Prédire **avant** d'exécuter : c'est là qu'on apprend.

<div class="support">📖 Support §2.8 — À vous</div>

<!--
Durée : 20 à 30 min
OPTIONNEL — variable d'ajustement. L'exercice 1 peut se faire en 5 min collectivement, à main levée, si le temps manque.
-->

---

<!-- _class: section -->

# 3. Conditions, boucles, tableaux, méthodes

---

## `if` / `else` : frais de livraison

```java
int subtotalCents = 2350;
int feeCents;

if (subtotalCents >= 3000) {
    feeCents = 0;                  // livraison offerte
} else if (subtotalCents >= 1500) {
    feeCents = 199;
} else {
    feeCents = 399;
}
IO.println("Frais : " + feeCents + " centimes");
```

- Accolades **toujours**, même pour une seule ligne.

<div class="support">📖 Support §3.1 — if / else</div>

<!--
Durée : 2 min
Question à la salle : que vaut feeCents ici ? (199)
Les conditions sont testées dans l'ordre : la première vraie gagne.
Le compilateur vérifie que feeCents est affecté dans toutes les branches : retirez le else, ça ne compile plus.
-->

---

## `switch` en expression

```java
String zone = "PERIPHERIE";

int feeCents = switch (zone) {
    case "CENTRE" -> 199;
    case "PERIPHERIE", "BANLIEUE" -> 399;
    default -> 599;
};
IO.println(feeCents);          // 399
```

- `switch` **renvoie une valeur** ; flèche `->` : pas de `break` à oublier.
- Plusieurs valeurs par `case` ; `default` obligatoire pour une `String`.

<div class="support">📖 Support §3.2 — switch en expression</div>

<!--
Durée : 3 min
Si vous voyez un ancien switch avec case X: et break, c'est la forme d'avant Java 14 : oublier un break fait « tomber » dans le case suivant.
En séance 2, on fera un switch sur un enum (OrderStatus) : là, le compilateur vérifie qu'aucun cas n'est oublié.
-->

---

## Boucles `for` et `while`

```java
// for : on connaît le nombre de tours
for (int table = 1; table <= 3; table++) {
    IO.println("Table " + table + " servie");
}

// while : on répète tant qu'une condition est vraie
int budgetCents = 3000;
int pizzas = 0;
while (budgetCents >= 1250) {
    budgetCents -= 1250;
    pizzas++;
}
IO.println(pizzas + " pizzas, reste " + budgetCents);  // 2, reste 500
```

<div class="support">📖 Support §3.3 — Boucles</div>

<!--
Durée : 2 min
for : initialisation ; condition ; incrément.
while : attention à la boucle infinie si la condition ne devient jamais fausse.
break sort de la boucle, continue passe au tour suivant (détails dans le support).
-->

---

## Tableaux : le total d'une commande

```java
int[] pricesCents = {1250, 990, 350};   // pizza, salade, boisson
int[] quantities  = {2, 1, 3};

int totalCents = 0;
for (int i = 0; i < pricesCents.length; i++) {
    totalCents += pricesCents[i] * quantities[i];
}
IO.println(totalCents);                  // 4540

for (int price : pricesCents) {          // for-each : lecture seule
    IO.println(price);
}
```

- Taille **fixe**, indices de `0` à `length - 1`.

<div class="support">📖 Support §3.4 — Tableaux</div>

<!--
Durée : 2,5 min
Piège classique : i <= pricesCents.length → ArrayIndexOutOfBoundsException au dernier tour.
Piège d'affichage : IO.println(pricesCents) affiche [I@56a6d5a6 ; utilisez Arrays.toString(pricesCents).
Taille fixe = limite : en séance 2, on passera à List (taille variable).
-->

---

## Méthodes `static` : nommer un calcul

```java
static int totalCents(int[] prices, int[] quantities) {
    int total = 0;
    for (int i = 0; i < prices.length; i++) {
        total += prices[i] * quantities[i];
    }
    return total;
}

void main() {
    int t = totalCents(new int[] {1250, 350}, new int[] {2, 1});
    IO.println(t);                                    // 2850
}
```

- `static` + type de retour + nom + paramètres. `return` renvoie le résultat.

<div class="support">📖 Support §3.5 — Méthodes static</div>

<!--
Durée : 2,5 min
Décomposez la signature au tableau : static (pas besoin d'objet), int (type renvoyé), totalCents (nom en lowerCamelCase), paramètres typés.
Une méthode qui ne renvoie rien : void, pas de return obligatoire.
Pourquoi des méthodes ? Réutiliser, nommer une intention, et surtout TESTER (partie 5).
-->

---

## Composer des méthodes : appliquer une remise

```java
static int applyDiscount(int totalCents, int percent) {
    if (percent < 0 || percent > 100) {
        throw new IllegalArgumentException(
                "Remise invalide : " + percent);
    }
    int discount = Math.multiplyExact(totalCents, percent) / 100;
    return totalCents - discount;
}
// applyDiscount(2850, 10)  → 2565
// applyDiscount(1999, 15)  → 1700   (299,85 tronqué à 299)
// applyDiscount(2850, 150) → IllegalArgumentException
```

- `throw` arrête la méthode avec une erreur explicite (détails en séance 3).

<div class="support">📖 Support §3.5 — Méthodes static</div>

<!--
Durée : 2 min
Message : une méthode vérifie ses paramètres AVANT de calculer. Une remise de 150 % donnerait un total négatif.
Notez la troncature de la division entière : 299,85 centimes deviennent 299. Qui gagne le centime ? En S2, BigDecimal et RoundingMode permettent de choisir l'arrondi.
-->

---

## Aperçu : lire une `List` et une `Map`

```java
List<String> words = List.of("ADD", "Margherita", "2");
words.size();                         // 3
words.getFirst();                     // "ADD"
words.get(1);                         // "Margherita"

Map<String, Integer> stock = new HashMap<>();
stock.put("Margherita", 12);          // ajoute ou remplace
stock.get("Margherita");              // 12
stock.get("Sushi");                   // null : clé absente !
stock.getOrDefault("Sushi", 0);       // 0
stock.containsKey("Sushi");           // false
stock.remove("Margherita");           // 12
```

<div class="support">📖 Support §3.8 — Aperçu : List et Map</div>

<!--
Durée : 2,5 min
Message : juste de quoi LIRE et utiliser du code qui manipule des listes et des dictionnaires. Le détail (interfaces, génériques, equals/hashCode) arrive en séance 2.
List : comme un tableau, mais de taille variable ; indices de 0 à size() - 1 ; for-each fonctionne.
Map : une clé -> une valeur ; un put sur une clé existante remplace la valeur.
Piège : get renvoie null si la clé est absente ; stock.get("Sushi") - 1 lève une NullPointerException.
-->

---

## 🛠️ À vous — Partie 3 (30 à 40 min)

1. `dayLabel(int day)` : le jour de la semaine, avec un `switch`.
2. `sumUpTo(n)` et `sumEvenUpTo(n)`, suivies **au débogueur**.
3. `BasketStats` : maximum, nombre au-dessus d'un seuil, moyenne… et le panier vide ?

> L'exercice 3 resservira au §5.8 : gardez-le.

<div class="support">📖 Support §3.9 — À vous</div>

<!--
Durée : 30 à 40 min
OPTIONNEL — variable d'ajustement. Les exercices 1 et 2 suffisent si le temps manque. Le 3 est réutilisé pour les tests JUnit (§5.8).
Question pour le panier vide : renvoyer 0 ou refuser ? (refuser : 0 serait un mensonge silencieux)
-->

---

<!-- _class: section -->

# 4. Chaînes de caractères et validation

---

## Les méthodes de `String` à connaître

| Méthode | Exemple | Résultat |
|---|---|---|
| `length()` | `"Pizza".length()` | `5` |
| `charAt(i)` | `"Pizza".charAt(0)` | `'P'` |
| `substring(a, b)` | `"order-123".substring(6)` | `"123"` |
| `indexOf(s)` | `"order-123".indexOf("-")` | `5` |
| `toUpperCase()` | `"promo10".toUpperCase()` | `"PROMO10"` |
| `strip()` / `isBlank()` | `"  3 ".strip()` | `"3"` |
| `startsWith(s)` | `"order-123".startsWith("order-")` | `true` |

> Une `String` est **immuable** : ces méthodes renvoient une **nouvelle** chaîne.

<div class="support">📖 Support §4.1 — Méthodes de String</div>

<!--
Durée : 2 min
Piège : code.toUpperCase(); sans récupérer le résultat ne change rien. Il faut écrire code = code.toUpperCase();
Autres méthodes utiles : contains, replace, split, equals, equalsIgnoreCase, String.format.
-->

---

## `StringBuilder` : construire un ticket

```java
static String formatEuros(int cents) {
    return String.format("%d,%02d €", cents / 100, cents % 100);
}

static String ticket(String[] names, int[] pricesCents) {
    StringBuilder sb = new StringBuilder("=== FoodFast ===\n");
    for (int i = 0; i < names.length; i++) {
        sb.append(names[i]).append(" : ")
          .append(formatEuros(pricesCents[i])).append('\n');
    }
    return sb.toString();
}
// ticket({"Margherita", "Tiramisu"}, {1250, 650})
```

<div class="support">📖 Support §4.2 — StringBuilder</div>

<!--
Durée : 2 min
Pourquoi StringBuilder ? Dans une boucle, s = s + x crée une nouvelle String à chaque tour. StringBuilder modifie un tampon unique.
String.format : %d entier, %02d entier sur 2 chiffres complété par des zéros, %s chaîne.
Sortie : Margherita : 12,50 € / Tiramisu : 6,50 €
-->

---

## 🔒 Ne jamais faire confiance à une saisie

Une quantité saisie arrive sous forme de **texte** : `"3"`, `" 3"`, `"abc"`, `"-2"`, `"999999999999"`, `"٣"`…

1. **Présence** : ni `null`, ni vide.
2. **Format** : uniquement des chiffres `0` à `9`, longueur maximale.
3. **Bornes** : une quantité entre 1 et 20.
4. Seulement **ensuite**, convertir et calculer.

> On décrit ce qui est **autorisé** (liste blanche), pas ce qui est interdit.

<div class="support">📖 Support §4.3 — Valider les entrées</div>

<!--
Durée : 2 min
"٣" est le chiffre 3 arabe-indien : Character.isDigit et Integer.parseInt l'acceptent ! D'où la vérification explicite '0'..'9'.
Valider tôt, au plus près de l'entrée. Toute donnée externe est hostile : formulaire, fichier, API, base de données.
Référence : OWASP Input Validation Cheat Sheet.
-->

---

## 🔒 Valider une quantité saisie

```java
static final int MAX_QUANTITY = 20;

static boolean isValidQuantity(String input) {
    if (input == null || input.isBlank() || input.length() > 2) {
        return false;
    }
    for (char c : input.toCharArray()) {
        if (c < '0' || c > '9') {
            return false;
        }
    }
    int quantity = Integer.parseInt(input);
    return quantity >= 1 && quantity <= MAX_QUANTITY;
}
```

<div class="support">📖 Support §4.3 — Valider les entrées</div>

<!--
Durée : 2,5 min
Lire le code ligne par ligne. Question à la salle : que renvoie isValidQuantity(" 3") ? (false : l'espace n'est pas un chiffre ; c'est voulu, on peut faire strip() avant).
static final : une constante, en SCREAMING_SNAKE_CASE.
La longueur maximale (2) empêche aussi le débordement de parseInt.
-->

---

## Convertir un texte en nombre, sans planter

```java
static String addQuantity(long current, String text) {
    try {
        long delta = Long.parseLong(text);
        return "Total : " + Math.addExact(current, delta);
    } catch (NumberFormatException e) {
        return "Erreur : ce n'est pas un nombre entier";
    } catch (ArithmeticException e) {
        return "Erreur : dépassement de capacité";
    }
}
// addQuantity(10, "5")              → Total : 15
// addQuantity(10, "abc")            → Erreur : ce n'est pas un nombre…
// addQuantity(Long.MAX_VALUE, "1")  → Erreur : dépassement de capacité
```

<div class="support">📖 Support §4.6 — Long.parseLong et ses exceptions</div>

<!--
Durée : 3 min
Message : une donnée extérieure est toujours du texte, et la conversion peut échouer. try / catch : si une instruction du try lève l'exception nommée, on saute dans le catch. Le strict minimum aujourd'hui, le détail en séance 3.
🔒 Un programme qui s'arrête sur une entrée invalide, c'est un déni de service gratuit pour un attaquant.
Piège à signaler : parseLong est permissif ("+5", "007", "٣" sont acceptés). Si la spécification est plus stricte, valider le format AVANT (§4.3).
🔵 Dans le support §4.7 : caractères et octets, "é".length() vaut 1 mais 2 octets en UTF-8.
-->

---

## 🛠️ À vous — Partie 4 (25 à 35 min)

1. `normalizePromoCode` : `"  promo10 "` devient `"PROMO10"`, puis on valide.
2. `parseQuantity` avec `try` / `catch`… et le piège du chiffre `"٣"` 🔒
3. Un **ticket complet** avec `StringBuilder`.
4. 🔵 `"Crème brûlée"` : combien de caractères, combien d'octets ?

<div class="support">📖 Support §4.8 — À vous</div>

<!--
Durée : 25 à 35 min
OPTIONNEL — variable d'ajustement. L'exercice 2 est le plus important pour les profils cyber : Integer.parseInt accepte les chiffres Unicode, la liste blanche du §4.3 non.
L'exercice 4 prépare directement le TP (le protocole compte en octets).
-->

---

<!-- _class: section -->

# 5. Maven et JUnit

---

## Maven : construire un projet, pas un fichier

- **Construit** : `mvn test` compile puis lance les tests ; `mvn package` crée le `.jar`.
- **Télécharge les dépendances** (JUnit…) depuis Maven Central.
- **Impose une structure** : tout projet Maven se ressemble.

```text
foodfast/
├── pom.xml                                  ← description du projet
└── src/
    ├── main/java/fr/foodfast/
    │   └── OrderCalculator.java             ← code
    └── test/java/fr/foodfast/
        └── OrderCalculatorTest.java         ← tests
```

<div class="support">📖 Support §5.1, §5.2 et §5.4 — Maven</div>

<!--
Durée : 2 min
Les classes sont rangées dans un package (fr.foodfast) : le fichier commence par package fr.foodfast; et le dossier reflète le nom du package.
Le dossier target/ est généré par Maven : on ne le versionne pas (.gitignore).
Commandes : mvn compile, mvn test, mvn package, mvn clean. Chaque phase exécute les précédentes : mvn package lance aussi les tests, un test rouge bloque la livraison (tableau dans le support §5.4).
Démo live (préparer le projet avant le cours) : mvn test dans un terminal.
Votre IDE (IntelliJ, VS Code) lit le pom.xml et configure le projet tout seul.
-->

---

## `pom.xml` (1/2) : identité et Java 25

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <groupId>fr.foodfast</groupId>
    <artifactId>foodfast</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.release>25</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    <!-- ... dépendances et plugins : slide suivante -->
</project>
```

- `groupId` + `artifactId` + `version` : les coordonnées du projet.

<div class="support">📖 Support §5.3 — Le pom.xml minimal</div>

<!--
Durée : 2 min
maven.compiler.release=25 : compile pour Java 25 ET vérifie qu'on n'utilise pas d'API absente de Java 25.
Ancienne forme (source/target 11) dans de vieux tutoriels : à ne pas copier.
-->

---

## `pom.xml` (2/2) : JUnit et plugins

```xml
<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>6.1.3</version>
        <scope>test</scope>
    </dependency>
</dependencies>
<build><plugins>
    <plugin>
        <artifactId>maven-surefire-plugin</artifactId>
        <version>3.6.0</version>
    </plugin>
</plugins></build>
```

<div class="support">📖 Support §5.3 — POM complet, avec maven-compiler-plugin 3.16.0</div>

<!--
Durée : 2 min
scope test : JUnit sert aux tests, il n'est pas livré avec l'application.
surefire : le plugin qui exécute les tests pendant mvn test (groupId omis : org.apache.maven.plugins par défaut). On épingle sa version pour un build reproductible.
Piège vu dans de vieux supports : junit:junit:5.8.2 n'existe pas. JUnit 5 et 6, c'est org.junit.jupiter.
Versions vérifiées sur Maven Central en octobre 2026 : vérifiez-les en début d'année.
-->

---

## Un premier test JUnit

```java
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class OrderCalculatorTest {
    @Test
    void total_de_deux_pizzas_et_une_boisson() {
        int[] prices = {1250, 350};                        // Given
        int[] quantities = {2, 1};

        int total = OrderCalculator.totalCents(prices, quantities); // When

        assertEquals(2850, total);                         // Then
    }
}
```

<div class="support">📖 Support §5.5 — Premier test JUnit</div>

<!--
Durée : 3 min
@Test : JUnit exécute cette méthode. assertEquals(attendu, obtenu) : l'ordre compte pour le message d'erreur.
Démo live : mvn test (vert), puis casser totalCents (remplacer multiplyExact par addExact), relancer : expected: <2850> but was: <...>. Rouge. Réparer.
Message : le test appelle OrderCalculator.totalCents : il faut une classe NOMMÉE (retour à la partie 1).
Dans le projet, le fichier commence par package fr.foodfast; (coupé ici pour la place).
-->

---

## Écrire de bons tests

- **Given / When / Then** : préparer, agir, vérifier.
- **Un comportement par test**, et un nom qui le décrit.
- Tester le **cas nominal**, les **cas limites** (`0`, `1`, `20`, `21`, tableau vide)…
- … et les **entrées invalides** (`"abc"`, `null`, `"-2"`).

```java
@Test
void une_quantite_de_21_est_refusee() {
    assertFalse(InputValidator.isValidQuantity("21"));
}
```

<div class="support">📖 Support §5.6 — Écrire de bons tests</div>

<!--
Durée : 2 min
Les bugs se cachent aux frontières : 20 accepté, 21 refusé. Testez les deux côtés de chaque borne.
Pour les profils cyber : un test d'entrée invalide est un test de sécurité.
Bonus dans le support : @ParameterizedTest pour tester 10 entrées en un seul test.
-->

---

## 🛠️ À vous — Partie 5 (30 à 40 min)

1. Un projet **Maven** dans IntelliJ, `BasketStats` dans `src/main/java`.
2. **Trois tests** : un cas nominal, un cas limite, une entrée invalide.
3. Lancer `mvn test` **et** ▶▶ dans IntelliJ.
4. **Casser exprès** (`>` devient `>=`) : lire `expected` / `but was`.

> C'est exactement ainsi que commence le TP.

<div class="support">📖 Support §5.8 — À vous</div>

<!--
Durée : 30 à 40 min
OPTIONNEL — variable d'ajustement. Le plus utile des ateliers : à faire en priorité si un seul atelier est possible, éventuellement en début de TP.
Vérifier que chacun voit BUILD SUCCESS en terminal : c'est la preuve que Java 25 + Maven + JUnit fonctionnent.
-->

---

## Ce qu'il faut retenir

- `.java` → **`javac`** → bytecode → **JVM**. Java 25 : `void main()` et `IO.println`.
- Dans un projet : une **classe nommée**, testable.
- Montants en **centimes** (`int`) aujourd'hui, `BigDecimal` en séance 2.
- `String` : **`equals`**, jamais `==`. `StringBuilder` dans les boucles.
- 🔒 Débordement : `Math.addExact` / `multiplyExact`. Entrées : présence, format, bornes.
- `mvn test` + JUnit : **Given / When / Then**, cas limites, entrées invalides.

<!--
Durée : 2 min
Question à la salle : citez une entrée que isValidQuantity doit refuser.
-->

---

## Pour aller plus loin

Dans le **support détaillé** de la séance 1 :

- 🔵 Text blocks, `import module`, surcharge, portée des variables
- 🔵 Tests paramétrés (`@ParameterizedTest`)
- 🟣 **Défis** : algorithme de Luhn, robustesse d'un mot de passe, chiffre de César, FizzBuzz piloté par les tests, `switch` à motifs sur un `record`, code promo avec `SecureRandom`

> Séance 2 : de la donnée à l'objet. Classes, `record`, `enum`, `BigDecimal`, collections.

<div class="support">📖 Support §6 — Défis · §8 — Pour aller plus loin</div>

<!--
Durée : 1 min
Les étudiants avancés : attaquez les défis pendant que les autres terminent les exercices de base.
-->

---

## Pour le TP

Vous allez écrire **votre propre serveur compatible Redis / Valkey**. Aujourd'hui, vous réutiliserez :

- `switch` en expression sur une `String`, méthodes `static`
- `List` et `Map` en lecture (§3.8), `String` et `StringBuilder`
- `Long.parseLong`, `try` / `catch`, `Math.addExact` 🔒
- 🔵 la différence entre caractères et octets (§4.7)
- JUnit : un test par comportement, sans oublier les entrées invalides

> La spécification, c'est le vrai serveur : **« faites comme Valkey »**.

<!--
Durée : 1 min
Transition vers le TP. Le code réseau et le protocole sont fournis : on écrit les commandes.
-->
