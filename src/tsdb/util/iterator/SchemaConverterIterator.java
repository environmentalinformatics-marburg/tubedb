package tsdb.util.iterator;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.Util;

public class SchemaConverterIterator extends InputProcessingIterator {

	private int[] inputPos;

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