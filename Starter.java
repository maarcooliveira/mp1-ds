import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

/**
 * Reads the configuration file and sets the specifications for every server detailed on file.
 *
 * @author Cassio dos Santos Sousa <dssntss2@illinois.edu>
 * @version 1.0
 * @since 3/15/15
 */
public class Starter {

    private ArrayList<String> names;
    private ArrayList<String> addresses;
    private ArrayList<Integer> ports;
    private ArrayList<Integer> delays;

    /**
     * Reads all entries of a configuration file and creates a server for every entry.
     *
     * @throws IOException if the configuration file does not exist.
     */
    public Starter(String configName) throws IOException {
        names = new ArrayList<String>();
        addresses = new ArrayList<String>();
        ports = new ArrayList<Integer>();
        delays = new ArrayList<Integer>();
        readConfig(configName);
    }

    public ArrayList<String> getNames() {
        return names;
    }

    /**
     * Reads a configuration file and stores each one of its initialization variables.
     *
     * @param fileName the name of the file being used for configuration.
     * @throws IOException if the file does not exist.
     */
    protected void readConfig(String fileName) throws IOException {
        Scanner sc = new Scanner(new File(fileName));
        while (sc.hasNextLine()) {
            String[] line = sc.nextLine().split(" ");
            names.add(line[0]);
            addresses.add(line[1]);
            ports.add(Integer.valueOf(line[2]));
            delays.add(Integer.valueOf(line[3]));
        }
    }

    /**
     * Gets the address of a server given its name, or <em>null</em> otherwise.
     *
     * @param serverName the server name.
     * @return a string containing the address, or <em>null</em>.
     */
    protected String getAddress(String serverName) {
        if (names.contains(serverName))
            return addresses.get(names.indexOf(serverName));
        else return null;
    }

    /**
     * Gets the port number of a server given its name, or <em>null</em> otherwise.
     *
     * @param serverName the server name.
     * @return an integer containing the server port, or <em>null</em>.
     */
    protected Integer getPort(String serverName) {
        if (names.contains(serverName))
            return ports.get(names.indexOf(serverName));
        else return null;
    }

    /**
     * Gets the delay of a server given its name, or <em>null</em> otherwise.
     *
     * @param serverName the server name.
     * @return a float point number of the server's delay, or <em>null</em>.
     */
    protected Integer getDelay(String serverName) {
        if (names.contains(serverName))
            return delays.get(names.indexOf(serverName));
        else return null;
    }

    /**
     * Sets a random delay time (in seconds) to a given message, based on a maximum value set for the server.
     *
     * @param maxDelay the current maximum delay for the server.
     * @return a random integer between 0 (inclusive) and maxDelay (inclusive).
     */
    protected int setDelay(int maxDelay) {
        return new Random().nextInt(maxDelay + 1);
    }

    /**
     * Reads an input file, returning each line of it as a different command for the key-value system.*
     *
     * @param fileName the name and location of the input file.
     * @return an ArrayList of strings for each input.
     * @throws IOException if the input file cannot be found.
     */
    protected ArrayList<String> readInput(String fileName) throws IOException {
        ArrayList<String> allInputs = new ArrayList<String>();
        Scanner sc = new Scanner(new File(fileName));
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            allInputs.add(line);
        }
        return allInputs;
    }

    /**
     * Applies a delay to the server. Being a thread, it only means the server may be put to sleep.
     *
     * @param timeInSeconds a floating point number containing the total delay time in seconds.
     */
//    private void delay(float timeInSeconds) {
//        sleep(timeInSeconds * 1000.0);
//    }
}
