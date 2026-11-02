package shortlink;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** §4 Les commandes de la console. */
@Disabled("§4 : supprimez cette ligne quand vous commencez l'exercice")
class ShellTest {

    private final Shell shell = new Shell();

    private String run(String line) {
        return shell.execute(line);
    }

    // add : fourni, comme modèle

    @Test
    void addGivesSequentialCodes() {
        assertEquals("Lien créé : 4c92", run("add https://foodfast.fr/menu"));
        assertEquals("Lien créé : 4c93", run("add https://foodfast.fr/promo"));
    }

    @Test
    void addRefusesAnInvalidUrl() {
        assertEquals("Erreur : URL invalide", run("add javascript:alert(1)"));
        assertEquals("Erreur : usage : add <url>", run("add"));
    }

    @Test
    void unknownCommand() {
        assertEquals("Erreur : commande inconnue : fly", run("fly away"));
    }

    @Test
    void commandNamesIgnoreCaseAndExtraSpaces() {
        assertEquals("Lien créé : 4c92", run("  ADD   https://foodfast.fr  "));
    }

    // open

    @Test
    void openGivesTheLongUrl() {
        run("add https://foodfast.fr/menu");
        assertEquals("https://foodfast.fr/menu", run("open 4c92"));
    }

    @Test
    void openReportsUnknownAndInvalidCodes() {
        assertEquals("Erreur : code inconnu", run("open zzzz"));
        assertEquals("Erreur : code invalide", run("open a-b"));
        assertEquals("Erreur : usage : open <code>", run("open"));
        assertEquals("Erreur : usage : open <code>", run("open 4c92 4c93"));
    }

    // clicks

    @Test
    void clicksCountsEachOpen() {
        run("add https://foodfast.fr/menu");
        assertEquals("0", run("clicks 4c92"));
        run("open 4c92");
        run("open 4c92");
        assertEquals("2", run("clicks 4c92"));
    }

    @Test
    void clicksReportsErrorsLikeOpen() {
        assertEquals("Erreur : code inconnu", run("clicks zzzz"));
        assertEquals("Erreur : code invalide", run("clicks a"));
        assertEquals("Erreur : usage : clicks <code>", run("clicks"));
    }

    // delete

    @Test
    void deleteRemovesTheLink() {
        run("add https://foodfast.fr/menu");
        assertEquals("Lien supprimé : 4c92", run("delete 4c92"));
        assertEquals("Erreur : code inconnu", run("open 4c92"));
        assertEquals("Erreur : code inconnu", run("delete 4c92"));
    }

    @Test
    void deleteDoesNotReuseCodes() {
        run("add https://foodfast.fr/menu");
        run("delete 4c92");
        assertEquals("Lien créé : 4c93", run("add https://foodfast.fr/menu"));
    }

    // list

    @Test
    void listShowsLinksInCreationOrder() {
        run("add https://foodfast.fr/menu");
        run("add https://foodfast.fr/promo");
        run("add https://example.com");
        run("open 4c93");
        run("open 4c94");
        run("open 4c94");
        assertEquals("""
                4c92 -> https://foodfast.fr/menu (0 clic)
                4c93 -> https://foodfast.fr/promo (1 clic)
                4c94 -> https://example.com (2 clics)""", run("list"));
    }

    @Test
    void listWhenEmpty() {
        assertEquals("(aucun lien)", run("list"));
        assertEquals("Erreur : usage : list", run("list tout"));
    }

    // Écrivez ici au moins un test à vous par commande
}
