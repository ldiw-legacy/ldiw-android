package com.letsdoitworld.wastemapper;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;

public class RestrictMapView extends MapView {

  private Rect rect;
  private SharedPool sharedPool;
  private GestureDetector detector;
  private boolean useRestrictions;
  private RestrictGestureListener listener;

  public RestrictMapView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  public RestrictMapView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public RestrictMapView(Context context, String apiKey) {
    super(context, apiKey);
    init();
  }

  private void init() {
    rect = null;
    sharedPool = SharedPool.getInstance();
    listener = new RestrictGestureListener(this);
    detector = new GestureDetector(this.getContext(), listener);
    useRestrictions = false;
  }

  public void setUseRestrictions(boolean bool) {
    useRestrictions = bool;
  }

  public void setRect(GeoPoint point) {
    Rect re = new Rect();
    int bbox = (Integer) sharedPool.get(AppConstants.SP_BBOX_VALUE);
    re.top = point.getLatitudeE6() - ((2 * bbox * 1000000) / 3);
    re.bottom = point.getLatitudeE6() + ((2 * bbox * 1000000) / 3);
    re.left = point.getLongitudeE6() - bbox * 1000000;
    re.right = point.getLongitudeE6() + bbox * 1000000;
    rect = re;
    listener.setRect(rect);
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    if (useRestrictions) {
      listener.setGeoPoint(getMapCenter());
      listener.setProjection(getProjection());
      if (detector.onTouchEvent(ev))
        return true;
      boolean result = super.onTouchEvent(ev);
      checkZoom();
      return result;
    }
    return super.onTouchEvent(ev);
  }

  public void checkZoom() {
    MapController controller = getController();
    boolean zoomIn = true;
    while (zoomIn) {
      zoomIn = false;
      int newWidth = getLongitudeSpan();
      int newHeight = getLatitudeSpan();

      if (newWidth > rect.width()) {
        zoomIn = true;
      }

      if (newHeight > rect.height()) {
        zoomIn = true;
      }
      if (zoomIn) {
        controller.zoomIn();
        Map.maxZoomOut = getZoomLevel();
      }
    }
  }

}
