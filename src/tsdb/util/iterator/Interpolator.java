package tsdb.util.iterator;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.linear.SingularMatrixException;
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression;
import org.apache.commons.math3.stat.regression.SimpleRegression;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.TimeUtil;

/**
 * GabFiller interpolates missing values (NaN-values) in time series with data of a set of other time series
 * @author woellauer
 *
 */
public class Interpolator {

	private static final Logger log = LogManager.getLogger();

	/**
	 * count of values for training to fill one gap
	 */
	public static final int TRAINING_VALUE_COUNT = 4*7*24; // four weeks with one hour time interval

	/**
	 * maximum count of previous interpolated values in target training values
	 */
	//public static final int MAX_INTERPOLATED_IN_TRAINING_COUNT = 7*24;
	public static final int MAX_INTERPOLATED_IN_TRAINING_COUNT = TRAINING_VALUE_COUNT; //!! TODO
	
	private static final double MAX_MSE = 7d;

	/**
	 * minimum of different sources to interpolate from
	 */
	public static final int MIN_TRAINING_SOURCES = 2;

	/**
	 * Creates an array with size of TRAINING_VALUE_COUNT and checks count of previous interpolated data values
	 * @param gapPos
	 * @param data
	 * @param targetInterpolationFlags
	 * @return
	 */
	private static double[] createTargetTrainingArray(int gapPos, float[] data, boolean[] targetInterpolationFlags) {
		double[] result = new double[TRAINING_VALUE_COUNT];
		int startPos = gapPos-TRAINING_VALUE_COUNT;
		int interpolatedCounter = 0;
		for(int i=startPos;i<gapPos;i++) {
			if(Float.isNaN(data[i])) {
				return null;
			} else {
				if(targetInterpolationFlags[i]) {
					interpolatedCounter++;
					if(interpolatedCounter>MAX_INTERPOLATED_IN_TRAINING_COUNT) { // to much interpolated values in target
						return null;
					}
				}				
				result[i-startPos] = data[i];
			}
		}
		return result;
	}

	/**
	 * Creates an array with size of TRAINING_VALUE_COUNT + 1 of training data plus values for interpolation value calculation.
	 * @param gapPos
	 * @param data
	 * @return
	 */
	private static double[] createSourceTrainingArray(int gapPos, float[] data) {
		double[] result = new double[TRAINING_VALUE_COUNT+1];
		int startPos = gapPos-TRAINING_VALUE_COUNT;		
		for(int i=startPos;i<=gapPos;i++) {
			if(Float.isNaN(data[i])) {
				return null;
			} else {
				result[i-startPos] = data[i];
			}
		}
		return result;
	}

	/**
	 * Creates a matrix of all training arrays as input for OLSMultipleLinearRegression.
	 * @param trainingSourceList
	 * @return
	 */
	private static double[][] createTrainingMatrix(List<double[]> trainingSourceList) {		
		double[][] trainingMatrix = new double[TRAINING_VALUE_COUNT][trainingSourceList.size()];

		for(int sourceNr=0; sourceNr<trainingSourceList.size();sourceNr++) {
			double[] source = trainingSourceList.get(sourceNr);
			for(int rowNr=0;rowNr<TRAINING_VALUE_COUNT;rowNr++) {
				trainingMatrix[rowNr][sourceNr] = source[rowNr];
			}
		}		

		return trainingMatrix;
	}


