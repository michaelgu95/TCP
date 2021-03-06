import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Kevin on 2/22/2016.
 */
public class HW4Input1_2Server {

    /**
     * Step 1: Be sure to put the files directory into the cwd of your FTP server.
     * Step 2: Start up the FTP server
     * Step 3: Use 1st argument for host address, 2nd for port
     * <p>
     * Step Final: Check in retr_files if you have file1 and file2
     *
     * @param args
     */

    public static final String EOL = System.getProperty("line.separator");
    public static final String CRLF = "\r\n";

    private static String getIPAddr() throws UnknownHostException {
        //Unchecked to stop the program
        return InetAddress.getLocalHost().getHostAddress().replace('.', ',');

    }

    private static String convertPort(int port) {
        /**Singular port number -> 2 number constituents
         *
         * port>>8=integer division by 256
         * port&255=port%256. Finding the remainder
         */

        return String.format("%s,%s", port >> 8, port & 255);

    }

    public static void main(String[] args) throws UnknownHostException {
        String host;
        try {
            host = args[0];

        } catch (IndexOutOfBoundsException e) {
            throw e;
        }

        int port;
        try {
            port = Integer.parseInt(args[1]);

        } catch (IndexOutOfBoundsException e) {
            throw e;

        }

        String ip = getIPAddr();
        String ip_dot = getIPAddr().replace(",", "."); //lazy


        System.out.printf("USER anonymous%s", CRLF);
        System.out.printf("PASS guest@%s", CRLF);
        System.out.printf("SYST%s", CRLF);
        System.out.printf("TYPE I%s", CRLF);

        System.out.printf("PORT %s,%s%s", ip, convertPort(port), CRLF);
        System.out.printf("RETR files/important.txt%s", CRLF);

        System.out.printf("QUIT%s", CRLF);
    }

}
