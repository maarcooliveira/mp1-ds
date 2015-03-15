import models.ValueAndTimeStamp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Bruno on 3/15/2015.
 */
public class centralServer {

    static String centralId;
    static String centralAddress;
    static int centralPort;
    static int centralMaxDelay;
    static Starter config;
    HashMap<Integer, ValueAndTimeStamp> memory;
    ServerSocket listener;
    List<Socket> listSockets = new ArrayList<Socket>();


    public centralServer(Starter starter) {
        config = starter;
        centralId = "CENTRAL";
        centralAddress = config.getAddress(centralId);
        centralPort = config.getPort(centralId);
        centralMaxDelay = config.getDelay(centralId);
        memory = new HashMap<Integer, ValueAndTimeStamp>();
        try {
            listener = new ServerSocket(centralPort);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(String str : config.getNames()){
            if(!str.equals("CENTRAL")){
                int port = config.getPort(str);
                String address = config.getAddress(str);
                try {
                    Socket socket = new Socket(address, port);
                    listSockets.add(socket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
