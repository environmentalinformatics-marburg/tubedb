package tsdb.run.command;


import org.tinylog.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;

public class ClearMasks {
	

	private final TsDB tsdb;

	public static void main(String[] args) {
		try(TsDB tsdb = TsDBFactory.createDefault()) {
			ClearMasks clearMasks = new ClearMasks(tsdb);
			clearMasks.run(tsdb.configDirectory);
		} catch (Exception e) {
			Logger.error(e);
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
			Logger.error(e);
		}
	}
}
