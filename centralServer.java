import models.ValueAndTimeStamp;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
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
    static volatile List<String> queueMessage = new LinkedList<String>();
    static int ackCount = 0;
    static volatile boolean allAcksReceived = true;
    static int numServers = 2;
    static volatile int acksToWait = 0;
    static Long mostRecentTimestamp = null;
    static Integer mostRecentValue = null;

    public CentralServer(Starter starter) {
        config = starter;
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
                while (true) {
                    Socket socket = listener.accept();
                    BufferedReader messageFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    String message = messageFromClient.readLine();
                    String[] listArg = message.split(" ");
                    if(!listArg[0].equals("ack")) {
                        queueMessage.add(message);
                        messageToClient = new PrintWriter(socket.getOutputStream(), true);
                    } else {
                        if(listArg[1].equals("get")){
                            Long receivedGetTimestamp = Long.parseLong(listArg[4]);
                            Integer receivedGetValue = Integer.parseInt(listArg[3]);
                            System.out.println( "Timestamp: " + receivedGetTimestamp + " Value: " + receivedGetValue);
                            if(mostRecentTimestamp == null || mostRecentTimestamp < receivedGetTimestamp){
                                mostRecentTimestamp = receivedGetTimestamp;
                                mostRecentValue = receivedGetValue;
                            }
                        }
                        ackCount = (ackCount + 1)%4;
                        if (ackCount == 0){
                            allAcksReceived = true;
                        }
                        if(ackCount == acksToWait) {
                            String operation = listArg[1];
                            String output = "ack " + operation + " " + (operation.equals("get")? mostRecentValue : "");
                            messageToClient.println(output);
                        }
                        if(ackCount == numServers) {
                            allAcksReceived = true;
                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static class broadcastManager extends Thread{

        public void run(){
            while(true){
                if(queueMessage.size() > 0 && allAcksReceived){
                    String message = queueMessage.remove(0);
                    allAcksReceived = false;
                    acksToWait = numServers;
                    String[] listArg = message.split(" ");
                    String cmd = listArg[0];
                    if(!cmd.equals("delete")) {
                        int model = Integer.valueOf(listArg[listArg.length-1]);
                        if(model == 1)
                            acksToWait = numServers;
                        else if(model == 2)
                           if(cmd.equals("insert") || cmd.equals("update"))
                               acksToWait = numServers;
                           else
                               acksToWait = 0;
                        else if(model == 3)
                            acksToWait = 1;
                        else if(model == 4)
                            acksToWait = 2;
                    }
                    else{
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
