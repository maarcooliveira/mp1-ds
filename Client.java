import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.SimpleDateFormat;

/**
 * @author Marco Andre De Oliveira <mdeoliv2@illinois.edu>
 * @version 1.0
 * @since 3/14/15
 */
public class Client {
    public static void main(String[] args) throws IOException {
        String serverAddress = "localhost";
        Socket socket = new Socket(serverAddress, 9091);

        BufferedReader userMessage = new BufferedReader(new InputStreamReader(System.in));
        DataOutputStream messageToServer = new DataOutputStream(socket.getOutputStream());


        BufferedReader messageFromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        String message = userMessage.readLine();
        messageToServer.writeBytes(message);

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

        System.out.println("Sent \"" + message + "\" to x, system time is " + sdf.format(System.currentTimeMillis()));
//        String answer = messageFromServer.readLine();

//        System.exit(0);
    }


}
