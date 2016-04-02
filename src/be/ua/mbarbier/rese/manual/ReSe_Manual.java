package be.ua.mbarbier.rese.manual;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.io.File;

import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.Toolbar;

public class ReSe_Manual extends ImageWindow {

	PopupMenu pm = null;
	String[] roiNames;
	boolean mouseInCanvas = false;
	private RoiMouseListener ml; 
	Roi[] rois;
	Roi currentRoi;
	int currentRoiIndex;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ReSe_Manual(String title) {
		super( title );
	}

	public ReSe_Manual(ImagePlus imp, ImageCanvas ic, String[] roiNames ) {
		super( imp, ic );
		this.roiNames = roiNames;
		rois = new Roi[ roiNames.length ];
		this.currentRoiIndex = 0;
		this.currentRoi = rois[ this.currentRoiIndex ];
		this.setMl(new RoiMouseListener(roiNames, this));
	}

	public RoiMouseListener getMl() {
		return ml;
	}

	public void setMl(RoiMouseListener ml) {
		this.ml = ml;
	}

	@Override
	public void windowClosed(WindowEvent e) {
		System.exit(1);
	}
	
//	@Override
//	public void mouseReleased(MouseEvent e) {
//		IJ.log("Mouse released; # of clicks: " + e.getClickCount(), e);
//	}



	/**
	 * 
	 */
	public void roiDrawing() {
		
	}

	public void roiSelectionDialog() {
		
	}

	public class RoiMouseListener implements MouseListener, ActionListener {
		
		PopupMenu pm = null;
		PopupMenu m = null;
		MenuItem doneItem;
		MenuItem resetItem;
		String[] itemList = null;
		MenuItem[] menuItemList = null;
		ReSe_Manual mrs = null;
		int OFFSET = 10;
				
		public RoiMouseListener(String[] roiNames, ReSe_Manual mrs) {
			this.itemList = roiNames;
			this.mrs = mrs;
			this.menuItemList = new MenuItem[this.itemList.length];
			addPanel(this.mrs);
			m.show( mrs, mrs.ic.getX()-OFFSET, mrs.ic.getY()-OFFSET);
		}

		void addPopupMenu(ReSe_Manual mrs) {
			if ( pm != null) return;
			mrs.remove(pm);
			pm = new PopupMenu();
			doneItem = new MenuItem("Done");
			doneItem.addActionListener(this);
			pm.add(doneItem);
			resetItem = new MenuItem("Reset");
			resetItem.addActionListener(this);
			pm.add(resetItem);
			pm.addSeparator();
			for (int i = 0; i < itemList.length; i++ ) {
				menuItemList[i] = new MenuItem(itemList[i]);
				menuItemList[i].addActionListener(this);
				pm.add(menuItemList[i]);
			}
			mrs.add(pm);
		}

		void addPanel( ReSe_Manual mrs) {
			//if ( m != null) return;
			//mrs.remove(m);
			m = new PopupMenu();
			doneItem = new MenuItem("Done");
			doneItem.addActionListener(this);
			m.add(doneItem);
			resetItem = new MenuItem("Reset");
			resetItem.addActionListener(this);
			m.add(resetItem);
			m.addSeparator();
			for (int i = 0; i < itemList.length; i++ ) {
				menuItemList[i] = new MenuItem(itemList[i]);
				menuItemList[i].addActionListener(this);
				m.add(menuItemList[i]);
			}
			mrs.add(m);
		}

		
		@Override
		public void mouseClicked(MouseEvent e) {
			IJ.log(e.toString());
		}
		@Override
		public void mouseEntered(MouseEvent e) {
			mouseInCanvas = true;
			IJ.log(e.toString());
		}
		@Override
		public void mouseExited(MouseEvent e) {
			mouseInCanvas = false;
			IJ.log(e.toString());
		}
		@Override
		public void mousePressed(MouseEvent e) {
			IJ.log(e.toString());
		}
		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			IJ.log(e.toString());
			//int x = e.getXOnScreen();
			//int y = e.getYOnScreen();
			//GenericDialog gd = new GenericDialog("Choose a specific ROI or DONE");
			//gd.setBounds( x, y, gd.getWidth(), gd.getHeight() );
			//gd.addChoice("ROI", roiNames, roiNames[currentRoiIndex+1]);
			//gd.showDialog();

			//addPopupMenu(mrs);
			//pm.show(e.getComponent(), e.getX()+ OFFSET, e.getY()+ OFFSET);

		}

		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println(e.toString());
			if ( e.getActionCommand().equals(resetItem.getActionCommand()) ) {
				for ( int i = 0; i < itemList.length; i++ ) {
					menuItemList[ i ].setEnabled(true);
					rois[i] = null;
					mrs.imp.setOverlay(null);
				}
			}
			for ( int i = 0; i < itemList.length; i++ ) {
				String s = itemList[i];
				if (e.getActionCommand().equals(s) ) {
					Roi roi = imp.getRoi();
					currentRoiIndex = i;
					rois[ currentRoiIndex ] = roi;
					menuItemList[ currentRoiIndex ].setEnabled(false);
					Overlay ol = new Overlay();
					for ( int j = 0; j < itemList.length; j++ ) {
						if (rois[ j ] != null ) {
							ol.add( rois[ j ] );
						}
					}
					mrs.imp.setOverlay( ol );
				}
			}
			//		[e.getActionCommand()];
		}
	}
	
/*
 * 	public static void main(String[] args) {
		File sourceFile;
		String sourcePath = "c:/Users/Michael/Desktop/ReSe_Acapella/montages/montage_ch3.png";
		String[] rois = new String[]{"HT","HIP","Cx"};

		// start ImageJ
		new ImageJ();

		ImagePlus imp = IJ.openImage( sourcePath );
		ImageCanvas ic = new ImageCanvas(imp); 
		ReSe_Manual mrs = new ReSe_Manual( imp, ic, rois );
		IJ.setTool(Toolbar.FREEROI);
		ic.addMouseListener( mrs.getMl() );
		mrs.setVisible(true);
	}
*/

}
