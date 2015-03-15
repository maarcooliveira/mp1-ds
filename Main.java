import java.io.IOException;

/**
 * Creates a new server that listens to a given port of a given address. Initial delay is also set.
 *
 * @author Marco Andre De Oliveira <mdeoliv2@illinois.edu>
 * @version 1.0
 * @since 3/14/15
 */
public class Main {

    /**
     * Creates a new server. The delay unit is seconds.
     *
     * Usage (in the terminal): java Main [serverName] [IpAddress] [port] [delay]
     * Example: java Main A localhost 8080 2
     *
     * @param args all arguments required to create a server (4 in total).
     * @throws IOException if tried to listen to a port or address that does not exist.
     */
    public static void main(String[] args) throws IOException {

        if (args.length != 4) {
            System.out.println("Usage: java Main [serverName] [IpAddress] [port] [delay]");
            return;
        }

        String name = args[0];
        String address = args[1];
        int port = Integer.valueOf(args[2]);
        float delay = Float.valueOf(args[3]);

        Server s = new Server(name, address, port, delay);
        s.start();

    }
}
