package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNulls;


import org.tinylog.Logger;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.component.iterator.NocCheckIterator;
import tsdb.graph.node.Continuous;
import tsdb.util.iterator.TsIterator;

/**
 * This node filters values based on the difference to a reference source.
 * @author woellauer
 *
 */
public class NocCheck extends Continuous.Abstract {

	

	private final Continuous source; //not null

	private NocCheck(TsDB tsdb, Continuous source) {
		super(tsdb);
		throwNulls(source);
		if(!source.isContinuous()) {
			throw new RuntimeException("NocCheck needs continuous source");
		}
		this.source = source;
	}

	public static Continuous of(TsDB tsdb, Continuous continuous) {
			return new NocCheck(tsdb,continuous);
	}

	@Override
	public TsIterator get(Long start, Long end) {		
		TsIterator input_iterator = source.get(start, end);
		if(input_iterator==null||!input_iterator.hasNext()) {
			return null;
		}
		NocCheckIterator noc_iterator = new NocCheckIterator(input_iterator);
		return noc_iterator;
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
