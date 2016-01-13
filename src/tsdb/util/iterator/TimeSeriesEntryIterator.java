package tsdb.util.iterator;

import java.util.Iterator;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;

public class TimeSeriesEntryIterator extends TsIterator {
	
	private Iterator<TsEntry> input_iterator;

	public TimeSeriesEntryIterator(Iterator<TsEntry> input_iterator, String[] schema) {
		super(new TsSchema(schema));
		this.input_iterator = input_iterator;
	}

	@Override
	public boolean hasNext() {
		return input_iterator.hasNext();
	}

	@Override
	public TsEntry next() {
		return input_iterator.next();
	}
}
