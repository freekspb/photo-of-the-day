package hram.android.PhotoOfTheDay.appwidget;

import java.io.File;

import hram.android.PhotoOfTheDay.Wallpaper;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
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
        else if (action.equals(WidgetBroadcastEnum.OPEN_GALLERY_ACTION)) {
        	try {
              	File dir = SDHelper.getAlbumStorageDir();
    			if (!dir.exists()) {
    				return;
    			}
    			//String str = "content:/" + dir.getAbsolutePath();
//    			String str = "content:/" + Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();
//    			//Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT,  Uri.fromFile(dir));
//    			Intent galleryIntent = new Intent(Intent.ACTION_PICK);
//    			intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*"); 
//    			galleryIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//    			context.startActivity(galleryIntent);
    			
    			Intent myIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    			myIntent.setType("image/*");
    			myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			myIntent.setAction(Intent.ACTION_VIEW);
//    			Intent myIntent2 = Intent.createChooser(myIntent, "Select Picture");
//    			myIntent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    			context.startActivity(myIntent);
    			
//    			Intent myIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"));
//    			myIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//    			context.startActivity(myIntent);
			} catch (Exception e) {
				int a = 1;
			}
      }
        
   }	
}
