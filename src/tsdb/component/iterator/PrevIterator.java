package tsdb.component.iterator;


import org.tinylog.Logger;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.iterator.TsIterator;

public class PrevIterator extends PrevLookaheadIterator {
	

	private final int len;
	private float[] prevValue;
	private long[] prevTimestamp;

	public PrevIterator(TsIterator input_iterator, int lookaheadSize, TsSchema output_schema) {
		super(input_iterator, lookaheadSize, output_schema);
		this.len = schema.length;
		prevValue = new float[len];
		prevTimestamp = new long[len];
		for(int i = 0; i < len; i++) {
			prevValue[i] = Float.NaN;
		}
	}

	@Override
	protected TsEntry processElement(TsEntry current) {
		float[] data = current.data;
		long timestamp = current.timestamp;
		for(int i = 0; i < len; i++) {
			float v = current.data[i];
			if(Float.isFinite(v)) {
				prevValue[i] = v;
				prevTimestamp[i] = timestamp;
			}
			
		}
		return current;
	}

}
