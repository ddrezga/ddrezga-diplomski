package hr.drezga.diplomski.workspace.input;

import hr.drezga.diplomski.video.core.IVideoProducer;

import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

public class VideoEditorInput implements IEditorInput {
	
	IVideoProducer video;
	
	public VideoEditorInput(IVideoProducer video) {
		this.video = video;
	}

	@Override
	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}

	@Override
	public boolean exists() {
		return false;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	@Override
	public String getName() {
		return video.getId();
	}

	@Override
	public IPersistableElement getPersistable() {
		return null;
	}

	@Override
	public String getToolTipText() {
		return "";
	}
	
	public IVideoProducer getVideo() {
		return video;
	}

}
