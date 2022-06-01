package tsdb.graph.source;

import static tsdb.util.AssumptionCheck.throwNull;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.graph.node.Node;
import tsdb.util.iterator.TsIterator;

public class DelegateNode extends Node.Abstract {
	
	protected final Node source;

	public DelegateNode(TsDB tsdb, Node source) {
		super(tsdb);
		throwNull(source);
		this.source = source;
	}

	@Override
	public TsIterator get(Long start, Long end) {
		return source.get(start, end);
	}

	@Override
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
}