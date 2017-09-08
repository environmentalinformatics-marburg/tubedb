package tsdb.util.iterator;

import tsdb.util.Mutator;
import tsdb.util.TsEntry;

/**
 * Apply mutator to elements of iterator
 * @author woellauer
 *
 */
public class MutatorIterator extends InputIterator {
	
	private final Mutator mutator;
	
	/**
	 * Apply mutator to elements of iterator
	 * @param it
	 * @param mutator nullable
	 * @return resulting iterator
	 */
	public static TsIterator appendMutator(TsIterator it, Mutator mutator) {		
		if(mutator == null) {
			return it;
		}
		return new MutatorIterator(it, mutator);	
	}

	public MutatorIterator(TsIterator it, Mutator mutator) {
		super(it, it.getSchema());
		this.mutator = mutator;
	}

	@Override
	public TsEntry next() {
		TsEntry e = input_iterator.next();
		mutator.apply(e.timestamp, e.data);
		return e;
	}
}
