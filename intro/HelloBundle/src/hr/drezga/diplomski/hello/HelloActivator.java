package hr.drezga.diplomski.hello;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HelloActivator implements BundleActivator {

	Logger log = null;
	
	@Override
	public void start(BundleContext ctx) throws Exception {
		log = LoggerFactory.getLogger("hr.drezga.diplomski");
		log.info("Hello world!!");
	}

	@Override
	public void stop(BundleContext ctx) throws Exception {
		log.info("Goodbye world!");
	}

}
