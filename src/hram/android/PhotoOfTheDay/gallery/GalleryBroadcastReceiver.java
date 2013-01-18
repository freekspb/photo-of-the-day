package hram.android.PhotoOfTheDay.gallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;

public class GalleryBroadcastReceiver  extends BroadcastReceiver
{
	private AndroidCustomGalleryActivity gallery;
	public static final String TAG = "GalleryBroadcastReceiver";
	
	public GalleryBroadcastReceiver(AndroidCustomGalleryActivity glr) {
		gallery = glr;
	}

    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.i(TAG, "onReceive");
        //Ловим наш Broadcast, проверяем и выводим сообщение
        String action = intent.getAction();
        if (action.equals(GalleryBroadcastEnum.ANSWER_WALLPAPER)) {
        	try {
        		gallery.removeInstallWallapaperInclude();
	            return;
			} catch (Exception e) {
				//BugSenseHandler.sendException(e);
			}            
        }
    }
}
