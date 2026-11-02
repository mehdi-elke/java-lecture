// Compare votre Pub/Sub au vrai Valkey, avec plusieurs clients connectés en même temps.
//
//   java tools/PubSubCompare.java tests/pubsub.txt
//   java tools/PubSubCompare.java tests/pubsub.txt --ref localhost:6379 --ours localhost:6380
//   java tools/PubSubCompare.java tests/pubsub.txt --offline   compare avec tests/pubsub.expected
//   java tools/PubSubCompare.java tests/pubsub.txt --record    (re)crée pubsub.expected depuis Valkey
// Comme tools/Compare.java : si Valkey est injoignable, la comparaison se fait avec les réponses
// enregistrées dans le fichier .expected.
//
// Format du fichier : chaque ligne commence par le nom du client qui envoie la commande.
//   A SUBSCRIBE actu         le client A s'abonne (il est connecté à la première ligne qui le nomme)
//   B PUBLISH actu bonjour   le client B publie
//   A !close                 le client A ferme sa connexion
//   !sleep 200               attendre 200 ms
//   # titre                  un commentaire, affiché tel quel
// Après chaque commande, l'outil relève TOUT ce que chaque client reçoit : d'abord le client qui a
// envoyé la commande, puis les autres par ordre alphabétique. Un client qui ne reçoit rien n'apparaît pas.
//   => B (integer) 1                        la réponse de B
//   => A ["message", "actu", "bonjour"]     le message poussé chez A
// Un client qui n'a rien demandé ne doit rien recevoir : un message en trop est une différence.
//
// Fermez vos autres clients (Cli abonné, par exemple) avant de lancer l'outil : un abonné de plus
// change le nombre renvoyé par PUBLISH.

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

void main(String[] args) throws IOException {
    if (args.length == 0) {
        IO.println("usage : java tools/PubSubCompare.java tests/pubsub.txt [--ref hôte:port] [--ours hôte:port] [--offline | --record]");
        System.exit(2);
    }
    String ref = option(args, "--ref", "localhost:6379");
    String ours = option(args, "--ours", "localhost:6380");
    List<String> lines = Files.readAllLines(Path.of(args[0]), StandardCharsets.UTF_8);
    Path expectedFile = Path.of(args[0].replaceFirst("\\.txt$", "") + ".expected");

    if (List.of(args).contains("--record")) {
        if (!reachable(ref)) {
            IO.println("Valkey injoignable sur " + ref + " : rien à enregistrer.");
            System.exit(2);
        }
        List<String> output = new ArrayList<>();
        play(lines, ref, (line, received) -> {
            output.add(line);
            received.forEach(r -> output.add("=> " + r));
        });
        Files.write(expectedFile, output, StandardCharsets.UTF_8);
        IO.println("Réponses enregistrées dans " + expectedFile);
        return;
    }
    if (!reachable(ours)) {
        IO.println("Votre serveur ne répond pas sur " + ours + " : lancez d'abord Server.main (bouton ▶ ou mvn compile exec:java).");
        System.exit(2);
    }

    // Les réponses attendues, commande par commande : relevées sur Valkey maintenant, ou lues dans le .expected
    List<List<String>> expected = new ArrayList<>();
    if (List.of(args).contains("--offline") || !reachable(ref)) {
        if (!Files.exists(expectedFile)) {
            IO.println("Valkey non utilisé (" + ref + "), et pas de réponses enregistrées (" + expectedFile + ").");
            System.exit(2);
        }
        IO.println("Valkey non utilisé : comparaison avec les réponses enregistrées dans " + expectedFile);
        List<String> commands = new ArrayList<>();
        for (String line : Files.readAllLines(expectedFile, StandardCharsets.UTF_8)) {
            if (line.startsWith("=> ")) {
                expected.getLast().add(line.substring(3));
            } else {
                commands.add(line);
                if (isCommand(line)) expected.add(new ArrayList<>());
            }
        }
        if (!commands.stream().filter(l -> isCommand(l)).toList()
                .equals(lines.stream().filter(l -> isCommand(l)).toList())) {
            IO.println("⚠ " + args[0] + " ne contient plus les mêmes commandes que " + expectedFile + " :");
            IO.println("  ce sont les commandes enregistrées qui sont jouées. Vos ajouts demandent Valkey.");
        }
        lines = commands;
    } else {
        play(lines, ref, (line, received) -> expected.add(received));
    }

    int[] counts = new int[2]; // identiques, différents
    Iterator<List<String>> next = expected.iterator();
    play(lines, ours, (line, received) -> {
        List<String> wanted = next.hasNext() ? next.next() : List.of("(réponse absente de " + expectedFile + ")");
        if (wanted.equals(received)) {
            counts[0]++;
            IO.println("  ✓ " + line);
            wanted.forEach(r -> IO.println("      " + r));
        } else {
            counts[1]++;
            IO.println("  ✗ " + line);
            IO.println("      attendu :" + (wanted.isEmpty() ? " (rien)" : ""));
            wanted.forEach(r -> IO.println("        " + r));
            IO.println("      obtenu  :" + (received.isEmpty() ? " (rien)" : ""));
            received.forEach(r -> IO.println("        " + r));
        }
    }, title -> IO.println("\n" + title));
    IO.println("\n" + counts[0] + " identiques, " + counts[1] + " différents");
    if (counts[1] > 0) {
        IO.println("Un ✗ peut en entraîner d'autres plus bas (un abonnement manqué, un serveur arrêté) : corrigez le premier d'abord.");
    }
    System.exit(counts[1] == 0 ? 0 : 1);
}

