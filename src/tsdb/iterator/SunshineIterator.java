package tsdb.iterator;

import java.util.Arrays;

import tsdb.util.AssumptionCheck;
import tsdb.util.DataQuality;
import tsdb.util.TsEntry;
import tsdb.util.iterator.InputIterator;
import tsdb.util.iterator.TsIterator;

/**
 * fills sunshine columns with calculated values from Rn_300 column.
 * 1: sunhine at this point in time
 * 0: no sunshine at this point in time
 * @author woellauer
 *
 */
public class SunshineIterator extends InputIterator {
	
	public static final String RADIATION_SENSOR_NAME = "SWDR";
	public static final String SUNSHINE_SENSOR_NAME = "SD";
	private int radiation_pos = -1;
	private int SD_pos = -1;	

	public SunshineIterator(TsIterator input_iterator) {
		super(input_iterator, input_iterator.getSchema());
		String[] names = this.getNames();
		for(int i=0;i<names.length;i++) {
			if(names[i].equals(RADIATION_SENSOR_NAME)) {
				radiation_pos = i;
			}
			if(names[i].equals(SUNSHINE_SENSOR_NAME)) {
				SD_pos = i;
			}
		}
		AssumptionCheck.throwTrue(radiation_pos<0||SD_pos<0,"no radiation or sunshine for SunshineIterator");		
	}

	@Override
	public TsEntry next() {
		TsEntry entry = input_iterator.next();
		float[] data = Arrays.copyOf(entry.data, entry.data.length);
		float value = entry.data[radiation_pos];
		data[SD_pos] = Float.isNaN(value)?Float.NaN:(value>=120?1f:0f);
		DataQuality[] qf;
		if(entry.qualityFlag!=null) {
			qf = Arrays.copyOf(entry.qualityFlag, entry.qualityFlag.length);
			qf[SD_pos] = entry.qualityFlag[radiation_pos];
		} else {
			qf = null;
		}
		return new TsEntry(entry.timestamp, data, qf);
	}

}
