#!/bin/bash

# Script pour convertir les présentations Marp (.md) en HTML en utilisant Docker.

# Arrête le script si une commande échoue
set -e

# Le dossier où les fichiers HTML seront générés
OUTPUT_DIR="outputs"

# Nettoyer et (re)créer le dossier de sortie
echo "--- Nettoyage et création du dossier de sortie: $OUTPUT_DIR ---"
rm -rf "$OUTPUT_DIR"
mkdir -p "$OUTPUT_DIR"

# Trouver tous les fichiers .md dans les dossiers spécifiés
MD_FILES=$(find cours qcms -name "*.md")

if [ -z "$MD_FILES" ]; then
    echo "Aucun fichier Markdown trouvé. Fin du script."
    exit 0
fi

echo "--- Début de la conversion des fichiers Markdown ---"

# Boucler sur chaque fichier trouvé
for md_file in $MD_FILES; do
    # Définir le nom du fichier de sortie
    # Extrait le nom du fichier sans le chemin (ex: "seance1.md")
    base_name=$(basename "$md_file")
    # Définir les noms des fichiers de sortie HTML et PDF
    html_name="${base_name%.md}.html"
    pdf_name="${base_name%.md}.pdf"
    html_output_path="$OUTPUT_DIR/html/$html_name"
    pdf_output_path="$OUTPUT_DIR/pdf/$pdf_name"

    echo "Conversion de '$md_file' en HTML et PDF..."

    # Exécuter Marp CLI pour la sortie HTML
    docker run --rm -v "$(pwd)":/home/marp/app/ marpteam/marp-cli "$md_file" -o "$html_output_path" --allow-local-files

    # Exécuter Marp CLI pour la sortie PDF
    docker run --rm -v "$(pwd)":/home/marp/app/ marpteam/marp-cli "$md_file" --pdf -o "$pdf_output_path" --allow-local-files

done

echo "--- Conversion terminée avec succès! ---"
echo "Vos présentations HTML sont disponibles dans le dossier: $OUTPUT_DIR"
