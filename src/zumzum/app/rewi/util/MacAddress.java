package zumzum.app.rewi.util;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings.Secure;
import android.util.Log;

public class MacAddress {

	private String _macAddress;

	private static Context _context;
	
	public MacAddress(Context context){
		_context = context;
		setMacAddress();
	}
	
	public String getMacAddress() {
		
		
		return _macAddress;
	}

	public void setMacAddress() {
		
		this._macAddress = macAddress();
	}
	
	  private  String macAddress() {
		    WifiManager wifiMan = (WifiManager) _context
		        .getSystemService(Context.WIFI_SERVICE);
		    WifiInfo wifiInf = wifiMan.getConnectionInfo();
		    String mac = wifiInf.getMacAddress();
		    // If the mac address is null, return something that is workable.
		    if (mac == null) {
		      String uniqueId = Secure.getString(_context.getContentResolver(),
		          Secure.ANDROID_ID);
		      // We hope the last 9 chars are sufficiently unique.
		      mac = "001" + uniqueId.substring(0, 9);
		    }
		    Log.e("MAC ADDRESS",mac);
		    return mac;
		  }
	
}
