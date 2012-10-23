package hram.android.PhotoOfTheDay;

import hram.android.PhotoOfTheDay.Exceptions.ConnectionException;
import hram.android.PhotoOfTheDay.Parsers.BaseParser;
import hram.android.PhotoOfTheDay.Parsers.Flickr;
import hram.android.PhotoOfTheDay.Parsers.Nasa;
import hram.android.PhotoOfTheDay.Parsers.NationalGeographic;
import hram.android.PhotoOfTheDay.Parsers.Wikipedia;
import hram.android.PhotoOfTheDay.Parsers.Yandex;
import hram.android.PhotoOfTheDay.appwidget.WidgetBroadcastEnum;
import hram.android.PhotoOfTheDay.appwidget.WidgetBroadcastReceiver;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.service.wallpaper.WallpaperService;
import android.util.DisplayMetrics;
//import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.WindowManager;
import android.widget.Toast;

public class Wallpaper extends WallpaperService  
{
	public static final String TAG = "Wallpaper";
	private final Handler mHandler = new Handler();
	private final ImageDownloader imageDownloader = new ImageDownloader();
	private List<MyEngine> engines = new ArrayList<MyEngine>();
	private Lock l = new ReentrantLock();
	private NetworkInfo mWifi;
	
	private int currDay = -1;
	private Bitmap bm;
	private SharedPreferences preferences;
	private String currentUrl;
	private BaseParser parser;
	private int currentParser = -1;
	private int currentHeight = -1;
    private int currentWidth = -1;
	
	@Override
	public void onCreate() 
	{
		//Log.i(TAG, "�������� �������.");
		
		// ���������
		preferences = getSharedPreferences(Constants.SETTINGS_NAME, 0);
		
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		
		SetCurrentParser(Integer.decode(preferences.getString(Constants.SOURCES_NAME, "1")));
		
		ReadFile();
	}
	
	@Override
	public void onDestroy()
	{
		//Log.i(TAG, "�������� �������.");
		//unregisterReceiver(widgetReceiver);
	}
	
	@Override
	public Engine onCreateEngine() 
	{
		return new MyEngine(this);
	}
	
	/**
	 * ������������ �����������, ��������� � ������, 
	 * ����� ���������� ���� ������������ ����������� �� ����� ������
	 * @param object
	 */
	public void RegEngine(MyEngine object)
	{
		engines.add(object);
	}
	
	/**
	 * �������� ����������� ������������
	 * @param object
	 */
	public void UnregEngine(MyEngine object)
	{
		engines.remove(object);
	}
	
	/**
	 * ��������� ��������� �� ��������
	 * @param value
	 */
	public void SetBitmap(Bitmap value)
	{
		//Log.d(TAG, "���������� ��������� ��������");
		bm = value;
	}
	
	/**
	 * ���������� ��������� �� ��������
	 * @return
	 */
	public Bitmap GetBitmap()
	{
		return bm;
	}
	
	/**
	 * ��������� ������� ����
	 * @param value
	 */
	public void SetCurrentDay(int value)
	{
		// ��� ������� ����������
		//value -= 1;
		
		//Log.d(TAG, String.format("������� �����: %d", value));
		currDay = value;
	}
	
	/**
	 * ���������� ������� ����
	 * @return
	 */
	public int GetCurrentDay()
	{
		return currDay;
	}
	
	/**
	 * ��������� URL ������� ��������
	 * @param value
	 */
	public void SetCurrentUrl(String value)
	{
		//Log.d(TAG, String.format("������� URL: %s", value));
		currentUrl = value;
	}
	
	/**
	 * ���������� URL ������� ��������
	 * @return
	 */
	public String GetCurrentUrl()
	{
		return currentUrl;
	}
	
	/**
	 * ���������� ������ ������ �������� ������
	 * @return
	 */
	public boolean IsOnline() 
	{
		//Log.d(TAG, "����� isOnline()");
		
		try
		{
			ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			return cm.getActiveNetworkInfo().isConnectedOrConnecting();
		}
	    catch(Exception e) {
	    	//Log.d(TAG, "������ �������� online");
	    }
		
		return false;
	}
	
	public boolean IsWiFiEnabled()
	{
		boolean wifiOnly = preferences.getBoolean(Constants.WIFI_ONLY, false);
		
		//Log.d(TAG, String.format("������ ����� WiFi %s", wifiOnly ? "���" : "����"));
		
		return wifiOnly ? mWifi.isConnected() : true;
	}
	
