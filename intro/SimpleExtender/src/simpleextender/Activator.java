package simpleextender;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.util.tracker.BundleTracker;

public class Activator implements BundleActivator {
	
	private BundleTracker bt = null;

	@SuppressWarnings("unchecked")
	public void start(BundleContext bundleContext) throws Exception {
		bt = new BundleTracker(bundleContext, Bundle.STARTING, null) {
			@Override
			public Object addingBundle(Bundle bundle, BundleEvent event) {
				String regSvc = (String) bundle.getHeaders().get("Register-Service");
				if (regSvc != null) {
					Class<?> clazz;
					String className = regSvc.split("=")[0];
					String iface = regSvc.split("=")[1];
					try {
						clazz = bundle.loadClass(className);
						try {
							bundle.getBundleContext().registerService(iface, clazz.newInstance(), null);
							System.out.println(iface + " service registered for: " + clazz.getName());
						} catch (InstantiationException e) {
							System.out.println("Could not instantiate " + className);
						} catch (IllegalAccessException e) {
							System.out.println("Illegal access during instatiation of class " + className);
						}
					} catch (ClassNotFoundException e) {
						System.out.println("Could not find class " + className);
					}
				}
				return bundle;
			}
		};
		
		bt.open();
	}

	public void stop(BundleContext bundleContext) throws Exception {
		bt.close();
	}

}
