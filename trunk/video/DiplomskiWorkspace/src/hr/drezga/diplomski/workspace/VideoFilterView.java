package hr.drezga.diplomski.workspace;

import hr.drezga.diplomski.video.core.IVideoDispatcher;
import hr.drezga.diplomski.video.core.IVideoFilter;
import hr.drezga.diplomski.video.core.IVideoHandler;
import hr.drezga.diplomski.video.core.IVideoProducer;
import hr.drezga.diplomski.workspace.input.VideoEditorInput;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.context.BundleContextAware;

import com.jhlabs.image.ImageUtils;
import org.eclipse.swt.widgets.Label;

public class VideoFilterView extends EditorPart implements BundleContextAware, IVideoProducer {
	public VideoFilterView() {
	}
	public static final String ID = "VideoFilter.view"; //$NON-NLS-1$
	private VideoFilterView ref;
	private BufferedImage img;
	private Panel panel;
	private BundleContext bc;
	private ServiceRegistration<IVideoHandler> sr;
	private ServiceRegistration<IVideoProducer> vp;
	private volatile int scaling = 1;
	private volatile boolean takingSnapshot = false;
	private java.util.List<IVideoFilter> filters = new ArrayList<IVideoFilter>();
	ListViewer listViewer;
	Button btnStartProducer;
	IVideoDispatcher dispatcher = null;
	
	IVideoHandler videoHandler = new IVideoHandler() {
		@Override
		public void proccessFrame(BufferedImage i, long timestamp) {
			if (takingSnapshot)
				return;
//			img = i;
			img = ImageUtils.cloneImage(i);
			getEditorSite().getWorkbenchWindow().getShell().getDisplay().syncExec(new Runnable() {
				@Override
				public void run() {
					for (IVideoFilter filter : filters) {
						if (filter != null) {
							img = filter.filter(img);
						}
					}
					panel.repaint();
					if (dispatcher != null && btnStartProducer.getSelection())
						dispatcher.dispatch(img, txtName.getText());
				}
			});
		}
	};
	private Text txtName;
	
	class DropListener extends ViewerDropAdapter {

		protected DropListener(Viewer viewer) {
			super(viewer);
		}

		@Override
		public boolean performDrop(Object data) {
			java.util.List<ServiceReference<IVideoFilter>> services = null;
			try {
				services = (java.util.List<ServiceReference<IVideoFilter>>) bc.getServiceReferences(IVideoFilter.class, "(name=" + data.toString() + ")");
			} catch (InvalidSyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			filters.add(bc.getService(services.get(0)));
			getViewer().setInput(filters);
			getViewer().refresh();
			return true;
		}

		@Override
		public boolean validateDrop(Object target, int operation, TransferData transferType) {
			return true;
		}
		
	}
	
