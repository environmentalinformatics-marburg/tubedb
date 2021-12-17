package tsdb.run;

import java.util.List;
import java.util.NavigableSet;


import org.tinylog.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;

import tsdb.TsDB;
import tsdb.TsDBFactory;

public class InfluxDBDataReader {
	

	private final TsDB tsdb;
	private final InfluxDB influxDB;
	
	private long total_count = 0;

	public static void main(String[] args) {		
		TsDB tsdb = TsDBFactory.createDefault();
		InfluxDB influxDB = InfluxDBFactory.connect(InfluxDBDataWriter.dbHost, "root", "root");
		InfluxDBDataReader influxDBDataReader = new InfluxDBDataReader(tsdb, influxDB);
		influxDBDataReader.readAll();
		tsdb.close();		
	}

	public InfluxDBDataReader(TsDB tsdb, InfluxDB influxDB) {
		this.tsdb = tsdb;
		this.influxDB = influxDB;

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
					e.printStackTrace();
					Logger.error(e);
				}
			}
		} catch (Exception e) {
			Logger.error(e);
		}
		long timeEndImport = System.currentTimeMillis();
		Logger.info((timeEndImport-timeStartImport)/1000+" s Export "+total_count+" count");
	}

	private void readSeries(String stationName, String sensorName) {
		//QueryResult queryResult = influxDB.query(new Query("select * from \""+stationName+'/'+sensorName+'\"',InfluxDBDataWriter.dbName));
		QueryResult queryResult = influxDB.query(new Query("SELECT * FROM \""+sensorName+"\" WHERE station='"+stationName+"'",InfluxDBDataWriter.dbName));
		List<Result> resultList = queryResult.getResults();
		for(Result result:resultList) {
			//Logger.info("result");
			List<Series> seriesList = result.getSeries();
			if(seriesList==null) {
				Logger.warn("no series "+stationName+" "+sensorName);
				continue;
			}
			for(Series series:seriesList) {
				Logger.info(stationName+"   series "+series.getName());
				//Logger.info("columns "+series.getColumns());
				//Logger.info("tags "+series.getTags());
				//Logger.info("values "+series.getValues());
				List<List<Object>> valueList = series.getValues();
				for(List<Object> value:valueList) {
					value.get(1);
					//System.out.println(value.get(1));
					total_count++;
				}
			}
		}
	}

}
