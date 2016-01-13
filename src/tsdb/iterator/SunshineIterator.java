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
	
	private int Rn_300_pos = -1;
	private int sunshine_pos = -1;	

	public SunshineIterator(TsIterator input_iterator) {
		super(input_iterator, input_iterator.getSchema());
		String[] names = this.getNames();
		System.out.println(Arrays.toString(names));
		for(int i=0;i<names.length;i++) {
			if(names[i].equals("Rn_300")) {
				Rn_300_pos = i;
			}
			if(names[i].equals("sunshine")) {
				sunshine_pos = i;
			}
		}
		AssumptionCheck.throwTrue(Rn_300_pos<0||sunshine_pos<0,"no Rn_300 or sunshine for SunshineIterator");		
	}

	@Override
	public boolean hasNext() {
		return input_iterator.hasNext();
	}

	@Override
	public TsEntry next() {
		TsEntry entry = input_iterator.next();
		float[] data = Arrays.copyOf(entry.data, entry.data.length);
		float value = entry.data[Rn_300_pos];
		data[sunshine_pos] = Float.isNaN(value)?Float.NaN:(value>=120?1f:0f);
		DataQuality[] qf;
		if(entry.qualityFlag!=null) {
			qf = Arrays.copyOf(entry.qualityFlag, entry.qualityFlag.length);
			qf[sunshine_pos] = entry.qualityFlag[Rn_300_pos];
		} else {
			qf = null;
		}
		return new TsEntry(entry.timestamp, data, qf);
	}

}
