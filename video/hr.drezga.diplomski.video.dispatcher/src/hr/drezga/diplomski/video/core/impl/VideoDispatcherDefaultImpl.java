package hr.drezga.diplomski.video.core.impl;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.context.BundleContextAware;
import org.springframework.osgi.service.importer.ServiceReferenceProxy;

import hr.drezga.diplomski.video.core.IVideoDispatcher;
import hr.drezga.diplomski.video.core.IVideoHandler;

public class VideoDispatcherDefaultImpl implements IVideoDispatcher, BundleContextAware {

	private final Map<String, List<IVideoHandler>> handlerMap = new ConcurrentHashMap<String, List<IVideoHandler>>();
	private Map<String, DispatchRunnable> threadMap = new ConcurrentHashMap<String,DispatchRunnable>();
	private BundleContext bc;

	class DispatchRunnable extends Thread {
		private BufferedImage img;
		private boolean running;
		private String camera;

		public DispatchRunnable (String camera) {
			this.camera = camera;
		}

		public void run() {
			while (running) {
				try {
					synchronized(this) {
						this.wait();
						long t = new Date().getTime();
						for (IVideoHandler h : handlerMap.get(camera))
							h.proccessFrame(img, t);
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public boolean isRunning() {
			return running;
		}

		public void setRunning(boolean running) {
			this.running = running;
		}

		public void setImg(BufferedImage img) {
			this.img = img;
		}

	}

	@Override
	public void dispatch(BufferedImage img, String camera) {
		DispatchRunnable dr = threadMap.get(camera);
		if (dr == null)
			return;
		synchronized (dr) {
			dr.setImg(img);
			dr.notify();			
		}
	}

	public void registerHandler(ServiceReference sr) {
		String s = sr.getProperty("camera").toString();
		List<IVideoHandler> handlers = handlerMap.get(s);
		if (handlers == null) {
			handlers = new ArrayList<IVideoHandler>();
			handlerMap.put(s, handlers);
			DispatchRunnable dr = new DispatchRunnable(s);
			threadMap.put(s, dr);
			dr.setRunning(true);
			dr.start();
		}
		ServiceReference ref = null;
		if (sr instanceof ServiceReferenceProxy)
			ref = ((ServiceReferenceProxy)sr).getTargetServiceReference();
		else
			ref = sr;

		handlers.add(getHandlerFromService(ref));
	}	

	private IVideoHandler getHandlerFromService(ServiceReference sr) {
		return (IVideoHandler)bc.getService((ServiceReference)sr);
	}

	public void unregisterHandler(ServiceReference sr) {
		String s = sr.getProperty("camera").toString();
		List<IVideoHandler> handlers = handlerMap.get(s);
		if (handlers != null) {
			ServiceReference ref = null;

			if (sr instanceof ServiceReferenceProxy)
				ref = ((ServiceReferenceProxy)sr).getTargetServiceReference();
			else
				ref = sr;

			handlers.remove(getHandlerFromService(ref));
		}
		if (handlers.isEmpty()) {
			handlerMap.remove(s);
			DispatchRunnable dr = threadMap.get(s);
			dr.setRunning(false);
			dr.interrupt();
			threadMap.remove(s);
		}

	}

	@Override
	public void setBundleContext(BundleContext bc) {
		this.bc = bc;
	}
}
