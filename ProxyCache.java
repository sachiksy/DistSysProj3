import java.net.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

//One cache, multiple clients
public class ProxyCache {
	final static int PORT = 5005;
	
    static ServerSocket server;
    static Map<String, String> cache;
    static int maxDox, hit, miss;
    
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
    		ReentrantReadWriteLock locker = new ReentrantReadWriteLock();
    		
			String userInput, localFile = null;
			hit = 0;
			miss = 0;
			System.out.println("hello");	//tk
			try {
				BufferedReader br = new BufferedReader(new InputStreamReader(client.getInputStream()));
				System.out.println("my");	//tk
				while((userInput = br.readLine()) != null) {
					System.out.println("baby");	//tk
					//Check Cache for local copy
					if (cache.containsKey(userInput)) {
						//Access requested URL to reorder LinkedHashMap for LRU purposes
						String temp = get(userInput);
						hit += 1;
						System.out.println("HIT: " + hit);
						
						//Send local copy to client
							//tk
					}
					//No local, get HTML page from Web Server
					else {
						System.out.println("URL: " + userInput);	//tk
						
						//Get Document from Web Server
						URL url = new URL(userInput);
						HttpURLConnection connection = (HttpURLConnection)url.openConnection();
						int code = connection.getResponseCode();	//should be 200 for OK
						if (HttpURLConnection.HTTP_OK == code) {
							connection.connect();
							InputStream is = connection.getInputStream();
							int nthIndex = nthIndexOf(userInput, ':', 1);
							localFile = userInput.substring(nthIndex + 1);
							FileOutputStream fos = new FileOutputStream(localFile);
							
							int j;
							try {
								while ((j = is.read()) != -1) {
									fos.write(j);
								}
							} catch (IOException e) {
								System.out.println("ERROR: Problems reading from Web Server");
								System.exit(0);
							}

							try {
								is.close();
								fos.close();
							} catch (IOException e) {
								System.out.println("ERROR: Problems closing the streams used for getting Web Content.");
								System.exit(0);
							}
						}		
			
						int nthIndex = nthIndexOf(userInput, ':', 1);
						localFile = userInput.substring(nthIndex + 1);
						
						//Add to Cache
						put(userInput, localFile);
						miss += 1;
						System.out.println("MISS: " + miss);
//works up to here			
						//Send local copy to client
						System.out.println("f");	//tk
						File f = new File(localFile);	//avoid FileNotFoundException
						System.out.println("bis");	//tk
						BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));	//stream reads from file
						System.out.println("bos");	//tk
						BufferedOutputStream bos = new BufferedOutputStream(client.getOutputStream());	//stream writes to client
						byte data[] = new byte[1024];
						int read;

						System.out.println("Entering lock zone");	//tk
						locker.readLock().lock();

						//read file, send bytes to client
						while((read = bis.read(data)) != -1) {
							bos.write(data, 0, read);
							bos.flush();
						}
						
						System.out.println("Unlocking...");	//tk
						locker.readLock().unlock();
						
						bis.close();
						System.out.println(localFile + " sent");
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
}
