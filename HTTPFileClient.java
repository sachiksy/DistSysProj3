import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
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
	
	public static void main (String[] args) throws IOException {
		File file = new File(args[0]);

			BufferedReader br = new BufferedReader(new FileReader(file.toString()));	//stream reads from file of URls
			Socket sock;
			PrintWriter output;
			String url;
			
			int i = 0;
			sock = new Socket("localhost", 5005);	//connect to ProxyCache
			while((url = br.readLine()) != null) {	//read contents of file of URLs
				output = new PrintWriter(sock.getOutputStream());	//stream writes URL to ProxyCache
				output.print(url);
				output.flush();
				
				//Receive HTML file
				int nthIndex = nthIndexOf(url, ':', 1);
				url = url.substring(nthIndex + 1);
//works up to here					
				System.out.println("bis");	//tk
				BufferedInputStream bis = new BufferedInputStream(sock.getInputStream());	//stream reads from ProxyCache
				System.out.println("f");	//tk
				File f = new File(url);	//create new file to avoid FileNotFoundException with FileOutputStream
				System.out.println("bos");	//tk
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));	//stream writes to file
				byte data[] = new byte[1024];
				int read;
				
				System.out.println("READING => WRITING");	//tk
				while((read = bis.read(data)) != -1) {
					System.out.println("birds: " + read);	//tk
					bos.write(data, 0, read);
					System.out.println("flying");	//tk
					bos.flush();
					System.out.println("high");	//tk
				}
				
				System.out.println(url + " received");
			}
			//((Closeable) file).close();	//"close" file
			//sock.close();				//close socket	
	}
}
