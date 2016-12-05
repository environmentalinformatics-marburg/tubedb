package tsdb.loader.be;

import java.nio.file.Path;
import java.util.Arrays;

import tsdb.util.Interval;
import tsdb.util.TimeConverter;
import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.iterator.TimestampSeries;
import tsdb.util.iterator.TsIterator;

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
		Interval i = getTimeInterval();
		return TimeUtil.oleMinutesToText(i.start, i.end)+"\n"+Arrays.toString(sensorHeaders)+"\n"+filename;
	}
	
	/**
	 * Get interval of covered time.
	 * @return
	 */
	public Interval getTimeInterval() {
		return Interval.of((int)time[0], (int)time[time.length-1]);
	}
	
	public TsIterator toTsIterator() {
		return new It();
	}
	
	private class It extends TsIterator {
		
		private int i=0;

		public It() {
			super(new TsSchema(SensorHeader.toSensorNames(sensorHeaders)));
		}

		@Override
		public boolean hasNext() {
			return i<time.length;
		}

		@Override
		public TsEntry next() {
			TsEntry e = TsEntry.of(time[i], data[i]);
			i++;
			return e;
		}
		
	}

	public TimestampSeries toTimestampSeries() {
		String name = filename.getFileName().toString();
		String[] sensorNames = SensorHeader.toSensorNames(sensorHeaders);
		final int len = time.length;
		TsEntry[] entries = new TsEntry[len];		
		for(int i=0;i<len;i++) {
			entries[i] = new TsEntry(time[i], data[i]);
		}		
		return new TimestampSeries(name, sensorNames, Arrays.asList(entries));		
	}
}
