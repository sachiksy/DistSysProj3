import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class HTTPFileClient {
	//Return nthIndexOf instead of the first indexOf
	public static int nthIndexOf (String str, char c, int n) {
		int pos = str.indexOf(c, 0);
		while(n-- > 0 && pos != -1) {
			pos = str.indexOf(c, pos+1);
		}
		return pos;
	}
	
	public static void main (String[] args) {
		File file = new File(args[0]);
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file.toString()));
			Socket sock;
			PrintWriter output;
			InputStream is = null;
			String url;
			
			int i = 0;
			sock = new Socket("localhost", 5005);	//connect to ProxyCache
			while((url = reader.readLine()) != null) {
				output = new PrintWriter(sock.getOutputStream());
				output.print(url);
				output.flush();
				
				is = sock.getInputStream();
				//Receive HTML file
				try {
					//int buffer = sock.getReceiveBufferSize();
					//System.out.println("Buffer is: " + buffer);	//tk
					int nthIndex = nthIndexOf(url, ':', 1);
					url = url.substring(nthIndex + 1);
					System.out.println("File name: " + url);	//tk
					FileOutputStream fos = new FileOutputStream(url);
					BufferedOutputStream bos = new BufferedOutputStream(fos);
					byte[] data = new byte[1024];
					int count;
					
					System.out.println("Waiting/Blocked...");	//tk
					while((count = is.read(data)) > 0) {
						System.out.println("Reading: " + count);	//tk
						bos.write(data, 0, count);
					}
					
					bos.flush();
					bos.close();
					is.close();
					output.close();
				} catch (FileNotFoundException e) {
					//nothing b/c we are writing the file, so we are creating it or overwriting it so this should never trigger
				}
			}
			//((Closeable) file).close();	//"close" file
			//sock.close();				//close socket
		} catch (FileNotFoundException e) {
			System.out.println(file.toString() + " does not exist. more details: " + e);
		} catch (IOException e) {
			System.out.println("Failed to read a line from " + file.toString() + ". more details: " + e);
		}
	}
}
