package hram.android.PhotoOfTheDay.Parsers;

import hram.android.PhotoOfTheDay.Exceptions.IncorrectDataFormat;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.xml.sax.SAXException;

public class DieselStation extends BaseParser 
{	
	public DieselStation()
	{
	}
	
	@Override
	public String GetUrl() throws IOException, IncorrectDataFormat
	{
		try
		{
			String image = GetUrlBigImageMobile();
			if (image != null)
			{
				return image;
			}
			return GetUrlSmallImage();
		}catch (IOException e) {
			return GetUrlSmallImage();
		}catch (IncorrectDataFormat e) {
			return GetUrlSmallImage();
		}
	}
	
	public String GetUrlSmallImage() throws IOException
	{
		String url = "http://feeds.feedburner.com/dieselstation/txzD";
		final URL feedUrl = new URL(url);
		
		//ArrayList<String> urls = new ArrayList<String>(); 
		try {
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
	        
	        org.w3c.dom.Element link = (org.w3c.dom.Element) items.item( 0 );
        	org.w3c.dom.NodeList descriptions = link.getElementsByTagName("description");
        	if (descriptions.getLength() <= 0)
        	{
        		return null;
        	}
        	
        	org.w3c.dom.Element desc = (org.w3c.dom.Element) descriptions.item( 0 );
    		String str = desc.getTextContent();
    		int startPosition = str.indexOf("src=") + 5;
    		int endPosition = str.indexOf(" ", startPosition);
    		String image = new String(str.substring(startPosition, endPosition - 1));
    		return image;
		}catch (SAXException e) {
			//try{
			//	BugSenseHandler.sendExceptionMessage("EarthShots.SAXException", url, new hram.android.PhotoOfTheDay.Exceptions.OutOfMemoryError(e.getMessage()));
			//}catch (Exception e2) {}
		}catch (ParserConfigurationException e) {
			//try{
			//	BugSenseHandler.sendExceptionMessage("EarthShots.ParserConfigurationException", url, new hram.android.PhotoOfTheDay.Exceptions.OutOfMemoryError(e.getMessage()));
			//}catch (Exception e2) {}
		}catch (OutOfMemoryError e) {}
		return null;
	}
		
	public String GetUrlBigImageMobile() throws IOException, IncorrectDataFormat
	{
		String url = "http://m.dieselstation.com/";
		final String userAgent = "Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
		try {
	        Document doc0 = Jsoup.connect(url)
				.userAgent(userAgent)
				.get();
	        
			Element content = doc0.select("div[class=article]").first();
			if(content == null)
			{
				throw new IncorrectDataFormat(doc0.ownText());
			}
			Element href = content.select("a").first();
			url = href.attr("href");

	        Document doc1 = Jsoup.connect(url)
				.userAgent(userAgent)
				.get();
	        
			Element gallery = doc1.select("div[class=gallery]").first();
			if(gallery == null)
			{
				throw new IncorrectDataFormat(doc1.ownText());
			}
			Element galleryItem = gallery.select("div").first();
			if(galleryItem == null)
			{
				throw new IncorrectDataFormat(doc1.ownText());
			}
			Element galleryItemAttr = galleryItem.select("a").first();
			if(galleryItemAttr == null)
			{
				throw new IncorrectDataFormat(doc1.ownText());
			}
			url = galleryItemAttr.attr("href");
			
	        Document doc2 = Jsoup.connect(url)
				.userAgent(userAgent)
				.get();
			Element img = doc2.select("img[id=wallpaper]").first();
			if(img == null)
			{
				return null;
			}
			String imageUrl;
			try {
				imageUrl = img.attr("src");
			} catch (Exception e) {
				return null;
			}
			return imageUrl;
		}catch (OutOfMemoryError e) {}
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
		return "DS";
	}
}
