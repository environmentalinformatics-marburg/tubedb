package tsdb.usecase;

import java.util.DoubleSummaryStatistics;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.streamdb.StreamStorageStreamDB;
import tsdb.util.DataEntry;

public class SingleInsertPerformance {
	private static final Logger log = LogManager.getLogger();
	
	final static int rounds = 1000;
	final static int stations = 100;
	final static int sensors = 10;
	
	static float[] data = new float[rounds*stations*sensors];

	public static void main(String[] args) {
		
		DoubleSummaryStatistics dss = new DoubleSummaryStatistics();


		for(int i=0;i<data.length;i++) {
			data[i] = ((int) (Math.random()*1000d)/10f);
			//System.out.println(data[i]);
		}

		long timeStart = System.currentTimeMillis();
		
		//long element_count = run_tsdb();
		long element_count = run_influxDB();
		

		long timeEnd = System.currentTimeMillis();
		

		log.info(msToText(timeStart,timeEnd)+" insert "+element_count);
	}
	
	private static long run_tsdb() {
		TsDB tsdb = TsDBFactory.createDefault();
		StreamStorageStreamDB streamStorage = tsdb.streamStorage;
		streamStorage.setLogging(false);


		int timestamp=0;
		long element_count = 0;


		for(int round=0;round<rounds;round++) {
			if(round%100==0) {
				log.info("round "+round);
			}
			for(int station=0;station<stations;station++) {
				int t = timestamp++;
				for(int sensor=0;sensor<sensors;sensor++) {
					String stationName = "station"+station;
					String sensorName = "sensor"+sensor;
					DataEntry[] dataEntries = new DataEntry[]{ new DataEntry(t, data[t])};
					streamStorage.insertDataEntryArray(stationName, sensorName, dataEntries);
					element_count++;
				}
			}

		}

		tsdb.close();
		
		return element_count;

	}
	
	private static long run_influxDB() {
		InfluxDB influxDB = InfluxDBFactory.connect("http://localhost:8086", "root", "root");
		String dbname = "t1";

		int timestamp=0;
		long element_count = 0;


		for(int round=0;round<rounds;round++) {
			BatchPoints batchPoints = BatchPoints
					.database(dbname)
					//.tag("async", "true")
					//.retentionPolicy("default")
					//.consistency(ConsistencyLevel.ALL)
					.build();
			if(round%100==0) {
				log.info("round "+round);
			}
			for(int station=0;station<stations;station++) {
				int t = timestamp++;
				for(int sensor=0;sensor<sensors;sensor++) {
					String stationName = "station"+station;
					String sensorName = "sensor"+sensor;					
					Point point = Point.measurement(stationName+"/"+sensorName).time(t, TimeUnit.MINUTES).field("value", data[t]).build();
					//influxDB.write(dbname,"default",point);
					batchPoints.point(point);
					element_count++;
				}
			}
			influxDB.write(batchPoints);
		}
		
		return element_count;

	}
	

	private static String msToText(long start, long end) {
		long diff = end-start;
		long h = diff%1000/100;
		long z = diff%100/10;
		long e = diff%10;
		return diff/1000+"."+h+z+e+" s";
	}

}
