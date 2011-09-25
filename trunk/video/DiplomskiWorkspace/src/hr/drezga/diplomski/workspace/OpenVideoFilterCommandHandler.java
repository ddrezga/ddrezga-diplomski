package hr.drezga.diplomski.workspace;

import hr.drezga.diplomski.video.core.IVideoProducer;
import hr.drezga.diplomski.workspace.input.VideoEditorInput;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenVideoFilterCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection sel = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		IVideoProducer video = (IVideoProducer) sel.getFirstElement();
		
		try {
			HandlerUtil.getActiveSite(event).getPage().openEditor(new VideoEditorInput(video), "VideoFilter.view");
		} catch (PartInitException e) {
			e.printStackTrace();
		}
		
		return video;
	}

	
}
