// Met votre serveur à l'épreuve : plusieurs clients en même temps, et des clients malveillants.
//
//   java tools/LoadTest.java incr        50 clients × 2 000 INCR : le compteur doit valoir 100 000
//   java tools/LoadTest.java lock        50 clients font SET verrou NX en même temps, 200 fois : un seul doit gagner
//   java tools/LoadTest.java block       un client connecté mais muet ne doit pas bloquer les autres
//   java tools/LoadTest.java malformed   12 octets invalides ne doivent pas faire tomber le serveur
//   java tools/LoadTest.java bomb        une chaîne annoncée de 2 Go doit être refusée tout de suite
//   java tools/LoadTest.java all         tous les tests ci-dessus
//
// Défi Pub/Sub (enonce/defi-pubsub.md) :
//   java tools/LoadTest.java pubsub-race 20 éditeurs publient en même temps vers un abonné : rien ne se perd ni ne se mélange
//   java tools/LoadTest.java pubsub-slow un abonné qui ne lit plus ne doit pas bloquer les éditeurs
//   java tools/LoadTest.java pubsub      les deux
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
        IO.println("usage : java tools/LoadTest.java incr|lock|block|malformed|bomb|all|pubsub-race|pubsub-slow|pubsub [--server hôte:port]");
        System.exit(2);
    }
    String server = "localhost:6380";
    for (int i = 0; i < args.length - 1; i++) {
        if (args[i].equals("--server")) server = args[i + 1];
    }
    host = server.split(":")[0];
    port = Integer.parseInt(server.split(":")[1]);

    List<String> modes = switch (args[0]) {
        case "all" -> List.of("block", "malformed", "bomb", "incr", "lock");
        case "pubsub" -> List.of("pubsub-race", "pubsub-slow");
        default -> List.of(args[0]);
    };
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
                case "pubsub-race" -> pubsubRace();
                case "pubsub-slow" -> pubsubSlow();
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

