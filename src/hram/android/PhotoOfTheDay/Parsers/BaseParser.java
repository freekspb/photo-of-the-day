package hram.android.PhotoOfTheDay.Parsers;

import hram.android.PhotoOfTheDay.Constants;

import java.io.IOException;
import java.util.Random;

public abstract class BaseParser 
{
	public static final String TAG = Constants.TAG;
	protected Random rnd = new Random(System.currentTimeMillis());
	
	public abstract String GetUrl() throws IOException;
	
	public abstract boolean IsTagSupported();
	
	public abstract String getImageNamePrefix();
}
