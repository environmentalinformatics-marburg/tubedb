package tsdb;

import static tsdb.util.AssumptionCheck.throwNull;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ini4j.Profile.Section;
import org.ini4j.Wini;

import tsdb.component.LoggerType;
import tsdb.component.Region;
import tsdb.component.Sensor;
import tsdb.component.SensorCategory;
import tsdb.util.AggregationType;
import tsdb.util.Interval;
import tsdb.util.Table;
import tsdb.util.Table.ColumnReaderDouble;
import tsdb.util.Table.ColumnReaderFloat;
import tsdb.util.Table.ColumnReaderString;
import tsdb.util.TimeUtil;
import tsdb.util.Util;
import tsdb.util.Util.FloatRange;
import au.com.bytecode.opencsv.CSVReader;

/**
 * Reads config files and inserts meta data into TimeSeriesDatabase
 * @author woellauer
 *
 */
public class ConfigLoader {

	private static final Logger log = LogManager.getLogger();

	private final TsDB tsdb; //not null

	public ConfigLoader(TsDB tsdb) {
		throwNull(tsdb);
		this.tsdb = tsdb;
	}

	private class GeneralStationBuilder {

		public String name;
		public Region region;
		public String longName;
		public String group;

		public GeneralStationBuilder(String name) {
			this.name = name;
		}

		public GeneralStation create() {
			if(longName==null) {
				longName = name;
			}
			if(group==null) {
				group = name;
			}
			return new GeneralStation(name, region, longName, group);
		}
	}

