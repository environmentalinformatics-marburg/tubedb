package tsdb;

import static tsdb.util.AssumptionCheck.throwNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ini4j.Profile.Section;
import org.ini4j.Wini;
import org.json.JSONArray;
import org.json.JSONObject;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Construct;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Tag;

import tsdb.GeneralStation.GeneralStationBuilder;
import tsdb.component.LoggerType;
import tsdb.component.Region;
import tsdb.component.Sensor;
import tsdb.component.labeledproperty.LabeledProperty;
import tsdb.util.Interval;
import tsdb.util.NamedInterval;
import tsdb.util.Pair;
import tsdb.util.Table;
import tsdb.util.Table.ColumnReaderBoolean;
import tsdb.util.Table.ColumnReaderFloat;
import tsdb.util.Table.ColumnReaderString;
import tsdb.util.TimeUtil;
import tsdb.util.Util;
import tsdb.util.yaml.YamlList;
import tsdb.util.yaml.YamlMap;

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

			Section section_general_station_groups = ini.get("general_station_groups"); //******************** [general_station_groups]			
			if(section_general_station_groups != null) {
				for(Entry<String, String> entry:section_general_station_groups.entrySet()) {
					if(creationMap.containsKey(entry.getKey())) {
						creationMap.get(entry.getKey()).group = entry.getValue();
					} else {
						log.warn("general station unknown: "+entry.getKey());
					}
				}
			}

			Section section_general_station_view_time_ranges = ini.get("general_station_view_time_ranges");  //******************** [general_station_view_time_ranges]
			if(section_general_station_view_time_ranges != null) {
				for(Entry<String, String> entry:section_general_station_view_time_ranges.entrySet()) {
					if(creationMap.containsKey(entry.getKey())) {
						String range = entry.getValue();
						Interval interval = Interval.parse(range);
						if(interval != null) {
							if(interval.start>=1900&&interval.start<=2100&&interval.end>=1900&&interval.end<=2100) {
								int startTime = (int) TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(interval.start, 1, 1, 0, 0));
								int endTime = (int) TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(interval.end, 12, 31, 23, 0));
								creationMap.get(entry.getKey()).viewTimeRange = Interval.of(startTime,endTime);
							} else {
								log.warn("general_station_view_time_ranges section invalid year range "+range);
							}
						}


					} else {
						log.warn("general station unknown: "+entry.getKey());
					}
				}
			}
			
			Section section_general_station_plots = ini.get("general_station_assigned_plots"); //******************** [general_station_assigned_plots]
			if(section_general_station_plots != null) {
				for(Entry<String, String> entry : section_general_station_plots.entrySet()) {
					if(creationMap.containsKey(entry.getKey())) {
						String plotsText = entry.getValue();
						//String[] plots = plotsText.split("\\s+");
						String[] plots = plotsText.split(",");
						if(plots.length > 0) {
							GeneralStationBuilder builder = creationMap.get(entry.getKey());
							builder.addAssigned_plots(plots);												
						}
					} else {
						log.warn("general station unknown: "+entry.getKey());
					}
				}
			}

			for(GeneralStationBuilder e:creationMap.values()) {
				tsdb.insertGeneralStation(e.build());
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
		if(!new File(configFile).exists()) {
			log.warn("missing config file: " + configFile);
			return;
		}
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
						Sensor sensor = new Sensor(sensorName);
						sensor.internal = true; // sensors that do not exist in config are marked as internal
						tsdb.insertSensor(sensor);
					}
				}
				tsdb.insertLoggerType(new LoggerType(typeName, sensorNames));
			}
		} catch (Exception e) {
			log.error(e);
		}
	}

	public void readOptinalSensorTranslation(String iniFile) {
		try {
			File file = new File(iniFile);
			if(file.exists()) {
				Wini ini = new Wini(file);
				for(Section section:ini.values()) {
					String sectionName = section.getName();
					int index = sectionName.indexOf("_logger_type_sensor_translation");
					if(index>-1) {
						readLoggerTypeSensorTranslation(sectionName.substring(0,index),section, iniFile);
						continue;
					}
					index = sectionName.indexOf("_generalstation_sensor_translation");
					if(index>-1) {
						readGeneralStationSensorTranslation(sectionName.substring(0,index),section, iniFile);
						continue;
					}
					index = sectionName.indexOf("_station_sensor_translation");
					if(index>-1) {
						readStationSensorTranslation(sectionName.substring(0,index),section, iniFile);
						continue;
					}
					log.warn("section unknown: "+sectionName+"  at "+iniFile);
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
	}

	/**
	 * Read and insert sensor name corrections with time intervals in json format.
	 * @param jsonFile filename
	 */
	public void readOptionalSensorNameCorrection(String jsonFile) {
		Path filename = Paths.get(jsonFile);
		if(!Files.isRegularFile(filename)) {
			//log.error("ConfigJson file not found "+filename);
			//throw new RuntimeException("file not found: "+filename);
			return;
		}
		try {
			String jsonText = Util.removeComments(Files.readAllBytes(filename));
			JSONArray jsonArray = new JSONArray(jsonText);

			final int SIZE = jsonArray.length();
			for (int i = 0; i < SIZE; i++) {
				try {
					JSONObject obj = jsonArray.getJSONObject(i);
					String plotText = obj.getString("plot");
					String rawText = obj.getString("raw");
					String correctText = obj.getString("correct");
					String startText = obj.getString("start");
					String endText = obj.getString("end");

					int start = TimeUtil.parseStartTimestamp(startText);
					int end = TimeUtil.parseEndTimestamp(endText);
					NamedInterval entry = NamedInterval.of(start,end,correctText);

					Station station = tsdb.getStation(plotText);
					if(station==null) {
						log.warn("plot not found "+plotText+" at "+obj+" in "+jsonFile);
						continue;
					}
					if(station.sensorNameCorrectionMap==null) {
						station.sensorNameCorrectionMap = new HashMap<>();
					}
					NamedInterval[] corrections = station.sensorNameCorrectionMap.get(rawText);
					NamedInterval[] new_corrections = Util.addEntryToArray(corrections, entry);
					station.sensorNameCorrectionMap.put(rawText, new_corrections);					
				} catch(Exception e) {
					log.warn(e);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("readSensorNameCorrection ConfigJson file error "+e+"  in "+jsonFile);
			//throw new RuntimeException(e);
		}		
	}

	private void readLoggerTypeSensorTranslation(String loggerTypeName, Section section, String traceText) {
		LoggerType loggerType = tsdb.getLoggerType(loggerTypeName);
		if(loggerType==null) {
			log.error("logger not found: "+loggerTypeName+"  at "+traceText);
			return;
		}
		Map<String, String> translationMap = Util.readIniSectionMap(section);
		for(Entry<String, String> entry:translationMap.entrySet()) {
			if(loggerType.sensorNameTranlationMap.containsKey(entry.getKey())) {
				log.warn("overwriting"+"  at "+traceText);
			}
			if(entry.getKey().equals(entry.getValue())) {
				log.info("redundant entry "+entry+" in "+section.getName()+"  at "+traceText);
			}
			loggerType.sensorNameTranlationMap.put(entry.getKey(), entry.getValue());
		}
	}

	private void readGeneralStationSensorTranslation(String generalStationName, Section section, String traceText) {
		GeneralStation generalStation = tsdb.getGeneralStation(generalStationName);
		if(generalStation==null) {
			log.error("generalStation not found: "+generalStationName+"  at "+traceText);
			return;
		}
		Map<String, String> translationMap = Util.readIniSectionMap(section);
		for(Entry<String, String> entry:translationMap.entrySet()) {
			if(generalStation.sensorNameTranlationMap.containsKey(entry.getKey())) {
				log.warn("overwriting"+"  at "+traceText);
			}
			if(entry.getKey().equals(entry.getValue())) {
				log.info("redundant entry "+entry+" in "+section.getName()+"  at "+traceText);
			}
			generalStation.sensorNameTranlationMap.put(entry.getKey(), entry.getValue());
		}
	}

	private void readStationSensorTranslation(String stationName, Section section, String traceText) {
		Station station = tsdb.getStation(stationName);
		if(station==null) {
			log.error("station not found: "+stationName+"  at "+traceText);
			return;
		}
		Map<String, String> translationMap = Util.readIniSectionMap(section);
		for(Entry<String, String> entry:translationMap.entrySet()) {
			if(station.sensorNameTranlationMap.containsKey(entry.getKey())) {
				log.warn("overwriting"+"  at "+traceText);
			}
			station.sensorNameTranlationMap.put(entry.getKey(), entry.getValue());
		}
	}

	public void calcNearestStations() {
		tsdb.updateGeneralStations();
		for(Station station:tsdb.getStations()) {

			if(!station.isPlot) {
				continue;
			}

			double[] geoPos = transformCoordinates(station.geoPosLongitude,station.geoPosLatitude);
			List<Object[]> distanceList = new ArrayList<Object[]>();

			List<Station> stationList = station.generalStation.stationList;
			//System.out.println(station.plotID+" --> "+stationList);
			for(Station targetStation:stationList) {
				if(station!=targetStation) { // reference compare
					double[] targetGeoPos = transformCoordinates(targetStation.geoPosLongitude,targetStation.geoPosLatitude);
					double distance = getDistance(geoPos, targetGeoPos);
					distanceList.add(new Object[]{distance,targetStation});
				}
			}
			distanceList.sort(new Comparator<Object[]>() {
				@Override
				public int compare(Object[] o1, Object[] o2) {
					double d1 = (double) o1[0];
					double d2 = (double) o2[0];					
					return Double.compare(d1, d2);
				}
			});
			List<Station> targetStationList = new ArrayList<Station>(distanceList.size());
			for(Object[] targetStation:distanceList) {
				targetStationList.add((Station) targetStation[1]);
			}
			station.nearestStations = targetStationList;
			//System.out.println(station.plotID+" --> "+station.nearestStationList);
		}

	}

	public void calcNearestVirtualPlots() {
		tsdb.updateGeneralStations();

		for(VirtualPlot virtualPlot:tsdb.getVirtualPlots()) {
			List<Object[]> distanceList = new ArrayList<Object[]>();

			String group = virtualPlot.generalStation.group;
			List<VirtualPlot> virtualPlots = new ArrayList<VirtualPlot>();
			tsdb.getGeneralStationsOfGroup(group).forEach(gs->virtualPlots.addAll(gs.virtualPlots));

			for(VirtualPlot targetVirtualPlot:virtualPlots) {
				if(virtualPlot!=targetVirtualPlot) {
					double distance = getDistance(virtualPlot, targetVirtualPlot);
					distanceList.add(new Object[]{distance,targetVirtualPlot});
				}
			}
			distanceList.sort(new Comparator<Object[]>() {
				@Override
				public int compare(Object[] o1, Object[] o2) {
					double d1 = (double) o1[0];
					double d2 = (double) o2[0];					
					return Double.compare(d1, d2);
				}
			});

			virtualPlot.nearestVirtualPlots = distanceList.stream().map(o->(VirtualPlot)o[1]).collect(Collectors.toList());
			//System.out.println(virtualPlot.plotID+" --> "+virtualPlot.nearestVirtualPlots);
		}
	}

	public static double[] transformCoordinates(double longitude, double latitude) {
		return new double[]{longitude,latitude};
	}

	public static double getDistance(double[] geoPos, double[] targetGeoPos) {
		return Math.hypot(geoPos[0]-targetGeoPos[0], geoPos[1]-targetGeoPos[1]);
	}

	public static double getDistance(VirtualPlot source, VirtualPlot target) {
		return Math.hypot(source.geoPosEasting-target.geoPosEasting, source.geoPosNorthing-target.geoPosNorthing);
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
	 * read region config
	 * @param configFile
	 * @param justRegion if not null read just this region
	 * @return
	 */
	public Region readRegion(String configFile, String justRegion) {
		try {
			Region region = null;
			Wini ini = new Wini(new File(configFile));

			Section section = ini.get("region");
			if(section!=null) {
				Map<String, String> regionNameMap = Util.readIniSectionMap(section);
				for(Entry<String, String> entry:regionNameMap.entrySet()) {
					String regionName = entry.getKey();
					if(justRegion==null || justRegion.toLowerCase().equals(regionName.toLowerCase())) {
						String regionLongName = entry.getValue();
						region = new Region(regionName, regionLongName);
						tsdb.insertRegion(region);
					}
				}
			} else {
				log.warn("region section not found");
			}

			section = ini.get("region_view_time_range");
			if(section!=null) {
				Map<String, String> regionNameMap = Util.readIniSectionMap(section);
				for(Entry<String, String> entry:regionNameMap.entrySet()) {
					String regionName = entry.getKey();
					if(justRegion==null || justRegion.toLowerCase().equals(regionName.toLowerCase())) {
						String range = entry.getValue();
						Interval interval = Interval.parse(range);
						if(interval!=null) {
							if(interval.start>=1900&&interval.start<=2100&&interval.end>=1900&&interval.end<=2100) {
								int startTime = (int) TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(interval.start, 1, 1, 0, 0));
								int endTime = (int) TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(interval.end, 12, 31, 23, 0));
								Region region1 = tsdb.getRegion(regionName);
								if(region1!=null) {
									region1.viewTimeRange = Interval.of(startTime,endTime);
								} else {
									log.warn("region not found: "+regionName);
								}
							} else {
								log.warn("region_view_time_range section invalid year range "+range);
							}
						}
					}
				}
			} else {
				log.warn("region_view_time_range section not found");
			}

			section = ini.get("region_default_general_station");
			if(section!=null) {
				Map<String, String> defaultGeneralStationNameMap = Util.readIniSectionMap(section);
				for(Entry<String, String> entry:defaultGeneralStationNameMap.entrySet()) {
					String regionName = entry.getKey();
					if(justRegion==null || justRegion.toLowerCase().equals(regionName.toLowerCase())) {
						String name = entry.getValue();
						Region region1 = tsdb.getRegion(regionName);
						if(region1 != null) {
							region1.defaultGeneralStation = name;
						} else {
							log.warn("region not found: "+regionName);
						}
					}
				}
			}

			section = ini.get("region_description");
			if(section!=null) {
				Map<String, String> regionEntryMap = Util.readIniSectionMap(section);
				for(Entry<String, String> entry:regionEntryMap.entrySet()) {
					String regionName = entry.getKey();
					if(justRegion==null || justRegion.toLowerCase().equals(regionName.toLowerCase())) {
						String name = entry.getValue();
						Region region1 = tsdb.getRegion(regionName);
						if(region1 != null) {
							//log.info("len "+regionName+"  "+section.length(regionName));
							List<String> yy = section.getAll(regionName);
							String desc = null;
							for(String y:yy) {
								//log.info("get "+y);
								if(desc == null) {
									desc = y;
								} else {
									desc += '\n' + y;
								}
							}
							if(desc != null) {
								desc = desc.replace("\\n", "\n");
							}
							region1.description = desc;
							//log.info("set description for " + region1.name);
							//log.info(region.description);
						} else {
							log.warn("region not found: "+regionName);
						}
					}
				}
			}

			return region;
		} catch (IOException e) {
			log.warn("error at read region " + configFile + "   " + e);
			e.printStackTrace();
			return null;
		}
	}

	public void readPlotInventory(String configFile) {
		if(!new File(configFile).exists()) {
			log.warn("missing config file: " + configFile);
			return;
		}
		
		Table table = Table.readCSV(configFile,',');
		ColumnReaderString cr_plot = table.createColumnReader("plot");
		ColumnReaderString cr_general = table.createColumnReader("general");
		ColumnReaderBoolean cr_focal = table.createColumnReaderBooleanYN("focal", false);
		ColumnReaderFloat cr_lat = table.createColumnReaderFloat("lat", Float.NaN);
		ColumnReaderFloat cr_lon = table.createColumnReaderFloat("lon", Float.NaN);
		ColumnReaderFloat cr_easting = table.createColumnReaderFloat("easting", Float.NaN);
		ColumnReaderFloat cr_northing = table.createColumnReaderFloat("northing", Float.NaN);
		ColumnReaderFloat cr_elevation = table.createColumnReaderFloat("elevation", Float.NaN);
		ColumnReaderBoolean cr_is_station = table.createColumnReaderBooleanYN("is_station", false); // if plot is station
		ColumnReaderString cr_logger = table.containsColumn("logger")?table.createColumnReader("logger"):cr_general.then(g->g+"_logger"); // only for plots that are stations
		ColumnReaderString cr_alternative_id = table.createColumnReader("alternative_id", null);  // only for plots that are stations

		for(String[] row:table.rows) {
			if(row.length == 1 && (row[0].length() == 0 || row[0].trim().isEmpty() || row[0].trim().startsWith("#"))) { // skip empty rows and comments
				continue;
			}
			String plotID = cr_plot.get(row);
			String generalStationName = cr_general.get(row);
			GeneralStation generalStation = tsdb.getGeneralStation(generalStationName);
			if(generalStation==null) {
				log.error("GeneralStation not found "+generalStationName);
				continue;
			}			
			float lat = cr_lat.get(row,false);
			float lon = cr_lon.get(row,false);
			float easting = cr_easting.get(row,false);
			float northing = cr_northing.get(row,false);
			float elevation = cr_elevation.get(row,false);
			boolean isFocalPlot = cr_focal.get(row);
			boolean is_station = cr_is_station.get(row);
			if(is_station) {
				String alternative_id = cr_alternative_id.get(row);
				String loggerTypeName = cr_logger.get(row);
				LoggerType loggerType = tsdb.getLoggerType(loggerTypeName);
				if(loggerType==null) {
					log.error("logger type not found: "+loggerTypeName+"  at "+plotID);
					log.info(tsdb.getLoggerTypes());
					continue;
				}
				Map<String, String> propertyMap = new TreeMap<String, String>();
				propertyMap.put("PLOTID", plotID);
				propertyMap.put("DATE_START","1999-01-01");
				propertyMap.put("DATE_END","2099-12-31");
				propertyMap.put("TYPE", isFocalPlot?StationProperties.TYPE_VIP:"EP");
				StationProperties stationProperties = new StationProperties(propertyMap);			
				ArrayList<StationProperties> propertyList = new ArrayList<StationProperties>();
				propertyList.add(stationProperties);
				Station station = new Station(tsdb, generalStation, plotID, loggerType, propertyList, true);
				station.geoPosLatitude = lat;
				station.geoPosLongitude = lon;
				if(Float.isFinite(elevation)) {
					station.elevation = elevation;
				}
				tsdb.insertStation(station);
				if(alternative_id != null) {
					station.addAlias(alternative_id);
				}
			} else {
				VirtualPlot virtualPlot = new VirtualPlot(tsdb, plotID, generalStation, easting, northing, isFocalPlot);
				virtualPlot.geoPosLatitude = lat;
				virtualPlot.geoPosLongitude = lon;
				if(Float.isFinite(elevation)) {
					virtualPlot.setElevation(elevation);
				}
				tsdb.insertVirtualPlot(virtualPlot);
			}
		}

	}

	private static final Set<String> usedColumns = new HashSet<String>(){{addAll(Arrays.asList(new String[]{"plot","logger","serial","start","end"}));}};


	public void readOptionalStationInventory(String configFile) {
		File file = new File(configFile);
		if(file.exists()) {
			Table table = Table.readCSV(configFile,',');
			ColumnReaderString cr_plot = table.createColumnReader("plot");
			ColumnReaderString cr_logger = table.createColumnReader("logger");
			ColumnReaderString cr_serial = table.createColumnReader("serial");
			ColumnReaderString cr_start = table.createColumnReader("start", "*");
			ColumnReaderString cr_end = table.createColumnReader("end", "*");

			ColumnReaderString[] cr_properties = Arrays.stream(table.names)
					.filter(name->!usedColumns.contains(name))
					.map(name->table.createColumnReader(name))
					.toArray(ColumnReaderString[]::new);

			Map<String, List<StationProperties>> stationPropertiesListMap = new HashMap<String, List<StationProperties>>();

			for(String[] row:table.rows) {
				if(row.length == 0 || (row.length == 1 && row[0].isEmpty())) {
					continue;
				}
				String plotID = cr_plot.get(row);
				String loggerTypeName = cr_logger.get(row);
				String serial = cr_serial.get(row);
				String startText = cr_start.get(row);
				String endText = cr_end.get(row);

				Map<String, String> propertyMap = new TreeMap<String, String>();
				propertyMap.put(StationProperties.PROPERTY_PLOTID, plotID);
				propertyMap.put(StationProperties.PROPERTY_LOGGER, loggerTypeName);
				propertyMap.put(StationProperties.PROPERTY_SERIAL, serial);
				propertyMap.put(StationProperties.PROPERTY_START,startText);
				propertyMap.put(StationProperties.PROPERTY_END,endText);

				for(ColumnReaderString cr_property:cr_properties) {
					String value = cr_property.get(row);
					if(value!=null && !value.isEmpty()) {
						String key = table.getName(cr_property);
						propertyMap.put(key, value);
					}
				}				

				List<StationProperties> list = stationPropertiesListMap.get(serial);
				if(list==null) {
					list = new ArrayList<StationProperties>();
					stationPropertiesListMap.put(serial, list);
				}
				StationProperties stationProperties = new StationProperties(propertyMap);
				list.add(stationProperties);				
			}

			for(List<StationProperties> list:stationPropertiesListMap.values()) {
				LoggerType firstLoggerType = null;
				List<Pair<VirtualPlot, StationProperties>> virtualPlotEntryList = new ArrayList<Pair<VirtualPlot, StationProperties>>();
				for(StationProperties stationProperties:list) {
					String plotID = stationProperties.get_plotid();
					VirtualPlot virtualPlot = tsdb.getVirtualPlot(plotID);
					if(virtualPlot==null) {
						log.error("virtualPlot not found "+plotID);
						continue;
					}
					String loggerTypeName = stationProperties.get_logger_type_name();
					LoggerType loggerType = tsdb.getLoggerType(loggerTypeName);
					if(loggerType==null) {
						log.error("logger not found "+loggerTypeName);
						continue;
					}
					if(firstLoggerType==null) {
						firstLoggerType = loggerType;
					} else if(firstLoggerType != loggerType) {
						log.error("loggers need to be same type for one station "+firstLoggerType+"  "+loggerType);
						continue;
					}
					virtualPlotEntryList.add(Pair.of(virtualPlot, stationProperties));
				}
				if(!virtualPlotEntryList.isEmpty()) {
					Station station = new Station(tsdb,null,virtualPlotEntryList.get(0).b.get_serial(),firstLoggerType,list, false);
					tsdb.insertStation(station);
					for(Pair<VirtualPlot, StationProperties> pair:virtualPlotEntryList) {
						VirtualPlot virtualPlot = pair.a;
						StationProperties stationProperties = pair.b;
						virtualPlot.addStationEntry(station, stationProperties);
						station.addAlias(stationProperties.get_aliases());
					}
				}
			}
		}
	}

	private static class MyConstructor extends SafeConstructor {
		MyConstructor() {
			Construct stringConstructor = this.yamlConstructors.get(Tag.STR);
			this.yamlConstructors.put(Tag.TIMESTAMP, stringConstructor);
		}
	}

	public void readOptionalStationProperties(String yamlFile) {
		log.trace("read yaml");
		try {
			File file = new File(yamlFile);
			if(file.exists()) {
				Yaml yaml = new Yaml(new MyConstructor());
				InputStream in = new FileInputStream(file);
				Object yamlObject = yaml.load(in);
				YamlList yamlList = new YamlList(yamlObject);
				for(YamlMap entry:yamlList.asMaps()) {
					List<LabeledProperty> properties = LabeledProperty.parse(entry);
					for(LabeledProperty property:properties) {
						tsdb.insertLabeledProperty(property);
					}
				}
			}
		} catch (Exception e) {
			log.error("could not read station properties yaml file: "+e);
		}		
	}

	public void readSensorMetaData(String yamlFile) {
		log.trace("read sensors yaml");
		try {
			File file = new File(yamlFile);
			if(file.exists()) {
				Yaml yaml = new Yaml(new MyConstructor());
				InputStream in = new FileInputStream(file);
				Object yamlObject = yaml.load(in);
				YamlMap yamlMap = YamlMap.ofObject(yamlObject);
				for(String sensorName:yamlMap.keys()) {
					try {
						YamlMap sensorYaml = yamlMap.getMap(sensorName);
						Sensor sensor = Sensor.ofYaml(sensorName, sensorYaml);
						tsdb.insertSensor(sensor);
						//log.info(sensorName+" "+sensor.baseAggregationType);
					} catch(Exception e) {
						log.warn("could not read sensor yaml of: "+sensorName+"   in "+yamlFile);
					}
				}
			} else {
				log.warn("missing sensors yaml file: "+yamlFile);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("could not read sensors yaml file: "+e);
		}		
	}	
}
