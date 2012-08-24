package com.letsdoitworld.wastemapper.utils;

public class AppConstants {

  public static final String SP_DISTANCES = "distances";
  public static final String SP_LOCATION = "location";
  public static final String SP_POINTS_ARRAY = "pointsArray";
  public static final String SP_POINTS_LATITUDE = "pointsLatitude";
  public static final String SP_POINTS_LONGITUDE = "pointsLongitude";
  public static final String SP_POINTS_IDS = "pointsIds";
  public static final String SP_POINT_ID = "pointId_";
  public static final String SP_JSON_ARRAY_EXTRA_FIELDS = "JSONArrayExtraFields";
  public static final String SP_JSON_LOGIN = "JSONLogin";
  public static final String SP_MODE = "AppMode";
  public static final String SP_NEAREST_FEATURE = "isNearestTo";
  public static final String SP_BBOX_VALUE = "BBoxValue";
  public static final String SP_MAX_RESULTS_VALUE = "MaxResultsValue";
  public static final String SP_USERNAME = "loggedUsername";
  public static final String SP_LANGUAGE = "language";

  public static final String ADDRESS_JSON_EXTRA_FIELDS = "/waste-point-extra-fields.json";
  public static final String ADDRESS_JSON_LOGIN = "/user/login.json";
  public static final String ADDRESS_WASTE_POINT_MAXRES = "/waste_points.csv&max_results=";
  public static final String ADDRESS_WASTE_POINTS_BBOX = "&BBOX=";
  public static final String ADDRESS_WASTE_POINTS_NEAREST = "&nearest_points_to=";
  public static final String ADDRESS_LANGUAGE = "&language_code=";

  public static final String ADDRESS_PUT_WASTE_POINT = "/wp.json";
  public static final String ADDRESS_WASTE_POINT_DETAILS = "/wp/";

  public static final String TYPE_TEXT = "text";
  public static final String TYPE_FLOAT = "float";
  public static final String TYPE_INTEGER = "integer";
  public static final String TYPE_BOOLEAN = "boolean";

  public static final String ALLOWED_VALUES = "allowed_values";
  public static final String TYPICAL_VALUES = "typical_values";
  public static final String FIELD_NAME = "field_name";
  public static final String TYPE = "type";
  public static final String LABEL = "label";
  public static final String SUFFIX = "suffix";

  public static final int DEFAULT_BBOX_VALUE = 2;
  public static final int DEFAULT_MAX_RESULTS_VALUE = 10;
  public static final String DEFAULT_LANGUAGE = "en";

  public static final String SHARED_USERNAME = "username";
  public static final String SHARED_PASSWORD = "password";
  public static final String SHARED_UPTIME = "upTime";

  public static volatile boolean isLogged = false;

}
