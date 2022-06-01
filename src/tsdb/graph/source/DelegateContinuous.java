package tsdb.graph.source;

import static tsdb.util.AssumptionCheck.throwNull;

import tsdb.Station;
import tsdb.VirtualPlot;
import tsdb.graph.node.Continuous;
import tsdb.util.iterator.TsIterator;

public class DelegateContinuous implements Continuous {
	
	protected final Continuous source; //not null

	public DelegateContinuous(Continuous source) {
		throwNull(source);
		if(!source.isContinuous()) {
			throw new RuntimeException("need continuous source");
		}
		this.source = source;
	}

	@Override
	public TsIterator getExactly(long start, long end) {		
		return get(start,end);
	}

	public Station getSourceStation() {
		return source.getSourceStation();
	}

	@Override
	public VirtualPlot getSourceVirtualPlot() {
		return source.getSourceVirtualPlot();
	}

	@Override
	public long[] getTimeInterval() {
		return source.getTimeInterval();
	}

	@Override
	public int[] getSensorTimeInterval(String sensorName) {
		return source.getSensorTimeInterval(sensorName);
	}

	@Override
	public boolean isConstantTimestep() {
		return source.isConstantTimestep();
	}

	@Override
	public String[] getSchema() {
		return source.getSchema();
	}
}
