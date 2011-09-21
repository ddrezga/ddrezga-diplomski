package hr.drezga.diplomski.springbundle;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import hr.drezga.diplomski.springservice.ISpringGreeter;

public class Caller {
	private ISpringGreeter greeter;
	private ScheduledExecutorService sxs = Executors.newScheduledThreadPool(1);
	
	private void init() {
		sxs.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				System.out.println(new Random().nextInt());
			}
		}, 0, 1, TimeUnit.SECONDS);
	}
	
	private void destroy() {
		sxs.shutdown();
	}

	public void setGreeter(ISpringGreeter greeter) {
		this.greeter = greeter;
	}
}
