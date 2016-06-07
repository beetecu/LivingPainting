package zumzum.app.rewi.dataset;

import java.io.Serializable;
import java.util.List;

import zumzum.app.rewi.status.Camera;
import zumzum.app.rewi.status.SystemConf;
import zumzum.app.rewi.util.MacAddress;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class DatasetManager {
	
	static String server = "http://socialkiosko.appspot.com/welcome/default/call/json/";
	
	static String _screenConf = "screen_configuration";
	static String _currentCamControler = "update_current_camera?";
	static String _screenIdControler = "get_screen_Id?mac=";
	
	private static Context _context;
	
	private static String _screenId;
	
	private static boolean _isError;
	
	private static  Handler _replyTo;
	static Bundle data;
	public static final String CONFIGURATION = "Configuration";

	
	//String serverURL = "http://androidexample.com/media/webservice/JsonReturn.php";
	
	public DatasetManager(Context context, Handler replyTo){
		
		Log.e("Pos","DatasetManager1");
		
		_context = context;
		
		_replyTo = replyTo;
		data = new Bundle();
		
		_screenId = "";
		setError(false);
		
		
		MacAddress mac = new MacAddress(_context);
		
		//readCamlist();
		//updateCurrentCam("13");
		queryScreenId(mac.getMacAddress());
		
		Log.e("POS","DatasetManager2");
		
		
	}
	
	public static void readSystemConf(){
		
		if (_screenId.length() > 0){
			
			String screenConf= server + _screenConf + "?screen=" + _screenId;
			new  ReadScreenConf().execute(screenConf);
		}
		
		
	}
	
	public static void updateCurrentCam(String camId){
		
		String currentCam = server + _currentCamControler + "s=" + getScreenId() + "&c=" + camId;
		new UpdateCurrentCam().execute(currentCam);
	}
	
	public void queryScreenId(String screenMac){
		
		
		
		//String queryPath = server +  _screenIdControler + screenMac;
		//String queryPath = "http://192.168.1.132:8000/LivingWinfo/default/call/json/get_screen_Id?mac=B4:07:F9:33:A1:EC";
		String queryPath = "http://socialkiosko.appspot.com/welcome/default/call/json/get_screen_Id?mac=B4:07:F9:33:A1:EC";
		Log.e("queryPath ", queryPath );
		//Log.e("queryPath1 ", queryPath );
		new GetScreenId().execute(queryPath);
	}
	
	public static void setScreenId(String message){
		
		if (message.contains("Error")) {
			
			_screenId = null;
		
			setError(true);
			
			Log.e("screenId","ERROR");
		}
		
		else{
			
			Log.e("screenId",_screenId);
			_screenId =  message;
			 readSystemConf();
		}
			
			
	}

	
	public static String getScreenId(){
		
		return _screenId;
		
	}

	public boolean isError() {
		return _isError;
	}

	public static void setError(boolean isError) {
		_isError = isError;

	}
	
	public static void update( List<Camera> webcamlist,  boolean mode, Camera cam) {
		
		SystemConf conf = new SystemConf(webcamlist, mode, cam);


        Bundle data = new Bundle();
        data.putSerializable(CONFIGURATION, conf);


        Message msg = Message.obtain();
        msg.setData(data);
        _replyTo.sendMessage(msg);


	}
	
	public static void reportError() {
		
		SystemConf conf = null;


        Bundle data = new Bundle();
        data.putSerializable(CONFIGURATION, conf);


        Message msg = Message.obtain();
        msg.setData(data);
        _replyTo.sendMessage(msg);


	}
	
	
	
}



