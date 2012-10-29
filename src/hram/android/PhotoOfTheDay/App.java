package hram.android.PhotoOfTheDay;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

//@ReportsCrashes(formKey = "dGVacG0ydVHnaNHjRjVTUTEtb3FPWGc6MQ")
public class App extends Application {
	
	@Override
	  public void onCreate() {
	      // The following line triggers the initialization of ACRA
	      //ACRA.init(this);
	      super.onCreate();
	  }
}
