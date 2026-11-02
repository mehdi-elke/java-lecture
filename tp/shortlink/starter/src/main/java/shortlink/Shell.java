package shortlink;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Exécute les commandes tapées dans la console : {@code add https://foodfast.fr} répond
 * {@code Lien créé : 4c92}.
 *
 * Les liens sont gardés en mémoire, dans deux Map : code → URL longue, et code → nombre de clics.
 */
public final class Shell {

    /** Le premier lien créé reçoit le numéro 1 000 000, donc le code "4c92" : 4 caractères dès le début. */
    public static final long FIRST_ID = 1_000_000;

    // LinkedHashMap : list affiche les liens dans l'ordre de création
    private final Map<String, String> links = new LinkedHashMap<>();
    private final Map<String, Integer> clicks = new HashMap<>();
    private long nextId = FIRST_ID;

    /**
     * Exécute une ligne de commande.
     *
     * @param line ce que l'utilisateur a tapé : "add https://foodfast.fr"
     * @return le texte à afficher
     */
    public String execute(String line) {
        // "  ADD   https://foodfast.fr " → ["ADD", "https://foodfast.fr"]
        String[] words = line.strip().split("\\s+");
        String command = words[0].toLowerCase(Locale.ROOT);
        // Les arguments : tous les mots sauf le premier
        List<String> args = List.of(words).subList(1, words.length);
        return switch (command) {
            case "" -> "";                 // ligne vide : rien à afficher
            case "help" -> help();
            case "add" -> add(args);
            // §4 : ajoutez ici open, clicks, delete et list
            default -> "Erreur : commande inconnue : " + words[0];
        };
    }

    private String help() {
        return """
                add <url>        raccourcit une URL
                open <code>      affiche l'URL d'un code, et compte un clic
                clicks <code>    nombre de clics d'un code
                delete <code>    supprime un lien
                list             tous les liens
                quit             quitter""";
    }

    /** add <url> : crée un lien. C'est le modèle à suivre pour les autres commandes. */
    private String add(List<String> args) {
        // 1. Le bon nombre d'arguments ?
        if (args.size() != 1) {
            return usage("add <url>");
        }
        // 2. Des arguments valides ?
        String url = args.get(0);
        if (!Urls.isValid(url)) {
            return "Erreur : URL invalide";
        }
        // 3. Le travail
        String code = Codes.encode(nextId);
        nextId++;
        links.put(code, url);
        clicks.put(code, 0);
        // 4. La réponse
        return "Lien créé : " + code;
    }

    private static String usage(String syntax) {
        return "Erreur : usage : " + syntax;
    }
}
