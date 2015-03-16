import models.ValueAndTimeStamp;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bruno on 3/15/2015.
 */
public class CentralServer {

    static String centralId;
    static String centralAddress;
    static int centralPort;
    static int centralMaxDelay;
    static Starter config;
    HashMap<Integer, ValueAndTimeStamp> memory;
    static ServerSocket listener;
    List<Socket> listSockets = new ArrayList<Socket>();


    public CentralServer(Starter starter) {
        config = starter;
        centralId = "CENTRAL";
        centralAddress = config.getAddress(centralId);
        centralPort = config.getPort(centralId);
        centralMaxDelay = config.getDelay(centralId);
        memory = new HashMap<Integer, ValueAndTimeStamp>();

        new ServerT().start();

    }

    private static class ServerT extends Thread {

        public void run() {
            try {
                listener = new ServerSocket(centralPort);

                while (true) {
                    Socket socket = listener.accept();
                    BufferedReader messageFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    broadcast(messageFromClient.readLine());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void broadcast(String message) throws IOException {
        Socket socket = null;

        for(String str : config.getNames()){
            if(!str.equals("CENTRAL")){
                int port = config.getPort(str);
                String address = config.getAddress(str);

                try {
                    socket = new Socket(address, port);
                    DataOutputStream messageToServer = new DataOutputStream(socket.getOutputStream());
                    messageToServer.writeBytes(message);

                } catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    if(socket != null) {
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
