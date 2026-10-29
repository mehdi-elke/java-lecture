---
marp: true
theme: foodfast
paginate: true
title: "Java — Présentation du module"
---

<!-- _class: lead -->

# Java moderne
## Présentation du module

M1 Informatique · Java 25 · 16h

<!--
Durée : 1 min
-->

---

## L'objectif du module

> À la fin du module, vous saurez écrire en Java moderne une petite application métier **structurée en objets et testée**, et **repérer puis corriger les erreurs classiques de robustesse et de sécurité**.

- Un fil conducteur : **FoodFast**, une plateforme de livraison de repas
- Une version : **Java 25**, la version LTS actuelle
- Un fil rouge : 🔒 **la sécurité**, à chaque séance

<!--
Durée : 2 min
Message : on ne fait pas un catalogue du langage, on construit des réflexes de développeur.
-->

---

## Ce que vous saurez faire

1. Écrire, compiler, exécuter et **tester** un programme Java (Maven, JUnit)
2. **Modéliser** un domaine : classes, `record`, `enum`, encapsulation
3. Utiliser **héritage, interfaces et polymorphisme**
4. Choisir la bonne **collection** (`List`, `Set`, `Map`)
5. Transformer des données avec **lambdas et streams**
6. Gérer les erreurs avec des **exceptions** propres
7. Identifier et corriger une **race condition** et une **injection SQL**

<!--
Durée : 2 min
Ce sont les acquis évalués (AA1 à AA7 du cadrage).
-->

---

## Le programme

| Séance | Thème | Acquis |
|---|---|---|
| 1 | Les bases du langage, Maven et JUnit | 1 |
| 2 | De la donnée à l'objet | 2, 3, 4 |
| 3 | Lambdas, streams et exceptions | 5, 6 |
| 4 | Concurrence et bases de données | 7 |

Chaque séance de 4h : **≈ 1h de cours**, puis **pratique** et **évaluation**.

<!--
Durée : 2 min
-->

---

## Un public varié : trois niveaux

- 🟢 **Socle** : indispensable, évalué. Tout le monde.
- 🔵 **Approfondissement** : pour comprendre plus finement.
- 🟣 **Bonus** : Java moderne, défis pour celles et ceux qui ont déjà codé.
- 🔒 **Sécurité** : les réflexes à avoir dès maintenant.

> Vous découvrez Java ? Visez le 🟢, il suffit.
> Vous êtes en alternance ? Les 🟣 sont pour vous.

<!--
Durée : 2 min
Rassurer les profils cyber débutants, et donner un objectif aux alternants.
-->

---

## Deux supports par séance

<div class="cols">
<div>

### 🖥️ Les slides

- Projetées en cours
- L'essentiel, du code court
- Pour suivre et prendre des notes

</div>
<div>

### 📖 Le support détaillé

- Partagé après chaque séance (HTML, PDF)
- Explications complètes, code complet qui compile
- Approfondissements, bonus, corrigés

</div>
</div>

Chaque slide renvoie à sa section : `📖 Support §2.3`

<!--
Durée : 2 min
-->

---

## Évaluation

- **Contrôle continu** : plusieurs notes réparties sur les 4 séances
- Des QCM courts, des rendus de pratique, un échange oral sur votre code
- Modalités et barème détaillés en séance 1

> L'IA est autorisée pour apprendre. À l'oral, vous devez pouvoir **expliquer et modifier** votre code.

<!--
Durée : 2 min
TODO enseignant : compléter quand le dispositif d'évaluation sera fixé.
-->

---

## À installer avant la séance 1

- **JDK 25** : par exemple Eclipse Temurin (adoptium.net)
- **Un IDE** : IntelliJ IDEA (Community suffit) ou VS Code avec l'extension Java
- **Maven 3.9+** (IntelliJ l'intègre déjà)
- **Git**

Vérification :

```bash
java -version    # doit afficher 25
mvn -version
```

<!--
Durée : 2 min
-->

---

## Ressources sûres

- **dev.java** : tutoriels officiels
- **docs.oracle.com/en/java/javase/25** : la documentation des API
- **openjdk.org/jeps** : les nouveautés du langage
- **OWASP** et **SEI CERT Oracle Coding Standard for Java** : la sécurité

> Méfiez-vous des tutoriels anciens : `Date`, `Vector`, `StringBuffer`… c'est du Java d'il y a 15 ans.

<!--
Durée : 1 min
-->
