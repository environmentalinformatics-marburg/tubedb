package tsdb.iterator;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.iterator.InputProcessingIterator;
import tsdb.util.iterator.TsIterator;

/**
 * Calculates difference between consecutive time stamps
 * @author woellauer
 *
 */
public class DifferentialIterator extends InputProcessingIterator {
	
	public static TsSchema createSchema(TsSchema schema) {
		schema.throwNotContinuous();
		boolean isContinuous = true;
		return new TsSchema(schema.names, schema.aggregation, schema.timeStep, isContinuous);
	}
	
	private float[]  prev = null;
	
	public DifferentialIterator(TsIterator input_iterator) {
		super(input_iterator, createSchema(input_iterator.getSchema()));
		prev = new float[schema.length];
		for(int col=0;col<schema.length;col++) {
			prev[col] = Float.NaN;
		}
	}	

	@Override
	protected TsEntry getNext() {
		if(!input_iterator.hasNext()) {
			return null;
		}
		TsEntry element = input_iterator.next();
		float[] curr = element.data;
		float[] data = new float[schema.length];
		for(int col=0;col<schema.length;col++) {
			if(!Float.isNaN(prev[col])&&!Float.isNaN(curr[col])) {
				data[col] = curr[col]-prev[col];
			} else {
				data[col] = Float.NaN;
			}
			prev[col] = curr[col];
		}
		return new TsEntry(element.timestamp, data);
	}
}
