package hr.drezga.diplomski.video.USBCamera;

import hr.drezga.diplomski.video.core.IVideoDispatcher;
import hr.drezga.diplomski.video.core.IVideoProducer;

import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;

import de.humatic.dsj.DSCapture;
import de.humatic.dsj.DSFilterInfo;
import de.humatic.dsj.DSFiltergraph;

public class USBCameraImpl implements IVideoProducer {
	
	private volatile IVideoDispatcher dispatcher;
	private volatile boolean running;
	private int height;
	private int width;
	private Thread videoThread;
	private Integer videoIndex;
	
	
	DSCapture ds;
	
	@Override
	public void startVideo() {
		DSFilterInfo[][] vidCaptures = DSCapture.queryDevices();
		PropertyChangeListener pcl = new PropertyChangeListener() {
			@Override
			public void propertyChange(PropertyChangeEvent evt) {
				System.out.println(evt);
			}
		};
		ds = new DSCapture(DSFiltergraph.CAPTURE, vidCaptures[0][videoIndex], false, DSFilterInfo.doNotRender(), pcl);

		ds.setSize(width, height);
		ds.setRate(25);
		running = true;

		videoThread  = new Thread(){
			@Override
			public void run() {
				while (running && dispatcher != null){
						dispatcher.dispatch(ds.getImage(), "CAMERA_" + videoIndex);
					try {
						Thread.sleep(40);
					} catch (InterruptedException e) {}
				}
			}
		};
		
		videoThread.start();
	}

	@Override
	public void stopVideo() {

		if (ds != null)
			ds.stop();
		
		if (videoThread.isAlive())
			synchronized (videoThread) {
				dispatcher = null;
				running = false;
				videoThread.notify();
			}
		
		if (ds != null)
			ds.dispose();
		ds = null;
	}

	@Override
	public boolean isRunning() {
		return running;
	}

	@Override
	public void setDispatcher(IVideoDispatcher dispatcher) {
		this.dispatcher = dispatcher;
		if (dispatcher == null)
			stopVideo();
	}
	
	public void setHeight(int height) {
		this.height = height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setVideoIndex(Integer videoIndex) {
		this.videoIndex = videoIndex;
	}

	public Integer getVideoIndex() {
		return videoIndex;
	}

	@Override
	public String getId() {
		return "CAMERA_" + videoIndex;
	}

}
