package tsdb.streamdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;


import org.tinylog.Logger;

import tsdb.util.DataEntry;
import tsdb.util.DataRow;
import tsdb.util.TimeUtil;
import tsdb.util.TimeSeriesMask;
import tsdb.util.Util;
import tsdb.util.iterator.TimestampSeries;
import tsdb.util.iterator.TsIterator;

/**
 * Implementations of StreamStorage with StreamDB
 * @author woellauer
 *
 */
public class StreamStorageStreamDB implements StreamStorage {
	

	private StreamDB streamdb;

	private boolean logging;

	public StreamStorageStreamDB(String streamdbPathPrefix) {		
		this.streamdb = new StreamDB(streamdbPathPrefix);
	}

	public void setLogging(boolean logging) {
		this.logging = logging;
	}

	@Override
	public void clear() {
		streamdb.clear();		
	}

	@Override
	public void close() {
		streamdb.close();		
	}

	@Override
	public void insertData(String streamName, TreeMap<Long, DataRow> eventMap, String[] sensorNames) {
		Logger.trace("insertData "+Arrays.toString(sensorNames)+"  in "+streamName);
		ArrayList<DataEntry> sensorData = new ArrayList<DataEntry>(eventMap.size());
		for(int i=0;i<sensorNames.length;i++) {
			sensorData.clear();
			for(DataRow event:eventMap.values()) {
				float value = (float) event.data[i];
				if(!Float.isNaN(value)&&!(value==-9999f)&&(-999999f<value)&&(value<999999f)) { // NaN some files (in AET06)
					if(value<-9999f||value>9999f) {
						Logger.trace(value+"                     "+sensorNames[i]+"                "+streamName);
					}
					sensorData.add(new DataEntry((int) event.timestamp,value));
				}
			}
			if(!sensorData.isEmpty()) {
				streamdb.insertSensorData(streamName, sensorNames[i], sensorData.toArray(new DataEntry[0]));
			}
		}
	}

	@Override
	public void insertDataRows(String streamName, List<DataRow> eventList,long first, long last, String[] sensorNames) {
		ArrayList<DataEntry> sensorData = new ArrayList<DataEntry>(eventList.size());
		for(int i=0;i<sensorNames.length;i++) {
			sensorData.clear();
			for(DataRow event:eventList) {
				float value = (float) event.data[i];
				if(!Float.isNaN(value)) {
					sensorData.add(new DataEntry((int) event.timestamp,value));
				}
			}
			if(!sensorData.isEmpty()) {
				streamdb.insertSensorData(streamName, sensorNames[i], sensorData.toArray(new DataEntry[0]));
			}
		}	
	}

	@Override
	public TsIterator getRawIterator(String stationName, String[] sensorNames, Long start, Long end) {
		Logger.trace("StreamDB get "+stationName+" with "+Util.arrayToString(sensorNames)+"     at "+TimeUtil.oleMinutesToText(start)+" - "+TimeUtil.oleMinutesToText(end));
		int minTimestamp;
		int maxTimestamp;
		if(start==null) {
			minTimestamp = Integer.MIN_VALUE;
		} else {
			minTimestamp = (int)(long)start;
		}
		if(end==null) {
			maxTimestamp = Integer.MAX_VALUE;
		} else {
			maxTimestamp = (int)(long)end;
		}		
		return streamdb.getTsIterator(stationName, sensorNames, minTimestamp, maxTimestamp);
	}

	@Override
	public StreamIterator getRawSensorIterator(String stationName, String sensorName, Long start, Long end) {
		//Logger.info("StreamDB get raw sensor "+stationName+" with "+sensorName+"     at "+TimeUtil.oleMinutesToText(start)+" - "+TimeUtil.oleMinutesToText(end));
		int minTimestamp;
		int maxTimestamp;
		if(start==null) {
			minTimestamp = Integer.MIN_VALUE;
		} else {
			minTimestamp = start.intValue();
		}
		if(end==null) {
			maxTimestamp = Integer.MAX_VALUE;
		} else {
			maxTimestamp = end.intValue();
		}
		return streamdb.getSensorIterator(stationName, sensorName, minTimestamp, maxTimestamp);
	}

	@Override
	public void getInfo() {
		//TODO		
	}

	public long[] getStationTimeInterval(String streamName) {
		if(!streamdb.existStation(streamName)) {
			return null;
		}
		int[] interval = streamdb.getStationTimeInterval(streamName);
		if(interval == null) {
			return null;
		}
		return new long[]{interval[0],interval[1]};
	}
	
