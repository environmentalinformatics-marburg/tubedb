package tsdb.util;

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
}
