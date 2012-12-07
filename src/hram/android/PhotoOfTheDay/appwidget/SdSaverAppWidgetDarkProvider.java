package hram.android.PhotoOfTheDay.appwidget;

import hram.android.PhotoOfTheDay.R;
import android.content.Context;
import android.widget.RemoteViews;

public class SdSaverAppWidgetDarkProvider extends SdSaverAppWidgetProvider {
	@Override
    protected RemoteViews getRemoteViews(Context context)
    {
    	RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_dark);
        remoteViews.setImageViewResource(R.id.img_bluetooth, R.drawable.ic_widget_download_image_dark);
        remoteViews.setImageViewResource(R.id.img_wifi, R.drawable.ic_widget_open_gallery_dark);
        remoteViews.setImageViewResource(R.id.img_gps, R.drawable.ic_widget_next_parser_dark);
        remoteViews.setImageViewResource(R.id.img_sync, R.drawable.ic_widget_settings_dark);
        
        return remoteViews;
    }    
}
