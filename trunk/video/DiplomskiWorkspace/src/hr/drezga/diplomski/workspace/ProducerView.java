package hr.drezga.diplomski.workspace;

import java.util.ArrayList;
import java.util.List;

import hr.drezga.diplomski.video.core.IVideoHandler;
import hr.drezga.diplomski.video.core.IVideoProducer;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.context.BundleContextAware;
import org.springframework.osgi.service.importer.ServiceReferenceProxy;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;

@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
public class ProducerView extends ViewPart implements BundleContextAware {
	public static final String ID = "Producers.view";
	private List<IVideoProducer> producers = new ArrayList<IVideoProducer>();
	private BundleContext bc;
	private Display d;

	ListViewer listViewer;
	org.eclipse.swt.widgets.List list;

	@Override
	public void createPartControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		listViewer = new ListViewer(container, SWT.BORDER | SWT.V_SCROLL);
		list = listViewer.getList();
		
		listViewer.setLabelProvider(new ViewerLabelProvider());
		listViewer.setContentProvider(new ContentProvider());
		
		d = getSite().getShell().getDisplay();
		MenuManager menuManager = new MenuManager();
		list.setMenu(menuManager.createContextMenu(list));
		
		getSite().registerContextMenu(menuManager, listViewer);
		getSite().setSelectionProvider(listViewer);
	}

	private static class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return ((List<IVideoProducer>)inputElement).toArray(new IVideoProducer[0]);
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
			return ((IVideoProducer)element).getId();
		}
	}

	public void onBindProducer(ServiceReference sr) throws Exception {
		producers.add(getProducerFromService(getServiceReference(sr)));
		if (d != null)
			d.asyncExec(new Runnable() {
				public void run() {
					if (listViewer != null) {
						listViewer.setInput(producers);
						listViewer.refresh();
					}
				}
			});
	}

	public void onUnbindProducer(ServiceReference sr) throws Exception {
		producers.remove(getProducerFromService(getServiceReference(sr)));
		if (d != null)
			d.asyncExec(new Runnable() {
				public void run() {
					if (listViewer != null) {
						listViewer.setInput(producers);
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

	private IVideoProducer getProducerFromService(ServiceReference sr) {
		return (IVideoProducer)bc.getService((ServiceReference)sr);
	}

	@Override
	public void setFocus() {
		list.setFocus();
		listViewer.setInput(producers);
		listViewer.refresh();
	}

	@Override
	public void setBundleContext(BundleContext bc) {
		this.bc = bc;
	}
}
