package tsdb.run;

import java.util.List;
import java.util.NavigableSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.streamdb.StreamIterator;
import tsdb.util.DataEntry;

public class FullDataReader {
	private static final Logger log = LogManager.getLogger();

	private final TsDB tsdb;
	
	private long total_count = 0;
	private long series_count = 0;

	public static void main(String[] args) {		
		TsDB tsdb = TsDBFactory.createDefault();
		FullDataReader fullDataReader = new FullDataReader(tsdb);
		fullDataReader.readAll();
		tsdb.close();		
	}

	public FullDataReader(TsDB tsdb) {
		this.tsdb = tsdb;
	}


	public void readAll() {		
		NavigableSet<String> stationNames = tsdb.streamStorage.getStationNames();	

		long timeStartImport = System.currentTimeMillis();
		try {
			for(String stationName:stationNames) {
				try {
					String[] sensorNames = tsdb.streamStorage.getSensorNames(stationName);
					for(String sensorName:sensorNames) {
						readSeries(stationName,sensorName);
					}
				} catch(Exception e) {
					log.error(e);
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
		long timeEndImport = System.currentTimeMillis();
		log.info((timeEndImport-timeStartImport)/1000+" s Export "+(timeEndImport-timeStartImport)+" ms Export "+total_count+" total_count    "+series_count+" series_count");
	}

	private void readSeries(String stationName, String sensorName) {
		
		StreamIterator it = tsdb.streamStorage.getRawSensorIterator(stationName, sensorName, null, null);
		series_count++;
		
		while(it.hasNext()) {
			DataEntry e = it.next();
			float v = e.value;
			total_count++;
		}
		
	
	}

}
