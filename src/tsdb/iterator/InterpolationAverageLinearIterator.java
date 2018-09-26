package tsdb.iterator;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.TsEntry;
import tsdb.util.Util;
import tsdb.util.iterator.InputIterator;
import tsdb.util.iterator.TsIterator;

/**
 * Interpolates missing data values in iterator with interpolated data from other iterators with avarege of multiple linear regressions.
 * @author woellauer
 *
 */
public class InterpolationAverageLinearIterator extends InputIterator {
	private static final Logger log = LogManager.getLogger();

	private final TsIterator[] interpolationIterators;
	private final double[][] intercepts;
	private final double[][] slopes;
	private final double[][] weights;
	private final int[] posIndex;

	/**
	 * Creates interpolation iterator.
	 * @param input_iterator source data entry of which missing data should be filled.
	 * @param interpolationIterators Array of reference iterators.
	 * @param intercepts Array of intercepts to reference iterators from linear regression for each column in interpolationSchema.
	 * <br>intercept == intercepts[interpolationIteratorNo][interpolationSchemaNo]
	 * @param slopes Array of slopes to reference iterators from linear regression for each column in interpolationSchema.
	 * <br>slope == slopes[interpolationIteratorNo][interpolationSchemaNo]
	 * @param interpolationSchema Array of column names that should be interpolated.
	 * @param weights 
	 */
	public InterpolationAverageLinearIterator(TsIterator input_iterator, TsIterator[] interpolationIterators, double[][] intercepts, double[][] slopes, String[] interpolationSchema, double[][] weights) {
		super(input_iterator, input_iterator.getSchema());
		this.interpolationIterators = interpolationIterators;
		this.intercepts = intercepts;
		this.slopes = slopes;
		this.weights = weights;
		this.posIndex = Util.stringArrayToPositionIndexArray(interpolationSchema, input_iterator.getNames(), true, true);
	}

	@Override
	public TsEntry next() {
		TsEntry e = input_iterator.next();
		float[] y = Arrays.copyOf(e.data,e.data.length);
		
		float[][] interpolations = new float[interpolationIterators.length][];
		for(int i=0;i<interpolationIterators.length;i++) {
			if(interpolationIterators[i]!=null) {
				interpolations[i] = interpolationIterators[i].next().data;
			}			
		}
		
		/*boolean gapFree = true;
		for(int i:pos) {
			if(Float.isNaN(data[i])) {
				gapFree = false;
				break;
			}
		}
		if(gapFree) {
			return e;
		}*/
		
		boolean[] interpolated = e.interpolated == null ? new boolean[y.length] : e.interpolated;
		
		for(int i=0;i<posIndex.length;i++) {
			int pos = posIndex[i];
			if(Float.isNaN(y[pos])) {// try to interpolate
				double count=0;
				double sum=0;
				for(int itIndex=0;itIndex<interpolationIterators.length;itIndex++) {
					float[] x = interpolations[itIndex];					
					if(x!=null && !Float.isNaN(x[i]) && !Double.isNaN(intercepts[itIndex][i])) {
						//y[pos] = x[i];
						double weight = weights[itIndex][i];
						sum += (float) ((intercepts[itIndex][i] + slopes[itIndex][i] * x[i]) * weight); // weighted regression
						//sum += (float) (x[i] * weight); // weighted value
						//count++;
						count += weight;
					}
				}
				//log.info(count);
				if(count>0) {
					interpolated[pos] = true;
					y[pos] = (float) (sum/count);
					//y[pos] = (float) 20;
					//targetInterpolationFlags[pos] = true;
					//interpolated_counter++;
				} else {
					//y[pos] = (float) -20;					
				}
			}
		}
		
		return new TsEntry(e.timestamp, y,e.qualityFlag,e.qualityCounter, interpolated);
	}
}
