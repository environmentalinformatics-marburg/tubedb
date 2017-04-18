package tsdb.run.command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;

public class ClearMasks {
	private static final Logger log = LogManager.getLogger();

	private final TsDB tsdb;

	public static void main(String[] args) {
		try(TsDB tsdb = TsDBFactory.createDefault()) {
			ClearMasks clearMasks = new ClearMasks(tsdb);
			clearMasks.run(tsdb.configDirectory);
		} catch (Exception e) {
			log.error(e);
		}		
	}

	public ClearMasks(TsDB tsdb) {
		this.tsdb = tsdb;
	}	

	public void run(String configDirectory) {		
		try {		
			for(String stationName:tsdb.streamStorage.getStationNames()) {
				tsdb.streamStorage.clearMaskOfStation(stationName);
			}		
		} catch(Exception e) {
			log.error(e);
		}
	}
}
