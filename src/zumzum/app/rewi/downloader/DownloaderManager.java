package zumzum.app.rewi.downloader;

import zumzum.app.rewi.status.Camera;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class DownloaderManager {

	Handler _replyTo;
	
	boolean isRTMP;
	
	HTTPControler httpControler;
	
	RTMPControler rtmpControler;
	
	private Camera _camera;
	
	private Camera tempCamera;

	public DownloaderManager(Handler replyTo, Context _context){

		_replyTo = replyTo;
		
		httpControler = new HTTPControler();
		
		rtmpControler = new RTMPControler(_context);
		
		_camera = null;

	}
	

	//To know is the camera is online
	//send a message to status and BufferingFrames
	Handler cameraStatus = new Handler() {
		public void handleMessage(Message msg) {
			
			Log.e("DownloaderManager ", "cameraStatus");

			//msg.what=1;
			Message msgg = new Message();
			msgg.what=1;
			msgg.obj = msg.obj;
			_replyTo.sendMessage(msgg);
			
			boolean result = (Boolean) msg.obj;
			if (result){
				Log.e("CameraStatus ", "ONLINE");
				
				if (_camera != null){
					if (!isRTMP(_camera.getAddress())){

						Log.e("httpControler ", "stop");
						httpControler.stopThread();


					}
					else{
						if (!isRTMP(tempCamera.getAddress())){
							Log.e("rtmpControler ", "stop");
							rtmpControler.stopThread();
						}
						
					}
					
					
				}
				
				_camera = tempCamera;
				
				if (!isRTMP(_camera.getAddress())){
					
					httpControler.startDownloading(_camera, newFrameHandler);
				}
				else{
					
					rtmpControler.startDownloading(_camera, newFrameHandler);
					
					
				}
				
				
				
			}

		};
	};


	//Receive a new camera
	//send a message to check if it's online
	
		public void checkCam(Camera cam) {
			
			Log.e("DownloaderManager ", "checkCam");
			
			tempCamera = cam;
			
			if (!isRTMP(tempCamera.getAddress())){
			
				//check if camera is online
				httpControler.checkCamOnline(tempCamera.getAddress(), cameraStatus);
			}
			else
			{
				//rtmpControler.startDownloading(tempCamera, newFrameHandler);
				rtmpControler.checkCamOnline(tempCamera.getAddress(), cameraStatus);
			}
		}



	
	
	Handler newFrameHandler = new Handler() {
		public void handleMessage(Message msg) {
			
			//add to buffer new image
			Log.e("DownloaderManager ", "newFrameHandler");
			Message msgg = new Message();
			msgg.what=2;
			msgg.obj = msg.obj;
			
			_replyTo.sendMessage(msgg);
			
			
		};
	};
	
	
	


	protected boolean isRTMP(String address) {
		// TODO Auto-generated method stub
		boolean result = false;
		
		if (address.indexOf("rtmp") != -1) {
			Log.e("POS", "isRTMP mmm");
			 result = true;

		} else {
			Log.e("POS", "isRTMP NO");
			result = false;
		}
		
		
		return result;
	}
	

	public void doStop(){
		
		httpControler.stopThread();
		
		rtmpControler.stopThread();
		
	}	
		
	
	
	
	

}
