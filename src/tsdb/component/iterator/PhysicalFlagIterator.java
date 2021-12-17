package tsdb.component.iterator;

import tsdb.component.Sensor;
import tsdb.util.DataQuality;
import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.TsSchema.Aggregation;
import tsdb.util.iterator.InputIterator;
import tsdb.util.iterator.TsIterator;

/**
 * adds Quality flags to input data: physical check 
 * @author woellauer
 *
 */
public class PhysicalFlagIterator extends InputIterator {
	//

	private final Sensor[] sensors;
	private final int len; 
	
	public static TsSchema createSchema(TsSchema tsSchema) {
		String[] names = tsSchema.names;
		Aggregation aggregation = tsSchema.aggregation;
		int timeStep = tsSchema.timeStep;
		boolean isContinuous = tsSchema.isContinuous;
		boolean hasQualityFlags = true;
		boolean hasInterpolatedFlags = false;
		boolean hasQualityCounters = false;
		return new TsSchema(names, aggregation, timeStep, isContinuous, hasQualityFlags, hasInterpolatedFlags, hasQualityCounters);		
	}	

	public PhysicalFlagIterator(Sensor[] sensors, TsIterator input_iterator) {
		super(input_iterator, createSchema(input_iterator.getSchema()));
		this.sensors = sensors;
		this.len = schema.length;
	}

	@Override
	public TsEntry next() {
		TsEntry currEntry = input_iterator.next();
		float[] currData = currEntry.data;
		DataQuality[] flags = new DataQuality[len];
		for(int columnIndex = 0; columnIndex < len; columnIndex++) {
			flags[columnIndex] = sensors[columnIndex].checkPhysicalRange(currData[columnIndex]) ? DataQuality.PHYSICAL : DataQuality.NO;
		}
		return new TsEntry(currEntry.timestamp, currData, flags);
	}
}

