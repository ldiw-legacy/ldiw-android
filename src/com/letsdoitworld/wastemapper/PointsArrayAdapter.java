package com.letsdoitworld.wastemapper;

import java.util.ArrayList;
import java.util.HashMap;

import com.letsdoitworld.wastemapper.R;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class PointsArrayAdapter extends ArrayAdapter<String> {

  private Context mContext;
  private SharedPool sharedPool;
  private double[] distances;
  private ImageAdapter[] adapters;
  private Handler handler;
  private DoneListener listener;
  private boolean isWorking;
  private Thread getDataThread;

  public PointsArrayAdapter(Context context, int resource, int textViewResourceId, DoneListener listener) {
    super(context, resource, textViewResourceId, new ArrayList<String>());

    handler = new Handler();
    mContext = context;
    sharedPool = SharedPool.getInstance();
    distances = null;
    this.listener = listener;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    View view = convertView;
    if (view == null) {
      LayoutInflater vi = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      view = vi.inflate(R.layout.list_item, null);
    }
    final String id = getItem(position);
    TextView text;
    if (id != null) {
      text = (TextView) view.findViewById(R.id.point_id);
      if (id.length() > 5) {
        text.setText(id.substring(0, 5));
      } else {
        text.setText(id);
      }
    }
    if (distances != null) {
      try {
        double dist = distances[position];
        text = (TextView) view.findViewById(R.id.point_distance);
        text.setText(String.format("%.2f", (dist / 1000)));

      } catch (Exception e) {
      }
    }

    if (adapters != null) {
      try {
        ImageAdapter adapter = adapters[position];
        GridView grid = (GridView) view.findViewById(R.id.gridview);
        grid.setAdapter(adapter);
        grid.setOnItemClickListener(new OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
            Intent intent = new Intent(mContext, ShowPoint2Activity.class);
            intent.putExtra(ShowPointActivity.EXTRAS_POINT_ID, id);
            mContext.startActivity(intent);
          }
        });
      } catch (Exception e) {
      }
    }
    return view;
  }

  private Runnable getData = new Runnable() {
    @Override
    public void run() {
      while (!sharedPool.containsKey(AppConstants.SP_DISTANCES) && isWorking) {
      }
      if (!isWorking)
        return;
      distances = (double[]) sharedPool.get(AppConstants.SP_DISTANCES);
      while (!sharedPool.containsKey(AppConstants.SP_POINTS_ARRAY) && isWorking) {
      }
      if (!isWorking)
        return;
      try {
        ArrayList<HashMap<String, String>> array = (ArrayList<HashMap<String, String>>) sharedPool.get(AppConstants.SP_POINTS_ARRAY);
        adapters = new ImageAdapter[array.size()];
        for (int i = 0; i < array.size(); i++)
          adapters[i] = new ImageAdapter(mContext, array.get(i), 3, false);
      } catch (Exception e) {
        adapters = null;
      }

      while (!sharedPool.containsKey(AppConstants.SP_POINTS_IDS)) {
      }
      handler.post(addToArray);
    }
  };

  private Runnable addToArray = new Runnable() {
    @Override
    public void run() {
      String[] ids = (String[]) sharedPool.get(AppConstants.SP_POINTS_IDS);
      for (String e : ids) {
        add(e);
      }
      if (listener != null)
        listener.onDone();
    }
  };

  public void refreshAll() {
    isWorking = true;
    if (getDataThread == null || !getDataThread.isAlive()) {
      clear();
      getDataThread = new Thread(getData);
      getDataThread.start();
    }
  }

  public void stop() {
    isWorking = false;
  }

}
