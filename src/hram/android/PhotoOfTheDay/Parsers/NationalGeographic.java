package hram.android.PhotoOfTheDay.Parsers;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.bugsense.trace.BugSenseHandler;

public class NationalGeographic extends BaseParser 
{

	public NationalGeographic()
	{
		//Log.i(TAG, "Создание парсера NationalGeographic");
	}
	
	@Override
	public String GetUrl() throws IOException 
	{
		try{
			// чистка памяти
    		System.gc();
    		
			Document doc = Jsoup.connect("http://photography.nationalgeographic.com/photography/photo-of-the-day/").get();
			
			Element div = doc.select("div[class=primary_photo]").first();
			if(div == null)
			{
				return null;
			}
			
			Element href = div.select("a[href]").first();
			Element src = div.select("img[src]").first();
			if(href == null || src == null)
			{
				return null;
			}
			
			return src.attr("src");
		}catch (OutOfMemoryError e) {
			//try{
			//	BugSenseHandler.sendExceptionMessage("NationalGeographic.GetUrl", "constant url", new hram.android.PhotoOfTheDay.Exceptions.OutOfMemoryError(e.getMessage()));
			//}catch (Exception e2) {}
			return null;
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
		return "NG";
	}
}
