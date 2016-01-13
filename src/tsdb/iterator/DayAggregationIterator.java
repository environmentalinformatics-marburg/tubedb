package tsdb.iterator;

import tsdb.TsDB;
import tsdb.util.AggregationType;
import tsdb.util.iterator.TsIterator;

/**
 * aggregation iterator to day
 * @author woellauer
 *
 */
public class DayAggregationIterator extends AbstractAggregationIterator {	

	public DayAggregationIterator(TsDB tsdb, TsIterator input_iterator) {
		super(tsdb, input_iterator, createSchemaConstantStep(input_iterator.getSchema(),60,24*60));
	}

	@Override
	protected long calcAggregationTimestamp(long timestamp) {
		return timestamp - timestamp%(24*60);
	}

	@Override
	protected boolean isValidAggregate(int collectorCount, AggregationType aggregationType) {
		if(aggregationType == AggregationType.AVERAGE_ALBEDO) {
			return 5<=collectorCount;
		}
		return 22<=collectorCount;				
	}
}
