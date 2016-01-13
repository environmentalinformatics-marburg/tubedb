package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNull;
import tsdb.Station;
import tsdb.VirtualPlot;
import tsdb.graph.node.Continuous;
import tsdb.iterator.DifferentialIterator;
import tsdb.util.iterator.TsIterator;

/**
 * This node creates differential values from source.
 * @author woellauer
 *
 */
public class Differential implements Continuous {
	
	private Continuous source;

	protected Differential(Continuous source) {
		throwNull(source);
		this.source = source;
	}
	
	public static Differential of(Continuous source) {
		return new Differential(source);
	}

	@Override
	public TsIterator getExactly(long start, long end) {
		return get(start, end);
	}

	@Override
	public TsIterator get(Long start, Long end) {		
		TsIterator input_iterator = source.get(start, end);
		if(input_iterator==null||!input_iterator.hasNext()) {
			return null;
		}		
		DifferentialIterator it = new DifferentialIterator(input_iterator);
		if(it==null||!it.hasNext()) {
			return null;
		}
		return it;
	}

	@Override
	public Station getSourceStation() {
		return source.getSourceStation();
	}

	@Override
	public boolean isContinuous() {
		return source.isContinuous();
	}

	@Override
	public boolean isConstantTimestep() {
		return source.isConstantTimestep();
	}

	@Override
	public String[] getSchema() {
		return source.getSchema();
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
