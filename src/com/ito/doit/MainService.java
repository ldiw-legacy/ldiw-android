package com.ito.doit;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import org.json.JSONArray;
import org.json.JSONObject;
import com.ito.doit.MyLocation.LocationResult;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class MainService extends Service {
  public static final String TAG = "DoItTag";
  private static final String URL_BASIC_ADDRESS = "http://api.letsdoitworld.org/?q=get-api-base-url.json";
  public static final String API_BASE_URL_KEY = "api_base_url";
  public static final String SHARED_PREFS = "com.ito.doit.shared.preferences";
  public static final String USERNAME = "username";
  public static final String PASSWORD = "password";

  public static final int POINTS_DATA = 1;
  public static final int DISTANCES = 2;

  private static boolean isWorking = false;
  private static int numberOfUsers = 0;
  private static Timer timer;
  private static MainService service;
  private static LocationListener locationListener;
  private static OnDataListener dataListener;

  public static synchronized void setDataListener(OnDataListener dataListener) {
    MainService.dataListener = dataListener;
  }

  private static Context mContext;
  private boolean refreshAll;
  private SharedPool sharedPool;
  private CommandReceiver receiver;
  private MyLocation locationHelper;
  private Location location;
  private Location startingLocation;
  private static ArrayList<HashMap<String, String>> pointsData;
  private JSONObject object;
  private SharedPreferences sharedPrefs;
  private Handler mHandler;

  private LocationResult locationResult = new LocationResult() {
    @Override
    public void gotLocation(final Location locat) {
      if (locat == null) {
        checkLocation();
      } else {
        location = locat;
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putFloat("Lat", (float) locat.getLatitude());
        editor.putFloat("Lon", (float) locat.getLongitude());
        editor.commit();
      }

      if (locationListener != null)
        locationListener.onLocationChanged(location);
      sharedPool.put(AppConstants.SP_LOCATION, location);
      Log.i(TAG, "Location is available");
      Intent intent = new Intent(DoItActions.ACTION_RETURN_LOCATION);
      intent.putExtra(DoItActions.EXTRAS_LATITUDE, location.getLatitude());
      intent.putExtra(DoItActions.EXTRAS_LONGITUDE, location.getLongitude());
      intent.putExtra(DoItActions.EXTRAS_NAME, AppConstants.SP_LOCATION);
      sendBroadcast(intent);

      if (startingLocation == null) {
        startingLocation = location;
      } else if (startingLocation.distanceTo(location) > 5000) {
        refreshAll = true;
      }

      if (refreshAll) {
        Thread thread = new Thread(getDistances);
        thread.start();
        refreshAll = false;
      }

    }
  };

  @Override
  public IBinder onBind(Intent arg0) {
    return null;
  }

  @Override
  public void onCreate() {
    if (!isWorking) {
      mContext = this;
      mHandler = new Handler();
      isWorking = true;
      service = this;
      locationHelper = new MyLocation();
      location = null;
      startingLocation = null;
      pointsData = null;
      object = null;

      sharedPool = SharedPool.getInstance();
      sharedPrefs = getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
      receiver = new CommandReceiver();
      IntentFilter intentFilter = new IntentFilter();
      intentFilter.addAction(DoItActions.ACTION_GET_WASTE_POINTS);
      intentFilter.addAction(DoItActions.ACTION_GET_LOCATION);
      intentFilter.addAction(DoItActions.ACTION_GET_DISTANCES);
      intentFilter.addAction(DoItActions.ACTIONS_REFRESH_ALL);
      intentFilter.addAction(DoItActions.ACTION_REFRESH_DISTANCES);
      intentFilter.addAction(DoItActions.ACTIONS_START_SEEK_LOCATION);
      intentFilter.addAction(DoItActions.ACTIONS_STOP_SEEK_LOCATION);
      registerReceiver(receiver, intentFilter);
      refreshAll = true;
      locationHelper.getLocationFast(getApplicationContext(), locationResult);
      if (!locationHelper.isLocatingEnabled()) {
        Thread thread = new Thread(waitForLocating);
        thread.start();
      }

    }
    init();
    Log.i(TAG, "Created");
    super.onCreate();
  }

  private Runnable waitForLocating = new Runnable() {
    @Override
    public void run() {
      while (!locationHelper.isLocatingEnabled() && isWorking) {
      }
      if (!isWorking)
        return;
      locationHelper.getLocationFast(getApplicationContext(), locationResult);
    }
  };

  @Override
  public void onStart(Intent intent, int startId) {
    super.onStart(intent, startId);
    Log.i(TAG, "onStart");
  }

  public int onStartCommand(Intent intent, int flags, int startId) {
    Log.i(TAG, "onStartCommand");
    return 1;
  };

  public void init() {
    Log.i(TAG, "Initiation of main service");
  }

  @Override
  public void onDestroy() {
    isWorking = false;
    Log.i(TAG, "Service is quiting");
    try {
      timer.cancel();
      timer = null;
    } catch (Exception e) {
    }
    super.onDestroy();
  }

  /**
   * Use this method to start service
   */
  synchronized public static void startService(Context context) {
    if (!isWorking) {
      Log.i(TAG, "Creating new service");
      Intent service = new Intent(context, MainService.class);
      context.startService(service);
    }
    numberOfUsers++;
    if (numberOfUsers > 0 && timer != null) {
      try {
        timer.cancel();
        timer = null;
      } catch (Exception e) {
      }
    }
  }

  public static boolean isWorking() {
    return isWorking;
  }

  private class CommandReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
      final String action = intent.getAction();
      if (DoItActions.ACTION_GET_WASTE_POINTS.contentEquals(action)) {
        pointsData = null;
        sharedPool.remove(AppConstants.SP_POINTS_LATITUDE);
        sharedPool.remove(AppConstants.SP_DISTANCES);
        sharedPool.remove(AppConstants.SP_POINTS_IDS);
        sharedPool.remove(AppConstants.SP_POINTS_LONGITUDE);
        sharedPool.remove(AppConstants.SP_POINTS_ARRAY);
        Thread thread = new Thread(getDistances);
        thread.start();
      } else if (DoItActions.ACTION_GET_LOCATION.contentEquals(action)) {
        sharedPool.remove(AppConstants.SP_LOCATION);
        locationHelper.getLocation(getApplicationContext(), locationResult);
      } else if (DoItActions.ACTION_GET_DISTANCES.contentEquals(action)) {
        sharedPool.remove(AppConstants.SP_DISTANCES);
        Thread thread = new Thread(getDistances);
        thread.start();
      } else if (DoItActions.ACTIONS_REFRESH_ALL.contentEquals(action)) {
        refreshAll = true;
        sharedPool.remove(AppConstants.SP_DISTANCES);
        sharedPool.remove(AppConstants.SP_LOCATION);
        locationHelper.getLocation(getApplicationContext(), locationResult);
      } else if (DoItActions.ACTIONS_START_SEEK_LOCATION.contentEquals(action)) {
        locationHelper.setRefreshing(true);
        locationHelper.getLocation(getApplicationContext(), locationResult);
      } else if (DoItActions.ACTIONS_STOP_SEEK_LOCATION.contentEquals(action)) {
        locationHelper.setRefreshing(false);
        locationHelper.stopLocating();
      }

    }
  }

  private Runnable getObjectNet = new Runnable() {
    @Override
    public void run() {
      checkLocation();
      int count = 0;
      boolean showed = false;
      boolean repeat = true;
      String url = "";
      while (repeat && isWorking) {

        repeat = !getObject();
        count++;
        if (count == 5 && numberOfUsers > 0) {
          if (!showed) {
            mHandler.post(makeToast);
            showed = true;
          }
          count = 0;
        }
        if (repeat)
          continue;
        try {
          url = (String) object.get(API_BASE_URL_KEY);
          if (url != null && url.length() > 0) {
            sharedPool.put(API_BASE_URL_KEY, url);
            Thread thread = new Thread(getExtraJSON);
            thread.start();
          } else {
            repeat = true;
          }
        } catch (Exception e) {
          repeat = true;
        }
      }
      if (!isWorking)
        return;
      String pointUrl;
      try {
        int max = (Integer) sharedPool.get(AppConstants.SP_MAX_RESULTS_VALUE);
        pointUrl = url + AppConstants.ADDRESS_WASTE_POINT_MAXRES + max + AppConstants.ADDRESS_WASTE_POINTS_NEAREST + location.getLongitude() + "," + location.getLatitude();
        while (pointsData == null) {
          try {
            InputStream is = Downloader.connect(pointUrl, null);
            pointsData = Downloader.decodeCSV(is);
          } catch (Exception e) {
            pointsData = null;
          }
        }

        double[] distances = mapMinerDouble(pointsData, "distance_meters");
        sharedPool.put(AppConstants.SP_POINTS_LATITUDE, mapMinerDouble(pointsData, "lat"));
        sharedPool.put(AppConstants.SP_POINTS_LONGITUDE, mapMinerDouble(pointsData, "lon"));
        sharedPool.put(AppConstants.SP_POINTS_IDS, mapMinerString(pointsData, "id"));
        sharedPool.put(AppConstants.SP_DISTANCES, distances);
        sharedPool.put(AppConstants.SP_NEAREST_FEATURE, true);
        sharedPool.put(AppConstants.SP_POINTS_ARRAY, pointsData);
        if (dataListener != null)
          dataListener.onData(POINTS_DATA, pointsData);
        if (dataListener != null)
          dataListener.onData(DISTANCES, distances);
      } catch (Exception e) {
        while (pointsData == null) {
          try {
            int max = (Integer) sharedPool.get(AppConstants.SP_MAX_RESULTS_VALUE);
            int bbox = (Integer) sharedPool.get(AppConstants.SP_BBOX_VALUE);
            pointUrl = url + AppConstants.ADDRESS_WASTE_POINT_MAXRES + max + AppConstants.ADDRESS_WASTE_POINTS_BBOX + (location.getLongitude() - bbox) + ","
                + (location.getLatitude() - (2 * bbox) / 3) + "," + (location.getLongitude() + bbox) + "," + (location.getLatitude() + (2 * bbox) / 3);
            InputStream is = Downloader.connect(pointUrl, null);
            pointsData = Downloader.decodeCSV(is);
          } catch (Exception ex) {
            pointsData = null;
          }
        }
        sharedPool.put(AppConstants.SP_POINTS_LATITUDE, mapMinerDouble(pointsData, "lat"));
        sharedPool.put(AppConstants.SP_POINTS_LONGITUDE, mapMinerDouble(pointsData, "lon"));
        sharedPool.put(AppConstants.SP_POINTS_IDS, mapMinerString(pointsData, "id"));
        sharedPool.put(AppConstants.SP_NEAREST_FEATURE, false);
        sharedPool.put(AppConstants.SP_POINTS_ARRAY, pointsData);
        if (dataListener != null)
          dataListener.onData(POINTS_DATA, pointsData);
      }

      Log.i(TAG, "Points data is available");
    }
  };

  private Runnable makeToast = new Runnable() {
    public void run() {
      Toast.makeText(mContext, R.string.text_no_connection, Toast.LENGTH_SHORT).show();
    }
  };

  private Runnable getExtraJSON = new Runnable() {
    @Override
    public void run() {
      while (!sharedPool.containsKey(API_BASE_URL_KEY) && isWorking) {
      }
      if (!isWorking)
        return;

      String url = (String) sharedPool.get(API_BASE_URL_KEY);
      url = url + AppConstants.ADDRESS_JSON_EXTRA_FIELDS + AppConstants.ADDRESS_LANGUAGE + (String) sharedPool.get(AppConstants.SP_LANGUAGE);
      JSONArray extraFields = Downloader.getJSONArray(url);
      while (extraFields == null && isWorking) {
        extraFields = Downloader.getJSONArray(url);
      }
      if (!isWorking)
        return;
      sharedPool.put(AppConstants.SP_JSON_ARRAY_EXTRA_FIELDS, extraFields);
    }
  };

  public static double[] mapMinerDouble(ArrayList<HashMap<String, String>> data, String field) {
    ArrayList<Double> array = new ArrayList<Double>();
    for (HashMap<String, String> map : data) {
      array.add(Double.parseDouble(map.get(field)));
    }
    double[] t = new double[array.size()];
    for (int i = 0; i < array.size(); i++) {
      t[i] = array.get(i);
    }
    return t;
  }

  public static String[] mapMinerString(ArrayList<HashMap<String, String>> data, String field) {
    ArrayList<String> array = new ArrayList<String>();
    for (HashMap<String, String> map : data) {
      array.add(map.get(field));
    }
    String[] t = new String[array.size()];
    return array.toArray(t);
  }

  private Runnable getDistances = new Runnable() {
    public void run() {
      try {
        if (pointsData == null) {
          Thread thread = new Thread(getObjectNet);
          thread.start();
          while (thread.isAlive()) {
          }
          ;
          if (sharedPool.containsKey(AppConstants.SP_NEAREST_FEATURE)) {
            if ((Boolean) sharedPool.get(AppConstants.SP_NEAREST_FEATURE)) {
              return;
            }
          }
        }

        double[] lat = (double[]) sharedPool.get(AppConstants.SP_POINTS_LATITUDE);
        double[] lon = (double[]) sharedPool.get(AppConstants.SP_POINTS_LONGITUDE);
        int size = lat.length;
        double[] distances = new double[size];
        Location wp = new Location(location);
        for (int i = 0; i < size; i++) {
          wp.setLatitude(lat[i]);
          wp.setLongitude(lon[i]);
          distances[i] = (double) location.distanceTo(wp);
        }
        distances = sortByDistances(distances);
        sharedPool.put(AppConstants.SP_POINTS_LATITUDE, mapMinerDouble(pointsData, "lat"));
        sharedPool.put(AppConstants.SP_POINTS_LONGITUDE, mapMinerDouble(pointsData, "lon"));
        sharedPool.put(AppConstants.SP_POINTS_IDS, mapMinerString(pointsData, "id"));
        sharedPool.put(AppConstants.SP_POINTS_ARRAY, pointsData);
        sharedPool.put(AppConstants.SP_DISTANCES, distances);
        if (dataListener != null)
          dataListener.onData(DISTANCES, distances);
      } catch (Exception e) {
        pointsData = null;
      }

    }
  };

  private void checkLocation() {
    if (location == null) {
      location = new Location("Something");
      location.setLatitude(sharedPrefs.getFloat("Lat", 56.12345f));
      location.setLongitude(sharedPrefs.getFloat("Lon", 34.543222f));
    }
  }

  private double[] sortByDistances(double[] distances) {
    int end = distances.length - 1;
    double min, max, data;
    int mini, maxi;
    HashMap<String, String> data2;
    for (int start = 0; start <= end; start++, end--) {
      min = distances[start];
      max = distances[start];
      mini = start;
      maxi = start;
      for (int i = start + 1; i <= end; i++) {
        if (min > distances[i]) {
          mini = i;
          min = distances[i];
        } else if (max < distances[i]) {
          maxi = i;
          max = distances[i];
        }
      }
      if (mini == maxi)
        continue;
      if (mini != start) {
        data = distances[start];
        distances[start] = distances[mini];
        distances[mini] = data;

        data2 = pointsData.get(start);
        pointsData.remove(start);
        pointsData.add(start, pointsData.get(mini - 1));
        pointsData.remove(mini);
        pointsData.add(mini, data2);
      }
      if (maxi == start)
        maxi = mini;
      if (maxi != end) {
        data = distances[end];
        distances[end] = distances[maxi];
        distances[maxi] = data;

        data2 = pointsData.get(end);
        pointsData.remove(end);
        pointsData.add(end, pointsData.get(maxi));
        pointsData.remove(maxi);
        pointsData.add(maxi, data2);
      }
    }
    return distances;
  }

  synchronized public static void gone() {
    numberOfUsers--;
    if (numberOfUsers == 0) {
      if (timer == null)
        timer = new Timer();
      // Stop service after 5 minutes after exit
      timer.schedule(new ExitTask(), 180000);

    }
  }

  private static class ExitTask extends TimerTask {

    @Override
    public void run() {
      isWorking = false;
      try {
        Intent intent = new Intent(mContext, MainService.class);
        while (mContext.stopService(intent))
          ;
      } catch (Exception e) {
      }
    }
  };

  public static void setLocationListener(LocationListener listener) {
    locationListener = listener;
  }

  private boolean getObject() {
    object = Downloader.getJSONObject(URL_BASIC_ADDRESS, location.getLatitude(), location.getLongitude());
    return object != null;
  }

  public static void saveUserPass(Context context, String user, String pass) {
    SharedPreferences prefences = context.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
    SharedPreferences.Editor editor = prefences.edit();
    editor.putString(AppConstants.SHARED_USERNAME, user);
    editor.putString(AppConstants.SHARED_PASSWORD, pass);
    int timeStamp = genTimeStamp();
    editor.putInt(AppConstants.SHARED_UPTIME, timeStamp);
    editor.commit();
  }

  public static int genTimeStamp() {
    Calendar calendar = Calendar.getInstance();
    return calendar.get(Calendar.YEAR) * 10000 + calendar.get(Calendar.DAY_OF_MONTH) + calendar.get(Calendar.MONTH) * 100;
  }

}
