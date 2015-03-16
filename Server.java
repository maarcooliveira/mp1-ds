import models.ValueAndTimeStamp;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;

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
    static volatile HashMap<Integer, ValueAndTimeStamp> memory;

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
        memory = new HashMap<Integer, ValueAndTimeStamp>();
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
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        Socket socket;
        String destinationAddress;
        int destinationPort;
        DataOutputStream messageToServer;

        public void run() {
            try {
                BufferedReader userMessage = new BufferedReader(new InputStreamReader(System.in));

                while (true) {
                    executeCommand(userMessage.readLine());
                }

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
        }

        private void executeCommand(String command) throws IOException {
            String[] cmd = command.split(" ");
            if (cmd[0].equals("send")) {
                if (cmd.length == 3) {
                    String message = cmd[1];
                    String destinationName = cmd[2].toUpperCase();
                    destinationAddress = config.getAddress(destinationName);
                    destinationPort = config.getPort(destinationName);

                    socket = new Socket(destinationAddress, destinationPort);
                    messageToServer = new DataOutputStream(socket.getOutputStream());
                    messageToServer.writeBytes(serverId + " " + message);

                    System.out.println("Sent \"" + message + "\" to " + destinationName + ", system time is "
                            + sdf.format(System.currentTimeMillis()));

                    messageToServer.close();
                    socket.close();
                } else {
                    System.out.println("Invalid format. Try: send <message> <destinationServerName>");
                }
            } else if (cmd[0].equals("delete")) {
                if (cmd.length == 2) {
                    sendToCentral(command + " " + serverId);
                } else {
                    System.out.println("Invalid format. Try: delete <key>");
                }
            } else if (cmd[0].equals("get")) {
                if (cmd.length == 3) {
                    if(cmd[cmd.length - 1].equals("2")){
                        int key = Integer.valueOf(cmd[1]);
                        int val = memory.get(key).getValue();
                        System.out.println("get(" + key + ") = " + val);
                    }
                    else
                        sendToCentral(command + " " + serverId);
                } else {
                    System.out.println("Invalid format. Try: get <key> <model>");
                }
            } else if (cmd[0].equals("insert")) {
                if (cmd.length == 4) {
                    sendToCentral(command + " " + serverId);
                } else {
                    System.out.println("Invalid format. Try: insert <key> <value> <model>");
                }
            } else if (cmd[0].equals("update")) {
                if (cmd.length == 4) {
                    sendToCentral(command + " " + serverId);
                } else {
                    System.out.println("Invalid format. Try: update <key> <value> <model>");
                }
            } else if (cmd[0].equals("show-all")) {
                if (cmd.length == 1) {
                    for (Integer key : memory.keySet()) {
                        System.out.println(key + " : " + memory.get(key).getValue());
                    }
                } else {
                    System.out.println("Invalid format. Try: show-all");
                }
            } else if (cmd[0].equals("search")) {
                if (cmd.length == 2) {
                    sendToCentral(command);
                } else {
                    System.out.println("Invalid format. Try: search <key>");
                }
            } else if (cmd[0].equals("delay")) {
                if (cmd.length == 2) {
                    sendToCentral(command);
                } else {
                    System.out.println("Invalid format. Try: delay <seconds>");
                }
            } else if (cmd[0].equals("--help")) {
                if (cmd.length == 1)
                    askForHelp();
                else
                    System.out.println("Invalid format. Try: --help");
            } else {
                System.out.println("Invalid command. Try: --help");
            }
        }

        /**
         * Prints out all commands after a user requested the command --help, shown after typing an invalid command.
         */
        protected void askForHelp() {
            System.out.println("Valid commands for the key-value interface:");
            System.out.println("");
            System.out.println("    delay <seconds>");
            System.out.println("    delete <key>");
            System.out.println("    get <key> <model>");
            System.out.println("    insert <key> <value> <model>");
            System.out.println("    update <key> <value> <model>");
            System.out.println("    search <key>");
            System.out.println("    send <message> <destinationServerName>");
            System.out.println("    show-all");
            System.out.println("    --help");
            System.out.println("");
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
        long maxTime = 0;

        public void run() {
            try {
                ServerSocket listener = new ServerSocket(serverPort);
                MessageT mt = new MessageT();
                mt.start();

                while (true) {
                    Socket socket = listener.accept();
                    BufferedReader messageFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String[] inputs = messageFromClient.readLine().split(" ");
                    long time = Math.max(System.currentTimeMillis() + config.setDelay(serverMaxDelay) * 1000, maxTime);
                    maxTime = time;
                    Message message = new Message(inputs, time);
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

                        processMessage(msg);
                    }
                }
            }
        }

        public void add(Message msg) {
            list.push(msg);
        }

        private void processMessage(String[] msg) {
            if (msg[0].equals("delete")) {
                int key = Integer.valueOf(msg[1]);
                memory.remove(key);

                System.out.println("Key " + key + " deleted");
                sendToCentral("ack delete " + key);

            } else if (msg[0].equals("get")) {
                int key = Integer.valueOf(msg[1]);
                int value;
                long timestamp;

                ValueAndTimeStamp val = memory.get(key);
                value = val.getValue();
                timestamp = val.getTimeStamp();

                sendToCentral("ack get " + key + " " + value + " " + timestamp);

            } else if (msg[0].equals("insert")) {
                int key = Integer.valueOf(msg[1]);
                int value = Integer.valueOf(msg[2]);

                ValueAndTimeStamp val = new ValueAndTimeStamp(value, System.currentTimeMillis());
                memory.put(key, val);

                System.out.println("Inserted key " + key);
                sendToCentral("ack insert " + key + " " + value);

            } else if (msg[0].equals("update")) {
                int key = Integer.valueOf(msg[1]);
                int value = Integer.valueOf(msg[2]);
                int oldValue = 0;

                ValueAndTimeStamp oldVal = memory.get(key);
                oldValue = oldVal.getValue();

                ValueAndTimeStamp newVal = new ValueAndTimeStamp(value, System.currentTimeMillis());
                memory.replace(key, oldVal, newVal);

                System.out.println("Key " + key + " changed from " + oldValue + " to " + value);
                sendToCentral("ack update " + key + " from " + oldValue + " to " + value);

            } else if (msg[0].equals("search")) {
                int value = Integer.valueOf(msg[1]);
                if(memory.containsKey(value))
                    sendToCentral("ack search " + serverId);

            } else if (msg[0].equals("delay")) {
                //TODO: delay
            } else if (msg[0].equals("ack")) {
                String operation = msg[1];
                if(operation.equals("get"))
                    System.out.println("get(" + msg[2] + ") = (" + msg[3] + "," + msg[4] + ")");
                else if(operation.equals("update"))
                    System.out.println("Key " + msg[2] + " updated to " + msg[3]);
                else if(operation.equals("get-partial"))
                    System.out.println(msg[2] + ", " + msg[3]);
                else {
                    for (String m : msg) {
                        System.out.print(m + " ");
                    }
                    System.out.println();
                }
            } else { // Is a "send" command
                String origin = msg[0];
                String message = msg[1];

                System.out.println("Received \"" + message + "\" from " + origin + ", Max delay is " + serverMaxDelay
                        + "s, system time is " + sdf.format(System.currentTimeMillis()));
            }
        }
    }

    private static void sendToCentral(String message) {
        String destinationName = "CENTRAL";
        String destinationAddress = config.getAddress(destinationName);
        int destinationPort = config.getPort(destinationName);

        Socket socket = null;
        try {
            socket = new Socket(destinationAddress, destinationPort);
            DataOutputStream messageToServer = new DataOutputStream(socket.getOutputStream());
            messageToServer.writeBytes(message);

//            BufferedReader messageFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
//            String[] inputs = messageFromServer.readLine().split(" ");

//            System.out.println(messageFromServer.readLine());

            messageToServer.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
