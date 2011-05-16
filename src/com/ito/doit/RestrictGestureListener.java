package com.ito.doit;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;
import android.view.MotionEvent;
import android.view.GestureDetector.SimpleOnGestureListener;

public class RestrictGestureListener extends SimpleOnGestureListener {

  private MapView map;
  private GeoPoint point;
  private Projection projection;
  private Rect rect;

  public RestrictGestureListener(MapView mapView) {
    map = mapView;
  }

  public void setGeoPoint(GeoPoint point) {
    this.point = point;
  }

  public void setProjection(Projection proj) {
    projection = proj;
  }

  public void setRect(Rect rect) {
    this.rect = rect;
  }

  @Override
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    if (point == null || projection == null || rect == null)
      return false;
    Point center = projection.toPixels(point, null);
    center.x += distanceX;
    center.y += distanceY;
    GeoPoint futurePoint = projection.fromPixels(center.x, center.y);

    int x = futurePoint.getLongitudeE6();
    int y = futurePoint.getLatitudeE6();

    if (!rect.contains(futurePoint.getLongitudeE6(), futurePoint.getLatitudeE6())) {
      MapController controller = map.getController();
      int newX = futurePoint.getLongitudeE6();
      if (newX > rect.right) {
        newX = rect.right;
      } else if (newX < rect.left) {
        newX = rect.left;
      }

      int newY = futurePoint.getLatitudeE6();
      if (newY > rect.bottom) {
        newY = rect.bottom;
      } else if (newY < rect.top) {
        newY = rect.top;
      }
      controller.setCenter(new GeoPoint(newY, newX));
      return true;
    }
    return false;
  }

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    /*
     * if (point == null || projection == null || rect == null) return false;
     * Point center = projection.toPixels(point, null); center.x += velocityX;
     * center.y += velocityY; GeoPoint futurePoint =
     * projection.fromPixels(center.x, center.y); Log.w(MainService.TAG,
     * "X greitis: "+ velocityX); Log.w(MainService.TAG, "Y greitis: "+
     * velocityY);
     * 
     * int x = futurePoint.getLongitudeE6(); int y =
     * futurePoint.getLatitudeE6();
     * 
     * if (!rect.contains(futurePoint.getLongitudeE6(),
     * futurePoint.getLatitudeE6())){ MapController controller =
     * map.getController(); int newX = futurePoint.getLongitudeE6(); if (newX >
     * rect.right) { newX = rect.right; } else if (newX < rect.left){ newX =
     * rect.left; }
     * 
     * int newY = futurePoint.getLatitudeE6(); if (newY > rect.bottom){ newY =
     * rect.bottom; } else if (newY < rect.top){ newY = rect.top; }
     * controller.setCenter(new GeoPoint(newY, newX)); return true; } return
     * false;
     */
    return true;
  }

}
