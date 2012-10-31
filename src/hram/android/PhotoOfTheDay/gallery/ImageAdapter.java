package hram.android.PhotoOfTheDay.gallery;

import java.io.File;

import hram.android.PhotoOfTheDay.R;
import hram.android.PhotoOfTheDay.appwidget.SDHelper;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.ImageView;

public class ImageAdapter extends BaseAdapter {
	public static final String TAG = "ImageAdapter";

	int mGalleryItemBackground;
    private Context mContext;
    private String[] images;
    
    public ImageAdapter(Context context) {
        mContext = context;
        images = getImagesArray();
    }
    
    public int getCount() {
        return images.length;
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    private String[] getImagesArray() {
    	File folder = SDHelper.getAlbumStorageDir();
 	    if (!folder.exists()) {
 		    Log.d(TAG, "Нет сохраненных изображений");
 		    //Toast.makeText(this, mContext.getString(R.string.noImages), Toast.LENGTH_SHORT).show();
 	    	return  new String[0];
 	    }
 	    
 	    String[] allFiles = folder.list();
 	    
 	    String images[] = new String[allFiles.length];
    	for (int i = 0; i < allFiles.length; i++) {
			images[i] = folder.getAbsolutePath() + File.separator + allFiles[i];
		}
    	
    	return images;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView view = null;
        if (convertView != null) {
            view = (ImageView) convertView;
        } else {
            view = (ImageView)View.inflate(mContext, R.layout.image_item, null);
            view.setLayoutParams(new EcoGallery.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
            view.setScaleType(ImageView.ScaleType.FIT_CENTER);
        }

        Bitmap bm = BitmapFactory.decodeFile(images[position]);
        view.setImageBitmap(bm);
       /* view.setBackgroundResource(mGalleryItemBackground);*/

        return view;
    }
}
