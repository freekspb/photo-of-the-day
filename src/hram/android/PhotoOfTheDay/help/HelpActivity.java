package hram.android.PhotoOfTheDay.help;

import hram.android.PhotoOfTheDay.R;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

public final class HelpActivity extends Activity {

  // Use this key and one of the values below when launching this activity via intent. If not
  // present, the default page will be loaded.
  public static final String REQUESTED_PAGE_KEY = "requested_page_key";
  public static final String DEFAULT_PAGE = "index.html";
  public static final String WHATS_NEW_PAGE = "whatsnew.html";

  private static final String BASE_URL = "file:///android_asset/";
  private static final String WEBVIEW_STATE_PRESENT = "webview_state_present";

  private WebView webView;
  private Button backButton;

  private String getBaseUrl() {
	return BASE_URL + this.getString(R.string.assets_html_folder);
  }
  
  private final Button.OnClickListener backListener = new Button.OnClickListener() {
    public void onClick(View view) {
      webView.goBack();
    }
  };

  private final Button.OnClickListener doneListener = new Button.OnClickListener() {
    public void onClick(View view) {
      finish();
    }
  };

  @Override
  protected void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    setContentView(R.layout.help);

    webView = (WebView)findViewById(R.id.help_contents);
    webView.setWebViewClient(new HelpClient());

    // Froyo has a bug with calling onCreate() twice in a row, which causes the What's New page
    // that's auto-loaded on first run to appear blank. As a workaround we only call restoreState()
    // if a valid URL was loaded at the time the previous activity was torn down.
    Intent intent = getIntent();
    if (icicle != null && icicle.getBoolean(WEBVIEW_STATE_PRESENT, false)) {
      webView.restoreState(icicle);
    } else if (intent != null) {
      String page = intent.getStringExtra(REQUESTED_PAGE_KEY);
      if (page != null && page.length() > 0) {
        webView.loadUrl(getBaseUrl() + page);
        //webView.loadDataWithBaseURL("", BASE_URL + page, "text/html", "utf-8", "");
      } else {
        webView.loadUrl(getBaseUrl() + DEFAULT_PAGE);
        //webView.loadDataWithBaseURL("", BASE_URL + DEFAULT_PAGE, "text/html", "utf-8", "");
      }
    } else {
      webView.loadUrl(getBaseUrl() + DEFAULT_PAGE);
      //webView.loadDataWithBaseURL("", BASE_URL + DEFAULT_PAGE, "text/html", "utf-8", "");
    }

    backButton = (Button) findViewById(R.id.back_button);
    backButton.setOnClickListener(backListener);
    View doneButton = findViewById(R.id.done_button);
    doneButton.setOnClickListener(doneListener);
  }

  @Override
  protected void onSaveInstanceState(Bundle state) {
    String url = webView.getUrl();
    if (url != null && url.length() > 0) {
      webView.saveState(state);
      state.putBoolean(WEBVIEW_STATE_PRESENT, true);
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      if (webView.canGoBack()) {
        webView.goBack();
        return true;
      }
    }
    return super.onKeyDown(keyCode, event);
  }

  private final class HelpClient extends WebViewClient {
    @Override
    public void onPageFinished(WebView view, String url) {
      setTitle(view.getTitle());
      backButton.setEnabled(view.canGoBack());
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
      if (url.startsWith("file")) {
        // Keep local assets in this WebView.
        return false;
      } else {
        // Open external URLs in Browser.
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        return true;
      }
    }
  }
}
