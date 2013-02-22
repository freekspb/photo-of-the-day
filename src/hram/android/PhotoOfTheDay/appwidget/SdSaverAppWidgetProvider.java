/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package hram.android.PhotoOfTheDay.appwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import hram.android.PhotoOfTheDay.R;
import hram.android.PhotoOfTheDay.gallery.AndroidCustomGalleryActivity;

/**
 * Виджет отправляющий в Wallpaper событие необходимости сохранения текущих обоев.
 */
public class SdSaverAppWidgetProvider extends AppWidgetProvider {
    // log tag
    //private static final String TAG = "SdSaverAppWidgetProvider";

    protected RemoteViews getRemoteViews(Context context)
    {
    	RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
        remoteViews.setImageViewResource(R.id.img_bluetooth, R.drawable.ic_widget_download_image);
        remoteViews.setImageViewResource(R.id.img_wifi, R.drawable.ic_widget_open_gallery);
        remoteViews.setImageViewResource(R.id.img_gps, R.drawable.ic_widget_next_parser);
        remoteViews.setImageViewResource(R.id.img_sync, R.drawable.ic_widget_settings);
        
        return remoteViews;
    }
    
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
         //Создаем новый RemoteViews
         RemoteViews remoteViews = getRemoteViews(context);

         //Подготавливаем Intent для Broadcast
         Intent active = new Intent(WidgetBroadcastEnum.SAVE_ACTION);
         //создаем наше событие
         PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);
         //регистрируем наше событие
         remoteViews.setOnClickPendingIntent(R.id.btn_bluetooth, actionPendingIntent);

         // открытие галереи
         Intent intent = new Intent(context, AndroidCustomGalleryActivity.class);
         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
         PendingIntent openGalleryPendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
         //регистрируем наше событие
         remoteViews.setOnClickPendingIntent(R.id.btn_wifi, openGalleryPendingIntent);

         //Подготавливаем Intent для Broadcast
         Intent nextParser = new Intent(WidgetBroadcastEnum.NEXT_PARSER_ACTION);
         //создаем наше событие
         PendingIntent nextParserPendingIntent = PendingIntent.getBroadcast(context, 0, nextParser, 0);
         //регистрируем наше событие
         remoteViews.setOnClickPendingIntent(R.id.btn_gps, nextParserPendingIntent);

         //Подготавливаем Intent для Broadcast
         Intent openSttings = new Intent(WidgetBroadcastEnum.SETTINGS_ACTION);
         //создаем наше событие
         PendingIntent openSttingsPendingIntent = PendingIntent.getBroadcast(context, 0, openSttings, 0);
         //регистрируем наше событие
         remoteViews.setOnClickPendingIntent(R.id.btn_sync, openSttingsPendingIntent);

         //обновляем виджет
         appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }
  
//	@Override
//	public void onReceive(Context context, Intent intent) {
//        final String action = intent.getAction();
//        if (action.equals(WidgetBroadcastEnum.OPEN_GALLERY_ACTION)) {
//        	File dir = SDHelper.getAlbumStorageDir();
//			if (!dir.exists()) {
//				return;
//			}
//			Intent i = new Intent(Intent.ACTION_VIEW,  Uri.parse(dir.getAbsolutePath()));
//			context.startActivity(i);
//			
//			super.onReceive(context, intent);
//        }
//	}
}


