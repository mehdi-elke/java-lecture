package shortlink;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** §0 Un premier test, qui passe déjà : lancez-le pour vérifier que JUnit fonctionne. */
class PremierTest {

    @Test
    void theAlphabetHas62Symbols() {
        assertEquals(62, Codes.ALPHABET.length());
    }
}
