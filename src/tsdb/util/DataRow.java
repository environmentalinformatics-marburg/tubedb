package tsdb.util;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Entry of one row of data values at a timestamp. 
 * immutable (Data values should not be changed.)
 * @author woellauer
 */
public class DataRow {
	
	public final long timestamp;
	public final float[] data;
	
	public DataRow(float[] data, long timestamp) {
		this.data = data;
		this.timestamp = timestamp;
	}
	
	@Override
	public String toString() {
		return TimeUtil.oleMinutesToText(timestamp) + " " + Arrays.toString(data);
	}
	
	public static final Comparator<DataRow> TIMESTAMP_COMPARATOR = new Comparator<DataRow>() {
		@Override
		public int compare(DataRow t1, DataRow t2) {
			return Long.compare(t1.timestamp, t2.timestamp);
		}		
	};
}
