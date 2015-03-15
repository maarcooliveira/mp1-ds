import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.LinkedList;

/**
 * Creates a server that sends messages to other servers in a simulated distributed system.
 *
 * @author Bruno, Cassio, Marco
 * @version 1.0
 * @since 3/15/15
 */
public class Server {
    static String serverId;
    static String serverAddress;
    static int serverPort;
    static int serverMaxDelay;
    static Starter config;

    /**
     * Creates a server based on a name and a Starter object will all the specifications.
     *
     * @param starter the Starter object reading the required configuration file.
     * @param name    a name for the server.
     */
    public Server(Starter starter, String name) {
        config = starter;
        serverId = name;
        serverAddress = config.getAddress(name);
        serverPort = config.getPort(name);
        serverMaxDelay = config.getDelay(name);
    }

    /**
     * Starts two threads for the system, one responsible for the client side (sending messages to other servers) and
     * the server side (receiving messages from clients).
     *
     * @throws IOException if it is impossible to read the configuration file or the address/port is invalid.
     */
    public void start() throws IOException {
        System.out.println("Server " + serverId + " started at " + serverAddress + ":" + serverPort);

        new ServerT().start();
        new ClientT().start();
    }

    /**
     * Thread responsible for the client side.
     *
     * @author Bruno, Cassio, Marco
     * @version 1.0
     * @since 3/15/15
     */
    private static class ClientT extends Thread {

        public void run() {
            try {
                BufferedReader userMessage = new BufferedReader(new InputStreamReader(System.in));

                String cmd[];
                Socket socket;
                String destinationAddress;
                int destinationPort;
                DataOutputStream messageToServer;
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

                while (true) {
                    cmd = userMessage.readLine().split(" ");

                    if (cmd.length != 3) {
                        System.out.println("Use the format: <command> <message> <destinationServerName>");
                    } else {
                        String message = cmd[1];
                        String destinationName = cmd[2].toUpperCase();
                        destinationAddress = config.getAddress(destinationName);
                        destinationPort = config.getPort(destinationName);

                        socket = new Socket(destinationAddress, destinationPort);
                        messageToServer = new DataOutputStream(socket.getOutputStream());
                        messageToServer.writeBytes(destinationName + " " + message);

                        System.out.println("Sent \"" + message + "\" to " + destinationName + ", system time is "
                                + sdf.format(System.currentTimeMillis()));

                        messageToServer.close();
                        socket.close();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                System.out.println("Client closed");
            }
        }
    }

    /**
     * Thread responsible for the server side.
     *
     * @author Bruno, Cassio, Marco
     * @version 1.0
     * @since 3/15/15
     */
    private static class ServerT extends Thread {


        public void run() {
            try {
                ServerSocket listener = new ServerSocket(serverPort);
                MessageT mt = new MessageT();
                mt.start();

                while (true) {
                    Socket socket = listener.accept();
                    BufferedReader messageFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    String[] inputs = messageFromClient.readLine().split(" ");
                    Message message = new Message(inputs, System.currentTimeMillis() + config.setDelay(serverMaxDelay) * 1000);

                    mt.add(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
        }
    }

    /**
     * Message object that keeps track of its content (as a String array) and timestamp.
     *
     * @author Bruno, Cassio, Marco
     * @version 1.0
     * @since 3/15/15
     */
    private static class Message {
        String[] msg;
        long time;

        public Message(String[] msg, long time) {
            this.msg = msg;
            this.time = time;
        }
    }

    /**
     * Thread that handles the messages being received in a server to guarantee FIFO ordering.
     *
     * @author Bruno, Cassio, Marco
     * @version 1.0
     * @since 3/15/15
     */
    private static class MessageT extends Thread {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        volatile LinkedList<Message> list = new LinkedList<Message>();

        public void run() {
            while (true) {
                if (list.size() > 0) {
                    if (System.currentTimeMillis() >= list.get(0).time) {
                        String[] msg = list.get(0).msg;
                        list.remove(0);

                        String origin = msg[0];
                        String message = msg[1];

                        System.out.println("Received \"" + message + "\" from " + origin + ", Max delay is " + serverMaxDelay
                                + "s, system time is " + sdf.format(System.currentTimeMillis()));
                    }
                }
            }
        }

        public void add(Message msg) {
            list.push(msg);
        }
    }
}
