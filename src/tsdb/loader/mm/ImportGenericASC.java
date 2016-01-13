package tsdb.loader.mm;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.loader.ki.AscParser;
import tsdb.util.AssumptionCheck;
import tsdb.util.DataEntry;
import tsdb.util.iterator.TimestampSeries;

/**
 * Load data files from Myanmar stations
 * @author woellauer
 *
 */
public class ImportGenericASC {
	private static final Logger log = LogManager.getLogger();

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
					//log.info("dir "+sub);
					load(sub);
				}

			}
		} catch (Exception e) {
			log.error(e);
		}
	}

	public void loadFiles(Path rootPath) {
		try(DirectoryStream<Path> rootStream = Files.newDirectoryStream(rootPath)) {
			for(Path sub:rootStream) {
				if(!Files.isDirectory(sub)) {
					//log.info("file "+sub);
					loadFile(sub);
				}

			}
		} catch (Exception e) {
			log.error(e);
		}		
	}

	public void loadFile(Path filePath) {
		try {
			if(filePath.toString().toLowerCase().endsWith(".bin")) { // skip file
				return;
			}
			log.info("load file "+filePath);
			TimestampSeries timestampseries = AscParser.parse(filePath,true);
			if(timestampseries==null) {
				log.error("timestampseries null  "+filePath);
				return;
			}
			Station station = tsdb.getStation(timestampseries.name);
			if(station==null) {
				log.error("station not found "+timestampseries.name+"   in "+filePath);
				return;
			}
			
			if(timestampseries.entryList.isEmpty()) {
				log.info("no entries in timeseries "+filePath);
				return;
			}

			for(String sensorName:timestampseries.sensorNames) {
				String targetName = station.translateInputSensorName(sensorName, false);
				if(targetName!=null) {
					DataEntry[] data = timestampseries.toDataEntyArray(sensorName);

					if(targetName.equals("P_RT_NRT")) {
						//log.info("P_RT_NRT corrected");
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
					log.warn("sensor not found   "+sensorName+"     at logger "+station.loggerType.typeName+"   station "+station.stationID+"    "+filePath);
				}
			}
		} catch (Exception e) {
			log.error(e+"   "+filePath);
		}
	}

}
