package tsdb.iterator;

import java.util.Arrays;

import tsdb.util.TsEntry;
import tsdb.util.Util;
import tsdb.util.iterator.InputIterator;
import tsdb.util.iterator.TsIterator;

public class InterpolationAverageLinearIterator extends InputIterator {
	//private static final Logger log = LogManager.getLogger();

	private final TsIterator[] interpolationIterators;
	private final double[][] intercepts;
	private final double[][] slopes;
	private final int[] posIndex;

	public InterpolationAverageLinearIterator(TsIterator input_iterator, TsIterator[] interpolationIterators, double[][] intercepts, double[][] slopes) {
		super(input_iterator, input_iterator.getSchema());
		this.interpolationIterators = interpolationIterators;
		this.intercepts = intercepts;
		this.slopes = slopes;
		this.posIndex = Util.stringArrayToPositionIndexArray(interpolationIterators[0].getNames(),input_iterator.getNames(), true, true);
	}

	@Override
	public boolean hasNext() {
		return input_iterator.hasNext();
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
		
		boolean[] interpolated = new boolean[y.length];
		
		for(int i=0;i<posIndex.length;i++) {
			int pos = posIndex[i];
			if(Float.isNaN(y[pos])) {// try to interpolate
				double count=0;
				double sum=0;
				for(int itIndex=0;itIndex<interpolationIterators.length;itIndex++) {
					float[] x = interpolations[itIndex];					
					if(x!=null && !Float.isNaN(x[i]) && !Double.isNaN(intercepts[itIndex][i])) {
						//y[pos] = x[i];
						sum += (float) (intercepts[itIndex][i] + slopes[itIndex][i] * x[i]);
						count++;
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
