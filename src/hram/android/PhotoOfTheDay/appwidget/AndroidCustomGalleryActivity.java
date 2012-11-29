package hram.android.PhotoOfTheDay.appwidget;

import hram.android.PhotoOfTheDay.R;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

public class AndroidCustomGalleryActivity extends Activity 
{
	public ImageAdapter imageAdapter;
	public GridView imagegrid;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gallerygrid);
		
		imageAdapter = new ImageAdapter(this, GetImages());
		imagegrid = (GridView) findViewById(R.id.PhoneImageGrid);
		imagegrid.setAdapter(imageAdapter);
		
		if (imageAdapter.getCount() == 0)
		{
			Toast.makeText(this, getString(R.string.noImages), Toast.LENGTH_SHORT).show();
		}
		
		imagegrid.setOnItemClickListener(new OnItemClickListener() 
		{
			public void onItemClick(AdapterView<?> arg0, View v, int arg2, long arg3) 
			{
				int id = (Integer)v.getTag(R.id.imageID);
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				final String[] columns = { MediaStore.Images.Media.DATA };
				Cursor imagecursor = managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, MediaStore.Images.Media._ID + " = " + id, null, MediaStore.Images.Media._ID);
				
				if (imagecursor != null && imagecursor.moveToFirst())
				{
					String path = imagecursor.getString(imagecursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
					intent.setDataAndType(Uri.fromFile(new File(path)),"image/*");
					startActivity(intent);
				}
			}
		});
	}
	
	private Cursor GetImages() 
	{
		final String[] columns = { MediaStore.Images.Thumbnails._ID,  MediaStore.Images.Media.DATE_ADDED };
		final String orderBy = MediaStore.Images.Media._ID;
		
		return managedQuery(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns,
				MediaStore.Images.Media.DESCRIPTION + " like ? ",
			    new String[] {SDHelper.MEDIA_TAG},
			    orderBy);
	}
}