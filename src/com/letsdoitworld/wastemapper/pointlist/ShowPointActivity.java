package com.letsdoitworld.wastemapper.pointlist;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.letsdoitworld.wastemapper.R;
import com.letsdoitworld.wastemapper.R.drawable;
import com.letsdoitworld.wastemapper.R.id;
import com.letsdoitworld.wastemapper.R.layout;
import com.letsdoitworld.wastemapper.R.string;
import com.letsdoitworld.wastemapper.map.Map;
import com.letsdoitworld.wastemapper.map.MarkersOverlay;
import com.letsdoitworld.wastemapper.pointform.ImageAdapter;
import com.letsdoitworld.wastemapper.service.MainService;
import com.letsdoitworld.wastemapper.utils.AppConstants;
import com.letsdoitworld.wastemapper.utils.DoItActions;
import com.letsdoitworld.wastemapper.utils.Downloader;
import com.letsdoitworld.wastemapper.utils.SharedPool;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ShowPointActivity extends MapActivity {

  public static final String EXTRAS_POINT_ID = "com.letsdoitworld.wastemapper.showpointactvity.extras.point.id";

  private Context mContext;
  private SharedPool sharedPool;
  private String pointId;
  private HashMap<String, String> map;
  private Handler handler;
  private double lat;
  private double lon;
  private MapView mapView;
  private Drawable photo;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.show_point);
    mContext = this;
    handler = new Handler();

    mapView = (MapView) findViewById(R.id.mapview);

    View view = (View) findViewById(R.id.map_overlay);
    view.setOnClickListener(new OnClickListener() {

      @Override
      public void onClick(View v) {
        Intent intent = new Intent(mContext, Map.class);
        intent.putExtra(Map.EXTRAS_DO_LOCATE, false);
        mContext.startActivity(intent);
        finish();
      }

    });

    sharedPool = SharedPool.getInstance();
    Intent intent = getIntent();
    pointId = intent.getStringExtra(EXTRAS_POINT_ID);
    if (pointId == null) {
      Toast.makeText(mContext, R.string.text_no_id, Toast.LENGTH_SHORT);
      finish();
      return;
    }
    TextView textView = (TextView) findViewById(R.id.text_id);
    textView.setText(pointId);

    Thread thread = new Thread(getInfo);
    thread.start();

  }

  public HashMap<String, String> getPointInfo() {
    if (pointId == null)
      return null;
    if (!sharedPool.containsKey(AppConstants.SP_POINTS_ARRAY)) {
      Intent intent = new Intent(DoItActions.ACTION_GET_WASTE_POINTS);
      mContext.sendBroadcast(intent);
    }

    while (!sharedPool.containsKey(MainService.API_BASE_URL_KEY)) {
    }
    try {
      String url = (String) sharedPool.get(MainService.API_BASE_URL_KEY);
      url = url + "/wp/" + pointId + ".html&max_width=1";
      InputStream is = Downloader.connectGet(url);
      String data = Downloader.convertStreamToString(is);
      String imageUrl = findImageTag(data);
      if (imageUrl != null && imageUrl.length() != 0) {
        is = Downloader.connectGet(imageUrl);
        photo = Drawable.createFromStream(is, "src");
      }
    } catch (Exception e) {
    }

    while (!sharedPool.containsKey(AppConstants.SP_POINTS_ARRAY)) {
    }
    try {
      ArrayList<HashMap<String, String>> pointsData = (ArrayList<HashMap<String, String>>) sharedPool.get(AppConstants.SP_POINTS_ARRAY);
      double val;
      for (HashMap<String, String> map : pointsData) {
        if (map.containsValue(pointId)) {
          HashMap<String, String> result = new HashMap<String, String>();
          for (String key : map.keySet()) {
            String value = map.get(key);
            if (value != null && value.length() != 0) {
              try {
                val = Double.parseDouble(value);
              } catch (Exception e) {
                val = -1;
              }
              ;
              if (val != 0)
                result.put(key, value);
            }
          }
          try {
            lat = Double.parseDouble(result.remove("lat"));
            lon = Double.parseDouble(result.remove("lon"));
          } catch (Exception e) {
            lat = 0;
            lon = 0;
          }

          result.remove("id");

          return result;
        }
      }
    } catch (Exception e) {
    }
    return null;
  }

  private Runnable getInfo = new Runnable() {
    @Override
    public void run() {
      map = getPointInfo();
      handler.post(setInfo);
    }
  };

  private Runnable setInfo = new Runnable() {
    @Override
    public void run() {

      MapController controller = mapView.getController();
      controller.animateTo(new GeoPoint((int) (lat * 1000000), (int) (lon * 1000000)));

      List<Overlay> mapOverlays = mapView.getOverlays();

      Drawable drawable = getResources().getDrawable(R.drawable.pin);
      MarkersOverlay locationOverlay = new MarkersOverlay(drawable, mContext, true);
      GeoPoint point = new GeoPoint((int) (1000000 * lat), (int) (1000000 * lon));
      OverlayItem overlayitem = new OverlayItem(point, "Your", "position");
      locationOverlay.addOverlay(overlayitem);
      mapOverlays.add(locationOverlay);
      mapView.invalidate();

      GridView gridview = (GridView) findViewById(R.id.gridview);
      gridview.setAdapter(new ImageAdapter(mContext, map));

      if (map.containsKey("description")) {
        TextView textView = (TextView) findViewById(R.id.text_desc);
        textView.setText(Html.fromHtml(map.get("description")));
      }

      if (photo != null) {
        ImageView image = (ImageView) findViewById(R.id.photo);
        image.setImageDrawable(photo);
      }

    }
  };

  @Override
  protected void onPause() {
    MainService.gone();
    super.onPause();
  }

  @Override
  protected void onResume() {
    MainService.startService(this);
    super.onResume();
  }

  @Override
  protected boolean isRouteDisplayed() {
    return false;
  }

  private String findImageTag(String data) {
    if (data == null || data.length() == 0)
      return "";
    int start = data.indexOf("<img");
    if (start != -1) {
      int srcStart = data.indexOf("src", start);
      if (srcStart != -1) {
        start = data.indexOf("\"", srcStart);
        if (start != -1) {
          int end = data.indexOf("\"", start + 1);
          if (end != -1) {
            return data.substring(start + 1, end);
          }
        }
      }
    }
    return "";
  }

}
