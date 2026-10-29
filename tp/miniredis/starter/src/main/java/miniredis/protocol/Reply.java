package miniredis.protocol;

import java.util.List;

/**
 * Une réponse du serveur. Le protocole RESP2 en connaît exactement six sortes.
 *
 * <pre>
 * new Reply.Simple("OK")        →  +OK            (affiché OK par valkey-cli)
 * new Reply.Error("ERR ...")    →  -ERR ...       ((error) ERR ...)
 * new Reply.Int(42)             →  :42            ((integer) 42)
 * new Reply.Bulk("bonjour")     →  $7 bonjour     ("bonjour")
 * new Reply.Nil()               →  $-1            ((nil))
 * new Reply.Array(List.of(...)) →  *2 ...         (1) ... 2) ...)
 * </pre>
 *
 * Ce sont des records : deux réponses identiques sont {@code equals}, ce qui rend les tests simples :
 * {@code assertEquals(new Reply.Bulk("v"), processor.execute(List.of("GET", "k")))}.
 *
 * FOURNI — ne pas modifier.
 */
public sealed interface Reply {

    Reply OK = new Simple("OK");
    Reply NIL = new Nil();

    /** Chaîne courte, sans retour à la ligne : OK, PONG… */
    record Simple(String value) implements Reply {}

    /** Erreur. Par convention, le message commence par un code en majuscules : ERR, WRONGTYPE… */
    record Error(String message) implements Reply {}

    /** Entier signé sur 64 bits. */
    record Int(long value) implements Reply {}

    /** Chaîne quelconque (peut contenir des espaces, des retours à la ligne, de l'UTF-8…). */
    record Bulk(String value) implements Reply {}

    /** Absence de valeur : GET sur une clé qui n'existe pas. */
    record Nil() implements Reply {}

    /** Liste de réponses. */
    record Array(List<Reply> items) implements Reply {
        public Array {
            items = List.copyOf(items);
        }
    }
}
