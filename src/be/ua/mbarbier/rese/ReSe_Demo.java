package be.ua.mbarbier.rese;

import java.io.File;

import be.ua.mbarbier.rese.image.LibUtilities;
import ij.IJ;
import ij.ImageJ;
import ij.plugin.*;
import ij.plugin.filter.Binary;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.ImagePlus;

/**
 * @author mbarbie1
 *
 */
public class ReSe_Demo implements PlugIn {

	ImagePlus source;
	ImagePlus target;

	public void scenario_showImage(String arg) {

		File srcFile = new File("c:/Users/Michael/Desktop/demo/input/20.png");
		File refFile = new File("c:/Users/Michael/Desktop/demo/input/31.png");
		this.source = IJ.openImage( srcFile.getAbsolutePath() );		
		this.target = IJ.openImage( refFile.getAbsolutePath() );		
		this.source.show();
		this.target.show();
	}

	public ImagePlus scaleImp( ImagePlus imp, int binning) {
		int newWidth = imp.getWidth() / binning;
		ImageProcessor ip = imp.getProcessor();
		ip.setInterpolationMethod(ip.BILINEAR);
		ImageProcessor scaledSource = ip.resize( newWidth );
		ImagePlus scaledImp = new ImagePlus("scaled", scaledSource);
		
		return scaledImp;
	}

	public ImagePlus smoothMask( ImagePlus imp, int binning) {
		
		ImageProcessor ip = imp.getProcessor();
		ImagePlus scaledImp = scaleImp( imp, 4);
		ImageProcessor scaledIp = scaledImp.getProcessor();
		//if ( scaledIp.getBitDepth() > 8 ) {
		//	scaledIp.con
		//} 
		ImageStatistics stats = scaledImp.getStatistics();
		
		
		long[] histo = stats.getHistogram();

		//scaledIp.setAutoThreshold( , );
		scaledIp.autoThreshold();
	 	new ImagePlus( "mask", scaledIp ).show();
	 	ip.autoThreshold();
	 	new ImagePlus( "mask", ip ).show();

		ImagePlus maskImp = LibUtilities.mask( scaledIp, 0, 128 );

		IJ.run(scaledImp, "Histogram", "");
		IJ.run(imp, "Histogram", "");


		return maskImp;
	}

	public void scenario_PreRegistration(String arg) {

		//File srcFile = new File("c:/Users/Michael/Desktop/demo/input/20.png");
		File srcFile = new File("c:/Users/Michael/Desktop/demo/input/7.png");
		File refFile = new File("c:/Users/Michael/Desktop/demo/input/31.png");
		this.source = IJ.openImage( srcFile.getAbsolutePath() );		
		this.target = IJ.openImage( refFile.getAbsolutePath() );		
		this.source.show();
		this.target.show();
		ImagePlus scaledImp = scaleImp( this.source, 4);
		scaledImp.show();
		smoothMask( this.source, 4 );

	}

	@Override
	public void run(String arg) {

		//scenario_showImage(arg);
		scenario_PreRegistration(arg);
	}

	public static void main(String[] args) {
		// set the plugins.dir property to make the plugin appear in the Plugins menu
		Class<?> clazz = ReSe_Demo.class;
		String url = clazz.getResource("/" + clazz.getName().replace('.', '/') + ".class").toString();
		String pluginsDir = url.substring(5, url.length() - clazz.getName().length() - 6);
		System.setProperty("plugins.dir", pluginsDir);

		// start ImageJ
		new ImageJ();

		// run the plugin
		IJ.runPlugIn(clazz.getName(), "");
	}
}
