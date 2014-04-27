import java.net.*;
import java.io.*;
import java.util.*;

//One cache, multiple clients
public class ProxyCache {
	final static int PORT = 5005;
	
    static ServerSocket server;
    static Map<String, String> cache;
    static int maxDox;
    
    //Initialize cache with <maximum documents> and serversocket with fixed port number
    public ProxyCache(int maxDocs) {
    	this.maxDox = maxDocs;
    	//true = access order instead of insertion order
    	this.cache = new LinkedHashMap<String, String>(this.maxDox, 0.75f, true) {	//(capacity, default load factor, ordering mode)
    		protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
    			//When to remove eldest entry
    			return size() > maxDox;		//Size exceeded the allowable max
    		}
    	};
        try {
            server = new ServerSocket(PORT);
        } catch (IOException e) {
            System.out.println("ERROR: Could not create server socket: " + e);
            System.exit(0);
        }
    }

    //Spawn a thread per client and pass off to ClientHandler
    public void listen() throws IOException {
    	while(true) {
    		ClientHandler ch = new ClientHandler(server.accept());
    		ch.start();
    	}
    }
    
    //takes a single argument: <maximum documents>
    public static void main(String args[]) throws IOException {
        try {
            maxDox = Integer.parseInt(args[0]);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Single Parameter Required: <Maximum Documents>");
            System.exit(0);
        } catch (NumberFormatException e) {
            System.out.println("Please give <Maximum Documents> as an integer.");
            System.exit(0);
        }
        
        ProxyCache prox = new ProxyCache(maxDox);
        prox.listen();
    }
    
    //Lets do the heavy lifting
    public class ClientHandler extends Thread {
    	Socket client;
		BufferedReader input;
		
    	//Constructor
    	public ClientHandler(Socket client) {
    		this.client = client;
    	}
    	
        public synchronized void put(String key, String val) {
        	cache.put(key, val);
        }
        
        public synchronized String get(String key) {
        	return cache.get(key);
        }
        
    	//Return nthIndexOf instead of the first indexOf
    	public int nthIndexOf (String str, char c, int n) {
    		int pos = str.indexOf(c, 0);
    		while(n-- > 0 && pos != -1) {
    			pos = str.indexOf(c, pos+1);
    		}
    		return pos;
    	}
    	
    	//Marching orders
    	public void run() {
			try {
				input = new BufferedReader(new InputStreamReader(client.getInputStream()));
			} catch (IOException e) {
				System.out.println("ERROR: Problems setting up ProxyCache input and output with client.");
				System.exit(0);
			}
			
			String userInput, localFile = null;
				try {
					while((userInput = input.readLine()) != null) {
						//Check Cache for local copy
						if (cache.containsKey(userInput)) {
							//Send local copy to client
						}
						//No local, get HTML page from Web Server
						else {
							System.out.println("ELSE");	//tk
							//Get Document from Web Server
							URL url = new URL(userInput);
							System.out.println("URL: " + userInput);	//tk
							HttpURLConnection connection = (HttpURLConnection)url.openConnection();
							System.out.println("Connection OPEN");	//tk
							int code = connection.getResponseCode();	//should be 200 for OK
							System.out.println("Code: " + code);	//tk
							if (HttpURLConnection.HTTP_OK == code) {
								connection.connect();
								System.out.println("Connected");	//tk
								InputStream is = connection.getInputStream();
								System.out.println("Input stream set up");	//tk
								int nthIndex = nthIndexOf(userInput, ':', 1);
								System.out.println("nthIndex: " + nthIndex);	//tk
								localFile = userInput.substring(nthIndex + 1);
								System.out.println("file name is: " + localFile);	//tk
								FileOutputStream fos = new FileOutputStream(localFile);
								System.out.println("FOS up and running");	//tk
								
								int j;
								try {
									while ((j = is.read()) != -1) {
										fos.write(j);
									}
								} catch (IOException e) {
									System.out.println("ERROR: Problems reading from Web Server");
									System.exit(0);
								}
								System.out.println("Closing inputstream and FOS");	//tk
								try {
									is.close();
									fos.close();
								} catch (IOException e) {
									System.out.println("ERROR: Problems closing the streams used for getting Web Content.");
									System.exit(0);
								}
							}
							System.out.println("Downloaded HTML page");	//tk
							
							//Add to Cache
							System.out.println("Putting...");	//tk
							put(userInput, localFile);
							System.out.println("Caching: " + userInput + ", " + localFile);	//tk
							
							//Send local copy to client
							File asdf = new File(localFile);
							long fileSize = asdf.length();
							System.out.println("Size of file: " + fileSize);	//tk
							byte[] data = new byte[(int) fileSize];
							FileInputStream fis = null;
							try {
								fis = new FileInputStream(asdf);
							} catch (FileNotFoundException e) {
								System.out.println("ERROR: File not found: FileInputStream(asdf)");
								System.exit(0);
							}
							BufferedInputStream bis = new BufferedInputStream(fis);
							BufferedOutputStream out = null;
							try {
								out = new BufferedOutputStream(client.getOutputStream());
							} catch (IOException e) {
								System.out.println("ERROR: Trouble with BufferedOutputStream(client.getOutputStream)");
								System.exit(0);
							}
							int count;
							
							try {
								while((count = bis.read(data)) > 0) {
									System.out.println("Writing: " + count);	//tk
									out.write(data, 0, count);
								}
							} catch (IOException e) {
								System.out.println("ERROR: Problems sending to client.");
								System.exit(0);
							}
							
							System.out.println("Closing streams!");	//tk
							
							try {
								out.flush();
								out.close();
								fis.close();
								bis.close();
							} catch (IOException e) {
								System.out.println("ERROR: Problems flushing and closing various streams sending local copy");
								System.exit(0);
							}
						}
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
    	}
    }
}
