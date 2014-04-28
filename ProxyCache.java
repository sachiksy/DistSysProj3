import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;

//One cache, multiple clients
public class ProxyCache {
	final static int PORT = 5005;
	
    static ServerSocket server;
    static Map<String, String> cache;
    static int maxDox;
    double hit, miss;
    
    //Initialize cache with <maximum documents> and serversocket with fixed port number
    public ProxyCache(int maxDocs) {
    	this.maxDox = maxDocs;
    	//true = access order instead of insertion order
    	this.cache = new LinkedHashMap<String, String>(this.maxDox + 1, 1.1f, true) {	//(capacity, default load factor, ordering mode)
    		protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
    			//When to remove eldest entry
    			return this.size() > ProxyCache.this.maxDox;		//Size exceeded the allowable max
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
		private final Semaphore sem = new Semaphore(1);
		
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
			String userInput, localFile = null;
			hit = 0;
			miss = 0;
			
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
				
				//Populate Cache, Download HTML files to make available locally
				while((userInput = br.readLine()) != null) {
					sem.acquire();
					//Check Cache for local copy
					if (cache.containsKey(userInput)) {
						//Access requested URL to reorder LinkedHashMap for LRU purposes
						String temp = get(userInput);
						hit += 1;
						System.out.println("HIT: " + hit + "::" + temp);
					}
					//No local, get HTML page from Web Server
					else {
						//Get Document from Web Server
						URL url = new URL(userInput);
						HttpURLConnection connection = (HttpURLConnection)url.openConnection();
						int code = connection.getResponseCode();	//should be 200 for OK
						if (HttpURLConnection.HTTP_OK == code) {
							connection.connect();
							InputStream is = connection.getInputStream();
							int nthIndex = nthIndexOf(userInput, ':', 1);
							localFile = userInput.substring(nthIndex + 1);
							File fela = new File(localFile);
							FileOutputStream fos = new FileOutputStream(fela);
							
							int j;
							try {
								while ((j = is.read()) != -1) {
									fos.write(j);
								}
							} catch (IOException e) {
								System.out.println("ERROR: Problems reading from Web Server");
								sem.release();
								System.exit(0);
							}

							try {
								is.close();
								fos.close();
							} catch (IOException e) {
								System.out.println("ERROR: Problems closing the streams used for getting Web Content.");
								sem.release();
								System.exit(0);
							}
						}		
			
						int nthIndex = nthIndexOf(userInput, ':', 1);
						localFile = userInput.substring(nthIndex + 1);
						
						//Add to Cache
						put(userInput, localFile);
						miss += 1;
						System.out.println("MISS: " + miss + "::" + localFile);
					}
					sem.release();
				}
				
				double hitRate = hit/(hit+miss);
				double misRate = 1.00 - hitRate;
				
				System.out.println("Hit Rate: " + hitRate);
				System.out.println("Miss Rate: " + misRate);
				
				//Print Contents of Cache to verify correctness
				Set set = cache.entrySet();
				Iterator i = set.iterator();
				System.out.println("Contents of Cache to Verify Correctness(Least Recent => Most Recent [Vertical]): \n");
				while(i.hasNext()) {
					Map.Entry me = (Map.Entry)i.next();
				//	System.out.print(me.getKey() + ": ");
					System.out.println(me.getValue());
				}
				
				client.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
}
