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
                int serverPort = 9090;
                System.out.println("Running client mode, connected to port " + serverPort);
                BufferedReader userMessage = new BufferedReader(new InputStreamReader(System.in));
                String message;
                String serverAddress = "localhost";
                Socket socket = new Socket(serverAddress, serverPort);
                DataOutputStream messageToServer = new DataOutputStream(socket.getOutputStream());

                while(true) {
                    message = userMessage.readLine();
                    messageToServer.writeBytes(message);
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
                    System.out.println("Sent \"" + message + "\" to x, system time is " + sdf.format(System.currentTimeMillis()));
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
                    System.out.println("Received \"" + message + "\" from x, Max delay is " + serverMaxDelay +" s, system time is " + sdf.format(System.currentTimeMillis()));

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
