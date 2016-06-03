package tsdb.util.iterator;

import java.util.Arrays;
import java.util.Iterator;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.processingchain.ProcessingChainNode;

/**
 * Base iterator for time series data.
 * <p>
 * It contains meta data with schema and processing chain for information purposes.
 * @author woellauer
 *
 */
public abstract class TsIterator implements Iterator<TsEntry>, ProcessingChainNode {

	/**
	 * Meta data as schema.
	 */
	protected final TsSchema schema;
	
	/**
	 * Create iterator with schema.
	 * @param schema
	 */
	public TsIterator(TsSchema schema) {
		this.schema = schema;
	}
	
	/**
	 * Get schema (meta data).
	 * @return
	 */
	public TsSchema getSchema() {
		return schema;
	}

	/**
	 * Get data schema names (column names).
	 * @return
	 */
	public String[] getNames() {
		return schema.names;
	}	
	
	/**
	 * Get Text of title, schema names and processing chain.
	 */
	@Override
	public String toString() {		
		return getProcessingTitle()+" "+schema.toString()+" "+getProcessingChain().getText();
	}	
	
	/**
	 * Write all remaining elements to CSV-file.
	 * @param filename
	 */
	public void writeCSV(String filename) {
		CSV.write(this,filename);
	}
	
	/**
	 * Write all remaining elements to console.
	 * @param filename
	 */
	public void writeConsole() {
		while(this.hasNext()) {
			TsEntry e = this.next();
			System.out.println(e);
		}		
	}
	
	/**
	 * Collect all remaining elements into TimeSeries Object.
	 * @param filename
	 */
	public TimeSeries toTimeSeries() {
		return TimeSeries.create(this);
	}
	
	/**
	 * Collect all remaining elements into TimestampSeries Object.
	 * @param filename
	 */
	public TimestampSeries toTimestampSeries(String name) {
		return TimestampSeries.create(this,name);
	}
	
	/**
	 * Get array of schemas from array of iterators.
	 * @param input_iterators
	 * @return
	 */
	public static TsSchema[] toSchemas(TsIterator[] input_iterators) {
		return Arrays.stream(input_iterators).map(it->it.getSchema()).toArray(TsSchema[]::new);
	}
	
	/**
	 * Check if iterator contains elements.
	 * @param it (nullable)
	 * @return
	 */
	public static boolean isLive(Iterator<?> it) {
		return it!=null && it.hasNext();
	}
	
	/**
	 * Check if iterator does not contain elements.
	 * @param it (nullable)
	 * @return
	 */
	public static boolean isNotLive(Iterator<?> it) {
		return it==null || (!it.hasNext());
	}
}
