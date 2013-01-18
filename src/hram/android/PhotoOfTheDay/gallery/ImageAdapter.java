package hram.android.PhotoOfTheDay.gallery;

import java.sql.Date;
import java.text.DateFormat;

import hram.android.PhotoOfTheDay.R;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageAdapter extends BaseAdapter 
{
	private Context context;
	private Cursor cursor;
	private int _idColumnIndex;
	private int _dateColumnIndex;
	LayoutInflater inflater;

	public ImageAdapter(Context context, Cursor cursor) 
	{
		this.context = context;
		this.cursor = cursor;
		if (cursor == null)
		{
			return;
		}
		_idColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID);
		_dateColumnIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED);
		inflater = LayoutInflater.from(context);
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
	
	public static class ViewHolder {
		public TextView tv1;
		public TextView tv2;
		public ImageView image;
	}

	public View getView(int position, View convertView, ViewGroup parent) 
	{
		ViewHolder holder;		
		if (convertView == null) 
		{
			convertView = inflater.inflate(R.layout.item_gallery, null);
			
			holder = new ViewHolder();
			holder.tv1 = (TextView)convertView.findViewById(R.id.tv1);
			holder.tv2 = (TextView)convertView.findViewById(R.id.tv2);
			holder.image = (ImageView)convertView.findViewById(R.id.image);
			convertView.setTag(holder);
		} 
		else 
		{
			holder = (ViewHolder)convertView.getTag();
		}
		
		// заполнение
		cursor.moveToPosition(position);
		// картинка
		int _id = cursor.getInt(_idColumnIndex);
		holder.image.setImageBitmap(MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), _id, MediaStore.Images.Thumbnails.MINI_KIND, null));
		// дата картинки
		String date_added = cursor.getString(_dateColumnIndex);
		Date d = new Date(Long.parseLong(date_added) * 1000);
		DateFormat format = DateFormat.getDateInstance();
		holder.tv1.setText(format.format(d));
		holder.tv2.setText("");
		
		convertView.setTag(R.id.imageID, _id);
		
		return convertView;
	}
}
