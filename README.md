# Java — M1 Informatique

Supports du module Java (Java 25 LTS) : 16 heures, en 4 séances de 4 h. Chaque séance commence par environ 1 h de cours, suivie de TP.

| Séance | Thème | Slides | Support détaillé |
|---|---|---|---|
| 0 | Présentation du module | [PDF](pdf/slides/seance0_syllabus.pdf) · [md](cours/slides/seance0_syllabus.md) | — |
| Avant la 1 | Installer l'environnement | — | [PDF](pdf/supports/avant-seance1.pdf) · [md](cours/supports/avant-seance1.md) |
| 1 | Bases du langage, Maven et JUnit | [PDF](pdf/slides/seance1.pdf) · [md](cours/slides/seance1.md) | [PDF](pdf/supports/seance1.pdf) · [md](cours/supports/seance1.md) |
| 2 | Classes, `record`, `enum`, héritage, interfaces, collections | [PDF](pdf/slides/seance2.pdf) · [md](cours/slides/seance2.md) | [PDF](pdf/supports/seance2.pdf) · [md](cours/supports/seance2.md) |
| 3 | Lambdas, streams, exceptions, fichiers | [PDF](pdf/slides/seance3.pdf) · [md](cours/slides/seance3.md) | [PDF](pdf/supports/seance3.pdf) · [md](cours/supports/seance3.md) |
| 4 | Concurrence, JDBC, sécurité | [PDF](pdf/slides/seance4.pdf) · [md](cours/slides/seance4.md) | [PDF](pdf/supports/seance4.pdf) · [md](cours/supports/seance4.md) |

Le markdown est toujours la version la plus à jour ; les PDF sont régénérés à chaque mise à jour du cours.

## Le dépôt

```
.
├── cours/
│   ├── cadrage.md        # Objectifs, acquis visés, choix de contenu
│   ├── CONVENTIONS.md    # Règles de rédaction des slides, des supports et du code
│   ├── slides/           # Présentations projetées en cours (Marp)
│   ├── supports/         # Supports détaillés, à lire après le cours
│   ├── modele/           # Exemple de référence utilisé dans le cours (FoodFast)
│   └── theme/            # Thème Marp
├── pdf/
│   ├── slides/           # Slides en PDF
│   └── supports/         # Supports détaillés en PDF
├── tp/
│   ├── README.md         # Le TP, et le parcours autonome
│   ├── shortlink/        # Le TP : un raccourcisseur de liens, guidé
│   └── miniredis/        # Parcours autonome : un mini-Redis
├── tools/                # Vérification et génération des supports (Node.js)
└── build_support.sh
```

Deux supports par séance :
* **Slides** (`cours/slides/`) : ce qui est projeté en cours, volontairement léger.
* **Support détaillé** (`cours/supports/`) : les explications complètes, le code complet et les approfondissements.

## Le TP

Tout est expliqué dans [`tp/README.md`](tp/README.md).

**ShortLink** (`tp/shortlink/`), le TP du module : un raccourcisseur de liens, guidé pas à pas. Chaque comportement attendu est décrit par un test fourni. Prérequis : JDK 25 et Maven.

**Mini-Redis** (`tp/miniredis/`), parcours autonome 🟣 pour les expérimentés : vous écrivez en Java un serveur clé-valeur compatible avec le vrai client `valkey-cli`. La règle est simple : votre serveur doit répondre **exactement comme Valkey**. L'outil `tools/Compare.java` compare ses réponses à celles de Valkey et affiche les différences. Prérequis : JDK 25 et Maven (Docker facultatif).

## Générer les supports

Prérequis : Node.js 20+, JDK 25, Google Chrome.

```bash
./build_support.sh               # vérifie, génère outputs/ (HTML et PDF) et copie les PDF dans pdf/
./build_support.sh --skip-checks # génère sans vérifier
```

Le script vérifie d'abord deux choses :
* aucune slide ne déborde du cadre 1280x720 (`tools/check-slides.mjs`) ;
* chaque exemple Java marqué `// Fichier :` compile et s'exécute en Java 25 (`tools/check-java.mjs`).

Pour prévisualiser les slides, utilisez l'extension Marp for VS Code avec le thème `cours/theme/foodfast.css` (réglage `markdown.marp.themes`).

## Contributions

Vous avez trouvé une erreur ou une explication peu claire ? Ouvrez une Issue ou une Pull Request.
