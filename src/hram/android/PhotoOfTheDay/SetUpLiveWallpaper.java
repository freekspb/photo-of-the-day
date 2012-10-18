package hram.android.PhotoOfTheDay;

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class SetUpLiveWallpaper extends Activity 
{
	private static final String TAG = "Wallpaper";
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_up_live_wallpaper);
        
        //Log.d(TAG, "Старт окна выбора живых обоев");
        
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast_set_up_layout, (ViewGroup) findViewById(R.id.toast_layout_root));

        ImageView image = (ImageView) layout.findViewById(R.id.image);
        image.setImageResource(R.drawable.icon);
        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(R.string.setupDescription);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
        
        Intent intent = new Intent();
        intent.setAction(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER);
        startActivity(intent);
        finish();
    }
}
