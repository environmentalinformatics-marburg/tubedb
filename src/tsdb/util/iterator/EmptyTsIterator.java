package tsdb.util.iterator;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;

public class EmptyTsIterator extends TsIterator {

	public EmptyTsIterator(TsSchema schema) {
		super(schema);

	}
	
	@Override
	public boolean hasNext() {
		return false;
	}
	
	@Override
	public TsEntry next() {
		return null;
	}		
}