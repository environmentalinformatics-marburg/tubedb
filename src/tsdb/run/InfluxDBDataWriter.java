package tsdb.run;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.NavigableSet;
import java.util.concurrent.TimeUnit;


import org.tinylog.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.streamdb.StreamIterator;
import tsdb.util.DataEntry;
import tsdb.util.TimeUtil;

public class InfluxDBDataWriter {
	

	private final TsDB tsdb;

	public static String dbHost = "http://192.168.191.183:8086";
	//public static String dbHost = "http://localhost:8086/";


	public static String dbName = "testing_1a";

	private FileWriter writer = null;
	private InfluxDB influxDB = null;

	public static void main(String[] args) {



		TsDB tsdb = TsDBFactory.createDefault();
		InfluxDBDataWriter influxDBDataWriter = new InfluxDBDataWriter(tsdb);			
		//influxDBDataWriter.writeAllStationsToFile("c:/temp/data.txt");		
		influxDBDataWriter.writeAllStationsToDB(dbName);		
		tsdb.close();
	}

	public InfluxDBDataWriter(TsDB tsdb) {
		this.tsdb = tsdb;
	}


	public void writeAllStationsToDB(String dbName) {
		try {
			Logger.info("open connection to InfluxDB "+dbHost);
			influxDB = InfluxDBFactory.connect(dbHost, "root", "root");
			//int actions = 1000000;
			//int flushDuration = 1;
			//TimeUnit flushDurationTimeUnit = TimeUnit.MINUTES;
			//influxDB.enableBatch(actions, flushDuration, flushDurationTimeUnit);
			influxDB.deleteDatabase(dbName);
			//if(true)return;
			influxDB.createDatabase(dbName);

			NavigableSet<String> stationNames = tsdb.streamStorage.getStationNames();	

			long timeStartImport = System.currentTimeMillis();
			try {
				for(String stationName:stationNames) {
					try {
						String[] sensorNames = tsdb.streamStorage.getSensorNames(stationName);
						for(String sensorName:sensorNames) {
							StreamIterator it = tsdb.streamStorage.getRawSensorIterator(stationName, sensorName, null, null);
							if(it!=null&&it.hasNext()) {
								//Logger.info(it);
								boolean written = false;
								while(!written) {
									try {
										writeItDB(dbName, stationName, sensorName, it);
										written = true;
									} catch(Exception e) {
										Logger.warn("retry "+stationName+'/'+sensorName+": "+e.getMessage());
									}
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
			if(writer!=null) {
				try {
					writer.close();
				} catch (IOException e) {
					Logger.error(e);
				}
			}
		}

	}

	public void writeAllStationsToFile(String filename) {
		try {
			writer = new FileWriter(filename);

			NavigableSet<String> stationNames = tsdb.streamStorage.getStationNames();	

			long timeStartImport = System.currentTimeMillis();
			try {
				for(String stationName:stationNames) {
					try {
						String[] sensorNames = tsdb.streamStorage.getSensorNames(stationName);
						for(String sensorName:sensorNames) {
							StreamIterator it = tsdb.streamStorage.getRawSensorIterator(stationName, sensorName, null, null);
							if(it!=null&&it.hasNext()) {
								//Logger.info(it);
								writeItWriter(stationName, sensorName, it);
							}
						}
					} catch(Exception e) {
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
			if(writer!=null) {
				try {
					writer.close();
				} catch (IOException e) {
					Logger.error(e);
				}
			}
		}

	}

	long t = 0;

	//00:00:00 Coordinated Universal Time (UTC), Thursday, 1 January 1970
	public static final long INFLUXDB_TIME_START_OLE_MINUTES = TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(1970,01,01,0,0));

	public void writeItDB(String dbName, String stationName, String sensorName, StreamIterator it) throws IOException {		
		BatchPoints batchPoints = BatchPoints
				.database(dbName)
				.retentionPolicy("autogen")
				.consistency(ConsistencyLevel.ONE)
				.build();

		while(it.hasNext()) {			
			DataEntry e = it.next();
			long t = ((long)e.timestamp)-INFLUXDB_TIME_START_OLE_MINUTES;
			//System.out.println(t);
			//Point point = Point.measurement(it.stationName+"/"+it.sensorName).time(t, TimeUnit.MINUTES).addField("value", e.value).build();
			//Point point = Point.measurement(it.stationName).time(t, TimeUnit.MINUTES).tag("sensor", it.sensorName).addField("value", e.value).build();
			Point point = Point.measurement(it.sensorName).time(t, TimeUnit.MINUTES).tag("station", it.stationName).addField("value", e.value).build();
			batchPoints.point(point);
		}

		influxDB.write(batchPoints);

		/*while(it.hasNext()) {			
			DataEntry e = it.next();			
			Point point = Point.measurement(it.stationName).field(it.sensorName, e.value).build();
			influxDB.write(dbName, "default", point);
		}*/

		Logger.info("write "+it.stationName+" "+it.sensorName);

	}

	public void writeItWriter(String stationName, String sensorName, StreamIterator it) throws IOException {
		while(it.hasNext()) {
			DataEntry e = it.next();
			writer.write(stationName+",sensor="+sensorName+" value="+e.value+" "+e.timestamp+'\n');
		}
	}


}
