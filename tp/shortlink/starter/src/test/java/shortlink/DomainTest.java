package shortlink;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** §5 🔵 Urls.domain et la commande domains. */
@Disabled("§5 : supprimez cette ligne quand vous commencez l'exercice")
class DomainTest {

    @Test
    void theHostInLowerCaseWithoutWww() {
        assertEquals("foodfast.fr", Urls.domain("https://foodfast.fr"));
        assertEquals("foodfast.fr", Urls.domain("https://www.FoodFast.fr/menu"));
        assertEquals("api.foodfast.fr", Urls.domain("https://api.foodfast.fr/v1"));
    }

    @Test
    void theHostStopsBeforeThePortPathQueryOrFragment() {
        assertEquals("localhost", Urls.domain("http://localhost:8080/menu"));
        assertEquals("foodfast.fr", Urls.domain("https://foodfast.fr?promo=1"));
        assertEquals("foodfast.fr", Urls.domain("https://foodfast.fr#avis"));
    }

    @Test
    void whatComesBeforeAnAtSignIsNotTheDomain() {
        // 🔒 Le lecteur croit aller sur foodfast.fr : le navigateur va sur evil.example
        assertEquals("evil.example", Urls.domain("https://foodfast.fr@evil.example/connexion"));
        assertEquals("evil.example", Urls.domain("https://user:motdepasse@evil.example"));
    }

    @Test
    void anInvalidUrlIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> Urls.domain("javascript:alert(1)"));
    }

    @Test
    void domainsCountsLinksPerDomainInAlphabeticalOrder() {
        Shell shell = new Shell();
        assertEquals("(aucun lien)", shell.execute("domains"));
        shell.execute("add https://www.foodfast.fr/menu");
        shell.execute("add https://example.com");
        shell.execute("add https://FoodFast.fr/promo");
        assertEquals("""
                example.com 1
                foodfast.fr 2""", shell.execute("domains"));
    }

    // Écrivez ici au moins un test à vous
}