interface Step {
    void done(String line, List<String> received) throws IOException;
}

static void play(List<String> lines, String server, Step step) throws IOException {
    play(lines, server, step, _ -> {});
}

/** Joue le scénario sur un serveur, et passe à {@code step} ce que chaque commande a fait recevoir. */
static void play(List<String> lines, String server, Step step, java.util.function.Consumer<String> comment)
        throws IOException {
    Map<String, Client> clients = new TreeMap<>();
    try {
        for (String line : lines) {
            String trimmed = line.strip();
            if (trimmed.isEmpty()) continue;
            if (trimmed.startsWith("#")) {
                comment.accept(trimmed);
                continue;
            }
            if (trimmed.startsWith("!sleep ")) {
                sleep(Long.parseLong(trimmed.substring(7).strip()));
                continue;
            }
            String name = trimmed.substring(0, trimmed.indexOf(' '));
            String rest = trimmed.substring(name.length()).strip();
            Client sender = clients.computeIfAbsent(name, n -> new Client(n, server));
            List<String> received = new ArrayList<>();
            if (rest.equals("!close")) {
                sender.close();
                clients.remove(name);
                sleep(100); // laisser au serveur le temps de constater la déconnexion
            } else {
                List<String> command = words(rest);
                sender.send(command);
                sender.collect(received, true, command);
            }
            for (Client other : clients.values()) {
                if (other != sender) other.collect(received, false, null);
            }
            step.done(trimmed, received);
        }
    } finally {
        for (Client client : clients.values()) client.close();
    }
}

static boolean isCommand(String line) {
    String trimmed = line.strip();
    return !trimmed.isEmpty() && !trimmed.startsWith("#") && !trimmed.startsWith("!") && !trimmed.startsWith("=> ");
}

static boolean reachable(String server) {
    String[] parts = server.split(":");
    try (Socket _ = new Socket(parts[0], Integer.parseInt(parts[1]))) {
        return true;
    } catch (IOException e) {
        return false;
    }
}

/** Une connexion nommée (A, B…). */
static final class Client {
    private final String name;
    private final String server;
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private boolean closed;

    Client(String name, String server) {
        this.name = name;
        this.server = server;
    }

    void send(List<String> command) throws IOException {
        if (closed) return;
        try {
            if (socket == null) {
                String[] parts = server.split(":");
                socket = new Socket(parts[0], Integer.parseInt(parts[1]));
                in = new BufferedInputStream(socket.getInputStream());
                out = socket.getOutputStream();
            }
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
        } catch (IOException e) {
            closed = true;
        }
    }

    /**
     * Lit tout ce qui arrive. Le client qui vient d'envoyer une commande attend au moins une réponse
     * (3 s au plus) ; ensuite, on lit jusqu'à 150 ms de silence.
     */
    void collect(List<String> received, boolean waitForReply, List<String> command) throws IOException {
        if (socket == null) return;
        if (closed) {
            if (waitForReply) received.add(name + " (connexion fermée)");
            return;
        }
        boolean first = waitForReply;
        while (true) {
            try {
                socket.setSoTimeout(first ? 3000 : 150);
                String reply = readReply();
                received.add(name + " " + (command == null ? reply : normalize(command, reply)));
                first = false;
            } catch (SocketTimeoutException e) {
                if (first) received.add(name + " (pas de réponse après 3 s)");
                return;
            } catch (IOException e) {
                received.add(name + " (connexion fermée)");
                closed = true;
                return;
            }
        }
    }

    void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException ignored) {
            // déjà fermée
        }
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
        if (c == -1) throw new EOFException("fin du flux");
        in.read(); // \n
        return line.toString(StandardCharsets.UTF_8);
    }
}

/** PUBSUB CHANNELS : l'ordre n'est pas garanti, on trie avant de comparer. */
static String normalize(List<String> command, String reply) {
    String name = command.getFirst().toUpperCase();
    if (name.equals("PUBSUB") && reply.startsWith("[") && reply.length() > 2) {
        List<String> items = new ArrayList<>(List.of(reply.substring(1, reply.length() - 1).split(", ")));
        if (command.size() > 1 && command.get(1).equalsIgnoreCase("CHANNELS")) Collections.sort(items);
        return items.toString();
    }
    return reply;
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
