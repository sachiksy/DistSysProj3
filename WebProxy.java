import java.io.*;
import java.net.*;

public class WebProxy
{	
	private static int port;
	private static int MAX_DOCS;
	private ServerSocket serverSock;
	private Socket client;
	
	//Constructor
	public WebProxy (int harbor) {
		port = harbor;
	}

	//Validate args, Make proxy object, Run proxy server
	public static void main (String args[]) {
		//Error case: Improper arg count
		if (args.length != 3) {
			System.out.println("ERROR: Parameters should be: -p proxy_TCP_port_number MAX_DOCS");
			System.exit(0);
		}
		
		if (args[0].equals("-p")) {
			try {
				port = Integer.parseInt(args[1]);
			}
			//Error case: TCP port number not an integer.
			catch (NumberFormatException e) {
				System.out.println("ERROR: TCP port number not an integer: " + e);
				System.exit(0);
			}
			
			//Error case: Improper port number. Must be between 1024 and 65535
			if (port < 1024 || port > 65535) {
				System.out.println("ERROR: Improper port number. Must be between 1024 and 65535");
				System.exit(0);
			}
			else {
				try {
					WebProxy proxy = new WebProxy(port);
					System.out.println("WebProxy is on port: " + port + ". ^C to end WebProxy.\n");
					MAX_DOCS = Integer.parseInt(args[2]);
					proxy.run();
				}
				//Error case: Failed to receive port number so failed to create proxy
				catch (ArrayIndexOutOfBoundsException e) {
					System.out.println("ERROR: Failed to receive port number thus failed to create proxy: " + e);
					System.exit(0);
				}
			}
		}
		//Error case: Improper first parameter
		else {
			System.out.println("ERROR: Parameter 1 should be: '-p'");
			System.exit(0);
		}
	}

	//Create proxy server, accept and create thread(s) to handle client(s)
	public void run() {
		try {
			serverSock = new ServerSocket(port);
		}
		//Error case: Failed to create proxy server
		catch (IOException e) {
			System.out.println("ERROR: Failed to create proxy server: " + e);
			System.exit(0);
		}

		while (true) {
			try {
				client = serverSock.accept();
				ClientHandler handler = new ClientHandler(client, MAX_DOCS);
				handler.start();
			}
			//Error case: Failed to accept client
			catch (IOException e) {
				System.out.println("ERROR: Failed to accept a client: " + e);
				System.exit(0);
			}
		}
	}
}
