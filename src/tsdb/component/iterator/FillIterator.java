package tsdb.component.iterator;


import org.tinylog.Logger;

import tsdb.util.TsEntry;
import tsdb.util.iterator.TsIterator;

public class FillIterator extends PrevLookaheadIterator {
	

	public FillIterator(TsIterator input_iterator) {
		super(input_iterator, 3, input_iterator.getSchema());
	}

	@Override
	protected TsEntry processElement(TsEntry current) {
		float[] data = current.data;
		long timestamp = current.timestamp;
		long tmin = timestamp - 180;
		float[] result = new float[len];
		boolean[] interpolated = new boolean[len];
		boolean isInterpolated = false;
		for(int i = 0; i < len; i++) {
			result[i] = data[i];
			if(!Float.isFinite(result[i]) && tmin <= prevTimestamp[i]) {
				long tmax = prevTimestamp[i] + 240;
				for(TsEntry f:future) {
					if(tmax < f.timestamp) {
						break;
					}
					if(Float.isFinite(f.data[i])) {
						long gapTimeRange = (f.timestamp - prevTimestamp[i]) / 60;
						long a1TimeRange = (current.timestamp - prevTimestamp[i]) / 60;
						long a2TimeRange = (f.timestamp - current.timestamp) / 60;
						double a1 = ((double)a1TimeRange) / gapTimeRange;
						double a2 = ((double)a2TimeRange) / gapTimeRange;
						double ar = 1- a1;
						result[i] = ((a2TimeRange * prevValue[i]) + (a1TimeRange * f.data[i])) / gapTimeRange;
						interpolated[i] = true;
						isInterpolated = true;
						//Logger.info("filled " + TimeUtil.oleMinutesToText(current.timestamp) + "     " + TimeUtil.oleMinutesToText(prevTimestamp[i]) + " " + TimeUtil.oleMinutesToText(f.timestamp));
						//Logger.info("ranges " + gapTimeRange + "  " + a1TimeRange + "  " + a2TimeRange+"    "+a1+" "+a2+" "+ar+"        " + prevValue[i] + " " + f.data[i] +"  " + result[i]);
						break;
					}
				}
			}
		}
		return isInterpolated ? new TsEntry(timestamp, result, current.qualityFlag, current.qualityCounter, interpolated) : current;
	}

}