/** 50 clients veulent le même verrou au même instant, 200 fois de suite. Un seul doit l'obtenir à chaque fois. */
static boolean lock() throws Exception {
    int clients = 50;
    int rounds = 200;
    int badRounds = 0;
    List<Connection> connections = new ArrayList<>();
    try {
        // Tous connectés AVANT le top départ : les 50 SET NX arrivent vraiment en même temps
        for (int i = 0; i < clients; i++) {
            connections.add(new Connection());
        }
        try {
            for (Connection c : connections) {
                c.command("PING");
            }
        } catch (SocketTimeoutException e) {
            IO.println("le serveur ne répond qu'au premier client : faites d'abord le test block (threads)");
            return false;
        }
        for (int round = 1; round <= rounds; round++) {
            String reset = connections.getFirst().command("DEL", "loadtest:verrou");
            if (!reset.startsWith(":")) {
                IO.println("DEL a répondu " + reset);
                return false;
            }
            AtomicInteger winners = new AtomicInteger();
            AtomicInteger errors = new AtomicInteger();
            runInParallel(clients, id -> {
                String reply = connections.get(id).command("SET", "loadtest:verrou", "client-" + id, "NX");
                if (reply.equals("OK")) {
                    winners.incrementAndGet();
                } else if (reply.startsWith("-")) {
                    errors.incrementAndGet();
                }
            });
            if (errors.get() > 0) {
                IO.println("SET ... NX répond une erreur : la commande est-elle écrite ? (voir s4.txt)");
                return false;
            }
            if (winners.get() != 1) {
                badRounds++;
                if (badRounds <= 5) {
                    IO.println("tour " + round + " : " + winners.get() + " clients ont obtenu le verrou");
                }
            }
        }
    } finally {
        for (Connection c : connections) {
            c.close();
        }
    }
    IO.println(badRounds == 0
            ? rounds + " tours : à chaque fois, un seul gagnant"
            : badRounds + " tour(s) sur " + rounds + " avec plusieurs gagnants");
    return badRounds == 0;
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

/**
 * 20 éditeurs publient 500 messages chacun, en même temps, vers un seul abonné. Les messages ont des
 * tailles différentes : si deux threads écrivent sur la socket de l'abonné en même temps, leurs octets
 * se mélangent et le flux RESP devient illisible. L'abonné doit recevoir les 10 000 messages, intacts,
 * et ceux d'un même éditeur dans l'ordre où il les a publiés.
 */
static boolean pubsubRace() throws Exception {
    int publishers = 20;
    int perPublisher = 500;
    String channel = "loadtest:canal";
    try (Socket subscriber = new Socket(host, port)) {
        subscriber.setSoTimeout(10_000);
        InputStream in = new BufferedInputStream(subscriber.getInputStream());
        subscriber.getOutputStream().write(("*2\r\n$9\r\nSUBSCRIBE\r\n$" + channel.length() + "\r\n" + channel + "\r\n")
                .getBytes(StandardCharsets.UTF_8));
        subscriber.getOutputStream().flush();
        Object confirmation = readFrame(in);
        if (!(confirmation instanceof List<?> first) || !"subscribe".equals(first.getFirst())) {
            IO.println("SUBSCRIBE a répondu " + confirmation);
            return false;
        }
        // L'abonné lit pendant que les éditeurs publient, comme un vrai abonné
        int[] lastSeen = new int[publishers];
        Arrays.fill(lastSeen, -1);
        int[] counts = new int[2]; // reçus, hors d'ordre
        Future<String> reading;
        try (ExecutorService readerThread = Executors.newSingleThreadExecutor()) {
            reading = readerThread.submit(() -> {
                try {
                    while (counts[0] < publishers * perPublisher) {
                        Object frame = readFrame(in);
                        if (!(frame instanceof List<?> items) || items.size() != 3 || !"message".equals(items.getFirst())) {
                            return "message n° " + (counts[0] + 1) + " illisible : " + frame;
                        }
                        String[] parts = ((String) items.get(2)).split(":", 3);
                        int id = Integer.parseInt(parts[0]);
                        int i = Integer.parseInt(parts[1]);
                        if (parts[2].length() != (id * 997 + i * 31) % 3000) {
                            return "message " + id + ":" + i + " tronqué ou mélangé";
                        }
                        if (i <= lastSeen[id]) counts[1]++;
                        lastSeen[id] = i;
                        counts[0]++;
                    }
                    return null;
                } catch (SocketTimeoutException e) {
                    return "l'abonné n'a reçu que " + counts[0] + " messages sur " + publishers * perPublisher;
                } catch (IOException | RuntimeException e) {
                    return "flux illisible après " + counts[0] + " messages (" + e + ") : deux écritures se sont mélangées ?";
                }
            });
            AtomicInteger badReplies = new AtomicInteger();
            runInParallel(publishers, id -> {
                try (Connection c = new Connection()) {
                    for (int i = 0; i < perPublisher; i++) {
                        // de 1 à ~3 000 octets : les grosses écritures se découpent et se croisent plus facilement
                        String payload = id + ":" + i + ":" + "x".repeat((id * 997 + i * 31) % 3000);
                        if (!c.command("PUBLISH", channel, payload).equals(":1")) badReplies.incrementAndGet();
                    }
                }
            });
            String problem = reading.get(60, TimeUnit.SECONDS);
            if (problem != null) {
                IO.println(problem);
                return false;
            }
            int received = counts[0];
            int outOfOrder = counts[1];
            IO.println(received + " messages reçus intacts, " + outOfOrder + " hors d'ordre, "
                    + badReplies.get() + " PUBLISH qui n'ont pas répondu :1");
            return outOfOrder == 0 && badReplies.get() == 0;
        }
    }
}

/**
 * Un abonné s'abonne puis ne lit plus rien. Un éditeur publie 256 messages de 256 Ko (64 Mo en tout). Chaque PUBLISH doit
 * répondre (5 s au plus), un autre client doit toujours obtenir PONG, et l'abonné lent doit finir
 * déconnecté plutôt que de faire grossir la mémoire du serveur sans limite.
 */
static boolean pubsubSlow() throws Exception {
    String channel = "loadtest:lent";
    try (Socket slow = new Socket()) {
        slow.setReceiveBufferSize(4096);
        slow.connect(new java.net.InetSocketAddress(host, port));
        slow.setSoTimeout(5_000);
        InputStream in = new BufferedInputStream(slow.getInputStream());
        slow.getOutputStream().write(("*2\r\n$9\r\nSUBSCRIBE\r\n$" + channel.length() + "\r\n" + channel + "\r\n")
                .getBytes(StandardCharsets.UTF_8));
        slow.getOutputStream().flush();
        readFrame(in); // la confirmation ; ensuite, l'abonné ne lit plus
        String payload = "m".repeat(256 * 1024); // sous la limite de 512 Ko de la séance 4
        int messages = 256;
        long start = System.nanoTime();
        try (Connection publisher = new Connection()) {
            for (int i = 0; i < messages; i++) {
                String reply = publisher.command("PUBLISH", channel, payload);
                if (!reply.startsWith(":")) {
                    IO.println("PUBLISH n° " + (i + 1) + " a répondu " + reply);
                    return false;
                }
            }
        } catch (SocketTimeoutException e) {
            IO.println("un PUBLISH est resté bloqué plus de 5 s : l'éditeur attend l'abonné lent");
            return false;
        }
        IO.println(messages + " PUBLISH de 256 Ko en " + (System.nanoTime() - start) / 1_000_000 + " ms");
        if (!stillAlive()) return false;
        // L'abonné se remet à lire : il doit tomber sur la fin de la connexion avant d'avoir tout reçu
        int received = 0;
        try {
            while (received < messages) {
                readFrame(in);
                received++;
            }
        } catch (IOException e) {
            IO.println("abonné lent déconnecté par le serveur après " + received + " message(s) : ✓");
            return true;
        }
        IO.println("l'abonné lent a tout reçu (" + received / 4 + " Mo gardés en mémoire pour lui) : pas de limite");
        return false;
    }
}

/** Lit une réponse RESP complète : String, Long, null, une erreur "-..." ou une List. */
static Object readFrame(InputStream in) throws IOException {
    int type = in.read();
    if (type == -1) throw new EOFException("connexion fermée par le serveur");
    StringBuilder line = new StringBuilder();
    int c;
    while ((c = in.read()) != '\r') {
        if (c == -1) throw new EOFException("connexion fermée par le serveur");
        line.append((char) c);
    }
    in.read(); // \n
    return switch (type) {
        case '+' -> line.toString();
        case '-' -> "-" + line;
        case ':' -> Long.parseLong(line.toString());
        case '$' -> {
            int length = Integer.parseInt(line.toString());
            if (length < 0) yield null;
            byte[] data = in.readNBytes(length);
            if (data.length < length) throw new EOFException("connexion fermée par le serveur");
            if (in.read() != '\r' || in.read() != '\n') throw new IllegalStateException("\\r\\n manquant après une chaîne");
            yield new String(data, StandardCharsets.UTF_8);
        }
        case '*' -> {
            int count = Integer.parseInt(line.toString());
            List<Object> items = new ArrayList<>();
            for (int i = 0; i < count; i++) items.add(readFrame(in));
            yield items;
        }
        default -> throw new IllegalStateException("type RESP inconnu : " + (char) type);
    };
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
