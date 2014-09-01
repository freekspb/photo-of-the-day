package hram.android.PhotoOfTheDay;

import hram.android.PhotoOfTheDay.ZTouchMove.ZTouchMoveListener;
import hram.android.PhotoOfTheDay.Exceptions.ConnectionException;
import hram.android.PhotoOfTheDay.Exceptions.IncorrectDataFormat;
import hram.android.PhotoOfTheDay.Parsers.BaseParser;
import hram.android.PhotoOfTheDay.Parsers.Bing;
import hram.android.PhotoOfTheDay.Parsers.DieselStation;
import hram.android.PhotoOfTheDay.Parsers.EarthShots;
import hram.android.PhotoOfTheDay.Parsers.Flickr;
import hram.android.PhotoOfTheDay.Parsers.Nasa;
import hram.android.PhotoOfTheDay.Parsers.NationalGeographic;
import hram.android.PhotoOfTheDay.Parsers.TestParser;
import hram.android.PhotoOfTheDay.Parsers.Wikipedia;
import hram.android.PhotoOfTheDay.Parsers.Yandex;
import hram.android.PhotoOfTheDay.appwidget.SDHelper;
import hram.android.PhotoOfTheDay.appwidget.WidgetBroadcastEnum;
import hram.android.PhotoOfTheDay.appwidget.WidgetBroadcastReceiver;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.bugsense.trace.BugSenseHandler;
import com.novoda.imageloader.core.util.DirectLoader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.service.wallpaper.WallpaperService;
//import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;

public class Wallpaper extends WallpaperService {
	public static final String TAG = Constants.TAG;
	private final Handler mHandler = new Handler();
	private List<MyEngine> engines = new ArrayList<MyEngine>();
	private Lock l = new ReentrantLock();
	private NetworkInfo mWifi;

	private int currDay = -1;
	private Bitmap bm;
	public SharedPreferences preferences;
	private String currentUrl;
	// private BaseParser parser;
	private int currentParser = -1;
	private int currentHeight = -1;
	private int currentWidth = -1;
	private boolean widthRescaling = false;

	@Override
	public void onCreate() {
		// Log.i(TAG, "Создание сервиса.");
		BugSenseHandler.initAndStartSession(this, Constants.BUG_SENSE_APIKEY);

		// настройки
		preferences = getSharedPreferences(Constants.SETTINGS_NAME, 0);

		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

		try {
			int parser = Integer.decode(preferences.getString(Constants.SOURCES_NAME, "-1"));
			// если нет сохраненного в настройках
			if (parser < 0) {
				Point displaySize = getDisplaySize();
				int displayWidth = displaySize.x;
				int displayHeight = displaySize.y;
				// если ширина дисплея больше высоты (для планшетов)
				if (displayWidth > displayHeight) {
					// NG - так как там почти все изображения альбомные
					parser = 8;
				} else {
					parser = 1;
				}
			}

			SetCurrentParser(parser, true);
		} catch (Exception e) {
			preferences.edit().putString(Constants.SOURCES_NAME, "1").commit();
			SetCurrentParser(1, true);
		}

		widthRescaling = preferences.getBoolean(Constants.WIDTH_SCALE, false);
		
		ReadFile(true, false);
	}

	@Override
	public void onDestroy() {
		// Log.i(TAG, "Удаление сервиса.");
		// unregisterReceiver(widgetReceiver);
	}

	@Override
	public Engine onCreateEngine() {
		return new MyEngine(this);
	}

	/**
	 * Регистрирует рисовальщик, добавляет в список, после обновления фото
	 * рисовальщики оповещаются из этого списка
	 * 
	 * @param object
	 */
	public void RegEngine(MyEngine object) {
		engines.add(object);
	}

