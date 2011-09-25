package hr.drezga.diplomski.workspace;

import hr.drezga.diplomski.video.core.IVideoHandler;
import hr.drezga.diplomski.workspace.input.VideoEditorInput;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.Date;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.EditorPart;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.context.BundleContextAware;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.jface.viewers.ListViewer;
import swing2swt.layout.FlowLayout;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.widgets.Text;

public class VideoFilterView extends EditorPart implements BundleContextAware {
	public VideoFilterView() {
	}

	public static final String ID = "VideoFilter.view"; //$NON-NLS-1$

	private BufferedImage img;
	private Panel panel;
	private BundleContext bc;
	private ServiceRegistration<IVideoHandler> sr;
	private volatile int scaling = 1;
	private volatile boolean takingSnapshot = false;
	
	IVideoHandler videoHandler = new IVideoHandler() {
		@Override
		public void proccessFrame(BufferedImage i, long timestamp) {
			if (takingSnapshot)
				return;
			img = i;
			getEditorSite().getWorkbenchWindow().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					panel.repaint();
				}
			});
		}
	};
	private Text txtName;
	
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
		
		Button btnStartProducer = new Button(rootComposite, SWT.NONE);
		btnStartProducer.setText("Start producer");
		
		ToolBar toolBar = new ToolBar(rootComposite, SWT.FLAT | SWT.RIGHT);
		GridData gd_toolBar = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_toolBar.widthHint = 196;
		toolBar.setLayoutData(gd_toolBar);
		toolBar.setBounds(0, 0, 297, 474);
		
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
		
		ListViewer listViewer = new ListViewer(rootComposite, SWT.BORDER | SWT.V_SCROLL);
		List list = listViewer.getList();
		GridData gd_list = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
		gd_list.widthHint = 3;
		list.setLayoutData(gd_list);
		
		Composite composite = new Composite(rootComposite, SWT.EMBEDDED);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Frame frame = SWT_AWT.new_Frame(composite);
		
		panel = new Panel() {
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
}
