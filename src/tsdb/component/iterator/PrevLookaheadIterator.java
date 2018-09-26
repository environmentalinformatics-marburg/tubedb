package tsdb.component.iterator;

import java.util.ArrayDeque;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.iterator.InputIterator;
import tsdb.util.iterator.TsIterator;

public abstract class PrevLookaheadIterator extends InputIterator {
	
	protected final int len;
	
	protected float[] prevValue;
	protected long[] prevTimestamp;
	
	protected ArrayDeque<TsEntry> future;
	
	public PrevLookaheadIterator(TsIterator input_iterator, int lookaheadSize, TsSchema output_schema) {
		super(input_iterator, output_schema);
		this.len = schema.length;
		prevValue = new float[len];
		prevTimestamp = new long[len];
		for(int i = 0; i < len; i++) {
			prevValue[i] = Float.NaN;
		}
		future = new ArrayDeque<TsEntry>(lookaheadSize);
		while(input_iterator.hasNext() && future.size() < lookaheadSize) {
			future.addLast(input_iterator.next());
		}
	}

	@Override
	public boolean hasNext() {
		return !future.isEmpty();
	}

	@Override
	public final TsEntry next() {
		TsEntry current = future.pollFirst();
		if(input_iterator.hasNext()) {
			future.addLast(input_iterator.next());
		}
		TsEntry res = processElement(current);
		float[] data = current.data;
		long timestamp = current.timestamp;
		for(int i = 0; i < len; i++) {
			float v = data[i];
			if(Float.isFinite(v)) {
				prevValue[i] = v;
				prevTimestamp[i] = timestamp;
			}
			
		}
		return res;
	}

	protected abstract TsEntry processElement(TsEntry current);
}
