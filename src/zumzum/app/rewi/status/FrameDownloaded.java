package zumzum.app.rewi.status;

import android.graphics.Bitmap;

public class FrameDownloaded {
	
	private Camera _cam;
	private Bitmap _bmp;

	public FrameDownloaded(Camera cam, Bitmap bmp) {
		// TODO Auto-generated constructor stub
		setCamera(cam);
		setImageBmp(bmp);
	}

	public Camera getCamera() {
		return _cam;
	}

	public void setCamera(Camera _cam) {
		this._cam = _cam;
	}

	public Bitmap getImageBmp() {
		return _bmp;
	}

	public void setImageBmp(Bitmap _bmp) {
		this._bmp = _bmp;
	}
	
	

}
