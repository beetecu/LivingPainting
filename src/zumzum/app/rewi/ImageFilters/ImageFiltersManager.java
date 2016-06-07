package zumzum.app.rewi.ImageFilters;

import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;


import zumzum.app.rewi.status.FrameDownloaded;
import zumzum.app.rewi.status.FrameDownloadedQueue;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class ImageFiltersManager{


	private boolean isProcessing;
	private ImageFilters frameFilterThread;
	public boolean stop;

	public ImageFiltersManager(){

		stop = false;

		isProcessing = false;

	}


	public void stopThread() {

		stop = true;
	}



	public void startFiltering(Handler replyTo, FrameDownloadedQueue frameQueue){

		Log.e("ImageFiltersManager ", "startProcessing");

		frameFilterThread = new ImageFilters(replyTo, frameQueue);
		frameFilterThread.setPriority(Thread.NORM_PRIORITY - 1);
		frameFilterThread.setName("frameLoader");

		// start thread if it's not started yet
		if (frameFilterThread.getState() == Thread.State.NEW)
			frameFilterThread.start();

	}

	public class ImageFilters  extends Thread {

		private Handler _replyTo;

		private FrameDownloadedQueue _frameDownloadedQueue;


		public ImageFilters(Handler replyTo, FrameDownloadedQueue frameQueue){

			_replyTo = replyTo;

			_frameDownloadedQueue = frameQueue;

		}

		private Mat bmp2Mat(Bitmap bmp){

			Mat result = new Mat();

			//Mat imgCanny = new Mat();

			try{

				//if (bmp != null){

				Utils.bitmapToMat(bmp, result); 

				//}

			}catch(Exception e){

				result = null;
			}


			return result;


		}

		//convert bitmap to opencv image
		private Bitmap Mat2bmp(Mat image){

			Bitmap bmp = null;

			try{

				bmp = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888);;

			}catch(Exception e){

				bmp = null;
				e.printStackTrace();
			}

			return bmp;

		}


		private Mat cutImage(Mat image, FrameDownloaded data){

			Mat result = new Mat(); 

			try{

				if( data.getCamera().getWindX() +
						data.getCamera().getWindY() +
						data.getCamera().getWindH() +
						data.getCamera().getWindW() != 0){

					int x = data.getCamera().getWindX() > 0?data.getCamera().getWindX()-1:0;
					int y = data.getCamera().getWindY() > 0?data.getCamera().getWindY()-1:0;
					int w = (int) (data.getCamera().getWindW() > 0?image.size().width - data.getCamera().getWindW():image.size().width);
					int h = (int) (data.getCamera().getWindH() > 0?image.size().height - data.getCamera().getWindH():image.size().height);


					//Log.e("x", String.valueOf(x));
					//Log.e("y", String.valueOf(y));
					//Log.e("w", String.valueOf(w-x));
					//Log.e("h", String.valueOf(h-y));


					Rect roi = new Rect(x, y, w-x, h-y);

					result = new Mat(image, roi);

				}else{

					result = image;
				}
			}catch(Exception e){

				result = null;
			}


			return result;

		}

		private Mat oilFilter(Mat image){

			Mat result = new Mat(); 

			try{

				Mat destImageMat_temp = new Mat(); 


				Imgproc.cvtColor(image, destImageMat_temp, Imgproc.COLOR_RGBA2RGB, 0);

				Imgproc.bilateralFilter(destImageMat_temp, result, 7, 80, 200);

				destImageMat_temp.release();

			}catch(Exception e){

				result = null;
			}

			return result;

		}


		@Override
		public void run() {
			// TODO Auto-generated method stub

			FrameDownloaded data;
			Bitmap bmpTmp;


			while (!stop){

				if (_frameDownloadedQueue.size() != 0){

					try{
						
						data = _frameDownloadedQueue.getFrame2Process();

						//Log.e("ImageFiltersManager ", "run2");

						bmpTmp = data.getImageBmp();

						/*
						//Log.e("ImageFiltersManager ", "run3");

						Mat img = bmp2Mat(bmpTmp);

						//Log.e("ImageFiltersManager ", "run4");

						Mat filteredImg = new Mat();

						if (data.getCamera().getEffect().contains("oil")){

							filteredImg = oilFilter(cutImage(img, data));
						}

						//Log.e("ImageFiltersManager ", "run5");
						 
						 */

						Message msg = new Message();
						data.setImageBmp(bilateral(bmpTmp, data));
						//data.setImageBmp(bmpTmp);
						Log.e("ImageFiltersManager ", "run1");
						msg.obj = data; //Mat2bmp(filteredImg);
						_replyTo.sendMessage(msg);

						//img.release();
						//filteredImg.release();

					}catch(Exception e){

						e.printStackTrace();
					}

				}
				else{

					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}

		}


	}

	private Bitmap bilateral(Bitmap img, FrameDownloaded data){

		Bitmap bmp=null;

			try{
				Mat mRgba = new Mat();
				Mat destImageMat_temp = new Mat(); 
				Mat dst = new Mat();
				Mat result;
				int MAX_KERNEL_LENGTH=31;

				Utils.bitmapToMat(img, mRgba); 



				Imgproc.cvtColor(mRgba, destImageMat_temp, Imgproc.COLOR_RGBA2RGB, 0);

				Imgproc.bilateralFilter(destImageMat_temp, dst, 7, 80, 200);
				
				if( data.getCamera().getWindX() +
						data.getCamera().getWindY() +
						data.getCamera().getWindH() +
						data.getCamera().getWindW() != 0){

					int x = data.getCamera().getWindX() > 0?data.getCamera().getWindX()-1:0;
					int y = data.getCamera().getWindY() > 0?data.getCamera().getWindY()-1:0;
					int w = (int) (data.getCamera().getWindW() > 0?dst.size().width - data.getCamera().getWindW():dst.size().width);
					int h = (int) (data.getCamera().getWindH() > 0?dst.size().height - data.getCamera().getWindH():dst.size().height);


					//Log.e("x", String.valueOf(x));
					//Log.e("y", String.valueOf(y));
					//Log.e("w", String.valueOf(w-x));
					//Log.e("h", String.valueOf(h-y));


					Rect roi = new Rect(x, y, w-x, h-y);

					result = new Mat(dst, roi);

				}else{

					result = dst;
				}
				

				 //dst = cutImage(destImageMat_temp, data);
				
				bmp = Bitmap.createBitmap(result.cols(), result.rows(), Bitmap.Config.ARGB_8888);



				Utils.matToBitmap(result, bmp);
				//Utils.matToBitmap(mRgba, bmp);

				

				destImageMat_temp.release();
				mRgba.release();
				dst.release();


			}catch (CvException e){
				Log.d("Exception",e.getMessage());
			}
			catch (java.lang.OutOfMemoryError e){
				Log.d("Exception",e.getMessage());
			}
			
			return bmp;
		}


	private Mat cutImage(Mat image, FrameDownloaded data) {
		// TODO Auto-generated method stub
		Mat result = new Mat(); 

		try{

			if( data.getCamera().getWindX() +
					data.getCamera().getWindY() +
					data.getCamera().getWindH() +
					data.getCamera().getWindW() != 0){

				int x = data.getCamera().getWindX() > 0?data.getCamera().getWindX()-1:0;
				int y = data.getCamera().getWindY() > 0?data.getCamera().getWindY()-1:0;
				int w = (int) (data.getCamera().getWindW() > 0?image.size().width - data.getCamera().getWindW():image.size().width);
				int h = (int) (data.getCamera().getWindH() > 0?image.size().height - data.getCamera().getWindH():image.size().height);


				//Log.e("x", String.valueOf(x));
				//Log.e("y", String.valueOf(y));
				//Log.e("w", String.valueOf(w-x));
				//Log.e("h", String.valueOf(h-y));


				Rect roi = new Rect(x, y, w-x, h-y);

				result = new Mat(image, roi);

			}else{

				result = image;
			}
		}catch(Exception e){

			result = null;
		}


		return result;

	}


		



	

}
