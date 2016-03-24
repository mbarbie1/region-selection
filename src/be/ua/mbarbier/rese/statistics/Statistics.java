package be.ua.mbarbier.rese.statistics;

public class Statistics {

	public static double[] toDoubleArray( byte[] p) {
		double[] a = new double[p.length];
		for (int i = 0; i < a.length; i++ ) a[i] = (double) p[i];
		return a;
	}
	public static double[] toDoubleArray( int[] p) {
		double[] a = new double[p.length];
		for (int i = 0; i < a.length; i++ ) a[i] = (double) p[i];
		return a;
	}
	public static double[] toDoubleArray( long[] p) {
		double[] a = new double[p.length];
		for (int i = 0; i < a.length; i++ ) a[i] = (double) p[i];
		return a;
	}
	public static double[] toDoubleArray( short[] p) {
		double[] a = new double[p.length];
		for (int i = 0; i < a.length; i++ ) a[i] = (double) p[i];
		return a;
	}
	public static double[] toDoubleArray( float[] p) {
		double[] a = new double[p.length];
		for (int i = 0; i < a.length; i++ ) a[i] = (double) p[i];
		return a;
	}

	public static double mean( double[] num) {
		double out = 0.0;
		for (double d: num)	out += d;
		return out / num.length;
	}

	public static double median( double[] num) {
		java.util.Arrays.sort( num.clone() );
		double out;
		if (num.length % 2 == 0)
			out = ((double) num[num.length/2] + (double) num[num.length/2 - 1])/2;
		else 
			out = (double) num[num.length/2];		

		return out;
	}

	public static double sum( double[] num) {
		double out = 0.0;
		for (double d: num)	out += d;
		return out;
	}

	public static double var( double[] num) {
		double out = 0.0;
		double mu = mean( num );
		for (double d: num)	out += Math.pow( d-mu, 2);
		return out / num.length;
	}

	public static double sd( double[] num) {
		return Math.sqrt( var( num ) );
	}

	public static double max( double[] num) {
		double out = 0.0;
		for ( double d: num) if ( d > out ) out = d;
		return out;
	}

	public static int maxIndex( double[] num) {
		int out = 0;
		double max = 0.0;
		for ( int i = 0; i < num.length; i++ ) {
			if ( num[i] > max ) {
				out = i;
				max = num[i];
			}
		}
		return out;
	}

	public static double min( double[] num) {
		double out = num[0];
		for ( double d: num) if ( d < out ) out = d;
		return out;
	}

	public static int minIndex( double[] num) {
		int out = 0;
		double min = num[0];
		for ( int i = 0; i < num.length; i++ ) {
			if ( num[i] < min ) {
				out = i;
				min = num[i];
			}
		}
		return out;
	}

}
