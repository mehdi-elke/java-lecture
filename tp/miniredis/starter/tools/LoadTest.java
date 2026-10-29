// Met votre serveur à l'épreuve : plusieurs clients en même temps, et des clients malveillants.
//
//   java tools/LoadTest.java incr        50 clients × 2 000 INCR : le compteur doit valoir 100 000
//   java tools/LoadTest.java lock        50 clients font SET verrou NX en même temps : un seul doit gagner
//   java tools/LoadTest.java block       un client connecté mais muet ne doit pas bloquer les autres
//   java tools/LoadTest.java malformed   12 octets invalides ne doivent pas faire tomber le serveur
//   java tools/LoadTest.java bomb        une chaîne annoncée de 2 Go doit être refusée tout de suite
//   java tools/LoadTest.java all         tous les tests
//
// Option : --server hôte:port (localhost:6380 par défaut).
// Ne lancez ces tests que sur VOTRE serveur.

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

static String host;
static int port;

void main(String[] args) throws Exception {
    if (args.length == 0) {
        IO.println("usage : java tools/LoadTest.java incr|lock|block|malformed|bomb|all [--server hôte:port]");
        System.exit(2);
    }
    String server = "localhost:6380";
    for (int i = 0; i < args.length - 1; i++) {
        if (args[i].equals("--server")) server = args[i + 1];
    }
    host = server.split(":")[0];
    port = Integer.parseInt(server.split(":")[1]);

    List<String> modes = args[0].equals("all") ? List.of("block", "malformed", "bomb", "incr", "lock") : List.of(args[0]);
    int failures = 0;
    for (String mode : modes) {
        IO.println("\n== " + mode);
        boolean ok;
        try {
            ok = switch (mode) {
                case "incr" -> incr();
                case "lock" -> lock();
                case "block" -> block();
                case "malformed" -> malformed();
                case "bomb" -> bomb();
                default -> {
                    IO.println("mode inconnu : " + mode);
                    yield false;
                }
            };
        } catch (Exception e) {
            IO.println("erreur : " + e);
            ok = false;
        }
        IO.println(ok ? "✓ réussi" : "✗ échec");
        if (!ok) failures++;
    }
    System.exit(failures == 0 ? 0 : 1);
}

/** 50 clients × 2 000 INCR sur la même clé. Sans protection, des incréments se perdent. */
static boolean incr() throws Exception {
    int clients = 50;
    int perClient = 2_000;
    try (Connection c = new Connection()) {
        c.command("DEL", "loadtest:compteur");
    }
    long start = System.nanoTime();
    AtomicInteger errors = new AtomicInteger();
    runInParallel(clients, id -> {
        try (Connection c = new Connection()) {
            for (int i = 0; i < perClient; i++) {
                String reply = c.command("INCR", "loadtest:compteur");
                if (!reply.startsWith(":")) errors.incrementAndGet();
            }
        }
    });
    String value;
    try (Connection c = new Connection()) {
        value = c.command("GET", "loadtest:compteur");
    }
    long millis = (System.nanoTime() - start) / 1_000_000;
    IO.println("attendu : 100000, obtenu : " + value + " (" + errors.get() + " erreur(s), " + millis + " ms)");
    return value.equals("100000") && errors.get() == 0;
}

/** 50 clients veulent le même verrou au même instant, 20 fois de suite. Un seul doit l'obtenir à chaque fois. */
static boolean lock() throws Exception {
    int clients = 50;
    int rounds = 20;
    boolean ok = true;
    for (int round = 1; round <= rounds; round++) {
        try (Connection c = new Connection()) {
            c.command("DEL", "loadtest:verrou");
        }
        AtomicInteger winners = new AtomicInteger();
        runInParallel(clients, id -> {
            try (Connection c = new Connection()) {
                if (c.command("SET", "loadtest:verrou", "client-" + id, "NX").equals("OK")) {
                    winners.incrementAndGet();
                }
            }
        });
        if (winners.get() != 1) {
            IO.println("tour " + round + " : " + winners.get() + " clients ont obtenu le verrou");
            ok = false;
        }
    }
    if (ok) IO.println(rounds + " tours : à chaque fois, un seul gagnant");
    return ok;
}

/** Un client se connecte et ne dit rien. Un autre client doit quand même obtenir sa réponse. */
static boolean block() throws Exception {
    try (Socket silent = new Socket(host, port)) {
        Thread.sleep(200);
        try (Connection c = new Connection()) {
            String reply = c.command("PING");
            IO.println("PING pendant qu'un autre client est connecté : " + reply);
            return reply.equals("PONG");
        }
    }
}

