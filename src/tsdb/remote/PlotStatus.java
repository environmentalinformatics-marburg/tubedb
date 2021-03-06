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
	public final float voltage_min_watch;
	public final float voltage_min_good;
	public final float voltage_min_error;
	public final PlotMessage plotMessage;
	
	public PlotStatus(String plotID, int firstTimestamp, int lastTimestamp, float voltage, float voltage_min_watch, float voltage_min_good, float voltage_min_error, PlotMessage plotMessage) {
		this.plotID = plotID;
		this.firstTimestamp = firstTimestamp;
		this.lastTimestamp = lastTimestamp;
		this.voltage = voltage;
		this.voltage_min_watch = voltage_min_watch;
		this.voltage_min_good = voltage_min_good;
		this.voltage_min_error = voltage_min_error;
		this.plotMessage = plotMessage;
	}
	
	public static final Comparator<PlotStatus> END_COMPARATOR = (t1,t2)->{
		return Integer.compare(t1.lastTimestamp,t2.lastTimestamp);
	};
}
