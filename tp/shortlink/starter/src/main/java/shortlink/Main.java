package shortlink;

/**
 * La console ShortLink. Lancement : bouton ▶ dans IntelliJ, ou {@code mvn -q compile exec:java}.
 *
 * FOURNI.
 */
public final class Main {

    public static void main(String[] args) {
        Shell shell = new Shell();
        IO.println("ShortLink : tapez help pour l'aide, quit pour quitter.");
        while (true) {
            String line = IO.readln("shortlink> ");
            if (line == null || line.strip().equalsIgnoreCase("quit")) {
                break;
            }
            IO.println(shell.execute(line));
        }
    }
}
