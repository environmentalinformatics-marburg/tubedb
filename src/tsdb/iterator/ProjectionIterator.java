package tsdb.iterator;

import tsdb.util.DataQuality;
import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.Util;
import tsdb.util.TsSchema.Aggregation;
import tsdb.util.iterator.InputIterator;
import tsdb.util.iterator.TsIterator;

/**
 * converts input schema to output schema
 * output schema needs to be subset of input schema (with arbitrary order)
 * @author woellauer
 *
 */
public class ProjectionIterator extends InputIterator {

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

	public ProjectionIterator(TsIterator input_iterator, String[] outputSchema) {
		super(input_iterator, createSchema(input_iterator.getSchema(), outputSchema));
		this.parameterPos = Util.stringArrayToPositionIndexArray(outputSchema, input_iterator.getNames(), true, true);
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
			data[i] = element.data[parameterPos[i]];
		}
		DataQuality[] qualityFlag = null;
		if(element.qualityFlag!=null) {
			qualityFlag = new DataQuality[schema.length];
			for(int i=0;i<schema.length;i++) {
				qualityFlag[i] = element.qualityFlag[parameterPos[i]];
			}
		}
		boolean[] interpolated = null;
		if(element.interpolated!=null) {
			interpolated = new boolean[schema.length];
			for(int i=0;i<schema.length;i++) {
				interpolated[i] = element.interpolated[parameterPos[i]];
			}
		}
		int[][] qualityCounter = null;
		if(element.qualityCounter!=null) {
			qualityCounter = new int[schema.length][];
			for(int i=0;i<schema.length;i++) {
				qualityCounter[i] = element.qualityCounter[parameterPos[i]]; // const copy
			}
		}		
		return new TsEntry(element.timestamp, data, qualityFlag, qualityCounter, interpolated);
	}
}
