package hr.diplomski.drezga.logserviceconsumer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import hr.diplomski.drezga.logservice.ILogService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class LogServiceConsumerActivator implements BundleActivator {
	
	ServiceReference ref = null;
	ServiceReference[] refs = null;
	ILogService logService = null;
	List<ILogService> logServices = null;

	@Override
	public void start(BundleContext ctx) throws Exception {
		ref = ctx.getServiceReference(ILogService.class.getName());
		logService = (ILogService)ctx.getService(ref);
		
		logService.log(ILogService.INFO, "hr.drezga.diplomski", "Hello world!!");
		
		refs = ctx.getServiceReferences(ILogService.class.getName(),null);
		
		if (refs != null && refs.length != 0) {
			logServices = new ArrayList<ILogService>();
			for (int i = 0 ; i < refs.length ; i++)
				logServices.add((ILogService)ctx.getService(refs[i]));
		}
		
		for (ILogService logsvc : logServices)
			logsvc.log(ILogService.INFO, "hr.drezga.diplomski", "Hello world!!");
		
	}

	@Override
	public void stop(BundleContext ctx) throws Exception {
		logService.log(ILogService.INFO, "hr.drezga.diplomski", "Goodbye world!!");
		ctx.ungetService(ref);
		for (ServiceReference r : refs)
			ctx.ungetService(r);
	}

}
