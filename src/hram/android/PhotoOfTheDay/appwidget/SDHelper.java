package hram.android.PhotoOfTheDay.appwidget;

import hram.android.PhotoOfTheDay.R;
import hram.android.PhotoOfTheDay.Wallpaper;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class SDHelper {
	public static final String TAG = "SDHelper";
	public static final String MEDIA_TAG = "hram.android.PhotoOfTheDay";
	private static final String FOLDER = "PhotoOfTheDay";

	/* Checks if external storage is available for read and write */
	private static boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}

	/* Checks if external storage is available to at least read */
	/*
	private static boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}
	*/
	
	private static File getAlbumStorageDir(String albumName) {
	    // Get the directory for the user's public pictures directory. 
	    File file = new File(Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_PICTURES), albumName);
	    if (file.exists()) {
	    	return file;
	    }
	    if (!file.mkdirs()) {
	        Log.e(TAG, "Directory not created");
	    }
	    return file;
	}

	public static File getAlbumStorageDir() {
	    // Get the directory for the user's public pictures directory. 
	    File file = new File(Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_PICTURES), FOLDER);
	    return file;
	}
	
	public static String getAlbumStorageDirPath() {
		return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + FOLDER;
	}
	
	/* Определяет имя файла из текущего url */
	private static String getFilename(String url) {
		if (url == null) {
			Log.e(TAG, "Ошибка сохранения файла: CurrentUrl == null");
			return null;
		}
		String fileName = null;
		try {
			fileName = url.substring( url.lastIndexOf('/')+1, url.length() );
			if (!fileName.toLowerCase(Locale.US).endsWith(".jpg")) {
				fileName += ".jpg";
			}
		} catch (Exception e) {
			Log.e(TAG, "Ошибка определения имени файла из url");
			return null;
		}
		return fileName;
	}
	
	/**
	 * Возвращает префикс файла по префиксу парсера
	 * @param parserPrefix Префикс картинки из текущего парсера
	 * @return parserPrefix_yyyy_MM_dd_
	 */
	@SuppressLint("SimpleDateFormat")
	private static String getFilenamePrefix(String parserPrefix) {
		String prefix = "";
		if (parserPrefix == null) {
			Log.e(TAG, "Ошибка определения префикса файла: parserPrefix == null");
		}
		else {
			prefix = parserPrefix + "_";
		}
			
		
		Calendar c = Calendar.getInstance();

		SimpleDateFormat df = new SimpleDateFormat("yyyy_MM_dd_");
		String formattedDate = df.format(c.getTime());
		return prefix + formattedDate;
	}

	/**
	 * Сохраняет текущее изображение обоев на SD карту
	 * @param wp Сервис обоев
	 * @return Результат сохранения для отображения пользователю
	 */
	public static String saveImage(Wallpaper wp) {
		if (isExternalStorageWritable()) {
			try {
				if (wp.GetBitmap() == null) {
					return wp.getString(R.string.errorSaveFileToSD);
				}
				
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				try
				{
					wp.GetBitmap().compress(Bitmap.CompressFormat.JPEG, 100, bytes);
					
					String filename = getFilename(wp.GetCurrentUrl());
					if (filename == null) {
						return wp.getString(R.string.errorSaveFileToSD);
					}
	
					//File file = new File(getAlbumStorageDir(FOLDER).getPath(), filename);
					File file = new File(getAlbumStorageDir(FOLDER) + File.separator + getFilenamePrefix(wp.getImageNamePrefix()) + filename);
					if (file.exists()) {
						return wp.getString(R.string.saveFileToSD);
					}
					file.createNewFile();
					//write the bytes in file
					FileOutputStream fo = new FileOutputStream(file);
					try
					{
						fo.write(bytes.toByteArray());						
					}
					finally
					{
						fo.close();
					}
					addImageGallery(wp, file );
				}
				finally
				{
					bytes.close();
				}
				
				return wp.getString(R.string.saveFileToSD);
				
			} catch (IOException e) {
				Log.e(TAG, "Ошибка сохранения файла: " + e.getLocalizedMessage());
				return wp.getString(R.string.errorSaveFileToSD);
			}
		}
		return null;
	}
	
	/**
	 * Добавляет в галерею запись о добавленном файле(не надо сканировать всю память)
	 * @param contect
	 * @param file
	 */
	private static void addImageGallery(Context contect, File file ) {
	    ContentValues values = new ContentValues();
	    values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
	    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg"); // setar isso
	    values.put(MediaStore.Images.Media.DESCRIPTION, MEDIA_TAG);
	    contect.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
	}
	
	/*
	public static void appendLog(String text)
	{       
	   File logFile = new File("sdcard/photo-of-the-day-log.txt");
	   if (!logFile.exists())
	   {
	      try
	      {
	         logFile.createNewFile();
	      } 
	      catch (IOException e)
	      {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	      }
	   }
	   try
	   {
	      //BufferedWriter for performance, true to set append to file flag
	      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
	      buf.append(text);
	      buf.newLine();
	      buf.close();
	   }
	   catch (IOException e)
	   {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	   }
	}
	*/	
}
