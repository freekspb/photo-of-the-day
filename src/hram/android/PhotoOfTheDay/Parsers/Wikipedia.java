package hram.android.PhotoOfTheDay.Parsers;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Wikipedia extends BaseParser {
	
	public Wikipedia()
	{
		//Log.i(TAG, "Создание парсера Wikipedia");
	}

	@Override
	public String GetUrl() throws IOException 
	{
		Document doc = Jsoup.connect("http://commons.wikimedia.org/wiki/Main_Page")
				.userAgent("Mozilla")
				.get();
		
		Element picture = doc.select("div[id=mf-picture-picture]").first();
		if(picture == null)
		{
			return null;
		}
		Element image = picture.select("img").first();
		if(image == null)
		{
			return null;
		}
		String img = image.attr("src");
		
		// пытаемся загрузить в более хорошем качестве
		try{
			String imageWidth = image.attr("width");
			img = "http:" + img.replace(imageWidth + "px-", "1280px-");
			return img;
		}
		catch (Exception e)
		{
			return img;
		}
	}

	@Override
	public boolean IsTagSupported() 
	{
		return false;
	}
	
	@Override
	public String getImageNamePrefix()
	{
		return "Wikipedia";
	}
}