	/**
	 * 
	 * @param streamName
	 * @param excludeSensorNames nullable
	 * @return
	 */
	public long[] getStationTimeInterval(String streamName, Set<String> excludeSensorNames) {
		if(!streamdb.existStation(streamName)) {
			return null;
		}
		int[] interval = streamdb.getStationTimeInterval(streamName, excludeSensorNames);
		if(interval == null) {
			return null;
		}
		return new long[]{interval[0],interval[1]};
	}
	
	public int[] getStationTimeInterval(String streamName, int min, int max) {
		if(!streamdb.existStation(streamName)) {
			return null;
		}
		int[] interval = streamdb.getStationTimeInterval(streamName, min, max);
		return interval;
	}
	/**
	 * 
	 * @param streamName
	 * @param min
	 * @param max
	 * @param excludeSensorNames  nullable
	 * @return
	 */
	public int[] getStationTimeInterval(String streamName, int min, int max, Set<String> excludeSensorNames) {
		if(!streamdb.existStation(streamName)) {
			return null;
		}
		int[] interval = streamdb.getStationTimeInterval(streamName, min, max, excludeSensorNames);
		return interval;
	}

	public int[] getSensorTimeInterval(String stationName, String sensorName) {
		return streamdb.getSensorTimeInterval(stationName, sensorName);
	}
	
	public int[] getSensorTimeInterval(String stationName, String sensorName, int min, int max) {
		return streamdb.getSensorTimeInterval(stationName, sensorName, min, max);
	}

	@Override
	public String[] getSensorNames(String stationName) {
		NavigableSet<String> set = streamdb.getSensorNames(stationName);
		if(set==null) {
			return null;
		}
		return set.toArray(new String[set.size()]);
	}

	/**
	 * Check if station exists in StreamDB. Station exists only if it contains time series data.
	 * @param stationID
	 * @return
	 */
	public boolean existStation(String stationID) {
		return streamdb.existStation(stationID);
	}

	/**
	 * Check if data of sensor exits in station. If station does not exist return false. 
	 * @param stationID
	 * @param sensorName
	 * @return
	 */
	public boolean existSensor(String stationID, String sensorName) {
		return streamdb.existSensor(stationID, sensorName);
	}

	@Override
	public TimeSeriesMask getTimeSeriesMask(String stationName, String sensorName) {
		return streamdb.getSensorTimeSeriesMask(stationName, sensorName, false);
	}

	@Override
	public void setTimeSeriesMask(String stationName, String sensorName, TimeSeriesMask timeSeriesMask, boolean commit) {
		streamdb.setSensorTimeSeriesMask(stationName, sensorName, timeSeriesMask);
		if(commit) {
			streamdb.commit();
		}
	}

	@Override
	public void commit() {
		streamdb.commit();
	}

	@Override
	public void insertTimestampSeries(TimestampSeries timestampSeries) {
		if(logging) Logger.info("streamDB insert TimestampSeries "+timestampSeries.name);
		String stationName = timestampSeries.name;
		for(String sensorName:timestampSeries.sensorNames) {
			DataEntry[] data = timestampSeries.toDataEntyArray(sensorName);
			if(data!=null&&data.length>0) {
				//System.out.println("insert in station "+stationName+" sensor "+sensorName+"  elements "+data.length);
				streamdb.insertSensorData(stationName, sensorName, data);
			}
		}

	}

	public void insertDataRows(String stationName, String[] sensorNames, Collection<DataRow> dataRows) {
		int sensors = sensorNames.length;
		ArrayList<DataEntry> dataEntryList = new ArrayList<DataEntry>(dataRows.size());
		for(int i=0;i<sensors;i++) {
			dataEntryList.clear();
			for(DataRow dataRow:dataRows) {
				float v = dataRow.data[i];
				if(Float.isFinite(v)) {
					dataEntryList.add(new DataEntry((int)dataRow.timestamp, v));
				}

			}
			DataEntry[] dataEntries = dataEntryList.toArray(new DataEntry[0]);
			insertDataEntryArray(stationName, sensorNames[i], dataEntries);
		}		
	}

	public void insertDataEntryArray(String stationName, String sensorName, DataEntry[] dataEntries) {
		if(logging) Logger.info("streamDB insert DataEntyArray "+stationName+"/"+sensorName);
		if(dataEntries!=null&&dataEntries.length>0) {
			streamdb.insertSensorData(stationName, sensorName, dataEntries);
		}
	}

	@Override
	public void removeInterval(String stationName, int start, int end) {
		streamdb.removeInterval(stationName, start, end);
	}

	public NavigableSet<String> getStationNames() {
		return streamdb.getStationNames();		
	}

	public void clearMaskOfStation(String stationName) {
		streamdb.clearMaskOfStation(stationName);		
	}

}
