package tsdb.loader.mm;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


import org.tinylog.Logger;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.component.SourceEntry;
import tsdb.loader.ki.AscParser;
import tsdb.util.AssumptionCheck;
import tsdb.util.DataEntry;
import tsdb.util.TsSchema;
import tsdb.util.iterator.TimestampSeries;

/**
 * Load data files from Myanmar stations
 * @author woellauer
 *
 */
public class ImportGenericASC {
	

	private final TsDB tsdb;

	public ImportGenericASC(TsDB tsdb) {
		AssumptionCheck.throwNull(tsdb);
		this.tsdb = tsdb;
	}

	public void load(String rootPath) {
		load(Paths.get(rootPath));
	}


	public void load(Path rootPath) {
		loadFiles(rootPath);
		loadSubDirs(rootPath);
	}

	public void loadSubDirs(Path rootPath) {
		try(DirectoryStream<Path> rootStream = Files.newDirectoryStream(rootPath)) {
			for(Path sub:rootStream) {
				if(Files.isDirectory(sub)) {
					//Logger.info("dir "+sub);
					load(sub);
				}

			}
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	public void loadFiles(Path rootPath) {
		try(DirectoryStream<Path> rootStream = Files.newDirectoryStream(rootPath)) {
			for(Path sub:rootStream) {
				if(!Files.isDirectory(sub)) {
					//Logger.info("file "+sub);
					loadFile(sub);
				}

			}
		} catch (Exception e) {
			Logger.error(e);
		}		
	}

	public void loadFile(Path filePath) {
		try {
			if(filePath.toString().toLowerCase().endsWith(".bin")) { // skip file
				return;
			}
			Logger.trace("load file "+filePath);
			TimestampSeries timestampseries = AscParser.parse(filePath,true);
			if(timestampseries==null) {
				Logger.error("timestampseries null  "+filePath);
				return;
			}
			Station station = tsdb.getStation(timestampseries.name);
			if(station==null) {
				Logger.error("station not found "+timestampseries.name+"   in "+filePath);
				return;
			}

			if(timestampseries.entryList.isEmpty()) {
				Logger.info("no entries in timeseries "+filePath);
				return;
			}

			String[] sensorNames = new String[timestampseries.sensorNames.length];

			for (int s = 0; s < sensorNames.length; s++) {
				String targetName = station.translateInputSensorName(timestampseries.sensorNames[s], false);
				sensorNames[s] = targetName;
				if(targetName!=null) {
					DataEntry[] data = timestampseries.toDataEntyArray(timestampseries.sensorNames[s]);

					if(targetName.equals("P_RT_NRT")) {
						//Logger.info("P_RT_NRT corrected");
						DataEntry[] corrected_data =  new DataEntry[data.length];
						for(int i=0;i<data.length;i++) {
							corrected_data[i] = new DataEntry(data[i].timestamp,data[i].value*0.2f);
						}
						data = corrected_data;
					}						
					if(data!=null&&data.length>0) {
						tsdb.streamStorage.insertDataEntryArray(timestampseries.name, targetName, data);
					}
				} else {
					Logger.warn("sensor not found   "+timestampseries.sensorNames[s]+"     at logger "+station.loggerType.typeName+"   station "+station.stationID+"    "+filePath);
				}
			}

			SourceEntry sourceEntry = new SourceEntry(filePath, timestampseries.name, timestampseries.getFirstTimestamp(), timestampseries.getLastTimestamp(), timestampseries.size(), timestampseries.sensorNames, sensorNames, TsSchema.NO_CONSTANT_TIMESTEP);
			tsdb.sourceCatalog.insert(sourceEntry);

		} catch (Exception e) {
			Logger.error(e+"   "+filePath);
		}
	}

}
