package tsdb.loader.be;

import static tsdb.util.AssumptionCheck.throwNull;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.component.SourceEntry;
import tsdb.util.AssumptionCheck;
import tsdb.util.DataRow;
import tsdb.util.Pair;


/**
 * This class contains methods to read time series from input files in "BE"-Format and stores data into database.
 * @author woellauer
 *
 */
public class TimeSeriesLoaderBE {

	private static final Logger log = LogManager.getLogger();

	private final TsDB tsdb; //not null

	private final long minTimestamp;

	public TimeSeriesLoaderBE(TsDB tsdb, long minTimestamp) {
		throwNull(tsdb);
		this.tsdb = tsdb;
		this.minTimestamp = minTimestamp;
	}

	/**
	 * Loads directory with station directories with dat-files (tsm structure)
	 * @param rootPath
	 */
	public void loadDirectory_with_stations_flat(Path rootPath) {
		try {
			log.info("load directory with stations:        "+rootPath);
			DirectoryStream<Path> pathStream = Files.newDirectoryStream(rootPath);
			@SuppressWarnings("unchecked")
			Pair<Station,Path>[] pairs = StreamSupport.stream(pathStream.spliterator(), false)
			.sorted()
			.flatMap(stationPath->{
				try {
					String stationID = stationPath.getName(stationPath.getNameCount()-1).toString();
					Station station = tsdb.getStation(stationID);
					if(station!=null) {					
						return Stream.of(new Pair<Station,Path>(station,stationPath));
					} else {
						log.error("load directory with stations unknown station:   "+stationID+"    from   "+stationPath);
						return Stream.empty();
					}
				} catch(Exception e) {
					log.error(e);
					return Stream.empty();
				}
			})
			.toArray(Pair[]::new);

			pathStream.close();

			for(Pair<Station, Path> pair:pairs) {
				loadDirectoryOfOneStation(pair.a,pair.b);
			}			
		} catch (Exception e) {
			log.error("load directory with stations: "+rootPath+"   "+e);
		}		
	}

	/**
	 * Reads all UDBF-Files of one directory and inserts the data entries into database
	 * @param stationPath
	 */
	public void loadDirectoryOfOneStation(Station station, Path stationPath) {
		try {
			AssumptionCheck.throwNull(station);
			AssumptionCheck.throwNull(stationPath);
			log.info("load station "+station.stationID+"     from  "+stationPath);
			TreeMap<String,List<Path>> prefixFilenameMap = new TreeMap<String,List<Path>>(); // TreeMap: prefix needs to be ordered!
			collectFlatDirectoryOfOneStation(stationPath,prefixFilenameMap);
			if(!prefixFilenameMap.isEmpty()) {
				loadWithPrefixFilenameMapOfOneStation(station, prefixFilenameMap);
			} else {
				log.info("no files in "+stationPath);
			}
		} catch(Exception e) {
			e.printStackTrace();
			log.error("load directory of station:  "+station+"  "+stationPath+"  "+e);
		}
	}

	private void collectFlatDirectoryOfOneStation(Path directory, TreeMap<String,List<Path>> mapPrefixFilename) {
		try {
			DirectoryStream<Path> stream = Files.newDirectoryStream(directory, x -> x.toString().endsWith(".dat"));
			for(Path pathfilename:stream) {				
				try {
					String fileName = pathfilename.getFileName().toString();
					String prefix = fileName.substring(0,fileName.indexOf('_'));
					List<Path> list = mapPrefixFilename.get(prefix);
					if(list==null) {
						list = new ArrayList<Path>();
						mapPrefixFilename.put(prefix, list);
					}
					list.add(pathfilename);
				} catch(Exception e) {
					log.error("collect flatDirectory of one Station file:  "+pathfilename+"  "+e);
				}
			}
			stream.close();
		} catch (Exception e) {
			log.error("collect flatDirectory of one Station root loop:  "+directory+"  "+e);
		}		
	}

