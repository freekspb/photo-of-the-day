package hram.android.PhotoOfTheDay.Parsers;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.bugsense.trace.BugSenseHandler;

public class Bing extends BaseParser 
{	
	public Bing()
	{
	}
	
	@Override
	public String GetUrl() throws IOException
	{
		String url = "http://feeds.feedburner.com/bingimages";
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
    		int startPosition = str.indexOf("img src=") + 9;
    		int endPosition = str.indexOf(" ", startPosition);
    		String image = str.substring(startPosition, endPosition - 1);
    		return image;
		}catch (SAXException e) {
			try{
				BugSenseHandler.sendExceptionMessage("Bing.SAXException", url, new hram.android.PhotoOfTheDay.Exceptions.OutOfMemoryError(e.getMessage()));
			}catch (Exception e2) {}
		}catch (ParserConfigurationException e) {
			try{
				BugSenseHandler.sendExceptionMessage("Bing.ParserConfigurationException", url, new hram.android.PhotoOfTheDay.Exceptions.OutOfMemoryError(e.getMessage()));
			}catch (Exception e2) {}
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
		return "Bing";
	}
}
