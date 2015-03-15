import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;

/**
 * Created by Marco Andre De Oliveira <mdeoliv2@illinois.edu>
 * Date: 3/14/15
 */
public class Server {
    static String serverId;
    static String serverAddress;
    static int serverPort;
    static int serverMaxDelay;

    public Server(String id, String address, int port, int delay) {
        this.serverId = id;
        this.serverAddress = address;
        this.serverPort = port;
        this.serverMaxDelay = delay;
    }

    public void start() throws IOException {
        System.out.println("Server " + serverId + " started at " + serverAddress + ":" + serverPort);

        new ServerT().start();
        new ClientT().start();

    }

    private static class ClientT extends Thread {

        public void run() {
            try {
                int clientToPort;
                BufferedReader userMessage = new BufferedReader(new InputStreamReader(System.in));
                String cmd[];
                String serverAddress = "localhost";
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

                Socket socket;
                DataOutputStream messageToServer;

                while(true) {
                    cmd = userMessage.readLine().split(" ");

                    if(cmd.length != 3) {
                        System.out.println("Use the format: <command> <message> <destinationServerName>");
                    }
                    String message = cmd[1];
                    String toServer = cmd[2].toUpperCase();
                    if(toServer.equals("A")) {
                        clientToPort = 9090;
                    }
                    else if(toServer.equals("B")) {
                        clientToPort = 9091;
                    }
                    else if(toServer.equals("C")) {
                        clientToPort = 9092;
                    }
                    else { //It's D server
                        clientToPort = 9093;
                    }

                    socket = new Socket(serverAddress, clientToPort);
                    messageToServer = new DataOutputStream(socket.getOutputStream());
                    messageToServer.writeBytes(message);

                    System.out.println("Sent \"" + message + "\" to " + toServer + ", system time is " + sdf.format(System.currentTimeMillis()));

                    messageToServer.close();
                    socket.close();
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

                while(true){
                    Socket socket = listener.accept();
                    BufferedReader messageFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

                    String message = messageFromClient.readLine();

                    sleep(serverMaxDelay*1000);
                    System.out.println("Received \"" + message + "\" from x, max delay is " + serverMaxDelay +"s, system time is " + sdf.format(System.currentTimeMillis()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                System.out.println("Connection with client closed");
            }
        }
    }
}
