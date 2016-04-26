import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
//Michael Gu

public class FTPServer {
	public static final String CRLF="\r\n";
	private static final String EOL = System.getProperty("line.separator");
	private static ServerSocket controlSocket;
	private static LineReader inFromClient;

	public static void main(String[] args) throws IOException, InterruptedException {
		Integer dataPort = null;
		String dataHost = null;
		controlSocket = new ServerSocket(Integer.parseInt(args[0]));


		while(true){
			Socket acceptSocket = controlSocket.accept();
			//input stream
			inFromClient = new LineReader(new BufferedReader(new InputStreamReader(acceptSocket.getInputStream())));

			//output stream
			PrintWriter outToClient = new PrintWriter(acceptSocket.getOutputStream());

			System.out.printf("220 COMP 431 FTP server ready.%s", CRLF);
			outToClient.printf("220 COMP 431 FTP server ready.%s", CRLF);
			outToClient.flush();

			//fields to ensure proper command sequence:
			boolean userCheck = false;
			boolean passCheck = false;
			int portCount = 0;
			int retrCount = 0;

			while(true){
				String line = inFromClient.readLine();

				if(line != null){

					System.out.printf(line);

					//check if line has CRLF by seeing if the second to last character is '\r'
					boolean hasCRLF;
					//					System.out.println(line.charAt(line.length()-2));
					if(line.charAt(line.length()-2) == '\r'){
						hasCRLF = true;
					}else{
						hasCRLF = false;
					}
					//convert the line into a token
					StringTokenizer tokenizedLine = new StringTokenizer(line);
					String command = tokenizedLine.nextToken();

					//first round of if statements checks for valid command
					if(command.equalsIgnoreCase("USER")){
						if(userCheck == false && passCheck == false){

							//if there is a second token in the line
							if(tokenizedLine.hasMoreTokens()){
								String argument = tokenizedLine.nextToken();

								//check if argument's characters fall within ASCII128
								if(checkASCII(argument)){
									if(hasCRLF){
										//record USER command instance 
										userCheck = true;
										System.out.printf("331 Guest access OK, send password.%s", CRLF);
										outToClient.printf("331 Guest access OK, send password.%s", CRLF);
										outToClient.flush();

									}else{
										
										System.out.printf("331 Guest access OK, send password.%s", CRLF);
										outToClient.printf("331 Guest access OK, send password.%s", CRLF);
										outToClient.flush();
									}
									//not ASCII128 -> parameter error
								}else{
									System.out.printf("501 Syntax error in parameter.%s", CRLF);
									outToClient.printf("501 Syntax error in parameter.%s", CRLF);
									outToClient.flush();
								}

								//or there are no more tokens past the command; two cases:
							}else{
								//either there is whitespace before the CRLF -> parameter error
								if(line.charAt(line.length()-3) == ' '){
									System.out.printf("501 Syntax error in parameter.%s", CRLF);
									outToClient.printf("501 Syntax error in parameter.%s", CRLF);
									outToClient.flush();
									//or the command was followed immediately by the CRLF -> command error	
								}else{
									System.out.printf("500 Syntax error, command unrecognized.%s", CRLF);
									outToClient.printf("500 Syntax error, command unrecognized.%s", CRLF);
									outToClient.flush();
								}
							}
						}else{
							outToClient.printf("503 Bad sequence of commands.%s", CRLF);
						}

					}else if(command.equalsIgnoreCase("PASS")){
						//check if USER has occurred already
						if(userCheck == true && passCheck==false){

							//if there is a second token in the line
							if(tokenizedLine.hasMoreTokens()){
								String argument = tokenizedLine.nextToken();

								//check if argument's characters fall within ASCII128
								if(checkASCII(argument)){
									if(hasCRLF){
										//record PASS command instance
										passCheck = true;

										System.out.printf("230 Guest login OK.%s", CRLF);
										outToClient.printf("230 Guest login OK.%s", CRLF);
										outToClient.flush();
									}else{
										outToClient.printf("501 Syntax error in parameter.%s", CRLF);
									}
									//not ASCII128 -> argument error
								}else{
									System.out.printf("501 Syntax error in parameter.%s", CRLF);
									outToClient.printf("501 Syntax error in parameter.%s", CRLF);
									outToClient.flush();
								}

								//or there are no more tokens past the command; two cases:
							}else{
								//either there is whitespace before the CRLF -> argument error
								if(line.charAt(line.length()-3) == ' '){
									System.out.printf("501 Syntax error in parameter.%s", CRLF);
									outToClient.printf("501 Syntax error in parameter.%s", CRLF);
									outToClient.flush();

									//or the command was followed immediately by the CRLF -> command error	
								}else{
									System.out.printf("500 Syntax error, command unrecognized.%s", CRLF);
									outToClient.printf("500 Syntax error, command unrecognized.%s", CRLF);
									outToClient.flush();
								}
							}
						}else{
							System.out.printf("503 Bad sequence of commands.%s", CRLF);
							outToClient.printf("503 Bad sequence of commands.%s", CRLF);
							outToClient.flush();
						}

					}else if(command.equalsIgnoreCase("TYPE")){

						if(tokenizedLine.hasMoreTokens()){
							String argument = tokenizedLine.nextToken();

							//check if argument is 'A' or 'I'
							if(argument.equals("A") || argument.equals("I")){
								if(hasCRLF){
									//check if there is whitespace before the CRLF -> type code error
									if(line.charAt(line.length()-3) == ' '){
										System.out.printf("501 Syntax error in parameter.%s", CRLF);
										outToClient.printf("501 Syntax error in parameter.%s", CRLF);
										outToClient.flush();
									}else{
										if(userCheck == true){
											if(passCheck == true){
												if(argument.equals("A")){
													System.out.printf("200 Type set to A.%s", CRLF);
													outToClient.printf("200 Type set to A.%s", CRLF);
													outToClient.flush();
												}else{
													System.out.printf("200 Type set to I.%s", CRLF);
													outToClient.printf("200 Type set to I.%s", CRLF);
													outToClient.flush();
												}
											}else{
												System.out.printf("503 Bad sequence of commands.%s", CRLF);
												outToClient.printf("503 Bad sequence of commands.%s", CRLF);
												outToClient.flush();
											}
										}else{
											System.out.printf("530 Not logged in.%s", CRLF);
											outToClient.printf("530 Not logged in.%s", CRLF);
											outToClient.flush();
										}
									}
								}else{
									outToClient.printf("501 Syntax error in parameter.%s", CRLF);
								}
							}else{
								System.out.printf("501 Syntax error in parameter.%s", CRLF);
								outToClient.printf("501 Syntax error in parameter.%s", CRLF);
								outToClient.flush();
							}
						}else{
							//check if there is whitespace after the command -> type-code error
							if(line.charAt(line.length()-3) == ' '){
								System.out.printf("501 Syntax error in parameter.%s", CRLF);
								outToClient.printf("501 Syntax error in parameter.%s", CRLF);
								outToClient.flush();
							}else{

								//otherwise command error
								System.out.printf("500 Syntax error, command unrecognized.%s", CRLF);
								outToClient.printf("500 Syntax error, command unrecognized.%s", CRLF);
								outToClient.flush();
							}
						}


					}else if(command.equalsIgnoreCase("SYST")){

						if(tokenizedLine.hasMoreTokens()){
							System.out.printf("501 Syntax error in parameter.%s", CRLF);
							outToClient.printf("501 Syntax error in parameter.%s", CRLF);
							outToClient.flush();
						}else{

							//check if CRLF present at end
							if(hasCRLF){
								//check if whitespace is present before CRLF -> CRLF error
								if(line.charAt(line.length()-3) == ' '){
									System.out.printf("501 Syntax error in parameter.%s", CRLF);
									outToClient.printf("501 Syntax error in parameter.%s", CRLF);
									outToClient.flush();
								}else{
									if(userCheck == true){
										if(passCheck == true){
											System.out.printf("215 UNIX Type: L8.%s", CRLF);
											outToClient.printf("215 UNIX Type: L8.%s", CRLF);
											outToClient.flush();
										}else{
											System.out.printf("503 Bad sequence of commands.%s", CRLF);
											outToClient.printf("503 Bad sequence of commands.%s", CRLF);
											outToClient.flush();
										}
									}else{
										System.out.printf("530 Not logged in.%s", CRLF);
										outToClient.printf("530 Not logged in.%s", CRLF);
										outToClient.flush();
									}
								}
							}else{
								outToClient.printf("501 Syntax error in parameter.%s", CRLF);
							}
						}


					}else if(command.equalsIgnoreCase("NOOP")){
						if(tokenizedLine.hasMoreTokens()){
							System.out.printf("501 Syntax error in parameter.%s", CRLF);
							outToClient.printf("501 Syntax error in parameter.%s", CRLF);
							outToClient.flush();
						}else{

							//check if CRLF present at end
							if(hasCRLF){

								//check if whitespace is present before CRLF -> CRLF error
								if(line.charAt(line.length()-3) == ' '){
									System.out.printf("501 Syntax error in parameter.%s", CRLF);
									outToClient.printf("501 Syntax error in parameter.%s", CRLF);
									outToClient.flush();
								}else{
									if(userCheck == true){
										if(passCheck == true){
											System.out.printf("200 Command OK.%s", CRLF);
											outToClient.printf("200 Command OK.%s", CRLF);
											outToClient.flush();
										}else{
											System.out.printf("503 Bad sequence of commands.%s", CRLF);
											outToClient.printf("503 Bad sequence of commands.%s", CRLF);
											outToClient.flush();
										}
									}else{
										System.out.printf("530 Not logged in.%s", CRLF);
										outToClient.printf("530 Not logged in.%s", CRLF);
										outToClient.flush();
									}
								}
							}else{
								System.out.printf("501 Syntax error in parameter.%s", CRLF);
								outToClient.printf("501 Syntax error in parameter.%s", CRLF);
								outToClient.flush();
							}
						}
					}else if(command.equalsIgnoreCase("QUIT")){
						if(tokenizedLine.hasMoreTokens()){
							System.out.printf("501 Syntax error in parameter.%s", CRLF);
							outToClient.printf("501 Syntax error in parameter.%s", CRLF);
							outToClient.flush();
						}else{

							//check if CRLF present at end
							if(hasCRLF){
								//check if whitespace is present before CRLF -> CRLF error
								if(line.charAt(line.length()-3) == ' '){
									System.out.printf("501 Syntax error in parameter.%s", CRLF);
									outToClient.printf("501 Syntax error in parameter.%s", CRLF);
									outToClient.flush();
								}else{
									System.out.printf("221 Goodbye.%s", CRLF);
									outToClient.printf("221 Goodbye.%s", CRLF);
									outToClient.flush();
									outToClient.close();
									acceptSocket.close();
									break;
								}
							}else{
								System.out.printf("501 Syntax error in parameter.%s", CRLF);
								outToClient.printf("501 Syntax error in parameter.%s", CRLF);
								outToClient.flush();
							}
						}
					}else if(command.equalsIgnoreCase("PORT")){
						if(tokenizedLine.hasMoreTokens()){
							String argument = tokenizedLine.nextToken();


							//check if argument follows valid PORT pattern
							String[] splitArg = argument.split("\\,");
							boolean matches = false;
							if(splitArg.length == 6){
								for(String num : splitArg){
									if(num.matches("1?[0-9]{1,2}|2[0-4][0-9]|25[0-5]")){
										matches = true;
									}else{
										matches = false;
										break;
									}
								}
							}else{
								matches = false;
							}
							if(matches){
								if(hasCRLF){
									//check if there is whitespace before the CRLF -> type code error
									if(line.charAt(line.length()-3) == ' '){
										System.out.printf("501 Syntax error in parameter.%s", CRLF);
										outToClient.printf("501 Syntax error in parameter.%s", CRLF);
										outToClient.flush();
									}else{
										if(userCheck == true){
											if(passCheck == true){

												//only increase portCount if equal to retrCount, otherwise count as override
												if(portCount == retrCount){
													portCount++;
												}
												//convert commas to periods, then calculate port address
												String convertedArg = argument.replace(',', '.');
												String[] splitCon = convertedArg.split("\\.");
												String portNum = Integer.toString(Integer.parseInt(splitCon[4])*256 + Integer.parseInt(splitCon[5]));
												String IP = splitCon[0] + "." + splitCon[1] + "." + splitCon[2] + "." + splitCon[3];
												System.out.printf("200 Port command successful (" + IP + "," + portNum+").%s", CRLF);
												outToClient.printf("200 Port command successful (" + IP + "," + portNum+").%s", CRLF);
												outToClient.flush();

												//set port and host for data socket
												dataPort = Integer.parseInt(portNum);
												dataHost = IP;
											}else{
												System.out.printf("503 Bad sequence of commands.%s", CRLF);
												outToClient.printf("503 Bad sequence of commands.%s", CRLF);
												outToClient.flush();
											}
										}else{
											System.out.printf("530 Not logged in.%s", CRLF);
											outToClient.printf("530 Not logged in.%s", CRLF);
											outToClient.flush();
										}
									}
								}else{
									System.out.printf("501 Syntax error in parameter.%s", CRLF);
									outToClient.printf("501 Syntax error in parameter.%s", CRLF);
									outToClient.flush();
								}
							}else{
								System.out.printf("501 Syntax error in parameter.%s", CRLF);
								outToClient.printf("501 Syntax error in parameter.%s", CRLF);
								outToClient.flush();
							}
						}else{
							//check if there is whitespace after the command -> parameter error
							if(line.charAt(line.length()-3) == ' '){
								System.out.printf("501 Syntax error in parameter.%s", CRLF);
								outToClient.printf("501 Syntax error in parameter.%s", CRLF);
								outToClient.flush();
							}else{

								//otherwise command error
								System.out.printf("500 Syntax error, command unrecognized.%s", CRLF);
								outToClient.printf("500 Syntax error, command unrecognized.%s", CRLF);
								outToClient.flush();
							}
						}

					}else if(command.equalsIgnoreCase("RETR")){

						if(tokenizedLine.hasMoreTokens()){
							String argument = tokenizedLine.nextToken();
							if(checkASCII(argument)){
								if(hasCRLF){
									//check if there is whitespace before the CRLF -> type code error
									if(line.charAt(line.length()-3) == ' '){
										System.out.printf("501 Syntax error in parameter.%s", CRLF);
										outToClient.printf("501 Syntax error in parameter.%s", CRLF);
										outToClient.flush();
									}else{
										//check if logged in
										if(userCheck == true){
											if(passCheck == true){
												//check if PORT command was called beforehand
												if(retrCount == (portCount-1)){
													//check if argument starts with backslash, if so remove
													if(argument.charAt(0) == '/' || argument.charAt(0) == '\\'){
														argument = argument.substring(1);
													}
													File f = new File(argument);
													if(f.exists() && !f.isDirectory()){

														System.out.printf("150 File status okay.%s", CRLF);
														outToClient.printf("150 File status okay.%s", CRLF);
														outToClient.flush();

														try{
															//create welcoming socket 
															Socket dataSocket = new Socket(dataHost, dataPort);
															
															//output stream
															DataOutputStream dataOut = new DataOutputStream(dataSocket.getOutputStream());
															//input stream from File f
															FileInputStream is = new FileInputStream(f);
															byte[] bytes = new byte[16*1024];
															int count;
															//send bytes over TCP
															while ((count = is.read(bytes)) > 0) {
																dataOut.write(bytes, 0, count);
															}
															System.out.printf("250 Requested file action completed.%s", CRLF);
															outToClient.printf("250 Requested file action completed.%s", CRLF);
															outToClient.flush();
															retrCount++;

															dataOut.flush();
															dataSocket.close();
															dataOut.close();
															is.close();
														}catch(IOException e){
															e.printStackTrace();
															System.out.printf("425 Can not open data connection.%s", CRLF);
															outToClient.printf("425 Can not open data connection.%s", CRLF);
															outToClient.flush();
														}

													}else{
														System.out.printf("550 File not found or access denied.%s", CRLF);
														outToClient.printf("550 File not found or access denied.%s", CRLF);
														outToClient.flush();
													}
												}else{
													System.out.printf("503 Bad sequence of commands.%s", CRLF);
													outToClient.printf("503 Bad sequence of commands.%s", CRLF);
													outToClient.flush();
												}
											}else{
												System.out.printf("503 Bad sequence of commands.%s", CRLF);
												outToClient.printf("503 Bad sequence of commands.%s", CRLF);
												outToClient.flush();
											}
										}else{
											System.out.printf("530 Not logged in.%s", CRLF);
											outToClient.printf("530 Not logged in.%s", CRLF);
											outToClient.flush();
										}
									}
								}else{
									System.out.printf("501 Syntax error in parameter.%s", CRLF);
									outToClient.printf("501 Syntax error in parameter.%s", CRLF);
									outToClient.flush();
								}
							}else{
								System.out.printf("501 Syntax error in parameter.%s", CRLF);
								outToClient.printf("501 Syntax error in parameter.%s", CRLF);
								outToClient.flush();
							}
						}else{
							//check if there is whitespace after the command -> parameter error
							if(line.charAt(line.length()-3) == ' '){
								System.out.printf("501 Syntax error in parameter.%s", CRLF);
								outToClient.printf("501 Syntax error in parameter.%s", CRLF);
								outToClient.flush();
							}else{
								//otherwise command error
								System.out.printf("500 Syntax error, command unrecognized.%s", CRLF);
								outToClient.printf("500 Syntax error, command unrecognized.%s", CRLF);
								outToClient.flush();
							}
						}
					}else if(command.length() == 4){
						System.out.printf("502 Command not implemented.%s", CRLF);
						outToClient.printf("502 Command not implemented.%s", CRLF);
						outToClient.flush();
					}else{
						System.out.printf("500 Syntax error, command unrecognized.%s", CRLF);
						outToClient.printf("500 Syntax error, command unrecognized.%s", CRLF);
						outToClient.flush();
					}
				}else{
					//revert all fields
					userCheck = false;
					passCheck = false;
					portCount = 0;
					retrCount = 0;

					//wait for new client socket connection
					acceptSocket = controlSocket.accept();
					//input stream
					inFromClient = new LineReader(new BufferedReader(new InputStreamReader(acceptSocket.getInputStream())));
					//output stream
					outToClient = new PrintWriter(acceptSocket.getOutputStream());

					System.out.printf("220 COMP 431 FTP server ready.%s", CRLF);
					outToClient.printf("220 COMP 431 FTP server ready.%s", CRLF);
					outToClient.flush();

				}
			}
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
}