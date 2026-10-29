package miniredis.protocol;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Lit les commandes envoyées par un client.
 *
 * Deux formats sont acceptés :
 * <ul>
 *   <li>un tableau RESP, envoyé par valkey-cli et redis-cli :
 *       {@code *2\r\n$3\r\nGET\r\n$3\r\nnom\r\n} donne {@code ["GET", "nom"]} ;</li>
 *   <li>une commande « inline », tapée à la main dans nc ou telnet :
 *       {@code GET nom\r\n} donne aussi {@code ["GET", "nom"]}.</li>
 * </ul>
 *
 * FOURNI. Vous le modifierez en séance 3 et en séance 4 (sécurité).
 */
public final class RespReader {

    private final InputStream in;

    public RespReader(InputStream in) {
        this.in = new BufferedInputStream(in);
    }

    /**
     * Lit la commande suivante.
     *
     * @return les mots de la commande (le premier est son nom), une liste vide pour une ligne vide,
     *         ou {@code null} quand le client a fermé la connexion
     */
    public List<String> readCommand() throws IOException {
        int first = in.read();
        if (first == -1) {
            return null;
        }
        if (first != '*') {
            String line = ((char) first + readLine()).trim();
            return line.isEmpty() ? List.of() : List.of(line.split("\\s+"));
        }
        int count = Integer.parseInt(readLine());
        List<String> words = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            if (in.read() != '$') {
                throw new IOException("Protocol error: expected '$'");
            }
            int length = Integer.parseInt(readLine());
            byte[] data = new byte[length];
            if (in.readNBytes(data, 0, length) < length) {
                return null;
            }
            in.readNBytes(2); // \r\n
            words.add(new String(data, StandardCharsets.UTF_8));
        }
        return words;
    }

    private String readLine() throws IOException {
        StringBuilder line = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) {
            if (c == '\r') {
                in.read(); // \n
                break;
            }
            if (c == '\n') {
                break;
            }
            line.append((char) c);
        }
        return line.toString();
    }
}
