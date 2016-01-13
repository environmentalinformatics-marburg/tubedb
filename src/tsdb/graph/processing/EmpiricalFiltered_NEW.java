package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNulls;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.graph.node.Continuous;
import tsdb.graph.source.GroupAverageSource_NEW;
import tsdb.iterator.EmpiricalIterator;
import tsdb.util.iterator.TsIterator;

/**
 * This node filters values based on the difference to a reference source.
 * @author woellauer
 *
 */
public class EmpiricalFiltered_NEW extends Continuous.Abstract {

	private static final Logger log = LogManager.getLogger();

	private final Continuous source; //not null
	private final Continuous compareSource; //not null	
	private final String stationName; //not null


	public EmpiricalFiltered_NEW(TsDB tsdb, Continuous source, Continuous compareSource, String stationName) {
		super(tsdb);
		throwNulls(source,compareSource,stationName);
		if(!source.isContinuous()) {
			throw new RuntimeException("QualityChecked needs continuous source");
		}
		if(!compareSource.isContinuous()) {
			throw new RuntimeException("QualityChecked needs continuous compare source");
		}
		this.source = source;
		this.compareSource = compareSource;
		this.stationName = stationName;
	}

	public static Continuous of(TsDB tsdb, Continuous continuous, String plotID) {		
		Continuous compareSource = GroupAverageSource_NEW.ofPlot(tsdb, plotID, continuous.getSchema());
		if(compareSource!=null) {
			return new EmpiricalFiltered_NEW(tsdb,continuous,compareSource, plotID);
		} else {
			log.warn("no compare average source");
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
			log.warn("no compare iterator");
			return input_iterator;
		}		
		Float[] maxDiff = tsdb.getEmpiricalDiff(source.getSchema());
		float[] refValues = tsdb.getReferenceValues(stationName,source.getSchema());
		EmpiricalIterator empirical_iterator = new EmpiricalIterator(input_iterator, compare_iterator, maxDiff, refValues);
		return empirical_iterator;
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
		return get(start,end);
	}

	@Override
	public boolean isContinuous() {
		return source.isContinuous();
	}

	@Override
	public boolean isConstantTimestep() {
		return source.isContinuous();
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
