package hram.android.PhotoOfTheDay.appwidget;

import hram.android.PhotoOfTheDay.R;
import hram.android.PhotoOfTheDay.gallery.EcoGallery;
import hram.android.PhotoOfTheDay.gallery.ImageAdapter;
import android.app.Activity;
import android.os.Bundle;
import android.widget.BaseAdapter;

public class ImagesGallery extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.ecogallery);
		
        EcoGallery g = (EcoGallery) findViewById(R.id.ecoGallery);
        BaseAdapter adapter = new ImageAdapter(this);
    	g.setAdapter(adapter);
	}
}
