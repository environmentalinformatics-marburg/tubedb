package tsdb.run;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.NavigableSet;


import org.tinylog.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.streamdb.StreamIterator;
import tsdb.util.DataEntry;
import tsdb.util.TimeUtil;

public class DruidCsvWriter {
	

	private final TsDB tsdb;

	private BufferedWriter writer;

	public static void main(String[] args) throws IOException {
		TsDB tsdb = TsDBFactory.createDefault();
		DruidCsvWriter writer = new DruidCsvWriter(tsdb);			
		//influxDBDataWriter.writeAllStationsToFile("c:/temp/data.txt");		
		writer.writeAllStationsToDB();		
		tsdb.close();
	}

	public DruidCsvWriter(TsDB tsdb) throws IOException {
		this.tsdb = tsdb;
		Logger.info("start");
		writer = new BufferedWriter(new FileWriter("climate-sampled.csv"),1024*1024);

	}


	public void writeAllStationsToDB() throws IOException {
		try {


			NavigableSet<String> stationNames = tsdb.streamStorage.getStationNames();	

			long timeStartImport = System.currentTimeMillis();
			try {
				for(String stationName:stationNames) {
					try {
						String[] sensorNames = tsdb.streamStorage.getSensorNames(stationName);
						for(String sensorName:sensorNames) {
							StreamIterator it = tsdb.streamStorage.getRawSensorIterator(stationName, sensorName, null, null);
							if(it!=null) {
								while(it.hasNext()) {
									append(stationName, sensorName, it.next());									
								}
							}
						}
					} catch(Exception e) {
						e.printStackTrace();
						Logger.error(e);
					}
				}
			} catch (Exception e) {
				Logger.error(e);
			}
			long timeEndImport = System.currentTimeMillis();
			Logger.info((timeEndImport-timeStartImport)/1000+" s Export");
		} catch(Exception e) {
			Logger.error(e);
		} finally {
			writer.close();
		}

	}
	
	private void append(String stationName, String sensorName, DataEntry dataEntry) throws IOException {
		writer.append(TimeUtil.oleMinutesToLocalDateTime(dataEntry.timestamp).toString()+','+stationName+','+sensorName+','+dataEntry.value+'\n');
	}
}
