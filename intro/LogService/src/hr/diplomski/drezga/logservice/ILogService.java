package hr.diplomski.drezga.logservice;

public interface ILogService {
	public static int INFO = 0;
	public static int DEBUG = 1;
	public static int WARNING = 2;
	
	public void log(int type, String logPath, String message);
}
