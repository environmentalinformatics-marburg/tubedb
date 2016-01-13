package tsdb.iterator;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.TsSchema.Aggregation;
import tsdb.util.iterator.InputProcessingIterator;
import tsdb.util.iterator.TsIterator;

/**
 * Sends only input elements to output that have at least one not NaN-value in data.
 * @author woellauer
 *
 */
public class NanRemoveIterator extends InputProcessingIterator {
	
	public static TsSchema createSchema(TsSchema tsSchema) {
		String[] names = tsSchema.names;
		Aggregation aggregation = tsSchema.aggregation;
		int timeStep = tsSchema.timeStep;
		boolean isContinuous = false;
		boolean hasQualityFlags = tsSchema.hasQualityFlags;
		boolean hasInterpolatedFlags = tsSchema.hasInterpolatedFlags;
		boolean hasQualityCounters = tsSchema.hasQualityCounters;
		return new TsSchema(names, aggregation, timeStep, isContinuous, hasQualityFlags, hasInterpolatedFlags, hasQualityCounters);		
	}

	public NanRemoveIterator(TsIterator input_iterator) {
		super(input_iterator, createSchema(input_iterator.getSchema()));
	}

	@Override
	protected TsEntry getNext() {
		while(input_iterator.hasNext()) {
			TsEntry next = input_iterator.next();
			int counter = 0;
			for(float v:next.data) {
				if(!Float.isNaN(v)) {
					counter++;
				}
			}
			if(counter>0) {
				return next;
			}
		}
		return null;
	}
}
