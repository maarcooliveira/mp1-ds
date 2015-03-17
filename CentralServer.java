import models.ValueAndTimeStamp;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Creates a central server that communicates will all servers in order to guarantee different consistent models.
 *
 * @author Bruno, Cassio, Marco
 * @version 1.0
 */
public class CentralServer {

    static String centralId;
    static String centralAddress;
    static int centralPort;
    static int centralMaxDelay;
    static Config config;
    HashMap<Integer, ValueAndTimeStamp> memory;
    static ServerSocket listener;
    static volatile List<String> queueMessage = new LinkedList<String>();
    static int ackCount = 0;
    static volatile boolean allAcksReceived = true;
    static int numServers = 0;
    static volatile int acksToWait = 0;
    static volatile Long mostRecentTimestamp = null;
    static volatile Integer mostRecentValue = null;
    static volatile Long timestampRepair = null;
    static volatile Integer valueRepair = null;
    static SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
    static volatile Integer keyToRepair = null;

    /**
     * Creates a central server, which sends to and receive messages from all the other servers.
     *
     * @param starter the configuration file that shows where all the servers are.
     */
    public CentralServer(Config starter) {
        config = starter;
        numServers = config.getNames().size() - 1;
        centralId = "CENTRAL";
        centralAddress = config.getAddress(centralId);
        centralPort = config.getPort(centralId);
        centralMaxDelay = config.getDelay(centralId);
        memory = new HashMap<Integer, ValueAndTimeStamp>();
        new broadcastManager().start();
        new ServerT().start();
    }

    /**
     * Creates a server thread similar to the one inside Server, but adapted for broadcasting.
     *
     * @author Bruno, Cassio, Marco
     * @version 1.0
     */
    private static class ServerT extends Thread {

