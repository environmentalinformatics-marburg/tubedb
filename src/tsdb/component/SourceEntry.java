package tsdb.component;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;

import tsdb.util.DataEntry;
import tsdb.util.DataRow;
import tsdb.util.TimeUtil;
import tsdb.util.TsSchema;
import tsdb.util.Util;
import tsdb.util.iterator.TimestampSeries;

/**
 * Info about an imported data-file
 * immutable (Fields should not be changed.)
 * @author woellauer
 */
public class SourceEntry implements Serializable {
	private static final long serialVersionUID = 8603819733815550502L;
	
	public final String path;
	public final String filename;	
	public long firstTimestamp;
	public long lastTimestamp;	
	public final String stationName;
	public final int rows;
	public final String[] headerNames;
	public final String[] sensorNames;
	public final int timeStep;
	
	
	public SourceEntry(Path filename, String stationName, long firstTimestamp, long lastTimestamp, int rows, String[] headerNames, String[] sensorNames, int timeStep) {
		this.path = filename == null ? "" : filename.subpath(0, filename.getNameCount()-1).toString();
		this.filename = filename == null ? "" :filename.getFileName().toString();
		this.stationName = stationName;
		this.firstTimestamp = firstTimestamp;
		this.lastTimestamp = lastTimestamp;
		this.rows = rows;
		this.headerNames = headerNames;
		this.sensorNames = sensorNames;
		this.timeStep = timeStep;
	}
	
	public static SourceEntry of(TimestampSeries timestampSeries, Path filename, String[] sensorNames) {
		return new SourceEntry(filename,timestampSeries.name,timestampSeries.getFirstTimestamp(),timestampSeries.getLastTimestamp(),timestampSeries.size(),timestampSeries.sensorNames,sensorNames, TsSchema.NO_CONSTANT_TIMESTEP);
	}
	
	public static SourceEntry of(String stationName, String[] sensorNames, ArrayList<DataRow> dataRows, Path filename) {
		long firstTimestamp = dataRows.get(0).timestamp;
		long lastTimestamp = dataRows.get(dataRows.size()-1).timestamp;
		return new SourceEntry(filename, stationName, firstTimestamp, lastTimestamp, dataRows.size(), sensorNames, sensorNames, TsSchema.NO_CONSTANT_TIMESTEP);
	}
	
	public static SourceEntry ofDataEntry(String stationName, String sensorSrcName, String sensorDstName, ArrayList<DataEntry> dataEntryList, Path filename) {
		long firstTimestamp = dataEntryList.get(0).timestamp;
		long lastTimestamp = dataEntryList.get(dataEntryList.size()-1).timestamp;
		String[] sensorSrcNames = new String[] {sensorSrcName};
		String[] sensorDstNames = new String[] {sensorDstName};
		return new SourceEntry(filename, stationName, firstTimestamp, lastTimestamp, dataEntryList.size(), sensorSrcNames, sensorDstNames, TsSchema.NO_CONSTANT_TIMESTEP);
	}
	
	@Override
	public String toString() {
		return filename+"\t"+stationName+"\t"+TimeUtil.oleMinutesToText(firstTimestamp)+"\t"+TimeUtil.oleMinutesToText(lastTimestamp);
	}
	
	public String getStationName() {
		return stationName;
	}
	
	public String getFullPath() {
		return path+'/'+filename;
	}
	
	public String getTranslation() {
		if(headerNames.length==0 || sensorNames.length==0) {
			return (headerNames.length==0?'?':Util.arrayToStringNullable(headerNames))+"->"+(sensorNames.length==0?'?':Util.arrayToStringNullable(sensorNames));
		} else {
			String[] translation = new String[headerNames.length];
			for (int i = 0; i < headerNames.length; i++) {
				if(headerNames[i].equals(sensorNames[i])) {
					translation[i] = headerNames[i];
				} else {
					if(sensorNames[i]==null) {
						translation[i] = '<'+headerNames[i]+'>';
					} else {
						translation[i] = headerNames[i]+"->"+sensorNames[i];
					}
				}
			}			
			return Util.arrayToStringNullable(translation);
		}		
	}	
}
