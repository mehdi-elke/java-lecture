package shortlink;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** §3 Codes.encode. */
@Disabled("§3 : supprimez cette ligne quand vous commencez l'exercice")
class CodesEncodeTest {

    @Test
    void oneSymbolBelow62() {
        assertEquals("0", Codes.encode(0));
        assertEquals("9", Codes.encode(9));
        assertEquals("a", Codes.encode(10));
        assertEquals("z", Codes.encode(35));
        assertEquals("A", Codes.encode(36));
        assertEquals("Z", Codes.encode(61));
    }

    @Test
    void twoSymbolsFrom62() {
        assertEquals("10", Codes.encode(62));     // 1 × 62 + 0
        assertEquals("11", Codes.encode(63));     // 1 × 62 + 1
        assertEquals("ZZ", Codes.encode(3843));   // 61 × 62 + 61
        assertEquals("100", Codes.encode(3844));  // 62 × 62
    }

    @Test
    void theFirstLinkOfTheShell() {
        assertEquals("4c92", Codes.encode(Shell.FIRST_ID));
        assertEquals("4c93", Codes.encode(Shell.FIRST_ID + 1));
    }

    @Test
    void theLargestLong() {
        assertEquals("aZl8N0y58M7", Codes.encode(Long.MAX_VALUE));
    }

    @Test
    void negativeNumbersAreRefused() {
        assertThrows(IllegalArgumentException.class, () -> Codes.encode(-1));
    }

    // Écrivez ici au moins un test à vous
}
