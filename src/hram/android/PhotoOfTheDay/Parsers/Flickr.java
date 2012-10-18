package hram.android.PhotoOfTheDay.Parsers;

import hram.android.PhotoOfTheDay.Wallpaper;

import java.io.IOException;
import java.net.URL;
import java.util.Date;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.xml.sax.SAXException;

import android.content.SharedPreferences;
//import android.util.Log;

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
		long now = System.currentTimeMillis(); 
		Date date = new Date(now);
		
		String url = String.format("http://www.flickr.com/explore/interesting/%d/%02d/%02d/", date.getYear() + 1900, date.getMonth() + 1, date.getDate());
		
		Document doc = Jsoup.connect(url).get();
		
		Element table = doc.select("table[class=DayView]").first();
		for(Element ite: table.select("td"))
		{
			str = ite.toString();
			if(str == null || ite.childNodes().size() == 0)
			{
				continue;
			}
			
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
		
		return imageUrl;
	}
	
	public String GetUrlByTag(String tag) throws IOException
	{
		int minIndex = rnd.nextInt(10);
		int index = 0;
		String str = java.net.URLEncoder.encode(tag);
		String url = String.format("http://api.flickr.com/services/feeds/photos_public.gne?tags=%s", str);
		
		final URL feedUrl = new URL(url);
		
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
	        			if(index++ < minIndex)
	    				{
	    					continue;
	    				}
	        			
	        			imageUrl = imageUrl.substring(0,imageUrl.length() - 5) + "z.jpg";
	        			
	        			//Log.i(TAG, "src: " + imageUrl);
	    				if(wp.GetCurrentUrl() != null && wp.GetCurrentUrl().equals(imageUrl))
	    				{
	    					//Log.i(TAG, "исчем дальше");
	    					continue;
	    				}
	    				
	    				//Log.i(TAG, "нашли: " + imageUrl);
	    				return imageUrl;
	        		}
	        	}
	        }
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
}
