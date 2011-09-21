package hr.drezga.diplomski.video.core;

import java.awt.image.BufferedImage;

public interface IVideoHandler {
	
	public void proccessFrame(BufferedImage img, long timestamp);

}
