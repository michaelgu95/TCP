import java.util.Scanner;
import java.util.StringTokenizer;


public class FTPclient2 {
	static Scanner sc;
	private static final String LF = "\n";

	public static void main(String[] args){
		sc = new Scanner(System.in);
		sc.useDelimiter("(?<=(\r\n|\n|(\r(?!\n))))");
		while(sc.hasNext()){
			String line = sc.next();
			System.out.print(line);

			//check if line has CRLF by seeing if the second to last character is '\r'
			if(line.charAt(line.length()-2) == '\r'){
				if(checkASCII(line)){
					//convert the line into a token
					StringTokenizer tokenizedLine = new StringTokenizer(line);
					String command = tokenizedLine.nextToken();
					try{
						int replyCode = Integer.parseInt(command);
						if(0 <= replyCode && replyCode <= 599 ){
							if(tokenizedLine.hasMoreTokens()){
								//more than one space between replyCode and replyText
								if(line.charAt(command.length() + 1) == ' '){
									System.out.printf("ERROR -- reply-text%s", LF);
								}else{
									System.out.print("FTP reply " + replyCode + " accepted. Text is :");
									while(tokenizedLine.hasMoreTokens()){
										String next = tokenizedLine.nextToken();
										System.out.print(" " + next);
									}
									System.out.printf("%s", LF);
								}
							}else{
								//EOL immediately after reply code -> replyCode error
								if(line.length()-2 == command.length()){
									System.out.printf("ERROR -- reply-code%s", LF);
								//existing reply-text that was just spaces -> replyText error
								}else if (line.length()-2 > command.length()){
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
