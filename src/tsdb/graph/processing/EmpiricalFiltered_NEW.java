package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNulls;

import org.tinylog.Logger;

import tsdb.TsDB;
import tsdb.graph.node.Continuous;
import tsdb.graph.source.DelegateContinuousAbstract;
import tsdb.graph.source.GroupAverageSource_NEW;
import tsdb.iterator.EmpiricalIterator;
import tsdb.util.iterator.TsIterator;

/**
 * This node filters values based on the difference to a reference source.
 * @author woellauer
 *
 */
public class EmpiricalFiltered_NEW extends DelegateContinuousAbstract {	

	private final Continuous compareSource; //not null	
	private final String stationName; //not null

	public EmpiricalFiltered_NEW(TsDB tsdb, Continuous source, Continuous compareSource, String stationName) {
		super(tsdb, source);
		throwNulls(compareSource, stationName);
		if(!compareSource.isContinuous()) {
			throw new RuntimeException("QualityChecked needs continuous compare source");
		}
		this.compareSource = compareSource;
		this.stationName = stationName;
	}

	public static Continuous of(TsDB tsdb, Continuous continuous, String plotID) {		
		Continuous compareSource = GroupAverageSource_NEW.ofPlot(tsdb, plotID, continuous.getSchema());
		if(compareSource!=null) {
			return new EmpiricalFiltered_NEW(tsdb,continuous,compareSource, plotID);
		} else {
			Logger.warn("no compare average source");
			return continuous;
		}
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
		TsIterator input_iterator = source.get(start, end);
		if(input_iterator==null||!input_iterator.hasNext()) {
			return null;
		}
		TsIterator compare_iterator = compareSource.get(start, end);
		if(compare_iterator==null||!compare_iterator.hasNext()) {
			Logger.info("no reference compare iterator");
			return input_iterator;
		}		
		Float[] maxDiff = tsdb.getEmpiricalDiff(source.getSchema());
		//Logger.info("maxDiff "+Arrays.toString(maxDiff));
		float[] refValues = tsdb.getReferenceValues(stationName,source.getSchema());
		//Logger.info("refValues "+Arrays.toString(refValues));
		EmpiricalIterator empirical_iterator = new EmpiricalIterator(input_iterator, compare_iterator, maxDiff, refValues);
		return empirical_iterator;
	}	
}
