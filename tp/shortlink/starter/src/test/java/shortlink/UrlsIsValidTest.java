package shortlink;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** §2 🔒 Urls.isValid. */
@Disabled("§2 : supprimez cette ligne quand vous commencez l'exercice")
class UrlsIsValidTest {

    @Test
    void httpAndHttpsAreAccepted() {
        assertTrue(Urls.isValid("https://foodfast.fr"));
        assertTrue(Urls.isValid("http://localhost:8080/menu"));
        assertTrue(Urls.isValid("https://foodfast.fr/plats?id=4&taille=L#avis"));
    }

    @Test
    void theSchemeIgnoresCase() {
        assertTrue(Urls.isValid("HTTPS://FoodFast.fr"));
        assertTrue(Urls.isValid("Http://foodfast.fr"));
    }

    @Test
    void otherSchemesAreRefused() {
        assertFalse(Urls.isValid("foodfast.fr"));
        assertFalse(Urls.isValid("ftp://foodfast.fr"));
        // 🔒 Les deux dangereux : un lien court qui exécute du code dans le navigateur
        assertFalse(Urls.isValid("javascript:alert(document.cookie)"));
        assertFalse(Urls.isValid("data:text/html,<script>alert(1)</script>"));
    }

    @Test
    void somethingMustFollowTheScheme() {
        assertFalse(Urls.isValid("https://"));
        assertFalse(Urls.isValid("https:///chemin"));
        assertFalse(Urls.isValid("https://?q=1"));
        assertFalse(Urls.isValid("https://#haut"));
    }

    @Test
    void spacesAndControlCharactersAreRefused() {
        assertFalse(Urls.isValid("https://food fast.fr"));
        assertFalse(Urls.isValid(" https://foodfast.fr"));
        assertFalse(Urls.isValid("https://foodfast.fr\n"));
        assertFalse(Urls.isValid("https://foodfast.fr\tx"));
    }

    @Test
    void lengthIsLimited() {
        String base = "https://foodfast.fr/";                    // 20 caractères
        assertTrue(Urls.isValid(base + "a".repeat(1980)));      // 2 000 : la limite
        assertFalse(Urls.isValid(base + "a".repeat(1981)));     // 2 001
    }

    @Test
    void emptyAndNullAreRefused() {
        assertFalse(Urls.isValid(""));
        assertFalse(Urls.isValid(null));
    }

    // Écrivez ici au moins un test à vous
}
