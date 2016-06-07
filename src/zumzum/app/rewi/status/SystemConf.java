package zumzum.app.rewi.status;

import java.io.Serializable;
import java.util.List;

public class SystemConf implements Serializable{

	private List<Camera> _webcamlist;
	private Camera _camera;
	private boolean _mode;
	
	
	public SystemConf(List<Camera> webcamlist,  boolean mode, Camera cam) {
		// TODO Auto-generated constructor stub
		setWebcamlist(webcamlist);
		setModeManual(mode);
		setCameraManual(cam);
	}


	public List<Camera> getWebcamlist() {
		return _webcamlist;
	}


	public void setWebcamlist(List<Camera> _webcamlist) {
		this._webcamlist = _webcamlist;
	}


	public Camera getCameraManual() {
		return _camera;
	}


	public void setCameraManual(Camera _camera) {
		this._camera = _camera;
	}


	public boolean isModeManual() {
		return _mode;
	}


	public void setModeManual(boolean _mode) {
		this._mode = _mode;
	}

}
