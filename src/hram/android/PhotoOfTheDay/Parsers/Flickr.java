package hram.android.PhotoOfTheDay.Parsers;

import hram.android.PhotoOfTheDay.Wallpaper;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.xml.sax.SAXException;

import com.bugsense.trace.BugSenseHandler;

import android.content.SharedPreferences;

public class Flickr extends BaseParser 
{	
	private SharedPreferences preferences;
	private Wallpaper wp;
	
	public Flickr(Wallpaper wp, SharedPreferences preferences)
	{
		//Log.i(TAG, "Создание парсера Flickr");
		this.wp = wp;
		this.preferences = preferences;
	}
	
	@Override
	public String GetUrl() throws IOException 
	{
		if(preferences.getBoolean("tagPhotoEnable", false))
		{
			String tag = preferences.getString("tagPhotoValue", "");
			if(tag.length() > 0)
			{
				return GetUrlByTag(tag);
			}
		}
		
		String str;
		String imageUrl = null;
		
		// новый способ получения даты
		Calendar c = Calendar.getInstance();
		String url = String.format("http://www.flickr.com/explore/interesting/%d/%02d/%02d/", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DATE));
		
		try{
			// чистка памяти
    		System.gc(); 
    		
			Document doc = Jsoup.connect(url).get();
			
			Element table = doc.select("table[class=DayView]").first();
			for(Element ite: table.select("td"))
			{
				Element href = ite.select("a[href]").first();
				Element src = ite.select("img[src]").first();
				if(href == null || src == null)
				{
					continue;
				}
				
				str = href.attr("href");
				imageUrl = src.attr("src");
				imageUrl = imageUrl.substring(0,imageUrl.length() - 5) + "z.jpg";
				break;
			}
			
		}catch (OutOfMemoryError e) { // https://www.bugsense.com/dashboard/project/ab3f3ed5#error/68931299
			try{
				BugSenseHandler.sendExceptionMessage("Flickr.GetUrl", url, new hram.android.PhotoOfTheDay.Exceptions.OutOfMemoryError(e.getMessage()));
			}catch (Exception e2) {}
			return null;
		}
	
		
		return imageUrl;
	}
	
	public String GetUrlByTag(String tag) throws IOException
	{
		rnd = new Random(System.currentTimeMillis());
		
		String url = "http://api.flickr.com/services/feeds/photos_public.gne?tags=";
		boolean first = true;
		for(String str: tag.split(" "))
		{
			if(first)
			{
				url += URLEncoder.encode(str, "UTF-8");
				first = false;
			}
			else
			{
				url += "," + URLEncoder.encode(str, "UTF-8");
			}
		}
		
		final URL feedUrl = new URL(url);
		
		ArrayList<String> urls = new ArrayList<String>(); 
		try {
			// Создаем фабрику для создания постоителя документов.
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        // Непосредственно постоитель
	        DocumentBuilder builder = factory.newDocumentBuilder();
			// Собственно наш документ
	        org.w3c.dom.Document doc = builder.parse(feedUrl.openConnection().getInputStream());
	        
	        org.w3c.dom.Element root = doc.getDocumentElement();
	        org.w3c.dom.NodeList items = root.getElementsByTagName("link");
	        for (int i=0;i<items.getLength();i++)
	        {
	        	org.w3c.dom.Element link = (org.w3c.dom.Element) items.item( i );
	        	
	        	if(link.getAttribute("rel").equals("enclosure"))
	        	{
	        		String imageUrl = link.getAttribute("href");
	        		if(imageUrl.length() > 0)
	        		{
	        			imageUrl = imageUrl.substring(0,imageUrl.length() - 5) + "z.jpg";
	        			
	        			//Log.i(TAG, "src: " + imageUrl);
	    				if(wp.GetCurrentUrl() != null && wp.GetCurrentUrl().equals(imageUrl))
	    				{
	    					//Log.i(TAG, "исчем дальше");
	    					continue;
	    				}
	    				
	    				//Log.i(TAG, "нашли: " + imageUrl);
	    				urls.add(imageUrl);
	        		}
	        	}
	        }
	        
	        if(urls.size() == 0)
	        {
	        	return null;
	        }
	        
	        return urls.get(rnd.nextInt(urls.size()));
		} 
		catch (SAXException e) {
		}catch (ParserConfigurationException e) {
		}
		return null;
	}

	@Override
	public boolean IsTagSupported() 
	{
		return true;
	}
	
	@Override
	public String getImageNamePrefix()
	{
		return "Flickr";
	}
}
