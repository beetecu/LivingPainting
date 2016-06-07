package zumzum.app.rewi.status;

import java.util.Stack;

import android.util.Log;

// stores list of photos to download
public class FrameDownloadedQueue {
	private Stack<FrameDownloaded> frame2process = new Stack<FrameDownloaded>();

	// removes all instances of this ImageView
	public void Clean() {
		for (int j = 0; j < frame2process.size();) {
			if (frame2process.size() < j)
				frame2process.remove(j);
			else
				++j;
		}
	}

	public void removeAll() {
		frame2process.removeAllElements();
	}
	
	public void insertFrame(FrameDownloaded frame){
		
		//Log.e("FrameDownloadedQueue","insertFram");
		frame2process.add(frame);
		
		
	}
	

	public FrameDownloaded getFrame2Process(){
		
		FrameDownloaded frame = null;
		
		if (frame2process.size() != 0)
		
			frame = frame2process.firstElement();//.pop();
			frame2process.remove(0);
		
		return frame;
		
		
	}
	
	public int size(){
		
		return frame2process.size();
		
	}
}