package com.letsdoitworld.wastemapper;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.Projection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class CoverView extends ImageView {

  private Projection projection;
  private GeoPoint point;
  private Integer radius;
  private SharedPool sharedPool;
  private Boolean isNearest;

  public CoverView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    init();
  }

  public CoverView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public CoverView(Context context) {
    super(context);
    init();
  }

  private void init() {
    sharedPool = SharedPool.getInstance();
    isNearest = (Boolean) sharedPool.get(AppConstants.SP_NEAREST_FEATURE);
    if (isNearest == null)
      isNearest = false;
  }

  synchronized public void setProjection(Projection projection) {
    this.projection = projection;
  }

  synchronized public void setGeoPoint(GeoPoint point) {
    this.point = point;
  }

  @Override
  public void draw(Canvas canvas) {
    if (projection == null || point == null)
      return;
    int lat = point.getLatitudeE6();
    int lon = point.getLongitudeE6();
    if (!isNearest) {
      radius = (Integer) sharedPool.get(AppConstants.SP_BBOX_VALUE) * 1000000;
      Drawable drawable = getDrawable();
      GeoPoint newPoint = new GeoPoint(lat + (2 * radius) / 3, lon + radius);
      Point top = projection.toPixels(newPoint, null);
      newPoint = new GeoPoint(lat - (2 * radius) / 3, lon - radius);
      Point bottom = projection.toPixels(newPoint, null);
      drawable.setBounds(bottom.x, top.y, top.x, bottom.y);
      drawable.draw(canvas);
    } else {
      Point center = projection.toPixels(point, null);

      double[] lats = (double[]) sharedPool.get(AppConstants.SP_POINTS_LATITUDE);
      double[] lons = (double[]) sharedPool.get(AppConstants.SP_POINTS_LONGITUDE);
      GeoPoint farPoint = new GeoPoint((int) (lats[lats.length - 1] * 1000000), (int) (lons[lons.length - 1] * 1000000));
      Point farest = projection.toPixels(farPoint, null);
      float rad = distanceBetween(center, farest);
      Paint paint = new Paint();
      paint.setARGB(30, 100, 200, 100);
      paint.setStyle(Style.FILL);
      canvas.drawCircle(center.x, center.y, rad, paint);
      paint.setARGB(255, 100, 200, 100);
      paint.setStyle(Style.STROKE);
      canvas.drawCircle(center.x, center.y, rad, paint);
    }
  }

  private float distanceBetween(Point point1, Point point2) {
    float result = 0f;
    double result2 = Math.sqrt((Math.pow((point1.x - point2.x), 2) + Math.pow(point1.y - point2.y, 2)));

    result = (float) result2;
    return result;
  }

}
