package shortlink;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** §6 🔵 🔒 Codes.decode. */
@Disabled("§6 : supprimez cette ligne quand vous commencez l'exercice")
class CodesDecodeTest {

    @Test
    void decodeIsTheInverseOfEncode() {
        assertEquals(0, Codes.decode("0"));
        assertEquals(61, Codes.decode("Z"));
        assertEquals(62, Codes.decode("10"));
        assertEquals(Shell.FIRST_ID, Codes.decode("4c92"));
        for (long n : new long[] {1, 999, 123_456_789, Long.MAX_VALUE}) {
            assertEquals(n, Codes.decode(Codes.encode(n)));
        }
    }

    @Test
    void symbolsOutsideTheAlphabetAreRefused() {
        assertThrows(IllegalArgumentException.class, () -> Codes.decode("4c-2"));
        assertThrows(IllegalArgumentException.class, () -> Codes.decode(""));
    }

    @Test
    void numbersTooLargeForALongAreRefused() {
        // 🔒 Sans protection, le calcul déborde et donne un nombre négatif, sans aucune erreur
        String justAbove = "aZl8N0y58M8"; // Long.MAX_VALUE + 1
        assertThrows(IllegalArgumentException.class, () -> Codes.decode(justAbove));
        assertThrows(IllegalArgumentException.class, () -> Codes.decode("ZZZZZZZZZZZZ"));
    }

    // Écrivez ici au moins un test à vous
}
