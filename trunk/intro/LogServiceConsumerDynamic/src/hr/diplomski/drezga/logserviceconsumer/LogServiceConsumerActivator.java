package hr.diplomski.drezga.logserviceconsumer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import hr.diplomski.drezga.logservice.ILogService;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceReference;

public class LogServiceConsumerActivator implements BundleActivator {
	
	private List<ILogService> logServices = new ArrayList<ILogService>();
	private LogServiceListener lsLsnr = null;

	@Override
	public void start(BundleContext ctx) throws Exception {
		lsLsnr = new LogServiceListener(ctx, logServices);
		String filter = "(objectclass=" + ILogService.class.getName() + ")";

		ctx.addServiceListener(lsLsnr, filter);
		ServiceReference[] srl = ctx.getServiceReferences(null, filter);
		for(int i = 0; srl != null && i < srl.length; i++)
			lsLsnr.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED,srl[i])); 

		for (ILogService ls : logServices)
			ls.log(ILogService.INFO, "hr.drezga.diplomski", "Hello world!!");
	}

	@Override
	public void stop(BundleContext ctx) throws Exception {
		for (ILogService ls : logServices)
			ls.log(ILogService.INFO, "hr.drezga.diplomski", "Goodbye world!!");
	}

}
