package hram.android.PhotoOfTheDay.Parsers;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

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
		return GetUrl2();
		
		/*
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
		}*/
	}

	public String GetUrl2() throws IOException
	{
		final URL feedUrl = new URL("http://toolserver.org/~daniel/potd/commons/potd-400x300.rss");
		
		try {

			// -----Сначала в rss находим страницу сегодняшней картинки-----
			// Создаем фабрику для создания постоителя документов.
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	        // Непосредственно постhоитель
	        DocumentBuilder builder = factory.newDocumentBuilder();
			// Собственно наш документ
	        org.w3c.dom.Document doc = builder.parse(feedUrl.openConnection().getInputStream());
	        
	        org.w3c.dom.Element root = doc.getDocumentElement();
	        
	        org.w3c.dom.NodeList items = root.getElementsByTagName("item");
	        if (items.getLength() <= 0)
        	{
        		return null;
        	}
	        
	        org.w3c.dom.Element item = (org.w3c.dom.Element) items.item( 0 );
        	org.w3c.dom.NodeList links = item.getElementsByTagName("link");
        	if (links.getLength() <= 0)
        	{
        		return null;
        	}
	        org.w3c.dom.Element link = (org.w3c.dom.Element) links.item( 0 );
	        
			Document doc2 = Jsoup.connect(link.getTextContent())
					.userAgent("Mozilla")
					.get();
			
			Element span = doc2.select("span[class=mw-filepage-other-resolutions]").first();
			if(span == null)
			{
				return null;
			}
			Element image = span.select("a").last();
			if(image == null)
			{
				return null;
			}
			return "http:" + image.attr("href");
		}
		catch(Exception e) {}
		return null;
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
