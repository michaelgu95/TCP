import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Kevin on 2/17/2016.
 */
public class HW4Input1_1 {

    /**Step 1: Be sure to put the files directory into the cwd of your FTP server.
     * Step 2: Start up the FTP server
     * Step 3: Use 1st argument for host address, 2nd for port
     *
     * Step Final: Check in retr_files if you have file1 and file2
     *
     * @param args
     */

    public static final String EOL=System.getProperty("line.separator");
    public static final String CRLF="\r\n";

    private static String getIPAddr() throws UnknownHostException {
        //Unchecked to stop the program
        return InetAddress.getLocalHost().getHostAddress().replace('.',',');

    }

    private static String convertPort(int port) {
        /**Singular port number -> 2 number constituents
         *
         * port>>8=integer division by 256
         * port&255=port%256. Finding the remainder
         */

        return String.format("%s,%s", port>>8,port&255);

    }

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
        String ip=getIPAddr();
        String ip_dot=getIPAddr().replace(",","."); //lazy

        System.out.printf("CONNECT %s %d%s",host,port,EOL);
        System.out.printf("GET files/important.txt%s",EOL);
        System.out.printf("GET files/comp431/!.txt%s",EOL);
        System.out.printf("GET files/one_does_not_simply_get_this_file.txt%s",EOL);
        System.out.printf("QUIT%s",EOL);


    }
}


