package com.letsdoitworld.wastemapper;

import java.io.InputStream;

import com.letsdoitworld.wastemapper.R;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.webkit.WebView;

public class ShowPoint2Activity extends Activity {

  private SharedPool sharedPool;
  private WebView webView;
  private String data;
  private Handler mHandler;
  private ProgressDialog progress;
  private boolean done;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.show_point_2);
    sharedPool = SharedPool.getInstance();
    webView = (WebView) findViewById(R.id.webview);
    mHandler = new Handler();
    done = false;
    Thread thread = new Thread(getInfo);
    thread.start();
    progress = new ProgressDialog(this);
    progress.setIndeterminate(true);
    String wait = getResources().getString(R.string.message_wait);
    progress.setMessage(wait);
    progress.setCancelable(true);
    progress.setOnCancelListener(new OnCancelListener() {
      @Override
      public void onCancel(DialogInterface arg0) {
        finish();
      }
    });

  }

  private Runnable getInfo = new Runnable() {
    @Override
    public void run() {
      WindowManager manager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
      String url = getIntent().getStringExtra(ShowPointActivity.EXTRAS_POINT_ID);
      DisplayMetrics metrics = new DisplayMetrics();
      manager.getDefaultDisplay().getMetrics(metrics);
      double width = metrics.widthPixels / metrics.density;
      width = width - (12 * metrics.density);
      url = (String) sharedPool.get(MainService.API_BASE_URL_KEY) + AppConstants.ADDRESS_WASTE_POINT_DETAILS + url + ".html&max_width=" + (int) width
          + AppConstants.ADDRESS_LANGUAGE + (String) sharedPool.get(AppConstants.SP_LANGUAGE);

      InputStream is = Downloader.connectGet(url);
      data = Downloader.convertStreamToString(is);

      Resources res = getResources();
      is = res.openRawResource(R.raw.style);
      String style = Downloader.convertStreamToString(is);

      data = style.concat(data);

      mHandler.post(setInfo);
      done = true;
    }
  };

  private Runnable setInfo = new Runnable() {
    @Override
    public void run() {
      webView.loadDataWithBaseURL("fake://not/needed", data, "text/html", "utf-8", "");
      webView.invalidate();
      progress.dismiss();
    }
  };

  @Override
  protected void onPause() {
    if (!progress.isShowing())
      progress.hide();
    super.onPause();
  }

  @Override
  protected void onResume() {
    if (!done)
      progress.show();
    super.onResume();
  }

}
