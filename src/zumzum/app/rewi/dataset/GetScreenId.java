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
import java.util.List;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;


import zumzum.app.rewi.status.Camera;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

public class GetScreenId extends AsyncTask<String, Void, String> {

	// Required initialization

	//private final HttpClient Client = new DefaultHttpClient();
	private String Content = "";

	String data ="";


	List<Camera> _webcamlist;
	
	private String screenId;


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
	protected String doInBackground(String... urls) {
		int timeout = 8000;
		
		String result = "OK";
		Log.e("url", urls[0]);

		try {
			//Log.e("doInBackground", "doInBackground");
			
			Content = inputStreamToString (doHttpGet(urls[0])); 
			//Log.e("ScreenId", Content);
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
		return result;
	}

	protected void onPostExecute(String Result) {
		// NOTE: You can call UI Element here.

		// Close progress dialog

		String message = "";

		if (Result == null) {

			//uiUpdate.setText("Output : "+Error);
			Log.e("Error", "Errorrr");
			message = "Error";

		} else {

			//Log.e("Pos", "OKK");
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
				//String response = "11";
				
				Log.e("response",response);
				
				message = response;
			
				
				
			} catch (JSONException e) {
				
				message = "Error";

				e.printStackTrace();
			}


			
		}
		//Log.e("POS", "pos1");
		zumzum.app.rewi.dataset.DatasetManager.setScreenId(message);
		//Log.e("POS", "pos2");
	}

	public String getScreenId() {
		return screenId;
	}

	public void setScreenId(String screenId) {
		this.screenId = screenId;
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

