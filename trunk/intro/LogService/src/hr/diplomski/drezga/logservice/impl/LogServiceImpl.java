package hr.diplomski.drezga.logservice.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hr.diplomski.drezga.logservice.ILogService;

public class LogServiceImpl implements ILogService {

	@Override
	public void log(int type, String logPath, String message) {
		Logger log = LoggerFactory.getLogger(logPath);
		switch (type) {
		case ILogService.INFO:
			log.info(message);
			break;
		case ILogService.WARNING:
			log.warn(message);
			break;
		case ILogService.DEBUG:
			log.debug(message);
			break;
		default:
			break;
		}
	}
}
