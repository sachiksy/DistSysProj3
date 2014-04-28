import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

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
			String line;
			
			int i = 0;
			sock = new Socket("localhost", 5005);	//connect to ProxyCache
			while((line = br.readLine()) != null) {	//read contents of file of URLs
				if (line.trim().isEmpty()){
					continue;
				}
				output = new PrintWriter(sock.getOutputStream());	//stream writes URL to ProxyCache
				output.println(line);
				output.flush();
				
				//
				URL url = new URL(line);
				HttpURLConnection connection = (HttpURLConnection)url.openConnection();
				int code = connection.getResponseCode();	//should be 200 for OK
				if (HttpURLConnection.HTTP_OK == code) {
					connection.connect();
					InputStream is = connection.getInputStream();
					int nthIndex = nthIndexOf(line, ':', 1);
					line = line.substring(nthIndex + 1);
					File fela = new File(line);
					FileOutputStream fos = new FileOutputStream(fela);
					
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
			}
			
			System.out.println("Client is done!");
			//((Closeable) file).close();	//"close" file
			sock.close();				//close socket	
	}
}
