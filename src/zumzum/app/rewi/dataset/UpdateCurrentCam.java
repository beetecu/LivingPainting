package zumzum.app.rewi.dataset;

import java.io.BufferedReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import java.util.List;


import org.apache.http.client.HttpClient;


import org.apache.http.impl.client.DefaultHttpClient;

import org.json.JSONException;
import org.json.JSONObject;

import zumzum.app.rewi.status.Camera;

import android.os.AsyncTask;
import android.util.Log;

public class UpdateCurrentCam  extends AsyncTask<String, Void, Void> {

	// Required initialization

	private final HttpClient Client = new DefaultHttpClient();
	private String Content = "";
	private String Error = null;

	String data ="";


	List<Camera> _webcamlist;


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
		int timeout = 10000;

		try {
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
			
			Log.e("POS","UPDATE CURRENT CAM");

			switch (status) {
			case 200: Log.e("error","case 200");
			case 201:
				BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
				StringBuilder sb = new StringBuilder();
				String line;
				while ((line = br.readLine()) != null) {
					sb.append(line+"\n");
					Log.e("read",line);
				}
				br.close();
				//return sb.toString();
				Content = sb.toString();
				c.disconnect();
			}

		} catch (MalformedURLException ex) {
			ex.printStackTrace();
			Log.e("error","line1");
			//Logger.getLogger(DebugServer.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			ex.printStackTrace();
			Log.e("error","line1");
			//Logger.getLogger(DebugServer.class.getName()).log(Level.SEVERE, null, ex);
		}
		return null;
	}

	protected void onPostExecute(Void unused) {
		// NOTE: You can call UI Element here.

		// Close progress dialog


		if (Error != null) {

			//uiUpdate.setText("Output : "+Error);
			Log.e("Error", "Error");

		} else {

			Log.e("Pos", "OK");
			//Log.e("Content", Content);
			// Show Response Json On Screen (activity)
			//uiUpdate.setText( Content );

			/****************** Start Parse Response JSON Data *************/

			String OutputData = "";
			JSONObject jsonResponse;

			try {

				/****** Creates a new JSONObject with name/value mappings from the JSON string. ********/
				jsonResponse = new JSONObject(Content);

				/***** Returns the value mapped by name if it exists and is a JSONArray. ***/
				/*******  Returns null otherwise.  *******/
				
				String response = jsonResponse.optString("message").toString();
				
				Log.e("response",response);
				
				
				
				
			} catch (JSONException e) {

				e.printStackTrace();
			}


		}
	}

}

