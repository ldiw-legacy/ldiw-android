package com.letsdoitworld.wastemapper.utils;

public class DoItActions {

  /**
   * Action to send to service to get all waste points
   */
  public static final String ACTION_GET_WASTE_POINTS = "com.letsdoitworld.wastemapper.get.waste.points";

  /**
   * Action to send back waste for listener
   */
  public static final String ACTION_RETURN_WASTE_POINTS = "com.letsdoitworld.wastemapper.return.waste.points";

  /**
   * Action to get geo location
   */
  public static final String ACTION_GET_LOCATION = "com.letsdoitworld.wastemapper.get.location";

  /**
   * Action to return geo location
   */
  public static final String ACTION_RETURN_LOCATION = "com.letsdoitworld.wastemapper.return.location";

  /**
   * Extras for latitude
   */
  public static final String EXTRAS_LATITUDE = "com.letsdoitworld.wastemapper.extras.latitude";

  /**
   * Extras for longitude
   */
  public static final String EXTRAS_LONGITUDE = "com.letsdoitworld.wastemapper.extras.longitude";

  /**
   * Extras for latitude
   */
  public static final String EXTRAS_LATITUDES_ARRAY = "com.letsdoitworld.wastemapper.extras.latitudes.array";

  /**
   * Extras for longitude
   */
  public static final String EXTRAS_LONGITUDES_ARRAY = "com.letsdoitworld.wastemapper.extras.longitudes.array";

  /**
   * Extras for waste points ids
   */
  public static final String EXTRAS_IDS_ARRAY = "com.letsdoitworld.wastemapper.extras.ids.array";

  /**
   * Action to get distances array
   */
  public static final String ACTION_GET_DISTANCES = "com.letsdoitworld.wastemapper.action.get.distances";

  /**
   * Action to return distances array
   */
  public static final String ACTION_RETURN_DISTANCES = "com.letsdoitworld.wastemapper.action.return.distances";

  /**
   * Extras for distances array
   */
  public static final String EXTRAS_DISTANCES_ARRAY = "com.letsdoitworld.wastemapper.extras.ids.array";

  /**
   * Extras for name of VarInfo
   */
  public static final String EXTRAS_NAME = "com.letsdoitworld.wastemapper.extras.name";

  /**
   * Extras for type of VarInfo
   */
  public static final String EXTRAS_TYPE = "com.letsdoitworld.wastemapper.extras.type";

  public static final String ACTIONS_REFRESH_ALL = "com.letsdoitworld.wastemapper.action.refresh.all";

  public static final String ACTION_REFRESH_DISTANCES = "com.letsdoitworld.wastemapper.action.refresh.distances";

  public static final String ACTION_GET_POINT_INFO = "com.letsdoitworld.wastemapper.action.get.point.info";

  public static final String EXTRAS_POINT_ID = "com.letsdoitworld.wastemapper.extras.point.id";

  public static final String ACTIONS_START_SEEK_LOCATION = "com.letsdoitworld.wastemapper.actions.start.seek.location";

  public static final String ACTIONS_STOP_SEEK_LOCATION = "com.letsdoitworld.wastemapper.actions.stop.seek.location";

}
