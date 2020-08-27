package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNulls;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.graph.node.Node;
import tsdb.util.Mutator;
import tsdb.util.iterator.MutatorIterator;
import tsdb.util.iterator.TsIterator;

/**
 * This node applies mutator functions to source node.
 * @author woellauer
 *
 */
public class MutatorNode extends Node.Abstract {
	
	private final Node source;
	private final Mutator mutator;

	protected MutatorNode(TsDB tsdb, Node source, Mutator mutator) {
		super(tsdb);
		throwNulls(source, mutator);
		this.source = source;
		this.mutator = mutator;
	}
	
	public static MutatorNode of(TsDB tsdb, Node source, Mutator mutator) {
		if(mutator == null) {
			throw new RuntimeException();
		}
		return new MutatorNode(tsdb, source, mutator);
	}

	@Override
	public TsIterator get(Long start, Long end) {
		TsIterator input_iterator = source.get(start, end);
		if(input_iterator==null||!input_iterator.hasNext()) {
			return null;
		}
		MutatorIterator mi = new MutatorIterator(input_iterator, mutator);
		if(mi == null || !mi.hasNext()) {
			return null;
		}		
		return mi;
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
	public boolean isContinuous() {
		return source.isContinuous();
	}

	@Override
	public boolean isConstantTimestep() {
		return source.isConstantTimestep();
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
