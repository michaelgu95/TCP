import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Kevin on 2/22/2016.
 */
public class HW4Output1_2 {
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

        System.out.printf("badconnect valid 1%s",EOL);
            System.out.printf("ERROR -- request%s",EOL);
        System.out.printf("GET I/am/valid%s",EOL);
            System.out.printf("ERROR -- expecting CONNECT%s",EOL);

        System.out.printf("CONNECT %s %d%s",host,port,EOL);
            System.out.printf("CONNECT accepted for FTP server at host %s and port %d%s",host,port,EOL);
            System.out.printf("FTP reply 220 accepted. Text is : COMP 431 FTP server ready.%s",EOL);
        System.out.printf("USER anonymous%s",CRLF);
            System.out.printf("FTP reply 331 accepted. Text is : Guest access OK, send password.%s",EOL);
        System.out.printf("PASS guest@%s",CRLF);
            System.out.printf("FTP reply 230 accepted. Text is : Guest login OK.%s",EOL);
        System.out.printf("SYST%s",CRLF);
            System.out.printf("FTP reply 215 accepted. Text is : UNIX Type: L8.%s",EOL);
        System.out.printf("TYPE I%s",CRLF);
            System.out.printf("FTP reply 200 accepted. Text is : Type set to I.%s",EOL);

        System.out.printf("GET files/important.txt%s",EOL);
            System.out.printf("GET accepted for files/important.txt%s",EOL);
        System.out.printf("PORT %s,%s%s",ip,convertPort(port),CRLF);
            System.out.printf("FTP reply 200 accepted. Text is : Port command successful (%s,%d).%s",ip_dot,port++,EOL);
        System.out.printf("RETR files/important.txt%s",CRLF);
            System.out.printf("FTP reply 150 accepted. Text is : File status okay.%s",EOL);
            System.out.printf("FTP reply 250 accepted. Text is : Requested file action completed.%s",EOL);

        System.out.printf("QUIT%s",EOL);
            System.out.printf("QUIT accepted, terminating FTP client%s",EOL);
            System.out.printf("QUIT%s",CRLF);
            System.out.printf("FTP reply 221 accepted. Text is : Goodbye.%s",EOL);

    }
}
