package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNull;

import tsdb.graph.node.Continuous;
import tsdb.graph.source.DelegateContinuous;
import tsdb.iterator.ElementCopyIterator;
import tsdb.iterator.ElementCopyIterator.Action;
import tsdb.util.iterator.TsIterator;

/**
 * Node: calculates sunshine duration from source (Rn_300)
 * @author woellauer
 *
 */
public class ElementCopy extends DelegateContinuous {
	
	private final Action[] actions;
	
	protected ElementCopy(Continuous source, Action[] actions) {
		super(source);
		throwNull(actions);
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
}