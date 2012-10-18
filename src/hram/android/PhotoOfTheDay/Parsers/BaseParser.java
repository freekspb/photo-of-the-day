package hram.android.PhotoOfTheDay.Parsers;

import java.io.IOException;
import java.util.Random;

public abstract class BaseParser 
{
	public static final String TAG = "Wallpaper";
	protected Random rnd = new Random(System.currentTimeMillis());
	
	public abstract String GetUrl() throws IOException;
	
	public abstract boolean IsTagSupported();
}
