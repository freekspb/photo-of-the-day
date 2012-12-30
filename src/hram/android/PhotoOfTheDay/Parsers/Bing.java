package hram.android.PhotoOfTheDay.Parsers;

import hram.android.PhotoOfTheDay.R;

import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import android.content.Context;

import com.bugsense.trace.BugSenseHandler;

public class Bing extends BaseParser 
{
	private Context wp;
	
	public Bing(Context context)
	{
		this.wp = context;
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
	        
	        org.w3c.dom.Element link = null;
	        // перебираем все, чтобы найти US item
	        for (int i=0; i<items.getLength(); i++)
	        {
		        link = (org.w3c.dom.Element) items.item( i );
	        	org.w3c.dom.NodeList titles = link.getElementsByTagName("title");
	        	if (titles.getLength() <= 0)
	        	{
	        		continue;
	        	}
	        	org.w3c.dom.Element title = (org.w3c.dom.Element) titles.item( 0 );
	        	// проверяем наличие (Bing United States) в заголовке
	    		String str = title.getTextContent();
		        if (str.contains(wp.getString(R.string.bingCountryTag)))
		        {
		        	// заканчиваем поиск
		        	break;
		        }
	        }
	        
	        // если не нашли для US
	        if (link == null)
	        {
	        	// то берем первую
	        	link = (org.w3c.dom.Element) items.item( 0 );	
	        }
	        
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
