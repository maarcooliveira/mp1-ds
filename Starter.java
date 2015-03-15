import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Disposable class that reads the configuration file directly.
 *
 * @author Cassio dos Santos Sousa
 * @version 1.0
 * @since 3/14/15
 */
public class Starter {

    private ArrayList<String> names;
    private ArrayList<String> addresses;
    private ArrayList<Integer> ports;
    private ArrayList<Float> delays;
    private ArrayList<Server> servers;

    /**
     * Reads all entries of a configuration file and creates a server for every entry.
     *
     * @throws IOException if the configuration file does not exist.
     */
    public Starter () throws IOException{
        names = new ArrayList<String>();
        addresses = new ArrayList<String>();
        ports = new ArrayList<Integer>();
        delays = new ArrayList<Float>();
        servers = new ArrayList<Server>();
        String configName = "config.txt";
        readConfig(configName);
        createServers();
    }

    /**
     * Reads a configuration file and stores each one of its initialization variables.
     *
     * @param fileName the name of the file being used for configuration.
     * @throws IOException if the file does not exist.
     */
    protected void readConfig(String fileName) throws IOException{
        Scanner sc = new Scanner(new File(fileName));
        while(sc.hasNextLine()){
            String[] line = sc.nextLine().split(" ");
            names.add(line[0]);
            addresses.add(line[1]);
            ports.add(Integer.valueOf(line[2]));
            delays.add(Float.valueOf(line[3]));
        }
    }

    /**
     * Creates a new server for every value stored in the arrays.
     */
    protected void createServers(){
        for(int i=0; i<names.size(); i++){
            servers.add(new Server(names.get(i), addresses.get(i), ports.get(i), delays.get(i)));
        }
    }

}
