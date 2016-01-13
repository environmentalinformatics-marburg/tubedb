package tsdb.iterator;

import tsdb.util.TsEntry;
import tsdb.util.iterator.InputIterator;
import tsdb.util.iterator.TsIterator;

/**
 * apply a function to every entry in input_iterator
 * @author woellauer
 *
 */
public class ApplyIterator extends InputIterator {
	
	@FunctionalInterface
	public interface ApplyFunc {
		TsEntry apply(TsEntry entry);
	}

	private final ApplyFunc applyFunc;

	public ApplyIterator(TsIterator input_iterator, ApplyFunc applyFunc) {
		super(input_iterator, input_iterator.getSchema().copy());
		this.applyFunc = applyFunc;
	}

	@Override
	public boolean hasNext() {
		return input_iterator.hasNext();
	}

	@Override
	public TsEntry next() {
		return applyFunc.apply(input_iterator.next());
	}
}