	/**
	 * reads names of used general stations
	 * @param configFile
	 */
	public void readGeneralStation(String configFile) {		
		try {
			Wini ini = new Wini(new File(configFile));
			TreeMap<String, GeneralStationBuilder> creationMap = new TreeMap<String,GeneralStationBuilder>();

			Section section_general_stations = ini.get("general_stations");//********************  [general_stations]
			for(Entry<String, String> entry:section_general_stations.entrySet()) {
				GeneralStationBuilder generalStationBuilder = new GeneralStationBuilder(entry.getKey());
				String regionName = entry.getValue();
				generalStationBuilder.region = tsdb.getRegion(regionName);
				if(generalStationBuilder.region == null) {
					log.warn("region not found: "+regionName);
				}
				creationMap.put(generalStationBuilder.name, generalStationBuilder);
			}

			Section section_general_station_long_names = ini.get("general_station_long_names");  //******************** [general_station_long_names]
			if(section_general_station_long_names!=null) {
				for(Entry<String, String> entry:section_general_station_long_names.entrySet()) {
					if(creationMap.containsKey(entry.getKey())) {
						creationMap.get(entry.getKey()).longName = entry.getValue();
					} else {
						log.warn("general station unknown: "+entry.getKey());
					}
				}
			}

			Section section_general_station_groups = ini.get("general_station_groups"); //******************** [general_station_groups]			if(section_general_station_long_names!=null) {
			if(section_general_station_groups!=null) {
				for(Entry<String, String> entry:section_general_station_groups.entrySet()) {
					if(creationMap.containsKey(entry.getKey())) {
						creationMap.get(entry.getKey()).group = entry.getValue();
					} else {
						log.warn("general station unknown: "+entry.getKey());
					}
				}
			}

			for(GeneralStationBuilder e:creationMap.values()) {
				tsdb.insertGeneralStation(e.create());
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}		
	}

	/**
	 * for each station type read schema of data, only data of names in this schema is included in the database
	 * This method creates LoggerType Objects
	 * @param configFile
	 */
	public void readLoggerTypeSchema(String configFile) {
		try {
			Wini ini = new Wini(new File(configFile));
			for(String typeName:ini.keySet()) {
				Section section = ini.get(typeName);
				List<String> names = new ArrayList<String>();			
				for(String name:section.keySet()) {
					names.add(name);
				}
				String[] sensorNames = new String[names.size()];
				for(int i=0;i<names.size();i++) {
					String sensorName = names.get(i);
					sensorNames[i] = sensorName;
					if(tsdb.sensorExists(sensorName)) {
						// log.info("sensor already exists: "+sensorName+" new in "+typeName);
					} else {
						tsdb.insertSensor(new Sensor(sensorName));
					}
				}
				tsdb.insertLoggerType(new LoggerType(typeName, sensorNames));
			}
		} catch (Exception e) {
			log.error(e);
		}
	}

	/**
	 * reads properties of stations and creates Station Objects
	 * @param configFile
	 */
	public void readStation(String configFile) {
		Map<String,List<StationProperties>> plotIdMap = readStationConfigInternal(configFile);

		for(Entry<String, List<StationProperties>> entryMap:plotIdMap.entrySet()) {
			if(entryMap.getValue().size()!=1) {
				log.error("multiple properties for one station not implemented:\t"+entryMap.getValue());
			} else {
				String plotID = entryMap.getKey();
				String generalStationName = plotID.substring(0, 3);
				GeneralStation generalStation = tsdb.getGeneralStation(generalStationName);
				if(generalStation==null&&generalStationName.charAt(2)=='T') {
					generalStationName = ""+generalStationName.charAt(0)+generalStationName.charAt(1)+'W'; // AET06 and SET39 ==> AEW and SEW
					generalStation = tsdb.getGeneralStation(generalStationName);					
				}
				if(generalStation==null) {
					log.warn("general station not found: "+generalStationName+" of "+plotID);
				}
				LoggerType loggerType = tsdb.getLoggerType(entryMap.getValue().get(0).get_logger_type_name()); 
				if(loggerType!=null) {
					Station station = new Station(tsdb, generalStation, plotID, loggerType, entryMap.getValue(), true);
					tsdb.insertStation(station);
				} else {
					log.error("logger type not found: "+entryMap.getValue().get(0).get_logger_type_name()+" -> station not created: "+plotID);
				}				
			}		
		}	
	}

	/**
	 * reads properties of stations
	 * @param configFile
	 */
	static Map<String,List<StationProperties>> readStationConfigInternal(String config_file) {
		try {
			CSVReader reader = new CSVReader(new FileReader(config_file));
			List<String[]> list = reader.readAll();
			reader.close();
			String[] names = list.get(0);			
			final String NAN_TEXT = "NaN";			
			Map<String,Integer> nameMap = new HashMap<String,Integer>();			
			for(int i=0;i<names.length;i++) {
				if(!names[i].equals(NAN_TEXT)) {
					if(nameMap.containsKey(names[i])) {
						log.error("dublicate name: "+names[i]);
					} else {
						nameMap.put(names[i], i);
					}
				}
			}

			String[][] values = new String[list.size()-1][];
			for(int i=1;i<list.size();i++) {
				values[i-1] = list.get(i);
			}

			Map<String,List<StationProperties>> plotidMap = new HashMap<String,List<StationProperties>>();
			int plotidIndex = nameMap.get("PLOTID");
			for(String[] row:values) {
				String plotid = row[plotidIndex];
				List<StationProperties> entries = plotidMap.get(plotid);
				if(entries==null) {
					entries = new ArrayList<StationProperties>(1);
					plotidMap.put(plotid, entries);
				}				

				Map<String,String> valueMap = new TreeMap<String, String>();
				for(Entry<String, Integer> mapEntry:nameMap.entrySet()) {

					String value = row[mapEntry.getValue()];
					if(!value.toUpperCase().equals(NAN_TEXT.toUpperCase())) {
						valueMap.put(mapEntry.getKey(), value);
					}					
				}

				entries.add(new StationProperties(valueMap));
			}
			return plotidMap;			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public void readSensorTranslation(String iniFile) {
		try {
			Wini ini = new Wini(new File(iniFile));
			for(Section section:ini.values()) {
				String sectionName = section.getName();
				int index = sectionName.indexOf("_logger_type_sensor_translation");
				if(index>-1) {
					readLoggerTypeSensorTranslation(sectionName.substring(0,index),section);
					continue;
				}
				index = sectionName.indexOf("_generalstation_sensor_translation");
				if(index>-1) {
					readGeneralStationSensorTranslation(sectionName.substring(0,index),section);
					continue;
				}
				index = sectionName.indexOf("_station_sensor_translation");
				if(index>-1) {
					readStationSensorTranslation(sectionName.substring(0,index),section);
					continue;
				}
				log.warn("section unknown: "+sectionName);
			}
		} catch (Exception e) {
			log.error(e);
		}
	}

	private void readLoggerTypeSensorTranslation(String loggerTypeName, Section section) {
		LoggerType loggerType = tsdb.getLoggerType(loggerTypeName);
		if(loggerType==null) {
			log.error("logger not found: "+loggerTypeName);
			return;
		}
		Map<String, String> translationMap = Util.readIniSectionMap(section);
		for(Entry<String, String> entry:translationMap.entrySet()) {
			if(loggerType.sensorNameTranlationMap.containsKey(entry.getKey())) {
				log.warn("overwriting");
			}
			if(entry.getKey().equals(entry.getValue())) {
				log.info("redundant entry "+entry+" in "+section.getName());
			}
			loggerType.sensorNameTranlationMap.put(entry.getKey(), entry.getValue());
		}
	}

	private void readGeneralStationSensorTranslation(String generalStationName, Section section) {
		GeneralStation generalStation = tsdb.getGeneralStation(generalStationName);
		if(generalStation==null) {
			log.error("generalStation not found: "+generalStationName);
			return;
		}
		Map<String, String> translationMap = Util.readIniSectionMap(section);
		for(Entry<String, String> entry:translationMap.entrySet()) {
			if(generalStation.sensorNameTranlationMap.containsKey(entry.getKey())) {
				log.warn("overwriting");
			}
			if(entry.getKey().equals(entry.getValue())) {
				log.info("redundant entry "+entry+" in "+section.getName());
			}
			generalStation.sensorNameTranlationMap.put(entry.getKey(), entry.getValue());
		}
	}

	private void readStationSensorTranslation(String stationName, Section section) {
		Station station = tsdb.getStation(stationName);
		if(station==null) {
			log.error("station not found: "+stationName);
			return;
		}
		Map<String, String> translationMap = Util.readIniSectionMap(section);
		for(Entry<String, String> entry:translationMap.entrySet()) {
			if(station.sensorNameTranlationMap.containsKey(entry.getKey())) {
				log.warn("overwriting");
			}
			station.sensorNameTranlationMap.put(entry.getKey(), entry.getValue());
		}
	}


	/**
	 * reads config for translation of input sensor names to database sensor names
	 * @param configFile
	 */
	@Deprecated
	public void readSensorNameTranslationConfig(String configFile) {		
		final String SENSOR_NAME_CONVERSION_HEADER_SUFFIX = "_header_0000";		
		try {
			Wini ini = new Wini(new File(configFile));
			for(LoggerType loggerType:tsdb.getLoggerTypes()) {
				log.trace("read config for "+loggerType.typeName);
				Section section = ini.get(loggerType.typeName+SENSOR_NAME_CONVERSION_HEADER_SUFFIX);
				if(section!=null) {
					loggerType.sensorNameTranlationMap = Util.readIniSectionMap(section);
				} else {
					log.trace("logger type name tranlation not found:\t"+loggerType.typeName);
				}
			}

			final String NAME_CONVERSION_HEADER_SOIL_SUFFIX = "_soil_parameters_header_0000";
			for(Section section:ini.values()) {
				String sectionName = section.getName();
				for(GeneralStation generalStation:tsdb.getGeneralStations()) {
					String prefix = "000"+generalStation.name;
					if(sectionName.startsWith(prefix)) {
						String general_section = prefix+"xx"+NAME_CONVERSION_HEADER_SOIL_SUFFIX;
						if(sectionName.equals(general_section)) {
							generalStation.sensorNameTranlationMap = Util.readIniSectionMap(section);
						} else if(sectionName.endsWith(NAME_CONVERSION_HEADER_SOIL_SUFFIX)) {
							String plotID = sectionName.substring(3, 8);
							Station station = tsdb.getStation(plotID);
							if(station!=null) {
								station.sensorNameTranlationMap = Util.readIniSectionMap(section);
							} else {
								log.warn("station does not exist: "+plotID);
							}
						} else {
							log.warn("unknown: "+sectionName);
						}
					}				
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
	}

	/**
	 * read geo config of stations:
	 * 1. read geo pos of station
	 * 2. calculate ordered list for each station of stations nearest to current station within same general station
	 * @param config_file
	 */
	public void readStationGeoPosition(String config_file) {
		try{		
			Table table = Table.readCSV(config_file,',');		
			int plotidIndex = table.getColumnIndex("PlotID");
			int epplotidIndex = table.getColumnIndex("EP_Plotid"); 
			int lonIndex = table.getColumnIndex("Lon");
			int latIndex = table.getColumnIndex("Lat");			
			for(String[] row:table.rows) {
				String plotID = row[epplotidIndex];
				if(!plotID.endsWith("_canceled")) { // ignore plotid canceled positions
					Station station = tsdb.getStation(plotID);
					if(station!=null) {					
						try {					
							double lon = Double.parseDouble(row[lonIndex]);
							double lat = Double.parseDouble(row[latIndex]);					
							station.geoPosLongitude = lon;
							station.geoPosLatitude = lat;					
						} catch(Exception e) {
							log.warn("geo pos not read: "+plotID);
						}
						if(plotidIndex>-1) {
							station.alternativeID = row[plotidIndex];
						}
					} else {
						log.warn("station not found: "+row[epplotidIndex]+"\t"+row[lonIndex]+"\t"+row[latIndex]+"    in config file: "+config_file);
					}
				}

			}

		} catch(Exception e) {
			log.error(e);
		}		
		//calcNearestStations();		
	}

	public void calcNearestStations() {
		tsdb.updateGeneralStations();
		for(Station station:tsdb.getStations()) {

			if(!station.isPlot) {
				continue;
			}

			double[] geoPos = transformCoordinates(station.geoPosLongitude,station.geoPosLatitude);
			List<Object[]> differenceList = new ArrayList<Object[]>();

			List<Station> stationList = station.generalStation.stationList;
			//System.out.println(station.plotID+" --> "+stationList);
			for(Station targetStation:stationList) {
				if(station!=targetStation) { // reference compare
					double[] targetGeoPos = transformCoordinates(targetStation.geoPosLongitude,targetStation.geoPosLatitude);
					double difference = getDifference(geoPos, targetGeoPos);
					differenceList.add(new Object[]{difference,targetStation});
				}
			}
			differenceList.sort(new Comparator<Object[]>() {
				@Override
				public int compare(Object[] o1, Object[] o2) {
					double d1 = (double) o1[0];
					double d2 = (double) o2[0];					
					return Double.compare(d1, d2);
				}
			});
			List<Station> targetStationList = new ArrayList<Station>(differenceList.size());
			for(Object[] targetStation:differenceList) {
				targetStationList.add((Station) targetStation[1]);
			}
			station.nearestStations = targetStationList;
			//System.out.println(station.plotID+" --> "+station.nearestStationList);
		}

	}

	public void calcNearestVirtualPlots() {
		tsdb.updateGeneralStations();

		for(VirtualPlot virtualPlot:tsdb.getVirtualPlots()) {
			List<Object[]> differenceList = new ArrayList<Object[]>();

			String group = virtualPlot.generalStation.group;
			List<VirtualPlot> virtualPlots = new ArrayList<VirtualPlot>();
			tsdb.getGeneralStationsOfGroup(group).forEach(gs->virtualPlots.addAll(gs.virtualPlots));

			for(VirtualPlot targetVirtualPlot:virtualPlots) {
				if(virtualPlot!=targetVirtualPlot) {
					double difference = getDifference(virtualPlot, targetVirtualPlot);
					differenceList.add(new Object[]{difference,targetVirtualPlot});
				}
			}
			differenceList.sort(new Comparator<Object[]>() {
				@Override
				public int compare(Object[] o1, Object[] o2) {
					double d1 = (double) o1[0];
					double d2 = (double) o2[0];					
					return Double.compare(d1, d2);
				}
			});

			virtualPlot.nearestVirtualPlots = differenceList.stream().map(o->(VirtualPlot)o[1]).collect(Collectors.toList());
			//System.out.println(virtualPlot.plotID+" --> "+virtualPlot.nearestVirtualPlots);
		}
	}



	public static double[] transformCoordinates(double longitude, double latitude) {
		// TODO: do real transformation
		return new double[]{longitude,latitude};
	}

	public static double getDifference(double[] geoPos, double[] targetGeoPos) {
		return Math.sqrt((geoPos[0]-targetGeoPos[0])*(geoPos[0]-targetGeoPos[0])+(geoPos[1]-targetGeoPos[1])*(geoPos[1]-targetGeoPos[1]));
	}

	public static double getDifference(VirtualPlot source, VirtualPlot target) {
		return Math.sqrt((source.geoPosEasting-target.geoPosEasting)*(source.geoPosEasting-target.geoPosEasting)+(source.geoPosNorthing-target.geoPosNorthing)*(source.geoPosNorthing-target.geoPosNorthing));
	}

	public void readVirtualPlot(String config_file) {
		try{

			Table table = Table.readCSV(config_file,',');
			int plotidIndex = table.getColumnIndex("PlotID"); // virtual plotid
			//int lonIndex = table.getColumnIndex("Lon");
			//int latIndex = table.getColumnIndex("Lat");
			int eastingIndex = table.getColumnIndex("Easting");
			int northingIndex = table.getColumnIndex("Northing");
			int focalPlotIndex = table.getColumnIndex("FocalPlot");
			for(String[] row:table.rows) {
				String plotID = row[plotidIndex];				
				if(plotID.length()==4&&plotID.charAt(3)>='0'&&plotID.charAt(3)<='9') {
					String generalStationName = plotID.substring(0, 3);

					if(generalStationName.equals("sun")) {//correct sun -> cof
						generalStationName = "cof";
					}

					if(generalStationName.equals("mcg")) {
						generalStationName = "flm";
					}

					if(generalStationName.equals("mch")) {
						generalStationName = "fpo";
					}

					if(generalStationName.equals("mwh")) {
						generalStationName = "fpd";
					}

					GeneralStation generalStation = tsdb.getGeneralStation(generalStationName);					
					if(generalStation==null) {
						log.warn("unknown general station in: "+plotID+"\t"+generalStationName+"   in config file: "+config_file);
					}
					//String lon = row[lonIndex];
					//String lat = row[latIndex];
					String easting = row[eastingIndex];
					String northing = row[northingIndex];
					String focalPlot = row[focalPlotIndex];

					boolean isFocalPlot = false;
					if(focalPlot.equals("Y")) {
						isFocalPlot = true;
					}

					//double geoPoslongitude = Double.NaN;
					//double geoPosLatitude = Double.NaN;
					int geoPosEasting = -1;
					int geoPosNorthing = -1;

					/*try {					
						geoPoslongitude = Double.parseDouble(row[lonIndex]);
						geoPosLatitude = Double.parseDouble(row[latIndex]);					
					} catch(Exception e) {}

					if(Double.isNaN(geoPoslongitude)||Double.isNaN(geoPosLatitude)) {
						log.warn("geo pos not read: "+plotID);
					}*/

					try {
						geoPosEasting = Integer.parseInt(easting);
						geoPosNorthing = Integer.parseInt(northing);							
					} catch(Exception e) {}

					tsdb.insertVirtualPlot(new VirtualPlot(tsdb, plotID, generalStation, geoPosEasting, geoPosNorthing, isFocalPlot));					
				} else {
					log.warn("not valid plotID name: "+plotID+"  VirtualPlot not inserted"+"   in config file: "+config_file);;
				}

			}

			/*Table table = Table.readCSV(config_file);
			int plotidIndex = table.getColumnIndex("PlotID"); // virtual plotid
			int categoriesIndex = table.getColumnIndex("Categories"); // general station

			for(String[] row:table.rows) {
				String plotID = row[plotidIndex];

				String generalStationName = row[categoriesIndex];




				if(plotID.length()==4&&plotID.charAt(3)>='0'&&plotID.charAt(3)<='9') {
					String gen = plotID.substring(0, 3);
					if(timeseriesdatabase.generalStationExists(gen)) {
						generalStationName = gen;
					} else {
						log.warn("unknown general station in: "+plotID+"\t"+gen);
					}
				} else {
					log.warn("unknown general station in: "+plotID+"    config file: "+config_file);
				}



				if(!timeseriesdatabase.generalStationExists(generalStationName)) {// TODO
					log.warn("unknown general station: "+generalStationName+"   in config file: "+config_file);
				}				
				timeseriesdatabase.insertVirtualPlot(new VirtualPlot(timeseriesdatabase, plotID, generalStationName));
			}*/



			/*int plotidIndex = table.getColumnIndex("PlotID");
			int epplotidIndex = table.getColumnIndex("EP_Plotid"); 
			int lonIndex = table.getColumnIndex("Lon");
			int latIndex = table.getColumnIndex("Lat");			
			for(String[] row:table.rows) {
				String plotID = row[epplotidIndex];
				if(!plotID.endsWith("_canceled")) { // ignore plotid canceled positions
					Station station = stationMap.get(plotID);
					if(station!=null) {					
						try {					
							double lon = Double.parseDouble(row[lonIndex]);
							double lat = Double.parseDouble(row[latIndex]);					
							station.geoPoslongitude = lon;
							station.geoPosLatitude = lat;					
						} catch(Exception e) {
							log.warn("geo pos not read: "+plotID);
						}
						if(plotidIndex>-1) {
							station.serialID = row[plotidIndex];
						}
					} else {
						log.warn("station not found: "+row[epplotidIndex]+"\t"+row[lonIndex]+"\t"+row[latIndex]);
					}
				}

			}*/

		} catch(Exception e) {
			log.error(e);
		}				
	}

	/**
	 * reads properties of stations and creates Station Objects
	 * @param configFile
	 */
	public void readKiStation(String configFile) { //  KiLi
		Map<String, List<StationProperties>> serialNameMap = readKiLiStationConfigInternal(configFile);
		for(Entry<String, List<StationProperties>> entry:serialNameMap.entrySet()) {
			String serialName = entry.getKey();
			List<StationProperties> propertiesList = entry.getValue();
			if(!tsdb.stationExists(serialName)) {
				LoggerType loggerType = null;
				for(StationProperties properties:propertiesList) {
					String newloggerName = loggerPropertyKiLiToLoggerName(properties.get_logger_type_name());
					if(newloggerName!=null) {
						LoggerType newloggerType = tsdb.getLoggerType(newloggerName);
						if(newloggerType!=null) {
							if(loggerType!=null&&loggerType!=newloggerType) {
								log.warn("different logger types defined: "+loggerType+"  "+newloggerType+"   in "+serialName+"   in config file: "+configFile);
							}
							loggerType = newloggerType;
						} else {
							log.warn("loggertype not found: "+newloggerName+"   in config file: "+configFile);
						}
					} else {
						log.warn("no loggertype name");
					}
				}
				if(loggerType!=null) {
					Station station = new Station(tsdb,null,serialName,loggerType,propertiesList, false);
					tsdb.insertStation(station);					
					for(StationProperties properties:propertiesList) {
						String virtualPlotID = properties.get_plotid();
						VirtualPlot virtualPlot = tsdb.getVirtualPlot(virtualPlotID);
						if(virtualPlot==null) {
							if(virtualPlotID.length()==4) {
								String generalStationName = virtualPlotID.substring(0, 3);

								if(generalStationName.equals("sun")) {//correct sun -> cof
									generalStationName = "cof";
								}

								if(generalStationName.equals("mcg")) {
									generalStationName = "flm";
								}

								if(generalStationName.equals("mch")) {
									generalStationName = "fpo";
								}

								if(generalStationName.equals("mwh")) {
									generalStationName = "fpd";
								}

								GeneralStation generalStation = tsdb.getGeneralStation(generalStationName);
								if(generalStation!=null) {
									virtualPlot = new VirtualPlot(tsdb, virtualPlotID, generalStation, Float.NaN, Float.NaN, false);
									log.trace("insert missing virtual plot "+virtualPlotID+" with "+generalStationName);
									tsdb.insertVirtualPlot(virtualPlot);
								} else {
									log.warn("generalstation not found: "+generalStationName+"   from  "+virtualPlotID);
								}
							}
						}
						if(virtualPlot!=null) {
							virtualPlot.addStationEntry(station, properties);
						} else {
							log.warn("virtual plotID not found: "+virtualPlotID+"   in config file: "+configFile);
						}
					}				
				} else {
					log.warn("station with no logger type not inserted: "+serialName+"   in config file: "+configFile);
				}				
			} else {
				log.warn("serialName already inserted: "+serialName);
			}
		}


		/*Map<String, List<StationProperties>> serialMap = readKiLiStationConfigInternal(configFile);
		for(Entry<String, List<StationProperties>> entry:serialMap.entrySet()) {
			String serial = entry.getKey();
			//Map<String, String> firstProperyMap = entry.getValue().get(0); // !!
			//final String GENERALSTATION_PROPERTY_NAME = "TYPE";
			//String firstGeneralStationName = firstProperyMap.get(GENERALSTATION_PROPERTY_NAME); // not used
			//String firstGeneralStationName = plotIdKiLiToGeneralStationName(firstProperyMap.get("PLOTID"));
			//System.out.println("generalStationName: "+generalStationName+" serial: "+serial);
			if(!stationMap.containsKey(serial)) {
				//System.out.println(serial);
				//String loggerName = loggerPropertyKiLiToLoggerName(entry.getValue().get(0).get("LOGGER"));
				String loggerName = loggerPropertyKiLiToLoggerName(entry.getValue().get(entry.getValue().size()-1).get_logger_type_name());// !! better loggerType match of inventory
				//System.out.println("logger name: "+loggerName);
				LoggerType loggerType = loggerTypeMap.get(loggerName); 
				if(loggerType!=null) {
					//Station station = new Station(this, null, serial,loggerType, entry.getValue().get(0), entry.getValue()); // !!
					Station station = new Station(this, null, serial,loggerType, entry.getValue());
					stationMap.put(serial, station);
					for(StationProperties properties:entry.getValue()) {
						String plotid = properties.get_plotid();
						//String generalStationName = properyMap.get(GENERALSTATION_PROPERTY_NAME);
						VirtualPlot virtualplot = virtualplotMap.get(plotid);
						if(virtualplot!=null) {
							//if(!virtualplot.generalStationName.equals(generalStationName)) {
						//log.warn("different general station names: "+virtualplot.generalStationName+"\t"+generalStationName+" in "+plotid);
					//}

							try {


								virtualplot.addStationEntry(station, properties);
							} catch (Exception e) {
								log.warn("entry not added: "+e);
							}
						} else {
							log.warn("virtual plotid not found: "+plotid+"    "+serial);
						}
					}
				} else {				
					log.error("logger type not found: "+entry.getValue().get(0).get_logger_type_name()+" -> station not created: "+serial);				
				}						
			}
			else {
				log.warn("serial already inserted: "+serial);
			}
		}*/
	}

	/**
	 * reads properties of stations
	 * @param configFile
	 */
	static Map<String, List<StationProperties>> readKiLiStationConfigInternal(String config_file) {  //  KiLi
		try {
			CSVReader reader = new CSVReader(new FileReader(config_file));
			List<String[]> list = reader.readAll();			
			String[] names = list.get(0);			
			final String NAN_TEXT = "NaN";			
			Map<String,Integer> nameMap = new HashMap<String,Integer>(); // map: header name -> column index			
			for(int i=0;i<names.length;i++) {
				if(!names[i].equals(NAN_TEXT)) {
					if(nameMap.containsKey(names[i])) {
						log.error("dublicate name: "+names[i]);
					} else {
						nameMap.put(names[i], i);
					}
				}
			}

			String[][] values = new String[list.size()-1][];
			for(int i=1;i<list.size();i++) {
				values[i-1] = list.get(i);
			}



			Map<String,List<StationProperties>> serialMap = new HashMap<String,List<StationProperties>>();
			int serialIndex = nameMap.get("SERIAL");
			for(String[] row:values) {
				String serial = row[serialIndex];

				List<StationProperties> mapList = serialMap.get(serial);
				if(mapList==null) {
					mapList = new ArrayList<StationProperties>(1);
					serialMap.put(serial, mapList);
				}

				TreeMap<String, String> properyMap = new TreeMap<String,String>();

				for(Entry<String, Integer> mapEntry:nameMap.entrySet()) {
					String value = row[mapEntry.getValue()];
					//if(!value.toUpperCase().equals(NAN_TEXT.toUpperCase())) {// ?? Nan should be in property map
					properyMap.put(mapEntry.getKey(), value);
					//}					
				}				
				mapList.add(new StationProperties(properyMap));
			}
			reader.close();
			return serialMap;			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * reads names of input sensors, that should not be included in database
	 * @param configFile
	 */
	public void readIgnoreSensorName(String configFile) {		
		try {
			Wini ini = new Wini(new File(configFile));
			Section section = ini.get("ignore_sensors");
			for(String name:section.keySet()) {				
				tsdb.insertIgnoreSensorName(name);
			}

		} catch (Exception e) {
			log.error(e);
		}	
	}

	/**
	 * read config for sensors: physical minimum and maximum values
	 * @param configFile
	 */
	public void readSensorPhysicalRangeConfig(String configFile) {
		List<FloatRange> list = Util.readIniSectionFloatRange(configFile,"parameter_physical_range");
		if(list!=null) {
			for(FloatRange entry:list) {
				Sensor sensor =  tsdb.getSensor(entry.name);
				if(sensor != null) {
					sensor.physicalMin = entry.min;
					sensor.physicalMax = entry.max;
				} else {
					log.warn("sensor not found: "+entry.name);
				}
			}
		}
	}

	public void readSensorStepRangeConfig(String configFile) {
		List<FloatRange> list = Util.readIniSectionFloatRange(configFile,"paramter_step_range");
		if(list!=null) {
			for(FloatRange entry:list) {
				Sensor sensor = tsdb.getSensor(entry.name);
				if(sensor != null) {
					sensor.stepMin = entry.min;
					sensor.stepMax = entry.max;
				} else {
					log.warn("sensor not found: "+entry.name);
				}
			}
		}
	}

	/**
	 * reads sensor config for base aggregation: for each sensor the type of aggregation is read
	 * @param configFile
	 */
	public void readBaseAggregationConfig(String configFile) {
		try {
			Wini ini = new Wini(new File(configFile));
			Section section = ini.get("base_aggregation");
			if(section!=null) {
				for(String sensorName:section.keySet()) {
					String aggregateTypeText = section.get(sensorName);					
					AggregationType aggregateType = AggregationType.getAggregationType(aggregateTypeText);
					if(aggregateType!=null&&aggregateType!=AggregationType.NONE) {
						tsdb.insertBaseAggregation(sensorName, aggregateType);
					} else {
						if(aggregateType!=null&&aggregateType==AggregationType.NONE) {
							tsdb.insertRawSensor(sensorName);
						} else {
							log.warn("aggregate type unknown: "+aggregateTypeText+"\tin\t"+sensorName);
						}
					}
				}
			}
		} catch (IOException e) {
			log.warn(e);
		}		
	}

	/**
	 * read list of sensors that should be included in gap filling processing
	 * @param configFile
	 */
	public void readInterpolationSensorNameConfig(String configFile) {
		try {
			Wini ini = new Wini(new File(configFile));
			Section section = ini.get("interpolation_sensors");
			for(String name:section.keySet()) {
				Sensor sensor = tsdb.getSensor(name);
				if(sensor!=null) {
					sensor.useInterpolation = true;
				} else {
					log.warn("interpolation config: sensor not found: "+name);
				}
			}
			
			for(Entry<String, String> entry:section.entrySet()) {
				Sensor sensor = tsdb.getSensor(entry.getKey());
				if(sensor!=null) {
					sensor.useInterpolation = true;
					try {
						sensor.maxInterpolationMSE = Double.parseDouble(entry.getValue());
					} catch (Exception e) {
						log.warn("could not read max MSE for sensor "+entry.getKey()+"   "+entry.getValue()+"   "+e);
					}
				} else {
					log.warn("interpolation config: sensor not found: "+entry.getKey());
				}
			}

		} catch (Exception e) {
			log.error(e);
		}
	}

	public void readEmpiricalDiffConfig(String configFile) {
		try {
			Wini ini = new Wini(new File(configFile));
			Section section = ini.get("parameter_empirical_diff");
			if(section!=null) {
				for(String sensorName:section.keySet()) {
					Sensor sensor = tsdb.getSensor(sensorName);
					if(sensor!=null) {
						String sensorDiff = section.get(sensorName);
						float diff = Float.parseFloat(sensorDiff);
						sensor.empiricalDiff = diff;
					} else {
						log.warn("sensor not found: "+sensorName);
					}
				}
			} else {
				throw new RuntimeException("section not found");
			}
		} catch (IOException e) {
			log.warn(e);
		}		
	}

	public void readRegion(String configFile) {
		try {
			Wini ini = new Wini(new File(configFile));

			Section section = ini.get("region");
			if(section!=null) {
				Map<String, String> regionNameMap = Util.readIniSectionMap(section);
				for(Entry<String, String> entry:regionNameMap.entrySet()) {
					String regionName = entry.getKey();
					String regionLongName = entry.getValue();
					tsdb.insertRegion(new Region(regionName, regionLongName));
				}
			} else {
				log.warn("region section not found");
			}

			section = ini.get("region_view_time_range");
			if(section!=null) {
				Map<String, String> regionNameMap = Util.readIniSectionMap(section);
				for(Entry<String, String> entry:regionNameMap.entrySet()) {
					String regionName = entry.getKey();
					String range = entry.getValue();
					Interval interval = Interval.parse(range);
					if(interval!=null) {
						if(interval.start>=1900&&interval.start<=2100&&interval.end>=1900&&interval.end<=2100) {
							int startTime = (int) TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(interval.start, 1, 1, 0, 0));
							int endTime = (int) TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(interval.end, 12, 31, 23, 0));
							Region region = tsdb.getRegion(regionName);
							if(region!=null) {
								region.viewTimeRange = Interval.of(startTime,endTime);
							} else {
								log.warn("region not found: "+regionName);
							}
						} else {
							log.warn("region_view_time_range section invalid year range "+range);
						}
					}
				}
			} else {
				log.warn("region_view_time_range section not found");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readSensorDescriptionConfig(String configFile) {
		try {
			Wini ini = new Wini(new File(configFile));

			Section section = ini.get("sensor_description");
			if(section!=null) {
				Map<String, String> regionNameMap = Util.readIniSectionMap(section);
				for(Entry<String, String> entry:regionNameMap.entrySet()) {
					String sensorName = entry.getKey();
					String sensorDescription = entry.getValue();
					Sensor sensor = tsdb.getSensor(sensorName);
					if(sensor!=null) {
						sensor.description = sensorDescription;
					} else {
						log.warn("read sensor info; sensor not found: "+sensorName);
					}
				}
			} else {
				log.warn("sensor_info section not found");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readSensorUnitConfig(String configFile) {
		try {
			Wini ini = new Wini(new File(configFile));

			Section section = ini.get("sensor_unit");
			if(section!=null) {
				Map<String, String> regionNameMap = Util.readIniSectionMap(section);
				for(Entry<String, String> entry:regionNameMap.entrySet()) {
					String sensorName = entry.getKey();
					String sensorUnit = entry.getValue();
					Sensor sensor = tsdb.getSensor(sensorName);
					if(sensor!=null) {
						sensor.unitDescription = sensorUnit;
					} else {
						log.warn("read sensor unit; sensor not found: "+sensorName);
					}
				}
			} else {
				log.warn("sensor_unit section not found");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void readSensorCategoryConfig(String configFile) {
		try {
			Wini ini = new Wini(new File(configFile));

			Section section = ini.get("sensor_category");
			if(section!=null) {
				Map<String, String> nameMap = Util.readIniSectionMap(section);
				for(Entry<String, String> entry:nameMap.entrySet()) {
					String sensorName = entry.getKey();
					String sensorCategory = entry.getValue();
					Sensor sensor = tsdb.getSensor(sensorName);
					if(sensor!=null) {
						sensor.category = SensorCategory.parse(sensorCategory);
					} else {
						log.warn("read sensor category; sensor not found: "+sensorName);
					}
				}
			} else {
				log.warn("sensor_unit section not found");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * mark sensors in config-file as internal
	 * @param configFile
	 */
	public void readSensorInternalConfig(String configFile) {
		try {
			Wini ini = new Wini(new File(configFile));
			Section section = ini.get("internal_sensors");
			if(section!=null) {
				section.keySet().forEach(sensorName->tsdb.getOrCreateSensor(sensorName).internal = true);
			} else {
				log.warn("internal_sensors section not found");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}	

	public static String loggerPropertyKiLiToLoggerName(String s) {
		if((s.charAt(0)>='0'&&s.charAt(0)<='9')&&(s.charAt(1)>='0'&&s.charAt(1)<='9')&&(s.charAt(2)>='0'&&s.charAt(2)<='9')){
			return s.substring(3);
		} else {
			return s;
		}
	}

	public void readVirtualPlotElevation(String configFile) {
		Table table = Table.readCSV(configFile,',');

		ColumnReaderString plotidReader = table.createColumnReader("PlotID");
		ColumnReaderFloat elevationReader = table.createColumnReaderFloat("Elevation");
		if(plotidReader==null||elevationReader==null) {
			log.error("readVirtualPlotElevationConfig: columns not found");
			return;
		}

		for(String[] row:table.rows) {
			String plotID = plotidReader.get(row);
			float elevation = elevationReader.get(row,true);
			VirtualPlot virtualPlot = tsdb.getVirtualPlot(plotID);
			if(virtualPlot==null) {
				log.warn("plotID not found: "+plotID);
				continue;
			}
			virtualPlot.setElevation(elevation);
		}
	}

	public void readVirtualPlotGeoPosition(String configFile) { //overwriting old geo pos
		Table table = Table.readCSV(configFile,',');
		ColumnReaderString plotidReader = table.createColumnReader("PlotID");
		ColumnReaderFloat eastingReader = table.createColumnReaderFloat("Easting");
		ColumnReaderFloat northingReader = table.createColumnReaderFloat("Northing");
		ColumnReaderDouble latReader = table.createColumnReaderDouble("Lat");
		ColumnReaderDouble lonReader = table.createColumnReaderDouble("Lon");
		for(String[] row:table.rows) {
			String plotID = plotidReader.get(row);
			VirtualPlot virtualPlot = tsdb.getVirtualPlot(plotID);
			if(virtualPlot==null) {
				log.trace("virtual plotID not found: "+plotID+"  in "+configFile);
				continue;
			}
			float easting = eastingReader.get(row,true);
			float northing = northingReader.get(row,true);
			virtualPlot.geoPosEasting = easting;
			virtualPlot.geoPosNorthing = northing;
			double lat = latReader.get(row, true);
			double lon = lonReader.get(row, true);
			virtualPlot.geoPosLatitude = lat;
			virtualPlot.geoPosLongitude = lon;
		}
	}

	public void readSaStation(String configFile) {
		Table table = Table.readCSV(configFile,',');
		ColumnReaderString cr_stationID = table.createColumnReader("station");
		ColumnReaderString cr_general = table.createColumnReader("general");

		ColumnReaderFloat cr_lat = table.createColumnReaderFloat("lat");
		ColumnReaderFloat cr_lon = table.createColumnReaderFloat("lon");


		for(String[] row:table.rows) {
			String stationID = cr_stationID.get(row);
			String generalStationName = cr_general.get(row);
			GeneralStation generalStation = tsdb.getGeneralStation(generalStationName);
			if(generalStation==null) {
				log.error("general station not found: "+generalStationName+"  at "+stationID);
				continue;
			}
			String loggerTypeName = generalStationName+"_logger";
			LoggerType loggerType = tsdb.getLoggerType(loggerTypeName);
			if(loggerType==null) {
				log.error("logger type not found: "+loggerTypeName+"  at "+stationID);
				continue;
			}

			Map<String, String> propertyMap = new TreeMap<String, String>();
			propertyMap.put("PLOTID", stationID);
			propertyMap.put("DATE_START","1999-01-01");
			propertyMap.put("DATE_END","2099-12-31");
			StationProperties stationProperties = new StationProperties(propertyMap);			
			ArrayList<StationProperties> propertyList = new ArrayList<StationProperties>();
			propertyList.add(stationProperties);

			Station station = new Station(tsdb, generalStation, stationID, loggerType, propertyList, true);

			try {
				float lat = cr_lat.get(row,true);
				float lon = cr_lon.get(row,true);
				station.geoPosLatitude = lat;
				station.geoPosLongitude = lon;
			} catch(Exception e) {
				log.error(e);
			}

			tsdb.insertStation(station);
		}
	}

	public void readSaOwnPlotInventory(String configFile) {
		Table table = Table.readCSV(configFile,',');
		ColumnReaderString cr_plot = table.createColumnReader("plot");
		ColumnReaderString cr_general = table.createColumnReader("general");		
		ColumnReaderFloat cr_lat = table.createColumnReaderFloat("lat");
		ColumnReaderFloat cr_lon = table.createColumnReaderFloat("lon");

		for(String[] row:table.rows) {
			String plotID = cr_plot.get(row);
			String generalStationName = cr_general.get(row);
			float lat = cr_lat.get(row,true);
			float lon = cr_lon.get(row,true);
			GeneralStation generalStation = tsdb.getGeneralStation(generalStationName);
			if(generalStation==null) {
				log.error("GeneralStation not found "+generalStationName);
				continue;
			}

			float geoPosEasting = Float.NaN;
			float geoPosNorthing = Float.NaN;
			boolean isFocalPlot = false;
			VirtualPlot virtualPlot = new VirtualPlot(tsdb, plotID, generalStation, geoPosEasting, geoPosNorthing, isFocalPlot);
			virtualPlot.geoPosLatitude = lat;
			virtualPlot.geoPosLongitude = lon;
			tsdb.insertVirtualPlot(virtualPlot);
		}

	}

	public void readGenericStationInventory(String configFile) {
		Table table = Table.readCSV(configFile,',');
		ColumnReaderString cr_plot = table.createColumnReader("plot");
		ColumnReaderString cr_logger = table.createColumnReader("logger");
		ColumnReaderString cr_serial = table.createColumnReader("serial");
		ColumnReaderString cr_start = table.createColumnReader("start");
		ColumnReaderString cr_end = table.createColumnReader("end");

		for(String[] row:table.rows) {
			String plotID = cr_plot.get(row);
			String loggerTypeName = cr_logger.get(row);
			String serial = cr_serial.get(row);
			String startText = cr_start.get(row);
			String endText = cr_end.get(row);

			VirtualPlot virtualPlot = tsdb.getVirtualPlot(plotID);
			if(virtualPlot==null) {
				log.error("virtualPlot not found "+plotID);
				continue;
			}

			LoggerType loggerType = tsdb.getLoggerType(loggerTypeName);
			if(loggerType==null) {
				log.error("logger not found "+loggerTypeName);
				continue;
			}

			Map<String, String> propertyMap = new TreeMap<String, String>();
			propertyMap.put(StationProperties.PROPERTY_PLOTID, plotID);
			propertyMap.put(StationProperties.PROPERTY_LOGGER, loggerTypeName);
			propertyMap.put(StationProperties.PROPERTY_SERIAL, serial);
			propertyMap.put(StationProperties.PROPERTY_START,startText);
			propertyMap.put(StationProperties.PROPERTY_END,endText);
			StationProperties stationProperties = new StationProperties(propertyMap);			
			ArrayList<StationProperties> propertyList = new ArrayList<StationProperties>();
			propertyList.add(stationProperties);

			//new Station(tsdb, generalStation, stationID, loggerType, propertyMapList, false);
			Station station = new Station(tsdb,null,serial,loggerType,propertyList, false);
			tsdb.insertStation(station);
			virtualPlot.addStationEntry(station, stationProperties);
		}
	}
	
	public void readBaPlotInventory(String configFile) {
		Table table = Table.readCSV(configFile,',');
		ColumnReaderString cr_plot = table.createColumnReader("plot");
		ColumnReaderString cr_general = table.createColumnReader("general");		
		ColumnReaderFloat cr_easting = table.createColumnReaderFloat("easting");
		ColumnReaderFloat cr_northing = table.createColumnReaderFloat("northing");
		ColumnReaderFloat cr_elevation = table.createColumnReaderFloat("elevation");

		for(String[] row:table.rows) {
			String plotID = cr_plot.get(row);
			String generalStationName = cr_general.get(row);
			float easting = cr_easting.get(row,true);
			float northing = cr_northing.get(row,true);
			float elevation = cr_elevation.get(row,true); 
			GeneralStation generalStation = tsdb.getGeneralStation(generalStationName);
			if(generalStation==null) {
				log.error("GeneralStation not found "+generalStationName);
				continue;
			}

			boolean isFocalPlot = false;
			VirtualPlot virtualPlot = new VirtualPlot(tsdb, plotID, generalStation, easting, northing, isFocalPlot);
			virtualPlot.elevation = elevation;
			tsdb.insertVirtualPlot(virtualPlot);
		}

	}
}
