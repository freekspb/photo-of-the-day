package hram.android.PhotoOfTheDay.Parsers;

import hram.android.PhotoOfTheDay.Wallpaper;

import java.io.IOException;

import android.content.SharedPreferences;

/**
 * Тестовый парсер для проверки на upl из-за которых были ошибки
 * @author hram
 *
 */
public class TestParser extends BaseParser 
{
	private static String[] urls = new String[]{
		"http://images.nationalgeographic.com/wpf/media-live/photos/000/606/cache/art-grand-palais_60632_990x742.jpg", // Error #70600636
		"http://images.nationalgeographic.com/wpf/media-live/photos/000/606/cache/umbrellas-florence-italy_60643_990x742.jpg",
		"http://farm8.staticflickr.com/7116/8154477603_f8fc9a7eb7_z.jpg",
		"http://images.nationalgeographic.com/wpf/media-live/photos/000/606/cache/swimmers-lake-winnipeg_60641_990x742.jpg",
		"http://img-fotki.yandex.ru/get/6617/120219009.54/0_8b9cc_b79e95f_XL",
		"http://img-fotki.yandex.ru/get/6518/91721766.16/0_7e712_df2e30ac_XL",
		"http://img-fotki.yandex.ru/get/6418/91721766.16/0_7e710_5ebbf4df_XL"
	};
	
	public TestParser(Wallpaper wp, SharedPreferences preferences)
	{
		//Log.i(TAG, "Создание парсера Flickr");
		this.wp = wp;
		this.preferences = preferences;
	}
	
	@Override
	public String GetUrl() throws IOException 
	{
		try
		{
			String tag = preferences.getString("tagPhotoValue", "0");
			return urls[Integer.parseInt(tag)];
		}
		catch(Exception e)
		{
			return urls[0];
		}
	}

	@Override
	public boolean IsTagSupported() {
		return true;
	}

	@Override
	public String getImageNamePrefix() {
		return "Test";
	}
}
