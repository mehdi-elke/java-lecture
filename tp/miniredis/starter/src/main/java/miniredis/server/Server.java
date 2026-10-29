package miniredis.server;

import miniredis.CommandProcessor;
import miniredis.protocol.Reply;
import miniredis.protocol.RespReader;
import miniredis.protocol.RespWriter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * Le serveur : accepte les connexions et transmet chaque commande au {@link CommandProcessor}.
 *
 * Lancement : {@code mvn compile exec:java}, ou bouton ▶ de l'IDE sur {@code main}.
 * Option : le port en premier argument (6380 par défaut, pour ne pas gêner un vrai Valkey sur 6379).
 *
 * FOURNI. Limite connue : un seul client à la fois (séance 4).
 */
public final class Server {

    public static void main(String[] args) throws IOException {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 6380;
        CommandProcessor processor = new CommandProcessor();

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            IO.println("mini-redis prêt sur le port " + port);
            while (true) {
                try (Socket client = serverSocket.accept()) {
                    handle(client, processor);
                } catch (IOException e) {
                    IO.println("! erreur réseau : " + e.getMessage());
                }
            }
        }
    }

    /** Lit les commandes d'un client jusqu'à ce qu'il se déconnecte. */
    private static void handle(Socket client, CommandProcessor processor) throws IOException {
        IO.println("+ client " + client.getRemoteSocketAddress());
        RespReader reader = new RespReader(client.getInputStream());
        RespWriter writer = new RespWriter(client.getOutputStream());
        List<String> command;
        while ((command = reader.readCommand()) != null) {
            if (command.isEmpty()) {
                continue;
            }
            Reply reply = processor.execute(command);
            IO.println("  " + command + " -> " + reply);
            writer.write(reply);
        }
        IO.println("- client déconnecté");
    }
}
