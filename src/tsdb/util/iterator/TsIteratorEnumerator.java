package tsdb.util.iterator;

import tsdb.util.TsEntry;

public class TsIteratorEnumerator implements TsEnumerator {
	private final TsIterator iterator;
	private TsEntry current;
	
	public static TsEnumerator of(TsIterator iterator) {
		if(iterator instanceof TsEnumerator) {
			return (TsEnumerator) iterator;
		}
		return new TsIteratorEnumerator(iterator);
	}
	
	private TsIteratorEnumerator(TsIterator iterator) {
		this.iterator = iterator;
		this.current = null;
	}
	
	@Override
	public boolean moveNext() {
		if(iterator.hasNext()) {
			current = iterator.next();
			return true;
		} else {
			current = null;
			return false;
		}
	}
	
	@Override
	public TsEntry current() {
		return current;
	}
}