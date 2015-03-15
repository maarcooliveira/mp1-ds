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
        ServerSocket listener = new ServerSocket(serverPort);

        try {

            while (true) {
                new ServerT(listener.accept()).start();
                new ClientT().start();
            }
        }
        finally {
            listener.close();
        }
    }

    private static class ClientT extends Thread {

        public void run() {
            try {
                BufferedReader userMessage = new BufferedReader(new InputStreamReader(System.in));
                String message = userMessage.readLine();
                while(message != null) {
                    String serverAddress = "localhost";
                    Socket socket = new Socket(serverAddress, 9090);
                    DataOutputStream messageToServer = new DataOutputStream(socket.getOutputStream());

                    messageToServer.writeBytes(message);

                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

                    System.out.println("Sent \"" + message + "\" to x, system time is " + sdf.format(System.currentTimeMillis()));
                    message = userMessage.readLine();
                }


            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                System.out.println("Client closed");
            }
        }
    }


    private static class ServerT extends Thread {
        private Socket socket;

        public ServerT(Socket socket) {
            this.socket = socket;
            System.out.println("Connection with new client established");
        }

        public void run() {
            try {

                BufferedReader messageFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

                String message = messageFromClient.readLine();

//                if (message.split(" ")[2].equals(serverId)){
                    sleep(serverMaxDelay*1000);
                    System.out.println("Received \"" + message + "\" from x, Max delay is " + serverMaxDelay +" s, system time is " + sdf.format(System.currentTimeMillis()));
//                }


            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                System.out.println("Connection with client closed");
            }
        }
    }
}
