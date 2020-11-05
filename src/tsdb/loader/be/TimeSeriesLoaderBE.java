package tsdb.loader.be;

import static tsdb.util.AssumptionCheck.throwNull;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import tsdb.component.labeledproperty.LabeledProperty;
import tsdb.component.labeledproperty.PropertyCNR4;
import tsdb.component.labeledproperty.PropertyClear;
import tsdb.component.labeledproperty.PropertyComputation;
import tsdb.component.labeledproperty.PropertyReassign;
import tsdb.util.AssumptionCheck;
import tsdb.util.DataRow;
import tsdb.util.Interval;
import tsdb.util.Pair;
import tsdb.util.TimeUtil;


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

	private void merge(DataRow collector, DataRow add) {
		//log.info("merge "+Arrays.toString(collector.data));
		if(collector!=add) {
			float[] collectorData = collector.data;
			float[] currentData = add.data;
			int len = collectorData.length;
			for(int i=0;i<len;i++) {
				if(Float.isNaN(collectorData[i])) {
					collectorData[i] = currentData[i];
				}
			}
		}
	}

	private void addToMap(TreeMap<Long,DataRow> map, DataRow dataRow) {
		DataRow collector = map.get(dataRow.timestamp);
		if(collector==null) {
			map.put(dataRow.timestamp, dataRow);
		} else {
			merge(collector, dataRow);
		}
	}

	public void loadWithPrefixFilenameMapOfOneStation(Station station, TreeMap<String, List<Path>> prefixFilenameMap) {
		TreeMap<Long,DataRow> eventMap = new TreeMap<Long,DataRow>();

		for(Entry<String, List<Path>> prefixEntry:prefixFilenameMap.entrySet()) {
			//String prefix = entry.getKey();
			List<Path> pathList = prefixEntry.getValue();	

			List<List<DataRow>> eventLists = new ArrayList<List<DataRow>>();

			for(Path path:pathList) {
				try {
					UDBFTimestampSeries timeSeries = readUDBFTimeSeries(station.stationID, path);
					if(timeSeries!=null) {
						String[][] outInfoTranslatedSensorNames = new String[1][];
						List<DataRow> eventList = translateToEvents(station, timeSeries, minTimestamp, outInfoTranslatedSensorNames);
						if(eventList!=null) {
							eventLists.add(eventList);
							tsdb.sourceCatalog.insert(new SourceEntry(path,station.stationID,timeSeries.time[0],timeSeries.time[timeSeries.time.length-1],timeSeries.time.length,timeSeries.getHeaderNames(), outInfoTranslatedSensorNames[0],(int)timeSeries.timeConverter.getTimeStep().toMinutes()));
						}
					}
				} catch (Exception e) {
					//e.printStackTrace();
					log.error("file not read: "+path+"\t"+e);
				}
			}

			@SuppressWarnings("unchecked")
			Iterator<DataRow>[] iterators = new Iterator[eventLists.size()];

			for(int i=0;i<eventLists.size();i++) {
				iterators[i]=eventLists.get(i).iterator();
			}

			DataRow[] itDataRows = new DataRow[iterators.length];
			for(int i=0;i<iterators.length;i++) {
				if(iterators[i].hasNext()) {
					itDataRows[i] = iterators[i].next();
				}				
			}

			long collectorTimestamp = -1;
			DataRow collectorRow = null;
			while(true) {
				int currentItIndex=-1;
				long currentTimestamp = Long.MAX_VALUE;
				for(int i=0;i<iterators.length;i++) { // minimum timestamp of itDataRows
					DataRow dataRow = itDataRows[i];
					if(dataRow!=null) {
						if(dataRow.timestamp<currentTimestamp) {
							currentTimestamp = dataRow.timestamp;
							currentItIndex = i;
						}
					}
				}
				if(currentItIndex<0) { // no more elements
					break;
				}
				DataRow currentDataRow = itDataRows[currentItIndex];
				if(collectorTimestamp<currentTimestamp) { // new timestamp, insert old collectorRow
					if(collectorRow!=null) {
						addToMap(eventMap, collectorRow);
					}
					collectorTimestamp = currentTimestamp;
					collectorRow = currentDataRow;
				} else if(currentTimestamp==collectorTimestamp) { // merge elements
					merge(collectorRow, currentDataRow);
				} else {
					throw new RuntimeException("timestamps not ordered");
				}				
				itDataRows[currentItIndex] = iterators[currentItIndex].hasNext()?iterators[currentItIndex].next():null;
			}			
			if(collectorRow!=null) { // last row
				addToMap(eventMap, collectorRow);
			}		
		}	

		if(eventMap.size()>0) {

			Iterator<LabeledProperty> it = station.labeledProperties.query_iterator(eventMap.firstKey().intValue(), eventMap.lastKey().intValue());

			while(it.hasNext()) {
				LabeledProperty prop = it.next();
				switch (prop.label) {
				case "computation": {
					Collection<DataRow> rows = eventMap.subMap((long)prop.start, true, (long)prop.end, true).values();
					try {
						PropertyComputation cprop = (PropertyComputation) prop.content;
						log.info(TimeUtil.oleMinutesToText(eventMap.firstKey().intValue(), eventMap.lastKey().intValue()) + "    computation "+prop.station + "  " +  TimeUtil.oleMinutesToText(prop.start, prop.end) + "   " + cprop.target +"    "+ cprop.formula_org);
						cprop.calculate(rows, station.loggerType.sensorNames);
					} catch(Exception e) {
						log.warn(e);
					}
					break;
				}
				case "CNR4": {
					Collection<DataRow> rows = eventMap.subMap((long)prop.start, true, (long)prop.end, true).values();
					try {
						((PropertyCNR4)prop.content).calculate(rows, station.loggerType.sensorNames);
					} catch(Exception e) {
						log.warn(station.stationID+"   "+ e);
					}
					break;
				}
				case "reassign": {
					Collection<DataRow> rows = eventMap.subMap((long)prop.start, true, (long)prop.end, true).values();
					try {
						((PropertyReassign)prop.content).calculate(rows, station.loggerType.sensorNames);
					} catch(Exception e) {
						log.warn(station.stationID+"   "+ e);
					}
					break;
				}
				case "clear": {
					Collection<DataRow> rows = eventMap.subMap((long)prop.start, true, (long)prop.end, true).values();
					try {
						((PropertyClear)prop.content).calculate(rows, station.loggerType.sensorNames);
					} catch(Exception e) {
						log.warn(station.stationID+"   "+ e);
					}
					break;
				}
				default:
					log.warn("unknown property label: "+prop.label);
					break;
				}
			}


/*
			{
				List<LabeledProperty> cnr4List = station.labeledProperties.query("CNR4", eventMap.firstKey().intValue(), eventMap.lastKey().intValue());
				if(cnr4List.size()>0) {
					log.info("LabeledProperty CNR4");				
					for(LabeledProperty prop:cnr4List) {					
						Collection<DataRow> rows = eventMap.subMap((long)prop.start, true, (long)prop.end, true).values();
						try {
							((PropertyCNR4)prop.content).calculate(rows, station.loggerType.sensorNames);
						} catch(Exception e) {
							log.warn(e);
						}
					}
				}
			}

			{
				List<LabeledProperty> cnr4_calcList = station.labeledProperties.query("CNR4_calc", eventMap.firstKey().intValue(), eventMap.lastKey().intValue());
				if(cnr4_calcList.size()>0) {
					log.info("LabeledProperty CNR4_calc");				
					for(LabeledProperty prop:cnr4_calcList) {					
						Collection<DataRow> rows = eventMap.subMap((long)prop.start, true, (long)prop.end, true).values();
						try {
							((PropertyCNR4_calc)prop.content).calculate(rows, station.loggerType.sensorNames);
						} catch(Exception e) {
							log.warn(e);
						}
					}
				}
			}

			List<LabeledProperty> computationList = station.labeledProperties.query("computation", eventMap.firstKey().intValue(), eventMap.lastKey().intValue());
			if(computationList.size()>0) {
				log.info("LabeledProperty computation");				
				for(LabeledProperty prop:computationList) {					
					Collection<DataRow> rows = eventMap.subMap((long)prop.start, true, (long)prop.end, true).values();
					try {
						((PropertyComputation)prop.content).calculate(rows, station.loggerType.sensorNames);
					} catch(Exception e) {
						log.warn(e);
					}
				}
			}*/

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
			return udbfTimeSeries;
		} else {
			log.info("empty file: "+filename);
			return null;
		}		
	}

	/**
	 * Convertes rows of input file data into events with matching schema of the event stream of this plotID 
	 * @param udbfTimeSeries
	 * @param minTimestamp minimal timestamp that should be included in result
	 * @return List of Events, time stamp ordered 
	 */
	public List<DataRow> translateToEvents(Station station, UDBFTimestampSeries udbfTimeSeries, long minTimestamp, String[][] outInfoTranslatedSensorNames) {
		List<DataRow> resultList = new ArrayList<DataRow>(); // result list of events

		Interval fileTimeInterval = udbfTimeSeries.getTimeInterval();

		//mapping: UDBFTimeSeries column index position -> Event column index position;    eventPos[i] == -1 -> no mapping		
		int[] eventPos = new int[udbfTimeSeries.sensorHeaders.length];  

		//sensor names contained in event stream schema
		String[] sensorNames = station.loggerType.sensorNames;


		ArrayList<String> infoListNoMapping = new ArrayList<String>(1);
		ArrayList<String> infoListNoSchemaMapping = new ArrayList<String>(1);
		String[] infoTranslatedSensorNames = new String[udbfTimeSeries.sensorHeaders.length];
		outInfoTranslatedSensorNames[0] = infoTranslatedSensorNames;

		//creates mapping eventPos   (  udbf pos -> event pos )
		for(int sensorIndex=0; sensorIndex<udbfTimeSeries.sensorHeaders.length; sensorIndex++) {
			eventPos[sensorIndex] = -1;
			SensorHeader sensorHeader = udbfTimeSeries.sensorHeaders[sensorIndex];
			String rawSensorName = sensorHeader.name;
			if(!tsdb.containsIgnoreSensorName(rawSensorName)) {

				//correct rawSensorName
				String correctedSensorName = station.correctRawSensorName(rawSensorName, fileTimeInterval);				
				String sensorName = station.translateInputSensorName(correctedSensorName,true);
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
				} else {
					infoTranslatedSensorNames[sensorIndex] = sensorName;
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
		float[] payload = new float[station.loggerType.sensorNames.length];
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
}
