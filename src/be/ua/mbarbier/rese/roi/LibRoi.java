package be.ua.mbarbier.rese.roi;

import java.awt.Color;
import java.util.LinkedHashMap;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.Overlay;
import ij.gui.Roi;

public class LibRoi {

	static String EVOTEC_REGION_PREFIX = "EVT_Regions_";
	
	public static ImagePlus getOverlayImage( Roi[] rois, ImagePlus imp ) {
		ImagePlus impRois = new ImagePlus( "Image with rois", imp.getProcessor().duplicate().convertToRGB() );
		Overlay overlay = new Overlay();
		for (int i = 0; i < rois.length; i++ ) {
			Roi currentRoi = rois[i];
			currentRoi.setStrokeWidth(2);
			Color color = roiColor().get( currentRoi.getName() );
			if (color != null) {} else color = Color.GRAY;
			currentRoi.setStrokeColor( color );
			overlay.add( currentRoi );
//			overlay.setLabelColor( color );
//			overlay.setStrokeColor( color ); 
		}
		overlay.drawNames( true );
		overlay.drawLabels( true );
		//overlay.setLabelFont( Font( "fontName", Font.PLAIN, int( impRois.getWidth()/50.0 ) ) )
		impRois.setOverlay( overlay );
		impRois.setHideOverlay(false);

		return impRois;
	}

	public static LinkedHashMap<String,Color> roiColor() {
		LinkedHashMap<String,Color> colors = new LinkedHashMap<String,Color>(); 
		colors.put( "Bg", Color.gray );
		colors.put( "Cx", Color.red );
		colors.put( "Hip", Color.green );
		colors.put( "Th", Color.blue );
		colors.put( "Bs", Color.magenta );
		colors.put( "Mb", Color.yellow );
		colors.put( "Cb", Color.cyan );

		return colors;
	}

	public static String labelToEvotecLabel( String label ) {
		return EVOTEC_REGION_PREFIX + label;
	}

	public static String evotecLabelToLabel( String evotecLabel ) {
		return evotecLabel.substring( EVOTEC_REGION_PREFIX.length() );
	}
}
