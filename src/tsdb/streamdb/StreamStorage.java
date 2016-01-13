package tsdb.streamdb;

import java.util.List;
import java.util.TreeMap;

import tsdb.util.DataRow;
import tsdb.util.TimeSeriesMask;
import tsdb.util.iterator.TimestampSeries;
import tsdb.util.iterator.TsIterator;

/**
 * This class encapsulates StreamDB and provides basic methods to store and query time series data. 
 * @author woellauer
 *
 */
public interface StreamStorage {

	public void clear();

	/**
	 * closes all open files and flushes pending data to disk.
	 */
	public void close();

	/**
	 * Saves new data on disk. If some timestamps exist already in database, no data is overwritten.
	 * @param streamName
	 * @param eventMap
	 * @param sensorNames 
	 */
	public void insertData(String streamName, TreeMap<Long,DataRow> eventMap, String[] sensorNames);
	
	/**
	 * insert ordered list of data with time span first to last
	 * @param streamName
	 * @param eventList
	 * @param first
	 * @param last
	 * @param sensorNames 
	 */
	public void insertDataRows(String streamName, List<DataRow> eventList, long first, long last, String[] sensorNames);

	public TsIterator getRawIterator(String stationName, String[] sensorNames, Long minTimestamp, Long maxTimestamp);
	
	public void getInfo();

	long[] getStationTimeInterval(String streamName);

	String[] getSensorNames(String stationName);

	StreamIterator getRawSensorIterator(String stationName, String sensorName, Long start, Long end);

	TimeSeriesMask getTimeSeriesMask(String stationName, String sensorName);

	void setTimeSeriesMask(String stationName, String sensorName, TimeSeriesMask timeSeriesMask);

	public void insertTimestampSeries(TimestampSeries timestampSeries);

	void removeInterval(String stationName, int start, int end);
}
