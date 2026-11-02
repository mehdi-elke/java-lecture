package shortlink;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** §7 🟣 Alias choisis et codes imprévisibles. */
@Disabled("§7 : supprimez cette ligne quand vous commencez l'exercice")
class BonusTest {

    @Test
    void addWithAnAlias() {
        Shell shell = new Shell();
        assertEquals("Lien créé : promo", shell.execute("add https://foodfast.fr/promo promo"));
        assertEquals("https://foodfast.fr/promo", shell.execute("open promo"));
        assertEquals("Erreur : code déjà pris", shell.execute("add https://example.com promo"));
        assertEquals("Erreur : code invalide", shell.execute("add https://example.com a/b"));
        assertEquals("Lien créé : 4c92", shell.execute("add https://example.com"));
    }

    @Test
    void anAliasCannotStealAFutureSequentialCode() {
        Shell shell = new Shell();
        shell.execute("add https://evil.example 4c92");
        // le lien suivant ne doit pas écraser l'alias : il prend le code libre d'après
        assertEquals("Lien créé : 4c93", shell.execute("add https://foodfast.fr"));
        assertEquals("https://evil.example", shell.execute("open 4c92"));
    }

    @Test
    void randomCodesHaveTheRightShape() {
        for (int i = 0; i < 100; i++) {
            String code = Codes.random(8);
            assertEquals(8, code.length());
            assertTrue(Codes.isValid(code));
        }
        assertThrows(IllegalArgumentException.class, () -> Codes.random(3));
    }

    @Test
    void randomCodesDoNotRepeat() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 1000; i++) {
            seen.add(Codes.random(8));
        }
        assertEquals(1000, seen.size());
    }
}
