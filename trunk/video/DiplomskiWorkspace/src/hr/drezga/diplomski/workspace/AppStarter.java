package hr.drezga.diplomski.workspace;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.osgi.framework.BundleContext;
import org.springframework.osgi.context.BundleContextAware;

public class AppStarter implements BundleContextAware {
	
	IApplication app;
	BundleContext bc;

	@Override
	public void setBundleContext(BundleContext bc) {
		this.bc = bc;
	}
	
	public void setApp(IApplication app) {
		this.app = app;
	}
	
	public void start() {
		new Thread(){
			public void run() {
				try {
					app.start(null);
				} catch (Exception e) {
					app.stop();
				}
			};
		}.start();
	}
	
	public void stop() {
		app.stop();
	}
}
