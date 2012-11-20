package hram.android.PhotoOfTheDay.appwidget;

import hram.android.PhotoOfTheDay.R;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter 
{
	private Context context;
	private Cursor cursor;
	private int _idColumnIndex;

	public ImageAdapter(Context context, Cursor cursor) 
	{
		this.context = context;
		this.cursor = cursor;
		if (cursor == null)
		{
			return;
		}
		_idColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
	}
	
	public int getCount() {
		return (cursor != null) ? cursor.getCount() : 0;
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) 
	{
		ImageView imageView;		
		if (convertView == null) 
		{
			imageView = new ImageView(context);
		} 
		else 
		{
			imageView = (ImageView)convertView;
		}
		
		if (getCount() == 0)
		{
			return imageView;
		}
		
		cursor.moveToPosition(position);
		int _id = cursor.getInt(_idColumnIndex);
		imageView.setImageBitmap(MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), _id, MediaStore.Images.Thumbnails.MINI_KIND, null));
		imageView.setTag(R.id.imageID, _id);
		
		return imageView;
	}
}
