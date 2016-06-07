package zumzum.app.rewi.status;

import java.util.Stack;

import android.graphics.Bitmap;
import android.util.Log;

// stores list of photos to download
public class BufferFrames2Show {
	private Stack<FrameDownloaded> frame2show = new Stack<FrameDownloaded>();

	// removes all instances of this ImageView
	public void Clean() {
		for (int j = 0; j < frame2show.size();) {
			if (frame2show.size() < j)
				frame2show.remove(j);
			else
				++j;
		}
	}

	public void removeAll() {
		frame2show.removeAllElements();
	}
	
	public void insertFrame(FrameDownloaded frame){
		
		//Log.e("FrameDownloadedQueue","insertFram");
		frame2show.add(frame);// .push(frame);
		
		
	}
	

	public FrameDownloaded getFrame2show(){
		
		FrameDownloaded frame = null;
		
		if (frame2show.size() != 0)
			
			Log.e("list frames", Integer.toString(frame2show.size()));
		
			frame = frame2show.firstElement();//.pop();
			frame2show.remove(0);
			
			//zumzum.app.rewi.dataset.DatasetManager.updateCurrentCam(frame.getCamera().getId());
		
		return frame;
		
		
	}
	
	public int size(){
		
		return frame2show.size();
		
	}
}