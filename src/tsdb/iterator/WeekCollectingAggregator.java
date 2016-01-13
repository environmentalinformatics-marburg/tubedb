package tsdb.iterator;

import tsdb.util.AggregationType;

/**
 * collecting aggregator to week
 * @author woellauer
 *
 */
public class WeekCollectingAggregator extends AbstractCollectingAggregator {

	public WeekCollectingAggregator(DayCollectingAggregator dca) {
		super(dca, 7);
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
	public String getProcessingTitle() {
		return "WeekCollectingAggregator";
	}

}
