/**
 * 
 */
package be.ua.mbarbier.rese.image;

import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;
import ij.process.ShortBlitter;
import ij.process.ImageStatistics;
import ij.plugin.CanvasResizer;

/**
 * @author mbarbie1
 *
 */
public class LibUtilities {

	public static class ImagesResized {
		ImagePlus imp1;
		ImagePlus imp2;
		int[] offset1 = new int[2];
		int[] offset2 = new int[2];
		
		public ImagesResized(ImagePlus imp1, ImagePlus imp2, int newX1, int newY1, int newX2, int newY2) {
			this.imp1 = imp1;
			this.imp2 = imp2;
			this.offset1 = new int[]{newX1, newY1};
			this.offset2 = new int[]{newX2, newY2};
		}

		public ImagePlus getImp1() {
			return imp1;
		}

		public void setImp1(ImagePlus imp1) {
			this.imp1 = imp1;
		}

		public ImagePlus getImp2() {
			return imp2;
		}

		public void setImp2(ImagePlus imp2) {
			this.imp2 = imp2;
		}

		public int[] getOffset1() {
			return offset1;
		}

		public void setOffset1(int[] offset1) {
			this.offset1 = offset1;
		}

		public int[] getOffset2() {
			return offset2;
		}

		public void setOffset2(int[] offset2) {
			this.offset2 = offset2;
		}
		
	}
	
	public static ImagesResized resizeImages( ImagePlus imp1, ImagePlus imp2 ) {

		int w1 = imp1.getWidth();
		int w2 = imp2.getWidth();
		int h1 = imp1.getHeight();
		int h2 = imp2.getHeight();
		int newW = Math.max(w1, w2);
		int newH = Math.max(h1, h2);
		int newX1 = 0;
		int newX2 = 0;
		int newY1 = 0;
		int newY2 = 0;
		if (newW > w1) {
			newX1 = (int) ( Math.round( (newW - w1) / 2.0 ) );
			newX2 = 0;
		} else {
			newX2 = (int) ( Math.round( (newW - w2) / 2.0 ) );
			newX1 = 0;
		}
		if (newH > h1) {
			newY1 = (int) ( Math.round( (newH - h1) / 2.0 ) );
			newY2 = 0;
		} else {
			newY2 = (int) ( Math.round( (newH - h2) / 2.0 ) );
			newY1 = 0;
		}

		CanvasResizer cr = new CanvasResizer();
		ImagePlus impR1 = new ImagePlus( imp1.getTitle(), cr.expandImage(imp1.getProcessor(), newW, newH, newX1, newY1) );
		ImagePlus impR2 = new ImagePlus( imp2.getTitle(), cr.expandImage(imp2.getProcessor(), newW, newH, newX2, newY2) );

		return new LibUtilities.ImagesResized( impR1, impR2, newX1, newY1, newX2, newY2 );
	}

	public ImagesResized resizeImagesBorder( ImagePlus imp1, ImagePlus imp2, int borderWidth ) {

		int w1 = imp1.getWidth();
		int w2 = imp2.getWidth();
		int h1 = imp1.getHeight();
		int h2 = imp2.getHeight();
		int newW = Math.max(w1, w2) + borderWidth;
		int newH = Math.max(h1, h2) + borderWidth;
		int newX1 = 0;
		int newX2 = 0;
		int newY1 = 0;
		int newY2 = 0;
		newX1 = (int) ( Math.round( (newW - w1) / 2.0 ) );
		newX2 = (int) ( Math.round( (newW - w2) / 2.0 ) );
		newY1 = (int) ( Math.round( (newH - h1) / 2.0 ) );
		newY2 = (int) ( Math.round( (newH - h2) / 2.0 ) );

		CanvasResizer cr = new CanvasResizer();
		ImagePlus impR1 = new ImagePlus( imp1.getTitle(), cr.expandImage(imp1.getProcessor(), newW, newH, newX1, newY1) );
		ImagePlus impR2 = new ImagePlus( imp2.getTitle(), cr.expandImage(imp2.getProcessor(), newW, newH, newX2, newY2) );

		return new LibUtilities.ImagesResized( impR1, impR2, newX1, newY1, newX2, newY2 );
	}
	

	public static double maskArea( ImageProcessor ip ) {
		ImageStatistics stats = ip.getStatistics();
		
		return stats.area * stats.mean / ip.maxValue();
	} 
	
	public static double area( ImageProcessor ip ) {
		ImageStatistics stats = ip.getStatistics();
		
		return stats.area;
	} 

	public static ImagePlus mask( ImageProcessor ip, double valueA, double valueB ) {
		ip = ip.duplicate();
		ip.setThreshold(valueA, valueB, ImageProcessor.NO_LUT_UPDATE);

		ImagePlus imp = new ImagePlus("Mask", ip);
		IJ.run(imp, "Convert to Mask", "");
		
		return imp;
	}

	public static double sumOfSquares( ImageProcessor ip ) {
		ImageStatistics stats = ip.getStatistics();

		return stats.area * stats.mean;
	}

