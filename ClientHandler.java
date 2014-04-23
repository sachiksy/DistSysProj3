import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

class ClientHandler extends Thread
{
	private Socket client;
	private Socket webServer;
	private static final String CRLF = "\r\n";
	private static int RANDOM = 8192; 
	private static CacheData[] cache;
	private static int numOfHits = 0;
	private static int numOfRequests = 0;
	
	//Constructor
	public ClientHandler(Socket sock, int cacheMax) {
		client = sock;
		cache = new CacheData[cacheMax];
	}

	//The Whole Shebang
	public void run() {
		//Client I/O streams
		BufferedInputStream fromClient = null;
		BufferedOutputStream toClient = null;
		
		//Web Server I/O streams
		BufferedInputStream fromServer = null;
		BufferedOutputStream toServer = null;
		
		//HTTP request receptacles
		ByteArrayOutputStream byteRequest;
		byte[] request = null;
		int requestLen = 0;
		
		//Web server socket info
		StringBuffer host = new StringBuffer("");
		String hostName = "";
		
		//Setup client I/O streams
		try {
			fromClient = new BufferedInputStream(client.getInputStream());
			toClient = new BufferedOutputStream(client.getOutputStream());
		}
		//Error case: Failed to setup client I/O streams
		catch (IOException e) {
			System.out.println("ERROR: Failed to setup client I/O streams");
			System.exit(0);
		}
		
		//Receive HTTP request
		byteRequest = new ByteArrayOutputStream();
		requestLen = readHTTP(fromClient, byteRequest, host);
		request = byteRequest.toByteArray();
		
		hostName = host.toString();
		
		//Open socket to web server
		try {
			webServer = new Socket(hostName, 80);
		} catch (UnknownHostException e) {
			System.out.println("ERROR: Failed to recognize host: " + hostName);
			System.exit(0);
		} catch (IOException e) {
			System.out.println("ERROR: Failed to open socket to web server: " + e);
			System.exit(0);
		}
		
		if (webServer != null) {
			//Setup Web Server I/O streams
			try {
				fromServer = new BufferedInputStream(webServer.getInputStream());
				toServer = new BufferedOutputStream(webServer.getOutputStream());
			} catch (IOException e) {
				System.out.println("ERROR: Failed to setup web server I/O streams: " + e);
				System.exit(0);
			}
			
			//Send HTTP request to web server
			try {
				toServer.write(request, 0, requestLen);
				toServer.flush();
			} catch (IOException e) {
				System.out.println("ERROR: Failed to send HTTP request to web server: " + e);
				System.exit(0);
			}
			
			//Receive HTTP response and send to client
			StringBuffer pons = new StringBuffer("");
			readHTTP(fromServer, toClient, pons);
			
			//Close web server I/O streams
			try {
				fromServer.close();
				toServer.close();
			} catch (IOException e) {
				System.out.println("ERROR: Failed to close web server I/O streams: " + e);
				System.exit(0);
			}
		}
		
		//Close all client I/O streams and client
		try {
			toClient.close();
			fromClient.close();
			client.close();
		} catch (IOException e) {
			System.out.println("ERROR: Failed to close client and client I/O streams: " + e);
			System.exit(0);
		}
	}

	//Read HTTP request/response char by char until Carriage-Return-Line-Feed
	private String readLine (InputStream vein) {
		int eightBits;
		StringBuffer line = new StringBuffer("");
		
		vein.mark(1);
		try {
			//HTTP request/response empty, return null
			if (vein.read() == -1) {
				return null;
			}
			//Reset, jump over, and start reading
			else {
				vein.reset();
			}
		} catch (IOException e) {
			System.out.println("ERROR: Failed to read a single character of HTTP request/response: " + e);
			System.exit(0);
		}
		
		//Read until nothing to read
		try {
			while ((eightBits = vein.read()) != -1) {
				//Carriage return, end of the line (pun intended)
				if (eightBits == '\r') {
					break;
				}
				//Keep building the line
				else {
					line.append((char)eightBits);
				}
			}
		} catch (IOException e) {
			System.out.println("ERROR: Failed to read HTTP request/response: " + e);
			System.exit(0);
		}
	
		//Read line feed (eightBits == '\n')
		try {
			vein.read();
		} catch (IOException e) {
			System.out.println("ERROR: Failed to read '\n': " + e);
			System.exit(0);
		}
		//Return line of HTTP request/response
		return line.toString();
	}
	
