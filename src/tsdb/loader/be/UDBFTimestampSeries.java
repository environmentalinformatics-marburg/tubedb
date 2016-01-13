package tsdb.loader.be;

import java.nio.file.Path;
import java.util.Arrays;

import tsdb.util.TimeConverter;

/**
 * contains relevant data of a UDBF-File.
 * immutable (Field values should not be changed.) 
 * @author woellauer
 */
public class UDBFTimestampSeries {
	
	public final Path filename;
	public final SensorHeader[] sensorHeaders;
	public final TimeConverter timeConverter;
	public final long[] time;
	public final float[][] data;

	public UDBFTimestampSeries(Path filename, SensorHeader[] sensorHeaders, TimeConverter timeConverter, long[] time, float[][] data) {
		this.filename = filename;
		this.sensorHeaders = sensorHeaders;
		this.timeConverter = timeConverter;
		this.time = time;
		this.data = data;
	}
	
	public String[] getHeaderNames() {
		String[] headerNames = new String[sensorHeaders.length];
		for(int i=0;i<sensorHeaders.length;i++) {
			headerNames[i] = sensorHeaders[i].name;
		}
		return headerNames;
	}
	
	@Override
	public String toString() {
		return Arrays.toString(sensorHeaders)+" "+filename;
	}
}
