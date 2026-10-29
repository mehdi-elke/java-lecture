package miniredis.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Tests du protocole fourni. À lire : ils montrent les octets réellement échangés. */
class RespTest {

    private static List<String> read(String raw) throws IOException {
        var reader = new RespReader(new ByteArrayInputStream(raw.getBytes(StandardCharsets.UTF_8)));
        return reader.readCommand();
    }

    private static String write(Reply reply) throws IOException {
        var out = new ByteArrayOutputStream();
        new RespWriter(out).write(reply);
        return out.toString(StandardCharsets.UTF_8);
    }

    @Test
    void readsAnArrayOfBulkStrings() throws IOException {
        assertEquals(List.of("SET", "nom", "Ada Lovelace"),
                read("*3\r\n$3\r\nSET\r\n$3\r\nnom\r\n$12\r\nAda Lovelace\r\n"));
    }

    @Test
    void readsAnInlineCommand() throws IOException {
        assertEquals(List.of("GET", "nom"), read("GET nom\r\n"));
    }

    @Test
    void returnsNullAtEndOfStream() throws IOException {
        assertNull(read(""));
    }

    @Test
    void writesEachKindOfReply() throws IOException {
        assertEquals("+OK\r\n", write(Reply.OK));
        assertEquals("-ERR oups\r\n", write(new Reply.Error("ERR oups")));
        assertEquals(":42\r\n", write(new Reply.Int(42)));
        assertEquals("$-1\r\n", write(Reply.NIL));
        assertEquals("*2\r\n:1\r\n$1\r\na\r\n",
                write(new Reply.Array(List.of(new Reply.Int(1), new Reply.Bulk("a")))));
    }

    @Test
    void bulkLengthIsInBytesNotCharacters() throws IOException {
        // 4 caractères, mais 6 octets en UTF-8 : chaque « é » en occupe 2.
        assertEquals("$6\r\nétés\r\n", write(new Reply.Bulk("étés")));
    }
}
