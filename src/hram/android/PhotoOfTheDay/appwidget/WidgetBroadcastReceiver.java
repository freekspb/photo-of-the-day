package hram.android.PhotoOfTheDay.appwidget;

import hram.android.PhotoOfTheDay.Wallpaper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class WidgetBroadcastReceiver extends BroadcastReceiver 
{
	private Wallpaper wp;
	public static final String TAG = "WidgetBroadcastReceiver";
	
	public WidgetBroadcastReceiver(Wallpaper wallpaper) {
		wp = wallpaper;
	}

    @Override
    public void onReceive(Context context, Intent intent) {
    	Log.i(TAG, "onReceive");
        //Ловим наш Broadcast, проверяем и выводим сообщение
        final String action = intent.getAction();
        if (action.equals(WidgetBroadcastEnum.SAVE_ACTION)) {
        	Log.i(TAG, "onReceive - SAVE_ACTION");

            String msg = "null";
            try {
            	msg = SDHelper.saveImage(wp);
            } catch (Exception e) {
            	Log.e("Error", e.getLocalizedMessage());
            }
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
        }
   }	
}
