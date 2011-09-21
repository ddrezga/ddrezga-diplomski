package hr.drezga.diplomski.video.core.impl;

import org.osgi.framework.ServiceReference;

public class ServiceListener {
	
	private VideoDispatcherDefaultImpl handlerRegistry;
	
	public void setHandlerRegistry(VideoDispatcherDefaultImpl handlerRegistry) {
		this.handlerRegistry = handlerRegistry;
	}

	public void onBind(ServiceReference sr) throws Exception {
		handlerRegistry.registerHandler(sr);		
	}

	public void onUnbind(ServiceReference sr) throws Exception {
		handlerRegistry.unregisterHandler(sr);
	}}
