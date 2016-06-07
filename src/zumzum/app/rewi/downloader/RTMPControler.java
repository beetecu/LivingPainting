package zumzum.app.rewi.downloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;
import org.opencv.core.CvException;

import zumzum.app.rewi.downloader.RTMPControler.PauseAction.Reminder;
import zumzum.app.rewi.status.Camera;
import zumzum.app.rewi.status.FrameDownloaded;

import com.googlecode.javacv.FFmpegFrameGrabber;
import com.googlecode.javacv.FrameGrabber.Exception;
import com.googlecode.javacv.cpp.opencv_core;
import com.googlecode.javacv.cpp.opencv_imgproc;
import com.googlecode.javacv.cpp.opencv_core.IplImage;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class RTMPControler {

	//int BUFFMAX = 100000;

	FrameLoader frameLoaderThread;
	public boolean isDownloading;

	public static Context _context;


	private boolean isFirstBuffMsg;


	private boolean isPlaying;
	private PauseAction pause;

	RTMPdownload downloader;

	File folder; 

	Reminder _reminder;

	private Handler _handlerCheck;

	private String currentVideo = "video1";
	private boolean reStarting;

	public RTMPControler(Context context){


		_context = context;
		CopyReadAssets("rtmpdump");
		

		downloader = null;

		pause = null;

		isPlaying = false;

		isFirstBuffMsg = true;

		reStarting = true;

		_handlerCheck = null;

		folder = new File(Environment.getExternalStorageDirectory()
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

	public void checkCamOnline(String url, Handler handlermsg){
		
		Log.e("RTMPControler ", "start checkCamOnline");
		
		if (isPlaying){
			GetStream.stopThread();
		}

		
		//Boolean result = isValid(url);

		//new Thread(new CheckCam(url, handlermsg)).start();

		Log.e("RTMPControler ", "end checkCamOnline");

		//if (pause != null)
		//pause.cancel();

		String mPath = Environment.getExternalStorageDirectory()
				+ "/isWEBCAM/"+ getTestVideo() + ".flv";
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

		isFirstBuffMsg = true;

		//reStarting = true;


		new Thread(new GetStream(responseHandler, url,
				_context.getFilesDir(), file)).start();


		_handlerCheck = handlermsg;

	}

	public class CheckCam extends Thread {

		private String _url;


		public CheckCam(String url, Handler handlermsg){

			_url = url;
			_handlerCheck = handlermsg;

		}


		private Handler handlershow = new Handler();

		// private Activity context;
		@Override
		public void run() {
			Log.e("Pos", "checkcam");
			handlershow.post(new Runnable() {

				@Override
				public void run() {

					String mPath = Environment.getExternalStorageDirectory()
							+ "/isWEBCAM/"+ getTestVideo() + ".flv";
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

					isFirstBuffMsg = true;

					reStarting = true;


					new Thread(new GetStream(responseHandler, _url,
							_context.getFilesDir(), file)).start();
				}
			});

		}

	}

	public String getCurrentVideo() {



		String video = currentVideo;



		return video;
	}

	public String getTestVideo() {

		String video="video1";


		if (currentVideo.contains("video1"))
			video = "video2";
		else
			video = "video1";




		return video;
	}

	public void setCurrentVideo() {
		if (currentVideo.contains("video1"))
			currentVideo = "video2";
		else
			currentVideo = "video1";
	}


	public void startDownloading(Camera cam, Handler callbackToDrow){

		cont = 1;

		_reminder = null;
		
		if (!isPlaying){

			//start frames capture
			frameLoaderThread = new FrameLoader(cam, callbackToDrow);
			frameLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);
			frameLoaderThread.setName("frameLoader");

			frameLoaderThread.start();
		}

		if (pause != null)
			pause.cancel();

		pause = new PauseAction(cam, callbackToDrow);



	}

	public void reStartDownloading(Camera cam, Handler callbackToDrow){

		Log.e("RTMPControler", "reStartDownloading---------");

		String mPath = Environment.getExternalStorageDirectory()
				+ "/isWEBCAM/"+ getTestVideo() + ".flv";
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


		reStarting = true;


		new Thread(new GetStream(responseHandler, cam.getAddress(),
				_context.getFilesDir(), file)).start();


		Log.e("RTMPControler", "reStartDownloading---------");

		if (pause != null)
			pause.cancel();

		pause = new PauseAction(cam, callbackToDrow);


	}






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





	public void stopThread() {

		Log.e("RTMPControler ", "stopThread");

		if (_reminder != null){
			_reminder.timer.cancel();
		}

		if (isPlaying){
			//frameLoaderThread.interrupt();
			GetStream.stopThread();
			frameLoaderThread.Stop();
			if (_reminder != null)
				_reminder.timer.cancel();
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
		public int BUFFMAXX = 300000;

		@SuppressLint("ParserError")
		public void handleMessage(Message msg) {

			//String message = (String) msg.obj;
			String message = (String) msg.getData().get(GetStream.STATUS);
			//Log.e("RTMPControler",message);

			if (message.contains("#")) {

				if (isFirstBuffMsg) {
					isFirstBuffMsg = false;

					Log.e("RTMPControler ", "start playing");

					Message mssg = new Message();
					mssg.obj = true;
					//msg.obj = result;
					_handlerCheck.sendMessage(mssg);
					reStarting = false;
					setCurrentVideo();
					cont = 1;

					//if (frameLoaderThread.getState() == Thread.State.NEW)
					//	frameLoaderThread.start();
				}

				if (reStarting){

					Log.e("tag", "reStartinggggggggggggggggggggggggggg---------------------");
					setCurrentVideo();
					reStarting = false;
					cont = 1;


				}

				buffered = Integer.valueOf(message.substring(1));

				int percent = buffered * 100 / BUFFMAXX;


				if ((buffered >= BUFFMAXX)) {
					//stop for a while

					Log.e("RTMPControler ", "start pause");
					pause.doPause(60*10);
					//downloader.stopThread();
					GetStream.stopThread();
					//
				}

			}

			if (message.equals("Channel is down")) {

				if (isFirstBuffMsg){

					Message mssg = new Message();
					mssg.obj = false;
					//msg.obj = result;
					_handlerCheck.sendMessage(mssg);

				}else{

					Log.e("msg", "Channel is downnnnnnnnnnnnnnnnnnnnn");
					Log.e("RTMPControler ", "start pause");
					pause.doPause(60*2);
					//downloader.stopThread();
					GetStream.stopThread();
				}


			}

		}
	};
	public int cont;




	class PauseAction{

		boolean _cancel;

		Camera _cam;
		Handler _callbackToDrow;

		public PauseAction(Camera cam, Handler callbackToDrow){
			_cancel = false;
			_cam = cam;
			_callbackToDrow = callbackToDrow;
			_reminder = null;
		}

		public void cancel(){

			if (_reminder != null)
				_reminder.timer.cancel();

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
					Log.e("tag", "Time's uppppppppppppppppppppppppppppp!%n");
					//startDownloading(_cam, _callbackToDrow);
					reStartDownloading(_cam, _callbackToDrow);
					timer.cancel(); //Terminate the timer thread
					_reminder = null;
				}
			}


		}


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

			cont = 15;


			//FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(mPath);



			//grabber.start();

			while (!stop) {

				try {

					if (!isFirstBuffMsg){

						isPlaying = true;
						String mPath = Environment.getExternalStorageDirectory()
								+ "/isWEBCAM/" + currentVideo +".flv";

						IplImage frame;
						Log.e("RTMPControler FrameLoader", currentVideo);

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



						cont = cont + 50;


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
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else{
						try {
							Thread.sleep(3000);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}





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
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.e("RTMPControler","end while");
			isPlaying = false;
			//grabber.release();

		}
	}
}
