package tsdb.experiment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;


import org.tinylog.Logger;
import org.influxdb.BatchOptions;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;

import tsdb.util.DataRow;
import tsdb.util.TimeUtil;

public class Experiment_InfluxDB extends Experiment {	
	

	//00:00:00 Coordinated Universal Time (UTC), Thursday, 1 January 1970
	private static final long INFLUXDB_TIME_START_OLE_MINUTES = TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(1970,01,01,0,0));

	private InfluxDB influxDB;
	private static final String DB_NAME = "testing";

	public Experiment_InfluxDB() {	
		influxDB = createConnection();
		BiConsumer<Iterable<Point>, Throwable> exceptionHandler = (points, throwable) -> {
			Logger.error("error in write batch: " + points.toString() + "  " + throwable);
		 };
		BatchOptions b = BatchOptions.DEFAULTS
				.actions(1_000)
				.bufferLimit(10_000)
				.consistency(ConsistencyLevel.ONE)
				.exceptionHandler(exceptionHandler)
				.flushDuration(10_000)
				.jitterDuration(10_000)
				.precision(TimeUnit.MINUTES)
				.threadFactory(Executors.defaultThreadFactory());
		influxDB.enableBatch(b);
	}
	
	InfluxDB createConnection() {
		okhttp3.OkHttpClient.Builder connectionBuilder = new okhttp3.OkHttpClient.Builder();
		connectionBuilder.callTimeout(1, TimeUnit.HOURS);
		connectionBuilder.connectTimeout(1, TimeUnit.HOURS);
		connectionBuilder.readTimeout(1, TimeUnit.HOURS);
		connectionBuilder.writeTimeout(1, TimeUnit.HOURS);
		return InfluxDBFactory.connect("http://127.0.0.1:8086", "user", "password", connectionBuilder);
	}

	@Override
	protected void clear() {
		influxDB.query(new Query("DROP DATABASE " + DB_NAME));
		influxDB.query(new Query("CREATE DATABASE " + DB_NAME));
	}

	@Override
	protected void insertTimeseries(String stationName, String[] sensorNames, ArrayList<DataRow> dataRows) {
		int sensorNamesLen = sensorNames.length;
		long batchPointCount = 0;
		for(DataRow dataRow:dataRows) {
			long t = ((long) dataRow.timestamp) - INFLUXDB_TIME_START_OLE_MINUTES;
			Builder b = Point.measurement(stationName).time(t, TimeUnit.MINUTES);
			for (int i = 0; i < sensorNamesLen; i++) {
				float v = dataRow.data[i];
				if(Float.isFinite(v)) {
					b.addField(sensorNames[i], v);
				}
			}
			if(b.hasFields()) {				
				influxDB.write(b.build());
				batchPointCount++;
				if(batchPointCount >= 1_00) {
					Logger.info("flush");
					influxDB.flush();
					batchPointCount = 0;
				}
			}
		}		
		influxDB.flush();
		batchPointCount = 0;
	}

	@Override
	public void close() throws Exception {
		influxDB.close();
	}

	@Override
	protected long full_read() {
		long rowCount = 0;
		Query query = new Query("SHOW MEASUREMENTS ON " + DB_NAME);	
		QueryResult queryResult = influxDB.query(query);
		List<Result> results = queryResult.getResults();
		for(Result result:results) {
			List<Series> seriess = result.getSeries();
			for(Series series:seriess) {
				List<List<Object>> values = series.getValues();
				for(List<Object> value:values) {
					for(Object v:value) {
						rowCount += full_read_station(v.toString());
					}
				}
			}
		}
		return rowCount;
	}

	private long full_read_station(String stationName) {
		//Logger.info(stationName);
		long rowCount = 0;
		Query query = new Query("SHOW FIELD KEYS ON " + DB_NAME + " FROM " + stationName);	
		QueryResult queryResult = influxDB.query(query);
		List<Result> results = queryResult.getResults();
		for(Result result:results) {
			List<Series> seriess = result.getSeries();
			for(Series series:seriess) {
				List<List<Object>> values = series.getValues();
				String[] sensorNames = values.stream().map(v -> v.get(0).toString()).toArray(String[]::new);
				rowCount += full_read_station_data(stationName, sensorNames);
			}
		}


		return rowCount;		
	}

	private long full_read_station_data(String stationName, String[] sensorNames) {
		long rowCount = 0;
		Logger.info(stationName + " " + Arrays.toString(sensorNames));
		String fields = String.join(",", sensorNames);
		String q = "SELECT "+ fields +" FROM " + stationName;
		Logger.info(q);
		Query query = new Query(q, DB_NAME);	
		QueryResult queryResult = influxDB.query(query);
		List<Result> results = queryResult.getResults();
		for(Result result:results) {
			List<Series> seriess = result.getSeries();
			for(Series series:seriess) {
				List<List<Object>> values = series.getValues();
				Logger.info("series " + values.size());
				for(List<Object> value:values) {
					//Logger.info("entry " + value.get(0));
					rowCount++;
					/*for(Object v:value) {
						Logger.info("v " + v);
					}*/
				}
			}
		}		
		return rowCount;		
	}
}
