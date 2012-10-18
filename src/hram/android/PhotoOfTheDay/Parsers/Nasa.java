package hram.android.PhotoOfTheDay.Parsers;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Nasa extends BaseParser 
{

	public Nasa()
	{
		//Log.i(TAG, "Создание парсера Nasa");
	}
	
	@Override
	public String GetUrl() throws IOException 
	{
		Document doc = Jsoup.connect("http://apod.nasa.gov/apod/astropix.html").get();
		
		Element  img = doc.select("img[alt]").first();
		if(img == null)
		{
			return null;
		}
		
		Element src = img.select("img[src]").first();
		if(src == null)
		{
			return null;
		}
		
		return "http://apod.nasa.gov/apod/" + src.attr("src");
	}

	@Override
	public boolean IsTagSupported() 
	{
		return false;
	}
}
