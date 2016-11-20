
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;



public class webCrawler {

	private final String USER_AGENT = "Mozilla/5.0";
	
	
	public static void main(String[] args) throws Exception {

		webCrawler webCrawler = new webCrawler();

		final String placename = "hongkong";
		System.out.println("\nEventbrite Crawler - Current crawl event location : " + placename);
		List<String> namelist = new ArrayList<String>();
		List<String> desciption = new ArrayList<String>();
		List<String> DATE = new ArrayList<String>();
		List<String> capacity = new ArrayList<String>();
		List<String> catagoryID = new ArrayList<String>();
		List<String> venue_id  = new ArrayList<String>();
		List<String> logo_id = new ArrayList<String>();
		List<String> TIME = new ArrayList<String>();
	
		int numOfPage = 3 ;
		int numof = 0;
		System.out.println("Stage1 - Crawl the events data ");
		
		for (int i=1;i<=numOfPage;i++)
		{
			String uri = "https://www.eventbriteapi.com/v3/events/search/?location.address="+placename+"&page="+String.valueOf(i);
			String str = executePost(uri);	
			JSONObject json = new JSONObject(str);
		    if (i==1)
		    {
		    	numOfPage = (int)((JSONObject)json.get("pagination")).get("page_count");
		    	System.out.println("Number of Page Crawl :"+numOfPage);
		    }
		    JSONArray array = json.getJSONArray("events");
			for(int j = 0 ; j < array.length() ; j++){	
				
				
				String des = ((JSONObject)array.getJSONObject(j).get("description")).get("text").toString();
				//if (des.length()>=700)
				//{
				//	continue;
				//} 
				++numof;
				namelist.add(((JSONObject)array.getJSONObject(j).get("name")).get("text").toString());
				desciption.add(((JSONObject)array.getJSONObject(j).get("description")).get("text").toString());
				String str2 = ((JSONObject)array.getJSONObject(j).get("start")).get("local").toString();
				DATE.add(str2.substring(0,str2.indexOf("T")));
				//TIME.add(str2.substring(str2.indexOf('T'),str2.length()));
				//DATE.add("");
				TIME.add(str2.substring(str2.indexOf('T')+1,str2.length()));
				capacity.add(array.getJSONObject(j).get("capacity").toString());
				catagoryID.add(array.getJSONObject(j).get("category_id").toString());
				venue_id.add(array.getJSONObject(j).get("venue_id").toString());
				if (!array.getJSONObject(j).get("logo").toString().equals("null"))
				{
					logo_id.add(((JSONObject)array.getJSONObject(j).get("logo")).get("url").toString());
				} else 
				{
					logo_id.add("");
				}
				
			}
			System.out.println(i+"/"+numOfPage);
		}
		System.out.println("Stage1 - Finish ");
		System.out.println("Stage2 - Crawl the locations ");
		for (int i=0;i<venue_id.size();i++)
		{
			if (venue_id.get(i).equals("null")) continue;
			String uri = "https://www.eventbriteapi.com/v3/venues/"+venue_id.get(i)+"/";
			String str = executePost(uri);
			JSONObject json = new JSONObject(str);
			String str1 = json.get("name").toString();
		    String str2 = ((JSONObject)json.get("address")).get("localized_address_display").toString();
			venue_id.set(i,str1+","+str2);
			System.out.println(i+"/"+venue_id.size());
		}
		System.out.println("Stage2 - Finish ");
		System.out.println("Stage3 - Crawl the catogaries ");
		for (int i=0;i<catagoryID.size();i++)
		{
			if (catagoryID.get(i).equals("null")) continue;
			String uri = "https://www.eventbriteapi.com/v3/categories/"+catagoryID.get(i)+"/";
			String str = executePost(uri);
			JSONObject json = new JSONObject(str);
			String str1 = json.get("name").toString();
			catagoryID.set(i,str1);
			System.out.println(i+"/"+catagoryID.size());
		}
		System.out.println("Stage3 - Finish ");
		System.out.println("Stage4 - Output to txt ");
		outputtxt(namelist,desciption,DATE,capacity,catagoryID,venue_id,logo_id,TIME);
		System.out.println("Stage4 - Finish ");
		System.out.println("Finish + total data crawl : " + numof);
		
	}
	
	public static void outputtxt(List<String> list1,List<String> list2,List<String> list3,List<String> list4,List<String> list5,List<String> list6,List<String> list7,List<String> list8)
	{
		BufferedWriter writer = null;
		try
		{
			//////////////////////The path of the txt ///////////////
			File file = new File("D:\\sql.txt");
			file.getParentFile().mkdirs();
		    writer = new BufferedWriter( new FileWriter(file));
		    Random random = new Random();
		    for(int j = 0 ; j < list1.size() ; j++){	
		    	writer.write("INSERT INTO 'EVENT' ('12',"
		    			+"'"+list1.get(j)+"',"
                        +"'"+list7.get(j)+"',"
                        +"'"+list8.get(j)+"',"
                        +"'"+list3.get(j)+"',"
                        +"'HongKong',"
                        +"'"+list4.get(j)+"',"
                        +"'"+random.nextInt(1000)+"',"
                        +"'"+list6.get(j)+"',"
                        +"'"+list5.get(j)+"',"
                        +"'"+list2.get(j)+"',"		 
                        +"'')"
		    			);
		    	writer.newLine();
			}	    
		}
		catch ( IOException e)
		{
		}
		finally
		{
		    try
		    {
		        if ( writer != null)
		        writer.close( );
		    }
		    catch ( IOException e)
		    {
		    }
		}
		
	}
	
	public static String executePost(String targetURL) {
		  HttpURLConnection connection = null;

		  String credentials = "Bearer  VVL3XUETCJW52DKLMVV5";
		  //String encoding = Base64Converter.encode(credentials.getBytes("UTF-8"));
  
		  try {
		    //Create connection
		    URL url = new URL(targetURL);
		    connection = (HttpURLConnection) url.openConnection();
		    connection.setRequestMethod("GET");
		    connection.setRequestProperty("Authorization", credentials);
		    connection.setRequestProperty("Accept", "application/json; charset=utf-8");
		    connection.setRequestProperty("Accept-Charset", "UTF-8"); 
		    
		    connection.setUseCaches(false);
		    connection.setDoOutput(true);
		    
		    //Get Response  
		    InputStream is = connection.getInputStream();
		    BufferedReader rd = new BufferedReader(new InputStreamReader(is,"UTF-8"));
		    
		    StringBuilder response = new StringBuilder(); // or StringBuffer if Java version 5+
		   
		    String line;
		    while ((line = rd.readLine()) != null) {
		      response.append(line);
		    }
		    rd.close();
		    return response.toString();
		  } catch (Exception e) {
		    e.printStackTrace();
		    return null;
		  } finally {
		    if (connection != null) {
		      connection.disconnect();
		    }
		  }
		}
}
