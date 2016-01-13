package tsdb.iterator;

import java.util.ArrayList;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.iterator.MoveIterator;
import tsdb.util.processingchain.ProcessingChain;

/**
 * Evaluates collections from CollectingAggregator to statics: min, max, median, quantiles 
 * @author woellauer
 *
 */
public class EvaluatingAggregationIterator extends MoveIterator {

	private final CollectingAggregator collectingAggregator;

	public EvaluatingAggregationIterator(TsSchema schema, CollectingAggregator 	collectingAggregator) {
		super(schema);
		this.collectingAggregator = collectingAggregator;
	}

	@Override
	protected TsEntry getNext() {		
		long timestamp = collectingAggregator.calcNextOutput();
		if(timestamp<0) {
			return null;
		}
		ArrayList<Float>[] inputs = collectingAggregator.getOutputs();
		ArrayList<Float> values = inputs[0];
		
		if(values.isEmpty()) {
			return TsEntry.of(timestamp, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN);
		}
		
		values.sort(null);

		int size = values.size();
		float min = values.get(0);
		float q1;
		float med;
		float q3;
		float max = values.get(size-1);

		//[0] size(1)
		//[0] 1  size(2)
		//[0] 1  2  size(3)
		// 0[]1  2  3  size(4)     4%4=0  ->  (0+1)/2
		// 0 [1] 2  3  4  size(5)
		// 0 [1] 2  3  4  5  size(6)
		// 0 [1] 2  3  4  5  6  size(7)
		// 0  1[]2  3  4  5  6  7  size(8)    8%4=0 -> (1+2)/2
		// 0  1 [2] 3  4  5  6  7  8  size(9)

		// 0 1 2 3 4 5 6 7 
		if(size%4==0) { 
			int quarter = size/4;
			q1 = (values.get(quarter-1)+values.get(quarter))/2;
		} else {
			q1 = values.get(size/4);
		}


		if(size%2==0) { // 0 1[]2 3    size(4)    ([1]+[2])/2
			int half = size/2;
			med = (values.get(half-1)+values.get(half))/2;
		} else { // 0 1 [2] 3 4  size(5)     [2]
			med = values.get(size/2);
		}

		//[0] size(1)
		// 0 [1] size(2)
		// 0  1 [2] size(3)
		// 0  1  2[]3  size(4)     4%4=0  ->  ([2]+[3])/2
		// 0  1  2 [3] 4  size(5)
		// 0  1  2  3 [4] 5  size(6)
		// 0  1  2  3  4 [5] 6  size(7)
		// 0  1  2  3  4  5[]6  7  size(8)    8%4=0 -> ([5]+[6])/2
		// 0  1  2  3  4  5 [6] 7  8  size(9)
		
		if((size*3)%4==0) {
			int three_quarter = (size*3)/4;
			q3 = (values.get(three_quarter-1)+values.get(three_quarter))/2;
		} else {
			q3 = values.get((size*3)/4);
		}
		
		float iqr_1_5 = 1.5f*(q3-q1);
		float dmin = q1-iqr_1_5;
		float dmax = q3+iqr_1_5;
		if(dmin<min) {
			dmin = min;
		}
		if(max<dmax) {
			dmax = max;
		}


		return TsEntry.of(timestamp, min, dmin, q1, med, q3, dmax, max);
	}
	
	@Override
	public ProcessingChain getProcessingChain() {		
		return ProcessingChain.of(collectingAggregator,this);
	}	

}