	public static double sumOfProduct( ImageProcessor ip1, ImageProcessor ip2 ) {
		
		ImageProcessor ip1Temp = ip1.duplicate().convertToShort(false);
		ImageProcessor ip2Temp = ip2.duplicate().convertToShort(false);
		ip1Temp.copyBits(ip2Temp, 0, 0, ShortBlitter.MULTIPLY);
		ImageStatistics stats = ip1Temp.getStatistics();

		return stats.mean * stats.area;
	}

	public static ImageProcessor maskIntersection( ImageProcessor ip1, ImageProcessor ip2 ) {
		// Should we convert to mask to be sure?
		ImageProcessor ip1Temp = ip1.duplicate().convertToShort(false);
		ImageProcessor ip2Temp = ip2.duplicate().convertToShort(false);
		ip1Temp.copyBits(ip2Temp, 0, 0, ShortBlitter.AND);
		
		return ip1Temp;
	}

	public static ImageProcessor maskUnion( ImageProcessor ip1, ImageProcessor ip2 ) {
		// Should we convert to mask to be sure?
		ImageProcessor ip1Temp = ip1.duplicate().convertToShort(false);
		ImageProcessor ip2Temp = ip2.duplicate().convertToShort(false);
		ip1Temp.copyBits(ip2Temp, 0, 0, ShortBlitter.OR);
		
		return ip1Temp;
	}

	public static ImageProcessor minus( ImageProcessor ip1, ImageProcessor ip2 ) {
		// Should we convert to mask to be sure?
		ImageProcessor ip1Temp = ip1.duplicate().convertToShort(false);
		ImageProcessor ip2Temp = ip2.duplicate().convertToShort(false);
		ip1Temp.copyBits(ip2Temp, 0, 0, ShortBlitter.SUBTRACT);
		
		return ip1Temp;
	}

	
}


/*

def maskArea(ip):
	""" compute the number of pixels of an image mask (pixels == 1) """
	stats = ip.getStatistics()
	return stats.area * stats.mean / ip.maxValue()

def area(ip):
	""" compute the number of pixels of an image """
	stats = ip.getStatistics()
	return stats.area

def sumOfSquares(ip):
	""" compute the sum of the squares of the pixels of an image """
	ipTemp = ip.duplicate()
	ipTemp = ipTemp.convertToShort(0)
	ipTemp.sqr()
	stats = ipTemp.getStatistics()
	return stats.mean * stats.area

def sumOfProduct(ip1,ip2):
	""" compute the sum of the product image of two images """
	ip1Temp = ip1.duplicate()
	ip1Temp = ip1Temp.convertToShort(0)
	ip2Temp = ip2.duplicate()
	ip2Temp = ip2Temp.convertToShort(0)
	ip1Temp.copyBits(ip2Temp, 0, 0, ShortBlitter.MULTIPLY)
	stats = ip1Temp.getStatistics()
	return stats.mean * stats.area

def mask(ip, valueA, valueB):
	""" Mask the image with a threshold value ranging between valueA and valueB"""
	ip.setThreshold(valueA, valueB, ImageProcessor.NO_LUT_UPDATE)
	imp = ImagePlus('Mask', ip)
	IJ.run(imp, "Convert to Mask", "")
	
	return imp

def maskIntersection(ip1,ip2):
	""" Intersection of 2 images by their masks excluding zero pixels  """
	ip1Temp = ip1.duplicate()
	ip1Temp = ip1Temp.convertToShort(0)
	ip2Temp = ip2.duplicate()
	ip2Temp = ip2Temp.convertToShort(0)
	imp1 = mask(ip1Temp, 1, ip1Temp.maxValue())
	imp2 = mask(ip2Temp, 1, ip2Temp.maxValue())
	#imp1.show()
	#imp2.show()
	ip1Temp = imp1.getProcessor()
	ip2Temp = imp2.getProcessor()
	ip1Temp.copyBits(imp2.getProcessor(), 0, 0, ShortBlitter.AND)
	return ip1Temp

def maskUnion(ip1,ip2):
	""" Intersection of 2 images by their masks excluding zero pixels  """
	ip1Temp = ip1.duplicate()
	ip1Temp = ip1Temp.convertToShort(0)
	ip2Temp = ip2.duplicate()
	ip2Temp = ip2Temp.convertToShort(0)
	imp1 = mask(ip1Temp, 1, ip1Temp.maxValue())
	imp2 = mask(ip2Temp, 1, ip2Temp.maxValue())
	#imp1.show()
	#imp2.show()
	ip1Temp = imp1.getProcessor()
	ip2Temp = imp2.getProcessor()
	ip1Temp.copyBits(imp2.getProcessor(), 0, 0, ShortBlitter.OR)
	return ip1Temp

def minus(ip1,ip2):
	""" compute the subtraction image of two images """
	ip1Temp = ip1.duplicate()
	ip1Temp = ip1Temp.convertToShort(0)
	ip2Temp = ip2.duplicate()
	ip2Temp = ip2Temp.convertToShort(0)
	ip1Temp.copyBits(ip2Temp, 0, 0, ShortBlitter.SUBTRACT)
	return ip1Temp

def test_mask():
	""" Test of the function: mask(ip, valueA, valueB) """
	imp = IJ.openImage(filePathTest.getAbsolutePath())
	ip = imp.getProcessor().duplicate()
	valueA = 0
	valueB = 1
	mask(ip, valueA, valueB).show()

*/