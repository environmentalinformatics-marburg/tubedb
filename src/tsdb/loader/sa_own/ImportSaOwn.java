package tsdb.loader.sa_own;

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
 * Load data files from Southafrica own stations
 * @author woellauer
 *
 */
public class ImportSaOwn {
	private static final Logger log = LogManager.getLogger();

	private final TsDB tsdb;

	/*public static void main(String[] args) {
		try(TsDB tsdb = TsDBFactory.createDefault()) {		
			Path rootPath = Paths.get("C:/timeseriesdatabase_source/sa_own");			
			ImportSaOwn importSaOwn = new ImportSaOwn(tsdb);			
			importSaOwn.load(rootPath);
		} catch(Exception e) {
			log.error(e);
		}
	}*/

	public ImportSaOwn(TsDB tsdb) {
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
			log.info("load file "+filePath);
			TimestampSeries timestampseries = AscParser.parse(filePath);
			if(timestampseries==null) {
				log.error("timestampseries null");
				return;
			}
			Station station = tsdb.getStation(timestampseries.name);
			if(station==null) {
				log.error("station not found "+timestampseries.name);
				return;
			}
			
			//log.info(timestampseries);

			/*HashMap<String,String> translationMap = new HashMap<String, String>();
			translationMap.put("Temperature", "Ta_200");			
			translationMap.put("rel.Humidity", "rH_200");
			translationMap.put("DecagonECH2O", "DecagonECH2O");
			translationMap.put("Impulses", "P_RT_NRT");

			translationMap.put("Temperatur", "Ta_200");
			translationMap.put("rel.Feuchte", "rH_200");
			translationMap.put("Impulse", "P_RT_NRT");*/
			
			/*LoggerType loggerType = tsdb.getLoggerType("thdi");
			if(loggerType==null) {
				throw new RuntimeException("loggertype not found: "+"thdi");
			}
			Map<String, String> translationMap = loggerType.sensorNameTranlationMap;*/
			
			for(String sensorName:timestampseries.sensorNames) {
				String targetName = station.translateInputSensorName(sensorName, false);
				if(targetName!=null) {
					//String targetName = translationMap.get(sensorName);
					if(targetName!=null) {
						DataEntry[] data = timestampseries.toDataEntyArray(sensorName);
						
						if(targetName.equals("P_RT_NRT")) {
							log.info("P_RT_NRT corrected");
							DataEntry[] corrected_data =  new DataEntry[data.length];
							for(int i=0;i<data.length;i++) {
								corrected_data[i] = new DataEntry(data[i].timestamp,data[i].value*0.2f);
							}
							data = corrected_data;
						}						
						if(data!=null&&data.length>0) {
							//System.out.println("insert in station "+stationName+" sensor "+sensorName+"  elements "+data.length);
							//streamdb.insertSensorData(stationName, sensorName, data);
							//log.info("insert in station "+timestampseries.name+" sensor "+targetName+"  elements "+data.length);
							tsdb.streamStorage.insertDataEntryArray(timestampseries.name, targetName, data);
							
							if(targetName.equals("DecagonECH2O")) {
								log.info("DecagonECH2O translated");
								DataEntry[] transformed_data =  new DataEntry[data.length];
								for(int i=0;i<data.length;i++) {
									final float x = data[i].value / 1000f; // mV to V
									float y = ((56.8366f*(x-0.81f)-7.58579f)*(x-0.28f)+52.6316f)*(x-0.09f);
									//log.info(y);
									transformed_data[i] = new DataEntry(data[i].timestamp,y);
								}
								tsdb.streamStorage.insertDataEntryArray(timestampseries.name, "SM_10", transformed_data);
							}
						}
					}
				} else {
					log.info("sensor not found "+sensorName);
				}
			}
			//log.info(timestampseries);
		} catch (Exception e) {
			log.error(e);
		}
	}

}
