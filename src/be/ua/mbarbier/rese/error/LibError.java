/**
 * 
 */
package be.ua.mbarbier.rese.error;

import java.util.LinkedHashMap;
import be.ua.mbarbier.external.Hausdorff_Distance;
import be.ua.mbarbier.rese.image.LibUtilities;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;
import ij.process.ShortBlitter;

/**
 * @author mbarbie1
 *
 */
public class LibError {
	
	ImageProcessor ip1;
	ImageProcessor ip2;
	
	public LibError( ImagePlus imp1, ImagePlus imp2 ) {
		this.ip1 = imp1.getProcessor();
		this.ip2 = imp2.getProcessor();
	}

	/**
	 * The crossCorrelation (unnormalized) is defined as 
	 * CC(X,Y) = sum_i(Xi*Yi) / sqrt( sum_i(Xi)^2 * sum_i(Yi)^2 )
	 * 
	 * @return crossCorrelation (unnormalized)
	 */
	public double CC() {
		double cc = 0;
		double denom = Math.sqrt( LibUtilities.sumOfSquares( ip1 ) * LibUtilities.sumOfSquares( ip2 ) );
		double num = LibUtilities.sumOfProduct( ip1, ip2 );
		cc = num / denom;

		return cc;
	}

	/**
	 * The normalized crossCorrelation is defined as 
	 * NCC(X,Y) = sum_i((Xi-X)*(Yi-Y)) / sqrt( sum_i(Xi-X)^2 * sum_i(Yi-Y)^2 )
	 * 
	 * @return normalized crossCorrelation
	 */
	public double NCC() {
		double ncc = 0;
		ImageStatistics stats1 = ip1.getStatistics();
		ImageStatistics stats2 = ip2.getStatistics();
		ImageProcessor ipd1 = ip1.duplicate();
		ImageProcessor ipd2 = ip2.duplicate();
		ipd1.subtract(stats1.mean);
		ipd2.subtract(stats2.mean);
		double denom = Math.sqrt( LibUtilities.sumOfSquares( ipd1 ) * LibUtilities.sumOfSquares( ipd2 ) );
		double num = LibUtilities.sumOfProduct( ipd1, ipd2 );
		ncc = num / denom;

		return ncc;
	}

	/**
	 * The MSE (= Mean Square Error) is defined as
	 * MSE(X,Y) = sum_i(Yi-Xi)^2 / N, 
	 * Here we take into account only occupied pixels 
	 * (only pixels where at least one of the images is nonzero)
	 * 
	 * @return Mean Square Error (using only occupied pixels)
	 */
	public double MSE_nonEmpty() {
		ImageProcessor ip1Temp = ip1.duplicate();
		ip1Temp.copyBits(ip2, 0, 0, ShortBlitter.SUBTRACT);

		return LibUtilities.sumOfSquares( ip1Temp ) / LibUtilities.maskArea(  LibUtilities.maskUnion( ip1, ip2 ) );
	}

	/**
	 * The MSE (= Mean Square Error) is defined as 
	 * MSE(X,Y) = sum_i(Yi-Xi)^2 / N
	 * 
	 * @return Mean Square Error
	 */
	public double MSE() {
		ImageProcessor ip1Temp = ip1.duplicate();
		ip1Temp.copyBits(ip2, 0, 0, ShortBlitter.SUBTRACT);

		return LibUtilities.sumOfSquares( ip1Temp ) / LibUtilities.area( ip1 );
	}

	/**
	 * The RMSE (= Root Mean Square Error) is defined as 
	 * RMSE(X,Y)  = sqrt[ sum_i(Yi-Xi)^2 / N ]
	 * 
	 * @return Root Mean Square Error
	 */
	public double RMSE() {

		return Math.sqrt( MSE_nonEmpty() );
	}

	/**
	 * The normalized RMSE (= Root Mean Square Error) is defined as 
	 * NRMSE(X,Y)  = sqrt[ sum_i(Yi-Xi)^2 / N ] / ( max(Yi) - min(Yi) )
	 * 
	 * @return normalized RMSE
	 */
	public double NRMSE() {
		ImageStatistics stats = ip1.getStatistics();

		return RMSE() / (stats.max-stats.min);
	}

