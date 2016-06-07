package zumzum.app.rewi;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import zumzum.app.rewi.conectivity.InternetManager;
import zumzum.app.rewi.status.BufferFrames2Show;
import zumzum.app.rewi.status.Camera;
import zumzum.app.rewi.status.FrameDownloaded;
import zumzum.app.rewi.status.FrameDownloadedQueue;
import zumzum.app.rewi.status.SystemStatus;
import zumzum.app.rewi.util.LogSaver;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class DesktopActivity extends Activity {
	
	public BufferFrames2Show _bufferFrames2Show;
	
	public static ImageView _imgCurrent;
	public static ImageView _imgOld;

	public static Animation myFadeInAnimation;
	public static Animation myFadeOutAnimation;

	public static	Bitmap _currentImg;
	public static	Bitmap _oldImg;
	
	SystemStatus _systemStatus;
	
	InternetManager _netConextionManager;
	
	Context _context;
	
	LogSaver _logSaver;
	
	private boolean isStop;
	
	private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
			{
				Log.i("TAG", "OpenCV loaded successfully");
				
				// Create and set View
				//setContentView(R.layout.main);
			} break;
			default:
			{
				super.onManagerConnected(status);
			} break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		if (android.os.Build.VERSION.SDK_INT > 9) {
		    StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		    StrictMode.setThreadPolicy(policy);
		}
		
		//CopyReadAssets();
		
		if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_2, this, mOpenCVCallBack))
		{
			Log.e("TAG", "Cannot connect to OpenCV Manager");
		}
		
		myFadeInAnimation = AnimationUtils.loadAnimation(this,R.anim.fadein);
		myFadeOutAnimation = AnimationUtils.loadAnimation(this,R.anim.fadeout);
		
		_context = this;
		
		isStop = false;
		
		_netConextionManager = new InternetManager(_context); 
		
		_bufferFrames2Show = new BufferFrames2Show();
		
		_logSaver = new LogSaver(_context);
		
		waitConnection();
	}

	private void waitConnection(){
		
		Thread t = new Thread(){
            public void run(){
            	boolean flag = false;
        		
        		while (!flag){
        			
        			if (_netConextionManager.isConected())
        				flag = true;
        			else
        				try {
        					Thread.sleep(3000);
        				} catch (InterruptedException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
        			
        			
        		}
        		
                              
                    try{
                    handlestart.post(startToWork);
                    }catch(Exception e){
                    	_logSaver.save();
                    }
            }
    };
    
    Log.e("Desktop", "waitConnection");
    t.start();
		
    
		
	}
	
	final Handler handlestart = new Handler();	
	
	
	final Runnable startToWork = new Runnable(){
        public void run(){
                //Toast.makeText(RunnableTestActivity.this, "Este es un hilo en background", Toast.LENGTH_SHORT).show();
        	

        	Log.e("Desktop", "startToWork");
        	_systemStatus = new SystemStatus(getBaseContext(),_bufferFrames2Show);
        	
    		_imgCurrent = (ImageView) findViewById(R.id.imageViewCurrent);
    		_imgCurrent.startAnimation(myFadeInAnimation);

    		_imgOld = (ImageView) findViewById(R.id.imageViewOld);
    		_imgOld.startAnimation(myFadeOutAnimation);

    		
    		
    			
    	
            showPaintingSqueduler();
    		
    		//_socketListerner = new BluetoothSocketListerner(socketHandler); 
    		//SockectListener();
    		
            _logSaver.save();
    		
    		
    		Log.e("TAG", "create"); 
                
        }
};
	

	/*
	protected void onStop() {
        super.onStop();

        //Log.e(TAG, "onStop");
        
        isStop = true;
        
        _socketListerner.setStop(true);
        
        _imageEffectManager.set_isStop(true);
        
        _downloaderManagerRTMP.setStop(true);
        
        _system_status.setStop(true);
        finish();
        
        
    }
*/
	
	
	//_________________________________________________________________________________________________________//
	//              TO SHOW PAINTING                                                                           //
	//_________________________________________________________________________________________________________//

	final Handler handler = new Handler(); 
	final Timer timer = new Timer();
	final Runnable showPainting = new Runnable() {
		@Override
		public void run() {
			
			try{

			//Log.e("DesktopActivity", "showPainting1");
			
			if (_bufferFrames2Show.size() != 0){
				
				//Log.e("DesktopActivity", "showPainting2");

				//_currentImg =  getResizedBitmap(_system_status.getImage_to_show());
				if(_currentImg!=null){  
					_currentImg=null;
			     } 
				
				
				
				FrameDownloaded frame = _bufferFrames2Show.getFrame2show();
				
				if (frame.getCamera().getId() != null){
					//zumzum.app.rewi.dataset.DatasetManager.updateCurrentCam(frame.getCamera().getId());
					Log.e("frame.getCamera.id",frame.getCamera().getId());
				}
				
				_currentImg = frame.getImageBmp();
				_imgOld.setImageDrawable(null);
				_imgOld.setImageDrawable(new BitmapDrawable(_context.getResources(), _oldImg));
				//_imgOld.startAnimation(myFadeOutAnimation);

				_imgCurrent.setImageDrawable(null);
				_imgCurrent.setImageDrawable(new BitmapDrawable(_context.getResources(), _currentImg));
				_imgCurrent.startAnimation(myFadeInAnimation); //Set animation to your ImageView
				

				if(_oldImg!=null){  
					_oldImg=null;
			     } 
				_oldImg = _currentImg;
				
				_logSaver.save();
			}

			}catch(Exception e){
				
				e.printStackTrace();
			}


		}
		
	};



	public void showPaintingSqueduler(){

		//for (int i = 1; i <= 5; i++)  {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				//Log.e("Action","showPainting");
				//printLog();
				handler.post(showPainting);
			}
		};
		
		if (!isStop){
		
			timer.schedule(task, 0, 5000); 
			
		}
		//timer.schedule(task, i* 6000); 

		//}
	}
	
	
	@Override
    protected void onPause() {
        super.onPause();
        Log.e("DesktopActivity", "onPause()");
        
        
        
    }

    @Override
    protected void onStop() {
        super.onStop();
        
        Log.e("DesktopActivity", "onStop()");
        
        isStop=true;
        timer.cancel();
        
        _systemStatus.doStop();
        
        finish();
        
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        
        Log.e("DesktopActivity", "onDestroy");
        
        finish();
        
    }
    
	
	
}
