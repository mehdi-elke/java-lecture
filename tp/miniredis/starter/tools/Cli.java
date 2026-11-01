// Un mini valkey-cli, pour parler à votre serveur sans installer valkey-cli.
//
//   java tools/Cli.java                         mode interactif sur localhost:6380
//   java tools/Cli.java --server localhost:6379 mode interactif sur un autre serveur
//   java tools/Cli.java SET plat "pâtes fraîches"   une seule commande, puis s'arrête
//
// Les commandes s'écrivent comme dans valkey-cli : les mots séparés par des espaces,
//   "deux mots"   un argument qui contient des espaces (\" \\ \r \n acceptés).
// Chaque commande est envoyée en tableau RESP, encodée en UTF-8 : les accents et les
// guillemets arrivent intacts (ce que nc ne fait pas : il envoie la ligne telle quelle).
// Les réponses s'affichent comme dans valkey-cli. Pour quitter : quit, exit ou Ctrl+D.

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

void main(String[] args) throws IOException {
    String server = "localhost:6380";
    List<String> oneShot = new ArrayList<>();
    for (int i = 0; i < args.length; i++) {
        if (args[i].equals("--server") && i + 1 < args.length) {
            server = args[++i];
        } else {
            oneShot.add(args[i]);
        }
    }
    String[] parts = server.split(":");
    String host = parts[0];
    int port = Integer.parseInt(parts[1]);

    try (Socket socket = new Socket(host, port)) {
        socket.setSoTimeout(5000);
        InputStream in = new BufferedInputStream(socket.getInputStream());
        OutputStream out = socket.getOutputStream();

        if (!oneShot.isEmpty()) {
            IO.println(execute(oneShot, in, out));
            return;
        }
        String encoding = System.getProperty("stdin.encoding", System.getProperty("native.encoding"));
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in, Charset.forName(encoding)));
        String prompt = host + ":" + port + "> ";
        while (true) {
            IO.print(prompt);
            String line = console.readLine();
            if (line == null) break;
            List<String> command = words(line);
            if (command.isEmpty()) continue;
            String name = command.getFirst().toLowerCase();
            if (name.equals("quit") || name.equals("exit")) break;
            IO.println(execute(command, in, out));
        }
    } catch (java.net.ConnectException e) {
        IO.println("Impossible de se connecter à " + server + " : le serveur est-il lancé ?");
        System.exit(2);
    }
}

/** Envoie une commande et rend la réponse formatée. */
static String execute(List<String> command, InputStream in, OutputStream out) throws IOException {
    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    buffer.write(("*" + command.size() + "\r\n").getBytes(StandardCharsets.UTF_8));
    for (String word : command) {
        byte[] bytes = word.getBytes(StandardCharsets.UTF_8);
        buffer.write(("$" + bytes.length + "\r\n").getBytes(StandardCharsets.UTF_8));
        buffer.write(bytes);
        buffer.write("\r\n".getBytes(StandardCharsets.UTF_8));
    }
    out.write(buffer.toByteArray());
    out.flush();
    try {
        return readReply(in, "");
    } catch (java.net.SocketTimeoutException e) {
        IO.println("(pas de réponse après 5 s)");
        System.exit(1);
        return "";
    } catch (EOFException e) {
        IO.println("(connexion fermée par le serveur)");
        System.exit(1);
        return "";
    }
}

/** Lit une réponse et la met en forme comme valkey-cli. {@code indent} sert aux tableaux imbriqués. */
static String readReply(InputStream in, String indent) throws IOException {
    int type = in.read();
    if (type == -1) throw new EOFException("fin du flux");
    String line = readLine(in);
    return switch (type) {
        case '+' -> line;
        case '-' -> "(error) " + line;
        case ':' -> "(integer) " + line;
        case '$' -> {
            int length = Integer.parseInt(line);
            if (length < 0) yield "(nil)";
            byte[] data = in.readNBytes(length);
            in.readNBytes(2);
            yield "\"" + escape(new String(data, StandardCharsets.UTF_8)) + "\"";
        }
        case '*' -> {
            int count = Integer.parseInt(line);
            if (count < 0) yield "(nil)";
            if (count == 0) yield "(empty array)";
            StringBuilder text = new StringBuilder();
            for (int i = 1; i <= count; i++) {
                String number = i + ") ";
                String itemIndent = indent + " ".repeat(number.length());
                if (i > 1) text.append('\n').append(indent);
                text.append(number).append(readReply(in, itemIndent));
            }
            yield text.toString();
        }
        default -> "(réponse invalide, type '" + (char) type + "')";
    };
}

static String readLine(InputStream in) throws IOException {
    ByteArrayOutputStream line = new ByteArrayOutputStream();
    int c;
    while ((c = in.read()) != -1 && c != '\r') line.write(c);
    in.read(); // \n
    return line.toString(StandardCharsets.UTF_8);
}

/** Découpe une ligne en mots, en respectant les guillemets (comme tools/Compare.java). */
static List<String> words(String line) {
    List<String> words = new ArrayList<>();
    StringBuilder current = null;
    boolean quoted = false;
    for (int i = 0; i < line.length(); i++) {
        char c = line.charAt(i);
        if (quoted) {
            if (c == '\\' && i + 1 < line.length()) {
                char next = line.charAt(++i);
                current.append(switch (next) {
                    case 'n' -> '\n';
                    case 'r' -> '\r';
                    default -> next;
                });
            } else if (c == '"') {
                quoted = false;
            } else {
                current.append(c);
            }
        } else if (c == '"') {
            if (current == null) current = new StringBuilder();
            quoted = true;
        } else if (Character.isWhitespace(c)) {
            if (current != null) words.add(current.toString());
            current = null;
        } else {
            if (current == null) current = new StringBuilder();
            current.append(c);
        }
    }
    if (current != null) words.add(current.toString());
    return words;
}

static String escape(String s) {
    return s.replace("\\", "\\\\").replace("\r", "\\r").replace("\n", "\\n").replace("\"", "\\\"");
}
