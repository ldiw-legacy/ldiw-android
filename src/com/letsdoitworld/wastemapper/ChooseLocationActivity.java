package com.letsdoitworld.wastemapper;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;
import com.letsdoitworld.wastemapper.R;

public class ChooseLocationActivity extends MapActivity {

  public static final int OK = 1;
  private double lat;
  private double lon;
  private MapView mapView;
  private Context mContext;
  private AlertDialog.Builder builder;
  private int pastX;
  private int pastY;
  private ImageView zoomIn;
  private ImageView zoomOut;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    Thread thread = new Thread(create);
    thread.start();
    super.onCreate(savedInstanceState);
    setContentView(R.layout.map);
    mContext = this;
    mapView = (MapView) findViewById(R.id.mapview);
    mapView.setOnTouchListener(new OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
          pastX = (int) event.getX();
          pastY = (int) event.getY();

        } else if (action == MotionEvent.ACTION_UP) {
          Projection projection = mapView.getProjection();
          int x = (int) event.getX();
          int y = (int) event.getY();
          if (distance(pastX, pastY, x, y) > 10)
            return false;
          GeoPoint point = projection.fromPixels(x, y);
          lat = ((double) point.getLatitudeE6()) / 1000000;
          lon = ((double) point.getLongitudeE6()) / 1000000;
          refreshLocation(false);
        }

        int level = mapView.getZoomLevel();
        if (level <= 1) {
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
    setButtons();

  }

  private Runnable create = new Runnable() {
    @Override
    public void run() {
      createDialog();
    }
  };

  private void setButtons() {
    zoomIn = (ImageView) findViewById(R.id.zoom_in);
    zoomIn.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View arg0) {
        mapView.getController().zoomIn();
      }
    });
    zoomOut = (ImageView) findViewById(R.id.zoom_out);
    zoomOut.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View arg0) {
        mapView.getController().zoomOut();
      }
    });

  }

  @Override
  protected boolean isRouteDisplayed() {
    return false;
  }

  private int distance(int pastX, int pastY, int x, int y) {
    return (int) Math.sqrt(Math.pow((pastX - x), 2) + Math.pow((pastY - y), 2));
  }

  @Override
  protected void onResume() {
    super.onResume();
    MainService.startService(this);
    Intent intent = getIntent();
    try {
      lat = intent.getDoubleExtra(DoItActions.EXTRAS_LATITUDE, -1);
      lon = intent.getDoubleExtra(DoItActions.EXTRAS_LONGITUDE, -1);
    } catch (Exception e) {
      finish();
      return;
    }
    if (lat == -1 || lon == -1) {
      finish();
      return;
    }
    refreshLocation(true);
  }

  private void refreshLocation(boolean animate) {
    if (animate) {
      MapController controller = mapView.getController();
      controller.animateTo(new GeoPoint((int) (lat * 1000000), (int) (lon * 1000000)));
    }
    List<Overlay> mapOverlays = mapView.getOverlays();
    mapOverlays.clear();
    Drawable drawable = getResources().getDrawable(R.drawable.pin);
    MarkersOverlay locationOverlay = new MarkersOverlay(drawable, mContext, true);
    GeoPoint point = new GeoPoint((int) (1000000 * lat), (int) (1000000 * lon));
    OverlayItem overlayitem = new OverlayItem(point, "Your", "position");
    locationOverlay.addOverlay(overlayitem);
    mapOverlays.add(locationOverlay);
    mapView.invalidate();
  }

  @Override
  protected void onPause() {
    super.onPause();
    MainService.gone();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater inflater = getMenuInflater();
    inflater.inflate(R.layout.change_point_menu, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case R.id.ok:
      Intent intent = new Intent();
      intent.putExtra(DoItActions.EXTRAS_LATITUDE, lat);
      intent.putExtra(DoItActions.EXTRAS_LONGITUDE, lon);
      setResult(OK, intent);
      finish();
      return true;
    case R.id.cancel:
      finish();
      return true;
    }
    ;
    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      while (builder == null) {
      }
      builder.show();
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  private void createDialog() {
    builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.choose_title);
    builder.setMessage(R.string.choose_msg);
    builder.setPositiveButton(R.string.choose_set, new AlertDialog.OnClickListener() {
      @Override
      public void onClick(DialogInterface arg0, int arg1) {
        Intent intent = new Intent();
        intent.putExtra(DoItActions.EXTRAS_LATITUDE, lat);
        intent.putExtra(DoItActions.EXTRAS_LONGITUDE, lon);
        setResult(OK, intent);
        finish();
      }
    });
    builder.setNeutralButton(R.string.choose_leave, new AlertDialog.OnClickListener() {
      @Override
      public void onClick(DialogInterface arg0, int arg1) {
        finish();
      }
    });
    builder.setNegativeButton(R.string.choose_cancel, null);
  }

}
