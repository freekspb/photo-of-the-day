package hram.android.PhotoOfTheDay.appwidget;

import android.content.Intent;
import android.content.SharedPreferences;
import hram.android.PhotoOfTheDay.Settings;

public class FastSettings extends Settings {

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //Подготавливаем Intent для Broadcast
        Intent changeSttings = new Intent(WidgetBroadcastEnum.CHANGE_SETTINGS_ACTION);
        // помещаем данные об измененном свойстве
        changeSttings.putExtra(WidgetBroadcastEnum.SETTINGS_KEY, key);
        // отправляем
        this.sendBroadcast(changeSttings);
		
		super.onSharedPreferenceChanged(sharedPreferences, key);
	}
}