        /**
         * Runs the server thread, receiving messages and responding to them accordingly.
         */
        public void run() {
            try {
                listener = new ServerSocket(centralPort);
                String clientName = "";
                while (true) {
                    Socket socket = listener.accept();
                    BufferedReader messageFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message = messageFromClient.readLine();
                    String[] listArg = message.split(" ");

                    if (listArg[0].equals("exit")) {
                        broadcast("exit");
                        System.exit(0);
                    }
                    if (!listArg[0].equals("ack")) {
                        queueMessage.add(message);
                        clientName = listArg[listArg.length - 1];
                    } else {
                        if (listArg[1].equals("get") && ackCount < acksToWait) {
                            if (listArg.length >= 5) {
                                Long receivedGetTimestamp = Long.parseLong(listArg[4]);
                                Integer receivedGetValue = Integer.parseInt(listArg[3]);
                                sendToClient(clientName, "ack get-partial " + receivedGetValue + " " + sdf.format(receivedGetTimestamp));
                                if (mostRecentTimestamp == null || mostRecentTimestamp < receivedGetTimestamp) {
                                    mostRecentTimestamp = receivedGetTimestamp;
                                    mostRecentValue = receivedGetValue;
                                }
                            }
                        }
                        ackCount = (ackCount + 1);
                        if (listArg[1].equals("search")) {
                            if (listArg.length == 3)
                                sendToClient(clientName, "ack search " + listArg[2]);
                        } else if (listArg[1].equals("repair")) {
                            if (listArg.length >= 5) {
                                Long receivedGetTimestamp = Long.parseLong(listArg[4]);
                                Integer receivedGetValue = Integer.parseInt(listArg[3]);
                                if (timestampRepair == null || timestampRepair < receivedGetTimestamp) {
                                    timestampRepair = receivedGetTimestamp;
                                    valueRepair = receivedGetValue;
                                }
                            }
                            if (ackCount == numServers)
                                if (timestampRepair != null) {
                                    broadcast("repair insert " + keyToRepair + " " + valueRepair + " " + timestampRepair);
                                    sendToClient(clientName, "ack key repaired");
                                }
                        } else if (ackCount == acksToWait || (acksToWait == 0 && ackCount == 1)) {
                            String operation = listArg[1];
                            String output = "ack " + operation + " " + listArg[2];

                            if (operation.equals("get")) {
                                if (mostRecentValue != null) {
                                    output += " " + mostRecentValue + " " + sdf.format(mostRecentTimestamp);
                                }
                            } else if (operation.equals("update")) {
                                output += " " + listArg[6];
                            }

                            sendToClient(clientName, output);
                        }

                        if (ackCount == numServers) {
                            allAcksReceived = true;
                            ackCount %= 4;
                            mostRecentTimestamp = null;
                            mostRecentValue = null;
                        }

                    }

                } // End of run loop
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Sends a message back to the client that sent a command (initiating a conversation).
     *
     * @param clientId a String that shows which client to send the message back to.
     * @param message  the content of the message.
     * @throws IOException if the communication with the client is not possible.
     */
    private static void sendToClient(String clientId, String message) throws IOException {
        int port = config.getPort(clientId);
        String address = config.getAddress(clientId);
        Socket socket = null;
        try {
            socket = new Socket(address, port);
            DataOutputStream messageToServer = new DataOutputStream(socket.getOutputStream());
            messageToServer.writeBytes(message);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                socket.close();
            }
        }
    }

    /**
     * Manages how many messages were received and how many messages to look up to based on the model adopted for each
     * command, broadcasting messages when necessary.
     *
     * @author Bruno, Cassio, Marco
     * @version 1.0
     */
    private static class broadcastManager extends Thread {

        /**
         * Runs the broadcast manager, which interprets the command and broadcast messages accordingly.
         */
        public void run() {
            while (true) {
                if (queueMessage.size() > 0 && allAcksReceived) {
                    String message = queueMessage.remove(0);
                    allAcksReceived = false;
                    acksToWait = numServers;
                    String[] listArg = message.split(" ");
                    String cmd = listArg[0];

                    if (cmd.equals("search")) {
                        acksToWait = 4;
                    } else if (cmd.equals("repair")) {
                        acksToWait = 4;
                        keyToRepair = Integer.valueOf(listArg[1]);
                    } else if (!cmd.equals("delete")) {
                        int model = Integer.valueOf(listArg[listArg.length - 2]);
                        if (model == 1)
                            acksToWait = numServers;
                        else if (model == 2)
                            if (cmd.equals("insert") || cmd.equals("update"))
                                acksToWait = numServers;
                            else
                                acksToWait = 0;
                        else if (model == 3)
                            acksToWait = 1;
                        else if (model == 4)
                            acksToWait = 2;
                    } else {
                        acksToWait = 0;
                    }
                    try {
                        broadcast(message);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } // End of run loop
        }
    }

    /**
     * Broadcasts a message across all servers.
     *
     * @param message a String containing the contents of the message.
     * @throws IOException if one of the servers cannot be reached properly.
     */
    public static void broadcast(String message) throws IOException {
        Socket socket = null;

        for (String str : config.getNames()) {
            if (!str.equals("CENTRAL")) {
                int port = config.getPort(str);
                String address = config.getAddress(str);

                try {
                    socket = new Socket(address, port);
                    DataOutputStream messageToServer = new DataOutputStream(socket.getOutputStream());
                    messageToServer.writeBytes(message);

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (socket != null) {
                        socket.close();
                    }
                }
            }
        }
    }

    /**
     * Runs the central server, based on a standard configuration file.
     * <p/>
     * Usage (in the terminal): java CentralServer
     * <p/>
     * If necessary, compile the Java files first with: javac *.java
     *
     * @param args all arguments required for setup. None is required.
     * @throws IOException if it fails to read the configuration file.
     */
    public static void main(String[] args) throws IOException {
        Config config = new Config("config.txt");
        CentralServer centralServer = new CentralServer(config);
    }
}
