package tsdb.run;

import java.util.NavigableSet;


import org.tinylog.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.streamdb.StreamIterator;
import tsdb.util.DataEntry;

public class FullDataReader {
	

	private final TsDB tsdb;
	
	private long total_count = 0;
	private long series_count = 0;
	
	private long station_count = 0;

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
				boolean isValidStation = false;
				try {
					String[] sensorNames = tsdb.streamStorage.getSensorNames(stationName);
					for(String sensorName:sensorNames) {
						isValidStation = true;
						readSeries(stationName,sensorName);
					}
				} catch(Exception e) {
					Logger.error(e);
				}
				if(isValidStation) {
					station_count++;
				}
			}
		} catch (Exception e) {
			Logger.error(e);
		}
		long timeEndImport = System.currentTimeMillis();
		Logger.info((timeEndImport-timeStartImport)/1000+" s "+(timeEndImport-timeStartImport)+" ms "+total_count+" total_count    "+series_count+" series_count"+"  "+station_count+" station_count");
	}

	private void readSeries(String stationName, String sensorName) {
		
		StreamIterator it = tsdb.streamStorage.getRawSensorIterator(stationName, sensorName, null, null);
		series_count++;
		
		while(it.hasNext()) {
			@SuppressWarnings("unused")
			DataEntry e = it.next();
			//float v = e.value;
			total_count++;
		}	
	}
}
