package hram.android.PhotoOfTheDay.appwidget;

import hram.android.PhotoOfTheDay.R;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class SDImagesScaner extends Activity implements MediaScannerConnectionClient{
	public static final String TAG = "SDPhotoScaner";

    public String[] allFiles;
	private String SCAN_PATH ;
	private static final String FILE_TYPE = "image/*";
	
	private MediaScannerConnection conn;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	    File folder = SDHelper.getAlbumStorageDir();
	    if (!folder.exists()) {
		    Log.d(TAG, "Нет сохраненных изображений");
		    Toast.makeText(this, getString(R.string.noImages), Toast.LENGTH_SHORT).show();
	    	return;
	    }
	    
	    allFiles = folder.list();
	    if (allFiles.length <= 0) {
		    Log.d(TAG, "Нет файлов в папке");
		    Toast.makeText(this, getString(R.string.noImages), Toast.LENGTH_SHORT).show();
		    return;
	    }
	    SCAN_PATH = SDHelper.getAlbumStorageDir() + File.separator + allFiles[0];
	    startScan();
    }
	
    private void startScan()
    {
	    Log.d(TAG, "success" + conn);
	    if(conn!=null)
	    {
	    	conn.disconnect();
	    }
	    conn = new MediaScannerConnection(this,this);
	    conn.connect();
    }

    public void onMediaScannerConnected() {
	    Log.d(TAG, "onMediaScannerConnected success" + conn);
	    conn.scanFile(SCAN_PATH, FILE_TYPE);    
	}

	public void onScanCompleted(String path, Uri uri) {
	    try {
	        Log.d(TAG, "onScanCompleted " + uri + "success" + conn);
	        if (uri != null) 
	        {
    			Intent myIntent = new Intent();
    			myIntent.setDataAndType(uri, "image/*");
    			myIntent.setAction(Intent.ACTION_VIEW);
    			startActivity(myIntent);
	        }
        }
	    catch (Exception e) {
			Log.e(TAG, "onScanCompleted " + e.getLocalizedMessage());	    	
	    }
	    finally
        {
	        conn.disconnect();
	        conn = null;
        }
    }
}