	/**
	 * Tries to interpolate all nan-Values in target and sets for interpolated values flags in targetInterpolationFlags.
	 * @param inputSource
	 * @param target
	 * @param targetInterpolationFlags
	 * @return
	 */
	private static int processMultiLinearInternal(float[][] inputSource, float[] target, boolean[] targetInterpolationFlags) {
		int interpolatedgapCount = 0;
		for(int gapPos=TRAINING_VALUE_COUNT;gapPos<target.length;gapPos++) {
			if(Float.isNaN(target[gapPos])) { // gap found at gapPos
				double[] trainingTarget = createTargetTrainingArray(gapPos,target, targetInterpolationFlags);
				if(trainingTarget!= null) { // valid training target
					ArrayList<double[]> trainingSourceList = new ArrayList<double[]>(inputSource.length);
					for(int sourceNr=0;sourceNr<inputSource.length;sourceNr++) {
						double[] trainingSource = createSourceTrainingArray(gapPos, inputSource[sourceNr]);
						if(trainingSource!=null) {
							trainingSourceList.add(trainingSource);
						}
					}
					if(trainingSourceList.size()>=MIN_TRAINING_SOURCES) { // enough training sources
						try {
							double[][] trainingMatrix = createTrainingMatrix(trainingSourceList);
							OLSMultipleLinearRegression olsMultipleLinearRegression = new OLSMultipleLinearRegression();
							olsMultipleLinearRegression.newSampleData(trainingTarget, trainingMatrix);
							double[] regressionParameters = olsMultipleLinearRegression.estimateRegressionParameters();
							//*** fill gap
							double gapValue = regressionParameters[0];
							for(int sourceIndex=0; sourceIndex<trainingSourceList.size(); sourceIndex++) {							
								gapValue += trainingSourceList.get(sourceIndex)[TRAINING_VALUE_COUNT]*regressionParameters[sourceIndex+1];							
							}
							target[gapPos] = (float) gapValue;
							targetInterpolationFlags[gapPos] = true;						
							interpolatedgapCount++;
							//***
						} catch(SingularMatrixException e) {
							log.warn("multilinear interpolation not possible: "+e.toString()+" at "+gapPos);
						}
					}
				}
			}
		}
		return interpolatedgapCount;
	}


	/**
	 * Process gap filling of one target time series and one parameter. Some data in source time series is allowed to be
	 * left out.
	 * @param sourceTimeSeries
	 * @param targetTimeSeries
	 * @param interpolationName the sensor name that should be gap filled
	 */
	public static int processMultiLinear(TimeSeries[] sourceTimeSeries, TimeSeries targetTimeSeries, String interpolationName) {
		final int timeStep = targetTimeSeries.timeStep;
		long startTimestamp = targetTimeSeries.getFirstTimestamp();
		long endTimestamp = targetTimeSeries.getLastTimestamp();
		float[] target = targetTimeSeries.getValues(interpolationName);
		boolean[] targetInterpolationFlags = targetTimeSeries.getInterpolationFlags(interpolationName);

		//float[][] source = new float[sourceTimeSeries.length][];
		ArrayList<float[]> sourceList = new ArrayList<float[]>(sourceTimeSeries.length);
		for(int i=0;i<sourceTimeSeries.length;i++) {
			if(sourceTimeSeries[i]!=null && sourceTimeSeries[i].containsParamterName(interpolationName)) {
				if(startTimestamp!=sourceTimeSeries[i].getFirstTimestamp()) {
					log.error("all sources need to have same startTimestamp");
					return 0;
				}
				if(endTimestamp!=sourceTimeSeries[i].getLastTimestamp()) {
					log.error("all sources need to have same endTimestamp: "+TimeUtil.oleMinutesToText(endTimestamp)+"  "+TimeUtil.oleMinutesToText(sourceTimeSeries[i].getLastTimestamp()));
					return 0;
				}
				if(timeStep!=sourceTimeSeries[i].timeStep) {
					log.error("all sources need to have same time step");
					return 0;
				}
				sourceList.add(sourceTimeSeries[i].getValues(interpolationName));
			}
		}

		int interpolatedCount = processMultiLinearInternal(sourceList.toArray(new float[0][]),target,targetInterpolationFlags);
		return interpolatedCount;
	}

