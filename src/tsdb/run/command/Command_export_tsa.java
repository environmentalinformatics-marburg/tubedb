package tsdb.run.command;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.component.Region;
import tsdb.util.TimeSeriesArchivWriter;
import tsdb.util.iterator.TimestampSeries;
import tsdb.util.iterator.TsIterator;

public class Command_export_tsa {
	private static final Logger log = LogManager.getLogger();

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
					log.info("region not found: "+regionName);
					return;
				}
				Command_export_tsa export_region = new Command_export_tsa(tsdb);
				export_region.run(filename, region);
			} catch (Exception e) {
				e.printStackTrace();
				log.error(e);
			}
			break;
		}
		default:
			log.info("export_tsa needs 1 or 2 parameters: output filename and optional region name");
			return;
		}
		
		
		
		if(args.length != 1) {
			
		}
		
		
		/*if(args.length != 2) {
			log.info("syntax error");
			return;
		}
		try(TsDB tsdb = TsDBFactory.createDefault()) {
			String regionName = "BEf";
			Region region = tsdb.getRegion(regionName);
			if(region == null) {
				log.info("region not found: "+regionName);
				return;
			}
			String filename = "out.tsa";
			Command_export_tsa export_region = new Command_export_tsa(tsdb);
			export_region.run(region, filename);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}*/
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
						TsIterator it = tsdb.streamStorage.getRawIterator(s.stationID, sensorNames, null, null);
						TimestampSeries timestampSeries = it.toTimestampSeries(s.stationID);
						tsaWriter.writeTimestampSeries(timestampSeries);
					} else {
						//log.info("no sensors in " + s.stationID);
					}
				} catch(Exception e) {
					throw new RuntimeException(e);
				}
			}
		});

		tsaWriter.close();
	}
}
