package hram.android.PhotoOfTheDay.appwidget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.bugsense.trace.BugSenseHandler;

import hram.android.PhotoOfTheDay.Constants;
import hram.android.PhotoOfTheDay.Settings;
import hram.android.PhotoOfTheDay.Wallpaper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

public class WidgetBroadcastReceiver extends BroadcastReceiver 
{
	private Wallpaper wp;
	public static final String TAG = "WidgetBroadcastReceiver";
	private List<Integer> parsers;
	
	public WidgetBroadcastReceiver(Wallpaper wallpaper) {
		wp = wallpaper;
		parsers = getParsersInt(wp);
	}

    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.i(TAG, "onReceive");
        //Ловим наш Broadcast, проверяем и выводим сообщение
        final String action = intent.getAction();
        if (action.equals(WidgetBroadcastEnum.SAVE_ACTION)) {
        	try {
	        	Log.i(TAG, "onReceive - SAVE_ACTION");
	
	            String msg = "null";
	            try {
	            	msg = SDHelper.saveImage(wp);
	            } catch (Exception e) {
	            	Log.e(TAG, "onReceive" + e.getLocalizedMessage());
	            }
	            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				BugSenseHandler.sendException(e);
			}            
        }
        else if (action.equals(WidgetBroadcastEnum.OPEN_GALLERY_ACTION)) {
        	try {
        		Intent myIntent = new Intent(context, ImagesGallery.class);
    			myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
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
	        	int nextParser = getNextParser(wp.getCurrentParser());
	        	SharedPreferences.Editor editor = wp.preferences.edit();
	            editor.putString(Constants.SOURCES_NAME, "" + nextParser);
	            editor.commit();
	            wp.SetCurrentParser(nextParser);
	            wp.StartUpdate();
			} catch (Exception e) {
				BugSenseHandler.sendException(e);
			}
        }        
        else if (action.equals(WidgetBroadcastEnum.SETTINGS_ACTION)) {
        	try {
        		Intent myIntent = new Intent(context, Settings.class);
    			myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			context.startActivity(myIntent);
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
        	if (parsers.contains(currentParser)) {
        		return currentParser;
        	}
        	currentParser++;
		}
    	// если попали сюда, значит прошли все и надо перебирать сначала
    	currentParser = 0;
    	while (currentParser <= max) {
        	if (parsers.contains(currentParser)) {
        		return currentParser;
        	}
        	currentParser++;
		}
    	return 0;
    }
    
    
    private List<Integer> getParsersInt(Context context) {
    	
    	String[] strings = context.getResources().getStringArray(hram.android.PhotoOfTheDay.R.array.sourcesValues);
        List<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < strings.length; i++) {
        	list.add(Integer.parseInt(strings[i]));
        }
        return list;
	}
}
