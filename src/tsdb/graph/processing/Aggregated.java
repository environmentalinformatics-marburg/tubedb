package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNulls;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.graph.node.Continuous;
import tsdb.iterator.DayAggregationIterator;
import tsdb.iterator.MonthAggregationIterator;
import tsdb.iterator.WeekAggregationIterator;
import tsdb.iterator.YearAggregationIterator;
import tsdb.util.AggregationInterval;
import tsdb.util.iterator.TsIterator;

/**
 * This node creates aggregated data from continuous input data.
 * Depending on the requested aggregation interval a hierarchy of aggregation iterators will be created. 
 * @author woellauer
 *
 */
public class Aggregated extends Continuous.Abstract {	
	private static final Logger log = LogManager.getLogger();

	private final Continuous source; //not null
	private final AggregationInterval aggregationInterval; //not null

	protected Aggregated(TsDB tsdb, Continuous source, AggregationInterval aggregationInterval) {
		super(tsdb);
		throwNulls(source,aggregationInterval);
		this.source = source;
		this.aggregationInterval = aggregationInterval;		
		if(!source.isContinuous()) {
			throw new RuntimeException("source needs to be continuous");
		}
	}

	public static Aggregated of(TsDB tsdb, Continuous source, AggregationInterval aggregationInterval) {
		return new Aggregated(tsdb, source, aggregationInterval);
	}

	@Override
	public TsIterator get(Long start, Long end) {
		TsIterator continuous_iterator = source.get(start, end);
		if(continuous_iterator==null||!continuous_iterator.hasNext()) {
			return null;
		}
		if(aggregationInterval==AggregationInterval.HOUR) {
			return continuous_iterator;
		}
		
		DayAggregationIterator day_iterator = new DayAggregationIterator(tsdb,continuous_iterator);
		if(day_iterator==null||!day_iterator.hasNext()) {
			return null;
		}
		if(aggregationInterval==AggregationInterval.DAY) {
			return day_iterator;
		}
		if(aggregationInterval==AggregationInterval.WEEK) {
			WeekAggregationIterator week_iterator = new WeekAggregationIterator(tsdb, day_iterator);
			if(week_iterator==null||!week_iterator.hasNext()) {
				return null;
			}
			return week_iterator;
		}
		MonthAggregationIterator month_iterator = new MonthAggregationIterator(tsdb, day_iterator);
		if(month_iterator==null||!month_iterator.hasNext()) {
			return null;
		}
		if(aggregationInterval==AggregationInterval.MONTH) {			
			return month_iterator;
		}
		if(aggregationInterval==AggregationInterval.YEAR) {
			YearAggregationIterator year_iterator = new YearAggregationIterator(tsdb, month_iterator);
			if(year_iterator==null||!year_iterator.hasNext()) {
				return null;
			}
			return year_iterator;
		}
		new RuntimeException().printStackTrace();
		log.warn("unknown aggregation: "+aggregationInterval);
		return null;
	}

	@Override
	public Station getSourceStation() {
		return source.getSourceStation();
	}

	@Override
	public String[] getSchema() {
		return source.getSchema();
	}

	@Override
	public TsIterator getExactly(long start, long end) {
		return get(start, end);
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
	
	@Override
	public VirtualPlot getSourceVirtualPlot() {
		return source.getSourceVirtualPlot();
	}
	
	@Override
	public long[] getTimestampInterval() {
		return source.getTimestampInterval();
	}
}
