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
    static Starter config;
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
    static volatile boolean inconsistencyRepair = false;
    static volatile Integer keyToRepair = null;

    public CentralServer(Starter starter) {
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

    private static class ServerT extends Thread {

        public void run() {
            try {
                listener = new ServerSocket(centralPort);
                PrintWriter messageToClient = null;
                String clientName = "";
                while (true) {
                    Socket socket = listener.accept();
                    BufferedReader messageFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message = messageFromClient.readLine();
                    String[] listArg = message.split(" ");

                    if(listArg[0].equals("exit")) {
                        broadcast("exit");
                        System.exit(0);
                    }
                    if (!listArg[0].equals("ack")) {
                        queueMessage.add(message);
                        clientName = listArg[listArg.length - 1];
                    } else {
                        if (listArg[1].equals("get") && ackCount < acksToWait) {
                            if(listArg.length >= 5) {
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
                            if(listArg.length == 3)
                                sendToClient(clientName, "ack search " + listArg[2]);
                        }
                        else if (listArg[1].equals("repair")) {
                            if(listArg.length >= 5) {
                                Long receivedGetTimestamp = Long.parseLong(listArg[4]);
                                Integer receivedGetValue = Integer.parseInt(listArg[3]);
                                if (timestampRepair == null || timestampRepair < receivedGetTimestamp) {
                                    timestampRepair = receivedGetTimestamp;
                                    valueRepair = receivedGetValue;
                                }
                            }
                            if(ackCount == numServers)
                                if(timestampRepair != null) {
                                    broadcast("repair insert " + keyToRepair + " " + valueRepair + " " + timestampRepair);
                                    sendToClient(clientName, "ack key repaired");
                                }
                        }
                        else if (ackCount == acksToWait || (acksToWait == 0 && ackCount == 1)) {
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

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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

    private static class broadcastManager extends Thread {

        public void run() {
            while (true) {
                if (queueMessage.size() > 0 && allAcksReceived) {
                    String message = queueMessage.remove(0);
                    allAcksReceived = false;
                    acksToWait = numServers;
                    String[] listArg = message.split(" ");
                    String cmd = listArg[0];

                    if(cmd.equals("search")) {
                        acksToWait = 4;
                    }
                    else if(cmd.equals("repair")) {
                        acksToWait = 4;
                        keyToRepair = Integer.valueOf(listArg[1]);
                    }
                    else if (!cmd.equals("delete")) {
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
            }
        }

    }

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

    public static void main(String[] args) throws IOException {
        Starter starter = new Starter("config.txt");
        CentralServer centralServer = new CentralServer(starter);
    }
}
