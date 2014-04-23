import java.sql.Timestamp;


public class CacheData {
	
	private String URL;
	private Timestamp tstamp;
	private String filename;
	private static int tempCount = 0;
	
	public CacheData(String myURL){
		URL = myURL;
		tstamp.setTime(System.currentTimeMillis());
		filename = "temp"+tempCount;
		tempCount++;
	}
	public CacheData(){
		URL = null;
		tstamp = null;
		filename = null;
	}
	
	//Sets timestamp to current time
	public void updateTimestamp(){
		tstamp.setTime(System.currentTimeMillis());
	}
	
	public String getURL(){
		return URL;
	}
	
	public String getFilename(){
		return filename+".html";
	}
	
	public String getFolderName(){
		return filename;
	}
	
	public int compareTo(CacheData data){
		return tstamp.compareTo(data.tstamp);
	}
	
}
