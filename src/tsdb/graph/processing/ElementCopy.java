package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNull;

import tsdb.Station;
import tsdb.VirtualPlot;
import tsdb.graph.node.Continuous;
import tsdb.iterator.ElementCopyIterator;
import tsdb.iterator.ElementCopyIterator.Action;
import tsdb.util.iterator.TsIterator;

/**
 * Node: calculates sunshine duration from source (Rn_300)
 * @author woellauer
 *
 */
public class ElementCopy implements Continuous {

	private final Continuous source;
	private final Action[] actions;

	protected ElementCopy(Continuous source,Action[] actions) {
		throwNull(actions);
		this.source = source;
		this.actions = actions;
	}

	public static ElementCopy of(Continuous source, Action[] actions) {
		return new ElementCopy(source, actions);
	}

	@Override
	public TsIterator get(Long start, Long end) {
		TsIterator input_iterator = source.get(start, end);
		if(input_iterator==null||!input_iterator.hasNext()) {
			return null;
		}
		return new ElementCopyIterator(input_iterator, actions);
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

	@Override
	public TsIterator getExactly(long start, long end) {
		return source.getExactly(start, end);
	}
}
