package com.ito.doit;

import android.location.Location;

public interface LocationListener {
  public void onLocationChanged(Location location);
  public void gotGpsSignal(boolean isLocked);
}
