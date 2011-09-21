package hr.drezga.diplomski.video.core;

import java.awt.image.BufferedImage;

public interface IVideoDispatcher {
	public void dispatch(BufferedImage img, String camera);
}
