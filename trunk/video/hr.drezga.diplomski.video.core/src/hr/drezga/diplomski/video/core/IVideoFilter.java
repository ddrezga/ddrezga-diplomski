package hr.drezga.diplomski.video.core;

import java.awt.image.BufferedImage;

public interface IVideoFilter {
	public BufferedImage filter(BufferedImage i);
	public String getId();
}
