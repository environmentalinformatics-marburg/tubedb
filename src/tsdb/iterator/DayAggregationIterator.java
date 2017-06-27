package tsdb.iterator;

import tsdb.TsDB;
import tsdb.component.Sensor;
import tsdb.util.AggregationType;
import tsdb.util.TsEntry;
import tsdb.util.Mutator;
import tsdb.util.iterator.TsIterator;

/**
 * Aggregate hour data to day data.
 * @author woellauer
 *
 */
public class DayAggregationIterator extends AbstractAggregationIterator {
	
	private final Mutator[] dayMutators;

	public DayAggregationIterator(TsDB tsdb, TsIterator input_iterator, Mutator[] dayMutators) {
		super(tsdb, input_iterator, createSchemaConstantStep(input_iterator.getSchema(),60,24*60));
		this.dayMutators = dayMutators;
	}

	@Override
	protected long calcAggregationTimestamp(long timestamp) {
		return timestamp - timestamp%(24*60);
	}

	@Override
	protected boolean isValidAggregate(int collectorCount, AggregationType aggregationType) {
		switch(aggregationType) {
		case AVERAGE_ALBEDO:
			return 5<=collectorCount;
		case LAST:
			return 1<=collectorCount;
		default:
			return 22<=collectorCount;
		}						
	}
	
	@Override
	protected AggregationType[] getAggregationTypes(Sensor[] sensors) {
		AggregationType[] aggregation = new AggregationType[sensors.length];
		for (int i = 0; i < aggregation.length; i++) {
			aggregation[i] = sensors[i].getAggregationDay();
		}
		return aggregation;
	}

	@Override
	protected TsEntry getNext() {
		TsEntry entry = super.getNext();
		if(dayMutators != null && entry != null) {
			long timestamp = entry.timestamp;
			float[] data = entry.data;
			for(Mutator mutator:dayMutators) {
				mutator.apply(timestamp, data);
			}
		}
		return entry;
	}
}
