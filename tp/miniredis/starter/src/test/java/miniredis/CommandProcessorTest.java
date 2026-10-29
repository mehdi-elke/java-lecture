package miniredis;

import miniredis.protocol.Reply;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Pas besoin de réseau pour tester une commande : on appelle directement le processeur.
 * Ajoutez au moins un test par commande que vous écrivez.
 */
class CommandProcessorTest {

    private final CommandProcessor processor = new CommandProcessor();

    /** Raccourci : run("SET", "k", "v") exécute la commande SET k v. */
    private Reply run(String... command) {
        return processor.execute(List.of(command));
    }

    @Test
    void getReturnsTheValueStoredBySet() {
        assertEquals(Reply.OK, run("SET", "nom", "Ada"));
        assertEquals(new Reply.Bulk("Ada"), run("GET", "nom"));
    }

    @Test
    void getOnAMissingKeyReturnsNil() {
        assertEquals(Reply.NIL, run("GET", "absente"));
    }

    @Test
    void commandNamesAreCaseInsensitive() {
        run("set", "nom", "Ada");
        assertEquals(new Reply.Bulk("Ada"), run("gEt", "nom"));
    }

    @Test
    void wrongNumberOfArgumentsIsAnError() {
        assertEquals(new Reply.Error("ERR wrong number of arguments for 'get' command"), run("GET"));
    }

    @Test
    void unknownCommandIsAnError() {
        assertEquals(
                new Reply.Error("ERR unknown command 'FOO', with args beginning with: 'a' 'b' "),
                run("FOO", "a", "b"));
    }
}
