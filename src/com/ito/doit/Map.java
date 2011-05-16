package com.ito.doit;

import java.util.List;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class Map extends MapActivity {

  public static String EXTRAS_DO_LOCATE = "com.ito.doit.do.locate";

  private List<Overlay> mapOverlays;
  private MapView mapView;
  private Context mContext;
  private SharedPool sharedPool;
  private Handler handler;
  private MarkersOverlay locationOverlay;
  private MarkersOverlay wasteOverlay;
  private boolean working;
  private boolean isLocate;
  private Projection projection;
  private GeoPoint mLocation;
  private CoverView cover;
  private ImageView zoomIn;
  private ImageView zoomOut;
  private Thread overlayThread;

  public static int maxZoomOut = 1;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.map);

    mContext = this;
    handler = new Handler();
    working = true;
    cover = (CoverView) findViewById(R.id.zone);
    mapView = (MapView) findViewById(R.id.mapview);
    mapView.getController().setZoom(14);
    // OverlayManager manager = new OverlayManager(mContext, mapView);
    // ManagedOverlay overlay =
    // manager.createOverlay(getResources().getDrawable(R.drawable.dot));
    // overlay.setOnOverlayGestureListener(detector);

    projection = mapView.getProjection();
    cover.setProjection(projection);
    mapView.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View arg0, MotionEvent arg1) {
        if (mLocation != null) {
          projection = mapView.getProjection();
          cover.setProjection(projection);
          cover.invalidate();
        }
        int level = mapView.getZoomLevel();
        if (level <= maxZoomOut) {
          zoomOut.setEnabled(false);
        } else {
          zoomOut.setEnabled(true);
        }
        if (level >= 21) {
          zoomIn.setEnabled(false);
        } else {
          zoomIn.setEnabled(true);
        }
        return false;
      }
    });
    // mapView.setBuiltInZoomControls(true);
    isLocate = getIntent().getBooleanExtra(EXTRAS_DO_LOCATE, true);
    mapOverlays = mapView.getOverlays();
    // mapOverlays.add(overlay);
    // mapView.invalidate();
    sharedPool = SharedPool.getInstance();

    setButtons();

  }

  @Override
  protected boolean isRouteDisplayed() {
    return false;
  }

  private void setButtons() {
    zoomIn = (ImageView) findViewById(R.id.zoom_in);
    zoomIn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View arg0) {
        mapView.getController().zoomIn();
        zoomOut.setEnabled(true);
        if (mapView.getZoomLevel() == 21)
          zoomIn.setEnabled(false);
      }
    });

    zoomOut = (ImageView) findViewById(R.id.zoom_out);
    zoomOut.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View arg0) {
        mapView.getController().zoomOut();
        zoomIn.setEnabled(true);
        ((RestrictMapView) mapView).checkZoom();
        if (mapView.getZoomLevel() == maxZoomOut)
          zoomOut.setEnabled(false);
      }
    });

  }

  private Runnable getLocation = new Runnable() {
    @Override
    public void run() {
      handler.post(invalidate);

      while (!sharedPool.containsKey(AppConstants.SP_LOCATION) && working) {
      }
      if (!working)
        return;
      Location location = (Location) sharedPool.get(AppConstants.SP_LOCATION);
      if (isLocate) {
        MapController controller = mapView.getController();
        controller.animateTo(new GeoPoint((int) (location.getLatitude() * 1000000), (int) (location.getLongitude() * 1000000)));

      }

      try {
        mapOverlays.remove(locationOverlay);
      } catch (Exception e) {
      }

      Drawable drawable = getResources().getDrawable(R.drawable.dot);
      locationOverlay = new MarkersOverlay(drawable, mContext, true);
      mLocation = new GeoPoint((int) (1000000 * location.getLatitude()), (int) (1000000 * location.getLongitude()));
      cover.setGeoPoint(mLocation);
      OverlayItem overlayitem = new OverlayItem(mLocation, "Your", "position");
      RestrictMapView map = (RestrictMapView) findViewById(R.id.mapview);
      map.setRect(mLocation);
      map.setUseRestrictions(true);
      locationOverlay.addOverlay(overlayitem);
      mapOverlays.add(locationOverlay);
      handler.post(invalidate);

      try {
        mapOverlays.remove(wasteOverlay);
      } catch (Exception e) {
      }

      while (!sharedPool.containsKey(AppConstants.SP_POINTS_LATITUDE) && working) {
      }
      if (!working)
        return;
      double[] la = (double[]) sharedPool.get(AppConstants.SP_POINTS_LATITUDE);
      while (!sharedPool.containsKey(AppConstants.SP_POINTS_LONGITUDE) && working) {
      }
      if (!working)
        return;
      double[] lo = (double[]) sharedPool.get(AppConstants.SP_POINTS_LONGITUDE);
      while (!sharedPool.containsKey(AppConstants.SP_POINTS_IDS) && working) {
      }
      if (!working)
        return;
      String[] ids = (String[]) sharedPool.get(AppConstants.SP_POINTS_IDS);

      int size = la.length;
      if (size > 0) {
        drawable = getResources().getDrawable(R.drawable.pin);
        wasteOverlay = new MarkersOverlay(drawable, mContext);
        GeoPoint point;
        for (int i = 0; i < size; i++) {
          point = new GeoPoint((int) (1000000 * la[i]), (int) (1000000 * lo[i]));
          overlayitem = new OverlayItem(point, String.valueOf(i), ids[i]);
          wasteOverlay.addOverlay(overlayitem);
        }
        mapOverlays.add(wasteOverlay);
      }
      handler.post(invalidate);
    }
  };

  private Runnable invalidate = new Runnable() {
    @Override
    public void run() {
      mapView.invalidate();
      cover.invalidate();
    }
  };

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.layout.map_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.list:
      Intent intent = new Intent(this, PointsListActivity.class);
      this.startActivity(intent);
      return true;
    case R.id.new_point:
      Intent intent2 = new Intent(this, NewPointActivity.class);
      this.startActivity(intent2);
      return true;
    }
    ;
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onPause() {
    working = false;
    MainService.gone();
    Intent intent = new Intent(DoItActions.ACTIONS_STOP_SEEK_LOCATION);
    mContext.sendBroadcast(intent);
    super.onPause();
  }

  @Override
  protected void onResume() {
    working = true;
    MainService.startService(this);
    MainService.setLocationListener(listener);
    if (overlayThread == null || !overlayThread.isAlive()) {
      overlayThread = new Thread(getLocation);
      overlayThread.start();
    }
    MainService.setDataListener(dataListener);
    Thread thread = new Thread(waitToStart);
    thread.start();
    super.onResume();
  }

  private Runnable waitToStart = new Runnable() {
    @Override
    public void run() {
      while (!MainService.isWorking() && working) {
      }
      if (!working)
        return;
      Intent intent = new Intent(DoItActions.ACTIONS_START_SEEK_LOCATION);
      mContext.sendBroadcast(intent);
    }
  };

  private LocationListener listener = new LocationListener() {
    @Override
    public void onLocationChanged(Location location) {
      Log.i(MainService.TAG, "Location: " + location.getLatitude() + " " + location.getLongitude());

      try {
        mapOverlays.remove(locationOverlay);
      } catch (Exception e) {
      }

      Drawable drawable = getResources().getDrawable(R.drawable.dot);
      locationOverlay = new MarkersOverlay(drawable, mContext, true);
      mLocation = new GeoPoint((int) (1000000 * location.getLatitude()), (int) (1000000 * location.getLongitude()));
      cover.setGeoPoint(mLocation);

      OverlayItem overlayitem = new OverlayItem(mLocation, "Your", "position");
      RestrictMapView map = (RestrictMapView) findViewById(R.id.mapview);
      map.setRect(mLocation);
      map.setUseRestrictions(true);
      locationOverlay.addOverlay(overlayitem);
      mapOverlays.add(locationOverlay);
      handler.post(invalidate);
    }
  };

  private OnDataListener dataListener = new OnDataListener() {
    @Override
    public void onData(int objectCode, Object data) {
      if (MainService.POINTS_DATA == objectCode) {
        isLocate = true;
        if (overlayThread == null || !overlayThread.isAlive()) {
          overlayThread = new Thread(getLocation);
          overlayThread.start();
        }
      }
    }
  };

}