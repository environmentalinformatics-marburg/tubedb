package tsdb.util.iterator;

import java.util.Iterator;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;

/**
 * Converts raw Iterator<TsEntry> to TsIterator.
 * @author woellauer
 *
 */
public class TimeSeriesEntryIterator extends TsIterator {
	
	private Iterator<TsEntry> input_iterator;

	/**
	 * Creates convert iterator.
	 * @param input_iterator
	 * @param schema column names
	 */
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
