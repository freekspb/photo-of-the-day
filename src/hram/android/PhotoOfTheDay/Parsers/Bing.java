package hram.android.PhotoOfTheDay.Parsers;

import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import hram.android.PhotoOfTheDay.Exceptions.IncorrectDataFormat;

public class Bing extends BaseParser 
{
	public Bing()
	{
	}
	
	/*
	public String GetUrl2() throws IOException
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
			//try{
			//	BugSenseHandler.sendExceptionMessage("Bing.SAXException", url, new hram.android.PhotoOfTheDay.Exceptions.OutOfMemoryError(e.getMessage()));
			//}catch (Exception e2) {}
		}catch (ParserConfigurationException e) {
			//try{
			//	BugSenseHandler.sendExceptionMessage("Bing.ParserConfigurationException", url, new hram.android.PhotoOfTheDay.Exceptions.OutOfMemoryError(e.getMessage()));
			//}catch (Exception e2) {}
		}catch (OutOfMemoryError e) {}
		return null;
	}
	*/
	
	/*
	private void saveFile(String text) throws IOException
	{
		//File file = new File(getAlbumStorageDir(FOLDER).getPath(), filename);
		File file = new File(SDHelper.getAlbumStorageDir(SDHelper.FOLDER) + File.separator + "html.txt");
		if (file.exists()) {
			int a = 1;
		}
		file.createNewFile();
		//write the bytes in file
		FileWriter writer = new FileWriter(file);
		try
		{
			writer.append(text);
			writer.flush();
		}
		finally
		{
			writer.close();
		}		
	}
	*/
	
	@Override
	public String GetUrl() throws IncorrectDataFormat, IOException
	{
        try{
			// чистка памяти
    		System.gc();

	        Document doc = Jsoup.connect("http://bing.com")
				.userAgent("Mozilla")
				.get();
				
			String str = doc.html();
			int startPosition = str.indexOf("g_img={url:'");
			if (startPosition == -1)
			{
				throw new IncorrectDataFormat(doc.ownText());
			}
			startPosition += 13;
    		int endPosition = str.indexOf("'", startPosition);
    		String image = new String(str.substring(startPosition, endPosition));
    		str = null;
    		return "http://www.bing.com/" + image;
        }catch (OutOfMemoryError e) {
			//try{
			//	BugSenseHandler.sendExceptionMessage("Flickr.GetUrl", url, new hram.android.PhotoOfTheDay.Exceptions.OutOfMemoryError(e.getMessage()));
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
		return "Bing";
	}
}
