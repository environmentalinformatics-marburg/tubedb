package tsdb.run.command;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.NavigableSet;


import org.tinylog.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.util.AggregationInterval;
import tsdb.util.Timer;
import tsdb.util.iterator.CSV;
import tsdb.util.iterator.CSVTimeType;
import tsdb.util.iterator.TsIterator;

public class Command_export_csv {
	

	private final TsDB tsdb;

	public static void main(String[] args) {
		String path = args[0];
		try(TsDB tsdb = TsDBFactory.createDefault()) {				
			Command_export_csv export_region = new Command_export_csv(tsdb);
			export_region.run(path);
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error(e);
		}			
	}

	public Command_export_csv(TsDB tsdb) {
		this.tsdb = tsdb;
	}	

	public void run(String path) throws IOException {
		Paths.get(path).toFile().mkdirs();
		NavigableSet<String> stationNames = tsdb.streamStorage.getStationNames();	

		Timer.start("csv export");
		try {
			for(String stationName:stationNames) {
				Logger.info(stationName);
				try {
					String[] sensorNames = tsdb.streamStorage.getSensorNames(stationName);
					if(sensorNames != null) {
						Logger.info(stationName + "/" + Arrays.toString(sensorNames));
						TsIterator it = tsdb.streamStorage.getRawIterator(stationName, sensorNames, null, null);
						CSV.write(it, path + "/" + stationName + ".csv", ',', "", CSVTimeType.DATETIME, AggregationInterval.RAW);
					}
				} catch(Exception e) {
					Logger.error(e);
				}
			}
		} catch (Exception e) {
			Logger.error(e);
		}
		Logger.info(Timer.stop("csv export"));
	}
}
