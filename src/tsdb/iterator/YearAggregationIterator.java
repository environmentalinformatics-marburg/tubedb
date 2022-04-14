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
 * Aggregate month data to year data.
 * @author woellauer
 *
 */
public class YearAggregationIterator extends AbstractAggregationIterator  {

	public YearAggregationIterator(TsDB tsdb, TsIterator input_iterator) {
		super(tsdb, input_iterator, createSchemaVariableStep(input_iterator.getSchema(), Aggregation.MONTH, Aggregation.YEAR));
	}

	@Override
	protected long calcAggregationTimestamp(long timestamp) {		
		LocalDateTime datetime = TimeUtil.oleMinutesToLocalDateTime(timestamp);
		int year = datetime.getYear();
		LocalDateTime aggregationDatetime = LocalDateTime.of(year,Month.JANUARY,1,0,0);
		return TimeUtil.dateTimeToOleMinutes(aggregationDatetime);		
	}

	@Override
	protected boolean isValidAggregate(int collectorCount, AggregationType aggregationType) {		
		switch(aggregationType) {
		case LAST:
			return 1<=collectorCount;
		case SUM_ALWAYS:
			return 1<=collectorCount;
		default:
			return 12==collectorCount; 
		}		
	}

	@Override
	protected AggregationType[] getAggregationTypes(Sensor[] sensors) {
		AggregationType[] aggregation = new AggregationType[sensors.length];
		for (int i = 0; i < aggregation.length; i++) {
			aggregation[i] = sensors[i].getAggregationYear();
		}
		return aggregation;
	}
}