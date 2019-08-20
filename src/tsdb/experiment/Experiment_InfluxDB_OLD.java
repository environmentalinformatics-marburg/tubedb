package tsdb.experiment;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.ConsistencyLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;

import tsdb.util.DataRow;
import tsdb.util.TimeUtil;

public class Experiment_InfluxDB_OLD extends Experiment {	
	private static final Logger log = LogManager.getLogger();

	//00:00:00 Coordinated Universal Time (UTC), Thursday, 1 January 1970
	private static final long INFLUXDB_TIME_START_OLE_MINUTES = TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(1970,01,01,0,0));

	private InfluxDB influxDB;
	private static final String DB_NAME = "testing";
	//private static final String DB_RETENTION_POLICY ="autogen";
	private static final String DB_RETENTION_POLICY ="climatedata";

	public Experiment_InfluxDB_OLD() {	
		influxDB = createConnection();
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
		influxDB.query(new Query("DROP RETENTION POLICY " + DB_RETENTION_POLICY + " ON " + DB_NAME));
		influxDB.query(new Query("DROP DATABASE " + DB_NAME));
		influxDB.query(new Query("CREATE DATABASE " + DB_NAME));
		influxDB.query(new Query("CREATE RETENTION POLICY " + DB_RETENTION_POLICY + " ON " + DB_NAME + " DURATION INF REPLICATION 1 SHARD DURATION 52w DEFAULT"));
		influxDB.setRetentionPolicy(DB_RETENTION_POLICY);
	}

	protected void writePointsBatch(String stationName, String[] sensorNames, ArrayList<DataRow> dataRows) {
		boolean written = false;
		long tryCount = 0;
		int pointCount = 0;
		while(!written) {
			try {
				tryCount++;
				org.influxdb.dto.BatchPoints.Builder batchPointsBuilder = BatchPoints
						.database(DB_NAME)
						.precision(TimeUnit.MINUTES)
						.consistency(ConsistencyLevel.ONE)
						.retentionPolicy(DB_RETENTION_POLICY);
				int sensorNamesLen = sensorNames.length;
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
						batchPointsBuilder.point(b.build());
						pointCount++;
					}
				}
				if(pointCount > 0) {
					influxDB.write(batchPointsBuilder.build());
					
				}
				written = true;
			} catch (Exception e) {
				log.warn(stationName + " write failed, try again " + tryCount);
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {	
					log.warn(e1);
				}
			}
		}
	}

	@Override
	protected void insertTimeseries(String stationName, String[] sensorNames, ArrayList<DataRow> dataRows) {
		ArrayList<DataRow> points = new ArrayList<DataRow>();
		long batchNr = 0;
		long pointNr = 0;
		for(DataRow dataRow:dataRows) {
			points.add(dataRow);
			pointNr++;
			if(points.size() == 20_000) {
				batchNr++;
				log.info(stationName + " batch " + batchNr + "   p " + pointNr);
				writePointsBatch(stationName, sensorNames, points);
				points.clear();
			}
		}



		/*log.info("create batch");
		org.influxdb.dto.BatchPoints.Builder batchPointsBuilder = BatchPoints
				.database(DB_NAME)
				.precision(TimeUnit.MINUTES)
				.consistency(ConsistencyLevel.ONE)
				.retentionPolicy("autogen");
		int sensorNamesLen = sensorNames.length;




		BatchPoints batchPoints = batchPointsBuilder.build();
		long batchPointsCount = 0;
		long totalPointsCount = 0;
		int batchNr = 0;
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
				batchPoints.point(b.build());
				batchPointsCount++;
				totalPointsCount++;
				if(batchPointsCount >= 1_000) {
					log.info("write batch " + batchNr + "  " + totalPointsCount);

					boolean written = false;
					long tryCount = 0;
					while(!written) {
						try {
							influxDB.write(batchPoints);
							written = true;
						} catch (Exception e) {
							tryCount++;
							log.warn("write failed " + batchNr + "  " + totalPointsCount + ", try again"+ tryCount +": " + e);

							log.info("--- get measurements");
							Query query = new Query("SHOW MEASUREMENTS ON " + DB_NAME);	
							QueryResult queryResult = influxDB.query(query);
							List<Result> results = queryResult.getResults();
							for(Result result:results) {
								List<Series> seriess = result.getSeries();
								for(Series series:seriess) {
									List<List<Object>> values = series.getValues();
									for(List<Object> value:values) {
										for(Object v:value) {
											log.info(v);
										}
									}
								}
							}
							log.info("---");

							try {
								Thread.sleep(1000);
							} catch (InterruptedException e1) {	
								log.warn(e1);
							}

							influxDB.write(DB_NAME, "autogen", Point.measurement("check").time(1, TimeUnit.MINUTES).addField("try", tryCount).build());
						}
					}
					batchPoints = batchPointsBuilder.build();
					batchPointsCount = 0;
					batchNr++;
				}
			}
		}

		if(batchPointsCount > 0) {
			log.info("write final batch " + batchNr);
			boolean written = false;
			while(!written) {
				try {
					influxDB.writeWithRetry(batchPoints);
					written = true;
				} catch (Exception e) {
					log.warn("write failed, try again: " + e);
					try {
						Thread.sleep(100);
					} catch (InterruptedException e1) {	
						log.warn(e1);
					}
				}
			}
			batchPoints = batchPointsBuilder.build();
			batchPointsCount = 0;
			batchNr++;
		}*/
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
		//log.info(stationName);
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
		log.info(stationName + " " + Arrays.toString(sensorNames));
		String fields = String.join(",", sensorNames);
		String q = "SELECT "+ fields +" FROM " + stationName;
		log.info(q);
		Query query = new Query(q, DB_NAME);	
		QueryResult queryResult = influxDB.query(query);
		List<Result> results = queryResult.getResults();
		for(Result result:results) {
			List<Series> seriess = result.getSeries();
			for(Series series:seriess) {
				List<List<Object>> values = series.getValues();
				log.info("series " + values.size());
				for(List<Object> value:values) {
					//log.info("entry " + value.get(0));
					rowCount++;
					/*for(Object v:value) {
						log.info("v " + v);
					}*/
				}
			}
		}		
		return rowCount;		
	}
}
