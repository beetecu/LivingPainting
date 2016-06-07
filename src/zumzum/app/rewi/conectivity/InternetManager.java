package zumzum.app.rewi.conectivity;

import android.content.Context;
import android.net.ConnectivityManager;



	public class InternetManager{
		
		Context _context;
		
		public InternetManager(Context context){
		
			_context = context;
			isConected();
			
		}

		public boolean isConected() {
			
			boolean result = true;
			
			ConnectivityManager manager = (ConnectivityManager) _context.getSystemService(_context.CONNECTIVITY_SERVICE);

			//For 3G check
			boolean is3g = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
			            .isConnectedOrConnecting();
			//For WiFi Check
			boolean isWifi = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
			            .isConnectedOrConnecting();

			System.out.println(is3g + " net " + isWifi);

			if (!is3g && !isWifi) 
			{ 
			  result = false;
			} 
			 
			
			return result;
			
		}

		public void setIs_conected(boolean is_conected) {
		}

	}