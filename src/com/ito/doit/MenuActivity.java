package com.ito.doit;

import java.util.ArrayList;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MenuActivity extends Activity {
	
	private Context mContext;
	private String username;
	private String pass;
	private Handler mHandler;
	private SharedPreferences preferences;
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu_activity);
		mContext = this;
		SharedPool sharedPool = SharedPool.getInstance();
		preferences = getSharedPreferences(MainService.SHARED_PREFS, Context.MODE_PRIVATE);
		int value = preferences.getInt(AppConstants.SP_BBOX_VALUE, AppConstants.DEFAULT_BBOX_VALUE);
		sharedPool.put(AppConstants.SP_BBOX_VALUE, value);
		value = preferences.getInt(AppConstants.SP_MAX_RESULTS_VALUE, AppConstants.DEFAULT_MAX_RESULTS_VALUE);
		sharedPool.put(AppConstants.SP_MAX_RESULTS_VALUE, value);
		String language = preferences.getString(AppConstants.SP_LANGUAGE, AppConstants.DEFAULT_LANGUAGE);
		sharedPool.put(AppConstants.SP_LANGUAGE, language);
		mHandler = new Handler();
		if (preferences.contains(AppConstants.SHARED_UPTIME) && !AppConstants.isLogged){
			int timestamp = preferences.getInt(AppConstants.SHARED_UPTIME, 0);
			int now = MainService.genTimeStamp();
			if (now - timestamp < 3){
				username = preferences.getString(AppConstants.SHARED_USERNAME, "");
				pass = preferences.getString(AppConstants.SHARED_PASSWORD, "");
				Thread thread = new Thread(tryLogin);
				thread.start();
			} else {
				SharedPreferences.Editor editor = preferences.edit();
				editor.remove(AppConstants.SHARED_PASSWORD);
				editor.remove(AppConstants.SHARED_USERNAME);
				editor.remove(AppConstants.SHARED_UPTIME);
				editor.commit();
			}
		}
		
		
		Button button = (Button) findViewById(R.id.map);
		button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(mContext, Map.class);
				mContext.startActivity(intent);
			}			
		});
		
		button = (Button) findViewById(R.id.list);
		button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(mContext, PointsListActivity.class);
				mContext.startActivity(intent);
			}			
		});		
		
		button = (Button) findViewById(R.id.new_point);
		button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(mContext, NewPointActivity.class);
				mContext.startActivity(intent);
			}			
		});
		
		button = (Button) findViewById(R.id.options);
		button.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent(mContext, OptionsActivity.class);
				mContext.startActivity(intent);
			}			
		});	
		
		
	}

	@Override
	protected void onPause() {		
		super.onPause();
		MainService.gone();
	}

	@Override
	protected void onResume() {		
		super.onResume();
		MainService.startService(mContext);
	}

	private Runnable tryLogin = new Runnable(){
		@Override
		public void run() {
			AppConstants.isLogged = true;
			SharedPool sharedPool = SharedPool.getInstance();				
			ArrayList<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>();
		
			pairs.add(new BasicNameValuePair(MainService.USERNAME, username));
			pairs.add(new BasicNameValuePair(MainService.PASSWORD, pass));
		
			while(!sharedPool.containsKey(MainService.API_BASE_URL_KEY)) {}
		
			String url = (String) sharedPool.get(MainService.API_BASE_URL_KEY);
			JSONObject login = null;
			int count = 5;
			while (login == null && count != 0){
				login = Downloader.getJSONObject(url.concat(AppConstants.ADDRESS_JSON_LOGIN), pairs, null);
				count--;
			}			
			if (login != null){
				sharedPool.put(AppConstants.SP_USERNAME, username);
				sharedPool.put(AppConstants.SP_JSON_LOGIN, login);
				MainService.saveUserPass(mContext, username, pass);
			}	
			else {
				AppConstants.isLogged = false;
				SharedPreferences.Editor editor = preferences.edit();
				editor.remove(AppConstants.SHARED_PASSWORD);
				editor.remove(AppConstants.SHARED_USERNAME);
				editor.remove(AppConstants.SHARED_UPTIME);
				editor.commit();
				
				
			}
			mHandler.post(showToastLogin);			
		}
		
	};

	private Runnable showToastLogin = new Runnable(){
		@Override
		public void run() {
			if (AppConstants.isLogged){
				Toast.makeText(mContext, R.string.toast_login, Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mContext, R.string.toast_login_fail, Toast.LENGTH_SHORT).show();
			}			
		}		
	};
	
	
}
