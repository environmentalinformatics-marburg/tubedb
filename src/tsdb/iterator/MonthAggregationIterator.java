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
 * Aggregate day data to month data.
 * @author woellauer
 *
 */
public class MonthAggregationIterator extends AbstractAggregationIterator  {
	
	private final Mutator monthMutator;

	public MonthAggregationIterator(TsDB tsdb, TsIterator input_iterator, Mutator monthMutator) {
		super(tsdb, input_iterator, createSchemaFromConstantToVariableStep(input_iterator.getSchema(), 24*60, Aggregation.MONTH));
		this.monthMutator = monthMutator;
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
		switch(aggregationType) {
		case LAST:
			return 1<=collectorCount;
		case SUM_ALWAYS:
			return 1<=collectorCount;			
		default:
			return 27<=collectorCount;
		}		
	}
	
	@Override
	protected AggregationType[] getAggregationTypes(Sensor[] sensors) {
		AggregationType[] aggregation = new AggregationType[sensors.length];
		for (int i = 0; i < aggregation.length; i++) {
			aggregation[i] = sensors[i].getAggregationMonth();
		}
		return aggregation;
	}
	
	@Override
	protected TsEntry getNext() {
		TsEntry entry = super.getNext();
		if(monthMutator != null && entry != null) {
			monthMutator.apply(entry.timestamp, entry.data);
		}
		return entry;
	}
}