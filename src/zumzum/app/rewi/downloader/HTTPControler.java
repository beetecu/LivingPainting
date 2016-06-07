package zumzum.app.rewi.downloader;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import zumzum.app.rewi.status.Camera;
import zumzum.app.rewi.status.FrameDownloaded;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class HTTPControler{

	FrameLoader frameLoaderThread;
	public boolean isDownloading;

	public HTTPControler(){

		isDownloading = false;

	}

	public void startDownloading(Camera cam, Handler callbackToDrow){

		Log.e("HTTPControler ", "startDownloading");

		frameLoaderThread = new FrameLoader(cam, callbackToDrow);
		frameLoaderThread.setPriority(Thread.NORM_PRIORITY - 1);
		frameLoaderThread.setName("frameLoader");

		// start thread if it's not started yet
		if (frameLoaderThread.getState() == Thread.State.NEW)
			frameLoaderThread.start();

	}

	public void checkCamOnline(String url, Handler handlermsg){

		Log.e("HTTPControler ", "checkCamOnline");

		Bitmap bmp = isValid(url);


		boolean result = false;

		if (bmp != null){



			result = true; 
		}

		bmp = null;

		Message msg = new Message();
		msg.obj = result;
		//msg.obj = result;
		handlermsg.sendMessage(msg);

	}


	private Bitmap isValid(String url){

		Bitmap bresult = null;

		try {
			final URL url_value = new URL(url);


			// Read data with timeout
			Callable<Bitmap> readTask = new Callable<Bitmap>() {
				@Override
				public Bitmap call() throws Exception {
					//Log.e("GetStream", "Pos5");

					Bitmap result = null;
					final BitmapFactory.Options options = new BitmapFactory.Options();

					// Calculate inSampleSize
					options.inSampleSize = calculateInSampleSize(options, 1280, 800);

					// Decode bitmap with inSampleSize set
					options.inJustDecodeBounds = false;
					try {

						InputStream stream = url_value.openConnection().getInputStream();
						stream = new DoneHandlerInputStream(stream);

						result = BitmapFactory.decodeStream(stream, null, options);
						Log.e("HTTPManager", "isValid");

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						result = null;
					}
					catch (java.lang.OutOfMemoryError e){
						// TODO Auto-generated catch block
						e.printStackTrace();
						result = null;
					}
					return result ;
				}
			};

			ExecutorService executor = Executors.newFixedThreadPool(2);
			Bitmap readByte = null;


			Future<Bitmap> future = executor.submit(readTask);

			try {
				readByte = future.get(20000, TimeUnit.MILLISECONDS);


			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				readByte = null;
			} catch (ExecutionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				readByte = null;
			} catch (TimeoutException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				readByte = null;
			}

			if (readByte != null){

				Log.e("Action","checkHist");


				//Mat imgCanny = new Mat();


				try{

					bresult = readByte;
					readByte = null;


				}catch (java.lang.OutOfMemoryError e){

					Log.d("Exception",e.getMessage());
				}
				catch (java.lang.RuntimeException e){

					Log.d("Exception",e.getMessage());

				}
			} 



			//Log.e("Action","imgdownlod");

		} catch (MalformedURLException e2) {

			Log.d("Exception",e2.getMessage());


		}catch (java.lang.OutOfMemoryError e){

			Log.d("Exception",e.getMessage());

		}
		catch (java.lang.NullPointerException e){

			Log.d("Exception",e.getMessage());

		}





		return bresult;

	}


	final class DoneHandlerInputStream extends FilterInputStream {
		private boolean done;

		public DoneHandlerInputStream(InputStream stream) {
			super(stream);
		}

		@Override public int read(byte[] bytes, int offset, int count) throws IOException {
			if (!done) {
				int result = super.read(bytes, offset, count);
				if (result != -1) {
					return result;
				}
			}
			done = true;
			return -1;
		}
	}


	private Bitmap getBitmap1(String path) throws Exception {

		//Uri uri = getImageUri(path);
		final URL url_value = new URL(path);
		InputStream in = null;
		try {
			final int IMAGE_MAX_SIZE = 200000; // 1.2MP
			//in = mContentResolver.openInputStream(uri);

			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			//BitmapFactory.decodeStream(in, null, o);
			BitmapFactory.decodeStream(url_value
					.openConnection().getInputStream(), null, o);
			//in.close();

			int scale = 1;
			while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) > 
			IMAGE_MAX_SIZE) {
				scale++;
			}
			//Log.d("TAG2, "scale = " + scale + ", orig-width: " + o.outWidth + " orig-height: " + o.outHeight);

			Bitmap b = null;
			//in = mContentResolver.openInputStream(uri);
			if (scale > 1) {
				scale--;
				// scale to max possible inSampleSize that still yields an image
				// larger than target
				o = new BitmapFactory.Options();
				o.inSampleSize = scale;
				b = BitmapFactory.decodeStream(url_value
						.openConnection().getInputStream(), null, o);

				// resize to desired dimensions
				int height = b.getHeight();
				int width = b.getWidth();
				//Log.d(TAG, "1th scale operation dimenions - width: " + width + "height: " + height);

				double y = Math.sqrt(IMAGE_MAX_SIZE
						/ (((double) width) / height));
				double x = (y / height) * width;

				Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x, 
						(int) y, true);
				b.recycle();
				b = scaledBitmap;

				System.gc();
			} else {
				b = BitmapFactory.decodeStream(in);
			}
			//in.close();

			//Log.d(TAG, "bitmap size - width: " +b.getWidth() + ", height: " + b.getHeight());
			return b;
		} catch (IOException e) {
			Log.e("TAG", e.getMessage(),e);
			return null;
		}
	}




	private Bitmap getBitmap(String url){

		Bitmap bresult = null;

		try {
			final URL url_value = new URL(url);


			// Read data with timeout
			Callable<Bitmap> readTask = new Callable<Bitmap>() {
				@Override
				public Bitmap call() throws Exception {
					//Log.e("GetStream", "Pos5");

					//Uri uri = getImageUri(path);
					//final URL url_value = new URL(path);
					InputStream in = null;
					try {
						final int IMAGE_MAX_SIZE = 200000; // 1.2MP
						//in = mContentResolver.openInputStream(uri);

						// Decode image size
						BitmapFactory.Options o = new BitmapFactory.Options();
						o.inJustDecodeBounds = true;
						//BitmapFactory.decodeStream(in, null, o);
						BitmapFactory.decodeStream(url_value
								.openConnection().getInputStream(), null, o);
						//in.close();

						int scale = 1;
						while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) > 
						IMAGE_MAX_SIZE) {
							scale++;
						}
						//Log.d("TAG2, "scale = " + scale + ", orig-width: " + o.outWidth + " orig-height: " + o.outHeight);

						Bitmap b = null;
						//in = mContentResolver.openInputStream(uri);
						if (scale > 1) {
							scale--;
							// scale to max possible inSampleSize that still yields an image
							// larger than target
							o = new BitmapFactory.Options();
							o.inSampleSize = scale;
							b = BitmapFactory.decodeStream(url_value
									.openConnection().getInputStream(), null, o);

							// resize to desired dimensions
							int height = b.getHeight();
							int width = b.getWidth();
							//Log.d(TAG, "1th scale operation dimenions - width: " + width + "height: " + height);

							double y = Math.sqrt(IMAGE_MAX_SIZE
									/ (((double) width) / height));
							double x = (y / height) * width;

							Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x, 
									(int) y, true);
							b.recycle();
							b = scaledBitmap;

							System.gc();
						} else {
							b = BitmapFactory.decodeStream(in);
						}
						//in.close();

						//Log.d(TAG, "bitmap size - width: " +b.getWidth() + ", height: " + b.getHeight());
						return b;
					} catch (IOException e) {
						Log.e("TAG", e.getMessage(),e);
						return null;
					}
				}
			};

			ExecutorService executor = Executors.newFixedThreadPool(2);
			Bitmap readByte = null;


			Future<Bitmap> future = executor.submit(readTask);

			try {
				readByte = future.get(20000, TimeUnit.MILLISECONDS);


			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				readByte = null;
			} catch (ExecutionException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				readByte = null;
			} catch (TimeoutException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				readByte = null;
			}

			if (readByte != null){

				Log.e("Action","checkHist");


				//Mat imgCanny = new Mat();


				try{

					bresult = readByte;
					readByte = null;

				}catch (java.lang.OutOfMemoryError e){

					Log.d("Exception",e.getMessage());
				}
				catch (java.lang.RuntimeException e){

					Log.d("Exception",e.getMessage());

				}
			} 



			//Log.e("Action","imgdownlod");

		} catch (MalformedURLException e2) {

			Log.d("Exception",e2.getMessage());


		}catch (java.lang.OutOfMemoryError e){

			Log.d("Exception",e.getMessage());

		}
		catch (java.lang.NullPointerException e){

			Log.d("Exception",e.getMessage());

		}






		return bresult;

	}

	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Raw height and width of image
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) > reqHeight
					&& (halfWidth / inSampleSize) > reqWidth) {
				inSampleSize *= 2;
			}
		}

		return inSampleSize;
	}



	public void stopThread() {

		if (isDownloading){
			//frameLoaderThread.interrupt();
			frameLoaderThread.Stop();
			boolean flag = true;

			while(flag){


				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				if (!isDownloading)
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


	//frameLoader frameLoaderThread = new frameLoader();

	class FrameLoader extends Thread {

		public Camera _cam;
		private Handler _callbackToDrow;
		private Bitmap bmp;

		boolean stop;

		private final long startTimeLong = 15*60*1000;
		private final long startTimeShort = 5*60*1000;

		private final long interval = 1000;
		public MalibuCountDownTimer countDownTimer;

		public boolean statusLong = true;


		public FrameLoader(Camera cam, Handler callbackToDrow){

			_cam = cam;
			_callbackToDrow = callbackToDrow;
			stop = false;

			statusLong = true;

			countDownTimer = new MalibuCountDownTimer(startTimeLong, interval);
			countDownTimer.start();

		}

		public void Stop(){

			stop = true;
			countDownTimer.cancel();
		}

		public void run() {


			while (!stop) {

				try {

					if (statusLong){

						isDownloading = true;

						Log.e("HTTPControler ", _cam.getAddress());

						bmp = null;

						bmp = getBitmap(_cam.getAddress());

						if (bmp!= null){

							Message msg = new Message();
							FrameDownloaded frame = new FrameDownloaded(_cam, bmp);
							msg.obj = frame;
							_callbackToDrow.sendMessage(msg);
						}



						Thread.sleep(3000);




					}else
					{

						Thread.sleep(2000);
					}


					//bmp.recycle();

				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			Log.e("HTTPControler ", "STOPPPPPP");
			isDownloading = false;
		}
	}


	// CountDownTimer class
	public class MalibuCountDownTimer extends CountDownTimer
	{


		public MalibuCountDownTimer(long startTime, long interval)
		{
			super(startTime, interval);
		}

		@Override
		public void onFinish()
		{
			Log.e("TAG", "Time's up!");
			if (frameLoaderThread.statusLong){

				Log.e("TAG", "statusLong");

				cancel();
				frameLoaderThread.statusLong = false;

				frameLoaderThread.countDownTimer = new MalibuCountDownTimer(frameLoaderThread.startTimeShort, 1000);
				frameLoaderThread.countDownTimer.start();

			}
			else{

				Log.e("TAG", "statusShort");

				cancel();
				frameLoaderThread.statusLong = true;

				frameLoaderThread.countDownTimer = new MalibuCountDownTimer(frameLoaderThread.startTimeLong, 1000);
				frameLoaderThread.countDownTimer.start();
			}

			//timeElapsedView.setText("Time Elapsed: " + String.valueOf(startTime));
		}

		@Override
		public void onTick(long millisUntilFinished)
		{
			//text.setText("Time remain:" + millisUntilFinished);
			//timeElapsed = startTime - millisUntilFinished;
			//timeElapsedView.setText("Time Elapsed: " + String.valueOf(timeElapsed));
		}
	}



}
