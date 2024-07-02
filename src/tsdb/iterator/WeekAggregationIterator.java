package tsdb.iterator;

import tsdb.TsDB;
import tsdb.component.Sensor;
import tsdb.util.AggregationType;
import tsdb.util.Mutator;
import tsdb.util.TsEntry;
import tsdb.util.iterator.TsIterator;

/**
 * Aggregate day data to week data.
 * @author woellauer
 *
 */
public class WeekAggregationIterator extends AbstractAggregationIterator {
	
	private final Mutator weekMutator;

	public WeekAggregationIterator(TsDB tsdb, TsIterator input_iterator, Mutator weekMutator) {
		super(tsdb, input_iterator, createSchemaConstantStep(input_iterator.getSchema(), 24*60, 7*24*60));
		this.weekMutator = weekMutator;
	}
	
	@Override
	protected long calcAggregationTimestamp(long timestamp) {
		return timestamp - timestamp%(7*24*60) - (5*24*60);
	}

	@Override
	protected boolean isValidAggregate(int collectorCount, AggregationType aggregationType) {		
		switch(aggregationType) {
		case LAST:
			return 1<=collectorCount;
		case SUM_ALWAYS:
			return 1<=collectorCount;
		default:
			return 6<=collectorCount; 
		}		
	}
	
	@Override
	protected AggregationType[] getAggregationTypes(Sensor[] sensors) {
		AggregationType[] aggregation = new AggregationType[sensors.length];
		for (int i = 0; i < aggregation.length; i++) {
			aggregation[i] = sensors[i].getAggregationWeek();
		}
		return aggregation;
	}
	
	@Override
	protected TsEntry getNext() {
		TsEntry entry = super.getNext();
		if(weekMutator != null && entry != null) {
			weekMutator.apply(entry.timestamp, entry.data);
		}
		return entry;
	}
}
