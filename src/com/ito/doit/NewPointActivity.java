package com.ito.doit;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewSwitcher;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class NewPointActivity extends MapActivity {

  public static final int REQUEST_CODE_MAP = 22; // Nice number, in fact :)
  public static final int REQUEST_CODE_CAMERA = 12;

  private SharedPool sharedPool;
  private ViewGroup viewGroup;
  private LinearLayout layout;
  private Context mContext;
  private Handler mHandler;
  private boolean doWork;
  private boolean finished;
  private ArrayList<FieldInfo> map;
  private HashMap<String, Set<String>> fieldSets;
  private Double lat;
  private Double lon;
  private Location location;
  private MapView mapView;
  private Uri imageUri;
  private Button sendData;
  private ToggleButton modeButton;
  private File photoFile;
  private boolean gpsLocation;
  private ViewSwitcher switcher;
  private boolean added;
  private boolean afterResult;
  private ProgressDialog progress;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    gpsLocation = true;
    super.onCreate(savedInstanceState);
    setContentView(R.layout.new_point);
    afterResult = false;
    sharedPool = SharedPool.getInstance();
    mContext = this;
    mHandler = new Handler();

    map = new ArrayList<FieldInfo>();
    fieldSets = new HashMap<String, Set<String>>();
    lat = null;
    lon = null;

    modeButton = (ToggleButton) findViewById(R.id.modeChanger);
    modeButton.setOnCheckedChangeListener(new OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton arg0, boolean state) {
        Boolean mode = (Boolean) sharedPool.get(AppConstants.SP_MODE);
        if (state != mode) {
          changeMode();
        }
      }
    });

    mapView = (MapView) findViewById(R.id.mapview);

    viewGroup = (ViewGroup) findViewById(R.id.container);

    sendData = (Button) findViewById(R.id.confirm);
    sendData.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View arg0) {
        if (validateData()) {
          progress.show();
          Thread thread = new Thread(addPoint);
          thread.start();
          sendData.setEnabled(false);
        }
      }
    });

    switcher = (ViewSwitcher) findViewById(R.id.switcher);

    ImageButton imgButton = (ImageButton) findViewById(R.id.camera_button);
    imgButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        String fileName = "new-photo-name.jpg";
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, fileName);
        values.put(MediaStore.Images.Media.DESCRIPTION, "Image capture by camera");
        imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
      }
    });

    IMEView listen = (IMEView) findViewById(R.id.imeListener);
    listen.setIMEListener(changeVisibility);

    progress = new ProgressDialog(this);
    progress.setIndeterminate(true);
    String wait = getResources().getString(R.string.message_adding);
    progress.setMessage(wait);
    progress.setCancelable(false);

  }

  private Runnable addPoint = new Runnable() {
    @Override
    public void run() {

      String url = (String) sharedPool.get(MainService.API_BASE_URL_KEY);
      if (url == null)
        return;
      url = url.concat(AppConstants.ADDRESS_PUT_WASTE_POINT);

      //Get the session
      JSONObject session = (JSONObject) sharedPool.get(AppConstants.SP_JSON_LOGIN);
      String sessionString = null;
      if (session != null) {
        try {
          sessionString = session.getString("session_name") + "=" + session.getString("sessid");
        } catch (JSONException e1) {
          e1.printStackTrace();
        }
      }
      
      ArrayList<BasicNameValuePair> array = new ArrayList<BasicNameValuePair>();
      array.add(new BasicNameValuePair("lat", String.valueOf(lat)));
      array.add(new BasicNameValuePair("lon", String.valueOf(lon)));
      if (photoFile != null) {
        Rect out = new Rect();
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 4;
        options.outHeight = 400;
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
          Bitmap bitmapPhoto = BitmapFactory.decodeStream(new FileInputStream(photoFile), out, options);
          bitmapPhoto.compress(CompressFormat.JPEG, 90, outStream);
        } catch (FileNotFoundException e) {
        }
        String data = null;
        try {
          data = outStream.toString("Latin1");
        } catch (UnsupportedEncodingException e) {
          e.printStackTrace();
        }
        if (data != null)
          array.add(new BasicNameValuePair("photo_file_1", data));
      }

      Boolean mode = (Boolean) sharedPool.get(AppConstants.SP_MODE);
      if (mode) {
        insertAttributes(array);
      }
      JSONObject object = null;
      int count = 5;
      while (object == null && count > 0) {
        try {
          object = Downloader.getJSONObject(url, array, sessionString);
          count--;
        } catch (Exception e) {
        }
      }
      added = object != null;
      if (added) {
        Intent intent = new Intent(DoItActions.ACTION_GET_WASTE_POINTS);
        mContext.sendBroadcast(intent);
      }
      mHandler.post(afterAdd);
      mHandler.post(finishAdd);
    }

  };

  private Runnable finishAdd = new Runnable() {
    @Override
    public void run() {
      finish();
    }
  };

  private Runnable afterAdd = new Runnable() {
    @Override
    public void run() {
      if (added)
        Toast.makeText(mContext, R.string.message_added, Toast.LENGTH_SHORT).show();
      else
        Toast.makeText(mContext, R.string.message_not_added, Toast.LENGTH_SHORT).show();
      try {
        progress.dismiss();
      } catch (Exception e) {
      }
    }
  };

  private void insertAttributes(ArrayList<BasicNameValuePair> array) {
    for (FieldInfo info : map) {
      String type = info.viewName;
      if (type.contentEquals(EditText.class.getSimpleName())) {
        EditText edit = (EditText) info.view;
        array.add(new BasicNameValuePair(info.fieldName, edit.getEditableText().toString()));
      } else if (type.contentEquals(Spinner.class.getSimpleName())) {
        Spinner spinner = (Spinner) info.view;
        int i = spinner.getSelectedItemPosition();
        Set<String> set = fieldSets.get(info.fieldName);
        String value = String.valueOf((set.toArray())[i]);
        array.add(new BasicNameValuePair(info.fieldName, value));
      } else if (type.contentEquals(CheckBox.class.getSimpleName())) {
        CheckBox checkBox = (CheckBox) info.view;
        if (checkBox.isChecked())
          array.add(new BasicNameValuePair(info.fieldName, "1"));
        else
          array.add(new BasicNameValuePair(info.fieldName, "0"));
      }
    }
  }

  @Override
  protected boolean isRouteDisplayed() {
    return false;
  }

  @Override
  protected void onPause() {
    super.onPause();
    doWork = false;
    Intent intent = new Intent(DoItActions.ACTIONS_STOP_SEEK_LOCATION);
    mContext.sendBroadcast(intent);
    MainService.gone();
    // timer.cancel();
  }

  @Override
  protected void onResume() {

    super.onResume();
    MainService.startService(this);
    MainService.setLocationListener(listener);
    doWork = true;
    if (!finished) {
      Thread thread = new Thread(generateAtributes);
      thread.start();
    }
    if (lat == null || lon == null) {
      Thread thread = new Thread(getLocation);
      thread.start();
    } else {
      mHandler.post(setLocation);
    }
    validateMode();

    if (gpsLocation) {
      Intent intent = new Intent(DoItActions.ACTIONS_START_SEEK_LOCATION);
      mContext.sendBroadcast(intent);
    }

    // timer = new Timer();
    // timer.scheduleAtFixedRate(checkStatus, 100, 100);
  }

  private void validateMode() {
    Boolean mode;
    if (!sharedPool.containsKey(AppConstants.SP_MODE)) {
      mode = false;
      sharedPool.put(AppConstants.SP_MODE, mode);
    } else {
      mode = (Boolean) sharedPool.get(AppConstants.SP_MODE);
      if (mode == null) {
        mode = false;
        sharedPool.put(AppConstants.SP_MODE, mode);
      }
    }
    if (mode) {
      viewGroup.setVisibility(View.VISIBLE);
    } else {
      viewGroup.setVisibility(View.GONE);
    }
    modeButton.setChecked(mode);
  }

  private void changeMode() {
    Boolean mode = (Boolean) sharedPool.get(AppConstants.SP_MODE);
    mode = !mode;
    sharedPool.put(AppConstants.SP_MODE, mode);
    if (mode) {
      viewGroup.setVisibility(View.VISIBLE);
    } else {
      viewGroup.setVisibility(View.GONE);
    }
    modeButton.setChecked(mode);
  }

  private Runnable getLocation = new Runnable() {
    @Override
    public void run() {
      while (!sharedPool.containsKey(AppConstants.SP_LOCATION) && doWork) {
      }
      if (!doWork)
        return;
      location = (Location) sharedPool.get(AppConstants.SP_LOCATION);
      lat = location.getLatitude();
      lon = location.getLongitude();
      mHandler.post(setLocation);
    }
  };

  private Runnable setLocation = new Runnable() {
    @Override
    public void run() {
      MapController controller = mapView.getController();
      controller.animateTo(new GeoPoint((int) (lat * 1000000), (int) (lon * 1000000)));

      List<Overlay> mapOverlays = mapView.getOverlays();
      mapOverlays.clear();

      Drawable drawable = getResources().getDrawable(R.drawable.pin);
      MarkersOverlay locationOverlay = new MarkersOverlay(drawable, mContext, true);
      GeoPoint point = new GeoPoint((int) (1000000 * lat), (int) (1000000 * lon));
      OverlayItem overlayitem = new OverlayItem(point, "Your", "position");
      locationOverlay.addOverlay(overlayitem);
      mapOverlays.add(locationOverlay);
      mapView.invalidate();

      View view = findViewById(R.id.mapview_cover);
      view.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          Intent intent = new Intent(mContext, ChooseLocationActivity.class);
          intent.putExtra(DoItActions.EXTRAS_LATITUDE, lat);
          intent.putExtra(DoItActions.EXTRAS_LONGITUDE, lon);
          startActivityForResult(intent, REQUEST_CODE_MAP);
        }
      });

    }
  };

  private Runnable generateAtributes = new Runnable() {
    @Override
    public void run() {
      if (viewGroup == null)
        return;
      LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      layout = new LinearLayout(mContext);
      layout.setOrientation(LinearLayout.VERTICAL);
      while (!sharedPool.containsKey(AppConstants.SP_JSON_ARRAY_EXTRA_FIELDS) && doWork) {
      }
      if (!doWork)
        return;
      JSONArray array = (JSONArray) sharedPool.get(AppConstants.SP_JSON_ARRAY_EXTRA_FIELDS);
      int size = array.length();
      JSONObject object = null;
      View item = null;
      for (int i = 0; i < size; i++) {
        try {
          object = array.getJSONObject(i);
          String type = object.getString(AppConstants.TYPE);
          boolean hasAllowedValues = object.has(AppConstants.ALLOWED_VALUES);
          boolean hasTypicalValues = object.has(AppConstants.TYPICAL_VALUES);

          if (AppConstants.TYPE_TEXT.contentEquals(type)) {
            if (hasAllowedValues || hasTypicalValues) {
              item = generateSpinnerInput(inflater, object, hasAllowedValues);
            } else {
              item = generateTextInput(inflater, object);
            }
            layout.addView(item);
          } else if (AppConstants.TYPE_FLOAT.contentEquals(type)) {
            if (hasAllowedValues || hasTypicalValues) {
              item = generateSpinnerInput(inflater, object, hasAllowedValues);
            } else {
              item = generateFloatInput(inflater, object);
            }
            layout.addView(item);
          } else if (AppConstants.TYPE_INTEGER.contentEquals(type)) {
            if (hasAllowedValues || hasTypicalValues) {
              item = generateSpinnerInput(inflater, object, hasAllowedValues);
            } else {
              item = generateIntegerInput(inflater, object);
            }
            layout.addView(item);
          } else if (AppConstants.TYPE_BOOLEAN.contentEquals(type)) {
            item = generateBooleanInput(inflater, object);
            layout.addView(item);
          }
        } catch (Exception e) {
        }
      }
      if (doWork) {
        mHandler.post(invalidateAtributes);
        finished = true;
      }
    }
  };

  private View generateSpinnerInput(LayoutInflater inflater, JSONObject object, boolean isAllowed) {
    View view = null;
    try {
      view = inflater.inflate(R.xml.text_spinner, null);
      String label = makeLabel(object);
      TextView text = (TextView) view.findViewById(R.id.label);
      text.setText(label);
      Spinner edit = (Spinner) view.findViewById(R.id.edit);
      label = object.getString(AppConstants.FIELD_NAME);
      JSONArray array = null;
      if (isAllowed) {
        array = object.getJSONArray(AppConstants.ALLOWED_VALUES);
      } else {
        array = object.getJSONArray(AppConstants.TYPICAL_VALUES);
      }
      LinkedHashMap<String, String> hashMap = genHashMap(array);
      String[] values = new String[hashMap.size()];
      values = hashMap.values().toArray(values);
      ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, values);
      adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      edit.setAdapter(adapter);

      if (label != null && label.length() != 0) {
        FieldInfo info = new FieldInfo();
        info.fieldName = label;
        info.view = edit;
        info.viewName = edit.getClass().getSimpleName();
        map.add(info);
        fieldSets.put(label, hashMap.keySet());
      }

    } catch (Exception e) {
      return null;
    }
    return view;
  }

  private LinkedHashMap<String, String> genHashMap(JSONArray array) throws JSONException {
    int size = array.length();
    LinkedHashMap<String, String> hashMap = new LinkedHashMap<String, String>();
    JSONArray miniArray;
    for (int i = 0; i < size; i++) {
      miniArray = array.getJSONArray(i);
      hashMap.put(String.valueOf(miniArray.get(0)), String.valueOf(miniArray.get(1)));
    }
    return hashMap;
  }

  private View generateTextInput(LayoutInflater inflater, JSONObject object) {
    View view = null;
    try {
      view = inflater.inflate(R.xml.text_edit, null);
      String label = makeLabel(object);
      TextView text = (TextView) view.findViewById(R.id.label);
      text.setText(label);
      EditText edit = (EditText) view.findViewById(R.id.edit);
      label = object.getString(AppConstants.FIELD_NAME);
      if (label != null && label.length() != 0) {
        FieldInfo info = new FieldInfo();
        info.fieldName = label;
        info.view = edit;
        info.viewName = edit.getClass().getSimpleName();
        map.add(info);
      }

    } catch (Exception e) {
      return null;
    }
    return view;
  }

  private View generateIntegerInput(LayoutInflater inflater, JSONObject object) {
    View view = null;
    try {
      view = inflater.inflate(R.xml.text_edit, null);
      String label = makeLabel(object);
      TextView text = (TextView) view.findViewById(R.id.label);
      text.setText(label);

      EditText edit = (EditText) view.findViewById(R.id.edit);
      edit.setInputType(InputType.TYPE_CLASS_NUMBER);
      label = object.getString(AppConstants.FIELD_NAME);
      if (label != null && label.length() != 0) {
        FieldInfo info = new FieldInfo();
        info.fieldName = label;
        info.view = edit;
        info.viewName = edit.getClass().getSimpleName();
        map.add(info);
      }

    } catch (Exception e) {
      return null;
    }
    return view;
  }

  private View generateBooleanInput(LayoutInflater inflater, JSONObject object) {
    View view = null;
    try {
      view = inflater.inflate(R.xml.check, null);
      String label = makeLabel(object);
      CheckBox edit = (CheckBox) view.findViewById(R.id.checkbox);
      edit.setText(label);
      label = object.getString(AppConstants.FIELD_NAME);
      if (label != null && label.length() != 0) {
        FieldInfo info = new FieldInfo();
        info.fieldName = label;
        info.view = edit;
        info.viewName = edit.getClass().getSimpleName();
        map.add(info);
      }

    } catch (Exception e) {
      return null;
    }
    return view;
  }

  private View generateFloatInput(LayoutInflater inflater, JSONObject object) {
    View view = null;
    try {
      view = inflater.inflate(R.xml.text_edit, null);
      String label = makeLabel(object);
      TextView text = (TextView) view.findViewById(R.id.label);
      text.setText(label);

      EditText edit = (EditText) view.findViewById(R.id.edit);
      edit.setInputType(InputType.TYPE_CLASS_NUMBER);
      label = object.getString(AppConstants.FIELD_NAME);
      if (label != null && label.length() != 0) {
        FieldInfo info = new FieldInfo();
        info.fieldName = label;
        info.view = edit;
        info.viewName = edit.getClass().getSimpleName();
        map.add(info);
      }

    } catch (Exception e) {
      return null;
    }
    return view;
  }

  private String makeLabel(JSONObject object) {
    String label;
    try {
      label = object.getString(AppConstants.LABEL);
    } catch (Exception e) {
      label = "Unknown";
    }
    try {
      String suffix = object.getString(AppConstants.SUFFIX);
      if (suffix != null && suffix.length() != 0)
        label = label + " (" + suffix + ")";
    } catch (Exception e) {
    }
    return label;
  }

  private Runnable invalidateAtributes = new Runnable() {
    @Override
    public void run() {
      viewGroup.addView(layout);
      viewGroup.invalidate();
    }
  };

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CODE_MAP) {
      if (resultCode == ChooseLocationActivity.OK) {
        if (data != null) {
          Bundle bundle = data.getExtras();
          if (bundle.containsKey(DoItActions.EXTRAS_LATITUDE) && bundle.containsKey(DoItActions.EXTRAS_LONGITUDE)) {
            try {
              gpsLocation = false;
              lat = bundle.getDouble(DoItActions.EXTRAS_LATITUDE);
              lon = bundle.getDouble(DoItActions.EXTRAS_LONGITUDE);
              mHandler.post(setLocation);
              Intent intent = new Intent(DoItActions.ACTIONS_STOP_SEEK_LOCATION);
              mContext.sendBroadcast(intent);
            } catch (Exception e) {
            }
          }
        }
      }
    } else if (requestCode == REQUEST_CODE_CAMERA) {
      if (resultCode == Activity.RESULT_OK) {
        try {
          File photo = convertImageUriToFile(imageUri, this);
          if (photo != null)
            photoFile = photo;
          else
            photo = photoFile;
          ImageView imageView = (ImageView) findViewById(R.id.photo);
          Rect out = new Rect();
          BitmapFactory.Options options = new BitmapFactory.Options();
          options.inSampleSize = 12;
          Bitmap bitmapPhoto = BitmapFactory.decodeStream(new FileInputStream(photo), out, options);

          imageView.setImageBitmap(bitmapPhoto);

          switcher.showNext();
          switcher.invalidate();

        } catch (Exception e) {
        }

      }
    }
    afterResult = true;
    super.onActivityResult(requestCode, resultCode, data);
  }

  public static File convertImageUriToFile(Uri imageUri, Activity activity) {
    Cursor cursor = null;
    try {
      String[] proj = { MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID, MediaStore.Images.ImageColumns.ORIENTATION };
      cursor = activity.managedQuery(imageUri, proj, null, null, null);
      int file_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
      int orientation_ColumnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION);
      if (cursor.moveToFirst()) {
        // String orientation =
        cursor.getString(orientation_ColumnIndex);
        return new File(cursor.getString(file_ColumnIndex));
      }
      return null;
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
  }

  private IMEListener changeVisibility = new IMEListener() {
    @Override
    public void onChange(boolean isShown) {
      if (isShown && !afterResult) {
        sendData.setVisibility(View.GONE);
        mapView.setVisibility(View.GONE);
        modeButton.setVisibility(View.GONE);
        switcher.setVisibility(View.GONE);
      } else {
        sendData.setVisibility(View.VISIBLE);
        mapView.setVisibility(View.VISIBLE);
        modeButton.setVisibility(View.VISIBLE);
        switcher.setVisibility(View.VISIBLE);
        afterResult = false;
      }
    }
  };

  private LocationListener listener = new LocationListener() {
    @Override
    public void onLocationChanged(Location locat) {
      if (gpsLocation) {
        if (!location.hasAccuracy() && locat.hasAccuracy())
          location = locat;
        else if (location.hasAccuracy() && locat.hasAccuracy()) {
          if (location.getAccuracy() < locat.getAccuracy()) {
            location = locat;
          }
        }
        lat = location.getLatitude();
        lon = location.getLongitude();
        mHandler.post(setLocation);
      } else {
        Intent intent = new Intent(DoItActions.ACTIONS_STOP_SEEK_LOCATION);
        mContext.sendBroadcast(intent);
      }
    }
  };

  private void createExitDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
    builder.setTitle(R.string.newpoint_exit_title);
    builder.setMessage(R.string.newpoint_exit_msg);
    builder.setPositiveButton(R.string.map_dialog_yes, new AlertDialog.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        finish();
      }
    });
    builder.setNeutralButton(R.string.map_dialog_no, null);
    builder.setCancelable(true);
    builder.setOnCancelListener(new OnCancelListener() {
      @Override
      public void onCancel(DialogInterface dialog) {
        finish();
      }
    });
    builder.show();
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_BACK) {
      createExitDialog();
      return true;
    } else
      return super.onKeyDown(keyCode, event);
  }

  private boolean validateData() {
    EditText edit;
    if ((Boolean) sharedPool.get(AppConstants.SP_MODE)) {
      for (FieldInfo info : map) {
        if (info.viewName.contentEquals(EditText.class.getSimpleName())) {
          edit = (EditText) info.view;
          try {
            String text = edit.getEditableText().toString();
            if (text == null || text.length() == 0) {
              Toast.makeText(mContext, "Not all field are filled! It is required in ADVANCED mode", Toast.LENGTH_SHORT).show();
              return false;
            }
          } catch (Exception e) {
            return false;
          }
        }
      }
    }
    if (photoFile == null) {
      Toast.makeText(mContext, "Photo is required!", Toast.LENGTH_SHORT).show();
      return false;
    }
    return true;
  }

}
