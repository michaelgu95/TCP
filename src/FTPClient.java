import java.net.*;
import java.io.*;
import java.util.Scanner;
import java.util.StringTokenizer;
//Michael Gu

public class FTPClient {
	private static Scanner sc;
	private static final String CRLF = "\r\n";
	private static final String LF = System.getProperty("line.separator");

	public static void main(String[] args) throws IOException{
		System.setProperty("line.separator", CRLF);
		if(args[0] != null){
			Integer dataPortNum = Integer.parseInt(args[0]);
			Socket clientSocket = null;
			BufferedWriter outToServer = null;
			LineReader inFromServer = null;
			boolean connected = false;
			int retrCount = 0;

			sc = new Scanner(System.in);
			sc.useDelimiter("(?<=(\r\n|\n|(\r(?!\n))))");
			while(sc.hasNext()){
				String line = sc.next();
				System.out.printf(line, LF);
				//get the number of characters in LF to help when checking for space at end
				int numCharInLF = 1;
				if(line.charAt(line.length()-2) == '\r'){
					numCharInLF = 2;
				}
				if(line.charAt(0) == ' '){
					System.out.printf("ERROR -- request%s", LF);
				}else{
					//convert the line into a token
					StringTokenizer tokenizedLine = new StringTokenizer(line);
					String command = tokenizedLine.nextToken();

					if(command.equalsIgnoreCase("CONNECT")){


						if(tokenizedLine.hasMoreTokens()){
							String serverHost = tokenizedLine.nextToken();
							if(serverHost.matches("^[a-zA-Z0-9]+.[a-zA-Z0-9]+(.[a-zA-Z0-9]+(.[a-zA-Z0-9]+)?)?$") && serverHost.length() > 1 ){
								if(tokenizedLine.hasMoreTokens()){
									Integer serverPort = Integer.parseInt(tokenizedLine.nextToken());
									if(0<= serverPort && serverPort <=65535){
										//a token or a space after the server-port -> server-port error
										if(tokenizedLine.hasMoreTokens() || line.charAt(line.length()-numCharInLF-1) == ' '){
											System.out.printf("ERROR -- server-port%s",LF);
										}else{
											System.out.printf("CONNECT accepted for FTP server at host " + serverHost+ " and port " + serverPort + "%s", LF);
											//create socket
											try {
												clientSocket = new Socket(serverHost, serverPort);

												//close previous TCP connections if connected
												if(connected == true && outToServer != null && !clientSocket.isClosed()){
													outToServer.close();
													inFromServer.close();
												}

												//stream to server
												outToServer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

												//data stream back from server
												inFromServer = new LineReader(new BufferedReader(new InputStreamReader(clientSocket.getInputStream())));

												//handle initial response from server
												String res = inFromServer.readLine();
												handleServerRes(line, res);

												//send USER
												System.out.printf("USER anonymous%s", CRLF);
												outToServer.write("USER anonymous" + "\r\n");
												outToServer.flush();
												try{
													handleServerRes(line, inFromServer.readLine());

													//send PASS
													System.out.printf("PASS guest@%s", CRLF);
													outToServer.write("PASS guest@" + "\r\n");
													outToServer.flush();
													handleServerRes(line, inFromServer.readLine());

													//send SYST
													System.out.printf("SYST%s", CRLF);
													outToServer.write("SYST" + "\r\n");
													outToServer.flush();
													handleServerRes(line, inFromServer.readLine());

													//send TYPE
													System.out.printf("TYPE I%s", CRLF);
													outToServer.write("TYPE I" + "\r\n");
													outToServer.flush();
													handleServerRes(line, inFromServer.readLine());

													connected = true;
												}catch(RuntimeException e){
													e.printStackTrace();
													connected = false;
												}
											} catch (UnknownHostException e1) {
												System.out.printf("CONNECT failed%s", LF);

											} catch (IOException e1) {
												System.out.printf("CONNECT failed%s", LF);
											} catch(Exception e){
												System.out.printf("CONNECT failed%s", LF);
											}
										}
									}else{
										System.out.printf("ERROR -- server-port%s", LF);
									}
								}else{
									//existing serverPort that was just spaces -> serverPort error
									if(line.charAt(line.length()-numCharInLF-1) == ' '){
										System.out.printf("ERROR -- server-port%s", LF);
										//LF immediately after serverHost -> serverHost error
									}else{
										System.out.printf("ERROR -- server-host%s", LF);
									}
								}
							}else{
								System.out.printf("ERROR -- server-host%s", LF);
							}
						}else{
							//LF immediately after command -> command error
							if(line.length()-numCharInLF == command.length()){
								System.out.printf("ERROR -- request%s", LF);
								//existing serverHost that was just spaces -> serverhost error
							}else if (line.length()-numCharInLF > command.length()){
								System.out.printf("ERROR -- server-host%s", LF);
							}
						}

					}else if(command.equalsIgnoreCase("GET")){
						if(connected){
							if(tokenizedLine.hasMoreTokens()){
								String pathName = tokenizedLine.nextToken();
								if(checkASCII(pathName)){
									if(line.charAt(line.length()-1) == ' ' || tokenizedLine.hasMoreTokens()){
										System.out.printf("ERROR -- pathname%s", LF);
									}else{
										System.out.printf("GET accepted for " + pathName + "%s", LF);

										InetAddress myInet;
										myInet = InetAddress.getLocalHost();
										String hostAddress = myInet.getHostAddress();
										hostAddress = hostAddress.replace('.', ',');
										String portNum1 = Integer.toString(dataPortNum>>8);
										String portNum2 = Integer.toString(dataPortNum&255);
										String portNumber = "," + portNum1 + "," + portNum2;

										//send PORT/RETR to server
										try{

											//accept welcoming socket at portNum
											Integer dataPort = Integer.parseInt(portNum1)*256 + Integer.parseInt(portNum2);
											ServerSocket welcome = new ServerSocket(dataPort);

											//send PORT to server
											System.out.printf("PORT " + hostAddress + portNumber + "%s", CRLF);
											outToServer.write("PORT " + hostAddress + portNumber + "\r\n");
											outToServer.flush();
											dataPortNum++;

											//handle response from server
											handleServerRes(line, inFromServer.readLine());

											//send RETR to server
											System.out.printf("RETR " + pathName + "%s", CRLF);
											outToServer.write("RETR " + pathName +  "\r\n");
											outToServer.flush();
											
											//handle 150 response from server
											handleServerRes(line, inFromServer.readLine());
											
											
											try{
												Socket dataSocket = welcome.accept();
												
												//no errors in finding file, increment retrCount
												retrCount++;
														
												//data stream containing file bytes
												BufferedReader dataIn = new BufferedReader(new InputStreamReader(dataSocket.getInputStream()));

												//destination of file data
												OutputStream toFile = new FileOutputStream(new File("retr_files/file" + retrCount));
												int len;

												//start download
												while ((len = dataIn.read()) != -1) {
													toFile.write(len);
												}
												
												dataIn.close();
												toFile.close();
												dataSocket.close();

												//handle 250 response from server
												handleServerRes(line, inFromServer.readLine());

											}catch(RuntimeException e){
												System.out.println("GET failed, FTP-data port not allocated.");
											}
										}catch(RuntimeException e){
											
										}
									}
								}else{
									System.out.printf("ERROR -- pathname%s", LF);
								}
							}else{
								//LF immediately after command -> request error
								if(line.length()-numCharInLF == command.length()){
									System.out.printf("ERROR -- request%s", LF);
									//existing serverHost that was just spaces -> serverhost error
								}else if (line.length()-numCharInLF > command.length()){
									System.out.printf("ERROR -- pathname%s", LF);
								}
							}
						}else{
							System.out.printf("ERROR -- expecting CONNECT%s", LF);
						}
					}else if(command.equalsIgnoreCase("QUIT")){
						if(connected){
							if(tokenizedLine.hasMoreTokens()){
								System.out.printf("ERROR -- request%s", LF);
							}else{
								System.out.printf("QUIT accepted, terminating FTP client%s", LF);
								System.out.printf("QUIT%s", CRLF);
								if(connected){
									outToServer.write("QUIT" + "\r\n");
									outToServer.flush();
									handleServerRes(line, inFromServer.readLine());
									clientSocket.close();
									outToServer.close();
									inFromServer.close();
								}
								System.exit(0);
							}
						}else{
							System.out.printf("ERROR -- expecting CONNECT%s", LF);
						}
					}else{
						System.out.printf("ERROR -- request%s", LF);
					}
				}
			}
		}else{
			System.out.println("No port number");
		}
	}

