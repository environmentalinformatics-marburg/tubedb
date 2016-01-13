package tsdb.iterator;

import java.time.LocalDateTime;
import java.time.Month;

import tsdb.TsDB;
import tsdb.util.AggregationType;
import tsdb.util.TimeUtil;
import tsdb.util.TsSchema.Aggregation;
import tsdb.util.iterator.TsIterator;

/**
 * aggregation iterator to year
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
		return 12==collectorCount; 
	}
}