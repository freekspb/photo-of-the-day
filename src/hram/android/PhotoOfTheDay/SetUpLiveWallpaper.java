package hram.android.PhotoOfTheDay;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

/***
 * Активити для запуска активити выбора и установки живых обоев
 * @author hram
 *
 */
public class SetUpLiveWallpaper extends Activity 
{	
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_up_live_wallpaper);
        
        //Log.d(TAG, "Старт окна выбора живых обоев");
        
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_set_up_layout, (ViewGroup) findViewById(R.id.toast_layout_root));

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
        
        try
        {
        	Intent intent = new Intent();
        	intent.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
        	startActivity(intent);
        }
        catch(Exception e){
        	//BugSenseHandler.sendExceptionMessage("error/67505696", "После исправления", e);
        }
        
        finish();
    }
}
