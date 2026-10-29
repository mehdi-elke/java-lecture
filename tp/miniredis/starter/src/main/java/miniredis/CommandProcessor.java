package miniredis;

import miniredis.protocol.Reply;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exécute les commandes et conserve les données.
 *
 * La signature de {@link #execute(List)} ne changera jamais : vos tests l'utilisent,
 * et ils doivent continuer à passer quand vous réorganiserez le code (séance 2).
 */
public final class CommandProcessor {

    private final Map<String, String> data = new HashMap<>();

    /**
     * Exécute une commande.
     *
     * @param command les mots de la commande : {@code ["SET", "nom", "Ada"]}. Jamais vide.
     * @return la réponse à renvoyer au client
     */
    public Reply execute(List<String> command) {
        // Le nom de la commande est insensible à la casse : set, SET et Set sont identiques.
        String name = command.getFirst().toUpperCase();
        // Les arguments, sans le nom : ["nom", "Ada"]
        List<String> args = command.subList(1, command.size());

        return switch (name) {
            case "SET" -> set(args);
            case "GET" -> get(args);
            // TODO S1 : PING, ECHO, DEL, EXISTS, STRLEN, APPEND, MGET, INCR, DECR, INCRBY, DECRBY

            // Utilisées par les outils (Compare) et par valkey-cli : ne pas supprimer.
            case "FLUSHALL" -> flushAll(args);
            case "COMMAND" -> new Reply.Array(List.of());
            case "INFO" -> new Reply.Bulk("# Server\r\nredis_version:7.2.4\r\nserver_name:mini-redis\r\n");
            default -> unknownCommand(command);
        };
    }

    // SET clé valeur  →  OK
    private Reply set(List<String> args) {
        if (args.size() != 2) {
            return wrongNumberOfArguments("set");
        }
        data.put(args.get(0), args.get(1));
        return Reply.OK;
    }

    // GET clé  →  la valeur, ou (nil) si la clé n'existe pas
    private Reply get(List<String> args) {
        if (args.size() != 1) {
            return wrongNumberOfArguments("get");
        }
        String value = data.get(args.get(0));
        if (value == null) {
            return Reply.NIL;
        }
        return new Reply.Bulk(value);
    }

    // FLUSHALL  →  OK, et toutes les clés sont supprimées
    private Reply flushAll(List<String> args) {
        if (!args.isEmpty()) {
            return wrongNumberOfArguments("flushall");
        }
        data.clear();
        return Reply.OK;
    }

    /** Erreur renvoyée par Valkey quand le nombre d'arguments est incorrect. */
    private static Reply wrongNumberOfArguments(String commandName) {
        return new Reply.Error("ERR wrong number of arguments for '" + commandName + "' command");
    }

    /** Erreur renvoyée par Valkey pour une commande inconnue (chaque argument entre quotes, suivi d'un espace). */
    private static Reply unknownCommand(List<String> command) {
        StringBuilder message = new StringBuilder("ERR unknown command '")
                .append(command.getFirst())
                .append("', with args beginning with: ");
        for (String arg : command.subList(1, command.size())) {
            message.append('\'').append(arg).append("' ");
        }
        return new Reply.Error(message.toString());
    }
}
