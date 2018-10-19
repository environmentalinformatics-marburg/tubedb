package tsdb.util.iterator;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;

/**
 * Time series iterator that stores the current element.
 * <p>
 * This iterator my simplify processing iterators.
 * <br>
 * Extending classes need to implement getNext() only.
 * @author woellauer
 *
 */
public abstract class MoveIterator extends TsIterator implements TsEnumerator {

	private TsEntry current = null;
	private boolean closed = false;

	public MoveIterator(TsSchema schema) {
		super(schema);
	}

	/**
	 * redirects processing to getNext()
	 */
	@Override
	public final boolean hasNext() {
		if(closed) {
			return false;
		} else {
			if(current == null) {
				current = getNext();
				if(current == null) {
					closed = true;
					return false;
				} 
			}				
		}
		return true;
	}

	/**
	 * redirects processing to getNext()
	 */
	@Override
	public final TsEntry next() {
		hasNext();
		TsEntry result = current;
		current = null;
		hasNext();
		return result;
	}

	@Override
	public final boolean moveNext() {
		if(closed) {
			return false;
		}
		current = getNext();
		if(current == null) {
			closed = true;
			return false;
		}
		return true;

	}

	@Override
	public final TsEntry current() {
		return current;
	}

	/**
	 * Request next element. 
	 * <p>
	 * It's guaranteed to not be called again when it first returns null (no next element).
	 * <br>
	 * This method should be called by MoveIterator internal only.
	 * @return next element or null if there is no next element
	 */
	protected abstract TsEntry getNext();

	/**
	 * Signals that iterator does not contain a next element. 
	 * <br>
	 * So getNext() will not be called again.
	 */
	public void close() {
		current = null;		
		closed = true;
	}
}
