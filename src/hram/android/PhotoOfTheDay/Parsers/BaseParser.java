package hram.android.PhotoOfTheDay.Parsers;

//import hram.android.PhotoOfTheDay.Constants;
import hram.android.PhotoOfTheDay.Wallpaper;
import hram.android.PhotoOfTheDay.Exceptions.IncorrectDataFormat;

import java.io.IOException;
import java.util.Random;

import android.content.SharedPreferences;

public abstract class BaseParser 
{
	//public static final String TAG = Constants.TAG;
	protected Random rnd = new Random(System.currentTimeMillis());
	protected SharedPreferences preferences;
	protected Wallpaper wp;
	
	public abstract String GetUrl() throws IOException, IncorrectDataFormat;
	
	public abstract boolean IsTagSupported();
	
	public abstract String getImageNamePrefix();
}