	/**
	 * ���������� ��� �������� ���
	 * @return
	 * @throws IOException
	 */
	public String GetUrl() throws IOException
    {
		//Log.d(TAG, "��������� URL ��������");
		
		return parser.GetUrl();
    }
	
	/**
	 * ������� ��������� ���������� �������
	 * @param value ����� �������
	 * @return
	 */
	public boolean SetCurrentParser(int value)
	{
		l.lock();
	    try 
	    {
	    	if(currentParser == value)
	 		{
	 			return false;
	 		}
	    	currentParser = value;
	    	
	    } finally {
	        l.unlock();
	    }
		
		switch (value) {
		case 1:
			parser = new Yandex(this, preferences);
			break;
		case 2:
			parser = new Flickr(this, preferences);
			break;
		case 3:
			parser = new NationalGeographic();
			break;
		case 4:
			parser = new Nasa();
			break;
		case 5:
			parser = new Wikipedia();
			break;
		default:
			//Log.i(TAG, "�������� ������� �� ���������");
			parser = new Yandex(this, preferences);
			break;
		}
		
		ResetBitmap();
		return true;
	}
	
	/**
	 * ����� �������� ��� ���� ���� �������������� �������� ��������
	 */
	public void ResetBitmap()
	{
		SetBitmap(null);
		currDay = -1;
		currentUrl = null;
	}
	
	/**
	 * ������ ����������� �������� �� �����
	 */
	public void ReadFile()
	{
		//Log.d(TAG, "������ �������� �� �����");
		
		FileInputStream stream = null;
		try 
		{
			long lastUpdate = preferences.getLong(Constants.LAST_UPDATE, 0);
			if(lastUpdate == 0)
			{
				return;
			}
			
			SetCurrentUrl(preferences.getString(Constants.LAST_URL, ""));
			SetCurrentDay(new Date(lastUpdate).getDate());
			
			if(GetCurrentUrl().length() > 0)
			{
				stream = openFileInput(Constants.FILE_NAME);
				bm = BitmapFactory.decodeStream(stream);
				//Log.d(TAG, "������� �������� �� �����");
				
				currentHeight = bm.getHeight();
				currentWidth = bm.getWidth();
				//Log.d(TAG, String.format("������: %d, ������: %d", currentWidth, currentHeight));
			}
				
		}
		catch (Exception e) {
			//Log.d(TAG, "�� ��������� ������");
			ResetBitmap();
		}
		finally {
            if (stream != null) try {
                stream.close();
            } catch (IOException e) {}
        }

	}
	
	/**
	 * ���������� �������� � ����
	 * @param bm
	 * @param url
	 */
	public void SaveFile(Bitmap bm, String url)
	{
		//Log.d(TAG, "���������� �������� � ����");
		try 
		{
			FileOutputStream fos = openFileOutput(Constants.FILE_NAME, Context.MODE_PRIVATE);
			bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
			fos.close();
			
			long now = System.currentTimeMillis(); 
			
			// ���������� ������� ���������� ����������
            SharedPreferences.Editor editor = preferences.edit();
            editor.putLong(Constants.LAST_UPDATE, now);
            editor.putString(Constants.LAST_URL, url);
            editor.commit();
            
            SetCurrentDay(new Date(now).getDate());
            SetCurrentUrl(url);

		} catch (IOException e) {
			//Log.d(TAG, "������ ���������� ��������");
		}
	}
	
	/**
	 * �������� � ������ ������ ����������
	 */
	public void StartUpdate()
    {
    	new Thread(new Runnable() {
		    public void run() {
		    	update();
		    }
		  }).start();
    }
	
