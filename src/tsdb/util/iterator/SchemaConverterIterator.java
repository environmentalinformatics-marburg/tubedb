package tsdb.util.iterator;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.Util;

/**
 * Converts schema of input iterator to output schema.
 * <p>
 * column names that are missing in input iterator are filled with NaN-values.
 * @author woellauer
 *
 */
public class SchemaConverterIterator extends InputProcessingIterator {

	private int[] inputPos;

	/**
	 * Creates converter iterator.
	 * @param input_iterator
	 * @param outputSchema
	 * @param fillWithNaN If false and column name is missing in input then log a warning message.
	 */
	public SchemaConverterIterator(TsIterator input_iterator, String[] outputSchema, boolean fillWithNaN) {
		super(input_iterator, new TsSchema(outputSchema));		
		this.inputPos = Util.stringArrayToPositionIndexArray(outputSchema, input_iterator.getNames(), !fillWithNaN, false);
	}

	@Override
	protected TsEntry getNext() {
		if(input_iterator.hasNext()) {
			TsEntry e = input_iterator.next();
			float[] data = new float[inputPos.length];
			for(int i=0;i<data.length;i++) {
				int pos = inputPos[i];
				if(pos==-1) {
					data[i] = Float.NaN;
				} else {
					data[i] = e.data[pos];
				}					
			}
			return new TsEntry(e.timestamp,data);
		} else {
			return null;
		}
	}
}