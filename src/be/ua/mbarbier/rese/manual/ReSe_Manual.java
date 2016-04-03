package be.ua.mbarbier.rese.manual;

import java.awt.Button;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import be.ua.mbarbier.rese.roi.LibEvotecRois;
import be.ua.mbarbier.rese.roi.LibRoi;
import be.ua.mbarbier.rese.utilities.text.LibText;
import ij.IJ;
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
	ImagePlus impOverlay = null;
	ImagePlus image = null;
	String outputDir;
	String inputPath;
	String outputRoiPath;
	String outputImageOverlayPath;
	boolean saveOverlayImage = true;
	boolean saveRois = true;

	private static final long serialVersionUID = 1L;

	public ReSe_Manual(String title) {
		super( title );
	}

	public ReSe_Manual( String inputPath, String[] roiNames ) {
		super(inputPath);
		ImagePlus imp = IJ.openImage( inputPath );
		IJ.setTool(Toolbar.FREEROI);
		ImageCanvas ic = new ImageCanvas(imp);
		this.setImage(imp);
		IJ.run(imp, "Enhance Contrast", "saturated=0.35");
		this.roiNames = roiNames;
		rois = new Roi[ roiNames.length ];
		this.currentRoiIndex = 0;
		this.currentRoi = rois[ this.currentRoiIndex ];
		this.setMl(new RoiMouseListener(roiNames, this));
		ic.addMouseListener( this.getMl() );
		this.setVisible(true);
	}

	public ReSe_Manual( ImagePlus imp, ImageCanvas ic, String[] roiNames ) {
		super( imp, ic );
		this.roiNames = roiNames;
		rois = new Roi[ roiNames.length ];
		this.currentRoiIndex = 0;
		this.currentRoi = rois[ this.currentRoiIndex ];
		this.setMl(new RoiMouseListener(roiNames, this));
	}

	public String getOutputRoiPath() {
		return outputRoiPath;
	}

	public void setOutputRoiPath(String outputRoiPath) {
		this.outputRoiPath = outputRoiPath;
	}

	public String getOutputImageOverlayPath() {
		return outputImageOverlayPath;
	}

	public void setOutputImageOverlayPath(String outputImageOverlayPath) {
		this.outputImageOverlayPath = outputImageOverlayPath;
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

	public class RoiMouseListener implements MouseListener, ActionListener {

		Frame f = null;
		String[] itemList = null;
		Button doneButton;
		Button resetButton;
		Button finishButton;
		Button[] buttonItemList = null;
		ReSe_Manual mrs = null;

		public RoiMouseListener(String[] roiNames, ReSe_Manual mrs) {
			this.itemList = roiNames;
			this.mrs = mrs;
			this.buttonItemList = new Button[this.itemList.length];
			addFrame(this.mrs);
			f.setVisible(true);
		}

		void addFrame(ReSe_Manual mrs) {

			this.f = new Frame("Manual ReSe");
			this.f.setAlwaysOnTop(true);
			f.setLayout(new GridLayout(0,1));
			doneButton = new Button("Show");
			doneButton.addActionListener(this);
			f.add(doneButton);
			resetButton = new Button("Reset");
			resetButton.addActionListener(this);
			f.add(resetButton);
			finishButton = new Button("Finish");
			finishButton.addActionListener(this);
			f.add(finishButton);
			for (int i = 0; i < itemList.length; i++ ) {
				buttonItemList[i] = new Button(itemList[i]);
				buttonItemList[i].addActionListener(this);
				f.add(buttonItemList[i]);
			}
			f.pack();
			f.setVisible(true);
		}
		@Override
		public void mouseClicked(MouseEvent e) {
		}
		@Override
		public void mouseEntered(MouseEvent e) {
			mouseInCanvas = true;
		}
		@Override
		public void mouseExited(MouseEvent e) {
			mouseInCanvas = false;
		}
		@Override
		public void mousePressed(MouseEvent e) {
		}
		@Override
		public void mouseReleased(MouseEvent e) {
		}

		/**
		 * Implements an ActionListener: Actions to perform when the user clicks:
		 * - Show (doneButton): Show the overlay image, remove any selection 
		 * - Reset: remove any selection, remove all ROIs, re-enable the regions to be selected  
		 * - Finish: Return the program, save the Roi as an Evotec roi
		 * - One of the region names: 
		 */
		@Override
		public void actionPerformed(ActionEvent e) {
			System.out.println(e.toString());
			if ( e.getActionCommand().equals(finishButton.getActionCommand()) ) {
				mrs.impOverlay = LibRoi.getOverlayImage( mrs.rois, mrs.imp );
				mrs.finish();
				mrs.getImagePlus().close();
			}
			if ( e.getActionCommand().equals(resetButton.getActionCommand()) ) {
				for ( int i = 0; i < itemList.length; i++ ) {
					buttonItemList[ i ].setEnabled(true);
					rois[i] = null;
				}
				mrs.getImagePlus().setOverlay(null);
				IJ.run( mrs.getImagePlus() , "Select None", "");
			}
			if ( e.getActionCommand().equals(doneButton.getActionCommand()) ) {
				mrs.impOverlay = LibRoi.getOverlayImage( mrs.rois, mrs.imp );
				IJ.run( mrs.getImagePlus() , "Select None", "");
				mrs.setImage(mrs.impOverlay);
			}
			for ( int i = 0; i < itemList.length; i++ ) {
				String s = itemList[i];
				if (e.getActionCommand().equals(s) ) {
					IJ.run("Fit Spline");
					Roi roi = imp.getRoi();
					roi.setName(s);
					currentRoiIndex = i;
					rois[ currentRoiIndex ] = roi;
					buttonItemList[ currentRoiIndex ].setEnabled(false);
					Overlay ol = new Overlay();
					for ( int j = 0; j < itemList.length; j++ ) {
						if (rois[ j ] != null ) {
							ol.add( rois[ j ] );
						}
					}
					mrs.imp.setOverlay( ol );
				}
			}
		}
	}

	public void saveImageOverlay() {
		IJ.save(impOverlay, this.outputImageOverlayPath);
	}
	
	public void saveRois() {
		int width = this.getImagePlus().getWidth();
		int height = this.getImagePlus().getHeight();
		float xltUnit = 0.0f;
		float yltUnit = 0.0f;
		float xrbUnit = new Integer( width ).floatValue();
		float yrbUnit = new Integer( height ).floatValue();
		
		for (int i = 0; i < roiNames.length; i++ ) {
			roiNames[i] = LibRoi.labelToEvotecLabel(roiNames[i]);
		}
		String roiHeader = LibText.concatenateStringArray(roiNames, "\t");
		String roiRow = LibEvotecRois.getEvotecRoisRow( xltUnit, yltUnit, xrbUnit, yrbUnit, width, height, rois );
		
		PrintWriter writer;
		try {
			writer = new PrintWriter( this.outputRoiPath );
			writer.println(roiHeader);
			writer.println(roiRow);
			writer.close();
		} catch(FileNotFoundException e) {
			IJ.log(e.getMessage());
		}
	}
	
	/**
	 * Wrap up function for when the finish button is clicked:
	 * 	- saves the ROIs and the overlay image when necessary
	 */
	public void finish() {
		if ( saveOverlayImage ) {
			saveImageOverlay();
		}
		if ( saveRois ) {
			saveRois();
		}
	}
}