	/**
	 * �������� ������� ���������� � � ������ ���������� �������� ����,
	 * ��������� ��� ������� � ���������� � ����
	 */
	public void update()
    {
    	//Log.d(TAG, "����� MyEngine.update()");
    	
    	try 
    	{
    		if(IsOnline() == false)
    		{
    			throw new ConnectionException("��� �������� ����������.");
    		}
    		
        	String url = GetUrl();
        	if(url == null)
        	{
        		throw new ConnectionException("������ ��������� URL ��������");
        	}
        	
        	if(url.equals(GetCurrentUrl()))
        	{
        		//Log.d(TAG, "URL ���������, ��� �� ��������");
        		return;
        	}
        	
        	//Log.d(TAG, "�������� �������� �� ������: " + url);
        	Bitmap bm = imageDownloader.downloadBitmap(url);
        	if(bm == null)
        	{
        		throw new ConnectionException("������ �������� ��������");
        	}
        	
    		//Log.d(TAG, "�������� ������� ���������");
    		currentHeight = -1;
    		currentWidth = -1;
    		SetBitmap(bm);
    		SaveFile(bm, url);
    		
    		for (MyEngine info : engines)
    		{
    			//Log.d(TAG, "����� drawFrame()");
    			info.drawFrame();
    		}
    	} 
	    catch (IOException e) {
	    	//Log.w(TAG, String.format("������ ��������� URL: %s. ������ �������������", e.getLocalizedMessage()));
	    	CheckOnline();
		}
    	catch(ConnectionException e){
    		//Log.w(TAG, String.format("%s. ������ �������������", e.getMessage()));
    		CheckOnline();
    	}
	    catch(Exception e) {
	    	//Log.e(TAG, "����������� ������ ����������: " + e.getLocalizedMessage());
	    }
    }

	/**
	 * �������� � ������ ������� �������� ������� ������ �������� ������
	 */
	private void CheckOnline()
    {
		//Log.d(TAG, "�������� �������");
		
		try
		{
			new Timer().scheduleAtFixedRate(new TimerTask() 
			{
				@Override
				public void run() 
				{
					//Log.d(TAG, "������ �������� ����������");
					if(IsOnline() == false)
					{
						//Log.d(TAG, "�������� ������ ���������");
						return;
					}
					
					//Log.d(TAG, "�������� ������ ��������. ��������� ������� �������� ����������");
					if(cancel())
					{
						//Log.d(TAG, "������ ������� ����������");
					}
					
					//Log.d(TAG, "������ ����������");
					StartUpdate();
				}
				
			}, 10000, 10000);
		}
		catch(Exception e) {
	    	//Log.e(TAG, "����������� ������: " + e.getLocalizedMessage());
	    }
    	
    	//Log.d(TAG, "������ ������ � �������");
    }
	
	public class MyEngine extends Engine implements SharedPreferences.OnSharedPreferenceChangeListener 
	{
		private final Paint mPaint = new Paint();
        private int mPixels;
        private float mXStep;
        private Timer timer = new Timer();
        private int mHeight = -1;
        private int mWidth = -1;
        private Wallpaper wp;
        //private Rect mRectFrame;
        private boolean mHorizontal;
        private Bitmap download;
        private SharedPreferences preferences;
        final WidgetBroadcastReceiver widgetReceiver;

        private final Runnable drawRunner = new Runnable() {
            public void run() {
                drawFrame();
            }
        };
        
        private boolean mVisible;

		/**
		 * ����������� ������������
		 * @param service ������ �� ������ �����
		 */
        MyEngine(Wallpaper service) 
        {
        	//Log.i(TAG, "�������� Engine");
        	
        	final Paint paint = mPaint;
            paint.setColor(0xffffffff);
            paint.setTextSize(30);
            paint.setAntiAlias(true);
            paint.setTextAlign(Align.CENTER);
            
            preferences = Wallpaper.this.getSharedPreferences(Constants.SETTINGS_NAME, 0);
            preferences.registerOnSharedPreferenceChangeListener(this);
            
        	wp = service;
        	download = BitmapFactory.decodeResource(getResources(),  R.drawable.download);
            widgetReceiver = new WidgetBroadcastReceiver(wp);
        }
        
        @Override
        public void onCreate(SurfaceHolder surfaceHolder) 
        {
            super.onCreate(surfaceHolder);

            wp.RegEngine(this);
            //Log.d(TAG, "����� MyEngine.onCreate()");
            
            netUpdates();
    		registerReceiver(widgetReceiver, new IntentFilter(WidgetBroadcastEnum.SAVE_ACTION));            
        }

        @Override
        public void onDestroy() 
        {
        	//Log.i(TAG, "�������� Engine");
        	
        	wp.UnregEngine(this);
        	
        	timer.cancel();
            mHandler.removeCallbacks(drawRunner);
        	if(preferences != null)
        	{
        		preferences.unregisterOnSharedPreferenceChangeListener(this);
        	}
        	unregisterReceiver(widgetReceiver);
            super.onDestroy();
        }
        
