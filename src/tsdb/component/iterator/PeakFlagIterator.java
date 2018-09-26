package tsdb.component.iterator;

import java.util.Arrays;
import java.util.Iterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.component.Sensor;
import tsdb.util.DataQuality;
import tsdb.util.TsEntry;
import tsdb.util.iterator.TsIterator;

/**
 * HEG25 Ta_200_min aggregation to days
 * @author woellauer
 *
 */
public class PeakFlagIterator extends TimeWindowIterator {
	private static final Logger log = LogManager.getLogger();

	private final int len;
	private long[] cleanTimestamp;
	private final double[] steps;

	public PeakFlagIterator(Sensor[] sensors, TsIterator input_iterator) {
		super(input_iterator, 180, input_iterator.getSchema());
		this.len = schema.length;
		cleanTimestamp = new long[input_iterator.getNames().length];
		//log.info("PeakFlagIterator schema " + Arrays.toString(input_iterator.getNames()));
		steps = Arrays.stream(sensors).mapToDouble(Sensor::getStepMax).toArray();
	}

	@Override
	protected TsEntry processElement(TsEntry current) {
		/*log.info("---");
		log.info("current: "  + TimeUtil.oleMinutesToText(current.timestamp));
		{
			Iterator<TsEntry> it = past.descendingIterator();
			while(it.hasNext()) {
				TsEntry e = it.next();
				log.info("past: "  + TimeUtil.oleMinutesToText(e.timestamp));	
			}
		}
		{
			Iterator<TsEntry> it = future.iterator();
			while(it.hasNext()) {
				TsEntry e = it.next();
				log.info("future: "  + TimeUtil.oleMinutesToText(e.timestamp));	
			}
		}
		log.info("---");*/

		//log.info(TimeUtil.oleMinutesToText(current.timestamp));

		float[] prev = new float[len];
		for(int columnIndex = 0; columnIndex < len; columnIndex++) {
			prev[columnIndex] = Float.NaN;
		}		
		int fillCount = 0;
		Iterator<TsEntry> it = past.descendingIterator();
		pastLoop: while(it.hasNext()) {
			TsEntry e = it.next();
			DataQuality[] qf = e.qualityFlag;
			float[] data = e.data;
			for(int columnIndex = 0; columnIndex < len; columnIndex++) {
				if(!Float.isFinite(prev[columnIndex]) && qf[columnIndex] == DataQuality.PHYSICAL) {
					prev[columnIndex] = data[columnIndex];
					fillCount++;
					if(fillCount == len) {
						//log.info("break");
						break pastLoop;
					}
				}
			}

		}

		DataQuality[] flags = new DataQuality[len];
		for(int columnIndex = 0; columnIndex < len; columnIndex++) {
			float currV = current.data[columnIndex];
			float prevV = prev[columnIndex];
			DataQuality currQ = current.qualityFlag[columnIndex];
			if(cleanTimestamp[columnIndex] <= current.timestamp && currQ == DataQuality.PHYSICAL && Float.isFinite(prevV)) {
				currQ = DataQuality.STEP;
				float diff = Math.abs(prevV - currV);
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
			flags[columnIndex] = currQ;			
			
			/*if(current.timestamp < cleanTimestamp[columnIndex]) {
				currQ = DataQuality.NO;
				log.info(TimeUtil.oleMinutesToText(current.timestamp) + " L " + currV);
			} else if(Float.isFinite(currV) && Float.isFinite(prevV)) {
				float diff = Math.abs(prevV - currV);
				if(diff >= 10) {
					//log.info(TimeUtil.oleMinutesToText(current.timestamp) + " d " + diff);
					for(TsEntry e : future) {
						float f = e.data[columnIndex];
						if(Float.isFinite(f)) {
							float fdiff = Math.abs(prevV - f);
							if(fdiff <= 10) {
								currQuality = DataQuality.NO;
								log.info(TimeUtil.oleMinutesToText(e.timestamp) + " f " + fdiff);
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
