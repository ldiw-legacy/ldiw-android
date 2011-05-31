package com.letsdoitworld.wastemapper;

import java.util.ArrayList;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.letsdoitworld.wastemapper.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;

public class OptionsActivity extends Activity {

  private Handler mHandler;
  private Activity mActivity;
  private AlertDialog.Builder alertDialog;
  private SharedPool sharedPool;
  private SharedPreferences preferences;
  private int[] bboxValues;
  private int[] maxresValues;
  private String[] languageValues;
  private Context mContext;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.options);
    mActivity = this;
    mHandler = new Handler();
    sharedPool = SharedPool.getInstance();
    mContext = this;
    preferences = getSharedPreferences(MainService.SHARED_PREFS, Context.MODE_PRIVATE);
    Resources res = getResources();
    bboxValues = res.getIntArray(R.array.array_bbox);
    maxresValues = res.getIntArray(R.array.array_maxres);
    languageValues = res.getStringArray(R.array.array_language);
    Thread thread = new Thread(createDialog);
    thread.start();
    Button button = (Button) findViewById(R.id.button_login);
    button.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        alertDialog.show();
        alertDialog = null;
        Thread thread = new Thread(createDialog);
        thread.start();
      }
    });

    button = (Button) findViewById(R.id.button_logoff);
    button.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        sharedPool.remove(AppConstants.SP_JSON_LOGIN);
        sharedPool.remove(AppConstants.SP_USERNAME);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove(AppConstants.SHARED_PASSWORD);
        editor.remove(AppConstants.SHARED_USERNAME);
        editor.remove(AppConstants.SHARED_UPTIME);
        editor.commit();
        AppConstants.isLogged = false;
        mHandler.post(disableLogoff);
        mHandler.post(updateUsernameView);
      }
    });

    loadData();
  }

  private Runnable createDialog = new Runnable() {
    @Override
    public void run() {
      try {
        if (mActivity == null)
          return;
        alertDialog = showDialog(mActivity);
      } catch (Exception e) {
        mHandler.post(createDialog);
      }
    }
  };

  public AlertDialog.Builder showDialog(final Activity mActivity) {
    LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    final View view = inflater.inflate(R.layout.login, null);
    AlertDialog.Builder dialog = new AlertDialog.Builder(mActivity);
    dialog.setTitle(R.string.login_title);
    dialog.setView(view);
    dialog.setNeutralButton(R.string.login_yes, new AlertDialog.OnClickListener() {
      @Override
      public void onClick(DialogInterface arg0, int arg1) {
        if (!AppConstants.isLogged) {
          Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
              AppConstants.isLogged = true;
              mHandler.post(disableLogin);
              SharedPool sharedPool = SharedPool.getInstance();
              ArrayList<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();

              EditText edit = (EditText) view.findViewById(R.id.username);
              String username = edit.getText().toString();
              pairs.add(new BasicNameValuePair(MainService.USERNAME, username));

              edit = (EditText) view.findViewById(R.id.password);
              String pass = edit.getText().toString();
              pairs.add(new BasicNameValuePair(MainService.PASSWORD, pass));

              while (!sharedPool.containsKey(MainService.API_BASE_URL_KEY)) {
              }

              String url = (String) sharedPool.get(MainService.API_BASE_URL_KEY);
              JSONObject login = null;
              int count = 5;
              while (login == null && count != 0) {
                login = Downloader.getJSONObject(url.concat(AppConstants.ADDRESS_JSON_LOGIN), pairs, null);
                count--;
              }
              if (login != null) {
                sharedPool.put(AppConstants.SP_JSON_LOGIN, login);
                mHandler.post(enableLogoff);
                sharedPool.put(AppConstants.SP_USERNAME, username);
                mHandler.post(updateUsernameView);
                MainService.saveUserPass(mContext, username, pass);
              } else {
                AppConstants.isLogged = false;
                mHandler.post(disableLogoff);
                sharedPool.remove(AppConstants.SP_USERNAME);
                mHandler.post(updateUsernameView);
                sharedPool.remove(AppConstants.SP_JSON_LOGIN);
              }
              mHandler.post(enableLogin);
              mHandler.post(showToastLogin);
            }
          });
          thread.start();
        }
      }
    });

    dialog.setNegativeButton(R.string.login_no, null);
    return dialog;
  }

  private Runnable disableLogin = new Runnable() {
    @Override
    public void run() {
      Button button = (Button) findViewById(R.id.button_login);
      button.setEnabled(false);
    }
  };

  private Runnable enableLogin = new Runnable() {
    @Override
    public void run() {
      Button button = (Button) findViewById(R.id.button_login);
      button.setEnabled(true);
    }
  };

  private Runnable enableLogoff = new Runnable() {
    @Override
    public void run() {
      Button button = (Button) findViewById(R.id.button_logoff);
      button.setEnabled(true);
    }
  };

  private Runnable disableLogoff = new Runnable() {
    @Override
    public void run() {
      Button button = (Button) findViewById(R.id.button_logoff);
      button.setEnabled(false);
    }
  };

  private Runnable updateUsernameView = new Runnable() {
    @Override
    public void run() {
      TextView text = (TextView) findViewById(R.id.textview_logged);
      if (sharedPool.containsKey(AppConstants.SP_USERNAME)) {
        String logged = getResources().getString(R.string.text_logged);
        text.setText(logged + " " + (String) sharedPool.get(AppConstants.SP_USERNAME));
        text.setVisibility(View.VISIBLE);
      } else {
        text.setVisibility(View.GONE);
      }
    }
  };

  private Runnable makeToast = new Runnable() {
    @Override
    public void run() {
      Toast.makeText(mContext, R.string.about_bbox, Toast.LENGTH_SHORT).show();
    }
  };

  private void loadData() {
    if (AppConstants.isLogged) {
      mHandler.post(enableLogoff);
      if (!sharedPool.containsKey(AppConstants.SP_JSON_LOGIN))
        mHandler.post(disableLogin);
      else {
        Button login = (Button) findViewById(R.id.button_login);
        login.setText(R.string.text_relogin);
      }
      mHandler.post(updateUsernameView);
    }
    int current = (Integer) sharedPool.get(AppConstants.SP_BBOX_VALUE);
    Spinner spinner = (Spinner) findViewById(R.id.spinner_bbox);
    spinner.setSelection(getIndex(bboxValues, current), false);

    spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3) {
        try {
          int value = bboxValues[pos];
          sharedPool.put(AppConstants.SP_BBOX_VALUE, value);
          preferences.edit().putInt(AppConstants.SP_BBOX_VALUE, value).commit();
          mHandler.post(makeToast);
        } catch (Exception e) {
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> arg0) {
      }
    });

    current = (Integer) sharedPool.get(AppConstants.SP_MAX_RESULTS_VALUE);
    spinner = (Spinner) findViewById(R.id.spinner_maxres);
    spinner.setSelection(getIndex(maxresValues, current), false);
    spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3) {
        try {
          int value = maxresValues[pos];
          sharedPool.put(AppConstants.SP_MAX_RESULTS_VALUE, value);
          preferences.edit().putInt(AppConstants.SP_MAX_RESULTS_VALUE, value).commit();
        } catch (Exception e) {
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> arg0) {
      }
    });

    String language = (String) sharedPool.get(AppConstants.SP_LANGUAGE);
    spinner = (Spinner) findViewById(R.id.spinner_language);
    spinner.setSelection(getIndex(languageValues, language), false);
    spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> arg0, View arg1, int pos, long arg3) {
        try {
          String value = languageValues[pos];
          sharedPool.put(AppConstants.SP_LANGUAGE, value);
          preferences.edit().putString(AppConstants.SP_LANGUAGE, value).commit();
        } catch (Exception e) {
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> arg0) {
      }
    });

  }

  private int getIndex(int[] array, int value) {
    for (int i = 0; i < array.length; i++)
      if (array[i] == value)
        return i;
    return 0;
  }

  private int getIndex(String[] array, String value) {
    for (int i = 0; i < array.length; i++)
      if (array[i].contentEquals(value))
        return i;
    return 0;
  }

  private Runnable showToastLogin = new Runnable() {
    @Override
    public void run() {
      if (AppConstants.isLogged) {
        Toast.makeText(mContext, R.string.toast_login, Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(mContext, R.string.toast_login_fail, Toast.LENGTH_SHORT).show();
      }
    }
  };
}
