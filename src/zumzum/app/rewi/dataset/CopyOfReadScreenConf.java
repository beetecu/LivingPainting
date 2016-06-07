package zumzum.app.rewi.dataset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import zumzum.app.rewi.status.Camera;
import zumzum.app.rewi.status.SystemStatus;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

// Class with extends AsyncTask class
public class CopyOfReadScreenConf  extends AsyncTask<String, Void, Void> {

	// Required initialization

	private final HttpClient Client = new DefaultHttpClient();
	private String Content = "";
	private String Error = null;

	String data ="";


	public List<Camera> _webcamlist;
	public Camera _camera;
	public boolean _mode;


	protected void onPreExecute() {
		// NOTE: You can call UI Element here.

		//Start Progress Dialog (Message)

		try{

			

			// Set Request parameter
			data +="&" + URLEncoder.encode("data", "UTF-8") + "=";

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	// Call after onPreExecute method
	protected Void doInBackground(String... urls) {
		int timeout = 8000;
		
		String result = "OK";


			try {
				
				Log.e("doInBackground", "doInBackground");
				
				Content = inputStreamToString (doHttpGet(urls[0])); 
				Log.e("ScreenId", Content);
				/*
				URL u = new URL(urls[0]);
				HttpURLConnection c = (HttpURLConnection) u.openConnection();
				c.setRequestMethod("GET");
				c.setRequestProperty("Content-length", "0");
				c.setUseCaches(false);
				c.setAllowUserInteraction(false);
				c.setConnectTimeout(timeout);
				c.setReadTimeout(timeout);
				c.connect();
				int status = c.getResponseCode();

				switch (status) {
				case 200: 
				case 201:
					BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
					StringBuilder sb = new StringBuilder();
					String line;
					while ((line = br.readLine()) != null) {
						sb.append(line+"\n");
						Log.e("read screen",line);
					}
					br.close();
					//return sb.toString();
					Content = sb.toString();
					c.disconnect();
				}
				*/

			} catch (MalformedURLException ex) {
				ex.printStackTrace();
				Log.e("error","line1");
				result = null;
				
			} catch (IOException ex) {
				ex.printStackTrace();
				Log.e("error","line1");
				result = null;
				
			}
			//return result;
			return null;
		}

	protected void onPostExecute(String Result) {
		// NOTE: You can call UI Element here.


		String message = "";

		if (Content.isEmpty()) {

			//uiUpdate.setText("Output : "+Error);
			Log.e("Error", "Errorrr");
			message = "Error";

		} else {

			//Log.e("Pos", "OK");
			//Log.e("Content", Content);
			// Show Response Json On Screen (activity)
			//uiUpdate.setText( Content );

			/****************** Start Parse Response JSON Data *************/

			
			JSONObject jsonResponse;

			try {

				/****** Creates a new JSONObject with name/value mappings from the JSON string. ********/
				jsonResponse = new JSONObject(Content);

				/***** Returns the value mapped by name if it exists and is a JSONArray. ***/
				/*******  Returns null otherwise.  *******/
				JSONArray jsonMainNode = jsonResponse.optJSONArray("cameras");

				/*********** Process  JSON Node  for camera list************/

				int lengthJsonArr = jsonMainNode.length(); 

				//Log.e("lengthJsonArr", Integer.toString(lengthJsonArr));

			

				for(int i=0; i < lengthJsonArr; i++)
				{
					Camera camera = new Camera();

					/****** Get Object for each JSON node.***********/
					JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);

					/******* Fetch node values **********/

					camera.setAddress(jsonChildNode.optString("f_address_string").toString());
					//Log.e("address",jsonChildNode.optString("f_address_string").toString());
					camera.setEffect(jsonChildNode.optString("f_img_effect_string").toString());
					camera.setOperative(jsonChildNode.optBoolean("is_active"));
					camera.setRefrestime(jsonChildNode.optLong ("f_refresh_time"));
					camera.setTime_in(jsonChildNode.optString("f_time_in").toString());
					camera.setTime_out(jsonChildNode.optString("f_time_out").toString());
					camera.setWindH(jsonChildNode.optInt("f_cut_height"));
					camera.setWindW(jsonChildNode.optInt("f_cut_width"));
					camera.setWindX(jsonChildNode.optInt("f_cut_x"));
					camera.setWindY(jsonChildNode.optInt("f_cut_y"));
					camera.setId(jsonChildNode.optString("id").toString());
					
					_webcamlist.add(camera);
				}
				
				/*********** Process  JSON Node  for Mode ************/
				JSONObject jsmode = jsonResponse.getJSONObject("mode");

				_mode = jsmode.getBoolean("isModeManual");
				
				
				if (_mode) 
				
					Log.e("mode manual","True");
				else
					Log.e("mode manual","False");
				
				/*********** Process  JSON Node  for Camera in mode Manual ************/
				

				/***** Returns the value mapped by name if it exists and is a JSONArray. ***/
				/*******  Returns null otherwise.  *******/
				

				/*********** Process  JSON Node  for camera list************/
				JSONObject jscamera = jsonResponse.getJSONObject("camera");
				

				_camera = new Camera();

				
				/******* Fetch node values **********/


				_camera.setAddress(jscamera.getString("f_address_string").toString());
				
				_camera.setEffect(jscamera.getString("f_img_effect_string").toString());
				
				_camera.setOperative(jscamera.getBoolean("is_active"));
				
				_camera.setRefrestime(jscamera.getLong ("f_refresh_time"));
				
				_camera.setTime_in(jscamera.getString("f_time_in").toString());
				_camera.setTime_out(jscamera.getString("f_time_out").toString());
				_camera.setWindH(jscamera.getInt("f_cut_height"));
				_camera.setWindW(jscamera.getInt("f_cut_width"));
				//Log.e("address",jscamera.getString("f_address_string").toString());
				_camera.setWindX(jscamera.getInt("f_cut_x"));
				
				_camera.setWindY(jscamera.getInt("f_cut_y"));
				
				_camera.setId(jscamera.getString("id").toString());
				
				//OJO arreglar
				
				//update system configuration
				
				DatasetManager.update(_webcamlist, _mode, _camera);
			
				

			} catch (JSONException e) {

				Log.e("Pos","JSONException");
				e.printStackTrace();
				DatasetManager.reportError();
			}
			catch (Exception e) {

				Log.e("Exception","Exception");
				e.printStackTrace();
				DatasetManager.reportError();
			}


		}
	}
	
private  InputStream doHttpGet(String urlString) throws IOException{
		
		InputStream inputStream = null;
        int response = -1;
               
        URL url = new URL(urlString); 
        URLConnection conn = url.openConnection();
                 
        if (!(conn instanceof HttpURLConnection))                     
            throw new IOException("Not an HTTP connection");
        
        try{
            HttpURLConnection httpConn = (HttpURLConnection) conn;
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.setUseCaches(true);
            Object content = httpConn.getContent();
    		if( content instanceof Bitmap ){
    			Bitmap bitmap = (Bitmap)content;
    		}
            
            response = httpConn.getResponseCode();                 
            if (response == HttpURLConnection.HTTP_OK) {
                inputStream = httpConn.getInputStream();                                 
            }                     
        }
        catch (Exception ex)
        {
            throw new IOException("Error connecting" + ex);            
        }
        return inputStream;
	}
		
	public String inputStreamToString (InputStream in) throws IOException {
	    StringBuilder out = new StringBuilder();
	    byte[] b = new byte[4096];
	    for (int n; (n = in.read(b)) != -1;) {
	        out.append(new String(b, 0, n));
	    }
	    return out.toString();
	}

}

