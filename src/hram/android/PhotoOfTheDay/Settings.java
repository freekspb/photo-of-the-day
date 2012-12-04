package hram.android.PhotoOfTheDay;

import hram.android.PhotoOfTheDay.help.HelpActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
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
        
        Preference pref = findPreference("howToUsePref");
        pref.setOnPreferenceClickListener( new OnPreferenceClickListener() 
        {
			public boolean onPreferenceClick(Preference preference) 
			{
				ShowInBrowser();
				return true;
			}
		} );

        Preference whatsNew = findPreference("softwareVersionPref");
        // выводим текущую версию
        String version = "";
		try {
			PackageInfo info = getPackageManager().getPackageInfo(this.getPackageName(), 0);
			version = info.versionName;
		} catch (NameNotFoundException e) {
		}
        whatsNew.setSummary(getString(R.string.softwareVersion) + ": " + version);
        // обработчик клика
        whatsNew.setOnPreferenceClickListener( new OnPreferenceClickListener() 
        {
			public boolean onPreferenceClick(Preference preference) 
			{
				ShowHelp();
				return true;
			}
		} );
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
	
	public void ShowInBrowser()
	{
		Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://blog.hram0v.com/2012/05/foto-dnya-zhivyie-oboi-ot-yandeks-fotki-flickr-i-na/"));
        //intent/*.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)*/.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
	}

	public void ShowHelp()
	{
		Intent intent = new Intent(this, HelpActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		String page = HelpActivity.WHATS_NEW_PAGE;
		//page = HelpActivity.DEFAULT_PAGE;
		intent.putExtra(HelpActivity.REQUESTED_PAGE_KEY, page);
		
		startActivity(intent);
	}
}
