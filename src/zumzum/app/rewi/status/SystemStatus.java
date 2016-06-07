package zumzum.app.rewi.status;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import zumzum.app.rewi.downloader.DownloaderManager;

import zumzum.app.rewi.R;
import zumzum.app.rewi.ImageFilters.ImageFiltersManager;
import zumzum.app.rewi.dataset.DatasetManager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class SystemStatus {

	private static boolean _newConfiguration;

	private DatasetManager _datasetManager;

	SystemConf _conf;

	private Camera _currentcam;
	private boolean _currentModeManual;

	private int _currentAddressindex;

	FrameDownloadedQueue _frameDownloadedQueue;

	BufferFrames2Show _bufferFrames2Show;

	DownloaderManager _downloaderManager;

	private ImageFiltersManager _imageFiltersManager;

	Context _context;

	boolean _running;

	protected boolean emptylist;

	protected Reminder _reminder;

	Timer t;

	public SystemStatus(Context context, BufferFrames2Show bufferFrames2Show) {

		_datasetManager = new DatasetManager(context, confHandler);
		_newConfiguration = false;
		_conf = null;
		_currentModeManual = false;
		_currentAddressindex = 0;
		_context = context;
		_frameDownloadedQueue = new FrameDownloadedQueue();
		_downloaderManager = new DownloaderManager(camStatusMsg, _context);
		_bufferFrames2Show = bufferFrames2Show;
		// _bufferFrames2Show = new BufferFrames2Show();

		_imageFiltersManager = new ImageFiltersManager();
		_imageFiltersManager.startFiltering(newFrame, _frameDownloadedQueue);

		_running = false;

		emptylist = false;

		_reminder = null;

		t = null;

		keepUpdatedDatasetConfiguration();

	}

	public boolean isNewConfiguration() {
		return _newConfiguration;
	}

	public void setNewConfiguration(final boolean newConfiguration) {
		_newConfiguration = newConfiguration;
	}

	// falta poner el stop
	private void keepUpdatedDatasetConfiguration() {

		// Declare the timer
		t = new Timer();
		// Set the schedule function and rate
		t.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				Log.e("POS", "keepUpdatedDatasetConfiguration");
				zumzum.app.rewi.dataset.DatasetManager.readSystemConf();
			}

		},
		// Set how long before to start calling the TimerTask (in milliseconds)
		0,
		// Set the amount of time between each execution (in
		// milliseconds)
		1000 * 60 * 60);
	}

	public Camera getCurrentcam() {
		return _currentcam;
	}

	public void setCurrentcam(Camera _currentcam) {

		// envia un mensaje avisando que se ha cambiado la camara
		this._currentcam = _currentcam;
		Log.e("current cam", _currentcam.getAddress());
		_downloaderManager.checkCam(this.getCurrentcam());

	}

	Handler confHandler = new Handler() {
		public void handleMessage(Message msg) {

			// Log.e("Pos", " confHandler");

			SystemConf configuration = (SystemConf) msg.getData().get(
					DatasetManager.CONFIGURATION);

			if (configuration != null) {

				// checking for new configuration
				if (_conf == null) {

					Log.e("_newConfiguration ", "trueeee");

					_newConfiguration = true;
					_currentModeManual = configuration.isModeManual();

				}

				else {

					if (_conf.isModeManual() != configuration.isModeManual()) {

						_currentModeManual = configuration.isModeManual();

						Log.e("_newConfiguration ", "true1");

						_newConfiguration = true;

					}
					if (!_conf
							.getCameraManual()
							.getAddress()
							.contains(
									configuration.getCameraManual()
									.getAddress())) {

						Log.e("_newConfiguration ", "true2");

						if (configuration.isModeManual()) {

							emptylist = true;

							setCurrentcam(configuration.getCameraManual());

							if (_reminder != null) {
								_reminder.timer.cancel();
							}
						}

					}

				}
				_conf = configuration;

				if (_newConfiguration) {

					changeMode();
					_newConfiguration = false;
				}
			} else {
				Log.e("ERROR ", "error reading dataset");
			}

		};
	};

	private void changeMode() {

		Log.e("Pos", "changeMode");

		if (_reminder != null) {
			_reminder.timer.cancel();
		}

		if (_currentModeManual) {

			Log.e("System Status", "Mode Manual");

			_frameDownloadedQueue.removeAll();
			_bufferFrames2Show.removeAll();

			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// add image inicial
			Bitmap bm = BitmapFactory.decodeResource(_context.getResources(),
					R.drawable.livingw);
			FrameDownloaded frameinic = new FrameDownloaded(
					this.getCurrentcam(), bm);
			_bufferFrames2Show.insertFrame(frameinic);
			setCurrentcam(_conf.getCameraManual());

		} else {
			// select webcam from list
			Log.e("System Status", "Mode Automatic");
			_currentModeManual = false;
			selectCam();

		}

	}

	public void selectCam(){

		boolean flag= true;

		while (flag){


			this._currentAddressindex++;

			if (this._currentAddressindex > this._conf.getWebcamlist().size() -1) this._currentAddressindex=0;

			try{

				if (this._conf.getWebcamlist().get(this._currentAddressindex).getAddress().contains("rtmp")){

					String time_in  = this._conf.getWebcamlist().get(this._currentAddressindex).getTime_in().get(0);
					String time_out = this._conf.getWebcamlist().get(this._currentAddressindex).getTime_out().get(0);




					if (this.isOnTime(time_in, time_out)){


						flag = false;
					}
				}
			}catch(Exception e){
				this._currentAddressindex=0;
				flag = false;
			}

		}




		Log.e("URL random",this._conf.getWebcamlist().get(this._currentAddressindex).getAddress());
		this.setCurrentcam(this._conf.getWebcamlist().get(this._currentAddressindex));



	}

	public boolean isOnTime(String time_in, String time_out) {

		boolean result = false;

		try {

			SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");

			df.setTimeZone(TimeZone.getTimeZone("gmt"));
			String gmtTime = df.format(new Date());

			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			Date date_in, date_out, current_date;
			try {
				// //Log.e("pos", "pos1");
				date_in = sdf.parse(time_in);
				// //Log.e("pos", "pos2");
				date_out = sdf.parse(time_out);
				// //Log.e("pos", "pos3");
				current_date = sdf.parse(gmtTime);
				// //Log.e("pos", "pos4");

				int res = date_in.compareTo(date_out);

				if (res < 0) {

					if ((date_in.compareTo(current_date) < 0)
							&& (current_date.compareTo(date_out) < 0)) {

						result = true;
					}
				}
				if (res > 0) {

					if (current_date.compareTo(date_out) <= 0) {

						result = true;
					}

					if ((current_date.compareTo(date_in) >= 0)) {

						result = true;
					}

				}
				if (res == 0) {

					result = true;

				}

			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				result = true;
			}

		} catch (Exception e) {

			result = false;
		}

		return result;

	}

	Handler camStatusMsg = new Handler() {
		public void handleMessage(Message msg) {

			if (msg.what == 1) {

				Log.e("SystemStatus", "camStatusMsg");

				boolean status = (Boolean) msg.obj;

				if (!status) {

					Log.e("SystemStatus", "camara caida");

					if (!_currentModeManual) {

						selectCam();

					}

				} else {

					// update database
					// zumzum.app.rewi.dataset.DatasetManager.updateCurrentCam(_currentcam.getId());

					if (!_currentModeManual) {

						try {
							// Thread.sleep(_currentcam.getRefrestime());

							if (!_running) {
								// _reminder = new
								// Reminder(_currentcam.getRefrestime());
								Log.e("TAG", "reminderrrrrrr");
								_reminder = new Reminder(3 * 60);
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}

				}

			}

			if (msg.what == 2) {

				// Log.e("Pos", "new frame");
				// zumzum.app.rewi.dataset.DatasetManager.updateCurrentCam(_currentcam.getId());
				String address = ((FrameDownloaded) msg.obj).getCamera()
						.getAddress();
				// if (address.contains(_currentcam.getAddress()))
				_frameDownloadedQueue.insertFrame((FrameDownloaded) msg.obj);

				if (emptylist) {

					_frameDownloadedQueue.removeAll();
					_bufferFrames2Show.removeAll();

					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// add image inicial
					Bitmap bm = BitmapFactory.decodeResource(
							_context.getResources(), R.drawable.livingw);
					FrameDownloaded frameinic = new FrameDownloaded(
							getCurrentcam(), bm);
					_bufferFrames2Show.insertFrame(frameinic);

					emptylist = false;

				}

				// _bufferFrames2Show.insertFrame((FrameDownloaded) msg.obj);
			}

		};
	};

	/*
	 * final int delay = 5000; final int period = 1000; final Runnable planner =
	 * new Runnable() { public void run() {
	 * //Toast.makeText(getApplicationContext
	 * (),"RUN!",Toast.LENGTH_SHORT).show(); postDelayed(this, period); } };
	 * 
	 * 
	 * postDelayed(planner, delay);
	 */

	public class Reminder {
		public Timer timer;

		public Reminder(long seconds) {
			timer = new Timer();
			timer.schedule(new RemindTask(), seconds * 1000);
		}

		class RemindTask extends TimerTask {
			public void run() {
				Log.e("tag", "Time's uppppppppppppppppppppppppppppp!%n");
				// startDownloading(_cam, _callbackToDrow);
				selectCam();
				timer.cancel(); // Terminate the timer thread
				_reminder = null;
			}
		}

	}

	Handler newFrame = new Handler() {
		public void handleMessage(Message msg) {

			Log.e("Pos", "new frame to show");
			// zumzum.app.rewi.dataset.DatasetManager.updateCurrentCam(_currentcam.getId());

			String address = ((FrameDownloaded) msg.obj).getCamera()
					.getAddress();
			// if (address.contains(_currentcam.getAddress()))
			_bufferFrames2Show.insertFrame((FrameDownloaded) msg.obj);

		};
	};

	public void doStop() {

		if (t != null) {
			t.cancel();
		}

		if (_reminder != null) {
			_reminder.timer.cancel();
		}

		_frameDownloadedQueue.removeAll();
		_bufferFrames2Show.removeAll();

		_imageFiltersManager.stopThread();

		_downloaderManager.doStop();

	}

}
