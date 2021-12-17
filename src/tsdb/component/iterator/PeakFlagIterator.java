package tsdb.component.iterator;

import java.util.Arrays;
import java.util.Iterator;


import org.tinylog.Logger;

import tsdb.component.Sensor;
import tsdb.util.DataQuality;
import tsdb.util.TsEntry;
import tsdb.util.iterator.TsIterator;

/**
 * Data quality check based on step range sensor property.
 * @author woellauer
 *
 */
public class PeakFlagIterator extends TimeWindowIterator {
	

	private final int SchemaLen;
	private long[] cleanTimestamp;
	private final double[] steps;

	public PeakFlagIterator(Sensor[] sensors, TsIterator input_iterator) {
		super(input_iterator, 180, input_iterator.getSchema());
		this.SchemaLen = schema.length;
		cleanTimestamp = new long[input_iterator.getNames().length];
		//Logger.info("PeakFlagIterator schema " + Arrays.toString(input_iterator.getNames()));
		steps = Arrays.stream(sensors).mapToDouble(Sensor::getStepMax).toArray();
	}

	@Override
	protected TsEntry processElement(TsEntry current) {
		//Logger.info("processElement " + current.toString());
		/*Logger.info("---");
		Logger.info("current: "  + TimeUtil.oleMinutesToText(current.timestamp));
		{
			Iterator<TsEntry> it = past.descendingIterator();
			while(it.hasNext()) {
				TsEntry e = it.next();
				Logger.info("past: "  + TimeUtil.oleMinutesToText(e.timestamp));	
			}
		}
		{
			Iterator<TsEntry> it = future.iterator();
			while(it.hasNext()) {
				TsEntry e = it.next();
				Logger.info("future: "  + TimeUtil.oleMinutesToText(e.timestamp));	
			}
		}
		Logger.info("---");*/

		//Logger.info(TimeUtil.oleMinutesToText(current.timestamp));

		float[] prev = new float[SchemaLen];
		for(int columnIndex = 0; columnIndex < SchemaLen; columnIndex++) {
			prev[columnIndex] = Float.NaN;
		}
		
		//Logger.info("pastFilled " + isPastFilled());
		if(past.isEmpty()){
			float[] currentData = current.data;
			DataQuality[] currentQf = current.qualityFlag;
			for(int columnIndex = 0; columnIndex < SchemaLen; columnIndex++) {
				if(currentQf[columnIndex] == DataQuality.PHYSICAL) {
					prev[columnIndex] = currentData[columnIndex];
				}
			}
		} else {
			int fillCount = 0;
			Iterator<TsEntry> it = past.descendingIterator();
			pastLoop: while(it.hasNext()) {
				TsEntry e = it.next();
				DataQuality[] qf = e.qualityFlag;
				float[] data = e.data;
				for(int columnIndex = 0; columnIndex < SchemaLen; columnIndex++) {
					if(!Float.isFinite(prev[columnIndex]) && qf[columnIndex] == DataQuality.PHYSICAL) {
						prev[columnIndex] = data[columnIndex];
						fillCount++;
						if(fillCount == SchemaLen) {
							//Logger.info("break");
							break pastLoop;
						}
					}
				}
			}
			if(fillCount < SchemaLen && !isPastFilled()) {
				float[] currentData = current.data;
				DataQuality[] currentQf = current.qualityFlag;
				for(int columnIndex = 0; columnIndex < SchemaLen; columnIndex++) {
					if(!Float.isFinite(prev[columnIndex]) && currentQf[columnIndex] == DataQuality.PHYSICAL) {
						prev[columnIndex] = currentData[columnIndex];
					}
				}				
			}
		}		

		DataQuality[] flags = new DataQuality[SchemaLen];
		for(int columnIndex = 0; columnIndex < SchemaLen; columnIndex++) {
			float currV = current.data[columnIndex];
			float prevV = prev[columnIndex];
			DataQuality currQ = current.qualityFlag[columnIndex];
			if(currQ == DataQuality.PHYSICAL) {
				if(steps[columnIndex] == Float.MAX_VALUE) { // no step check
					currQ = DataQuality.STEP;	
				} else {  // step check
					if(cleanTimestamp[columnIndex] <= current.timestamp && Float.isFinite(prevV)) {
						//Logger.info("check " + current.toString() +  "prev " + prevV);
						currQ = DataQuality.STEP;
						float diff = Math.abs(prevV - currV);
						//Logger.info("diff " + diff);
						if(diff > steps[columnIndex]) {
							for(TsEntry e : future) {
								float f = e.data[columnIndex];
								if(Float.isFinite(f)) {
									float fdiff = Math.abs(prevV - f);
									if(fdiff < steps[columnIndex]) {
										currQ = DataQuality.PHYSICAL;
										cleanTimestamp[columnIndex] = e.timestamp;
										break;
									}
								}
							}
						}
					}
				}				
			}
			flags[columnIndex] = currQ;			

			/*if(current.timestamp < cleanTimestamp[columnIndex]) {
				currQ = DataQuality.NO;
				Logger.info(TimeUtil.oleMinutesToText(current.timestamp) + " L " + currV);
			} else if(Float.isFinite(currV) && Float.isFinite(prevV)) {
				float diff = Math.abs(prevV - currV);
				if(diff >= 10) {
					//Logger.info(TimeUtil.oleMinutesToText(current.timestamp) + " d " + diff);
					for(TsEntry e : future) {
						float f = e.data[columnIndex];
						if(Float.isFinite(f)) {
							float fdiff = Math.abs(prevV - f);
							if(fdiff <= 10) {
								currQuality = DataQuality.NO;
								Logger.info(TimeUtil.oleMinutesToText(e.timestamp) + " f " + fdiff);
								cleanTimestamp[columnIndex] = e.timestamp;
								break;
							}
						}
					}
				}
			}
			flags[columnIndex] = currQuality;*/
		}
		return new TsEntry(current.timestamp, current.data, flags);
	}
}
