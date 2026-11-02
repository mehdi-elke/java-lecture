package shortlink;

/** Ce qu'on accepte comme URL longue, et ce qu'on en extrait. */
public final class Urls {

    public static final int MAX_LENGTH = 2000;

    private Urls() {
    }

    /**
     * §2 🔒 Une URL valide :
     * <ul>
     *   <li>n'est pas null, et a entre 1 et {@link #MAX_LENGTH} caractères ;</li>
     *   <li>commence par {@code http://} ou {@code https://}, en majuscules ou en minuscules ;</li>
     *   <li>a au moins un caractère après {@code ://}, et ce caractère n'est ni {@code /}, ni {@code ?}, ni {@code #} ;</li>
     *   <li>ne contient aucun espace ni caractère de contrôle (retour à la ligne, tabulation…).</li>
     * </ul>
     */
    public static boolean isValid(String url) {
        throw new UnsupportedOperationException("§2 : à écrire");
    }

    /**
     * §5 🔵 Le domaine d'une URL valide, en minuscules, sans {@code www.}, sans port, sans
     * identifiant : {@code https://www.FoodFast.fr:8443/menu} donne {@code foodfast.fr}.
     *
     * 🔒 Dans {@code https://foodfast.fr@evil.example/}, ce qui précède {@code @} est un identifiant :
     * le domaine est {@code evil.example}.
     *
     * @throws IllegalArgumentException si l'URL n'est pas valide
     */
    public static String domain(String url) {
        throw new UnsupportedOperationException("§5 : à écrire");
    }
}
