package hr.diplomski.drezga.logserviceconsumer;

import hr.diplomski.drezga.logservice.ILogService;

import java.util.ArrayList;
import java.util.List;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;

public class LogServiceListener implements ServiceListener {
	
	private BundleContext bc = null;
	private List<ILogService> logsvcs;
	
	public LogServiceListener(BundleContext bc, List<ILogService> lsList) {
		this.bc = bc;
		logsvcs = lsList;
	}

	@Override
	public void serviceChanged(ServiceEvent se) {
		switch (se.getType()) {
		case ServiceEvent.REGISTERED:
			logsvcs.add((ILogService)bc.getService(se.getServiceReference()));
			break;
		case ServiceEvent.UNREGISTERING:
			logsvcs.remove((ILogService)bc.getService(se.getServiceReference()));
		default:
			break;
		}
	}
}