		/**
		 * ������ ���������� ����������. ��������� ����� ���. 
		 * � ������ ���� �������� ����. ���� ��������� ����������.
		 */
        private void netUpdates()
    	{
        	//Log.d(TAG, "�������� ������� ����������");
        	try
			{
				timer.scheduleAtFixedRate(new TimerTask() 
				{
					@Override
					public void run() 
					{
						//Log.d(TAG, "�������� ������ ����������");
						if(IsNeedDownloadEveryUpdate())
						{
							//Log.d(TAG, "������ �������������� ����������");
							wp.StartUpdate();
							return;
						}

						//Log.d(TAG, "�������� ������� ���������� ����������");
						int now = new Date(System.currentTimeMillis()).getDate();
						if(wp.GetCurrentDay() != now)
						{
							//Log.d(TAG, "������ ����������");
							wp.StartUpdate();
						}
						else
						{
							//Log.d(TAG, String.format("���������� �� �����. ������: %d, �������: %d", now, wp.GetCurrentDay()));
						}
					}
					
				}, 0, Constants.UPDATE_INTERVAL);
			}
			catch(Exception e) {
				//Log.e(TAG, "����������� ������: " + e.getLocalizedMessage());
			}
    		
    		//Log.d(TAG, "������ ���������� �������");
    	}
       
        
        private boolean IsNeedDownloadEveryUpdate()
        {
        	try
        	{
	        	//Log.d(TAG, "���� ��� ������ �� �� ��������� �� �������");
	        	if(isPreview())
	        	{
	        		//Log.d(TAG, "��� ������ �� ��������� �� �������");
	        		return false;
	        	}
	        	
	        	//Log.d(TAG, "���� ������ �� ������������"); 
	        	if(parser.IsTagSupported() == false)
	        	{
	        		//Log.d(TAG, "������ �� ������������");
	        		return false;
	        	}
	        	
	        	//Log.d(TAG, "���� �� �������� ������ �� �����");
	        	if(preferences.getBoolean("tagPhotoEnable", false) == false)
	    		{
	        		//Log.d(TAG, "�� �������� ������ �� �����");
	        		return false;
	    		}
	        	
	        	//Log.d(TAG, "���� ����� �������������� ���������� �� ��������"); 
	        	if(preferences.getBoolean("downloadEveryUpdate", false) == false)
	        	{
	        		//Log.d(TAG, "����� �������������� ���������� �� ��������");
	        		return false;
	        	}
	        	
	        	//Log.d(TAG, "���� ��� �� ������");
	        	String tag = preferences.getString("tagPhotoValue", "");
	        	if(tag.length() == 0)
	        	{
	        		//Log.d(TAG, "��� �� ������");
	        		return false;
	        	}
        	}
        	catch(Exception e)
        	{
        		//Log.e(TAG, e.getMessage());
        		return false;
        	}
        	
        	//Log.d(TAG, "���� ���������");
        	return true;
        }
        
