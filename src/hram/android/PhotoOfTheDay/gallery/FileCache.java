package hram.android.PhotoOfTheDay.gallery;

import hram.android.PhotoOfTheDay.Constants;

import java.io.File;

import android.content.Context;

public class FileCache 
{    
	//private static final String TAG = Constants.TAG_CACHE;
    private File cacheDir;
    
    public static boolean FileIsExists(Context context, String dir, String url)
    {
    	File checkDir;
    	if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
        {
    		checkDir = new File(android.os.Environment.getExternalStorageDirectory(), Constants.CACHE_DIR + "/" + dir);
        }
        else
        {
        	checkDir = new File(context.getCacheDir(), dir);
        }
        
        if(!checkDir.exists())
        {
        	return false;
        }
        
        String filename = String.valueOf(url.hashCode());
        File f = new File(checkDir, filename);
        return f.exists();
    }
    
    public FileCache(Context context)
    {
        //Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
        {
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), Constants.CACHE_DIR);
        }
        else
        {
            cacheDir = context.getCacheDir();
        }
        
        if(!cacheDir.exists())
        {
        	//Log.d(TAG, "�������� ���������� ��� ����");
            cacheDir.mkdirs();
        }
    }
    
    public FileCache(Context context, String dir)
    {
        //Find the dir to save cached images
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
        {
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), Constants.CACHE_DIR + "/" + dir);
        }
        else
        {
            cacheDir = new File(context.getCacheDir(), dir);
        }
        
        if(!cacheDir.exists())
        {
        	//Log.d(TAG, "�������� ���������� ��� ����");
            cacheDir.mkdirs();
        }
    }
    
    public File getFile(String url)
    {
        //I identify images by hashcode. Not a perfect solution, good for the demo.
        String filename=String.valueOf(url.hashCode());
        //String filename = url.substring(url.lastIndexOf("/") + 1);
        File f = new File(cacheDir, filename);
        return f;
    }
    
    public File getFile(String url, String ext)
    {
        //I identify images by hashcode. Not a perfect solution, good for the demo.
        String filename = String.valueOf(url.hashCode()) + "." + ext;
        File f = new File(cacheDir, filename);
        return f;
    }
    
    public File getExportImportFile()
    {
        File f = new File(cacheDir, "ExportedFavorites.json");
        return f;
    }
    
    public void clear()
    {
    	/*
        File[] files = cacheDir.listFiles();
        for(File f:files)
        {
        	if(f.isDirectory())
        	{
        		
        	}
            f.delete();
        }
        */
        deleteDir(cacheDir);
    }

    public File[] GetFiles()
    {
    	return cacheDir.listFiles();
    }
    
    private static boolean deleteDir(File dir) 
    {
        if (dir.isDirectory()) {
                String[] children = dir.list();
                for (int i = 0; i < children.length; i++) {
                        boolean success = deleteDir(new File(dir, children[i]));
                        if (!success) {
                                return false;
                        }
                }
        }
        return dir.delete();
    }
}