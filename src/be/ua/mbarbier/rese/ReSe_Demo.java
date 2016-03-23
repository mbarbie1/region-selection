package be.ua.mbarbier.rese;

import java.io.File;

import ij.IJ;
import ij.ImageJ;
import ij.plugin.*;
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

	public void scenario_PreRegistration(String arg) {
		
		//File srcFile = new File("c:/Users/Michael/Desktop/demo/input/20.png");
		File srcFile = new File("c:/Users/Michael/Desktop/demo/input/7.png");
		File refFile = new File("c:/Users/Michael/Desktop/demo/input/31.png");
		this.source = IJ.openImage( srcFile.getAbsolutePath() );		
		this.target = IJ.openImage( refFile.getAbsolutePath() );		
		this.source.show();
		this.target.show();
		
		
		
	}
	
	@Override
	public void run(String arg) {

		scenario_showImage(arg);
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
