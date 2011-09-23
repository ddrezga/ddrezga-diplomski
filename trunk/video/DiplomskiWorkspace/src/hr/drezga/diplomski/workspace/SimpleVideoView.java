package hr.drezga.diplomski.workspace;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import hr.drezga.diplomski.video.core.IVideoHandler;

import java.awt.Frame;
import org.eclipse.swt.awt.SWT_AWT;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.springframework.osgi.context.BundleContextAware;

import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.BorderLayout;
import javax.swing.JRootPane;
import java.awt.Canvas;
import java.awt.image.BufferedImage;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import org.eclipse.swt.layout.FillLayout;
import javax.swing.JPanel;

public class SimpleVideoView extends ViewPart implements BundleContextAware{
	public SimpleVideoView() {
	}
	public static final String ID = "SimpleVideo.view";

	private BufferedImage img;
	private Panel panel;
	private BundleContext bc;
	private ServiceRegistration<IVideoHandler> sr;
	
	IVideoHandler videoHandler = new IVideoHandler() {
		@Override
		public void proccessFrame(BufferedImage i, long timestamp) {
			img = i;
			getViewSite().getWorkbenchWindow().getShell().getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					panel.repaint();
				}
			});
		}
	};
	
	public void createPartControl(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.EMBEDDED);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Frame frame = SWT_AWT.new_Frame(composite);
		
		panel = new Panel() {
			@Override
			public void paint(Graphics g) {
				update(g);
			}
			@Override
			public void update(Graphics g) {
				g.drawImage(img, 0, 0, this.getWidth(), this.getHeight(), null);
			}
		};
		frame.add(panel, BorderLayout.CENTER);
		Dictionary<String, String> props = new Hashtable<String, String>(); 
		props.put("camera", "CAMERA_0");
		sr = bc.registerService(IVideoHandler.class, videoHandler, props);	
	}

	@Override
	public void setFocus() {
	}
	
	@Override
	public void dispose() {
		sr.unregister();
		super.dispose();
	}

	@Override
	public void setBundleContext(BundleContext bc) {
		this.bc = bc;
	}
	
}