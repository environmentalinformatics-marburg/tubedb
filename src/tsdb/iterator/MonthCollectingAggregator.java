package tsdb.iterator;

import java.time.LocalDateTime;
import java.time.Month;

import tsdb.util.AggregationType;
import tsdb.util.TimeUtil;

/**
 * collecting aggregator to month
 * @author woellauer
 *
 */
public class MonthCollectingAggregator extends AbstractCollectingAggregator {

	public MonthCollectingAggregator(DayCollectingAggregator dca) {
		super(dca,31);
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
	public String getProcessingTitle() {
		return "MonthCollectingAggregator";
	}
}