	/**
	 * The Coefficient of Variation normalized RMSE (= Root Mean Square Error) is defined as 
	 * CVRMSE(X,Y)  = sqrt[ sum_i(Yi-Xi)^2 / N ] / mean(Yi) )
	 * 
	 * @return Coefficient of Variation
	 */
	public double CVRMSE() {
		ImageStatistics stats = ip1.getStatistics();

		return RMSE() / ( stats.mean );
	}

	/**
	 * Calculation of the pixel intensity based error measures between both images:
	 * 	Cross-correlation
	 *  Normalized cross-correlation
	 *  Mean Square Error
	 *  Root Mean Square Error
	 *  Normalized Root Mean Square Error 
	 *  Coefficient of Variation
	 * 
	 * @return LinkedHashMap of error measures
	 */
	public LinkedHashMap<String, Double> measureError() {
		ImageStatistics stats = ip1.getStatistics();

		double mse = LibUtilities.sumOfSquares( LibUtilities.minus(ip1,ip2) ) / LibUtilities.area(ip1);
		double rmse = Math.sqrt( mse );
		double n_rmse = rmse / (stats.max-stats.min);
		double cv_rmse = rmse / stats.mean;

		LinkedHashMap<String, Double> error = new LinkedHashMap<String, Double>();
		error.put("cc", CC() );
		error.put("ncc", NCC() );
		error.put("mse_roi", MSE_nonEmpty());
		error.put("mse", mse );
		error.put("rmse", rmse );
		error.put("n_rmse", n_rmse );
		error.put("cv_rmse", cv_rmse );
		return error;
	}

	/**
	 * The VOP (= Volume Overlap Percentage, or SI = Similarity Index) is defined as:
	 *  VOPj  =  2 * V( intersection(Aj,Bj) ) / ( V(Aj) + V(Bj) )
	 *  with V() the volume function, label j, and labeled pixels in image A and B.
	 * 
	 * @return VOP = si, the VOP = si1 with reference of the first image, the VOP = si2 with reference of the second image
	 */
	public LinkedHashMap<String, Double> VOP() {
		ImageProcessor mask1 = LibUtilities.mask( ip1, 0.0, 1.0 ).getProcessor();
		ImageProcessor mask2 = LibUtilities.mask( ip2, 0.0, 1.0 ).getProcessor();
		double MaskArea1 = LibUtilities.maskArea( mask1 );
		double MaskArea2 = LibUtilities.maskArea( mask2 );
		ImageProcessor mI = LibUtilities.maskIntersection( mask1, mask2 );
		double MaskAreaI = LibUtilities.maskArea(mI);
		double si = 2 * MaskAreaI / ( MaskArea1 + MaskArea2);
		double si1 = MaskAreaI / MaskArea1;
		double si2 = MaskAreaI / MaskArea2;
		LinkedHashMap<String, Double> vop = new LinkedHashMap<String, Double>();
		vop.put( "si", si );
		vop.put( "si1", si1 );
		vop.put( "si2", si2 );
		
		return vop;
	}

	/**
	 * The Hausdorff distance is the maximum of the minimal border distance between the pixels of the two mask borders
	 * The averaged Hausdorff distance is the average of the minimal border distance between the pixels of the two mask borders
	 * 
	 * @return  Hausdorff distance = hd, Averaged Hausdorff distance = hda
	 */
	public LinkedHashMap<String, Double> haussdorffError() {
		LinkedHashMap<String, Double> error = new LinkedHashMap<String, Double>();
		ImagePlus mask1 = LibUtilities.mask( ip1, 0.0, 1.0 );
		ImagePlus mask2 = LibUtilities.mask( ip2, 0.0, 1.0 );
		Hausdorff_Distance hd = new Hausdorff_Distance();
		hd.exec( mask1, mask2 );
		error.put("hd", hd.getHausdorffDistance() );
		error.put("hda", hd.getAveragedHausdorffDistance() );

		return error;
	}

	
}
