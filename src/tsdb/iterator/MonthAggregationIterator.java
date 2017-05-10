package tsdb.iterator;

import java.time.LocalDateTime;
import java.time.Month;

import tsdb.TsDB;
import tsdb.component.Sensor;
import tsdb.util.AggregationType;
import tsdb.util.TimeUtil;
import tsdb.util.TsSchema.Aggregation;
import tsdb.util.iterator.TsIterator;

/**
 * Aggregate day data to month data.
 * @author woellauer
 *
 */
public class MonthAggregationIterator extends AbstractAggregationIterator  {

	public MonthAggregationIterator(TsDB tsdb, TsIterator input_iterator) {
		super(tsdb, input_iterator, createSchemaFromConstantToVariableStep(input_iterator.getSchema(), 24*60, Aggregation.MONTH));
	}
	
	@Override
	protected long calcAggregationTimestamp(long timestamp) {
		LocalDateTime datetime = TimeUtil.oleMinutesToLocalDateTime(timestamp);
		int year = datetime.getYear();
		Month month = datetime.getMonth();
		LocalDateTime aggregationDatetime = LocalDateTime.of(year,month,1,0,0);
		return TimeUtil.dateTimeToOleMinutes(aggregationDatetime);
	}

	@Override
	protected boolean isValidAggregate(int collectorCount, AggregationType aggregationType) {
		return 27<=collectorCount;
	}
	
	@Override
	protected AggregationType[] getAggregationTypes(Sensor[] sensors) {
		AggregationType[] aggregation = new AggregationType[sensors.length];
		for (int i = 0; i < aggregation.length; i++) {
			aggregation[i] = sensors[i].getAggregationMonth();
		}
		return aggregation;
	}
}