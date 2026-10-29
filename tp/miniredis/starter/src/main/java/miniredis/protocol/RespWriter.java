package miniredis.protocol;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Encode une {@link Reply} au format RESP2 et l'envoie.
 *
 * FOURNI — ne pas modifier.
 */
public final class RespWriter {

    private final OutputStream out;

    public RespWriter(OutputStream out) {
        this.out = new BufferedOutputStream(out);
    }

    /** Écrit la réponse et l'envoie immédiatement. */
    public void write(Reply reply) throws IOException {
        encode(reply);
        out.flush();
    }

    private void encode(Reply reply) throws IOException {
        switch (reply) {
            case Reply.Simple s -> line("+" + s.value());
            case Reply.Error e -> line("-" + e.message());
            case Reply.Int i -> line(":" + i.value());
            case Reply.Nil _ -> line("$-1");
            case Reply.Bulk b -> {
                // La longueur annoncée est en OCTETS, pas en caractères : "é" fait 2 octets en UTF-8.
                byte[] data = b.value().getBytes(StandardCharsets.UTF_8);
                line("$" + data.length);
                out.write(data);
                out.write(CRLF);
            }
            case Reply.Array a -> {
                line("*" + a.items().size());
                for (Reply item : a.items()) {
                    encode(item);
                }
            }
        }
    }

    private void line(String s) throws IOException {
        out.write(s.getBytes(StandardCharsets.UTF_8));
        out.write(CRLF);
    }

    private static final byte[] CRLF = {'\r', '\n'};
}
