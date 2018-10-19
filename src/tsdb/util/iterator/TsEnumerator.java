package tsdb.util.iterator;

import tsdb.util.TsEntry;

public interface TsEnumerator {
	public boolean moveNext();
	public TsEntry current();
}