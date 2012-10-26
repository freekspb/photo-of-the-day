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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.RemoteViews;
// Need the following import to get access to the app resources, since this
// class is in a sub-package.
import hram.android.PhotoOfTheDay.R;

/**
 * Виджет отправляющий в Wallpaper событие необходимости сохранения текущих обоев.
 */
public class SdSaverAppWidgetProvider extends AppWidgetProvider {
    // log tag
    private static final String TAG = "SdSaverAppWidgetProvider";
   
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
         //Создаем новый RemoteViews
         RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);
         remoteViews.setImageViewResource(R.id.img_bluetooth, R.drawable.ic_widget_download_image);
         remoteViews.setImageViewResource(R.id.img_wifi, R.drawable.ic_widget_open_gallery);

         //Подготавливаем Intent для Broadcast
         Intent active = new Intent(WidgetBroadcastEnum.SAVE_ACTION);

         //создаем наше событие
         PendingIntent actionPendingIntent = PendingIntent.getBroadcast(context, 0, active, 0);

         //регистрируем наше событие
         remoteViews.setOnClickPendingIntent(R.id.btn_bluetooth, actionPendingIntent);

         //Подготавливаем Intent для Broadcast
         Intent openGallery = new Intent(WidgetBroadcastEnum.OPEN_GALLERY_ACTION);

         //создаем наше событие
         PendingIntent openGalleryPendingIntent = PendingIntent.getBroadcast(context, 0, openGallery, 0);

         //регистрируем наше событие
         remoteViews.setOnClickPendingIntent(R.id.btn_wifi, openGalleryPendingIntent);

         //обновляем виджет
         appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
    }
  
    @Override
    public void onEnabled(Context context) {
        Log.d(TAG, "onEnabled");
        // When the first widget is created, register for
        // broadcasts.  We don't want to be listening for these if nobody has our widget active.
        // This setting is sticky across reboots, but that doesn't matter, because this will
        // be called after boot if there is a widget instance for this provider.
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName("hram.android.PhotoOfTheDay", ".appwidget.WidgetBroadcastReceiver"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(
                new ComponentName("hram.android.PhotoOfTheDay", ".appwidget.SdSaverAppWidgetProvider"),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onDisabled(Context context) {
        // When the first widget is created, stop listening for
        // TIME_CHANGED broadcasts.
        Log.d(TAG, "onDisabled");
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(
                new ComponentName("hram.android.PhotoOfTheDay", ".appwidget.WidgetBroadcastReceiver"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
        pm.setComponentEnabledSetting(
                new ComponentName("hram.android.PhotoOfTheDay", ".appwidget.SdSaverAppWidgetProvider"),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
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


