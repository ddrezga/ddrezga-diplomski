package hr.diplomski.drezga.logservice.impl;

import hr.diplomski.drezga.logservice.ILogService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class LogServiceActivator implements BundleActivator {

	ILogService logService = null;
	ServiceRegistration sr = null;
	
	@Override
	public void start(BundleContext ctx) throws Exception {
		logService = new LogServiceImpl();
		sr = ctx.registerService(ILogService.class.getName(), logService, null);
	}

	@Override
	public void stop(BundleContext ctx) throws Exception {
		sr.unregister();
	}

}
