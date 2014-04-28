import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
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
	
	@SuppressWarnings("resource")
	public static void main (String[] args) throws IOException {
		File file = new File(args[0]);

			BufferedReader br = new BufferedReader(new FileReader(file.toString()));	//stream reads from file of URls
			Socket sock;
			PrintWriter output;
			String url;
			
			sock = new Socket("localhost", 5005);	//connect to ProxyCache
			while((url = br.readLine()) != null) {	//read contents of file of URLs
				if(url.trim().isEmpty()) {
					continue;
				}
				
				output = new PrintWriter(sock.getOutputStream());	//stream writes URL to ProxyCache
				output.println(url);
				output.flush();
				
				//Receive HTML file
				int nthIndex = nthIndexOf(url, ':', 1);
				url = url.substring(nthIndex + 1);

				File f = new File(url);	//create new file to avoid FileNotFoundException with FileOutputStream
				BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));	//stream writes to file
				BufferedInputStream bis = new BufferedInputStream(sock.getInputStream());	//stream reads from ProxyCache
				byte data[] = new byte[1024];
				int read;
				
				while((read = bis.read(data)) != -1) {
					bos.write(data, 0, read);
					bos.flush();
					if (read != 1024) {
						break;
					}
				}
				
				System.out.println(url + " received");
			}
			//((Closeable) file).close();	//"close" file
			//sock.close();				//close socket	
	}
}
