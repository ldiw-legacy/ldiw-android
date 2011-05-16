package com.ito.doit;

import java.util.Timer;
import java.util.TimerTask;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

public class MyLocation {
  Timer timer1;
  LocationManager lm;
  LocationResult locationResult;
  private boolean gps_enabled = false;
  private boolean network_enabled = false;
  private boolean continueRefresh = false;

  public void setRefreshing(boolean refresh) {
    continueRefresh = refresh;
  }

  public boolean getLocation(Context context, LocationResult result) {
    // I use LocationResult callback class to pass location value from
    // MyLocation to user code.
    locationResult = result;
    if (lm == null)
      lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

    // exceptions will be thrown if provider is not permitted.
    try {
      gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    } catch (Exception ex) {
    }
    try {
      network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    } catch (Exception ex) {
    }

    // don't start listeners if no provider is enabled
    if (!gps_enabled && !network_enabled) {
      result.gotLocation(null);
      return false;
    }

    if (gps_enabled)
      lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListenerGps);
    else if (network_enabled)
      lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListenerNetwork);

    timer1 = new Timer();
    timer1.schedule(new GetLastLocation(), 20000);

    return true;
  }

  public boolean getLocationFast(Context context, LocationResult result) {
    locationResult = result;
    if (lm == null)
      lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

    // exceptions will be thrown if provider is not permitted.
    try {
      gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    } catch (Exception ex) {
    }
    try {
      network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    } catch (Exception ex) {
    }

    // don't start listeners if no provider is enabled
    if (!gps_enabled && !network_enabled) {
      result.gotLocation(null);
      return false;
    }

    Location net_loc = null, gps_loc = null;
    if (gps_enabled)
      gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    if (network_enabled)
      net_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

    // if there are both values use the latest one
    if (gps_loc != null && net_loc != null) {
      if (gps_loc.getTime() > net_loc.getTime())
        locationResult.gotLocation(gps_loc);
      else
        locationResult.gotLocation(net_loc);
      return true;
    }

    if (gps_loc != null) {
      locationResult.gotLocation(gps_loc);
      return true;
    }
    if (net_loc != null) {
      locationResult.gotLocation(net_loc);
      return true;
    }
    locationResult.gotLocation(null);
    return false;
  }

  public boolean isLocatingEnabled() {
    try {
      gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    } catch (Exception ex) {
    }
    try {
      network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    } catch (Exception ex) {
    }
    return gps_enabled || network_enabled;
  }

  LocationListener locationListenerGps = new LocationListener() {
    public void onLocationChanged(Location location) {
      timer1.cancel();
      if (!continueRefresh) {
        lm.removeUpdates(this);
        lm.removeUpdates(locationListenerNetwork);
      }
      locationResult.gotLocation(location);
    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
  };

  LocationListener locationListenerNetwork = new LocationListener() {
    public void onLocationChanged(Location location) {
      timer1.cancel();
      if (!continueRefresh) {
        lm.removeUpdates(this);
        lm.removeUpdates(locationListenerGps);
      }
      locationResult.gotLocation(location);
    }

    public void onProviderDisabled(String provider) {
    }

    public void onProviderEnabled(String provider) {
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
  };

  class GetLastLocation extends TimerTask {
    @Override
    public void run() {
      if (!continueRefresh) {
        lm.removeUpdates(locationListenerGps);
        lm.removeUpdates(locationListenerNetwork);
      }
      Location net_loc = null, gps_loc = null;
      if (gps_enabled)
        gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
      if (network_enabled)
        net_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

      // if there are both values use the latest one
      if (gps_loc != null && net_loc != null) {
        if (gps_loc.getTime() > net_loc.getTime())
          locationResult.gotLocation(gps_loc);
        else
          locationResult.gotLocation(net_loc);
        return;
      }

      if (gps_loc != null) {
        locationResult.gotLocation(gps_loc);
        return;
      }
      if (net_loc != null) {
        locationResult.gotLocation(net_loc);
        return;
      }
      locationResult.gotLocation(null);
    }
  }

  public static abstract class LocationResult {
    public abstract void gotLocation(Location location);
  }

  public void stopLocating() {
    try {
      lm.removeUpdates(locationListenerGps);
      lm.removeUpdates(locationListenerNetwork);
    } catch (Exception e) {
    }
  }
}