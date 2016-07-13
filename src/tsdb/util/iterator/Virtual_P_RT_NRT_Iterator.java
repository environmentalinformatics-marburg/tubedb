package tsdb.util.iterator;

import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.DataQuality;
import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.TsSchema.Aggregation;

/**
 * Specific iterator that calculates current precipitation from rain container sensor (P_container_RT to P_RT_NRT).
 * <p>
 * Rain container value errors are recognized by checking delta values.
 * @author woellauer
 *
 */
public class Virtual_P_RT_NRT_Iterator extends InputIterator {
	
	private static float MAX_DELTA = 15;
	
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger();

	private final int pos_P_container_RT;
	private final int pos_P_RT_NRT;

	float prevV = Float.NaN;
	float prevDelta = 0f;
	
	public static TsSchema createSchema(TsSchema tsSchema) {
		String[] names = tsSchema.names;
		Aggregation aggregation = tsSchema.aggregation;
		int timeStep = tsSchema.timeStep;
		boolean isContinuous = tsSchema.isContinuous;
		tsSchema.throwNoQualityFlags();
		boolean hasQualityFlags = true;
		boolean hasInterpolatedFlags = false;
		boolean hasQualityCounters = false;
		return new TsSchema(names, aggregation, timeStep, isContinuous, hasQualityFlags, hasInterpolatedFlags, hasQualityCounters);		
	}	

	public Virtual_P_RT_NRT_Iterator(TsIterator input_iterator, int pos_P_container_RT, int pos_P_RT_NRT) {
		super(input_iterator, createSchema(input_iterator.schema));
		//log.info("input_iterator "+input_iterator.schema);
		//log.info("input_iterator "+this.schema);
		this.pos_P_container_RT = pos_P_container_RT;
		this.pos_P_RT_NRT = pos_P_RT_NRT;
	}

	@Override
	public TsEntry next() {
		TsEntry entry = input_iterator.next();
		float[] result = Arrays.copyOf(entry.data, entry. data.length);
		DataQuality[] resultFlags = Arrays.copyOf(entry.qualityFlag, entry.qualityFlag.length); 
		float v = result[pos_P_container_RT];		

		if(Float.isNaN(prevV)) {
			result[pos_P_RT_NRT] = Float.NaN;
			resultFlags[pos_P_RT_NRT] = DataQuality.NO;
			prevDelta = 0f;
		} else {
			if(Float.isNaN(v)) {
				result[pos_P_RT_NRT] = Float.NaN;
				resultFlags[pos_P_RT_NRT] = DataQuality.NO;
				prevDelta = 0f;
			} else {
				float delta = v-prevV;
				if(prevDelta>-0.5f && delta>=0f && delta<=MAX_DELTA) {
					result[pos_P_RT_NRT] = delta;
					resultFlags[pos_P_RT_NRT] = resultFlags[pos_P_container_RT];
				} else {
					result[pos_P_RT_NRT] = 0f;
					resultFlags[pos_P_RT_NRT] = resultFlags[pos_P_container_RT];
				}
				prevDelta = delta;
			}
		}
		prevV = v;
		return new TsEntry(entry.timestamp, result, resultFlags);
	}
}
