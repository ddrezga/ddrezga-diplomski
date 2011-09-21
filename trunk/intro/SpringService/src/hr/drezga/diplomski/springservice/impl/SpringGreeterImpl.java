package hr.drezga.diplomski.springservice.impl;

import hr.drezga.diplomski.springservice.ISpringGreeter;

public class SpringGreeterImpl implements ISpringGreeter {

	@Override
	public void say(String s) {
		System.out.println(s);
	}

}
