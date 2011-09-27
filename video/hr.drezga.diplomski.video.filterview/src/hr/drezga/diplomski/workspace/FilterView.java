package hr.drezga.diplomski.workspace;

import hr.drezga.diplomski.video.core.IVideoFilter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.context.BundleContextAware;
import org.springframework.osgi.service.importer.ServiceReferenceProxy;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

public class FilterView extends ViewPart implements BundleContextAware{
	public FilterView() {
	}

	public static final String ID = "Filters.view"; //$NON-NLS-1$
	private List<IVideoFilter> filters = new ArrayList<IVideoFilter>();
	private BundleContext bc;
	private Display d;

	ListViewer listViewer;
	org.eclipse.swt.widgets.List list;

	DragSourceListener dragsource = new DragSourceListener() {
		
		@Override
		public void dragStart(DragSourceEvent event) {
			System.out.println(event);
		}
		
		@Override
		public void dragSetData(DragSourceEvent event) {
			event.data = ((IVideoFilter)((IStructuredSelection)listViewer.getSelection()).getFirstElement()).getId();
		}
		
		@Override
		public void dragFinished(DragSourceEvent event) {
			System.out.println(event);
		}
	};
	
	@Override
	public void createPartControl(Composite parent) {
		d = getSite().getShell().getDisplay();

		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		listViewer = new ListViewer(container, SWT.BORDER | SWT.V_SCROLL);
		list = listViewer.getList();
		
		listViewer.setLabelProvider(new ViewerLabelProvider());
		listViewer.setContentProvider(new ContentProvider());
		int operations = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transferTypes = new Transfer[]{TextTransfer.getInstance()};
		listViewer.addDragSupport(operations, transferTypes , dragsource);
		
		setFocus();
	}

	private static class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return ((List<IVideoFilter>)inputElement).toArray(new IVideoFilter[0]);
		}
		public void dispose() {
		}
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
	}

	private static class ViewerLabelProvider extends LabelProvider {
		public Image getImage(Object element) {
			return null;
		}
		public String getText(Object element) {
			return ((IVideoFilter)element).getId();
		}
	}

	public void onBindFilter(ServiceReference sr) throws Exception {
		filters.add(getProducerFromService(getServiceReference(sr)));
		if (d != null)
			d.asyncExec(new Runnable() {
				public void run() {
					if (listViewer != null) {
						listViewer.setInput(filters);
						listViewer.refresh();
					}
				}
			});
	}

	public void onUnbindFilter(ServiceReference sr) throws Exception {
		filters.remove(getProducerFromService(getServiceReference(sr)));
		if (d != null)
			d.asyncExec(new Runnable() {
				public void run() {
					if (listViewer != null) {
						listViewer.setInput(filters);
						listViewer.refresh();
					}
				}
			});
	}

	public void registerProducer(ServiceReference sr) {
	}

	private ServiceReference getServiceReference(ServiceReference sr) {
		return (sr instanceof ServiceReferenceProxy)?((ServiceReferenceProxy)sr).getTargetServiceReference():sr;
	}

	private IVideoFilter getProducerFromService(ServiceReference sr) {
		return (IVideoFilter)bc.getService((ServiceReference)sr);
	}

	@Override
	public void setFocus() {
		list.setFocus();
		listViewer.setInput(filters);
		listViewer.refresh();
	}

	@Override
	public void setBundleContext(BundleContext bc) {
		this.bc = bc;
	}
}
