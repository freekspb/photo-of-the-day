package hram.android.PhotoOfTheDay.Parsers;

import hram.android.PhotoOfTheDay.Wallpaper;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import android.content.SharedPreferences;
//import android.util.Log;

public class Yandex extends BaseParser 
{
	private SharedPreferences preferences;
	private Wallpaper wp;
	
	public Yandex(Wallpaper wp, SharedPreferences preferences)
	{
		//Log.i(TAG, "Создание парсера Yandex");
		this.wp = wp;
		this.preferences = preferences;
	}
	
	@Override
	public String GetUrl() throws IOException
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
        String str;
        
        Document doc = Jsoup.connect("http://fotki.yandex.ru/calendar").get();
			
		Element table = doc.select("table[class=photos]").first();
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
			url = src.attr("src");
			url = url.substring(0,url.length() - 3) + "L";
		}
		
		return url;
	}
	
	public String GetUrlByTag(String tag) throws IOException
	{
		int minIndex = rnd.nextInt(20);
		int index = 0;
		String str = java.net.URLEncoder.encode(tag);
		String url = String.format("http://fotki.yandex.ru/tag/%s/", str);
		Document doc = Jsoup.connect(url).get();
		
		//Log.i(TAG, "URL: " + url);
		
		Element div = doc.select("div[class=b-preview-photos]").first();
		if(div != null)
		{
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
				
				if(index++ < minIndex)
				{
					continue;
				}
				
				str = src.attr("src");
				str = str.substring(0, str.length() - 1) + "L";
				//Log.i(TAG, "src: " + str);
				if(wp.GetCurrentUrl() != null && wp.GetCurrentUrl().equals(str))
				{
					//Log.i(TAG, "исчем дальше");
					continue;
				}
				
				//Log.i(TAG, "нашли: " + str);
				return str;
			}
		}
		return null;
	}
	
	@Override
	public boolean IsTagSupported() 
	{
		return true;
	}
}
