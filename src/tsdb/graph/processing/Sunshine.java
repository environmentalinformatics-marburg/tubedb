package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNull;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.graph.node.Node;
import tsdb.iterator.SunshineIterator;
import tsdb.util.iterator.TsIterator;

/**
 * Node: calculates sunshine duration from source (Rn_300)
 * @author woellauer
 *
 */
public class Sunshine extends Node.Abstract{
	
	private final Node source;
	
	protected Sunshine(TsDB tsdb,Node source) {
		super(tsdb);
		throwNull(source);
		this.source = source;
	}
	
	public static Sunshine of(TsDB tsdb,Node source) {
		return new Sunshine(tsdb, source);
	}

	@Override
	public TsIterator get(Long start, Long end) {
		TsIterator input_iterator = source.get(start, end);
		if(input_iterator==null||!input_iterator.hasNext()) {
			return null;
		}			
		return new SunshineIterator(input_iterator);
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
