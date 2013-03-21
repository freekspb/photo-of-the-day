package hram.android.PhotoOfTheDay.gallery;

import hram.android.PhotoOfTheDay.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import com.bugsense.trace.BugSenseHandler;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
//import android.util.Log;
import android.widget.ImageView;

public class ImageLoader 
{   
	MemoryCache memoryCache = new MemoryCache();
    FileCache fileCache;
    private Map<ImageView, String> imageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    ExecutorService executorService;
    private Context context;
    
	public ImageLoader(Context context)
    {
    	this.context = context;
        fileCache=new FileCache(context);
        executorService = Executors.newFixedThreadPool(1);
    }
    
    final int stub_id = R.drawable.ic_gallery_picture;
    public void DisplayImage(int id, ImageView imageView)
    {
    	String url = "" + id;
		imageViews.put(imageView, url);
        Bitmap bitmap = memoryCache.get(url);
        if(bitmap != null)
        {
        	//Log.d("ImageLoader", "Картинка в MemoryCache");
            imageView.setImageBitmap(bitmap);
        }
        else
        {
        	queuePhoto(url, imageView);
        	imageView.setScaleType(ImageView.ScaleType.CENTER);
        	imageView.setImageResource(stub_id);
        }
    }
        
    private void queuePhoto(String url, ImageView imageView)
    {
    	if(fileCache != null)
    	{
    		PhotoToLoad p = new PhotoToLoad(url, imageView);
    		executorService.submit(new PhotosLoader(p));
    	}
    }
    
    private Bitmap getBitmap(String id) 
    {
    	final String url = "CachFile_" + id;
        File f = fileCache.getFile(url);
        
        //from SD cache
        Bitmap b = decodeFile(f);
        if(b != null)
        {
        	//Log.d("ImageLoader", "Картинка в FileCache");
            return b;
        }
        
        //from MediaStore
        try 
        {
        	//Log.d("ImageLoader", "Картинки нет, читаем из MediaStore");
        	
        	// ������ ������
    		//System.gc();
        	
        	Bitmap bm = MediaStore.Images.Thumbnails.getThumbnail(context.getContentResolver(), Integer.parseInt(id), MediaStore.Images.Thumbnails.MINI_KIND, null);
        	if(bm != null)
        	{
        		//Log.d("ImageLoader", "Сохранение кеша на SD: " + f.getAbsolutePath());
        		OutputStream fos = new FileOutputStream(f);
        		try
        		{
        			bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
        		}
        		finally
        		{
        			fos.close();
        		}
        	}
        	return bm;
        } catch (Exception ex){
        	//Log.e("ImageLoader", "Ошибка MediaStore: " + ex.getMessage());
        	//BugSenseHandler.log("ImageLoader", ex);
        	return null;
        }
    }

    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f)
    {
    	FileInputStream fis = null;
        try 
        {
        	fis = new FileInputStream(f);
        	return BitmapFactory.decodeStream(fis);
        	/*
            //decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);
            
            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE=70;
            int width_tmp=o.outWidth, height_tmp=o.outHeight;
            int scale=1;
            while(true){
                if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                    break;
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }
            
            //decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
            */
        } catch (FileNotFoundException e) {
        	//Log.e("ImageLoader", "Ошибка чтения из файла: " + e.getMessage());
        }
        finally{
        	try {
				fis.close();
			} catch (Exception e) {
			}
        }
        return null;
    }
    
  //Task for the queue
    private class PhotoToLoad
    {
        public String url;
        public ImageView imageView;
        public PhotoToLoad(String u, ImageView i){
            url=u; 
            imageView=i;
        }
    }
    
    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;
        PhotosLoader(PhotoToLoad photoToLoad){
            this.photoToLoad=photoToLoad;
        }
        
        public void run() {
            if(imageViewReused(photoToLoad))
                return;
            Bitmap bmp = getBitmap(photoToLoad.url);
            memoryCache.put(photoToLoad.url, bmp);
            if(imageViewReused(photoToLoad))
                return;
            BitmapDisplayer bd=new BitmapDisplayer(bmp, photoToLoad);
            Activity a=(Activity)photoToLoad.imageView.getContext();
            a.runOnUiThread(bd);
        }
    }
    
    boolean imageViewReused(PhotoToLoad photoToLoad){
        String tag=imageViews.get(photoToLoad.imageView);
        if(tag==null || !tag.equals(photoToLoad.url))
            return true;
        return false;
    }
    
    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;
        public BitmapDisplayer(Bitmap b, PhotoToLoad p){bitmap=b;photoToLoad=p;}
        public void run()
        {
            if(imageViewReused(photoToLoad))
                return;
            if(bitmap!=null)
            {
            	photoToLoad.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                photoToLoad.imageView.setImageBitmap(bitmap);
            }
            else
            {
            	photoToLoad.imageView.setScaleType(ImageView.ScaleType.CENTER);
                photoToLoad.imageView.setImageResource(stub_id);
            }
        }
    }

    public void clearCache() 
    {
    	//Log.d("ImageLoader", "Очистка кеша");
        memoryCache.clear();
        //fileCache.clear();
    }
}
