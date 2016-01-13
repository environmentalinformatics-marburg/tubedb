package tsdb.util.iterator;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;

public class Virtual_P_RT_NRT_Iterator extends InputIterator {
	
	private static float MAX_DELTA = 15;
	
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger();

	private final int pos_P_container_RT;
	private final int pos_P_RT_NRT;

	float prevV = Float.NaN;
	float prevDelta = 0f;

	public Virtual_P_RT_NRT_Iterator(TsIterator input_iterator, int pos_P_container_RT, int pos_P_RT_NRT) {
		super(input_iterator, new TsSchema(input_iterator.getNames()));
		this.pos_P_container_RT = pos_P_container_RT;
		this.pos_P_RT_NRT = pos_P_RT_NRT;
	}

	@Override
	public boolean hasNext() {
		return input_iterator.hasNext();
	}

	@Override
	public TsEntry next() {
		TsEntry entry = input_iterator.next();
		float[] result = Arrays.copyOf(entry.data,entry.data.length);
		float v = result[pos_P_container_RT];		

		if(Float.isNaN(prevV)) {
			result[pos_P_RT_NRT] = Float.NaN;
			prevDelta = 0f;
		} else {
			if(Float.isNaN(v)) {
				result[pos_P_RT_NRT] = Float.NaN;
				prevDelta = 0f;
			} else {
				float delta = v-prevV;
				if(prevDelta>-0.5f && delta>=0f && delta<=MAX_DELTA) {
					result[pos_P_RT_NRT] = delta;
				} else {
					result[pos_P_RT_NRT] = 0f;
				}
				prevDelta = delta;
			}
		}
		prevV = v;
		return new TsEntry(entry.timestamp, result);
	}
}
