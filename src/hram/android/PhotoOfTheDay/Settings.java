package hram.android.PhotoOfTheDay;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class Settings  extends PreferenceActivity  implements SharedPreferences.OnSharedPreferenceChangeListener 
{
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    getPreferenceManager().setSharedPreferencesName(Constants.SETTINGS_NAME);
        addPreferencesFromResource(R.xml.preferences);
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        
        ListPreference sourcesPref = (ListPreference) findPreference(Constants.SOURCES_NAME);
        sourcesPref.setDefaultValue(getPreferenceManager().getSharedPreferences().getString(Constants.SOURCES_NAME, "1"));
	}
	
	@Override
    protected void onResume() 
	{
        super.onResume();
    }

    @Override
    protected void onDestroy() 
    {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onDestroy();
    }

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) 
	{
		
	}
	
}