	/**
	 * Отменяет регистрацию рисовальщика
	 * 
	 * @param object
	 */
	public void UnregEngine(MyEngine object) {
		engines.remove(object);
	}

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")	
	private Point getDisplaySize()
	{
		Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		
        // API Level 13
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point size = new Point();
            display.getSize(size);
            return size;
        } else {
            // API Level <13
        	return new Point(display.getWidth(), display.getHeight());
        }
	}

    private float getWidthScale()
    {
    	if (widthRescaling)
    	{
    		return 1.2f;
    	}
    	return 1;
    	
    }
    
	/**
	 * Сохраняет указатель на картинку
	 * 
	 * @param value
	 */
    public void SetBitmap(Bitmap value) {
		// Log.d(TAG, "Сохранение указателя картинки");

//    	SDHelper.appendLog("SetBitmap started...");
    	
		if (value == null) {
			bm = null;
			currentHeight = -1;
			currentWidth = -1;
			return;
		}
		int bmWidth = value.getWidth(); // исходная ширина
		int bmHeight = value.getHeight(); // исходная высота

		Point displaySize = getDisplaySize();
		int displayWidth = displaySize.x;
		int displayHeight = displaySize.y;
		
//		SDHelper.appendLog("bmHeight=" + Integer.toString(bmHeight) + "; displayHeight=" + Integer.toString(displayHeight) 
//				+ "; bmWidth=" + Integer.toString(bmWidth) + "; displayWidth=" + Integer.toString(displayWidth));
		
		if (bmWidth != displayWidth || bmHeight != displayHeight) {
			// Log.d(TAG,String.format("Изменились размеры, изменяем размер: %d->%d, %d->%d",
			// currentHeight, mHeight, currentWidth, mWidth));
			float rescaling = (float) displayHeight / bmHeight;

			int newBmHeight = displayHeight;
			int newBmWidth = (int) (bmWidth * rescaling);

			// если ширина экрана больше картинки после мастабировния
			if (newBmWidth < displayWidth) {
				// то масштабируем по ширине
				// коэффициент, чтобы обои скролились
				newBmWidth = (int)(displayWidth * getWidthScale());
				rescaling = (float) newBmWidth / bmWidth;
				newBmHeight = (int) (bmHeight * rescaling);
			}

//			SDHelper.appendLog("rescaling=" + Float.toString(rescaling));
					
			// если рассчиталось что-то неверно, то берем начальный размер
			if (newBmHeight <= 0 || newBmHeight <= 0)
			{
				bm.recycle();
				bm = value;
				return;
			}
			
			if (bm != null && !bm.isRecycled())
			{
				bm.recycle();					
			}
			try
			{
				bm = Bitmap.createScaledBitmap(value, newBmWidth, newBmHeight, true);
				currentWidth = newBmWidth;
				currentHeight = newBmHeight;
				
//				SDHelper.appendLog("currentHeight=" + Integer.toString(currentHeight) + "; currentWidth=" + Integer.toString(currentWidth)); 
				
			}
			catch(OutOfMemoryError e)
			{
				bm = value;
				currentWidth = bm.getWidth();
				currentHeight = bm.getHeight();
			}
			
//			SDHelper.appendLog("SetBitmap finished1...");
			
			return;
		}

		if (bm != null && !bm.isRecycled())
		{
			bm.recycle();					
		}
		bm = value;
		currentWidth = bm.getWidth();
		currentHeight = bm.getHeight();
		
//		SDHelper.appendLog("currentHeight=" + Integer.toString(currentHeight) + "; currentWidth=" + Integer.toString(currentWidth)); 
//		SDHelper.appendLog("SetBitmap finished2...");		
	}

	/**
	 * Возвращает указатель на картинку
	 * 
	 * @return
	 */
	public Bitmap GetBitmap() {
		return bm;
	}

	/**
	 * Сохраняет текущий день
	 * 
	 * @param value
	 */
	public void SetCurrentDay(int value) {
		// для отладки обновления
		// value -= 1;

		// Log.d(TAG, String.format("Текущее число: %d", value));
		currDay = value;
	}

	/**
	 * Возвращает текущий день
	 * 
	 * @return
	 */
	public int GetCurrentDay() {
		return currDay;
	}

	/**
	 * Сохраняет URL текущей картинки
	 * 
	 * @param value
	 */
	public void SetCurrentUrl(String value) {
		// Log.d(TAG, String.format("Текущий URL: %s", value));
		currentUrl = value;
	}

	/**
	 * Возвращает URL текущей картинки
	 * 
	 * @return
	 */
	public String GetCurrentUrl() {
		return currentUrl;
	}

	/**
	 * Возвращает статус услуги передачи данных
	 * 
	 * @return
	 */
	public boolean IsOnline() {
		// Log.d(TAG, "Вызов isOnline()");

		try {
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			return cm.getActiveNetworkInfo().isConnectedOrConnecting();
		} catch (Exception e) {
			// Log.d(TAG, "Ошибка проверки online");
		}

		return false;
	}

	public boolean IsWiFiEnabled() {
		boolean wifiOnly = preferences.getBoolean(Constants.WIFI_ONLY, false);

		// Log.d(TAG, String.format("Только через WiFi %s", wifiOnly ? "Вкл" :
		// "Откл"));

		return wifiOnly ? mWifi.isConnected() : true;
	}

	/**
	 * Возвращает урл картинки дня
	 * 
	 * @return
	 * @throws IOException
	 */
	public String GetUrl() throws IOException, IncorrectDataFormat {
		// Log.d(TAG, "Получение URL картинки");

		return createCurrentParser().GetUrl();
	}

	public String getImageNamePrefix() {
		return createCurrentParser().getImageNamePrefix();
	}

	/**
	 * Создает экземпляр выбранного парсера
	 * 
	 * @param value
	 *            номер парсера
	 * @return
	 */
	public boolean SetCurrentParser(int value, Boolean resetBitmap) {
		l.lock();
		try {
			if (currentParser == value) {
				return false;
			}
			currentParser = value;

		} finally {
			l.unlock();
		}
		if (resetBitmap)
		{
			ResetBitmap();
		}
		return true;
	}

	public int getCurrentParser() {
		return currentParser;
	}

	public BaseParser createCurrentParser() {
		switch (getCurrentParser()) {
		case 1:
			return new Yandex(this, preferences);
		case 2:
			return new Flickr(this, preferences);
		case 3:
			return new NationalGeographic();
		case 4:
			return new Nasa();
		case 10:
			return new Wikipedia();
		case 6:
			return new TestParser(this, preferences);
		case 7:
			return new EarthShots();
		case 8:
			return new Bing();
		case 9:
			return new DieselStation();
		default:
			// Log.i(TAG, "Создание парсера по умолчанию");
			return new Yandex(this, preferences);
		}
	}

	/**
	 * Сброс настроек для того чтоб отрисовывалась картинка загрузки
	 */
	public void ResetBitmap() {
		//SetBitmap(null);
		currDay = -1;
		currentUrl = null;
		currentWidth = -1;
		currentHeight = -1;
	}

	/**
	 * Чтение сохраненной картинки из файла
	 */
	public void ReadFile(boolean updateUrl, boolean updateFromSD) {
		// Log.d(TAG, "Чтение картинки из файла");

		FileInputStream stream = null;
		try {
			long lastUpdate = preferences.getLong(Constants.LAST_UPDATE, 0);
			if (updateFromSD == false && lastUpdate == 0) {
				return;
			}

			if (updateUrl)
			{
				SetCurrentUrl(preferences.getString(Constants.LAST_URL, ""));
			}

			if (updateFromSD == false)
			{
				Calendar c = Calendar.getInstance();
				c.setTimeInMillis(lastUpdate);
				SetCurrentDay(c.get(Calendar.DATE));
			}
			if (updateFromSD == false && GetCurrentUrl().length() <= 0) {
				return;
			}

			stream = openFileInput(Constants.FILE_NAME);
			Bitmap readBm = BitmapFactory.decodeStream(stream);
			SetBitmap(readBm);
			if (GetBitmap() != readBm)
			{
				readBm.recycle();
			}

		} catch (Exception e) {
			// Log.d(TAG, "Не известная ошибка");
			ResetBitmap();
		} finally {
			if (stream != null)
				try {
					stream.close();
				} catch (IOException e) {
				}
		}
	}

	/**
	 * Сохранение картинки в файл
	 * 
	 * @param bm
	 * @param url
	 */
	public void SaveFile(Bitmap bm, String url) {
		// Log.d(TAG, "Сохранение картинки в файл");
		try {
			FileOutputStream fos = openFileOutput(Constants.FILE_NAME,
					Context.MODE_PRIVATE);
			try {
				// PNG which is lossless, will ignore the quality setting
				bm.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			} finally {
				fos.close();	
			}

			long now = System.currentTimeMillis();

			// сохранение времени последнего обновления
			SharedPreferences.Editor editor = preferences.edit();
			editor.putLong(Constants.LAST_UPDATE, now);
			editor.putString(Constants.LAST_URL, url);
			editor.commit();

			SetCurrentDay(Calendar.getInstance().get(Calendar.DATE));
			SetCurrentUrl(url);

		} catch (IOException e) {
			// Log.d(TAG, "Ошибка сохранения картинки");
		}
	}

	/**
	 * Создание и запуск потока обновления
	 */
	public void StartUpdate() {
		new Thread(new Runnable() {
			public void run() {
				update();
			}
		}).start();
	}

	/**
	 * Проверка условий ибновления и в случае выполнения загрузка фото,
	 * установка его текущим и сохранение в файл
	 */
	public void update() {
		// Log.d(TAG, "Вызов MyEngine.update()");

		try {
			if (IsOnline() == false) {
				throw new ConnectionException("Нет интернет соединения.");
			}

			String url = GetUrl();
			if (url == null) {
				throw new ConnectionException("Ошибка получения URL картинки");
			}

			if (url.equals(GetCurrentUrl())) {
				// Log.d(TAG, "URL совпадает, еще не обновили");
				return;
			}

			// Log.d(TAG, "Загрузка картинки по адресу: " + url);
			// Bitmap bm = ImageDownloader.loadImageFromUrl(url);
			Bitmap bm = new DirectLoader().download(url);
			if (bm == null) {
				throw new ConnectionException("Ошибка загрузки киртинки");
			}

			// Log.d(TAG, "Картинка успешно загружена");
			currentHeight = -1;
			currentWidth = -1;
			SaveFile(bm, url);
			SetBitmap(bm);
			// если текущее изображение не является инстансом локального bm,
			// т.к. на SetBitmap может создаться изображение с другими размерами
			if (GetBitmap() != bm)
			{
				// чистим локальный bm
				bm.recycle();
			}

			for (MyEngine info : engines) {
				// Log.d(TAG, "Вызов drawFrame()");
				info.drawFrame();
			}
		} catch (IOException e) {
			// Log.w(TAG,
			// String.format("Ошибка получения URL: %s. Запуск проверяльщика",
			// e.getLocalizedMessage()));
			CheckOnline();
		} catch (ConnectionException e) {
			// Log.w(TAG, String.format("%s. Запуск проверяльщика",
			// e.getMessage()));
			CheckOnline();
		} catch (IncorrectDataFormat e) {
			// try {
			// BugSenseHandler.sendExceptionMessage("IncorrectDataFormat", "" +
			// getCurrentParser(), e);
			// }catch (Exception e2) {}
		} catch (OutOfMemoryError e) {
			System.gc();
			CheckOnline();
		} catch (Exception e) {
			// try {
			// BugSenseHandler.sendExceptionMessage("Wallpaper.update", "" +
			// getCurrentParser(), e);
			// }catch (Exception e2) {}
		}
	}

	/**
	 * Создание и запуск таймера проверки наличия услуги передачи данных
	 */
	private void CheckOnline() {
		// Log.d(TAG, "Создание таймера");

		try {
			new Timer().scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					// Log.d(TAG, "Запуск проверки соединения");
					if (IsOnline() == false) {
						// Log.d(TAG, "Передача данных отключена");
						return;
					}

					// Log.d(TAG,
					// "Передача данных включена. Остановка таймера проверки соединения");
					if (cancel()) {
						// Log.d(TAG, "Таймер успешно остановлен");
					}

					// Log.d(TAG, "Запуск обновления");
					StartUpdate();
				}

			}, 10000, 10000);
		} catch (Exception e) {
			// Log.e(TAG, "Неизвестная ошибка: " + e.getLocalizedMessage());
		}

		// Log.d(TAG, "Таймер создан и запущен");
	}

	public class MyEngine extends Engine implements
			SharedPreferences.OnSharedPreferenceChangeListener, ZTouchMoveListener {
		private final Paint mPaint = new Paint();
		private int mPixels = 0;
		private float mXStep = 0;
		// для программного скролинга
		private float mOffset = 0;
		private Boolean mProgramScroling = false;
		private Boolean mDisabledScroling = false;
		private Timer timer = new Timer();
		private int mHeight = -1;
		private int mWidth = -1;
		private Wallpaper wp;
		// private Rect mRectFrame;
		private Bitmap download;
		private SharedPreferences preferences;
		final WidgetBroadcastReceiver widgetReceiver;
		ZTouchMove mTouchMove;

		private final Runnable drawRunner = new Runnable() {
			public void run() {
				drawFrame();
			}
		};

		private boolean mVisible;

		/**
		 * Конструктор рисовальщика
		 * 
		 * @param service
		 *            ссылка на сервис обоев
		 */
		MyEngine(Wallpaper service) {
			// Log.i(TAG, "Создание Engine");

			final Paint paint = mPaint;
			paint.setColor(0xffffffff);
			paint.setTextSize(30);
			paint.setAntiAlias(true);
			paint.setTextAlign(Align.CENTER);

			preferences = Wallpaper.this.getSharedPreferences(
					Constants.SETTINGS_NAME, 0);
			preferences.registerOnSharedPreferenceChangeListener(this);
			mProgramScroling = preferences.getBoolean("programScrolingPref", false);
			mDisabledScroling = preferences.getBoolean("disableScrolingPref", false);
			
			wp = service;
			widgetReceiver = new WidgetBroadcastReceiver(wp, this);
		}

		@Override
		public void onCreate(SurfaceHolder surfaceHolder) {
			super.onCreate(surfaceHolder);

			wp.RegEngine(this);
			// Log.d(TAG, "Вызов MyEngine.onCreate()");

			netUpdates();
			registerReceiver(widgetReceiver, new IntentFilter(
					WidgetBroadcastEnum.SAVE_ACTION));
			registerReceiver(widgetReceiver, new IntentFilter(
					WidgetBroadcastEnum.OPEN_GALLERY_ACTION));
			registerReceiver(widgetReceiver, new IntentFilter(
					WidgetBroadcastEnum.NEXT_PARSER_ACTION));
			registerReceiver(widgetReceiver, new IntentFilter(
					WidgetBroadcastEnum.AUTO_NEXT_PARSER_ACTION));
			registerReceiver(widgetReceiver, new IntentFilter(
					WidgetBroadcastEnum.SETTINGS_ACTION));
			registerReceiver(widgetReceiver, new IntentFilter(
					WidgetBroadcastEnum.CHANGE_SETTINGS_ACTION));
			
//			if (isPreview())
//			{
//				return;
//			}
			if (preferences.getBoolean("programScrolingPref", false) == false)
			{
				return;
			}
			// активация программного скролинга
			onProgramScrolingChanged();
		}

		@Override
		public void onDestroy() {
			// Log.i(TAG, "Удаление Engine");

			wp.UnregEngine(this);

			timer.cancel();
			mHandler.removeCallbacks(drawRunner);
			if (preferences != null) {
				preferences.unregisterOnSharedPreferenceChangeListener(this);
			}

			// https://www.bugsense.com/dashboard/project/ab3f3ed5#error/67667495
			try {
				unregisterReceiver(widgetReceiver);
			} catch (java.lang.IllegalArgumentException e) {
				// BugSenseHandler.sendExceptionMessage("error/67667495",
				// "После исправления", e);
			} catch (Exception e) {
				// BugSenseHandler.sendExceptionMessage("unregisterReceiver",
				// "После исправления", e);
			}
			super.onDestroy();
		}

		/**
		 * Берет следующую случайную картинку из галереи сохраненных и устанавливает на обои.
		 */
		private void changeImage()
		{
			String[] imagesColumns = new String[] { MediaStore.Images.Media._ID, MediaStore.Images.Media.DATA };
			Uri imagesUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
			String imagesQuery = MediaStore.Images.Media.DESCRIPTION + " like ? ";
			
			String path = "";
			Cursor cursor = getContentResolver().query(imagesUri, imagesColumns, imagesQuery, new String[] {SDHelper.MEDIA_TAG}, null);
			try {
				int count = cursor.getCount();
				if (count <= 0)
				{
					return;
				}
				
				Random r = new Random();
				int position = r.nextInt(count);
				
				cursor.moveToPosition(position);
				int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				path = cursor.getString(columnIndex);				
			} finally {
				cursor.close();
			}
			if (path == "")
			{
				return;
			}
			
	        File source = new File(path);
	        if (source.exists()) {
	        	FileInputStream fis = null;
	            FileOutputStream fos = null;
	        	FileChannel src = null;
	        	FileChannel dst = null;
				try {
					fis = new FileInputStream(source);
		            src = fis.getChannel();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				try {
					fos = openFileOutput(Constants.FILE_NAME, Context.MODE_PRIVATE);
		            dst = fos.getChannel();
				} catch (FileNotFoundException e1) {
					e1.printStackTrace();
				}
				if (src == null || dst == null)
				{
					return;
				}
	            try
	            {
	            	dst.transferFrom(src, 0, src.size());
	            } catch (IOException e) {
					e.printStackTrace();
				}
	            finally
	            {
	            	try {
			            src.close();										
					} catch (Exception e) {
					}
	            	try {
			            dst.close();
					} catch (Exception e) {
					}
	            	try {
			            fis.close();										
					} catch (Exception e) {
					}
	            	try {
			            fos.close();
					} catch (Exception e) {
					}
	            }
	            ReadFile(false, true);
				for (MyEngine info : engines) {
					// Log.d(TAG, "Вызов drawFrame()");
					info.drawFrame();
				}
	        }										
		}
		
		/**
		 * Таймер обновления фотографии. Проверяет смену дня. В случае если
		 * наступил след. день запускает обновление.
		 */
		private void netUpdates() {
			// Log.d(TAG, "Создание таймера обновлений");

			timer.scheduleAtFixedRate(new TimerTask() {
				@Override
				public void run() {
					try {
						boolean needAutoChangeSource = IsNeedAutoChangeSource();
						
						if (needAutoChangeSource && (IsOnline() == false)) {
							changeImage();
							return;
						}

						if (needAutoChangeSource && IsOnline() ) {
							wp.sendBroadcast(new Intent(WidgetBroadcastEnum.AUTO_NEXT_PARSER_ACTION));
							return;
						}
						
						// Log.d(TAG, "Сработал таймер обновления");
						if (IsNeedDownloadEveryUpdate()) {
							// Log.d(TAG, "Запуск периодического обновления");
							wp.StartUpdate();
							return;
						}

						// Log.d(TAG, "Проверка времени последнего обновления");
						if (wp.GetCurrentDay() != Calendar.getInstance().get(
								Calendar.DATE)) {
							// Log.d(TAG, "Запуск обновления");
							wp.StartUpdate();
						} else {
							// Log.d(TAG,
							// String.format("Обновление не нужно. Сейчас: %d, текущий: %d",
							// now, wp.GetCurrentDay()));
						}
					} catch (Exception e) {
						// Log.e(TAG, "Неизвестная ошибка: " +
						// e.getLocalizedMessage());
					}
				}

			}, 0, Constants.UPDATE_INTERVAL);

			// Log.d(TAG, "Таймер обновлений запущен");
		}

		/**
		 * Возвращает флаг того, что необходимо обновлять картинку при каждой
		 * проверке. Флаг используется для того чтобы картинки обновлялись
		 * ежечастно по тегу если источник обоев поддерживает теги или для
		 * превью.
		 * 
		 * @return true если необходимо обновлять при каждой проверке
		 */
		private boolean IsNeedDownloadEveryUpdate() {
			try {
				// Log.d(TAG, "если это превью то не обновляем по таймеру");
				if (isPreview()) {
					// Log.d(TAG, "это превью не обновляем по таймеру");
					return false;
				}

				// Log.d(TAG, "если парсер не поддерживает");
				if (createCurrentParser().IsTagSupported() == false) {
					// Log.d(TAG, "парсер не поддерживает");
					return false;
				}

				// Log.d(TAG, "если не включена работа по тегам");
				if (preferences.getBoolean("tagPhotoEnable", false) == false) {
					// Log.d(TAG, "не включена работа по тегам");
					return false;
				}

				// Log.d(TAG,
				// "если опция периодического обновления не включена");
				if (preferences.getBoolean("downloadEveryUpdate", false) == false) {
					// Log.d(TAG,
					// "опция периодического обновления не включена");
					return false;
				}

				// Log.d(TAG, "если тег не введен");
				String tag = preferences.getString("tagPhotoValue", "");
				if (tag.length() == 0) {
					// Log.d(TAG, "тег не введен");
					return false;
				}
			} catch (Exception e) {
				// Log.e(TAG, e.getMessage());
				return false;
			}

			// Log.d(TAG, "надо обновлять");
			return true;
		}

		/**
		 * Возвращает флаг того, что необходимо переключать источник обоев
		 * и обновлять картинку. Используется для ежечасной смены источников.
		 * 
		 * @return true если необходимо менять источник при каждой проверке
		 */
		private boolean IsNeedAutoChangeSource() {
			try {
				// Log.d(TAG, "если это превью то не обновляем по таймеру");
				if (isPreview()) {
					// Log.d(TAG, "это превью не обновляем по таймеру");
					return false;
				}

				if (preferences.getBoolean(Constants.AUTO_CHANGE_SOURCE, false) == true) {
					return true;
				}
			} catch (Exception e) {
				return false;
			}

			return false;
		}

		@Override
		public void onVisibilityChanged(boolean visible) {
			// Log.d(TAG, "Вызов MyEngine.onVisibilityChanged()");

			mVisible = visible;
			if (visible) {
				drawFrame();
			} else {
				mHandler.removeCallbacks(drawRunner);
			}
		}

		@Override
		public void onSurfaceChanged(SurfaceHolder holder, int format,
				int width, int height) {
			super.onSurfaceChanged(holder, format, width, height);

//			SDHelper.appendLog("onSurfaceChanged: width=" + Integer.toString(width) + "; height=" + Integer.toString(height) + "; mWidth=" + Integer.toString(mWidth) + "; mHeight=" + Integer.toString(mHeight));
			
			// Log.d(TAG, "Вызов MyEngine.onSurfaceChanged()");

			// в случае mHeight == -1 && mWidth == -1 - это первый запуск,
			// в этом случае ReadFile уже отработает, повоторно не нужно вызывать
			if (mHeight != -1 && mWidth != -1 && (mHeight != height || mWidth != width))
			{
				mHeight = height;
				mWidth = width;
				ReadFile(false, false);
			}
			else
			{
				mHeight = height;
				mWidth = width;
			}
			// изменяем ширину в скролинге 
			if (mTouchMove != null)
			{
				mTouchMove.initWidth(mWidth);
			}
//			SDHelper.appendLog("onSurfaceChanged: mTouchMove is null=" + Boolean.toString(mTouchMove == null));
			// рисуем
			drawFrame();
		}

		@Override
		public void onSurfaceDestroyed(SurfaceHolder holder) {
			super.onSurfaceDestroyed(holder);
			mVisible = false;
			mHandler.removeCallbacks(drawRunner);
		}

		@Override
		public void onOffsetsChanged(float xOffset, float yOffset, float xStep,
				float yStep, int xPixels, int yPixels) {
			// Log.d(TAG, "Вызов MyEngine.onOffsetsChanged()");
			// Log.d(TAG, String.format("xStep: %f, xPixels: %d", xStep,
			// xPixels));

//			SDHelper.appendLog("onOffsetsChanged: xStep=" + Float.toString(xStep) + "; mXStep=" + Float.toString(mXStep) 
//					+ "; xPixels=" + Integer.toString(xPixels) + "; mPixels=" + Integer.toString(mPixels)
//					+ "; xOffset=" + Float.toString(xOffset) + "; mOffset=" + Float.toString(mOffset)
//					+ "; mProgramScroling=" + Boolean.toString(mProgramScroling));
			
			mXStep = xStep;
			mPixels = xPixels;
			
			//if (mProgramScroling == true && mOffset != 0.5f)
			if (mProgramScroling == true)
			{
				return;
			}
			
			mOffset = xOffset;
			
			drawFrame();
		}
		
		public void onTouchOffsetChanged(float xOffset) {
			
//			SDHelper.appendLog("onTouchOffsetChanged: xOffset=" + Float.toString(xOffset) + "; mOffset=" + Float.toString(mOffset)
//					+ "; mProgramScroling=" + Boolean.toString(mProgramScroling));
			
			// если сколинг выключен
			// либо центральный экран - отработает onOffsetsChanged - для отработки возвращения на центральный экран по кнопке Home
			//if (mProgramScroling == false || mOffset == 0.5f)
			if (mProgramScroling == false)
			{
				return;
			}
			
			mOffset = xOffset;
			//Log.e(TAG, String.format("xOffset = %f", xOffset));
			drawFrame();
		}
		
		// для тестирования вывода ошибки о том что мало памяти
		/*
		 * void drawFrameOutOf() { // Log.d(TAG, "Процедура отрисовки");
		 * 
		 * final SurfaceHolder holder = getSurfaceHolder();
		 * 
		 * Canvas c = null; try {
		 * 
		 * c = holder.lockCanvas(); if (c != null) {
		 * c.drawText(getText(R.string.error).toString(), mWidth / 2, mHeight /
		 * 2 - 50, mPaint);
		 * c.drawText(getText(R.string.isOutOfMemory1).toString(), mWidth / 2,
		 * mHeight / 2, mPaint);
		 * c.drawText(getText(R.string.isOutOfMemory2).toString(), mWidth / 2,
		 * mHeight / 2 + 50, mPaint); } } finally { if (c != null)
		 * holder.unlockCanvasAndPost(c);
		 * 
		 * // Reschedule the next redraw mHandler.removeCallbacks(drawRunner);
		 * if (mVisible) { // mHandler.postDelayed(drawRunner, 1000); } } }
		 */

		@Override
		public void onTouchEvent(MotionEvent event) {
			
//			SDHelper.appendLog("onTouchEvent: mProgramScroling=" + Boolean.toString(mProgramScroling));

			if (mProgramScroling == false)
			{
				return;
			}
			
			mTouchMove.onTouchEvent(event);
			super.onTouchEvent(event);
		}

		/**
		 * Draw one frame of the animation. This method gets called repeatedly
		 * by posting a delayed Runnable. You can do any drawing you want in
		 * here. This example draws a wireframe cube.
		 */
		void drawFrame() {
			// Log.d(TAG, "Процедура отрисовки");

			final SurfaceHolder holder = getSurfaceHolder();

			Canvas c = null;
			try {
				c = holder.lockCanvas();
				Bitmap bm = wp.GetBitmap();
				if (c != null) {
					if (bm == null) {
						if (download == null) {
							download = BitmapFactory.decodeResource(
									getResources(), R.drawable.download);
						}
						// Log.d(TAG, "Картинки нет рисуем загрузку");

						int topOffset;
						int leftOffset;
						int newBmHeight;
						int newBmWidth;
						float rescaling = 1;
						
						// если высота экрана больше ширины
						if (mHeight > mWidth) {
							// отступ сбоку - 1/6 часть экрана по ширине
							leftOffset = (int) (mWidth / 6);
							// ширина изображения - 4/6 экрана по ширине
							newBmWidth = mWidth - 2 * leftOffset;
							// коэффициент масштабирования
							rescaling = (float) newBmWidth
									/ download.getWidth();
							// высота
							newBmHeight = (int) (download.getHeight() * rescaling);
							// отступ сверху
							topOffset = (int) (mHeight - newBmHeight) / 2;
						}
						// если ширина экрана больше высоты
						else {
							// отступ сверху - 1/6 часть экрана по высоте
							topOffset = (int) (mHeight / 6);
							// высоты изображения - 4/6 экрана по высоте
							newBmHeight = mHeight - 2 * topOffset;
							// коэффициент масштабирования
							rescaling = (float) newBmHeight
									/ download.getHeight();
							// ширина
							newBmWidth = (int) (download.getWidth() * rescaling);
							// левый отступ ижображения
							leftOffset = (int) (mWidth - newBmWidth) / 2;
						}
						// если картинка еще не отмасштабирована
						if (rescaling != 1)
						{
							// масштабируем картинку под нужное разрешение
							download = Bitmap.createScaledBitmap(download, newBmWidth, newBmHeight, true);
						}

						Paint paint = new Paint();
						paint.setColor(getResources().getColor(R.color.download_background));
						c.drawRect(new Rect(0, 0, mWidth, mHeight), paint);
						c.drawBitmap(download, leftOffset, topOffset, null);
						
						if (IsOnline()) {
							c.drawText(getText(R.string.download).toString(),
									mWidth / 2, 100, mPaint);
						} else {
							c.drawText(getText(R.string.error).toString(),
									mWidth / 2, 100, mPaint);
							c.drawText(getText(R.string.isOffline).toString(),
									mWidth / 2, 150, mPaint);
						}
						return;
					}

					// чистим, чтобы не виселов памяти
					if (download != null) {
						download.recycle();
						download = null;
					}

					float rescaling = 1;

//					SDHelper.appendLog("drawFrame started...");
//					SDHelper.appendLog("mHeight=" + Integer.toString(mHeight) + "; currentHeight=" + Float.toString(currentHeight) 
//							+ "; mWidth=" + Integer.toString(mWidth) + "; currentWidth=" + Integer.toString(currentWidth));
					
					if (mHeight != currentHeight || mWidth != currentWidth) {
						// Log.d(TAG,String.format("Изменились размеры, изменяем размер: %d->%d, %d->%d",
						// currentHeight, mHeight, currentWidth, mWidth));
						rescaling = (float) mHeight / bm.getHeight();
						int newBmWidth = (int) (bm.getWidth() * rescaling);

						// если ширина экрана больше картинки после
						// мастабировния
						if (newBmWidth < mWidth) {
							// то масштабируем по ширине
							rescaling = (float) mWidth / bm.getWidth();
						}
						// если ширина экрана больше картинки после мастабировния
						if (newBmWidth < mWidth) {
							// то масштабируем по ширине
							// коэффициент, чтобы обои скролились
							newBmWidth = (int)(mWidth * getWidthScale());
							rescaling = (float) newBmWidth / bm.getWidth();
						}
						
//						SDHelper.appendLog("bmHeight=" + Integer.toString(bm.getHeight()) + "; bmWidth=" + Integer.toString(bm.getWidth())
//								+ "; rescaling=" + Float.toString(rescaling));
												
					}

					// матрица масштабирования
					Matrix matrix = new Matrix();
					matrix.setScale(rescaling, rescaling);

//					SDHelper.appendLog("drawFrame calculate dX...");
//					SDHelper.appendLog("isPreview=" + Boolean.toString(isPreview()) + "; mProgramScroling=" + Boolean.toString(mProgramScroling) 
//							+ "; mDisabledScroling=" + Boolean.toString(mDisabledScroling)
//							+ "; mXStep=" + Float.toString(mXStep)  + "; mPixels=" + Float.toString(mPixels));
					
					float dX = 0;
					// смещение для устройств со скролингом
					if (isPreview() == false && mProgramScroling == false && mDisabledScroling == false && mXStep != 0 && mPixels != 0) {
						float step1 = mWidth * mXStep;
						float step2 = (bm.getWidth() - mWidth) * mXStep;
						dX = (float) mPixels * (step2 / step1);
					}
					
//					SDHelper.appendLog("dx1=" + Float.toString(dX));
					
					// если предварительный просмотр
					if (isPreview() || mDisabledScroling) {
						// всегда центруем
						dX = (mWidth - bm.getWidth()) / 2;
					}

//					SDHelper.appendLog("dx2=" + Float.toString(dX));
					
					// если не превью и программный скролинг включен
					if (isPreview() == false && mDisabledScroling == false && mProgramScroling) {
						dX = (float) (mWidth - bm.getWidth()) * mOffset;
					}
					
//					SDHelper.appendLog("dx3=" + Float.toString(dX));
					
					float dY = (mHeight - bm.getHeight()) / 2;
					//float dY = 0;
					//Log.d(TAG, String.format("dX = %f", dX));
					if (dX != 0 || dY != 0) {
						c.translate(dX, dY);
					}

					try {
						c.drawBitmap(bm, matrix, null);
//						SDHelper.appendLog("drawFrame draw...");
//						SDHelper.appendLog("drawFrame finished...");
					} catch (OutOfMemoryError e) {
						System.gc();
						c.drawText(getText(R.string.error).toString(), mWidth / 2, 100, mPaint);
						c.drawText(getText(R.string.isOutOfMemory1).toString(), mWidth / 2, 150, mPaint);
						c.drawText(getText(R.string.isOutOfMemory2).toString(), mWidth / 2, 200, mPaint);
						try {
							// логирование показало что картинки скачиваются
							// нормальные (корректный URL) и нормально
							// отображаются (проверено с пом. тестового парсера)
							String msg = String.format(Locale.US, "URL: %s, Width: %d, Height: %d, mWidth: %d, mHeight: %d, rescaling: %f", GetCurrentUrl(), bm.getWidth(), bm.getHeight(), mWidth, mHeight, (float) rescaling);
							BugSenseHandler.sendExceptionMessage("c.drawBitmap(bm, matrix, null)", msg, new hram.android.PhotoOfTheDay.Exceptions.OutOfMemoryError(e.getMessage()));
						} catch (Exception e2) {
						} finally {
							ResetBitmap();
						}
						return;
					}
				}
			} finally {
				if (c != null)
					holder.unlockCanvasAndPost(c);

				// Reschedule the next redraw
				mHandler.removeCallbacks(drawRunner);
				if (mVisible) {
					// mHandler.postDelayed(drawRunner, 1000);
				}
			}
		}

		private void internalPreferenceChanged(SharedPreferences prefs, String key, Boolean checkPreview)
		{
			if (checkPreview && isPreview())
			{
				return;
			}
			// Log.d(TAG, "Изменено " + arg1);
			String tag = prefs.getString("tagPhotoValue", "");
			if (key.equals("tagPhotoEnable")
					&& createCurrentParser().IsTagSupported()) {
				if (prefs.getBoolean(key, false) && tag.length() == 0) {
					return;
				}

				StartUpdate();
			}
			if (key.equals("tagPhotoValue")
					&& createCurrentParser().IsTagSupported()
					&& tag.length() > 0) {
				StartUpdate();
			} else if (key.equals(Constants.SOURCES_NAME)) {
				String value = prefs.getString(key, "0");

				if (SetCurrentParser(Integer.decode(value), true)) {
					StartUpdate();
				}
			} else if (key.equals(Constants.FAVORITE_SOURCES)) {
				widgetReceiver.refreshFavoriteParsers();
			} else if (key.equals("programScrolingPref")) {
				setProgramScroling(preferences.getBoolean(key, false));
			} else if (key.equals("disableScrolingPref")) {
				setDisabledScroling(preferences.getBoolean(key, false));
			} else if (key.equals("scrollingEffect")) {
				String value = prefs.getString(key, "0");
				setScrolingEffect(Integer.decode(value));
			} else if (key.equals("numVirtualScreens")) {
				String value = preferences.getString("numVirtualScreens", "7");
				int intVal = 7;
				try {
					intVal = Integer.decode(value);
				} catch (Exception e) {
				}
				setNumVirtualScreens(intVal);
			} else if (key.equals(Constants.WIDTH_SCALE)) {
				widthRescaling = preferences.getBoolean(Constants.WIDTH_SCALE, false);
				ReadFile(false, false);
			}						
		}
		
		public void onPreferenceChanged(String key) {
			// Log.d(TAG, "Изменено " + key);
			internalPreferenceChanged(preferences, key, false);
		}

		public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
			// тут напутано prefs и preferences
			internalPreferenceChanged(preferences, key, false);
		}
		
		private void setProgramScroling(Boolean value)
		{
			if (mProgramScroling == value)
			{
				return;
			}
			mProgramScroling = value;
			onProgramScrolingChanged();
		}
		
		private void onProgramScrolingChanged() {
			if (mProgramScroling == true)
			{
				setTouchEventsEnabled(true);
				String value = preferences.getString("numVirtualScreens", "7");
				int intVal = 7;
				try {
					intVal = Integer.decode(value);
				} catch (Exception e) {
				}
				mTouchMove = new ZTouchMove();
				mTouchMove.init(wp, intVal, mWidth);
				mTouchMove.addMovingListener(this);
			}
			else
			{
				setTouchEventsEnabled(false);
				if (mTouchMove == null)
				{
					return;
				}
				mTouchMove.removeMovingListener(this);
				mTouchMove.recycle();
				mTouchMove = null;
			}
		}

		private void setDisabledScroling(Boolean value)
		{
			if (mDisabledScroling == value)
			{
				return;
			}
			mDisabledScroling = value;
			drawFrame();
		}

		private void setScrolingEffect(int value)
		{
			if (mTouchMove == null)
			{
				return;
			}
			if (value == 1)
				mTouchMove.setSpringMode(true);
			else
				mTouchMove.setSpringMode(false);
		}

		private void setNumVirtualScreens(int value)
		{
			if (mTouchMove == null)
			{
				return;
			}
			mTouchMove.setNumVirtualScreens(value);
		}
		
		private void StartUpdate() {
			if (wp.IsWiFiEnabled() == false) {
				return;
			}

			if (wp.IsOnline() == false) {
				if (preferences.getBoolean(Constants.LOAD_FROM_SD, true) == true)
				{
					changeImage();
				}
				return;
			}
			
//			Toast.makeText(wp, getString(R.string.updateStarted),
//					Toast.LENGTH_SHORT).show();

			wp.ResetBitmap();

			// сброс времени последнего обновления
			SharedPreferences.Editor editor = preferences.edit();
			editor.putLong(Constants.LAST_UPDATE, 0);
			editor.putString(Constants.LAST_URL, "");
			editor.commit();

			wp.StartUpdate();
		}
	}
}