	private static class ContentProvider implements IStructuredContentProvider {
		public Object[] getElements(Object inputElement) {
			return ((java.util.List<IVideoFilter>)inputElement).toArray(new IVideoFilter[0]);
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

	/**
	 * Create contents of the editor part.
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		
		Composite rootComposite = new Composite(parent, SWT.NONE);
		GridLayout gl_rootComposite = new GridLayout(3, false);
		gl_rootComposite.marginHeight = 0;
		gl_rootComposite.marginWidth = 0;
		rootComposite.setLayout(gl_rootComposite);
		
		txtName = new Text(rootComposite, SWT.BORDER);
		txtName.setText("Name");
		GridData gd_txtName = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtName.widthHint = 104;
		txtName.setLayoutData(gd_txtName);
		
		ref = this;
		
		btnStartProducer = new Button(rootComposite, SWT.TOGGLE);
		btnStartProducer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (btnStartProducer.getSelection()) {
					Dictionary<String, String> props = new Hashtable<String, String>();
					props.put("camera", txtName.getText());
					vp = bc.registerService(IVideoProducer.class, ref, props);
				} else {
					vp.unregister();
				}
			}
		});
		btnStartProducer.setText("Start producer");
		
		ToolBar toolBar = new ToolBar(rootComposite, SWT.FLAT | SWT.RIGHT);
		GridData gd_toolBar = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_toolBar.widthHint = 196;
		toolBar.setLayoutData(gd_toolBar);
		toolBar.setBounds(0, 0, 297, 474);
		
		ToolItem tltmDelete = new ToolItem(toolBar, SWT.NONE);
		tltmDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				filters.remove(((IStructuredSelection)listViewer.getSelection()).getFirstElement());
				listViewer.setInput(filters);
				listViewer.refresh();
			}
		});
		tltmDelete.setText("Delete");
		
		ToolItem toolItem = new ToolItem(toolBar, SWT.SEPARATOR);
		
		final ToolItem tltmOriginal = new ToolItem(toolBar, SWT.RADIO);
		tltmOriginal.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				scaling = 0;
			}
		});
		tltmOriginal.setText("Original");
		
		ToolItem tltmFit = new ToolItem(toolBar, SWT.RADIO);
		tltmFit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				scaling = 1;
			}
		});
		tltmFit.setSelection(true);
		tltmFit.setText("Fit");
		
		final ToolItem tltmScale = new ToolItem(toolBar, SWT.RADIO);
		tltmScale.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				scaling = 2;
			}
		});
		tltmScale.setText("Scale");
		
		listViewer = new ListViewer(rootComposite, SWT.BORDER | SWT.V_SCROLL);
		List list = listViewer.getList();
		GridData gd_list = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 3);
		gd_list.widthHint = 3;
		list.setLayoutData(gd_list);
		int operations = DND.DROP_COPY | DND.DROP_MOVE;
		Transfer[] transferTypes = new Transfer[]{TextTransfer.getInstance()};
		listViewer.addDropSupport(operations, transferTypes, new DropListener(listViewer));
		
		listViewer.setLabelProvider(new ViewerLabelProvider());
		listViewer.setContentProvider(new ContentProvider());
		
		Composite composite = new Composite(rootComposite, SWT.EMBEDDED);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3));
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Frame frame = SWT_AWT.new_Frame(composite);
		
		panel = new Panel() {
			BufferedImage bi;
			int localScaling = -1;
			@Override
			public void paint(Graphics g) {
				update(g);
			}
			@Override
			public void update(Graphics g) {
				if (img == null) {
					g.setColor(Color.black);
					g.fillRect(0, 0, this.getWidth(), this.getHeight());
					localScaling = scaling;
					return;
				}
				if (scaling != localScaling) {
					g.setColor(Color.black);
					g.fillRect(0, 0, this.getWidth(), this.getHeight());
					localScaling = scaling;
				}
				if (scaling == 0) {
					g.drawImage(img, (this.getWidth()-img.getWidth())/2, (this.getHeight()-img.getHeight())/2, null);
				}
				if (scaling == 1) {
					double imgRatio = (double)img.getWidth() / (double)img.getHeight();
					double panelRatio = (double)this.getWidth() / (double)this.getHeight();
					double width;
					double height;
					if (panelRatio > imgRatio) {
						height = panel.getHeight();
						width = height * imgRatio;
					} else {
						width = panel.getWidth();
						height = width / imgRatio;
					}
					g.drawImage(img, (int)(this.getWidth()-width)/2, (int)(this.getHeight()-height)/2, (int)width, (int)height, null);
				}
				if (scaling == 2) {
					g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), null);
				}
			}
		};
		panel.setBackground(Color.BLACK);
		frame.add(panel, BorderLayout.CENTER);
		
		Dictionary<String, String> props = new Hashtable<String, String>();
		props.put("camera", ((VideoEditorInput)getEditorInput()).getVideo().getId());
		sr = bc.registerService(IVideoHandler.class, videoHandler, props);
	}

	@Override
	public void dispose() {
		sr.unregister();
		vp.unregister();
		super.dispose();
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		setSite(site);
		setInput(input);
		setPartName(((VideoEditorInput)getEditorInput()).getVideo().getId());
	}

	@Override
	public boolean isDirty() {
		return false;
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	@Override
	public void setBundleContext(BundleContext bc) {
		this.bc = bc;
	}

	@Override
	public void startVideo() {
	}

	@Override
	public void stopVideo() {
	}

	@Override
	public boolean isRunning() {
		return btnStartProducer.getSelection();
	}

	@Override
	public void setDispatcher(IVideoDispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	@Override
	public String getId() {
		return txtName.getText();
	}
}
