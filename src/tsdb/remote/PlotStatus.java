package tsdb.remote;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Serializable plot status
 * immutable
 * @author woellauer
 */
public class PlotStatus implements Serializable {
	private static final long serialVersionUID = 7104450052055571808L;
	
	public final String plotID;
	public final int firstTimestamp;
	public final int lastTimestamp;
	public final float voltage;
	
	public PlotStatus(String plotID, int firstTimestamp, int lastTimestamp, float voltage) {
		this.plotID = plotID;
		this.firstTimestamp = firstTimestamp;
		this.lastTimestamp = lastTimestamp;
		this.voltage = voltage;
	}
	
	public static final Comparator<PlotStatus> END_COMPARATOR = (t1,t2)->{
		return Integer.compare(t1.lastTimestamp,t2.lastTimestamp);
	};

}
