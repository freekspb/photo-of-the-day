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
			String image = GetUrlBigImage();
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
    		String image = str.substring(startPosition, endPosition - 1);
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
	
	public String GetUrlBigImage() throws IOException, IncorrectDataFormat
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
	        
	        org.w3c.dom.Element item = (org.w3c.dom.Element) items.item( 0 );
        	org.w3c.dom.NodeList links = item.getElementsByTagName("link");
        	if (links.getLength() <= 0)
        	{
        		return null;
        	}
        	
        	org.w3c.dom.Element link = (org.w3c.dom.Element) links.item( 0 );
    		String str = link.getTextContent();

	        Document doc1 = Jsoup.connect(str)
				.userAgent("Mozilla")
				.get();
	        
			Element wallPreview = doc1.select("a[class=same_wallpaper]").first();
			if(wallPreview == null)
			{
				throw new IncorrectDataFormat(doc1.ownText());
			}
			String str2 = wallPreview.attr("href");
			
	        Document doc2 = Jsoup.connect(str2)
				.userAgent("Mozilla")
				.get();
			Element wallpaper = doc2.select("img[id=wallpaper]").first();
			if(wallpaper == null)
			{
				throw new IncorrectDataFormat(doc2.ownText());
			}
			return wallpaper.attr("src");
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
