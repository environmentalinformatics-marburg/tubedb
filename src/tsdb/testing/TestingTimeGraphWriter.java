package tsdb.testing;

import java.util.ArrayList;
import java.util.NavigableSet;


import org.tinylog.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.streamdb.StreamIterator;
import tsdb.util.DataEntries;
import tsdb.util.DataEntry;
import tsdb.util.TimeSeriesArchivWriter;

public class TestingTimeGraphWriter {
	
	
	public static void main(String[] args) {
		
		String filename = "out.tsa";
		
		TsDB tsdb = TsDBFactory.createDefault();

		NavigableSet<String> stationNames = tsdb.streamStorage.getStationNames();	

		long timeStartImport = System.currentTimeMillis();
		try {
			TimeSeriesArchivWriter tsaWriter = new TimeSeriesArchivWriter(filename);
			tsaWriter.open();
			for(String stationName:stationNames) {
				try {
					String[] sensorNames = tsdb.streamStorage.getSensorNames(stationName);
					for(String sensorName:sensorNames) {
						StreamIterator it = tsdb.streamStorage.getRawSensorIterator(stationName, sensorName, null, null);
						if(it!=null&&it.hasNext()) {
							ArrayList<DataEntry> list = new ArrayList<DataEntry>();
							while(it.hasNext()) {
								list.add(it.next());
							}
							if(!list.isEmpty()) {
								DataEntry[] dataentries = list.toArray(new DataEntry[0]);
								DataEntries.serialize(dataentries, null);
							}
						}
					}
				} catch(Exception e) {
					Logger.error(e);
				}
			}
			tsaWriter.close();
		} catch (Exception e) {
			Logger.error(e);
		}
		long timeEndImport = System.currentTimeMillis();
		Logger.info((timeEndImport-timeStartImport)/1000+" s Export");
	}
}
