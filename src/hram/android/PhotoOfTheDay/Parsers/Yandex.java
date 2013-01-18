package hram.android.PhotoOfTheDay.Parsers;

import hram.android.PhotoOfTheDay.Wallpaper;
import hram.android.PhotoOfTheDay.Exceptions.IncorrectDataFormat;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Random;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.content.SharedPreferences;
//import android.util.Log;

public class Yandex extends BaseParser 
{
	public Yandex(Wallpaper wp, SharedPreferences preferences)
	{
		//Log.i(TAG, "Создание парсера Yandex");
		this.wp = wp;
		this.preferences = preferences;
	}
	
	@Override
	public String GetUrl() throws IOException, IncorrectDataFormat
	{
		if(preferences.getBoolean("tagPhotoEnable", false))
		{
			String tag = preferences.getString("tagPhotoValue", "");
			//Log.d(TAG, "Поиск фото по тэгу " + tag);
			if(tag.length() > 0)
			{
				return GetUrlByTag(tag);
			}
		}
		
    	String url = null;
        
        try{
			// чистка памяти
    		System.gc();
    		
	        Document doc = Jsoup.connect("http://fotki.yandex.ru/calendar").get();
				
			Element table = doc.select("table[class=photos]").first();
			if(table == null)
			{
				throw new IncorrectDataFormat(doc.ownText());
			}
						
			for(Element ite: table.select("td"))
			{
				if(ite.getElementsByAttributeValue("class", "empty").isEmpty() == false)
				{
					continue;
				}
				
				Element href = ite.select("a[href]").first();
				Element src = ite.select("img[src]").first();
				if(href == null || src == null)
				{
					continue;
				}
				
				//String str = href.attr("href");
				url = src.attr("src");
				url = url.substring(0,url.length() - 3) + "XXL";
			}
        }catch (OutOfMemoryError e) {
			//try{
			//	BugSenseHandler.sendExceptionMessage("Flickr.GetUrl", url, new hram.android.PhotoOfTheDay.Exceptions.OutOfMemoryError(e.getMessage()));
			//}catch (Exception e2) {}
			return null;
		}
		
		return url;
	}
	
	public String GetUrlByTag(String tag) throws IOException
	{
		rnd = new Random(System.currentTimeMillis());
		
		String str = URLEncoder.encode(tag, "UTF-8").replace("+", "%20"); // java.net.URLEncoder.encode(tag) is deprecated
		String url = String.format("http://fotki.yandex.ru/tag/%s/", str);
		
		try{
			// чистка памяти
    		System.gc();
    		
    		Document doc = Jsoup.connect(url).get();
		
			//Log.i(TAG, "URL: " + url);
			
			Element div = doc.select("div[class=b-preview-photos]").first();
			if(div != null)
			{
				ArrayList<String> urls = new ArrayList<String>(); 
				for(Element ite: div.select("a[class=preview-link]"))
				{
					str = ite.toString();
					if(str == null || ite.childNodes().size() == 0)
					{
						continue;
					}
					
					Element src = ite.select("img[src]").first();
					if(src == null)
					{
						continue;
					}
					
					str = src.attr("src");
					str = str.substring(0, str.length() - 1) + "XXL";
					//Log.i(TAG, "src: " + str);
					if(wp.GetCurrentUrl() != null && wp.GetCurrentUrl().equals(str))
					{
						//Log.i(TAG, "исчем дальше");
						continue;
					}
					
					//Log.i(TAG, "нашли: " + str);
					urls.add(str);
				}
				
				if(urls.size() == 0)
		        {
		        	return null;
		        }
		        
		        return urls.get(rnd.nextInt(urls.size()));
			}
			
		}catch (OutOfMemoryError e) {
			//try{
			//	BugSenseHandler.sendExceptionMessage("Flickr.GetUrl", url, new hram.android.PhotoOfTheDay.Exceptions.OutOfMemoryError(e.getMessage()));
			//}catch (Exception e2) {}
			return null;
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
		return "Yandex";
	}
}
