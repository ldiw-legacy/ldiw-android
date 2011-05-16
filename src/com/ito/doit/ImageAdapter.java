package com.ito.doit;

import java.util.HashMap;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageAdapter extends BaseAdapter {
  private Context mContext;
  private String[] keys;
  private double[] values;
  private int limit;
  private boolean showTitle;

  public ImageAdapter(Context c, HashMap<String, String> map) {
    mContext = c;
    limit = -1;
    showTitle = true;
    mineData(map);
  }

  public ImageAdapter(Context c, HashMap<String, String> map, int limit, boolean showTitle) {
    mContext = c;
    this.showTitle = showTitle;
    this.limit = limit;
    mineData(map);
  }

  public int getCount() {
    return limit;
  }

  public Object getItem(int position) {
    return null;
  }

  public long getItemId(int position) {
    return 0;
  }

  public View getView(int position, View convertView, ViewGroup parent) {
    View view;
    if (convertView == null) {
      LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      if (showTitle)
        view = inflater.inflate(R.layout.comp_item, null);
      else
        view = inflater.inflate(R.layout.comp_item_no_title, null);
    } else {
      view = convertView;
    }

    ImageView imageView = (ImageView) view.findViewById(R.id.image);
    imageView.setImageResource(getImageId(keys[position]));
    if (showTitle) {
      TextView textView = (TextView) view.findViewById(R.id.title);
      textView.setText(getTextId(keys[position]));
    }
    return view;
  }

  private void mineData(HashMap<String, String> map) {
    HashMap<String, String> minedData = new HashMap<String, String>();
    if (map == null || map.size() == 0) {
      minedData.put("composition_other", "100");
    } else {
      for (String key : map.keySet()) {
        if (available(key)) {
          String value = map.get(key);
          if (value != null && value.length() != 0) {
            try {
              double val = Double.parseDouble(value);
              if (val > 0) {
                minedData.put(key, map.get(key));
              }
            } catch (Exception e) {
            }
          }
        }
      }
    }
    mapToArrays(minedData);
  }

  private boolean available(String key) {
    if (key.contentEquals("composition_glass"))
      return true;
    if (key.contentEquals("composition_large"))
      return true;
    if (key.contentEquals("composition_other"))
      return true;
    if (key.contentEquals("nr_of_tires"))
      return true;
    if (key.contentEquals("composition_pmp"))
      return true;
    if (key.contentEquals("composition_metal"))
      return true;
    return false;
  }

  private void mapToArrays(HashMap<String, String> map) {
    keys = new String[map.size()];
    values = new double[map.size()];
    keys = map.keySet().toArray(keys);
    if (limit < 0 || limit > keys.length) {
      limit = keys.length;
    }
    for (int i = 0; i < keys.length; i++)
      values[i] = Double.parseDouble(map.get(keys[i]));
    for (int i = 0; i < limit; i++) {
      double max = values[i];
      int index = i;
      for (int x = i + 1; x < keys.length; x++) {
        if (max < values[x]) {
          index = x;
          max = values[x];
        }
      }

      String key = keys[index];
      keys[index] = keys[i];
      keys[i] = key;
      values[index] = values[i];
      values[i] = max;
    }

  }

  public static int getImageId(String key) {
    if (key.contentEquals("composition_glass"))
      return R.drawable.glass;
    if (key.contentEquals("composition_large"))
      return R.drawable.large;
    if (key.contentEquals("composition_other"))
      return R.drawable.other;
    if (key.contentEquals("nr_of_tires"))
      return R.drawable.tires;
    if (key.contentEquals("composition_pmp"))
      return R.drawable.pmp;
    if (key.contentEquals("composition_metal"))
      return R.drawable.metal;
    return R.drawable.other;
  }

  public static int getTextId(String key) {
    if (key.contentEquals("composition_glass"))
      return R.string.sGlass;
    if (key.contentEquals("composition_large"))
      return R.string.sLarge;
    if (key.contentEquals("composition_other"))
      return R.string.sOther;
    if (key.contentEquals("nr_of_tires"))
      return R.string.sTires;
    if (key.contentEquals("composition_pmp"))
      return R.string.sPmp;
    if (key.contentEquals("composition_metal"))
      return R.string.sMetal;
    return R.string.sNo;
  }

}