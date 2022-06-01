package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNulls;

import org.tinylog.Logger;

import tsdb.TsDB;
import tsdb.graph.node.Continuous;
import tsdb.graph.source.DelegateContinuousAbstract;
import tsdb.iterator.DifferenceIterator;
import tsdb.util.Util;
import tsdb.util.iterator.TsIterator;

/**
 * This Node creates difference values between two sources.
 * @author woellauer
 *
 */
public class Difference extends DelegateContinuousAbstract {
	
	private final Continuous compareSource;
	private final String stationName;
	private final boolean absoluteDifference;

	protected Difference(TsDB tsdb, Continuous source, Continuous compareSource, String stationName, boolean absoluteDifference) {
		super(tsdb, source);
		throwNulls(compareSource, stationName);
		if(!compareSource.isContinuous()) {
			throw new RuntimeException("QualityChecked needs continuous compare source");
		}
		if(source.isConstantTimestep() != compareSource.isConstantTimestep()) {
			throw new RuntimeException("source and compare source are not compatible");
		}
		if(!Util.isContained(source.getSchema(), compareSource.getSchema())) {
			throw new RuntimeException("source and compare source are not compatible");
		}
		this.compareSource = compareSource;
		this.stationName = stationName;
		this.absoluteDifference = absoluteDifference;
	}
	
	public static Continuous of(TsDB tsdb, Continuous source, Continuous compareSource, String stationName, boolean absoluteDifference) {
		return new Difference(tsdb, source, compareSource, stationName, absoluteDifference);		
	}
	

	@Override
	public TsIterator getExactly(long start, long end) {
		TsIterator input_iterator = source.get(start, end);
		if(input_iterator==null||!input_iterator.hasNext()) {
			return null;
		}
		TsIterator compare_iterator = compareSource.get(start, end);
		//TimeSeriesIterator compare_iterator = new ProjectionIterator(compareSource.get(start, end),source.getSchema());
		if(compare_iterator==null||!compare_iterator.hasNext()) {
			Logger.warn("no compare iterator");
			return null;
		}
		float[] refValues = tsdb.getReferenceValues(stationName,source.getSchema());
		DifferenceIterator difference_iterator = new DifferenceIterator(input_iterator, compare_iterator, absoluteDifference, refValues);
		return difference_iterator;
	}

	@Override
	public TsIterator get(Long start, Long end) {
		if(start==null||end==null) {
			long[] interval = tsdb.getBaseTimeInterval(stationName);
			if(interval==null) {
				return null;
			}
			if(start==null) {
				start = interval[0];
			}
			if(end==null) {
				end = interval[1];
			}
		}
		return getExactly(start, end);
	}
}
