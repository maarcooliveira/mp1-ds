import java.io.IOException;

/**
 * Created by Marco Andre De Oliveira <mdeoliv2@illinois.edu>
 * Date: 3/14/15
 */
public class Main {

    public static void main(String[] args) throws IOException {
        String name = args[0];
        String address = args[1];
        int port = Integer.valueOf(args[2]);
        int delay = Integer.valueOf(args[3]);

        Server s = new Server(name, address, port, delay);
        s.start();

    }
}
