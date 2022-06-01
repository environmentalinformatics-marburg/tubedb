package tsdb.graph.processing;

import tsdb.graph.node.Continuous;
import tsdb.graph.source.DelegateContinuous;
import tsdb.util.Mutator;
import tsdb.util.iterator.MutatorIterator;
import tsdb.util.iterator.TsIterator;

public class PostHourMutation extends DelegateContinuous {
	private final Mutator postHourMutator; // nullable
	
	/**
	 * 
	 * @param source
	 * @param postHourMutator nullable
	 */
	public PostHourMutation(Continuous source, Mutator postHourMutator) {
		super(source);
		this.postHourMutator = postHourMutator;
	}		

	@Override
	public TsIterator get(Long start, Long end) {
		TsIterator it = source.get(start, end);
		return MutatorIterator.appendMutator(it, postHourMutator);
	}	
}
