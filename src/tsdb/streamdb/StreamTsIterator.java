package tsdb.streamdb;

import java.util.Iterator;


import org.tinylog.Logger;

import tsdb.util.DataEntry;
import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.iterator.TsIterator;

/**
 * converts StreamIterator (or other DataEntry Iterator) to TsIterator
 * @author woellauer
 *
 */
public class StreamTsIterator extends TsIterator {
	@SuppressWarnings("unused")
	
	
	private final Iterator<DataEntry> input_iterator;
	
	public StreamTsIterator(StreamIterator input_iterator) {
		this(input_iterator,input_iterator.sensorName);
		//Logger.info("transform "+input_iterator.sensorName);
	}
	
	public static StreamTsIterator of(StreamIterator input_iterator) {
		if(input_iterator==null) {
			return null;
		}
		return new StreamTsIterator(input_iterator);
	}
	
	public StreamTsIterator(Iterator<DataEntry> input_iterator, String sensorName) {
		super(new TsSchema(new String[]{sensorName}));
		this.input_iterator = input_iterator;
	}

	@Override
	public boolean hasNext() {
		return input_iterator.hasNext();
	}

	@Override
	public TsEntry next() {
		return TsEntry.of(input_iterator.next());
	}
}
