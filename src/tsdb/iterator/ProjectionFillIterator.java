package tsdb.iterator;

import tsdb.util.DataQuality;
import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.Util;
import tsdb.util.TsSchema.Aggregation;
import tsdb.util.iterator.InputIterator;
import tsdb.util.iterator.TsIterator;

/**
 * Transforms from input schema to ouput schema
 * output schema does not need to be subset of input schema (with arbitrary order)
 * If some parameter in output schema is not in input schema -> column will be filled with Na-values.
 * @author woellauer
 *
 */
public class ProjectionFillIterator extends InputIterator {

	private int[] parameterPos;

	public static TsSchema createSchema(TsSchema tsSchema, String[] outputSchema) {
		String[] names = outputSchema;
		Aggregation aggregation = tsSchema.aggregation;
		int timeStep = tsSchema.timeStep;
		boolean isContinuous = tsSchema.isContinuous;
		boolean hasQualityFlags = tsSchema.hasQualityFlags;
		boolean hasInterpolatedFlags = tsSchema.hasInterpolatedFlags;
		boolean hasQualityCounters = tsSchema.hasQualityCounters;
		return new TsSchema(names, aggregation, timeStep, isContinuous, hasQualityFlags, hasInterpolatedFlags, hasQualityCounters); 
	}

	public ProjectionFillIterator(TsIterator input_iterator, String[] outputSchema) {
		super(input_iterator, createSchema(input_iterator.getSchema(), outputSchema));
		this.parameterPos = Util.stringArrayToPositionIndexArray(outputSchema, input_iterator.getNames(), false, false);
	}

	@Override
	public boolean hasNext() {
		return input_iterator.hasNext();
	}

	@Override
	public TsEntry next() {
		TsEntry element = input_iterator.next();
		float[] data = new float[schema.length];
		for(int i=0;i<schema.length;i++) {
			if(parameterPos[i]>=0) {
				data[i] = element.data[parameterPos[i]];
			} else {
				data[i] = Float.NaN;	
			}
		}
		DataQuality[] qualityFlag = null;
		if(element.qualityFlag!=null) {
			qualityFlag = new DataQuality[schema.length];
			for(int i=0;i<schema.length;i++) {
				if(parameterPos[i]>=0) {
					qualityFlag[i] = element.qualityFlag[parameterPos[i]];
				} else {
					qualityFlag[i] = DataQuality.Na;
				}
			}
		}
		boolean[] interpolated = null;
		if(element.interpolated!=null) {
			interpolated = new boolean[schema.length];
			for(int i=0;i<schema.length;i++) {
				if(parameterPos[i]>=0) {
					interpolated[i] = element.interpolated[parameterPos[i]];
				} else {
					interpolated[i] = false;
				}
			}
		}
		int[][] qualityCounter = null;
		if(element.qualityCounter!=null) {
			qualityCounter = new int[schema.length][];
			for(int i=0;i<schema.length;i++) {
				if(parameterPos[i]>=0) {
					qualityCounter[i] = element.qualityCounter[parameterPos[i]]; // const copy
				} else {
					int[] q = new int[AbstractAggregationIterator.QUALITY_COUNTERS];
					for(int k=0;k<AbstractAggregationIterator.QUALITY_COUNTERS;k++) {
						q[k] = 0;
					}
					qualityCounter[i] = q;
				}
			}
		}
		return new TsEntry(element.timestamp, data, qualityFlag, qualityCounter, interpolated);
	}
}
