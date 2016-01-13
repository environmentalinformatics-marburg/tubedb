package tsdb.iterator;

import java.time.LocalDateTime;
import java.time.Month;

import tsdb.util.AggregationType;
import tsdb.util.TimeUtil;

/**
 * collecting aggregator to year
 * @author woellauer
 *
 */
public class YearCollectingAggregator extends AbstractCollectingAggregator {

	public YearCollectingAggregator(MonthCollectingAggregator mca) {
		super(mca, 12);
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

	@Override
	public String getProcessingTitle() {
		return "YearCollectingAggregator";
	}
}
