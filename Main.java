import java.io.IOException;

/**
 * Creates a new server that listens to a given port of a given address. Initial delay is also set.
 *
 * @author Marco Andre De Oliveira <mdeoliv2@illinois.edu>
 * @version 1.0
 */
public class Main {

    /**
     * Creates a new server based on the configurations shown in the config.txt file.
     * <p/>
     * Usage (in the terminal): java Main [serverName] Example: java Main A
     * <p/>
     * If necessary, compile the Java files first with: javac *.java
     *
     * @param args all arguments required to create a server (4 in total).
     * @throws IOException if tried to read a configuration file that does not exist.
     */
    public static void main(String[] args) throws IOException {

        if (args.length != 1) {
            System.out.println("Usage: java Main [serverName]");
            return;
        }

        String name = args[0];
        Server s = new Server(new Config("config.txt"), name);
        s.start();

    }
}
