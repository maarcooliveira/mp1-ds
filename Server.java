import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Marco Andre De Oliveira <mdeoliv2@illinois.edu>
 * Date: 3/14/15
 */
public class Server {
    static String serverId;
    static String serverAddress;
    static int serverPort;
    static int serverMaxDelay;
    static Starter config;
    static ArrayList<Long> delayList = new ArrayList<Long>();

    public Server(Starter starter, String name) {
        config = starter;
        serverId = name;
        serverAddress = config.getAddress(name);
        serverPort = config.getPort(name);
        serverMaxDelay = config.getDelay(name);
    }

    public void start() throws IOException {
        System.out.println("Server " + serverId + " started at " + serverAddress + ":" + serverPort);

        new ServerT().start();
        new ClientT().start();
    }

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

                while(true) {
                    cmd = userMessage.readLine().split(" ");

                    if(cmd.length != 3) {
                        System.out.println("Use the format: <command> <message> <destinationServerName>");
                    }
                    else {
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


    private static class ServerT extends Thread {


        public void run() {
            try {
                ServerSocket listener = new ServerSocket(serverPort);
                MessageT mt = new MessageT();
                mt.start();

                while(true) {
                    Socket socket = listener.accept();
                    BufferedReader messageFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    String[] inputs = messageFromClient.readLine().split(" ");
                    Message message = new Message(inputs, System.currentTimeMillis() + config.setDelay(serverMaxDelay)*1000);

                    mt.add(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
            }
        }
    }

    private static class Message {
        String[] msg;
        long time;

        public Message(String[] msg, long time) {
            this.msg = msg;
            this.time = time;
        }
    }

    private static class MessageT extends Thread {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
        volatile LinkedList<Message> list = new LinkedList<Message>();

        public void run() {
            while(true) {
                if(list.size() > 0) {
                    if(System.currentTimeMillis() >= list.get(0).time) {
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
