package be.ua.mbarbier.rese;

import java.io.File;
import java.util.LinkedHashMap;

import be.ua.mbarbier.rese.image.LibUtilities;
import be.ua.mbarbier.rese.image.LibUtilities.ImagesResized;
import be.ua.mbarbier.rese.manual.ReSe_Manual;
import be.ua.mbarbier.rese.registration.LibRegistration;
import be.ua.mbarbier.rese.statistics.Statistics;
import be.ua.mbarbier.rese.utilities.text.LibText;
import bunwarpj.Transformation;
import ij.CompositeImage;
import ij.IJ;
import ij.ImageJ;
import ij.plugin.*;
import ij.plugin.filter.Binary;
import ij.plugin.filter.EDM;
import ij.process.Blitter;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.ImagePlus;
import ij.gui.HistogramWindow;
import ij.gui.ImageCanvas;
import ij.gui.Plot;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.measure.Measurements;

/**
 * @author mbarbie1
 *
 */
public class ReSe_Demo implements PlugIn {

	ImagePlus source;
	ImagePlus target;
	
	final static String METHOD_MANUAL_REGISTRATION = "manual";
	final static String METHOD_WARPING = "warping";
	final static String METHOD_MAP_ATLAS = "atlas";

	public void scenario_showImage(String arg) {

		File srcFile = new File("c:/Users/Michael/Desktop/demo/input/31.png");
		File refFile = new File("c:/Users/Michael/Desktop/demo/input/20.png");
		this.source = IJ.openImage( srcFile.getAbsolutePath() );		
		this.target = IJ.openImage( refFile.getAbsolutePath() );		
		this.source.show();
		this.target.show();
	}

	public void resizeImages() {
		
	}
	
	public ImagePlus scaleImp( ImagePlus imp, int binning) {
		int newWidth = imp.getWidth() / binning;
		ImageProcessor ip = imp.getProcessor();
		ip.setInterpolationMethod(ip.BILINEAR);
		ImageProcessor scaledSource = ip.resize( newWidth );
		ImagePlus scaledImp = new ImagePlus("scaled", scaledSource);

		return scaledImp;
	}

	public void bgMask( ImagePlus imp ) {

		ImageProcessor ip = imp.getProcessor();
		ImagePlus bgImp = LibUtilities.mask( ip.duplicate(), 0, 1 );
		bgImp.getProcessor().invert();
		ImageProcessor bgIp = bgImp.getProcessor().duplicate();

		EDM edm = new EDM();
		ImageProcessor edmIp = bgIp.duplicate();
		edm.toEDM( edmIp );

		ImagePlus t = LibUtilities.mask( edmIp, 0.01, 5 );
		IJ.run(t,"Create Selection", "");
		Roi roi = t.getRoi();
		imp.setRoi(roi);
		ImageStatistics stats = imp.getStatistics(Measurements.MEAN | Measurements.MEDIAN | Measurements.AREA);

		bgImp.getProcessor().invert();
		IJ.run(bgImp,"Create Selection", "");
		Roi roibg = bgImp.getRoi();
		ip.setValue( stats.mean );
		ip.fill( roibg );
	}

	public ImagePlus smoothMask( ImagePlus imp, int binning) {
		
		ImageProcessor ip = imp.getProcessor();
		ImagePlus scaledImp = scaleImp( imp, 4);
		ImageProcessor scaledIp = scaledImp.getProcessor();
		//if ( scaledIp.getBitDepth() > 8 ) {
		//	scaledIp.con
		//} 
		ImageStatistics stats = scaledImp.getStatistics();
		
		//long[] histo = stats.getHistogram();

		//scaledIp.setAutoThreshold( , );
		scaledIp.autoThreshold();
	 	new ImagePlus( "mask", scaledIp ).show();
		ImagePlus bgImp = LibUtilities.mask( ip, 0, 1 );
		//bgImp.show();
		
		EDM edm = new EDM();
		edm.toEDM( bgImp.getProcessor() );
		ImagePlus t = LibUtilities.mask( bgImp.getProcessor(), 0.1, 1.1 );
		//t.show();
		
		imp.getProcessor().setMask( t.getProcessor() );
		//imp.getProcessor().setR
		int height = imp.getHeight();
		int width = imp.getWidth();
		//short[] pixels = (short[]) imp.getProcessor().getPixels();
		byte[] pixels = imp.getProcessor().getMaskArray();
		new ImageStatistics();
		for (int y=0; y < height; y++) {
			for (int x=0; x < width; x++) {
			}
		}

		new ImagePlus( "", imp.getProcessor().crop() ).show();
		//imp.getProcessor().getHistogram();
		//HistogramWindow hw = new HistogramWindow( imp );
		
		//Plot p = new Plot();
		
	 	//ip.autoThreshold();
	 	//new ImagePlus( "mask", ip ).show();

		ImagePlus maskImp = LibUtilities.mask( scaledIp, 0, 128 );

		//IJ.run(imp, "Convert to Mask", "");
		//IJ.run(imp, "FeatureJ Derivatives", "x-order=1 y-order=0 z-order=0 smoothing=0.5");
		
		//IJ.run(scaledImp, "Histogram", "");
		//IJ.run(imp, "Histogram", "");

		return maskImp;
	}

