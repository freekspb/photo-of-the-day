package hram.android.PhotoOfTheDay.appwidget;

import hram.android.PhotoOfTheDay.R;
import hram.android.PhotoOfTheDay.Wallpaper;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

public class SDHelper {
	public static final String TAG = "SDHelper";
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
	
	public static File getAlbumStorageDir(String albumName) {
	    // Get the directory for the user's public pictures directory. 
	    File file = new File(Environment.getExternalStoragePublicDirectory(
	            Environment.DIRECTORY_PICTURES), albumName);
	    if (!file.mkdirs()) {
	        Log.e(TAG, "Directory not created");
	    }
	    return file;
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
				wp.GetBitmap().compress(Bitmap.CompressFormat.JPEG, 80, bytes);
				
				String filename = getFilename(wp.GetCurrentUrl());
				if (filename == null) {
					return wp.getString(R.string.errorSaveFileToSD);
				}
								
				//File file = new File(getAlbumStorageDir(FOLDER).getPath(), filename);
				File file = new File(getAlbumStorageDir(FOLDER) + File.separator + getFilenamePrefix(wp.getImageNamePrefix()) + filename);
				file.createNewFile();
				//write the bytes in file
				FileOutputStream fo = new FileOutputStream(file);
				fo.write(bytes.toByteArray());
				
				addImageGallery(wp, file );
				
			} catch (IOException e) {
				Log.e(TAG, "Ошибка сохранения файла: " + e.getLocalizedMessage());
				return wp.getString(R.string.errorSaveFileToSD);
			}
		}
		
		return wp.getString(R.string.saveFileToSD);
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
	    contect.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
	}
}
