package hr.drezga.diplomski.filteri;

import java.awt.image.BufferedImage;

import com.jhlabs.image.AbstractBufferedImageOp;
import com.jhlabs.image.WholeImageFilter;

import hr.drezga.diplomski.video.core.IVideoFilter;

public class GenericFilter implements IVideoFilter {
	
	AbstractBufferedImageOp filter;

	@Override
	public BufferedImage filter(BufferedImage i) {
		filter.filter(i, i);
		return i;
	}

	public void setFilter(AbstractBufferedImageOp filter) {
		this.filter = filter;
	}

	@Override
	public String getId() {
		return filter.getClass().getSimpleName();
	}

}