        @Override
        public void onVisibilityChanged(boolean visible) 
        {
        	//Log.d(TAG, "����� MyEngine.onVisibilityChanged()");
        	
            mVisible = visible;
            if (visible) {
                drawFrame();
            } else {
                mHandler.removeCallbacks(drawRunner);
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height) 
        {
            super.onSurfaceChanged(holder, format, width, height);
            
            //Log.d(TAG, "����� MyEngine.onSurfaceChanged()");
            
            mHeight = height;
            mWidth = width;
            initFrameParams();
            drawFrame();
        }

        @Override
        public void onSurfaceCreated(SurfaceHolder holder) {
            super.onSurfaceCreated(holder);
        }

        @Override
        public void onSurfaceDestroyed(SurfaceHolder holder) {
            super.onSurfaceDestroyed(holder);
            mVisible = false;
            mHandler.removeCallbacks(drawRunner);
        }

        @Override
        public void onOffsetsChanged(float xOffset, float yOffset, float xStep, float yStep, int xPixels, int yPixels) 
        {
        	//Log.d(TAG, "����� MyEngine.onOffsetsChanged()");
        	//Log.d(TAG, String.format("xStep: %f, xPixels: %d", xStep, xPixels));
        	
        	mXStep = xStep;
            mPixels = xPixels;
            drawFrame();
        }

        /**
         * Draw one frame of the animation. This method gets called repeatedly
         * by posting a delayed Runnable. You can do any drawing you want in
         * here. This example draws a wireframe cube.
         */
        void drawFrame() 
        {
        	//Log.d(TAG, "��������� ���������");
        	
            final SurfaceHolder holder = getSurfaceHolder();

            Canvas c = null;
            try 
            {
                c = holder.lockCanvas();
                Bitmap bm = wp.GetBitmap();
                if (c != null) 
                {
                	if(bm == null)
                	{
                		//Log.d(TAG, "�������� ��� ������ ��������");
                		double rescaling = (double)mWidth / download.getWidth();
                		int width = (int)(download.getWidth() * rescaling);
                		int offset = (mHeight / 2) - (width / 2);
                		c.drawRect(new Rect(0, 0, mWidth, mHeight), new Paint());
                		c.drawBitmap(Bitmap.createScaledBitmap(download, (int)(download.getWidth() * rescaling), (int)(download.getHeight() * rescaling), true) , 0, offset, null);
                		if(IsOnline())
                		{
                			c.drawText(getText(R.string.download).toString(), mWidth / 2, 100, mPaint);
                		}
                		else
                		{
                			c.drawText(getText(R.string.error).toString(), mWidth / 2, 100, mPaint);
                			c.drawText(getText(R.string.isOffline).toString(), mWidth / 2, 150, mPaint);
                		}
                		return;
                	}
                	
                	if(mHeight != currentHeight || mWidth != currentWidth)
                	{
                		//Log.d(TAG, String.format("���������� �������, �������� ������: %d->%d, %d->%d", currentHeight, mHeight, currentWidth, mWidth));
                		double rescaling = (double)mHeight / bm.getHeight();
                		if(mHorizontal)
                		{
                			rescaling = (double)mWidth / bm.getWidth();
                			rescaling *=1.5;
                		}
                		

                		bm = Bitmap.createScaledBitmap(bm, (int)(bm.getWidth() * rescaling), (int)(bm.getHeight() * rescaling), true);
                		wp.SetBitmap(bm);
                		currentHeight = mHeight;
                		currentWidth = mWidth;
                	}
                	
                	
                	if(isPreview() == false)
                	{
	                	float step1 = mWidth * mXStep;
	                	float step2 = (bm.getWidth() - mWidth) * mXStep;
	                	float dX = (float)mPixels * (step2 / step1);
	                	c.translate(dX, 0f);
                	}
                	
                	if(mHorizontal)
                		c.drawBitmap(bm, 0, -currentHeight / 3, null);
                	else
                		c.drawBitmap(bm, 0, 0, null);
                }
            } finally {
                if (c != null) holder.unlockCanvasAndPost(c);
                
                // Reschedule the next redraw
                mHandler.removeCallbacks(drawRunner);
                if (mVisible) 
                {
                    //mHandler.postDelayed(drawRunner, 1000);
                }
            }
        }
        
        void initFrameParams()
        {
        	DisplayMetrics metrics = new DisplayMetrics();
        	Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
        	display.getMetrics(metrics);

        	//mRectFrame = new Rect(0, 0, metrics.widthPixels, metrics.heightPixels);


        	int rotation = display.getOrientation();
        	if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180)
        	{
        	    mHorizontal = false;
        	}
        	else
        	{
        	    mHorizontal = true;
        	}
        }

		public void onSharedPreferenceChanged(SharedPreferences prefs, String arg1) 
		{
			if(isPreview() == false)
			{
				return;
			}
			
			//Log.d(TAG, "�������� " +  arg1);
			String tag = prefs.getString("tagPhotoValue", "");
			if(arg1.equals("tagPhotoEnable") && parser.IsTagSupported())
			{
				if(prefs.getBoolean(arg1, false) && tag.length() == 0)
				{
					return;
				}
				
				StartUpdate();
			}
			if(arg1.equals("tagPhotoValue") && parser.IsTagSupported() && tag.length() > 0)
			{
		        StartUpdate();
			}
			else if(arg1.equals("sources"))
			{
				String value = prefs.getString(arg1, "0");
				
				if(SetCurrentParser(Integer.decode(value)))
				{
					StartUpdate();
				}
			}
		}
		
		private void StartUpdate()
		{
			if(wp.IsWiFiEnabled() == false)
			{
				return;
			}
			
			Toast.makeText(wp, getString(R.string.updateStarted), Toast.LENGTH_SHORT).show();
			
			wp.ResetBitmap();
			
			// ����� ������� ���������� ����������
	        SharedPreferences.Editor editor = preferences.edit();
	        editor.putLong(Constants.LAST_UPDATE, 0);
	        editor.putString(Constants.LAST_URL, "");
	        editor.commit();
	        
			wp.StartUpdate();
		}
	}
}
