package tsdb.loader.influx;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


import org.tinylog.Logger;

import ch.randelshofer.fastdoubleparser.JavaFloatParser;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;

import tsdb.TsDB;
import tsdb.component.SourceEntry;
import tsdb.loader.influx.InfluxLoaderConfig.Sensor;
import tsdb.util.AssumptionCheck;
import tsdb.util.DataEntry;
import tsdb.util.TimeUtil;

public class InfluxLoader {
	

	private final TsDB tsdb;

	public InfluxLoader(TsDB tsdb) {
		Logger.info("ImportInflux");
		AssumptionCheck.throwNull(tsdb);
		this.tsdb = tsdb;
	}

	public void load(InfluxLoaderConfig config) {
		InfluxDB influxDB = InfluxDBFactory.connect(config.url, config.user, config.password);

		{
			Logger.info("--- get measurements");
			Query query = new Query("SHOW MEASUREMENTS ON " + config.database);	
			QueryResult queryResult = influxDB.query(query);
			List<Result> results = queryResult.getResults();
			for(Result result:results) {
				List<Series> seriess = result.getSeries();
				for(Series series:seriess) {
					List<List<Object>> values = series.getValues();
					for(List<Object> value:values) {
						for(Object v:value) {
							Logger.info(v);
						}
					}
				}
			}
			Logger.info("---");
		}

		for(Sensor sensor:config.sensors) {
			ArrayList<String> stations = new ArrayList<String>();
			{
				{
					Logger.info("--- get stations of " + sensor.loggerName);
					Query query = new Query("SHOW TAG VALUES FROM \"" + sensor.loggerName + "\" WITH KEY = id", config.database);	
					QueryResult queryResult = influxDB.query(query);
					List<Result> results = queryResult.getResults();
					for(Result result:results) {
						List<Series> seriess = result.getSeries();
						for(Series series:seriess) {
							List<List<Object>> values = series.getValues();
							for(List<Object> value:values) {
								stations.add(value.get(1).toString());
							}
						}
					}
					Logger.info("---  " + stations);
				}
			}

			for(String stationName:stations){
				Logger.info("--- get station " + stationName);
				ArrayList<DataEntry> dataEntryList = new ArrayList<DataEntry>();
				int prevTimestamp = -1;
				String q = "SELECT \"" + sensor.srcName + "\" FROM \"" + sensor.loggerName + "\" WHERE id='" + stationName + "'";
				//Logger.info(q);
				Query query = new Query(q, config.database);	
				QueryResult queryResult = influxDB.query(query);
				List<Result> results = queryResult.getResults();
				for(Result result:results) {
					List<Series> seriess = result.getSeries();
					if(seriess != null) {
						for(Series series:seriess) {
							//Logger.info("series " + series.getName() + "   " + series.getColumns());
							List<List<Object>> values = series.getValues();



							for(List<Object> value:values) {

								String timestampText = ((String) value.get(0)).substring(0, 16);
								LocalDateTime datetime = LocalDateTime.parse(timestampText);
								int timestamp = (int) TimeUtil.dateTimeToOleMinutes(datetime);
								if(timestamp==prevTimestamp) {
									//Logger.warn("skip duplicate timestamp ");
									continue;
								}
								Object vObject = value.get(1);
								float m;
								if(vObject instanceof Double) {
									m = ((Double) vObject).floatValue();
								} else {
									//m = Float.parseFloat(vObject.toString());
									m = JavaFloatParser.parseFloat(vObject.toString());
								}
								dataEntryList.add(new DataEntry(timestamp, m));
								prevTimestamp = timestamp;
							}
						}
					}
				}
				if(!dataEntryList.isEmpty()) {
					Logger.info("insert " + stationName + " " +  sensor.loggerName + " " + sensor.srcName + " -> "  + sensor.dstName + " "  + dataEntryList.size());
					if(tsdb.getStation(stationName) == null) {
						Logger.warn("station not found: " + stationName);
					}
					tsdb.streamStorage.insertDataEntryArray(stationName, sensor.dstName, dataEntryList.toArray(new DataEntry[0]));
					Path path = Paths.get("influx", stationName + "__" + sensor.loggerName + "__" + sensor.dstName);
					tsdb.sourceCatalog.insert(SourceEntry.ofDataEntry(stationName, sensor.srcName, sensor.dstName, dataEntryList, path));
				}
			}
		}		
	}

}
