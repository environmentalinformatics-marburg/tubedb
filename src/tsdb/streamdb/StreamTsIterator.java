package tsdb.streamdb;

import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	private static final Logger log = LogManager.getLogger();
	
	private final Iterator<DataEntry> input_iterator;
	
	public StreamTsIterator(StreamIterator input_iterator) {
		this(input_iterator,input_iterator.sensorName);
		//log.info("transform "+input_iterator.sensorName);
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
		DataEntry e = input_iterator.next();
		return new TsEntry(e.timestamp, new float[]{e.value});
	}

}