	public void scenario_PreRegistration(String arg) {

		//File srcFile = new File("c:/Users/Michael/Desktop/demo/input/20.png");
		File srcFile = new File("c:/Users/Michael/Desktop/demo/input/7.png");
		File refFile = new File("c:/Users/Michael/Desktop/demo/input/31.png");
		this.source = IJ.openImage( srcFile.getAbsolutePath() );
		this.target = IJ.openImage( refFile.getAbsolutePath() );
		//this.source.show();
		//this.target.show();
		//ImagePlus scaledImp = scaleImp( this.source, 4);
		//scaledImp.show();
		
		// Resize images to have the same dimensions
		ImagesResized ir = LibUtilities.resizeImages( this.source, this.target );
		this.source = ir.getImp1();
		this.target = ir.getImp2();
		
		// Introduce average background signal where there is not signal in the image, 
		// this is to remove sharp borders for the feature detection
		bgMask( this.source );
		bgMask( this.target );
		
		// Obtaining SIFT features for the registration
		LinkedHashMap<String, Roi> out = LibRegistration.siftSingle( this.source, this.target, LibRegistration.siftParamDefault() );
		Roi roiSource = out.get("roiSource");
		Roi roiTarget = out.get("roiTarget");
		// Add SIFT features to the ROIs of the source and target images
		this.source.setRoi(roiSource);
		this.target.setRoi(roiTarget);
		
		// Elastic registration procedure
		Transformation transfo = LibRegistration.bunwarpj_param( this.source, this.target, LibRegistration.bunwarpjParamDefault() );
		
		
		ImagePlus reg = transfo.getDirectResults();
		//reg.show();

		int nChannels = 2;  
		int nSlices = 1; 
		int nFrames = 1;   
		ImagePlus impMerged = IJ.createHyperStack("merged", this.target.getWidth(), this.target.getHeight(), 2, 1, 1, this.target.getBitDepth());
		impMerged.getImageStack().setProcessor(reg.getStack().getProcessor(1).convertToShort(false), 1);
		impMerged.getImageStack().setProcessor(this.target.getProcessor(), 2);
		impMerged.setDimensions(nChannels, nSlices, nFrames);  
		CompositeImage impComp = new CompositeImage(impMerged, CompositeImage.COMPOSITE); 
		IJ.run(impComp, "Enhance Contrast", "saturated=0.35");

		impComp.show();  
		
		
		//smoothMask( this.source, 4 );

	}

	public void scenario_Manual( String sourcePath, String outputRoiPath, String outputImageOverlayPath ) {
		//String sourcePath = "c:/Users/Michael/Desktop/ReSe_Acapella/montages/montage_ch3.png";
		//String sourcePath = "c:/Users/Michael/Desktop/demo/input/manual/srcId_1_binning_16.png";
		String[] rois = new String[]{"Th","Hip","Cx","Md","Bs"};

		//ReSe_Manual mrs = new ReSe_Manual( sourcePath, rois );
		
		ImagePlus imp = IJ.openImage( sourcePath );
		IJ.setTool(Toolbar.FREEROI);
		ImageCanvas ic = new ImageCanvas(imp); 
		IJ.run(imp, "Enhance Contrast", "saturated=0.35");
		ReSe_Manual mrs = new ReSe_Manual( imp, ic, rois );
		ic.addMouseListener( mrs.getMl() );
		mrs.setOutputImageOverlayPath(outputImageOverlayPath);
		mrs.setOutputRoiPath(outputRoiPath);
		mrs.setVisible(true);
	}
	
	@Override
	public void run(String arg) {

		//scenario_showImage(arg);
		//scenario_PreRegistration(arg);
		
		String[] args = arg.split(",");
		if ( args[0].equals(METHOD_MANUAL_REGISTRATION) ) {
			for (int i = 0; i < args.length; i++) {
				System.out.println( args[i] );
			}
			scenario_Manual( args[1], args[2], args[3] );
		}
	}
	
	public static void main(String[] args) {
		// set the plugins.dir property to make the plugin appear in the Plugins menu
		Class<?> clazz = ReSe_Demo.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
		System.setProperty("plugins.dir", pluginsDir);

		// start ImageJ
		new ImageJ();

		// Testing arguments for the Manual registration
		String outputRoiPath = "c:/Users/Michael/Desktop/demo/output/rois.csv";
		String outputImageOverlayPath = "c:/Users/Michael/Desktop/demo/output/overlay.png";
		String sourcePath = "c:/Users/Michael/Desktop/demo/input/manual/srcId_1_binning_16.png";
		String[] fakeArgs = new String[]{ METHOD_MANUAL_REGISTRATION, sourcePath, outputRoiPath, outputImageOverlayPath }; 

		// run the plugin
		String arg = LibText.concatenateStringArray( fakeArgs, ",");
		//IJ.log(arg);
		//if (args.equals(null)) {
		//	IJ.log("[0]:" + args[0]);
		arg = LibText.concatenateStringArray( args, "," );
		//}
		//IJ.log(arg);
		IJ.runPlugIn(clazz.getName(), arg );
	}
}