/** "*1\r\n$abc\r\n" : une longueur qui n'est pas un nombre. Le serveur doit répondre une erreur et survivre. */
static boolean malformed() throws Exception {
    String reply = sendRaw("*1\r\n$abc\r\n");
    IO.println("réponse à *1 $abc : " + reply);
    return reply.startsWith("-ERR Protocol error") & stillAlive();
}

/** "*1\r\n$2000000000\r\n" : 30 octets qui annoncent 2 Go. Le serveur doit refuser sans attendre ni allouer. */
static boolean bomb() throws Exception {
    String reply = sendRaw("*1\r\n$2000000000\r\n");
    IO.println("réponse à une chaîne de 2 Go : " + reply);
    return reply.startsWith("-ERR Protocol error") & stillAlive();
}

/** Envoie des octets bruts et lit la première ligne de réponse (3 s maximum). */
static String sendRaw(String raw) {
    try (Socket socket = new Socket(host, port)) {
        socket.setSoTimeout(3000);
        socket.getOutputStream().write(raw.getBytes(StandardCharsets.UTF_8));
        socket.getOutputStream().flush();
        String line = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)).readLine();
        return line == null ? "(connexion fermée sans réponse)" : line;
    } catch (SocketTimeoutException e) {
        return "(pas de réponse après 3 s)";
    } catch (IOException e) {
        return "(" + e.getMessage() + ")";
    }
}

static boolean stillAlive() {
    try (Connection c = new Connection()) {
        String reply = c.command("PING");
        IO.println("le serveur répond encore : " + reply);
        return reply.equals("PONG");
    } catch (Exception e) {
        IO.println("le serveur ne répond plus : " + e.getMessage());
        return false;
    }
}

interface ClientTask {
    void run(int id) throws Exception;
}

/** Lance n clients en même temps (ils attendent tous le même top départ). */
static void runInParallel(int n, ClientTask task) throws Exception {
    CountDownLatch start = new CountDownLatch(1);
    List<Future<?>> futures = new ArrayList<>();
    try (ExecutorService pool = Executors.newFixedThreadPool(n)) {
        for (int i = 0; i < n; i++) {
            int id = i;
            futures.add(pool.submit(() -> {
                start.await();
                task.run(id);
                return null;
            }));
        }
        start.countDown();
        for (Future<?> f : futures) {
            try {
                f.get(60, TimeUnit.SECONDS);
            } catch (ExecutionException | TimeoutException e) {
                IO.println("un client a échoué : " + e);
            }
        }
    }
}

/** Une connexion simple : envoie une commande RESP, lit une réponse courte. */
static final class Connection implements AutoCloseable {
    private final Socket socket;
    private final InputStream in;
    private final OutputStream out;

    Connection() throws IOException {
        socket = new Socket(host, port);
        socket.setSoTimeout(5_000);
        in = new BufferedInputStream(socket.getInputStream());
        out = new BufferedOutputStream(socket.getOutputStream());
    }

    /** Renvoie "OK", "PONG", ":42", la valeur d'un bulk, "(nil)" ou "-ERR ...". */
    String command(String... words) throws IOException {
        StringBuilder sb = new StringBuilder("*").append(words.length).append("\r\n");
        for (String w : words) {
            sb.append('$').append(w.getBytes(StandardCharsets.UTF_8).length).append("\r\n").append(w).append("\r\n");
        }
        out.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        out.flush();
        int type = in.read();
        String line = readLine();
        return switch (type) {
            case '+' -> line;
            case '-' -> "-" + line;
            case ':' -> ":" + line;
            case '$' -> {
                int length = Integer.parseInt(line);
                if (length < 0) yield "(nil)";
                String value = new String(in.readNBytes(length), StandardCharsets.UTF_8);
                in.readNBytes(2);
                yield value;
            }
            case -1 -> throw new EOFException("connexion fermée par le serveur");
            default -> "(réponse inattendue : " + (char) type + line + ")";
        };
    }

    private String readLine() throws IOException {
        ByteArrayOutputStream line = new ByteArrayOutputStream();
        int c;
        while ((c = in.read()) != -1 && c != '\r') line.write(c);
        in.read();
        return line.toString(StandardCharsets.UTF_8);
    }

    @Override
    public void close() throws IOException {
        socket.close();
    }
}
