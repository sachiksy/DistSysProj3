import java.sql.Timestamp;

public class CacheData {
	
	private String URL;
	private Timestamp tstamp;
	private String filename;
	private static int tempCount = 0;
	
	public CacheData(String myURL){
		URL = myURL;
		java.util.Date now = new java.util.Date();
		tstamp = new Timestamp(now.getTime());
		filename = "temp"+tempCount+".html";
		tempCount++;
	}
	public CacheData(){
		URL = null;
		tstamp = null;
		filename = null;
	}
	
	//Sets timestamp to current time
	public void updateTimestamp(){
		java.util.Date now = new java.util.Date();
		tstamp.setTime(now.getTime());
	}
	
	public String getURL(){
		return URL;
	}
	
	public String getFilename(){
		return filename;
	}
	
	public int compareTo(CacheData data){
		return tstamp.compareTo(data.tstamp);
	}
	
}
