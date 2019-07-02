package tsdb.run;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;

import okhttp3.OkHttpClient;
import okhttp3.OkHttpClient.Builder;
import tsdb.TsDB;
import tsdb.TsDBFactory;
/*
 * 
 * SELECT mean("value") FROM "Ta_200" WHERE station='AEG01' AND time >= '2010-01-01T18:15:00Z' AND time <= '2014-12-31T23:59:59Z' GROUP BY time(1d)

SELECT MEAN(*) FROM "Ta_200" GROUP BY station
 * 
 * 
 */
public class InfluxDBMeanReader {
	private static final Logger log = LogManager.getLogger();

	//private final TsDB tsdb;
	private final InfluxDB influxDB;
	
	private long total_count = 0;

	public static void main(String[] args) {		
		TsDB tsdb = TsDBFactory.createDefault();
		Builder okHttpClientBuilder = new OkHttpClient.Builder().readTimeout(10, TimeUnit.MINUTES);
		InfluxDB influxDB = InfluxDBFactory.connect(InfluxDBDataWriter.dbHost, "root", "root", okHttpClientBuilder);
		InfluxDBMeanReader influxDBDataReader = new InfluxDBMeanReader(tsdb, influxDB);
		influxDBDataReader.readAll();
		tsdb.close();		
	}

	public InfluxDBMeanReader(TsDB tsdb, InfluxDB influxDB) {
		//this.tsdb = tsdb;
		this.influxDB = influxDB;

	}


	public void readAll() {		
		//NavigableSet<String> stationNames = tsdb.streamStorage.getStationNames();	

		long timeStartImport = System.currentTimeMillis();
		try {
			readSeries(null,"Ta_200");
			/*for(String stationName:stationNames) {
				try {
						readSeries(stationName,"Ta_200");
				} catch(Exception e) {
					e.printStackTrace();
					log.error(e);
				}
			}*/
		} catch (Exception e) {
			log.error(e);
		}
		long timeEndImport = System.currentTimeMillis();
		log.info((timeEndImport-timeStartImport)/1000+" s Export "+total_count+" count");
	}

	private void readSeries(String stationName, String sensorName) {
		//QueryResult queryResult = influxDB.query(new Query("select * from \""+stationName+'/'+sensorName+'\"',InfluxDBDataWriter.dbName));
	    //QueryResult queryResult = influxDB.query(new Query("SELECT mean(\"value\") FROM \""+sensorName+"\" WHERE station='"+stationName+"'",InfluxDBDataWriter.dbName));
	    QueryResult queryResult = influxDB.query(new Query("SELECT MEAN(*) FROM \""+sensorName+"\" GROUP BY station",InfluxDBDataWriter.dbName));
		List<Result> resultList = queryResult.getResults();
		log.info("results "+resultList.size());
		for(Result result:resultList) {
			//log.info("result");
			List<Series> seriesList = result.getSeries();
			if(seriesList==null) {
				log.warn("no series "+stationName+" "+sensorName);
				continue;
			}
			for(Series series:seriesList) {
				//log.info("series "+series.getName());
				//log.info("columns "+series.getColumns());
				//log.info("tags "+series.getTags());
				//log.info("values "+series.getValues());
				List<List<Object>> valueList = series.getValues();
				for(List<Object> value:valueList) {
					Object v = value.get(1);
					//System.out.println(value.get(1));
					total_count++;
					log.info(stationName+" "+sensorName+" avg "+v);
				}
			}
		}
	}

}
