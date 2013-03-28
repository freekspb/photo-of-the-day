package hram.android.PhotoOfTheDay.appwidget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bugsense.trace.BugSenseHandler;

import hram.android.PhotoOfTheDay.Constants;
import hram.android.PhotoOfTheDay.R;
import hram.android.PhotoOfTheDay.Wallpaper;
import hram.android.PhotoOfTheDay.Wallpaper.MyEngine;
import hram.android.PhotoOfTheDay.gallery.AndroidCustomGalleryActivity;
import hram.android.PhotoOfTheDay.preference.ListPreferenceConstants;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
//import android.util.Log;
import android.widget.Toast;

/**
 * @author freek
 *
 */
public class WidgetBroadcastReceiver extends BroadcastReceiver 
{
	private Wallpaper wp;
	private MyEngine eng;
	//public static final String TAG = "WidgetBroadcastReceiver";
	private List<Integer> parsers;
	private List<Integer> favoriteParsers;
	
	public WidgetBroadcastReceiver(Wallpaper wallpaper, MyEngine engine) {
		wp = wallpaper;
		eng = engine;
		parsers = getParsersInt(wp);
		favoriteParsers = getFavoriteParsersInt(wp.preferences, parsers);
	}

	private void nextParser()
	{
    	int nextParser = getNextParser(wp.getCurrentParser());
    	SharedPreferences.Editor editor = wp.preferences.edit();
        editor.putString(Constants.SOURCES_NAME, "" + nextParser);
        editor.commit();		
	}
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	//Log.i(TAG, "onReceive");
        //Ловим наш Broadcast, проверяем и выводим сообщение
        String action = intent.getAction();
        if (action.equals(WidgetBroadcastEnum.SAVE_ACTION)) {
        	try {
	        	//Log.i(TAG, "onReceive - SAVE_ACTION");
	
	            String msg = null;
	            try {
	            	msg = SDHelper.saveImage(wp);
	            } catch (Exception e) {
	            	//Log.e(TAG, "onReceive" + e.getLocalizedMessage());
	            }
	            if (msg == null)
	            {
	            	return;
	            }
	            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	            return;
			} catch (Exception e) {
				BugSenseHandler.sendException(e);
			}            
        }
        else if (action.equals(WidgetBroadcastEnum.OPEN_GALLERY_ACTION)) {
        	try {
//        		File dir = SDHelper.getAlbumStorageDir();
//        		String str = "content:/" + dir.getAbsolutePath() + File.separator;
//        	    Intent myIntent = new Intent();
//        	    myIntent.setDataAndType(Uri.parse(str), "image/*");
//                myIntent.setType("image/*");
//        		myIntent.setAction(Intent.ACTION_GET_CONTENT);
//                context.startActivity(Intent.createChooser(myIntent,
//                        "Select Picture"));
                
        		Intent myIntent = new Intent(context, AndroidCustomGalleryActivity.class);
    			myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			context.startActivity(myIntent);
        		return;
			} catch (Exception e) {
				BugSenseHandler.sendException(e);
			}
        }
        else if (action.equals(WidgetBroadcastEnum.NEXT_PARSER_ACTION)) {
        	if (wp == null || wp.preferences == null) {
        		return;
        	}
        	
        	try {
    			Toast.makeText(wp, wp.getString(R.string.updateStarted),
    					Toast.LENGTH_SHORT).show();
        		
	        	nextParser();
	            //wp.SetCurrentParser(nextParser, false);
	            //wp.StartUpdate();
	            return;
			} catch (Exception e) {
				BugSenseHandler.sendException(e);
			}
        }        
        else if (action.equals(WidgetBroadcastEnum.AUTO_NEXT_PARSER_ACTION)) {
        	if (wp == null || wp.preferences == null) {
        		return;
        	}
        	
        	try {
	        	nextParser();
	            return;
			} catch (Exception e) {
				BugSenseHandler.sendException(e);
			}
        }        
        else if (action.equals(WidgetBroadcastEnum.SETTINGS_ACTION)) {
        	try {
        		Intent myIntent = new Intent(context, FastSettings.class);
    			myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
    			context.startActivity(myIntent);
    			return;
			} catch (Exception e) {
				BugSenseHandler.sendException(e);
			}
        }
        else if (action.equals(WidgetBroadcastEnum.CHANGE_SETTINGS_ACTION)) {
        	try {
        		String key = intent.getExtras().getString(WidgetBroadcastEnum.SETTINGS_KEY);
	            eng.onPreferenceChanged(key);
    			return;
			} catch (Exception e) {
				BugSenseHandler.sendException(e);
			}
        }
    }
    
    private int getNextParser(int currentParser) {
    	if (parsers == null) {
    		return currentParser;
    	}
    	
    	int max = Collections.max(parsers);
    	
    	currentParser++;
    	while (currentParser <= max) {
        	if (parsers.contains(currentParser) && favoriteParsers.contains(currentParser)) {
        		return currentParser;
        	}
        	currentParser++;
		}
    	// если попали сюда, значит прошли все и надо перебирать сначала
    	currentParser = 0;
    	while (currentParser <= max) {
        	if (parsers.contains(currentParser) && favoriteParsers.contains(currentParser)) {
        		return currentParser;
        	}
        	currentParser++;
		}
    	return 0;
    }
    
    private static List<Integer> stringsToListInt(String[] strings) {
        List<Integer> list = new ArrayList<Integer>();
        if (strings == null)
        {
        	return list;
        }
        for (int i = 0; i < strings.length; i++) {
        	try
        	{
        		list.add(Integer.parseInt(strings[i]));
        	}
        	catch (NumberFormatException e)
        	{
        		continue;
        	}
        }
        return list;
	}
    
    private static List<Integer> getParsersInt(Context context) {
    	return stringsToListInt(context.getResources().getStringArray(hram.android.PhotoOfTheDay.R.array.sourcesValues));
	}
    
    /** Возвращает лист избранных парсеров. Если не заполнено, то вернет defaultList.
     * @param prefs Настройки.
     * @param defaultList Лист парсеров, который вернуть, если избранные парсеры не заполнены.
     * @return
     */
    private static List<Integer> getFavoriteParsersInt(SharedPreferences prefs, List<Integer> defaultList) {
    	// эти настройки должны браться из preferences
		String fvPref = prefs.getString(Constants.FAVORITE_SOURCES, ListPreferenceConstants.CHECK_ALL);
		String[] strings = fvPref.split(ListPreferenceConstants.SEPARATOR);
    	
    	// если пустой список или задано 'all'
    	if (strings == null || (strings.length > 0 && strings[0].equalsIgnoreCase(ListPreferenceConstants.CHECK_ALL)))
    	{
    		return defaultList;
    	}
    	List<Integer> res = stringsToListInt(strings);
		if (res.isEmpty())
		{
			return defaultList;
		}
		return res;
	}
    
    public void refreshFavoriteParsers() {
    	try
    	{
    		favoriteParsers = getFavoriteParsersInt(wp.preferences, parsers);
    	}
    	catch (Exception e)
    	{
			BugSenseHandler.sendException(e);    		
    	}
	}
}
