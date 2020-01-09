package tsdb.run;

import java.util.ArrayList;
import java.util.NavigableSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.streamdb.StreamIterator;
import tsdb.streamdb.StreamStorageStreamDB;
import tsdb.util.DataEntry;

public class StreamDBDataWriter {
	private static final Logger log = LogManager.getLogger();

	private final TsDB tsdb;

	private final StreamStorageStreamDB target;

	public static void main(String[] args) {
		TsDB tsdb = TsDBFactory.createDefault();
		StreamDBDataWriter writer = new StreamDBDataWriter(tsdb);			
		//influxDBDataWriter.writeAllStationsToFile("c:/temp/data.txt");		
		writer.writeAllStationsToDB();		
		tsdb.close();
	}

	public StreamDBDataWriter(TsDB tsdb) {
		this.tsdb = tsdb;
		String streamdbPathPrefix = "performance_testing"+"/streamdb";
		this.target = new StreamStorageStreamDB(streamdbPathPrefix);
	}


	public void writeAllStationsToDB() {
		try {


			NavigableSet<String> stationNames = tsdb.streamStorage.getStationNames();	

			long timeStartImport = System.currentTimeMillis();
			try {
				ArrayList<DataEntry> collector = new ArrayList<DataEntry>();
				for(String stationName:stationNames) {
					try {
						String[] sensorNames = tsdb.streamStorage.getSensorNames(stationName);
						for(String sensorName:sensorNames) {
							StreamIterator it = tsdb.streamStorage.getRawSensorIterator(stationName, sensorName, null, null);
							if(it!=null&&it.hasNext()) {
								collector.clear();
								while(it.hasNext()) {
									collector.add(it.next());									
								}
								target.insertDataEntryArray(stationName, sensorName, collector.toArray(new DataEntry[0]));
							}
						}
					} catch(Exception e) {
						e.printStackTrace();
						log.error(e);
					}
				}
			} catch (Exception e) {
				log.error(e);
			}
			long timeEndImport = System.currentTimeMillis();
			log.info((timeEndImport-timeStartImport)/1000+" s Export");
		} catch(Exception e) {
			log.error(e);
		} finally {
			target.close();
		}

	}
	
}
