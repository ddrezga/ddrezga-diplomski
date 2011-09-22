package hr.drezga.diplomski.video.core;

import java.awt.image.BufferedImage;
import java.util.List;

public interface IVideoDispatcher {
	public void dispatch(BufferedImage img, String camera);
	public List<String> getProducerList();
}
