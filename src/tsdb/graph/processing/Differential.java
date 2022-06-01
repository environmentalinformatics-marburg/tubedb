package tsdb.graph.processing;

import tsdb.graph.node.Continuous;
import tsdb.graph.source.DelegateContinuous;
import tsdb.iterator.DifferentialIterator;
import tsdb.util.iterator.TsIterator;

/**
 * This node creates differential values from source.
 * @author woellauer
 *
 */
public class Differential extends DelegateContinuous {
	
	protected Differential(Continuous source) {
		super(source);
	}
	
	public static Differential of(Continuous source) {
		return new Differential(source);
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
}