	public void loadWithPrefixFilenameMapOfOneStation(Station station, TreeMap<String, List<Path>> prefixFilenameMap) {
		TreeMap<Long,DataRow> eventMap = new TreeMap<Long,DataRow>();

		for(Entry<String, List<Path>> prefixEntry:prefixFilenameMap.entrySet()) {
			//String prefix = entry.getKey();
			List<Path> pathList = prefixEntry.getValue();	

			List<List<DataRow>> eventsList = new ArrayList<List<DataRow>>();

			for(Path path:pathList) {
				try {
					UDBFTimestampSeries timeSeries = readUDBFTimeSeries(station.stationID, path);
					if(timeSeries!=null) {
						List<DataRow> eventList = translateToEvents(station, timeSeries, minTimestamp);
						if(eventList!=null) {
							eventsList.add(eventList);
							tsdb.sourceCatalog.insert(new SourceEntry(path,station.stationID,timeSeries.time[0],timeSeries.time[timeSeries.time.length-1],timeSeries.time.length,timeSeries.getHeaderNames(), new String[0],(int)timeSeries.timeConverter.getTimeStep().toMinutes()));
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					log.error("file not read: "+path+"\t"+e);
				}
			}

			@SuppressWarnings("unchecked")
			Iterator<DataRow>[] iterators = new Iterator[eventsList.size()];

			for(int i=0;i<eventsList.size();i++) {
				iterators[i]=eventsList.get(i).iterator();
			}

			DataRow[] currentEvent = new DataRow[iterators.length];

			for(int i=0;i<iterators.length;i++) {
				if(iterators[i].hasNext()) {
					currentEvent[i] = iterators[i].next();
				}				
			}


			long currentTimestamp = -1;
			DataRow collectorEvent = null;

			while(true) {

				int minIndex=-1;
				long minTimeStamp = Long.MAX_VALUE;
				for(int i=0;i<iterators.length;i++) {
					if(currentEvent[i]!=null) {
						if(currentEvent[i].timestamp<minTimeStamp) {
							minTimeStamp = currentEvent[i].timestamp;
							minIndex = i;
						}
					}
				}

				if(minIndex<0) {
					break;
				}

				if(currentTimestamp<currentEvent[minIndex].timestamp) {
					if(collectorEvent!=null) {
						if(eventMap.containsKey(collectorEvent.timestamp)) {
							//log.warn("event already inserted");
						} else {
							eventMap.put(collectorEvent.timestamp, collectorEvent);
						}
					}
					currentTimestamp = currentEvent[minIndex].timestamp;
					collectorEvent = null;
				}
				if(collectorEvent==null) {
					collectorEvent = currentEvent[minIndex];
				} else {
					Object[] payload = currentEvent[minIndex].data;
					Object[] collectorPayload = collectorEvent.data;
					for(int i=0;i<collectorPayload.length-1;i++) { // TODO
						if(!Float.isNaN((float) payload[i])&&Float.isNaN((float) collectorPayload[i])) {
							collectorPayload[i] = payload[i];
						}
					}
				}

				if(iterators[minIndex].hasNext()) {
					currentEvent[minIndex] = iterators[minIndex].next();
				} else {
					currentEvent[minIndex] = null;
				}

			}

			if(collectorEvent!=null) {
				if(eventMap.containsKey(collectorEvent.timestamp)) {
					//log.warn("event already inserted");
				} else {
					eventMap.put(collectorEvent.timestamp, collectorEvent);
				}
			}			
		}	

		if(eventMap.size()>0) {
			tsdb.streamStorage.insertData(station.stationID, eventMap, station.loggerType.sensorNames);			
		} else {
			log.warn("no data to insert: "+station);
		}		
	}

	/**
	 * Reads an UDBF-File and return structured data as UDBFTimeSeries Object.
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static UDBFTimestampSeries readUDBFTimeSeries(String stationID_info, Path filename) throws IOException {
		log.trace("load UDBF file:\t"+filename+"\tplotID:\t"+stationID_info);
		UniversalDataBinFile udbFile = new UniversalDataBinFile(filename);
		if(!udbFile.isEmpty()){
			UDBFTimestampSeries udbfTimeSeries = udbFile.getUDBFTimeSeries();
			udbFile.close();
			return udbfTimeSeries;
		} else {
			log.info("empty file: "+filename);
			udbFile.close();
			return null;
		}		
	}

	/**
	 * Convertes rows of input file data into events with matching schema of the event stream of this plotID 
	 * @param udbfTimeSeries
	 * @param minTimestamp minimal timestamp that should be included in result
	 * @return List of Events, time stamp ordered 
	 */
	public List<DataRow> translateToEvents(Station station, UDBFTimestampSeries udbfTimeSeries, long minTimestamp) {
		List<DataRow> resultList = new ArrayList<DataRow>(); // result list of events	

		//mapping: UDBFTimeSeries column index position -> Event column index position;    eventPos[i] == -1 -> no mapping		
		int[] eventPos = new int[udbfTimeSeries.sensorHeaders.length];  

		//sensor names contained in event stream schema
		String[] sensorNames = station.loggerType.sensorNames;


		ArrayList<String> infoListNoMapping = new ArrayList<String>(1);
		ArrayList<String> infoListNoSchemaMapping = new ArrayList<String>(1);

		//creates mapping eventPos   (  udbf pos -> event pos )
		for(int sensorIndex=0; sensorIndex<udbfTimeSeries.sensorHeaders.length; sensorIndex++) {
			eventPos[sensorIndex] = -1;
			SensorHeader sensorHeader = udbfTimeSeries.sensorHeaders[sensorIndex];
			String rawSensorName = sensorHeader.name;
			if(!tsdb.containsIgnoreSensorName(rawSensorName)) {
				String sensorName = station.translateInputSensorName(rawSensorName,true);
				//System.out.println(sensorHeader.name+"->"+sensorName);
				if(sensorName!=null&&sensorName.equals("NaN")) { // ignore sensor
					continue;
				}
				if(sensorName != null) {
					for(int schemaIndex=0;schemaIndex<sensorNames.length;schemaIndex++) {
						String schemaSensorName = sensorNames[schemaIndex];
						if(schemaSensorName.equals(sensorName)) {
							eventPos[sensorIndex] = schemaIndex;
						}
					}
				}
				if(eventPos[sensorIndex] == -1) {
					if(sensorName==null) {
						infoListNoMapping.add(rawSensorName);
						//log.info("sensor name not in translation map: "+rawSensorName+" -> "+sensorName+"\t"+station.stationID+"\t"+udbfTimeSeries.filename+"\t"+station.loggerType);
					} else {
						infoListNoSchemaMapping.add(rawSensorName+" -> "+sensorName);
						//log.info("sensor name not in schema: "+rawSensorName+" -> "+sensorName+"\t"+station.stationID+"\t"+udbfTimeSeries.filename+"\t"+station.loggerType);
					}
				}
			}
		}

		if(!infoListNoMapping.isEmpty()) {
			log.warn("sensor names not in translation map: "+infoListNoMapping+"\t"+station.stationID+"\t"+udbfTimeSeries.filename+"\t"+station.loggerType);
		}

		if(!infoListNoSchemaMapping.isEmpty()) {
			log.warn("sensor names not in schema: "+infoListNoSchemaMapping+"\t"+station.stationID+"\t"+udbfTimeSeries.filename+"\t"+station.loggerType);
		}

		//mapping event index position -> sensor index position 
		int[] sensorPos = new int[sensorNames.length];
		for(int i=0;i<sensorPos.length;i++) {
			sensorPos[i] = -1;
		}
		int validSensorCount = 0;
		for(int i=0;i<eventPos.length;i++) {
			if(eventPos[i]>-1) {
				validSensorCount++;
				sensorPos[eventPos[i]] = i;
			}
		}

		if(validSensorCount<1) {
			log.trace("no fitting sensors in "+udbfTimeSeries.filename);
			return null; //all event columns are empty
		}

		//create events
		Float[] payload = new Float[station.loggerType.sensorNames.length];
		//short sampleRate = (short) udbfTimeSeries.timeConverter.getTimeStep().toMinutes();
		//iterate over input rows
		for(int rowIndex=0;rowIndex<udbfTimeSeries.time.length;rowIndex++) {			
			long timestamp = udbfTimeSeries.time[rowIndex];
			if(timestamp<minTimestamp) {
				continue;
			}

			// one input row
			float[] row = udbfTimeSeries.data[rowIndex];

			//fill event columns with input data values
			for(int attrNr=0;attrNr<sensorNames.length;attrNr++) {
				if(sensorPos[attrNr]<0) { // no input column
					payload[attrNr] = Float.NaN;
				} else {
					float value = row[sensorPos[attrNr]];				
					payload[attrNr] = value;
				}
			}

			resultList.add(new DataRow(Arrays.copyOf(payload, payload.length), timestamp));		
		}

		return resultList;
	}	

	/**
	 * specific to BE:
	 * read files with root folder
	 * @param rootPath
	 */
	@Deprecated
	public void loadDirectory_with_stations_structure_two(Path rootPath) {
		log.info("loadDirectory_with_stations_structure_two:\t"+rootPath);
		try {
			DirectoryStream<Path> stream = Files.newDirectoryStream(rootPath);
			for(Path stationPath:stream) {
				System.out.println(stationPath+"\t");
				String stationID = stationPath.getName(stationPath.getNameCount()-1).toString();				
				if(!tsdb.stationExists(stationID)) {
					log.error("station does not exist in database:\t"+stationID);
				} else {				
					Station station = tsdb.getStation(stationID);
					Path newPath = Paths.get(stationPath.toString(),"backup");
					if(Files.exists(newPath)) {
						loadDirectoryOfOneStation(station,newPath);
					}
				}
			}
			stream.close();
		} catch (IOException e) {
			log.error(e);
		}		
	}

	/**
	 * loads all files of all exploratories
	 * directory structure example: [exploratoriesPath]/HEG/HG01/20080130_^b0_0000.dat ... 
	 * @param exploratoriesPath
	 */
	@Deprecated
	public void loadDirectoryOfAllExploratories_structure_one(Path exploratoriesPath) {
		log.info("loadDirectoryOfAllExploratories_structure_one:\t"+exploratoriesPath);
		try {
			DirectoryStream<Path> stream = Files.newDirectoryStream(exploratoriesPath);
			for(Path path:stream) {
				System.out.println(path);
				loadDirectoryOfOneExploratory_structure_one(path);
			}
			stream.close();
		} catch (IOException e) {
			log.error(e);
		}
	}

	/**
	 * loads all files of one exploratory HEG, HEW, ...
	 * directory structure example: [exploratoriyPath]/HG01/20080130_^b0_0000.dat ... 
	 * @param exploratoriyPath
	 */
	@Deprecated
	public void loadDirectoryOfOneExploratory_structure_one(Path exploratoriyPath) {
		log.info("load exploratory:\t"+exploratoriyPath);
		try {
			DirectoryStream<Path> stream = Files.newDirectoryStream(exploratoriyPath);
			for(Path stationPath:stream) {
				String stationID = stationPath.subpath(stationPath.getNameCount()-1, stationPath.getNameCount()).toString();

				//*** workaround for directory names ***

				if(stationID.startsWith("HG")) {
					stationID = "HEG"+stationID.substring(2);
				} else if(stationID.startsWith("HW")) {
					stationID = "HEW"+stationID.substring(2);
				}

				//**********************************


				if(!tsdb.stationExists(stationID)) {
					log.error("station does not exist in database:\t"+stationID);
				} else {				
					Station station = tsdb.getStation(stationID);
					loadDirectoryOfOneStation(station,stationPath);
				}
			}
			stream.close();
		} catch (IOException e) {
			log.error(e);
		}
	}
}
