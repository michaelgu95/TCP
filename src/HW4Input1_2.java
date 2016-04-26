import java.net.UnknownHostException;

/**
 * Created by Kevin on 2/22/2016.
 */
public class HW4Input1_2 {

    /**Step 1: Be sure to put the files directory into the cwd of your FTP server.
     * Step 2: Start up the FTP server
     * Step 3: Use 1st argument for host address, 2nd for port
     *
     * Step Final: Check in retr_files if you have file1 and file2
     *
     * @param args
     */

    public static final String EOL=System.getProperty("line.separator");

    public static void main(String[] args) throws UnknownHostException {
        String host;
        try {
            host = args[0];

        } catch (IndexOutOfBoundsException e) {
            System.err.println("Please input a valid host for the connection or hardcode the host");
            throw e;
        }

        int port;
        try {
            port = Integer.parseInt(args[1]);

        } catch (IndexOutOfBoundsException e) {
            System.err.println("Please input a valid port for the connection");
            throw e;

        }

        System.out.printf("badconnect valid 1%s",EOL);

        System.out.printf("GET I/am/valid%s",EOL);

        System.out.printf("CONNECT %s %d%s",host,port,EOL);

        System.out.printf("GET files/important.txt%s",EOL);

        System.out.printf("QUIT%s",EOL);

    }
}
