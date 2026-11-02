package shortlink;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** §1 Codes.isValid. */
@Disabled("§1 : supprimez cette ligne quand vous commencez l'exercice")
class CodesIsValidTest {

    @Test
    void lettersAndDigitsAreAccepted() {
        assertTrue(Codes.isValid("4c92"));
        assertTrue(Codes.isValid("Promo2026"));
        assertTrue(Codes.isValid("ZZZZ"));
    }

    @Test
    void lengthMustBeBetween4And12() {
        assertFalse(Codes.isValid("abc"));          // 3 caractères
        assertTrue(Codes.isValid("abcd"));          // 4
        assertTrue(Codes.isValid("abcdefghijkl"));  // 12
        assertFalse(Codes.isValid("abcdefghijklm")); // 13
    }

    @Test
    void emptyAndNullAreRefused() {
        assertFalse(Codes.isValid(""));
        assertFalse(Codes.isValid(null));
    }

    @Test
    void anythingOutsideTheAlphabetIsRefused() {
        assertFalse(Codes.isValid("ab-cd"));
        assertFalse(Codes.isValid("ab cd"));
        assertFalse(Codes.isValid("café"));     // é n'est pas dans l'alphabet
        assertFalse(Codes.isValid("../etc"));
        assertFalse(Codes.isValid("abcd\n"));
    }

    // Écrivez ici au moins un test à vous
}
