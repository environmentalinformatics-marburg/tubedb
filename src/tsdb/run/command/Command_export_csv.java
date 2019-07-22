package tsdb.run.command;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.NavigableSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.util.Timer;
import tsdb.util.iterator.TsIterator;

public class Command_export_csv {
	private static final Logger log = LogManager.getLogger();

	private final TsDB tsdb;

	public static void main(String[] args) {
		String path = args[0];
		try(TsDB tsdb = TsDBFactory.createDefault()) {				
			Command_export_csv export_region = new Command_export_csv(tsdb);
			export_region.run(path);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
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
				log.info(stationName);
				try {
					String[] sensorNames = tsdb.streamStorage.getSensorNames(stationName);
					if(sensorNames != null) {
						log.info(stationName + "/" + Arrays.toString(sensorNames));
						TsIterator it = tsdb.streamStorage.getRawIterator(stationName, sensorNames, null, null);
						it.writeCSV(path + "/" + stationName + ".csv");
					}
				} catch(Exception e) {
					log.error(e);
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
		log.info(Timer.stop("csv export"));
	}
}
