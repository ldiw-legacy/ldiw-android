package com.ito.doit;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;


public class MarkersOverlay extends ItemizedOverlay<OverlayItem> {
	
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Drawable defaultMarker;
	private Context mContext;
	private boolean dont_show;
	private Rect rect;
	private SharedPool sharedPool;
	
	public MarkersOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
		dont_show = false;
		sharedPool = SharedPool.getInstance();
	}
	
	public MarkersOverlay(Drawable defaultMarker, Context context, boolean dont_show) {
		super(boundCenterBottom(defaultMarker));
		this.defaultMarker = defaultMarker;
		mContext = context;
		this.dont_show = dont_show;
		sharedPool = SharedPool.getInstance();
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		return mOverlays.size();
	}

	public void addOverlay(OverlayItem overlay) {
	    mOverlays.add(overlay);
	    populate();	    
	}
	
	@Override
	protected boolean onTap(int index) {
		if (!dont_show){
			final OverlayItem item = mOverlays.get(index);
			AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
			dialog.setTitle(R.string.map_dialog_title);
			dialog.setMessage(R.string.map_dialog_message);
			dialog.setNeutralButton(R.string.map_dialog_yes, new OnClickListener(){
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					Intent intent = new Intent(mContext, ShowPoint2Activity.class);
					intent.putExtra(ShowPointActivity.EXTRAS_POINT_ID, item.getSnippet());
					mContext.startActivity(intent);					
				}});
			dialog.setNegativeButton(R.string.map_dialog_no, null);
			dialog.show();
		}
	  return true;
	}
	
	public MarkersOverlay clone(){
		MarkersOverlay temp = new MarkersOverlay(defaultMarker, mContext, dont_show);
		int size = size();
		for (int i = 0; i < size; i ++){
			temp.addOverlay(this.getItem(i));
		}		
		return temp;
	}

	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
			long when) {
		validateMapView(mapView);
		return super.draw(canvas, mapView, shadow, when);
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		validateMapView(mapView);
		super.draw(canvas, mapView, shadow);
	}
	
	public void setRect(GeoPoint point){
		Rect re = new Rect();
		int bbox = (Integer) sharedPool.get(AppConstants.SP_BBOX_VALUE);
		re.left = point.getLatitudeE6() - ((2*bbox)/3)*1000000;
		re.right =  point.getLatitudeE6() + ((2*bbox)/3)*1000000;
		re.top = point.getLongitudeE6() - bbox*1000000;
		re.bottom = point.getLongitudeE6() + bbox*1000000;
		rect = re;
	}

	private void validateMapView(MapView mapView){
		if (rect != null){
			MapController controller = mapView.getController();	
			GeoPoint center = mapView.getMapCenter();
			if (!rect.contains(center.getLatitudeE6(), center.getLongitudeE6())){
				
				int newX = center.getLatitudeE6();
				if (newX > rect.right) {
					newX = rect.right;
				} else if (newX < rect.left){
					newX = rect.left;
				}
				
				int newY = center.getLongitudeE6();
				if (newY > rect.bottom){
					newY = rect.bottom;
				} else if (newY < rect.top){
					newY = rect.top;
				}				
				controller.setCenter(new GeoPoint(newX, newY));
			}
			
			int newWidth = mapView.getLatitudeSpan();
			int newHeight = mapView.getLongitudeSpan();
			
			if (newWidth > rect.width()){
				newWidth = rect.width();
			}
			
			if (newHeight> rect.height()){
				newHeight = rect.height();
			}
			
			controller.zoomToSpan(newWidth, newHeight);
		
		}
	}
	
}
