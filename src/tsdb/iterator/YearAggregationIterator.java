package tsdb.iterator;

import java.time.LocalDateTime;
import java.time.Month;

import tsdb.TsDB;
import tsdb.component.Sensor;
import tsdb.util.AggregationType;
import tsdb.util.Mutator;
import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;
import tsdb.util.TsSchema.Aggregation;
import tsdb.util.iterator.TsIterator;

/**
 * Aggregate month data to year data.
 * @author woellauer
 *
 */
public class YearAggregationIterator extends AbstractAggregationIterator  {
	
	private final Mutator yearMutator;

	public YearAggregationIterator(TsDB tsdb, TsIterator input_iterator, Mutator yearMutator) {
		super(tsdb, input_iterator, createSchemaVariableStep(input_iterator.getSchema(), Aggregation.MONTH, Aggregation.YEAR));
		this.yearMutator = yearMutator;
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
	
	@Override
	protected TsEntry getNext() {
		TsEntry entry = super.getNext();
		if(yearMutator != null && entry != null) {
			yearMutator.apply(entry.timestamp, entry.data);
		}
		return entry;
	}
}