package tsdb.component.iterator;

import tsdb.component.Sensor;
import tsdb.util.DataQuality;
import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.TsSchema.Aggregation;
import tsdb.util.iterator.InputProcessingIterator;
import tsdb.util.iterator.TsIterator;

/**
 * adds Quality flags to input data: physical- and step-check 
 * @author woellauer
 *
 */
public class QualityFlagIterator extends InputProcessingIterator {

	private static final int MAX_TIME_STEP = 60;
	
	long[] prevTimestamps;
	float[] prevData;
	
	final Sensor[] sensors;
	
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

	public QualityFlagIterator(Sensor[] sensors, TsIterator input_iterator) {
		super(input_iterator, createSchema(input_iterator.getSchema()));
		this.sensors = sensors;
		this.prevTimestamps = new long[schema.length];
		this.prevData = new float[schema.length];
		for(int i=0;i<schema.length;i++) {
			prevTimestamps[i] = -1000;
			prevData[i] = Float.NaN; 
		}
	}
	
	@Override
	public TsEntry getNext() {
		if(input_iterator.hasNext()) {
			TsEntry currEntry = input_iterator.next();
			long currTimestamp = currEntry.timestamp;
			float[] currData = currEntry.data;
			DataQuality[] flags = new DataQuality[schema.length];
			for(int columnIndex=0;columnIndex<schema.length;columnIndex++) {
				float currValue = currData[columnIndex];
				Sensor sensor = sensors[columnIndex];
				DataQuality currQuality = DataQuality.Na;
				if(!Float.isNaN(currValue)) {
					currQuality = DataQuality.NO;
					if(sensor.checkPhysicalRange(currValue)) {
						currQuality = DataQuality.PHYSICAL;
						long timewindow = prevTimestamps[columnIndex]+MAX_TIME_STEP;
						if( (!(currTimestamp<=timewindow))||sensor.checkStepRange(prevData[columnIndex], currValue)) {//step check
							currQuality = DataQuality.STEP;
							/*if(sensor.checkEmpiricalRange(currValue)) { // no empirical check here!
								currQuality = DataQuality.EMPIRICAL;
							}*/
						} 					
					}
					//if value is not NaN store element in prev
					prevTimestamps[columnIndex] = currTimestamp;
					prevData[columnIndex] = currValue;
				} 
				flags[columnIndex] = currQuality;
			}
			return new TsEntry(currTimestamp, currData, flags);
		} else {
			return null; // no elements left
		}
	}
}

