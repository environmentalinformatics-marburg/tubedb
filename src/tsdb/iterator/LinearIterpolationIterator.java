package tsdb.iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.iterator.InputProcessingIterator;
import tsdb.util.iterator.TsIterator;

public class LinearIterpolationIterator extends InputProcessingIterator {
	private static final Logger log = LogManager.getLogger();
	
	TsEntry prev = null;
	TsEntry curr = null;
	TsEntry next = null;
	
	private int interpolationCount = 0;
	
	private static TsSchema createSchema(TsSchema schema) {
		/*TimeSeriesSchema input_schema = input_iterator.getSchema();		
		String[] schema = input_schema.schema;
		boolean constantTimeStep = input_schema.constantTimeStep;
		int timeStep = input_schema.timeStep;
		boolean isContinuous = input_schema.isContinuous;		
		boolean hasQualityFlags = input_schema.hasQualityFlags;
		boolean hasInterpolatedFlags = input_schema.hasInterpolatedFlags;
		boolean hasQualityCounters = input_schema.hasQualityCounters;
		return new TimeSeriesSchema(schema, constantTimeStep, timeStep, isContinuous, hasQualityFlags, hasInterpolatedFlags, hasQualityCounters).toTsSchema();*/
		return schema.copy(); // TODO!
	}

	public LinearIterpolationIterator(TsIterator input_iterator) {
		super(input_iterator, createSchema(input_iterator.getSchema()));
		if(input_iterator.hasNext()) {
			next = input_iterator.next();
		}
	}

	@Override
	protected TsEntry getNext() {
		if(next==null) {// no elements left
			return null;
		}
		prev = curr;
		curr = next;
		if(input_iterator.hasNext()) {
			next = input_iterator.next();
		} else { //no next element
			System.out.println("LinearIterpolationIterator interpolated: "+interpolationCount);
			next = null;
		}
		if(prev!=null&&next!=null) { // interpolation possible
			boolean interpolate=false;
			for(int i=0;i<schema.length;i++) {
				if(Float.isNaN(curr.data[i])&&(!Float.isNaN(prev.data[i]))&&(!Float.isNaN(next.data[i]))) {
					interpolate = true;
					break;
				}
			}
			if(!interpolate) {
				return curr;
			}
			float[] result = new float[schema.length];
			for(int i=0;i<schema.length;i++) {
				if(Float.isNaN(curr.data[i])&&(!Float.isNaN(prev.data[i]))&&(!Float.isNaN(next.data[i]))) {
					result[i] = (prev.data[i]+next.data[i])/2;
					interpolationCount++;
				} else {
					result[i] = curr.data[i];
				}
			}
			TsEntry res = new TsEntry(curr.timestamp, result);
			log.info("-"+curr);
			log.info("+"+res);			
			return res;
		} else { // curr is first or last in iterator
			return curr;
		}
	}
}
