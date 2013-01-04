package hram.android.PhotoOfTheDay.Parsers;

import hram.android.PhotoOfTheDay.Wallpaper;
import hram.android.PhotoOfTheDay.Exceptions.IncorrectDataFormat;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Random;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.xml.sax.SAXException;

import com.bugsense.trace.BugSenseHandler;

import android.content.SharedPreferences;

public class Flickr extends BaseParser 
{	
	public Flickr(Wallpaper wp, SharedPreferences preferences)
	{
		//Log.i(TAG, "Создание парсера Flickr");
		this.wp = wp;
		this.preferences = preferences;
	}
	
	@Override
	public String GetUrl() throws IOException, IncorrectDataFormat
	{
		if(preferences.getBoolean("tagPhotoEnable", false))
		{
			String tag = preferences.getString("tagPhotoValue", "");
			if(tag.length() > 0)
			{
				return GetUrlByTag(tag);
			}
		}
		
		String imageUrl = null;
		String url = "http://www.flickr.com/explore/";
		
		try{
			// чистка памяти
    		System.gc();
    		
			Document doc = Jsoup.connect(url).get();
			
			Element table = doc.select("div[class=ju photo-display-container clearfix]").first();
			if(table == null)
			{
				throw new IncorrectDataFormat(doc.ownText());
			}
			
			Element rows = table.select("div[class=row row-]").first();
			if(rows == null)
			{
				throw new IncorrectDataFormat(doc.ownText());
			}
			
			Element item = rows.select("div[class=photo-display-item]").first();
			if(item == null)
			{
				throw new IncorrectDataFormat(doc.ownText());
			}

			Element photo = item.select("span[class=photo_container pc_ju]").first();
			if(photo == null)
			{
				throw new IncorrectDataFormat(doc.ownText());
			}
			
			//Element href = photo.select("a[href]").first();
			Element src = photo.select("img[data-defer-src]").first();
			//if(href == null || src == null)
			if(src == null)
			{
				throw new IncorrectDataFormat(doc.ownText());
			}
			
			//str = href.attr("href");
			imageUrl = src.attr("data-defer-src");
			imageUrl = imageUrl.substring(0,imageUrl.length() - 5) + "b.jpg";
		}catch (OutOfMemoryError e) {
			//try{
			//	BugSenseHandler.sendExceptionMessage("Flickr.GetUrl", url, new hram.android.PhotoOfTheDay.Exceptions.OutOfMemoryError(e.getMessage()));
			//}catch (Exception e2) {}
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
		}catch (SAXException e) {
			//try{
			//	BugSenseHandler.sendExceptionMessage("Flickr.SAXException", url, new hram.android.PhotoOfTheDay.Exceptions.OutOfMemoryError(e.getMessage()));
			//}catch (Exception e2) {}
		}catch (ParserConfigurationException e) {
			//try{
			//	BugSenseHandler.sendExceptionMessage("Flickr.ParserConfigurationException", url, new hram.android.PhotoOfTheDay.Exceptions.OutOfMemoryError(e.getMessage()));
			//}catch (Exception e2) {}
		}catch (OutOfMemoryError e) {
			//try{
			//	BugSenseHandler.sendExceptionMessage("Flickr.GetUrlByTag", url, new hram.android.PhotoOfTheDay.Exceptions.OutOfMemoryError(e.getMessage()));
			//}catch (Exception e2) {}
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
