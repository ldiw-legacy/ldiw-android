package com.letsdoitworld.wastemapper.pointlist;

import java.io.InputStream;
import java.util.Locale;

import com.letsdoitworld.wastemapper.R;
import com.letsdoitworld.wastemapper.R.id;
import com.letsdoitworld.wastemapper.R.layout;
import com.letsdoitworld.wastemapper.R.raw;
import com.letsdoitworld.wastemapper.R.string;
import com.letsdoitworld.wastemapper.service.MainService;
import com.letsdoitworld.wastemapper.utils.AppConstants;
import com.letsdoitworld.wastemapper.utils.Downloader;
import com.letsdoitworld.wastemapper.utils.SharedPool;

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
          + AppConstants.ADDRESS_LANGUAGE + Locale.getDefault().getLanguage();
      
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