	public static int processOneValueGaps(TimeSeries timeSeries) {//TODO set interpolated flag
		int interpolatedCount = 0;
		for(int colIndex=0;colIndex<timeSeries.data.length;colIndex++) {
			float[] colData = timeSeries.data[colIndex];
			for(int rowIndex=1;rowIndex<colData.length-1;rowIndex++) {
				if(Float.isNaN(colData[rowIndex])&&(!Float.isNaN(colData[rowIndex-1]))&&(!Float.isNaN(colData[rowIndex+1]))) {
					colData[rowIndex] = (colData[rowIndex-1]+colData[rowIndex+1])/2;
					interpolatedCount++;
				}
			}
		}
		return interpolatedCount;
	}

	public static int processLinear(TimeSeries[] interpolationTimeSeries, TimeSeries sourceTimeSeries, String interpolationName) {
		int interpolated_counter = 0;
		final int timeStep = sourceTimeSeries.timeStep;
		long startTimestamp = sourceTimeSeries.getFirstTimestamp();
		long endTimestamp = sourceTimeSeries.getLastTimestamp();
		float[] ys = sourceTimeSeries.getValues(interpolationName);
		boolean[] targetInterpolationFlags = sourceTimeSeries.getInterpolationFlags(interpolationName);

		double[] intercepts = new double[interpolationTimeSeries.length];
		double[] slopes = new double[interpolationTimeSeries.length];
		float[][] xss = new float[interpolationTimeSeries.length][];

		for(int i=0;i<interpolationTimeSeries.length;i++) {
			if(interpolationTimeSeries[i].containsParamterName(interpolationName)) {
				if(startTimestamp!=interpolationTimeSeries[i].getFirstTimestamp()) {
					log.error("all sources need to have same startTimestamp");
					return 0;
				}
				if(endTimestamp!=interpolationTimeSeries[i].getLastTimestamp()) {
					log.error("all sources need to have same endTimestamp: "+TimeUtil.oleMinutesToText(endTimestamp)+"  "+TimeUtil.oleMinutesToText(interpolationTimeSeries[i].getLastTimestamp()));
					return 0;
				}
				if(timeStep!=interpolationTimeSeries[i].timeStep) {
					log.error("all sources need to have same time step");
					return 0;
				}
				xss[i] = interpolationTimeSeries[i].getValues(interpolationName);
			}
		}

		for(int i=0;i<interpolationTimeSeries.length;i++) {
			float[] xs = xss[i];
			if(xs!=null) {
				SimpleRegression simpleRegression = new SimpleRegression();
				for(int pos=0;pos<ys.length;pos++) {
					float x = xs[pos];
					float y = ys[pos];
					if(Float.isFinite(x)&&Float.isFinite(y)) {
						simpleRegression.addData(x, y);
					}
				}
				if(TRAINING_VALUE_COUNT<=simpleRegression.getN()&&simpleRegression.getMeanSquareError()<=MAX_MSE) {
					//y = intercept + slope * x
					double intercept = simpleRegression.getIntercept();
					double slope = simpleRegression.getSlope();
					intercepts[i] = intercept;
					slopes[i] = slope;
					log.info("linear regression "+intercept+" "+slope+" "+simpleRegression.getMeanSquareError());
				} else {
					intercepts[i] = Float.NaN;
					slopes[i] = Float.NaN;
				}
			} else {
				intercepts[i] = Float.NaN;
				slopes[i] = Float.NaN;
			}
		}

		for(int pos=0;pos<ys.length;pos++) {
			if(Float.isNaN(ys[pos])) {
				double count=0;
				double sum=0;
				for(int i=0;i<interpolationTimeSeries.length;i++) {
					float[] xs = xss[i];
					if( (!Float.isNaN(xs[pos])) && (!Double.isNaN(slopes[i])) ) {
						sum += (float) (intercepts[i] + slopes[i] * xs[pos]);
						count++;
					}
				}
				if(count>0) {
					ys[pos] = (float) (sum/count);
					targetInterpolationFlags[pos] = true;
					interpolated_counter++;
				}
			}
		}
		return interpolated_counter;
	}
}
