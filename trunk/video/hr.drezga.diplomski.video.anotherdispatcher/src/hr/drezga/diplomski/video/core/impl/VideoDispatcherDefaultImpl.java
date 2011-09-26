package hr.drezga.diplomski.video.core.impl;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
import hr.drezga.diplomski.video.core.IVideoProducer;

public class VideoDispatcherDefaultImpl implements IVideoDispatcher, BundleContextAware {

	private final Map<String, List<IVideoHandler>> handlerMap = new ConcurrentHashMap<String, List<IVideoHandler>>();
	private final Map<String, IVideoProducer> producerMap = new ConcurrentHashMap<String, IVideoProducer>();
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
						this.notify();
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
			Graphics g = img.getGraphics();
			g.drawString(DateFormat.getTimeInstance(DateFormat.LONG).format(new Date()), 10, 10);
			dr.setImg(img);
			dr.notify();
			try {
				dr.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private IVideoHandler getHandlerFromService(ServiceReference sr) {
		return (IVideoHandler)bc.getService((ServiceReference)sr);
	}

	private IVideoProducer getProducerFromService(ServiceReference sr) {
		return (IVideoProducer)bc.getService((ServiceReference)sr);
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

		ServiceReference ref = (sr instanceof ServiceReferenceProxy)?((ServiceReferenceProxy)sr).getTargetServiceReference():sr;

		handlers.add(getHandlerFromService(ref));

		IVideoProducer p = producerMap.get(s);
		if (p!=null && !p.isRunning())
			p.startVideo();
	}	

	public void unregisterHandler(ServiceReference sr) {
		String s = sr.getProperty("camera").toString();
		List<IVideoHandler> handlers = handlerMap.get(s);
		if (handlers != null) {

			ServiceReference ref = (sr instanceof ServiceReferenceProxy)?((ServiceReferenceProxy)sr).getTargetServiceReference():sr;

			handlers.remove(getHandlerFromService(ref));
		}
		if (handlers.isEmpty()) {
			handlerMap.remove(s);
			DispatchRunnable dr = threadMap.get(s);
			dr.setRunning(false);
			dr.interrupt();
			threadMap.remove(s);
			IVideoProducer p = producerMap.get(s);
			if (p!=null && p.isRunning())
				p.stopVideo();			
		}
	}

	@Override
	public void setBundleContext(BundleContext bc) {
		this.bc = bc;
	}

	public void registerProducer(ServiceReference sr) {
		String s = sr.getProperty("camera").toString();
		ServiceReference ref = (sr instanceof ServiceReferenceProxy)?((ServiceReferenceProxy)sr).getTargetServiceReference():sr;
		IVideoProducer p = getProducerFromService(ref);
		producerMap.put(s, getProducerFromService(ref));
		p.setDispatcher(this);
	}

	public void unregisterProducer(ServiceReference sr) {
		String s = sr.getProperty("camera").toString();
		ServiceReference ref = (sr instanceof ServiceReferenceProxy)?((ServiceReferenceProxy)sr).getTargetServiceReference():sr;
		IVideoProducer p = getProducerFromService(ref);
		producerMap.remove(s);
		p.setDispatcher(null);
	}
	
	@Override
	public List<String> getProducerList() {
		return new ArrayList<String>(producerMap.keySet());
	}
}
