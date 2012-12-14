package hram.android.PhotoOfTheDay.appwidget;

import java.io.File;

import com.bugsense.trace.BugSenseHandler;

import hram.android.PhotoOfTheDay.Constants;
import hram.android.PhotoOfTheDay.R;
import hram.android.PhotoOfTheDay.help.HelpActivity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
//import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class AndroidCustomGalleryActivity extends Activity 
{
	public static final String TAG = "GalleryActivity";
	public ImageAdapter imageAdapter;
	private Cursor cursor;
	public GridView imagegrid;
	
	private String[] imagesColumns = null;
	private String imagesOrderBy = null;
	private Uri imagesUri = null;
	private String imagesQuery = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		//Log.i(TAG, "onCreate");
		try {
			showHelpOnFirstLaunch();
		} catch (Exception e) {
			BugSenseHandler.sendExceptionMessage("Wallpaper", "showHelpOnFirstLaunch", e);
		}
	}
	
	/**
	 * We want the help screen to be shown automatically the first time a new version of the app is
	 * run. The easiest way to do this is to check android:versionCode from the manifest, and compare
	 * it to a value stored as a preference.
	 */
	private boolean showHelpOnFirstLaunch() 
	{
		try 
		{
			PackageInfo info = getPackageManager().getPackageInfo(this.getPackageName(), 0);
			int currentVersion = info.versionCode;
			// Since we're paying to talk to the PackageManager anyway, it makes sense to cache the app
			// version name here for display in the about box later.
			SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
			int lastVersion = prefs.getInt(Constants.KEY_HELP_VERSION_SHOWN, 0);
			if (currentVersion > lastVersion)
			//if (true) 
			{
				prefs.edit().putInt(Constants.KEY_HELP_VERSION_SHOWN, currentVersion).commit();
				Intent intent = new Intent(this, HelpActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				// Show the default page on a clean install, and the what's new page on an upgrade.
				String page = lastVersion == 0 ? HelpActivity.DEFAULT_PAGE : HelpActivity.WHATS_NEW_PAGE;
				//page = HelpActivity.DEFAULT_PAGE;
				intent.putExtra(HelpActivity.REQUESTED_PAGE_KEY, page);
				startActivity(intent);
				return true;
			}
	    } catch (PackageManager.NameNotFoundException e) {
	    }
	    return false;
	}

	private Cursor getImages() 
	{
		return getContentResolver().query(imagesUri, imagesColumns, imagesQuery, new String[] {SDHelper.MEDIA_TAG}, imagesOrderBy);
		/*
		if (VERSION.SDK_INT < 11)
		{
			return managedQuery(imagesUri, imagesColumns, imagesQuery, new String[] {SDHelper.MEDIA_TAG}, imagesOrderBy);
		}
		else
		{
			return getImages_API_11();
		}
		*/
	}

	@TargetApi(11)
	private Cursor getImages_API_11() 
	{
		CursorLoader cursorLoader = new CursorLoader(this, imagesUri, imagesColumns, imagesQuery, new String[] {SDHelper.MEDIA_TAG}, imagesOrderBy);
		return cursorLoader.loadInBackground();
	}
	
	/** Встраивает layout по его id r_layout в верхней части галереи. */
	private void addInflateLayout(int r_layout) {
        LinearLayout submitScoreLayout = (LinearLayout)findViewById(R.id.gallery_include);
        submitScoreLayout.removeAllViews();

        // Create new LayoutInflater - this has to be done this way, as you can't directly inflate an XML without creating an inflater object first
        LayoutInflater inflater = getLayoutInflater();
        submitScoreLayout.addView(inflater.inflate(r_layout, null));
	}

	@Override
	protected void onStart() {
		super.onStart();
		
		//Log.i(TAG, "onStart");
		
		setContentView(R.layout.gallerygrid);
		
		imagesColumns = new String[] { MediaStore.Images.Thumbnails._ID,  MediaStore.Images.Media.DATE_ADDED };
		imagesOrderBy = MediaStore.Images.Media.DATE_ADDED + " DESC";
		imagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
		imagesQuery = MediaStore.Images.Media.DESCRIPTION + " like ? ";
		
		cursor = getImages();
		imageAdapter = new ImageAdapter(this, cursor);
		imagegrid = (GridView) findViewById(R.id.PhoneImageGrid);
		imagegrid.setAdapter(imageAdapter);

		if (imageAdapter.getCount() == 0)
		{
			Toast.makeText(this, getString(R.string.noImages), Toast.LENGTH_SHORT).show();
			
			addInflateLayout(R.layout.install_widget_include);
		}
		
		imagegrid.setOnItemClickListener(new OnItemClickListener() 
		{
			public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) 
			{
				openImage(v);
			}
		});
		
	}

	@Override
	protected void onStop() {
		
		//Log.i(TAG, "onStop");

		if (cursor != null)
		{
			cursor.close();
		}
		cursor = null;
		imageAdapter = null;
		imagegrid.setAdapter(null);
		
		imagesColumns = null;
		imagesOrderBy = null;
		imagesUri = null;
		imagesQuery = null;

		super.onStop();
	}
	
	private void openImage(View v)
	{
		int id = (Integer)v.getTag(R.id.imageID);
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		String[] columns = { MediaStore.Images.Media.DATA };
		
		Cursor imagecursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, MediaStore.Images.Media._ID + " = " + id, null, MediaStore.Images.Media._ID);
		/*
		if (VERSION.SDK_INT < 11)
		{
			imagecursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, MediaStore.Images.Media._ID + " = " + id, null, MediaStore.Images.Media._ID);
		}
		else
		{
			imagecursor = getImageCursor_API_11(columns, id);
		}
		*/
		if (imagecursor != null && imagecursor.moveToFirst())
		{
			String path = imagecursor.getString(imagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
			imagecursor.close();
			intent.setDataAndType(Uri.fromFile(new File(path)),"image/*");
			startActivity(intent);
		}
	}
	
	@TargetApi(11)
	private Cursor getImageCursor_API_11(String[] columns, int id) 
	{
		return new CursorLoader(this, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, MediaStore.Images.Media._ID + " = " + id, null, MediaStore.Images.Media._ID).loadInBackground();
	}
}