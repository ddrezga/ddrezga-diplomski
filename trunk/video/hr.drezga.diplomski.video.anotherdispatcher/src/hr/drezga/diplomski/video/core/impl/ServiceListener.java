package hr.drezga.diplomski.video.core.impl;

import org.osgi.framework.ServiceReference;

public class ServiceListener {
	
	private VideoDispatcherDefaultImpl videoRegistry;
	
	public void setHandlerRegistry(VideoDispatcherDefaultImpl handlerRegistry) {
		this.videoRegistry = handlerRegistry;
	}

	public void onBindHandler(ServiceReference sr) throws Exception {
		videoRegistry.registerHandler(sr);		
	}

	public void onUnbindHandler(ServiceReference sr) throws Exception {
		videoRegistry.unregisterHandler(sr);
	}

	public void onBindProducer(ServiceReference sr) throws Exception {
		videoRegistry.registerProducer(sr);		
	}

	public void onUnbindProducer(ServiceReference sr) throws Exception {
		videoRegistry.unregisterProducer(sr);
	}
}
