package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNull;

import org.tinylog.Logger;

import tsdb.TsDB;
import tsdb.graph.node.Continuous;
import tsdb.graph.source.DelegateContinuousAbstract;
import tsdb.iterator.DayAggregationIterator;
import tsdb.iterator.MonthAggregationIterator;
import tsdb.iterator.WeekAggregationIterator;
import tsdb.iterator.YearAggregationIterator;
import tsdb.util.AggregationInterval;
import tsdb.util.Mutator;
import tsdb.util.iterator.TsIterator;

/**
 * This node creates aggregated data from continuous input data.
 * Depending on the requested aggregation interval a hierarchy of aggregation iterators will be created. 
 * @author woellauer
 *
 */
public class Aggregated extends DelegateContinuousAbstract {	
	
	private final AggregationInterval aggregationInterval; //not null
	private final Mutator dayMutator; // nullable
	private final Mutator weekMutator; // nullable
	private final Mutator monthMutator; // nullable
	private final Mutator yearMutator; // nullable

	protected Aggregated(TsDB tsdb, Continuous source, AggregationInterval aggregationInterval, Mutator dayMutator, Mutator weekMutator, Mutator monthMutator, Mutator yearMutator) {
		super(tsdb, source);
		throwNull(aggregationInterval);
		this.aggregationInterval = aggregationInterval;
		this.dayMutator = dayMutator;
		this.weekMutator = weekMutator;
		this.monthMutator = monthMutator;
		this.yearMutator = yearMutator;
	}

	public static Aggregated of(TsDB tsdb, Continuous source, AggregationInterval aggregationInterval, Mutator dayMutator, Mutator weekMutator, Mutator monthMutator, Mutator yearMutator) {
		return new Aggregated(tsdb, source, aggregationInterval, dayMutator, weekMutator, monthMutator, yearMutator);
	}

	@Override
	public TsIterator get(Long start, Long end) {
		TsIterator continuous_iterator = source.get(start, end);
		if(continuous_iterator==null||!continuous_iterator.hasNext()) {
			return null;
		}
		if(aggregationInterval == AggregationInterval.HOUR) {
			return continuous_iterator;
		}
		
		DayAggregationIterator day_iterator = new DayAggregationIterator(tsdb,continuous_iterator, dayMutator);
		if(day_iterator == null || !day_iterator.hasNext()) {
			return null;
		}
		if(aggregationInterval == AggregationInterval.DAY) {
			return day_iterator;
		}
		if(aggregationInterval == AggregationInterval.WEEK) {
			WeekAggregationIterator week_iterator = new WeekAggregationIterator(tsdb, day_iterator, weekMutator);
			if(week_iterator == null || !week_iterator.hasNext()) {
				return null;
			}
			return week_iterator;
		}
		MonthAggregationIterator month_iterator = new MonthAggregationIterator(tsdb, day_iterator, monthMutator);
		if(month_iterator == null || !month_iterator.hasNext()) {
			return null;
		}
		if(aggregationInterval == AggregationInterval.MONTH) {			
			return month_iterator;
		}
		if(aggregationInterval == AggregationInterval.YEAR) {
			YearAggregationIterator year_iterator = new YearAggregationIterator(tsdb, month_iterator, yearMutator);
			if(year_iterator == null || !year_iterator.hasNext()) {
				return null;
			}
			return year_iterator;
		}
		new RuntimeException().printStackTrace();
		Logger.warn("unknown aggregation: "+aggregationInterval);
		return null;
	}	

	@Override
	public boolean isContinuous() {
		return true;
	}	

	@Override
	public boolean isConstantTimestep() {
		switch(aggregationInterval) {
		case HOUR:
		case DAY:
		case WEEK:
			return true;
		case MONTH:
		case YEAR:
			return false;
		default:
			throw new RuntimeException("unknown aggregation interval");	
		}
	}	
}
