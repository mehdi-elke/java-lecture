// Compare votre serveur au vrai Valkey : même commande, même réponse ?
//
//   java tools/Compare.java tests/s1.txt
//   java tools/Compare.java tests/s1.txt --ref localhost:6379 --ours localhost:6380
//
// Format du fichier : une commande par ligne, les mots séparés par des espaces.
//   "deux mots"   un argument qui contient des espaces (\" \\ \r \n acceptés)
//   # titre       un commentaire, affiché tel quel
//   !sleep 1100   attendre 1 100 ms (pour tester les expirations)
// Les deux bases sont vidées (FLUSHALL) avant de commencer.
// Les réponses de KEYS et SMEMBERS sont triées : Valkey ne garantit pas leur ordre.

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

void main(String[] args) throws IOException {
    if (args.length == 0) {
        IO.println("usage : java tools/Compare.java tests/s1.txt [--ref hôte:port] [--ours hôte:port]");
        System.exit(2);
    }
    String ref = option(args, "--ref", "localhost:6379");
    String ours = option(args, "--ours", "localhost:6380");
    List<String> lines = Files.readAllLines(Path.of(args[0]), StandardCharsets.UTF_8);

    Client valkey = new Client(ref);
    Client mine = new Client(ours);
    valkey.send(List.of("FLUSHALL"));
    mine.send(List.of("FLUSHALL"));

    int same = 0;
    int different = 0;
    for (String line : lines) {
        String trimmed = line.strip();
        if (trimmed.isEmpty()) continue;
        if (trimmed.startsWith("#")) {
            IO.println("\n" + trimmed);
            continue;
        }
        if (trimmed.startsWith("!sleep ")) {
            sleep(Long.parseLong(trimmed.substring(7).strip()));
            continue;
        }
        List<String> command = words(trimmed);
        String expected = normalize(command, valkey.send(command));
        String actual = normalize(command, mine.send(command));
        if (expected.equals(actual)) {
            same++;
            IO.println("  ✓ " + pad(trimmed) + " " + expected);
        } else {
            different++;
            IO.println("  ✗ " + pad(trimmed) + " attendu : " + expected);
            IO.println("    " + pad("") + " obtenu  : " + actual);
        }
    }
    IO.println("\n" + same + " identiques, " + different + " différents");
    System.exit(different == 0 ? 0 : 1);
}

/** Une connexion à un serveur. Se reconnecte si le serveur a fermé la connexion. */
static final class Client {
    private final String host;
    private final int port;
    private Socket socket;
    private InputStream in;
    private OutputStream out;

    Client(String hostAndPort) {
        String[] parts = hostAndPort.split(":");
        this.host = parts[0];
        this.port = Integer.parseInt(parts[1]);
    }

    String send(List<String> command) {
        try {
            if (socket == null || socket.isClosed()) connect();
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
            return readReply();
        } catch (java.net.ConnectException e) {
            return "(serveur injoignable sur " + host + ":" + port + ")";
        } catch (java.net.SocketTimeoutException e) {
            close();
            return "(pas de réponse après 3 s)";
        } catch (IOException e) {
            close();
            return "(connexion fermée : " + e.getMessage() + ")";
        }
    }

    private void connect() throws IOException {
        socket = new Socket(host, port);
        socket.setSoTimeout(3000);
        in = new BufferedInputStream(socket.getInputStream());
        out = socket.getOutputStream();
    }

    private void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
            // rien à faire : on se reconnectera
        }
        socket = null;
    }

    private String readReply() throws IOException {
        int type = in.read();
        if (type == -1) throw new EOFException("fin du flux");
        String line = readLine();
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
                List<String> items = new ArrayList<>();
                for (int i = 0; i < count; i++) items.add(readReply());
                yield items.toString();
            }
            default -> "(réponse invalide, type '" + (char) type + "')";
        };
    }

    private String readLine() throws IOException {
        ByteArrayOutputStream line = new ByteArrayOutputStream();
        int c;
        while ((c = in.read()) != -1 && c != '\r') line.write(c);
        in.read(); // \n
        return line.toString(StandardCharsets.UTF_8);
    }
}

/** KEYS et SMEMBERS : l'ordre n'est pas garanti, on trie avant de comparer. */
static String normalize(List<String> command, String reply) {
    String name = command.getFirst().toUpperCase();
    if ((name.equals("KEYS") || name.equals("SMEMBERS")) && reply.startsWith("[") && reply.length() > 2) {
        List<String> items = new ArrayList<>(List.of(reply.substring(1, reply.length() - 1).split(", ")));
        Collections.sort(items);
        return items.toString();
    }
    return reply;
}

/** Découpe une ligne en mots, en respectant les guillemets. */
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

static String pad(String s) {
    return s.length() >= 32 ? s : s + " ".repeat(32 - s.length());
}

static String option(String[] args, String name, String defaultValue) {
    for (int i = 0; i < args.length - 1; i++) {
        if (args[i].equals(name)) return args[i + 1];
    }
    return defaultValue;
}

static void sleep(long millis) {
    try {
        Thread.sleep(millis);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}
