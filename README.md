# Java — M1 Informatique

Supports du module Java (Java 25 LTS) : 16 heures, en 4 séances de 4 h. Chaque séance commence par environ 1 h de cours, suivie de TP.

| Séance | Thème | TP Mini-Redis |
|---|---|---|
| 1 | Bases du langage, Maven et JUnit | Premières commandes : `PING`, `INCR`, `APPEND`… |
| 2 | Classes, `record`, `enum`, héritage, interfaces, collections | Réorganisation en classes, listes, expiration des clés |
| 3 | Lambdas, streams, exceptions, fichiers | Robustesse, sauvegarde sur fichier |
| 4 | Concurrence, JDBC, sécurité | Plusieurs clients, sauvegarde H2, injection SQL |

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
├── tp/
│   └── miniredis/
│       ├── enonce/       # Énoncés, un par séance
│       └── starter/      # Projet Maven de départ
├── tools/                # Vérification et génération des supports (Node.js)
└── build_support.sh
```

Deux supports par séance :
* **Slides** (`cours/slides/`) : ce qui est projeté en cours, volontairement léger.
* **Support détaillé** (`cours/supports/`) : les explications complètes, le code complet et les approfondissements.

## Le TP : un mini-Redis

Vous écrivez en Java un serveur clé-valeur compatible avec le vrai client `valkey-cli`. La règle est simple : votre serveur doit répondre **exactement comme Valkey**. L'outil `tools/Compare.java` envoie les mêmes commandes aux deux serveurs et affiche les différences. Tout commence dans `tp/miniredis/enonce/`.

Prérequis : JDK 25, Maven, et Docker pour lancer le Valkey de référence.

## Générer les supports

Prérequis : Node.js 20+, JDK 25, Google Chrome.

```bash
./build_support.sh               # vérifie puis génère outputs/slides et outputs/supports (HTML et PDF)
./build_support.sh --skip-checks # génère sans vérifier
```

Le script vérifie d'abord deux choses :
* aucune slide ne déborde du cadre 1280x720 (`tools/check-slides.mjs`) ;
* chaque exemple Java marqué `// Fichier :` compile et s'exécute en Java 25 (`tools/check-java.mjs`).

Pour prévisualiser les slides, utilisez l'extension Marp for VS Code avec le thème `cours/theme/foodfast.css` (réglage `markdown.marp.themes`).

## Contributions

Vous avez trouvé une erreur ou une explication peu claire ? Ouvrez une Issue ou une Pull Request.
