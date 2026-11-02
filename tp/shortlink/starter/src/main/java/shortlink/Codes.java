package shortlink;

/**
 * Les codes courts : le « 4c92 » de https://sho.rt/4c92.
 *
 * Un code est un nombre écrit en base 62 : chiffres, puis minuscules, puis majuscules.
 */
public final class Codes {

    /** Les 62 symboles, dans l'ordre de leur valeur : '0' vaut 0, 'a' vaut 10, 'Z' vaut 61. */
    public static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static final int MIN_LENGTH = 4;
    public static final int MAX_LENGTH = 12;

    private Codes() {
    }

    /**
     * §1 Un code valide a entre {@link #MIN_LENGTH} et {@link #MAX_LENGTH} caractères, tous dans
     * {@link #ALPHABET}. {@code null} n'est pas valide.
     */
    public static boolean isValid(String code) {
        throw new UnsupportedOperationException("§1 : à écrire");
    }

    /**
     * §3 Écrit {@code number} en base 62 : {@code encode(0)} vaut "0", {@code encode(61)} vaut "Z",
     * {@code encode(62)} vaut "10".
     *
     * @throws IllegalArgumentException si number est négatif
     */
    public static String encode(long number) {
        throw new UnsupportedOperationException("§3 : à écrire");
    }

    /**
     * §6 🔵 🔒 L'inverse de {@link #encode} : {@code decode("10")} vaut 62.
     *
     * @throws IllegalArgumentException si code est vide, contient un caractère hors de
     *         {@link #ALPHABET}, ou représente un nombre trop grand pour un long
     */
    public static long decode(String code) {
        throw new UnsupportedOperationException("§6 : à écrire");
    }

    /**
     * §7 🟣 🔒 Un code imprévisible de {@code length} caractères tirés au hasard dans {@link #ALPHABET}.
     *
     * @throws IllegalArgumentException si length est hors de [MIN_LENGTH, MAX_LENGTH]
     */
    public static String random(int length) {
        throw new UnsupportedOperationException("§7 : à écrire");
    }
}
