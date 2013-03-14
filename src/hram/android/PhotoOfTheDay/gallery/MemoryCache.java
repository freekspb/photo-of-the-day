package hram.android.PhotoOfTheDay.gallery;

import java.lang.ref.SoftReference;
import android.graphics.Bitmap;
import android.util.SparseArray;

/**
 * 
 * @author hram
 *
 */
public class MemoryCache 
{
	//private static final String TAG = Constants.TAG_CACHE;
    private SparseArray<SoftReference<Bitmap>> cache = new SparseArray<SoftReference<Bitmap>>();
    
    public Bitmap get(int id)
    {
        
        //Log.w(TAG, String.format("������ �������� %s �� ���� %s", id, name));
        SoftReference<Bitmap> ref = cache.get(id);
        if(ref == null)
        {
            return null;
        }
        return ref.get();
    }
    
    /**
     * 
     * @param id ID 
     * @param bitmap 
     */
    public void put(int id, Bitmap bitmap)
    {
		//Log.d(TAG, String.format("���������� �������� %s � ��� %s.", id, name));
		cache.put(id, new SoftReference<Bitmap>(bitmap));
		//Log.d(TAG, "������ ����: " + cache.size());
    }

    /**
     * 
     */
    public void clear() 
    {
    	// чистка картинок
    	for(int i = 0; i < cache.size(); i++) {
    	   // get the object by the key.
    	   SoftReference<Bitmap> ref = cache.valueAt(i);
    	   if (ref == null)
    	   {
    		   continue;
    	   }
    	   Bitmap bm = ref.get();
    	   if (bm == null)
    	   {
    		   continue;
    	   }
    	   bm.recycle();
    	   bm = null;
    	}
    	
    	cache.clear();
    }
}