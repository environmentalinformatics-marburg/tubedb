package tsdb.testing;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.Point.Builder;
import org.influxdb.dto.Query;
import org.influxdb.dto.QueryResult;
import org.influxdb.dto.QueryResult.Result;
import org.influxdb.dto.QueryResult.Series;

public class TestingInflux {
	private static final Logger log = LogManager.getLogger();


	public static void main(String[] args) {
		InfluxDB influxDB = InfluxDBFactory.connect("http://127.0.0.1:8086", "user", "password");

		String dbName = "testing";
		influxDB.query(new Query("DROP DATABASE " + dbName));
		influxDB.query(new Query("CREATE DATABASE " + dbName));

		log.info("create batch");
		BatchPoints batchPoints = BatchPoints
				.database(dbName)
				.tag("async", "true")
				.build();
		for(int i=0; i<10000; i++) {
			Builder b = Point.measurement("cpu").time(i, TimeUnit.MINUTES);
			b.addField("a", 1f*i);
			b.addField("a", 1f*i);
			b.addField("b", 2f*i);
			b.addField("c", 3f*i);
			batchPoints.point(b.build());
		}

		log.info("write batch");
		influxDB.write(batchPoints);

		log.info("read");
		QueryResult result = influxDB.query(new Query("SELECT a,b,c FROM cpu", dbName));
		for(Result r:result.getResults()) {
			for(Series s:r.getSeries()) {
				for(List<Object> v:s.getValues()) {
					log.info(v);
				}
			}
		}

	}

}
