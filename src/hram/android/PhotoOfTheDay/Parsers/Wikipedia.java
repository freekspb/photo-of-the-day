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
		Document doc = Jsoup.connect("http://commons.wikimedia.org/wiki/Main_Page").get();
		
		Element table = doc.select("table[class=toccolours]").first();
		for(Element td: table.select("td[class=toccolours]"))
		{
			if(td.toString() == null || td.childNodes().size() == 0)
			{
				continue;
			}
			
			Element src = td.select("img[src]").first();
			if(src == null)
			{
				return null;
			}
			
			String imageUrl = src.attr("src");
			
			int start = imageUrl.indexOf("/thumb/") + 6;
			int end = imageUrl.indexOf(".jpg") + 4;
			
			return "http://upload.wikimedia.org/wikipedia/commons" + imageUrl.substring(start, end);
		}
		
		return null;
	}

	@Override
	public boolean IsTagSupported() 
	{
		return false;
	}
}
