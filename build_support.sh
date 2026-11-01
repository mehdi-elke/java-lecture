#!/bin/bash

# Génère les supports de cours :
#   - slides (Marp)      : cours/slides/*.md   -> outputs/slides/{html,pdf}
#   - supports détaillés : cours/supports/*.md -> outputs/supports/{html,pdf}
#   - supports enseignant : prive/outputs/ (si le dépôt privé est présent)
# Avant de générer, vérifie que les slides ne débordent pas et que les exemples Java
# compilent et s'exécutent (JDK 25).
#
# Prérequis : Node.js 20+, JDK 25, Google Chrome (ou CHROME_PATH).
# Options :
#   --skip-checks   génère sans lancer les vérifications

# Arrête le script si une commande échoue
set -e

cd "$(dirname "$0")/tools"

if [ ! -d node_modules ]; then
    echo "--- Installation des outils (première exécution) ---"
    npm ci --no-audit --no-fund
fi

mkdir -p lib
H2_JAR="lib/h2-2.5.252.jar"
JUNIT_JAR="lib/junit-platform-console-standalone-6.1.3.jar"
[ -f "$H2_JAR" ] || curl -sfL -o "$H2_JAR" https://repo1.maven.org/maven2/com/h2database/h2/2.5.252/h2-2.5.252.jar
[ -f "$JUNIT_JAR" ] || curl -sfL -o "$JUNIT_JAR" https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/6.1.3/junit-platform-console-standalone-6.1.3.jar

if [ "$1" != "--skip-checks" ]; then
    echo "--- Vérification : débordement des slides ---"
    node check-slides.mjs
    echo "--- Vérification : exemples Java ---"
    node check-java.mjs
fi

echo "--- Génération des supports ---"
node build.mjs

echo "--- Génération des supports enseignant (si prive/ est présent) ---"
node build-enseignant.mjs
