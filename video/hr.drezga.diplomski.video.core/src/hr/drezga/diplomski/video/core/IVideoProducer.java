package hr.drezga.diplomski.video.core;

public interface IVideoProducer {
	
	public void startVideo();
	public void stopVideo();
	public boolean isRunning();
	public void setDispatcher(IVideoDispatcher dispatcher);
	public String getId();
	
}
