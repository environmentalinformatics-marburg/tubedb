package tsdb.run.command;

import java.io.IOException;


import org.tinylog.Logger;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.component.Region;
import tsdb.streamdb.StreamIterator;
import tsdb.util.DataEntry;
import tsdb.util.TimeSeriesArchivWriter;

public class Command_export_tsa {
	

	private final TsDB tsdb;

	public static void main(String[] args) {
		switch(args.length) {
		case 1: {
			String filename = args[0];
			TimeSeriesArchivWriter.writeAllStationsToFile(filename);
			break;
		}
		case 2: {
			String filename = args[0];
			String regionName = args[1];
			try(TsDB tsdb = TsDBFactory.createDefault()) {				
				Region region = tsdb.getRegion(regionName);
				if(region == null) {
					Logger.info("region not found: "+regionName);
					return;
				}
				Command_export_tsa export_region = new Command_export_tsa(tsdb);
				export_region.run(filename, region);
			} catch (Exception e) {
				e.printStackTrace();
				Logger.error(e);
			}
			break;
		}
		default:
			Logger.info("export_tsa needs 1 or 2 parameters: output filename and optional region name");
			return;
		}
	}

	public Command_export_tsa(TsDB tsdb) {
		this.tsdb = tsdb;
	}	

	public void run(String filename, Region region) throws IOException {
		TimeSeriesArchivWriter tsaWriter = new TimeSeriesArchivWriter(filename);
		tsaWriter.open();
		
		tsdb.getGeneralStationsByRegion(region.name).forEach(g->{
			for(Station s:g.stationList) {
				try {
					String[] sensorNames = tsdb.streamStorage.getSensorNames(s.stationID);
					if(sensorNames != null && sensorNames.length > 0) {
						/*TsIterator it = tsdb.streamStorage.getRawIterator(s.stationID, sensorNames, null, null);
						TimestampSeries timestampSeries = it.toTimestampSeries(s.stationID);
						tsaWriter.writeTimestampSeries(timestampSeries);*/
						for(String sensorName : sensorNames) {
							StreamIterator it = tsdb.streamStorage.getRawSensorIterator(s.stationID, sensorName, null, null);
							if(it != null && it.hasNext()) {
								DataEntry[] dataEntries = it.remainingToArray();
								tsaWriter.writeDataEntryArray(s.stationID, sensorName, dataEntries);
							} else {
								Logger.warn("empty sensor " + s.stationID + " " + sensorName);
							}
						}
					} else {
						//Logger.info("no sensors in " + s.stationID);
					}
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			}
		});

		tsaWriter.close();
	}
}
