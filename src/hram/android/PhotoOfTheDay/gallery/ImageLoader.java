package hram.android.PhotoOfTheDay.gallery;

import hram.android.PhotoOfTheDay.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Stack;
import java.util.WeakHashMap;

//import com.bugsense.trace.BugSenseHandler;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.MediaStore;
import android.widget.ImageView;

public class ImageLoader 
{   
	//private static final String TAG = Constants.TAG_CACHE;
	final Context mainContext; 
    private MemoryCache memoryCache;
    private FileCache fileCache;
    private Map<ImageView, Integer> imageViews=Collections.synchronizedMap(new WeakHashMap<ImageView, Integer>());
    
    public ImageLoader(Context context)
    {
    	mainContext = context;
    	memoryCache = new MemoryCache();
    	
        //Make the background thead low priority. This way it will not affect the UI performance
        photoLoaderThread.setPriority(Thread.NORM_PRIORITY-1);
        
        fileCache = new FileCache(context);
    }
    
    final int stub_id = R.drawable.ic_gallery_picture;
    public void DisplayImage(int id, Context activity, ImageView imageView)
    {
    	//Log.i(TAG, "������� �������� ��������");
    	
        imageViews.put(imageView, id);
        Bitmap bitmap = memoryCache.get(id);
        
        if(bitmap != null)
        {
        	//Log.w(TAG, "����������� �� ����");
        	imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageBitmap(bitmap);
        }
        else
        {
        	//Log.e(TAG, "������ ��� ����");
            queuePhoto(id, activity, imageView);
        }    
    }
        
    private void queuePhoto(int id, Context activity, ImageView imageView)
    {
        //This ImageView may be used for other images before. So there may be some old tasks in the queue. We need to discard them. 
        photosQueue.Clean(imageView);
        PhotoToLoad p = new PhotoToLoad(id, imageView);
        synchronized(photosQueue.photosToLoad)
        {
            photosQueue.Add(p);
        }
        
        //start thread if it's not started yet
        if(photoLoaderThread.getState()==Thread.State.NEW)
            photoLoaderThread.start();
    }
    
    private Bitmap getBitmap(int id) 
    {
    	final String url = "CachFile_" + String.valueOf(id);
        File f = fileCache.getFile(url);
        
        //from SD cache
        Bitmap b = decodeFile(f);
        if(b != null)
        {
        	//Log.d(TAG, "������ ����: " + f.getName());
            return b;
        }
        
        //from MediaStore
        try 
        {
        	// ������ ������
    		//System.gc();
    		
        	//Log.d(TAG, "���������� ��������: " + url);
        	
        	Bitmap bm = MediaStore.Images.Thumbnails.getThumbnail(mainContext.getContentResolver(), id, MediaStore.Images.Thumbnails.MINI_KIND, null);
        	if(bm != null)
        	{
        		//Log.d(TAG, "���������� �������� � ����: " + f.getName());
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
        	//BugSenseHandler.log("ImageLoader", ex);
        	return null;
        }
    }

    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f)
    {
        try 
        {
        	return BitmapFactory.decodeStream(new FileInputStream(f));
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
        	//Log.e(TAG, "���� �� ������ " + f.getName());
        }
        return null;
    }
    
    //Task for the queue
    private class PhotoToLoad
    {
        //public String url;
    	public int id;
        public ImageView imageView;
        public PhotoToLoad(int u, ImageView i){
            id=u; 
            imageView=i;
        }
    }
    
    PhotosQueue photosQueue = new PhotosQueue();
    
    public void stopThread()
    {
        photoLoaderThread.interrupt();
    }
    
    //stores list of photos to download
    class PhotosQueue
    {
        private Stack<PhotoToLoad> photosToLoad = new Stack<PhotoToLoad>();
        
        public void Add(PhotoToLoad value)
        {
        	//Log.d(TAG, "���������� � ���� �� ����������. ������: " + photosToLoad.size());
        	
        	photosToLoad.push(value);
        	photosToLoad.notifyAll();
        }
        
        public PhotoToLoad Pop()
        {
        	//Log.d(TAG, "��������� � �������� �� ����� �� ����������. ������: " + photosToLoad.size());
        	
        	return photosToLoad.pop();
        }
        
        //removes all instances of this ImageView
        public void Clean(ImageView image)
        {
        	//Log.d(TAG, "������� ����� �� ����������. ������: " + photosToLoad.size());
        	
        	try
        	{
	            for(int j=0 ;j<photosToLoad.size();){
	                if(photosToLoad.get(j).imageView==image)
	                    photosToLoad.remove(j);
	                else
	                    ++j;
	            }
        	}catch (ArrayIndexOutOfBoundsException e) {
			}
        }
        
        public void Clean() {
			photosToLoad.clear();
		}
    }
    
    class PhotosLoader extends Thread 
    {
        public void run() 
        {
            try {
                while(true)
                {
                    //thread waits until there are any images to load in the queue
                    if(photosQueue.photosToLoad.size() == 0)
                    {
                        synchronized(photosQueue.photosToLoad)
                        {
                            photosQueue.photosToLoad.wait();
                        }
                    }
                    
                    if(photosQueue.photosToLoad.size() != 0)
                    {
                        PhotoToLoad photoToLoad;
                        synchronized(photosQueue.photosToLoad)
                        {
                            photoToLoad = photosQueue.Pop();
                        }
                        Bitmap bmp = getBitmap(photoToLoad.id);
                        
                        // ���������� �������� � ����
                        memoryCache.put(photoToLoad.id, bmp);
                        //String tag=imageViews.get(photoToLoad.imageView);
                        Integer tag=imageViews.get(photoToLoad.imageView);
                        if(tag!=null && tag.equals(photoToLoad.id))
                        {
                            BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
                            Activity a=(Activity)photoToLoad.imageView.getContext();
                            a.runOnUiThread(bd);
                        }
                    }
                    if(Thread.interrupted())
                        break;
                }
            } catch (InterruptedException e) {
                //allow thread to exit
            }
        }
    }
    
    // ����� �������� ����������
    PhotosLoader photoLoaderThread = new PhotosLoader();
    
    // Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;
        public BitmapDisplayer(Bitmap b, PhotoToLoad p){bitmap=b;photoToLoad=p;}
        public void run()
        {
            //String tag=imageViews.get(photoToLoad.imageView);
        	Integer tag=imageViews.get(photoToLoad.imageView);
            if(tag==null || !tag.equals(photoToLoad.id))
                return;
            if(bitmap != null)
            {
            	photoToLoad.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                photoToLoad.imageView.setImageBitmap(bitmap);
            }
        }
    }

    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }

    public void clearMemoryCache() {
        memoryCache.clear();
        stopThread();
        photosQueue.Clean();
        imageViews.clear();
    }
}
