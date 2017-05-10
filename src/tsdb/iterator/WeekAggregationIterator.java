package tsdb.iterator;

import tsdb.TsDB;
import tsdb.component.Sensor;
import tsdb.util.AggregationType;
import tsdb.util.iterator.TsIterator;

/**
 * Aggregate day data to week data.
 * @author woellauer
 *
 */
public class WeekAggregationIterator extends AbstractAggregationIterator {

	public WeekAggregationIterator(TsDB tsdb, TsIterator input_iterator) {
		super(tsdb, input_iterator, createSchemaConstantStep(input_iterator.getSchema(), 24*60, 7*24*60));
	}
	
	@Override
	protected long calcAggregationTimestamp(long timestamp) {
		return timestamp - timestamp%(7*24*60) - (5*24*60);
	}

	@Override
	protected boolean isValidAggregate(int collectorCount, AggregationType aggregationType) {
		return 6<=collectorCount; 
	}
	
	@Override
	protected AggregationType[] getAggregationTypes(Sensor[] sensors) {
		AggregationType[] aggregation = new AggregationType[sensors.length];
		for (int i = 0; i < aggregation.length; i++) {
			aggregation[i] = sensors[i].getAggregationWeek();
		}
		return aggregation;
	}
}
