# Conventions de rédaction des supports

À lire avec `cours/cadrage.md` (le *quoi*). Ce fichier décrit le *comment*.

## Fichiers

| Fichier | Rôle |
|---|---|
| `cours/slides/seanceN.md` | Présentation Marp, projetée en cours |
| `cours/supports/seanceN.md` | Support détaillé, partagé avec les étudiants (HTML et PDF) |
| `cours/modele/FoodFastModel.java` | Modèle FoodFast de référence : **les supports reprennent ces noms et ces signatures** |
| `cours/theme/foodfast.css` | Thème Marp (ne pas surcharger avec du CSS dans les slides) |

Outils (dans `tools/`, après `npm install`) :
- `npm run check` : détecte les slides qui débordent. **0 débordement obligatoire.**
- `npm run check-java` : compile et exécute les exemples Java marqués `// Fichier :`. **0 échec obligatoire.**
- `npm run build` : génère `outputs/slides` et `outputs/supports` (HTML et PDF).

## Style commun

- Français, **vouvoiement**. Ton direct, phrases courtes.
- Structure de chaque notion : **Pourquoi ?** (le problème) → **Comment ?** (la solution Java) → **Exemple concret** (FoodFast) → **Piège** (❌ puis ✅).
- Badges de niveau dans les titres : 🟢 Socle, 🔵 Approfondissement, 🟣 Bonus, 🔒 Sécurité.
- ❌ et ✅ pour opposer le mauvais et le bon code.
- Java 25, uniquement des fonctionnalités **finales**. Pas de preview, pas d'API obsolète (`Date`, `Vector`, `Hashtable`, `StringBuffer`, sérialisation comme bonne pratique, `printStackTrace`).
- Modèle FoodFast : reprendre `Dish` (record), `DishSize`, `OrderStatus`, `User`/`Customer`/`Courier`, `Order`, `Discount`, `DeliveryPlatform` tels que définis dans `cours/modele/FoodFastModel.java`. Une séance peut ajouter des éléments (exceptions métier, repository JDBC…) sans contredire le modèle.
- Montants : toujours `BigDecimal`, construit depuis une `String`. Comparaison avec `compareTo`.

## Slides (`cours/slides/seanceN.md`)

En-tête obligatoire :

```markdown
---
marp: true
theme: foodfast
paginate: true
title: "Java — Séance N : Titre"
---
```

Règles :
- **Une idée par slide.** Au plus 6 puces ou 14 lignes de code, jamais les deux à la fois.
- **Lignes de code de 75 caractères au plus.** On retire les imports et le code de démarrage (ils sont dans le support) ; un commentaire `// ...` signale ce qui a été coupé.
- Le code d'une slide peut être un extrait, mais **sa version complète et compilée existe dans le support**.
- Titres en `##`. Slide de titre : `<!-- _class: lead -->`. Slide de transition entre parties : `<!-- _class: section -->`.
- Message clé : une citation `> …` (encadré orange).
- Deux colonnes (❌ / ✅) : `<div class="cols"><div>…</div><div>…</div></div>`, avec une ligne vide avant et après chaque bloc markdown à l'intérieur des `<div>`.
- Renvoi au support, en bas de slide : `<div class="support">📖 Support §3.2 — Les Map</div>`.
- Une classe plus dense, `<!-- _class: small -->`, existe pour les cas exceptionnels. **Ne pas l'utiliser pour « faire rentrer » une slide trop chargée : la découper.**
- **Notes présentateur** dans un commentaire HTML en fin de slide : durée, message à faire passer, question à poser à la salle, démo à faire.
  ```markdown
  <!--
  Durée : 3 min
  Question à la salle : que vaut 0.1 + 0.2 ?
  Démo : jshell
  -->
  ```
- **Interactivité** : au moins 2 slides « 🤔 Prédisez la sortie » par séance, et au moins une démo live annoncée en notes.
- Budget : **60 à 75 min de cours**, soit environ **30 à 40 slides** (titre et transitions compris). Les notes donnent une durée par slide, et leur total doit tenir dans le budget.
- Ordre de la séance : slide de titre → objectifs (les AA visés) → parties → récapitulatif « ce qu'il faut retenir » → slide « Pour aller plus loin » qui pointe vers le support.

## Support détaillé (`cours/supports/seanceN.md`)

Markdown standard (pas de Marp, pas d'en-tête YAML). Le build génère un sommaire automatique à partir des titres `##` et `###`.

Structure :

```markdown
# Séance N — Titre

> **Objectifs** : AA… (reprendre le cadrage)
> **Prérequis** : …
> **Comment lire ce support** : 🟢 indispensable · 🔵 pour approfondir · 🟣 bonus · 🔒 sécurité

## 1. Partie
### 1.1 🟢 Notion
…
## N. Ce qu'il faut retenir
## N+1. Pour aller plus loin
(ressources sûres : dev.java, docs.oracle.com/en/java/javase/25, openjdk.org/jeps, OWASP, SEI CERT Oracle Coding Standard for Java)
```

- Numérotation `## 1.` / `### 1.2` **stable** : les slides y renvoient (`📖 Support §1.2`).
- Tout ce qui a été coupé des slides pour tenir le temps (détails, cas limites, 🔵, 🟣) vit ici.
- Chaque notion 🟢 a au moins **un exemple complet exécutable** et **un piège ❌ → ✅**.
- Encadrés sécurité : `> 🔒 **Sécurité** — …`
- Liens vers la doc officielle Java 25 pour les API importantes.
- Pas de limite de longueur stricte, mais viser la clarté (environ 600 à 1200 lignes par séance).

## Code Java vérifiable

Tout bloc ```` ```java ```` dont la première ligne est `// Fichier : Nom.java` est **compilé et exécuté** par `npm run check-java` (JDK 25, classpath = H2 et JUnit 6).

- Exemple autonome : un *compact source file* (`void main() { … }`) ou une classe avec `main`. Toutes les classes nécessaires dans le même bloc (plusieurs classes non publiques par fichier, c'est permis).
- Options sur la ligne d'en-tête :
  - `// Fichier : OrderTest.java (avec Order.java, Dish.java)` : compile avec d'autres blocs du même document.
  - `// Fichier : Crash.java (exception attendue)` : la démo doit s'arrêter sur une exception.
  - `// Fichier : Repository.java (compilation seule)` : pas de `main`, on vérifie seulement que ça compile.
- Une classe nommée `…Test` est exécutée avec JUnit 6 (`org.junit.jupiter.api.*`).
- Pas de lecture clavier dans les exemples vérifiés (stdin vide) : utilisez des valeurs en dur.
- Une démo de race condition doit **vraiment** montrer le problème : assez de threads et d'itérations pour qu'on perde des incréments à coup sûr.
- **Dans le support, tous les exemples complets doivent porter l'en-tête `// Fichier :`.** Les extraits courts sans en-tête sont tolérés pour illustrer une ligne ou un piège.
- Dans les slides, l'en-tête est facultatif (on montre souvent des extraits).