	//Parse HTTP request/response, return HTTP request/response size/length
	private int readHTTP (InputStream in, OutputStream out, StringBuffer blah) {
		//HTTP request/response
		StringBuffer http = new StringBuffer("");
		
		//Single line of request/response
		String line = "";
		
		//Placeholder in line
		int index;
		
		//Reponse (true) or Request (false)?
		boolean ponds = true;
		
		//HTTP request malform check and fix
		String method;
		String uri;
		String ver;

		//Self explanatory
		int conLen = 0;
		int bytes = 0;
		
		try {
			//First line = request-line of HTTP request or status-line of HTTP response
			line = readLine(in);
			if (line != null) {
				//HTTP request
				if (line.toUpperCase().startsWith("GET") || line.toUpperCase().startsWith("POST") || line.toUpperCase().startsWith("HEAD")
						|| line.toUpperCase().startsWith("PUT") || line.toUpperCase().startsWith("DELETE")) {
					/*if (line.startsWith("get") || line.startsWith("post") || line.startsWith("head") || line.startsWith("put") || line.startsWith("delete")) {
						mal = true;
					}*/
					ponds = false;
					
					String[] thirds = line.split(" ", 3);
					
					//Fix any malformed request-line components
					method = thirds[0].toUpperCase();
					uri = thirds[1];
					ver = thirds[2].toUpperCase();
					
					//Reconstitutes line
					line = method + " " + uri + " " + ver;
					
					//Add parsed first line to StringBuffer
					http.append(line + CRLF);	
				}
				//HTTP response
				else {
					//Add parsed first line to StringBuffer
					http.append(line + CRLF);	
				}
				
				System.out.println(line);	//tk
			}
		/*	else {
				System.out.println("ERROR: HTTP request/response is empty.");	//Caused random errors
				System.exit(0);
			}*/
			
			//Header-Line(s)
			while ((line = readLine(in)) != null) {
				//Blank line separating Header-Line(s) and Data
				if (line.equals("")) {
					break;
				}
				
				//Get Host
				index = line.toLowerCase().indexOf("host:");
				if (index >= 0) {
					blah.append(line.substring(index + "host:".length()).trim());
				}
				
				//Get Content-Length
				index = line.toLowerCase().indexOf("content-length:");
				if (index >= 0) {
					conLen = Integer.parseInt(line.substring(index + "content-length:".length()).trim());
				}
				
				//Check HTTP request for malformed Header Field Names and fix
				if (!ponds) {
					String[] halves = line.split(": ", 3);
					
					//Header field name check and fix (one word)
					if (halves[0].equals("accept") || halves[0].equals("Accept") || halves[0].equals("authorization") || halves[0].equals("Authorization")
							 || halves[0].equals("connection") || halves[0].equals("Connection") || halves[0].equals("cookie")  || halves[0].equals("Cookie")
							 || halves[0].equals("date") || halves[0].equals("Date") || halves[0].equals("expect") || halves[0].equals("Expect")
							 || halves[0].equals("from") || halves[0].equals("From") || halves[0].equals("host") || halves[0].equals("Host")
							 || halves[0].equals("origin") || halves[0].equals("Origin") || halves[0].equals("pragma") || halves[0].equals("Pragma")
							 || halves[0].equals("range") || halves[0].equals("Range") || halves[0].equals("referer")  || halves[0].equals("Referer")
							 || halves[0].equals("te") || halves[0].equals("TE") || halves[0].equals("via") || halves[0].equals("Via") || halves[0].equals("warning")
							 || halves[0].equals("Warning")) {
						halves[0] = halves[0].substring(0, 1).toUpperCase() + halves[0].substring(1);
					}
					//Header field name check and fix (two words)
					else if (halves[0].equals("accept-charset") || halves[0].equals("Accept-Charset") || halves[0].equals("accept-encoding") || halves[0].equals("Accept-Encoding")
							|| halves[0].equals("accept-language") || halves[0].equals("Accept-Language") || halves[0].equals("accept-datetime") || halves[0].equals("Accept-Datetime")
							|| halves[0].equals("cache-control") || halves[0].equals("Cache-Control") || halves[0].equals("content-length") || halves[0].equals("Content-Length")
							|| halves[0].equals("content-md5") || halves[0].equals("Content-MD5") || halves[0].equals("content-type") || halves[0].equals("Content-Type")
							|| halves[0].equals("if-match") || halves[0].equals("If-Match") || halves[0].equals("if-range") || halves[0].equals("If-Range") || halves[0].equals("max-forwards")
							|| halves[0].equals("Max-Forwards") || halves[0].equals("proxy-authorization")  || halves[0].equals("Proxy-Authorization") || halves[0].equals("user-agent")  || halves[0].equals("User-Agent")) {
						String[] part = halves[0].split("-");
						String part1 = part[0];
						String part2 = part[1];
						
						part1 = part1.substring(0, 1).toUpperCase() + part1.substring(1);
						part2 = part2.substring(0, 1).toUpperCase() + part2.substring(1);
						
						halves[0] = part1 + "-" + part2;
					}
					//Header field name check and fix (three words)
					else if (halves[0].equals("if-modified-since") || halves[0].equals("If-Modified-Since") || halves[0].equals("if-none-match") || halves[0].equals("If-None-Match")
							|| halves[0].equals("if-unmodified-since")  || halves[0].equals("If-Unmodified-Since")) {
						String[] part = halves[0].split("-");
						String part1 = part[0];
						String part2 = part[1];
						String part3 = part[2];
						
						part1 = part1.substring(0, 1).toUpperCase() + part1.substring(1);
						part2 = part2.substring(0, 1).toUpperCase() + part2.substring(1);
						part3 = part3.substring(0, 1).toUpperCase() + part3.substring(1);
						
						halves[0] = part1 + "-" + part2 + "-" + part3;
					}
					
					//Reconstitutes line
					line = halves[0] + ": " + halves[1];
				}
				
				//Add parsed header-line to StringBuffer
				http.append(line + CRLF);
				
				System.out.println(line);	//tk
			}
			
			//Blank line separating http-Line(s) and Data
			http.append(CRLF);
			
			System.out.println("");	//tk
			
			//Write HTTP request/response from StringBuffer to String to a byte[]
			out.write(http.toString().getBytes(), 0, http.length());
			
			//No Content-Length, flush and return
			if (conLen == 0)
			{
				out.flush();
				return http.length();
			}
			//Content-Length exists, read in Data/Body
			else {
				try {
					byte[] buffer = new byte[RANDOM];
					int readIn = 0;
					while (((readIn = in.read(buffer)) >= 0) && (bytes < conLen)) {
						out.write(buffer, 0, readIn);
						bytes += readIn;
					}
				}
				//Error case: Failed to read HTTP request/response data/body
				catch (Exception e)  {
					System.out.println("ERROR: Failed to read HTTP request/response data: " + e);
					System.exit(0);
				}
			}
		}
		//Error case: Failed to read HTTP request/response line
		catch (Exception e)  {
			System.out.println("ERROR: Failed to read HTTP request/response line: " + e);
			System.exit(0);
		}
		
		try {
			out.flush();
		}  catch (Exception e) {
			System.out.println("ERROR: Failed to flush contents of outputstream: " + e);
			System.exit(0);
		}
		
		return (bytes + http.length());
	}
	