	private static boolean checkASCII(String token){
		boolean within = true;
		for(int i=0; i<token.length(); i++){
			int asciiVal = token.charAt(i);
			if(asciiVal > 128){
				within = false;
			}
		}
		return within;
	}

	private static void handleServerRes(String line, String res){
		if(res.charAt(res.length()-2) == '\r'){
			if(checkASCII(res)){
				//convert the line into a token
				StringTokenizer tokenizedRes = new StringTokenizer(res);
				String resCode = tokenizedRes.nextToken();
				try{
					int replyCode = Integer.parseInt(resCode);
					if(0 <= replyCode && replyCode <= 599 ){
						if(tokenizedRes.hasMoreTokens()){
							//more than one space between replyCode and replyText
							if(res.charAt(resCode.length() + 1) == ' '){
								System.out.printf("ERROR -- reply-text%s", LF);
							}else{
								System.out.print("FTP reply " + replyCode + " accepted. Text is :");
								while(tokenizedRes.hasMoreTokens()){
									String next = tokenizedRes.nextToken();
									System.out.print(" " + next);
								}
								System.out.printf("%s", LF);
								if(replyCode >= 400){
									throw new RuntimeException();
								}
							}
						}else{
							//LF immediately after reply code -> replyCode error
							if(line.length()-2 == resCode.length()){
								System.out.printf("ERROR -- reply-code%s", LF);
								//existing reply-text that was just spaces -> replyText error
							}else if (line.length()-2 > resCode.length()){
								System.out.printf("ERROR -- reply-text%s", LF);
							}
						}
					}else{
						System.out.printf("ERROR -- reply-code%s", LF);
					}
				}catch(NumberFormatException e){
					System.out.printf("ERROR -- reply-code%s", LF);
				}
			}else{
				System.out.printf("ERROR -- reply-text%s", LF);
			}
		}else{
			System.out.printf("ERROR -- CRLF%s", LF);
		}
	}

}

// class to wrap around BufferedReader in order to process CRLF
class LineReader {   
	private int i = 256;
	private BufferedReader br;
	public LineReader(BufferedReader br) { this.br = br; }
	public String readLine() throws IOException {
		if (i == 256) i = br.read();
		if (i < 0) return null;
		StringBuilder sb = new StringBuilder();
		sb.append((char)i);
		if (i != '\r' && i != '\n') {
			while (0 <= (i = br.read()) && i != '\r' && i != '\n') {
				sb.append((char)i);
			}
			if (i < 0) return sb.toString();
			sb.append((char)i);
		}
		if (i == '\r') {
			i = br.read();
			if (i != '\n') return sb.toString(); 
			sb.append((char)'\n');
		}
		i = 256;
		return sb.toString();
	}

	public void close() throws IOException{
		this.br.close();
	}
}
