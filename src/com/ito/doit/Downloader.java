package com.ito.doit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class Downloader {
	
	/**
	 * Gets InputStream from url
	 */	
	public static InputStream connect(String url, ArrayList<BasicNameValuePair> pairs){
		HttpClient httpclient = new DefaultHttpClient();
		
		HttpPost httppost = new HttpPost(url);
		if (pairs != null){
			try{
				httppost.setEntity(new UrlEncodedFormEntity(pairs));
			} catch (Exception e) {}
		}
		try {
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return entity.getContent();
			}			
		} catch (Exception e) {}		
		return null;
	}
	
	/**
	 * Gets InputStream from url
	 */	
	public static InputStream connectGet(String url){
		HttpClient httpclient = new DefaultHttpClient();	
		HttpGet httppost = new HttpGet(url);		
		try {
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				return entity.getContent();
			}			
		} catch (Exception e) {}		
		return null;
	}
	
	
	/**
	 * Decodes InputStream of CSV format
	 */
	public static ArrayList<HashMap<String, String>> decodeCSV(InputStream is){
		if (is == null) {
			return null;
		}
		
		ArrayList<HashMap<String, String>> array = new ArrayList<HashMap<String, String>>();		
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line = null;
		try {			
			line = reader.readLine();
			String[] names = decodeCSVLine(line);					
			while ((line = reader.readLine()) != null) {
				array.add(arrayToMap(names, (decodeCSVLine(line))));
			}			
		} catch (Exception e){}		
		return array;
	}
	
	/**
	 * Decodes String of CSV Format
	 */	
	public static String[] decodeCSVLine(String line){
		if (line == null || line.length() == 0)
			return null;
		ArrayList<String> array = new ArrayList<String>();
		int start = 0;
		int cIndex = -1;
		int qIndex = -1;
		while (start != line.length()-1){
			cIndex = line.indexOf(',', start);
			qIndex = line.indexOf('"', start);
			if (cIndex == -1 && qIndex == -1){
				String tmp = line.substring(start);
				if (tmp.length() != 0 )				
					array.add(line.substring(start));
				break;
			}			
			if (cIndex == -1 && qIndex != -1){
				try {
					int qIndex2 = line.indexOf('"', qIndex+1);
					array.add(line.substring(qIndex+1, qIndex2));
					start = qIndex2 + 1;	
					continue;					
				} catch (Exception e){}
			}			
			if (cIndex != -1 && qIndex == -1){
				if (start != cIndex){
					array.add(line.substring(start, cIndex));
					start = cIndex+1;
					continue;
				} else start++;
			}			
			if (cIndex != -1 && qIndex != -1){
				if (qIndex < cIndex) {
					int qIndex2 = line.indexOf('"', qIndex+1);
					array.add(line.substring(qIndex+1, qIndex2));
					start = qIndex2 + 1;	
					continue;
				} else {
					if (start != cIndex){
						array.add(line.substring(start, cIndex));
						start = cIndex+1;
						continue;
					} else start++;
					
				}
			}		
		}
		String[] array2 = new String[array.size()]; 
		return array.toArray(array2);		
	}
	
	/**
	 * Creates to HashMap from two String arrays
	 */
	public static HashMap<String, String> arrayToMap(String[] names, String[] values){
		if (names == null || values == null)
			return null;
		HashMap<String, String> map = new HashMap<String, String>();
		int i = names.length;
		if (i > values.length)
			i = values.length;
		for (int x = 0; x < i; x++) {
			map.put(names[x], values[x]);
		}	
		return map;
	}
	
	
	/* This is a test function which will connects to a given
     * rest service and prints it's response to Android Log with
     * labels "Praeda".
     */
    public static JSONObject getJSONObject(String url, double lat, double lon)
    {
    	HttpClient httpclient = new DefaultHttpClient();
 
        // Prepare a request object
        HttpPost httpget = new HttpPost(url);
        ArrayList<BasicNameValuePair> pairs = new ArrayList<BasicNameValuePair>(2);
        pairs.add(new BasicNameValuePair("lat", String.valueOf(lat)));
        pairs.add(new BasicNameValuePair("lon", String.valueOf(lon)));
        try {
			httpget.setEntity(new UrlEncodedFormEntity(pairs));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}       
 
        // Execute the request
        HttpResponse response;
        try {
            response = httpclient.execute(httpget);            
 
            // Get hold of the response entity
            HttpEntity entity = response.getEntity();
             
            if (entity != null) {
            	// A Simple JSON Response Read
                InputStream instream = entity.getContent();
                String result = convertStreamToString(instream);
                // A Simple JSONObject Creation
                JSONObject json = new JSONObject(result);
                instream.close();
                return json;              
                /*
                // A Simple JSONObject Parsing
                JSONArray nameArray=json.names();
                JSONArray valArray=json.toJSONArray(nameArray);
                for(int i=0;i<valArray.length();i++)
                {
                    Log.i(MainService.TAG,"<jsonname"+i+">\n"+nameArray.getString(i)+"\n</jsonname"+i+">\n"
                            +"<jsonvalue"+i+">\n"+valArray.getString(i)+"\n</jsonvalue"+i+">");
                } 
                // A Simple JSONObject Value Pushing
                json.put("sample key", "sample value");
                Log.i(MainService.TAG,"<jsonobject>\n"+json.toString()+"\n</jsonobject>");
 				*/
                // Closing the input stream will trigger connection release
                
            } else {
            	Log.i(MainService.TAG, "Entity is null");
            } 
 
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(MainService.TAG, "Connection is done");
        return null;
    }
    
    public static String convertStreamToString(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder(); 
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
    
    public static String convertFileToString(File file) {
        FileReader reader;
		try {
			reader = new FileReader(file);
		} catch (FileNotFoundException e1) {
			return null;
		}
        StringBuilder sb = new StringBuilder(); 
        int line = -1;
        try {
            while ((line = reader.read()) != -1) {
            	char a = (char) line;
                sb.append(a);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
    
    public static JSONArray getJSONArray(String url)
    {
    	HttpClient httpclient = new DefaultHttpClient();
 
        // Prepare a request object
        HttpPost httpget = new HttpPost(url);            
 
        // Execute the request
        HttpResponse response;
        try {
            response = httpclient.execute(httpget);            
 
            // Get hold of the response entity
            HttpEntity entity = response.getEntity();
             
            if (entity != null) {
            	// A Simple JSON Response Read
                InputStream instream = entity.getContent();
                String result = convertStreamToString(instream);
                // A Simple JSONObject Creation
                JSONArray json = new JSONArray(result);
                instream.close();
                return json;              
                               
            } else {
            	Log.i(MainService.TAG, "Entity is null");
            } 
 
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(MainService.TAG, "Connection is done");
        return null;
    }
    
    public static JSONObject getJSONObject(String url, ArrayList<BasicNameValuePair> pairs, String cookie)
    {
    	DefaultHttpClient httpclient = new DefaultHttpClient();
    		
        HttpPost httppost = new HttpPost(url);
        if (cookie != null)
        	httppost.setHeader("Cookie", cookie);
       
        try {
			httppost.setEntity(new UrlEncodedFormEntity(pairs));
		} catch (Exception e) {}
        HttpResponse response;
        try {
            response = httpclient.execute(httppost);            
            HttpEntity entity = response.getEntity();             
            if (entity != null) {
                InputStream instream = entity.getContent();
                String result = convertStreamToString(instream);
                JSONObject json = new JSONObject(result);
                instream.close();
                return json; 
            } else {
            	Log.i(MainService.TAG, "Entity is null");
            }  
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.i(MainService.TAG, "Connection is done");
        return null;
    }
    
    
    
    
}
