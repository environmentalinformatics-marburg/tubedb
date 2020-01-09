package tsdb.testing;

import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;

/**
 * testing OLSMultipleLinearRegression
 * @author woellauer
 *
 */
public class TestingInterpolation {	
	
	public static void main(String[] args) {
		
		//test1();
		testRandom();
		
	}
	
	public static void printArray(double[] a) {
		for(int i=0;i<a.length;i++) {
			System.out.format("%.2f  ",a[i]);
		}
		System.out.println();
	}
	
	
	public static void test1() {
		double[] y = new double[]{3,4,5,6};
		double[][]x = new double[][]{{1.1,1.01,0.9},{2.0,2.00,1.8},{3.1,3,3.1},{4,4.01,5.9}};
		
		OLSMultipleLinearRegression reg = new OLSMultipleLinearRegression();
		reg.newSampleData(y, x);
		
		double[] a = reg.estimateRegressionParameters();		
		printArray(a);
		
		double[] z = new double[]{1.5,1.5};
		double result = a[0]+z[0]*a[1]+z[1];
		System.out.println("result: "+result);		
	}
	
	public static void testRandom() {

		final int dataEntries = 4*7*24*100;
		final int variables = 15;

		double[] y = new double[dataEntries];
		double[][] x = new double[dataEntries][variables];

		System.out.println("generate data...");

		for(int i=0;i<dataEntries;i++) {
			y[i] = Math.random()*1000d;
			for(int j=0;j<variables;j++) {
				x[i][j] = Math.random()*10;
			}
		}

		double[] a=null;
		long start = System.nanoTime();
		for(int k=0;k<24*31;k++) {
			System.out.println("init regression...");
			OLSMultipleLinearRegression reg = new OLSMultipleLinearRegression();
			reg.newSampleData(y, x);

			System.out.println("estimate regression parameters...");
			a = reg.estimateRegressionParameters();

			System.out.println("...done");
		}
		long end = System.nanoTime();

		System.out.format("time: %.3f s \n",((end-start)/1000_000_000d));

		printArray(a);




	}
	
	
	
	
	

}
