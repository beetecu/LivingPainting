package zumzum.app.rewi.downloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opencv.core.CvException;

import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_imgproc;
import com.googlecode.javacv.cpp.opencv_core.IplImage;

import zumzum.app.rewi.downloader.RTMPControlerv1.PauseAction.Reminder;
import zumzum.app.rewi.status.Camera;
import zumzum.app.rewi.status.FrameDownloaded;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class RTMPControlerv1 {

	//int BUFFMAX = 100000;

	FrameLoader frameLoaderThread;
	public boolean isDownloading;

	public static Context _context;
	Process process_check = null;
	byte[] buff = null;

	private boolean isFirstBuffMsg;


	private boolean isPlaying;
	private PauseAction pause;

	RTMPdownload downloader;

	File folder; 
	
	Reminder _reminder;

	public RTMPControlerv1(Context context){

		isDownloading = false;
		_context = context;
		CopyReadAssets("rtmpdump");
		CopyReadAssets("rtmpdumpvalid");

		downloader = null;

		pause = null;

		isPlaying = false;

		isFirstBuffMsg = true;

		folder = new File(Environment.getExternalStorageDirectory()
				+ "/WEBCAM");

		boolean success = false;
		if (!folder.exists()) {
			success = folder.mkdir();
			// Log.e("Pos", "folder.mkdir");
		}
		if (folder.exists()) {
			success = true;
			// Log.e("Pos", "folder.ok");
		}

	}


	////////////////////////////////////////////////////////////////////////////////////
	///rtmpdump
	private void CopyReadAssets(String filename) {
		AssetManager assetManager =  _context.getAssets();

		InputStream in = null;
		OutputStream out = null;

		Log.e("fd",  _context.getFilesDir().toString());

		File file = new File( _context.getFilesDir(), filename);
		try {

			in = assetManager.open("rtmpdump");

			out =  _context.openFileOutput(file.getName(), Context.MODE_WORLD_READABLE);

			copyFile(in, out);
			in.close();
			in = null;
			out.flush();
			out.close();
			out = null;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			Runtime.getRuntime().exec("/system/bin/chmod 777 "+ filename,null, _context.getFilesDir());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void copyFile(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[1024];
		int read;
		while ((read = in.read(buffer)) != -1) {
			out.write(buffer, 0, read);
		}
	}
	////////////////////////////////////////////////////////////////////////



	public void startDownloading(Camera cam, Handler callbackToDrow){

		Log.e("HTTPControler ", "startDownloading");

		isFirstBuffMsg = true;
		
		_reminder = null;

		if (isPlaying){

			//pause.doPause(60);
			//downloader.stopThread();
			GetStream.stopThread();
			reStartDownloading( cam,  callbackToDrow);
			


		}
		else{

			isPlaying = false;

			if (pause != null)
				pause.cancel();

			pause = new PauseAction(cam, callbackToDrow);

			//start frames capture
			frameLoaderThread = new FrameLoader(cam, callbackToDrow);
			frameLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);
			frameLoaderThread.setName("frameLoader");

			new Thread(new DownloadCam(cam.getAddress(), responseHandler)).start();
		}


	}
	
	public void reStartDownloading(Camera cam, Handler callbackToDrow){
		
		Log.e("RTMPControler", "reStartDownloading---------");
		isFirstBuffMsg = true;
		
		_reminder = null;

		if (isPlaying){

			frameLoaderThread.Stop();

			while (isPlaying){

				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		}

		isPlaying = false;

		if (pause != null)
			pause.cancel();

		pause = new PauseAction(cam, callbackToDrow);

		//start frames capture
		frameLoaderThread = new FrameLoader(cam, callbackToDrow);
		frameLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);
		frameLoaderThread.setName("frameLoader");

		
		String mPath = Environment.getExternalStorageDirectory()
				+ "/isWEBCAM/video.flv";
		File file = new File(mPath);

		file.deleteOnExit();
		if (file.exists()) {
			file.delete();
		}
		try {
			file.createNewFile();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		new Thread(new GetStream(responseHandler, cam.getAddress(),
				_context.getFilesDir(), file)).start();
		
		
		Log.e("RTMPControler", "reStartDownloading---------");

		
	}


	public void checkCamOnline(String url, Handler handlermsg){

		Log.e("RTMPControler ", "start checkCamOnline");
		//Boolean result = isValid(url);

		new Thread(new CheckCam(url, handlermsg)).start();

		Log.e("RTMPControler ", "end checkCamOnline");



	}

	// Read data with timeout
	Callable<Integer> readTask = new Callable<Integer>() {
		@Override
		public Integer call() throws IOException {
			//Log.e("GetStream", "Pos5");
			buff = new byte[1024 * 4];
			return  process_check.getInputStream().read(buff); 
		}
	};


	public class DownloadCam extends Thread {

		private String _url;
		private Handler _handlermsg;

		public DownloadCam(String url, Handler handlermsg){

			_url = url;
			_handlermsg = handlermsg;

		}


		private Handler handler = new Handler();

		// private Activity context;
		@Override
		public void run() {
			Log.e("RTMPControler ", "downloadcam");
			handler.post(new Runnable() {

				@Override
				public void run() {
					// btPlay.setEnabled(false);
					boolean mExternalStorageAvailable = false;

					String state = Environment.getExternalStorageState();

					// Log.e("Pos", "loadChannel()");

					// if (Environment.MEDIA_MOUNTED.equals(mExternalStorageAvailable)) {
					// file =new File(getCacheDir(),"downloadingMedia.flv");
					// }
					// else{

					// Falta chequear que no hay almacenamiento externo

		

					File folder = new File(Environment.getExternalStorageDirectory()
							+ "/isWEBCAM");

					boolean success = false;
					if (!folder.exists()) {
						success = folder.mkdir();
						// Log.e("Pos", "folder.mkdir");
					}
					if (folder.exists()) {
						success = true;
						// Log.e("Pos", "folder.ok");
					}

					

					// mPath = Environment.getExternalStorageDirectory() + "/isTV/video"
					// + String.valueOf(countL) + ".flv";
					
					
					String mPath = Environment.getExternalStorageDirectory()
							+ "/isWEBCAM/video.flv";
					File file = new File(mPath);

					file.deleteOnExit();
					if (file.exists()) {
						file.delete();
					}
					try {
						file.createNewFile();
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}

			
					//Log.e("Pos", "loadChannel()2");
					// setUrl(
					// "-r rtmp://video2.earthcam.com/fecnetwork/4048spaceage.flv.flv -p http://www.earthcam.com -W http://www.earthcam.com/swf/cam_player_v2/ecnPlayer.swf?20120504_a live=1  -v -o -");
					new Thread(new GetStream(responseHandler, _url,
							_context.getFilesDir(), file)).start();
					
					

					Log.e("Pos", "check stream");
					//checkStream();

					//new Thread(new RTMPdownload(_handlermsg, _url,
						//	_context.getFilesDir(), file)).start();


				}
			});

		}

	}



	public class CheckCam extends Thread {

		private String _url;
		private Handler _handlermsg;

		public CheckCam(String url, Handler handlermsg){

			_url = url;
			_handlermsg = handlermsg;

		}


		private Handler handlershow = new Handler();

		// private Activity context;
		@Override
		public void run() {
			Log.e("Pos", "checkcam");
			handlershow.post(new Runnable() {

				@Override
				public void run() {

					isValid(_url, _handlermsg);
				}
			});

		}

	}



	@SuppressLint({ "ParserError", "ParserError", "ParserError", "ParserError",
		"ParserError", "ParserError" })
	private void isValid(String path,  Handler handlermsg){

		boolean result= false;

		boolean isWorking = true;

		try {

			//String testpath = "-r rtmp://video2.earthcam.com/fecnetwork/sliberty.flv.flv   -p http://www.earthcam.com  -W http://www.earthcam.com/swf/cam_player_v2/ecnPlayer.swf?20120504_a live=1 -v -o -";

			process_check = Runtime.getRuntime().exec("./rtmpdumpvalid " + path,
					null, _context.getFilesDir());

			Log.e("Pos", "rtmpdump..OK");


		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			process_check.destroy();
			isWorking = false;
		}

		if (isWorking){


			buff = new byte[1024 * 4];

			ExecutorService executor = Executors.newFixedThreadPool(2);
			Integer readByte = 1;

			Future<Integer> future = executor.submit(readTask);
			//Log.e("GetStream", "Pos1");
			try {
				readByte = future.get(30000, TimeUnit.MILLISECONDS);
				//Log.e("GetStream", "Pos2");
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				result = false;
				readByte=0;
			} catch (ExecutionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				result = false;
				readByte=0;
			} catch (TimeoutException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				result = false;
				readByte=0;
			}

			if (readByte > 0){

				result = true;
			}

		}


		Message msg = new Message();
		msg.obj = result;
		//msg.obj = result;
		handlermsg.sendMessage(msg);


	}




	public void stopThread() {

		Log.e("RTMPControler ", "stopThread");
		
		if (_reminder != null){
			_reminder.timer.cancel();
		}

		if (isPlaying){
			//frameLoaderThread.interrupt();
			GetStream.stopThread();
			frameLoaderThread.Stop();
			boolean flag = true;

			while(flag){


				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (!isPlaying)
					flag = false;

			}



			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//frameLoaderThread.destroy();
		}
	}



	@SuppressLint("ParserError")
	Handler responseHandler = new Handler() {

		private int buffered;
		public int BUFFMAXX = 3000;

		@SuppressLint("ParserError")
		public void handleMessage(Message msg) {

			//String message = (String) msg.obj;
			String message = (String) msg.getData().get(GetStream.STATUS);
			//Log.e("RTMPControler",message);

			if (message.contains("#")) {


				if (isFirstBuffMsg) {
					isFirstBuffMsg = false;
					buffered = 0;

					// start thread if it's not started yet
					if (!isPlaying){

						Log.e("RTMPControler ", "start playing");

						if (frameLoaderThread.getState() == Thread.State.NEW)
							frameLoaderThread.start();
					}


				}

				buffered = Integer.valueOf(message.substring(1));

				int percent = buffered * 100 / BUFFMAXX;


				if ((buffered >= BUFFMAXX)) {
					//stop for a while

					Log.e("RTMPControler ", "start pause");
					pause.doPause(60);
					//downloader.stopThread();
					GetStream.stopThread();
					//
				}

			}

			if (message.equals("Channel is down")) {

				Log.e("msg", "Channel is down");
				Log.e("RTMPControler ", "start pause");
				pause.doPause(60);
				//downloader.stopThread();
				GetStream.stopThread();


			}

		}
	};




	class PauseAction{

		boolean _cancel;

		Camera _cam;
		Handler _callbackToDrow;

		public PauseAction(Camera cam, Handler callbackToDrow){
			_cancel = false;
			_cam = cam;
			_callbackToDrow = callbackToDrow;
		}

		public void cancel(){

			_cancel = true;

		}


		
		public void doPause(int delay){

			//PauseTimer();
			_reminder =	new Reminder(delay);
		}
		
		
		public class Reminder {
		    public Timer timer;

		    public Reminder(int seconds) {
		        timer = new Timer();
		        timer.schedule(new RemindTask(), seconds*1000);
			}

		    class RemindTask extends TimerTask {
		        public void run() {
		            Log.e("tag", "Time's up!%n");
		            //startDownloading(_cam, _callbackToDrow);
		            reStartDownloading(_cam, _callbackToDrow);
		            timer.cancel(); //Terminate the timer thread
		        }
		    }

		    
		}
		
		/*
		
		
		final Timer timer = new Timer();

		private void PauseTimer(){


			//for (int i = 1; i <= 5; i++)  {
			TimerTask task = new TimerTask() {
				@Override
				public void run() {

					Log.e("RTMPControler", "PAUSE out");

					if (!_cancel){

						startDownloading(_cam, _callbackToDrow);
						timer.cancel();

					}
					else{
						timer.cancel();

					}


				}
			};

			Log.e("RTMPControler", "PAUSE in");
			//timer.schedule(task, 300000); //5 minutes
			timer.schedule(task, 60000); //5 minutes

		}
		*/

	}




	//frameLoader frameLoaderThread = new frameLoader();

	class FrameLoader extends Thread {

		public Camera _cam;
		private Handler _callbackToDrow;
		

		boolean stop;

		

		public FrameLoader(Camera cam, Handler callbackToDrow){

			_cam = cam;
			_callbackToDrow = callbackToDrow;
			stop = false;
		}

		public void Stop(){

			stop = true;
		}

		public void run() {

			int cont = 1;


			String mPath = Environment.getExternalStorageDirectory()
					+ "/isWEBCAM/video.flv";

			//FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(mPath);

			try {

				//grabber.start();

				while (!stop) {

					isPlaying = true;

					IplImage frame;
					//Log.e("RTMPControler FrameLoader", _cam.getAddress());

					FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(mPath);

					grabber.start();


					Log.e("Frame Numer", Integer.toString(cont));

					grabber.setFrameNumber(cont);
					// Log.e("ActionPOS","grabber4");

					frame = grabber.grab();

					IplImage frame1 = IplImage.create(frame.width(),
							frame.height(), opencv_core.IPL_DEPTH_8U, 4);


					opencv_imgproc.cvCvtColor(frame, frame1,
							opencv_imgproc.CV_BGR2RGBA);

					//opencv_imgproc.
					IplImage frame2 = IplImage.create(800,
							600, opencv_core.IPL_DEPTH_8U, 4);

					opencv_imgproc.cvResize(frame1, frame2);



					cont = cont + 15;


					// Log.e("ActionPOS","grabber6");
					// Now we make an Android Bitmap with matching size
					// ... Nb. at this point we functionally have 3
					// buffers == image size. Watch your memory usage!

					Bitmap bm = null;
					try{
						bm = Bitmap.createBitmap(frame2.width(),
								frame2.height(), Bitmap.Config.ARGB_8888);
						bm.copyPixelsFromBuffer(frame2.getByteBuffer());
						
						
					}catch(java.lang.OutOfMemoryError e){
						frame = null;
						Log.d("Exception",e.getMessage());
						
					}catch (java.lang.NullPointerException e){

						frame = null;
						Log.d("Exception",e.getMessage());
					}

					if (frame != null) {
						 //Log.e("ActionPOS", "frame not null");

						Message msg = new Message();
						FrameDownloaded framed = new FrameDownloaded(_cam, bm);
						msg.obj = framed;
						_callbackToDrow.sendMessage(msg);
						frame.release();
						

					}else{
						//Log.e("ActionPOS", "frame null");
					}

					frame2.release();
					frame1.release();
					


					grabber.release();


					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				Log.e("RTMPControler","end while");
				isPlaying = false;
				//grabber.release();

			} catch (CvException e){

				Log.d("Exception",e.getMessage());
			}
			catch (java.lang.OutOfMemoryError e){

				Log.d("Exception",e.getMessage());
			}
			catch (Exception e){

				Log.d("Exception",e.getMessage());
			}
			catch (java.lang.NullPointerException e){

				Log.d("Exception",e.getMessage());
			}




		}
	}
}