	private synchronized void getWebPage(String cliURL){
		int lru = 0;
		numOfRequests++;
		for(int i=0; i<cache.length; i++){
			//Found an empty spot in the cache
			if ( cache[i] == null ){
				cache[i]=new CacheData(cliURL);
		 		updateCache(cliURL, i);
		  		sendPage(cliURL);
		  		return;
			}
			//Found the cached copy
			else if ( cliURL.equals(cache[i].getURL()) ){
				sendPage(cliURL);
				numOfHits++;
				cache[i].updateTimestamp();
				return;
			}
			//CacheData at index i was least recently used
			else if ( ( cache[lru].compareTo(cache[i]) ) > 0 ){
				lru=i;
			}
		}
		//page not cached; replace lru page with requested one
		File lruPage = new File(cache[lru].getFilename());
		lruPage.delete();
		cache[lru]=new CacheData(cliURL);
		updateCache(cliURL, lru);
  		sendPage(cliURL);
		return;
	}
	
	public void sendPage(String URL){
		//retrieve the file and folder and send to client
	}
	
	private void updateCache(String URL, int index){
		//Step ONE: pull page from the origin server
		// --- Code goes here --- //
		//Step TWO: add page to cache[] at index
		cache[index]=new CacheData(URL);
	}
	
	public float getHitRate(){
		return numOfHits/numOfRequests;
		//we need some way of sending this to the client after processing all input
		//maybe store it in a file server-side instead?
	}
	
	public float getMissRate(){
		return 1 - getHitRate();
		//we need some way of sending this to the client after processing all input
		//maybe store it in a file server-side instead?
	}
}