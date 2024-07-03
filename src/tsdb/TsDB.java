package tsdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;


import org.tinylog.Logger;

import tsdb.component.LoggerType;
import tsdb.component.Region;
import tsdb.component.Sensor;
import tsdb.component.SourceCatalog;
import tsdb.component.labeledproperty.LabeledProperty;
import tsdb.streamdb.StreamStorageStreamDB;
import tsdb.util.AggregationType;
import tsdb.util.AssumptionCheck;
import tsdb.util.BaseAggregationTimeUtil;
import tsdb.util.Util;

/**
 * This is the main class of the timeseries database.
 * @author woellauer
 *
 */
public class TsDB implements AutoCloseable {
	
	public static final String tubedb_version = "1.31";

	/**
	 * map regionName -> Region
	 */
	private Map<String,Region> regionMap;	

	/**
	 * station/logger type name	->	LoggerType Object
	 * 00CEMU, ...
	 */
	private Map<String,LoggerType> loggerTypeMap;

	/**
	 * plot id	->	Station Object
	 * HEG01, ...
	 */
	private Map<String,Station> stationMap;

	/**
	 * alias names for stations
	 * does include station names
	 */
	private Map<String,Station> stationAliasMap;

	/**
	 * general station name	->	GeneralStation Object
	 * HEG, HEW, ...
	 */
	private Map<String,GeneralStation> generalStationMap;

	/**
	 * sensor name	->	Sensor Object
	 * Ta_200, ...
	 */
	private Map<String,Sensor> sensorMap;

	/**
	 * set of sensor names of input files, that should not be stored in database
	 */
	private Set<String> ignoreSensorNameSet;

	/**
	 * set of sensor name, that should be included in base aggregation processing
	 */
	private Set<String> baseAggregationSensorNameSet;



	private Map<String,VirtualPlot> virtualplotMap;

	//*** begin persistent information ***

	/**
	 * storage of all time series
	 */
	public StreamStorageStreamDB streamStorage;

	public StreamStorageStreamDB streamCache;

	public SourceCatalog sourceCatalog; 

	public final String configDirectory;
	
	public final Set<String> excludeStatusSensorNames;


	//*** end persistent information ***



	/**
	 * create a new TimeSeriesDatabase object and connects to stored database files
	 * @param databasePath
	 * @param cachePath
	 * @param streamdbPathPrefix
	 * @param configDirectory 
	 */
	public TsDB(String databasePath, String cachePath, String streamdbPathPrefix, String configDirectory) {		
		Logger.info("TubeDB v"+ TsDB.tubedb_version);		

		this.regionMap = new TreeMap<String,Region>();

		this.streamStorage = new StreamStorageStreamDB(streamdbPathPrefix);
		loggerTypeMap = new TreeMap<String, LoggerType>();
		stationMap = new TreeMap<String,Station>();
		stationAliasMap = new TreeMap<String,Station>();
		generalStationMap = new TreeMap<String, GeneralStation>();
		sensorMap = new TreeMap<String,Sensor>();
		ignoreSensorNameSet = new TreeSet<String>();
		baseAggregationSensorNameSet = new TreeSet<String>();

		this.streamCache = new StreamStorageStreamDB(streamdbPathPrefix+"__cache");

		this.virtualplotMap = new TreeMap<String, VirtualPlot>();

		this.sourceCatalog = new SourceCatalog(databasePath);

		this.configDirectory = configDirectory;
		
		this.excludeStatusSensorNames = new HashSet<String>(Arrays.asList(new String[] {"precipitation_radolan", "precipitation_dwd_year"}));
	}	

	/**
	 * Clears all database files.
	 */
	public void clear() {
		streamCache.clear();
		sourceCatalog.clear();		
		streamStorage.clear();
	}

	/**
	 * Close database. All pending data is written to disk.
	 * 
	 */
	@Override
	public void close() {
		try {
			streamStorage.close();
		} catch(Exception e) {
			Logger.error("error in streamStorage.close: "+e);
		}
		try {
			streamCache.close();
		}  catch(Exception e) {
			Logger.error("error in streamCache.close: "+e);
		}
		try {
			sourceCatalog.close();
		} catch(Exception e) {
			Logger.error("error in sourceCatalog.close: "+e);
		}
	}	

	public Float[] getEmpiricalDiff(String[] schema) {
		Float[] diff = new Float[schema.length];
		for(int i=0;i<schema.length;i++) {
			Sensor sensor = sensorMap.get(schema[i]);
			if(sensor!=null) {
				diff[i] = sensor.empiricalDiff;
			} else {
				throw new RuntimeException("sensor not found: "+schema[i]);
			}
		}
		return diff;
	}

	/**
	 * Transform array of sensor names to array of sensor objects.
	 * Complement missing sensors by new raw sensor objects.
	 * @param outputTimeSeriesSchema
	 * @return
	 */
	public Sensor[] getSensors(String[] names) {
		return getSensors(names, true);
	}


	/**
	 * Transform array of sensor names to array of sensor objects.
	 * @param names
	 * @param createMissing complement missing sensors by new raw sensor objects or set entry to null
	 * @return
	 */
	public Sensor[] getSensors(String[] names, boolean createMissing) {
		Sensor[] sensors = new Sensor[names.length];
		for(int i=0;i<names.length;i++) {
			sensors[i] = sensorMap.get(names[i]);
			if(sensors[i]==null) {
				Logger.warn("sensor "+names[i]+" not found");
				if(createMissing) {
					sensors[i] = new Sensor(names[i]);
					sensors[i].internal = true; // sensors that do not exist in config are marked as internal
				}
			}
		}
		return sensors;
	}

	/**
	 * Get stream of sensors.
	 * @param names
	 * @return stream of sensors (or null elements if sensor not exists).
	 */
	public Stream<Sensor> getSensorStream(String[] names) {
		AssumptionCheck.throwNullArray(names);
		return Arrays.stream(names).map(name->sensorMap.get(name));
	}

	public void updateGeneralStations() {

		for(GeneralStation g:getGeneralStations()) {
			g.stationList = new ArrayList<Station>();
			g.virtualPlots = new ArrayList<VirtualPlot>();
		}

		for(Station station:getStations()) {
			if(station.generalStation != null) {
				GeneralStation generalStation = station.generalStation;
				if(generalStation!=null) {
					generalStation.stationList.add(station);
				} else {
					Logger.warn("no general station in "+station.stationID);
				}
			}
		}

		for(VirtualPlot virtualplot:virtualplotMap.values()) {
			if(virtualplot.generalStation != null) {
				virtualplot.generalStation.virtualPlots.add(virtualplot);
			} else {
				Logger.warn("no general station in "+virtualplot.plotID);
			}

		}

		for(GeneralStation g:getGeneralStations()) {
			if(g.assigned_plots != null) {
				for(String plotName : g.assigned_plots) {
					VirtualPlot virtualplot = getVirtualPlot(plotName);
					if(virtualplot != null) {
						if( !g.virtualPlots.contains(virtualplot)) {
							g.virtualPlots.add(virtualplot);
						}
					} else {
						Station station = getStation(plotName);
						if(station != null) {
							if( !g.stationList.contains(station)) {
								g.stationList.add(station);
							}
						}
					}
				}
			}
		}
	}

	//*********************************************** begin Station *************************************************

	public Station getStation(String stationName) {
		return stationMap.get(stationName);		
	}

	public Station getStationWithAlias(String stationName) {
		return stationAliasMap.get(stationName);		
	}

	public Collection<Station> getStations() {
		return stationMap.values();
	}

	public void refresStationAliasMap() {
		stationAliasMap.clear();
		stationAliasMap.putAll(stationMap);
		for(Station station:getStations()) {
			for(String alias : station.getAliases()) {
				if(stationAliasMap.containsKey(alias)) {
					Logger.warn("alias already in map: "+alias +" with "+stationAliasMap.get(alias).stationID+" overwrite with "+station.stationID);
				}
				stationAliasMap.put(alias, station);
			}
		}
	}

	public Set<String> getStationNames() {
		return stationMap.keySet();
	}

	public boolean stationExists(String stationName) {
		return stationMap.containsKey(stationName);
	}

	public boolean stationExistsWithAlias(String stationName) {
		return stationAliasMap.containsKey(stationName);
	}

	public void insertStation(Station station, String originConfigFile) {
		if(stationMap.containsKey(station.stationID)) {
			Logger.warn("override station (already exists): " + station.stationID + "  in " + originConfigFile);
		}
		stationMap.put(station.stationID, station);
	}

	public void replaceStation(Station station) {
		if(stationMap.containsKey(station.stationID)) {
			stationMap.put(station.stationID, station);
		} else {
			Logger.warn("station does not exist already no insert: "+station.stationID);			
		}
	}

	/**
	 * gets first and last timestamp of virtualplot or station
	 * @param stationName
	 * @return null if empty
	 */
	public long[] getTimeInterval(String stationName) {
		VirtualPlot virtualPlot = getVirtualPlot(stationName);
		if(virtualPlot!=null) {
			return virtualPlot.getTimeInterval();
		}
		return streamStorage.getStationTimeInterval(stationName);
	}
	
	/**
	 * Gets first and last timestamp of virtualplot or station.
	 * Excluding sensor names that are not relevant for status of measuring stations.
	 * @param stationName
	 * @return null if empty
	 */
	public long[] getStatusTimeInterval(String stationName) {
		VirtualPlot virtualPlot = getVirtualPlot(stationName);
		if(virtualPlot!=null) {
			return virtualPlot.getTimeInterval(excludeStatusSensorNames);
		}
		return streamStorage.getStationTimeInterval(stationName, excludeStatusSensorNames);
	}

	/**
	 * gets first and last timestamp of virtualplot or station
	 * @param stationName
	 * @return null if empty
	 */
	public long[] getBaseTimeInterval(String stationName) {
		long[] interval = getTimeInterval(stationName);
		if(interval==null) {
			return null;
		}
		return new long[]{BaseAggregationTimeUtil.alignQueryTimestampToBaseAggregationTime(interval[0]),BaseAggregationTimeUtil.alignQueryTimestampToBaseAggregationTime(interval[1])};
	}

	//*********************************************** end Station *************************************************

	//*********************************************** begin GeneralStation *************************************************

	public boolean generalStationExists(String generalStationName) {
		return generalStationMap.containsKey(generalStationName);
	}

	public void insertGeneralStation(GeneralStation generalStation, String originConfigFile) {
		//Logger.info("insert: " + generalStation);
		if(generalStationExists(generalStation.name)) {
			Logger.warn("override general station (already exists): " + generalStation.name + "  in " + originConfigFile);
		}
		generalStationMap.put(generalStation.name, generalStation);
	}

	public Collection<GeneralStation> getGeneralStations() {
		return generalStationMap.values();
	}

	public Stream<GeneralStation> getGeneralStationsOfGroup(String group) {
		return generalStationMap.values().stream().filter(gs -> group.equals(gs.group));
	}

	public GeneralStation getGeneralStation(String generalStationName) {
		return generalStationMap.get(generalStationName);
	}

	public GeneralStation getGeneralStationByLongName(String longName) {
		for(GeneralStation generalStaion:generalStationMap.values()) {
			if(generalStaion.longName.equals(longName)) {
				return generalStaion;
			}
		}
		return null;
	}

	public String[] getGeneralStationNames() {
		return generalStationMap.keySet().stream().toArray(String[]::new);
	}

	public Stream<GeneralStation> getGeneralStationsByRegion(String regionName) {
		return generalStationMap.values().stream().filter(generalStation -> {
			if(generalStation.region == null) {
				Logger.warn("missing region for " + generalStation );
				return false;
			}
			return regionName.equals(generalStation.region.name);
		});
	}

	public Set<String> getGeneralStationGroups() {
		Set<String> set = new TreeSet<String>();
		getGeneralStations().forEach(gs->set.add(gs.group));
		return set;
	}

	public Stream<String> getStationAndVirtualPlotNames(String group) {		
		return getGeneralStationsOfGroup(group).flatMap(gs->gs.getStationAndVirtualPlotNames());
	}

	public Stream<String> getPlotNames() {
		Stream<String> stationStream = stationMap.values().stream().filter(s->s.isPlot).map(s->s.stationID);
		Stream<String> virtualPlotStream = virtualplotMap.keySet().stream();
		return Stream.concat(stationStream,virtualPlotStream);
	}

	//*********************************************** end GeneralStation *************************************************

	//*********************************************** begin Sensor *******************************************************

	public boolean sensorExists(String sensorName) {
		return sensorMap.containsKey(sensorName);
	}

	public void insertSensor(Sensor sensor) {
		if(sensorExists(sensor.name)) {
			Logger.warn("override sensor (already exists): "+sensor.name);
		}
		sensorMap.put(sensor.name, sensor);
		if(sensor.isAggregable()) {
			baseAggregationSensorNameSet.add(sensor.name);
		}
	}

	public Sensor getSensor(String sensorName) {
		return sensorMap.get(sensorName);
	}

	@Deprecated
	public Sensor getOrCreateSensor(String sensorName) { // not used
		Sensor sensor = sensorMap.get(sensorName);
		if(sensor==null) {
			sensor = new Sensor(sensorName);
			sensor.internal = true; // sensors that do not exist in config are marked as internal
			insertSensor(sensor);
			return sensor;
		} else {
			return sensor;
		}
	}

	public Collection<Sensor> getSensors() {
		return sensorMap.values();
	}
	
	public HashSet<String> getSensorDependencySources(String sensorName) {
		HashSet<String> collector = new HashSet<String>();
		HashSet<String> visitied = new HashSet<String>();
		getSensorDependencySources(sensorName, collector, visitied);
		return collector;
	}
	
	public void getSensorDependencySources(String sensorName, Set<String> collector, Set<String> visited) {
		visited.add(sensorName);
		Sensor sensor = sensorMap.get(sensorName);
		if(sensor == null || sensor.dependency == null) {
			collector.add(sensorName);
		} else {
			for(String dependency:sensor.dependency) {
				if(!visited.contains(dependency)) {
					getSensorDependencySources(dependency, collector, visited);
				}
			}
		}
	}

	//*********************************************** end Sensor **********************************************************

	//*********************************************** begin LoggerType *******************************************************

	public boolean loggerTypeExists(String loggerTypeName) {
		return loggerTypeMap.containsKey(loggerTypeName);
	}

	public void insertLoggerType(LoggerType loggertype, String originConfigFile) {
		if(loggerTypeExists(loggertype.typeName)) {
			Logger.warn("override logger type (already exists): " + loggertype.typeName + "  in " + originConfigFile);
		}
		loggerTypeMap.put(loggertype.typeName, loggertype);
	}

	public LoggerType getLoggerType(String loggerTypeName) {
		return loggerTypeMap.get(loggerTypeName);
	}

	public Collection<LoggerType> getLoggerTypes() {
		return loggerTypeMap.values();
	}

	//*********************************************** begin VirtualPlot *******************************************************

	public boolean virtualPlotExists(String plotID) {
		return virtualplotMap.containsKey(plotID);
	}

	public void insertVirtualPlot(VirtualPlot virtualPlot, String originConfigFile) {
		if(virtualPlotExists(virtualPlot.plotID)) {
			Logger.warn("overwrite virtual plot (already exists): " + virtualPlot.plotID + "  in " + originConfigFile);
		}
		virtualplotMap.put(virtualPlot.plotID, virtualPlot);
	}

	public VirtualPlot getVirtualPlot(String plotID) {
		return virtualplotMap.get(plotID);
	}

	public Collection<VirtualPlot> getVirtualPlots() {
		return virtualplotMap.values();
	}

	//*********************************************** end VirtualPlot *********************************************************

	//*********************************************** begin ignore sensor names *******************************************************

	public boolean containsIgnoreSensorName(String sensorName) {
		return ignoreSensorNameSet.contains(sensorName);
	}

	public void insertIgnoreSensorName(String sensorName) {
		if(containsIgnoreSensorName(sensorName)) {
			Logger.warn("sensor name already ignored: "+sensorName);
		}
		ignoreSensorNameSet.add(sensorName);
	}

	//*********************************************** end ignore sensor names *******************************************************




	//*********************************************** begin Region *******************************************************************

	public boolean regionExists(String regionName) {
		return regionMap.containsKey(regionName);
	}

	public void insertRegion(Region region) {
		if(regionExists(region.name)) {
			Logger.warn("overwrite region (already exists): "+region.name);
			//new Throwable().printStackTrace();
		}
		regionMap.put(region.name, region);
	}

	public Collection<Region> getRegions() {
		return regionMap.values();
	}

	public Set<String> getRegionNames() {
		return regionMap.keySet();
	}

	public Stream<String> getRegionLongNames() {
		return regionMap.values().stream().map(x -> x.longName);
	}

	/**
	 * slow method
	 * @param longName
	 * @return 
	 */
	public Region getRegionByLongName(String longName) {
		for(Region region:regionMap.values()) {
			if(region.longName.equals(longName)) {
				return region;
			}
		}
		return null;
	}

	public Region getRegion(String regionName) {
		return regionMap.get(regionName);
	}



	public String[] getGeneralStationLongNames(String regionName) {
		return getGeneralStationsByRegion(regionName).map(x -> x.longName).sorted().toArray(String[]::new);
	}

	//*********************************************** end Region *************************************************************************


	//*********************************************** begin base aggregation *************************************************************************

	public boolean baseAggregationExists(String sensorName) {
		return baseAggregationSensorNameSet.contains(sensorName);
	}

	@Deprecated
	public void insertRawSensor(String sensorName) { // not used
		Sensor sensor = getSensor(sensorName);
		if(sensor==null) {
			Logger.trace("created new sensor "+sensorName);
			sensor = new Sensor(sensorName);
			sensor.internal = true; // sensors that do not exist in config are marked as internal
			insertSensor(sensor);
		}			
		if(baseAggregationExists(sensorName)) {
			Logger.error("base aggregation for raw exists: "+sensorName);
		}
		sensor.setAllAggregations(AggregationType.NONE);		
	}

	public String[] getBaseSchema(String[] rawSchema) {
		ArrayList<String> sensorNames = new ArrayList<String>();
		for(String name:rawSchema) {
			if(this.baseAggregationExists(name)) {
				sensorNames.add(name);
			}
		}
		if(sensorNames.isEmpty()) {
			return null;
		}
		return sensorNames.toArray(new String[0]);
	}

	public Set<String> getBaseAggregationSensorNames() {
		return baseAggregationSensorNameSet;
	}

	public boolean isBaseSchema(String[] schema) {
		for(String sensorName:schema) {
			if(!baseAggregationSensorNameSet.contains(sensorName)) {
				return false;
			}
		}
		return true;
	}

	//*********************************************** end base aggregation *************************************************************************

	public String[] getSensorNamesOfPlot(String plotID) {
		VirtualPlot virtualPlot = getVirtualPlot(plotID);
		if(virtualPlot!=null) {
			return virtualPlot.getSensorNames();
		}
		Station station = getStation(plotID);
		if(station!=null) {
			return station.getSensorNames();
		}		
		String[] parts = plotID.split(":"); // structure plotID:stationID
		if(parts.length!=2) {
			throw new RuntimeException("plotID not found: "+plotID);
		}
		station = getStation(parts[1]);
		if(station!=null) {
			return station.getSensorNames();
		}

		return null;
	}


	public String[] getValidSchema(String plotID, String[] schema) {
		VirtualPlot virtualPlot = getVirtualPlot(plotID);
		if(virtualPlot!=null) {
			return virtualPlot.getValidSchemaEntries(schema);
		}
		Station station = getStation(plotID);
		if(station!=null) {
			return station.getValidSchemaEntries(schema);
		}		
		String[] parts = plotID.split(":"); // structure plotID:stationID
		if(parts.length!=2) {
			throw new RuntimeException("plotID not found: "+plotID);
		}
		station = getStation(parts[1]);
		if(station!=null) {
			return station.getValidSchemaEntries(schema);
		}

		throw new RuntimeException("plotID not found: "+plotID);
	}

	public String[] getValidSchemaWithVirtualSensors(String plotID, String[] schema) {
		VirtualPlot virtualPlot = getVirtualPlot(plotID);
		if(virtualPlot!=null) {
			return virtualPlot.getValidSchemaEntriesWithVirtualSensors(schema);
		}
		Station station = getStation(plotID);
		if(station!=null) {
			return station.getValidSchemaEntriesWithVirtualSensors(schema);
		}		
		String[] parts = plotID.split(":"); // structure plotID:stationID
		if(parts.length!=2) {
			throw new RuntimeException("plotID not found: "+plotID);
		}
		station = getStation(parts[1]);
		if(station!=null) {
			return station.getValidSchemaEntriesWithVirtualSensors(schema);
		}

		throw new RuntimeException("plotID not found: "+plotID);
	}

	public boolean isValidSchema(String plotID, String[] schema) {

		VirtualPlot virtualPlot = getVirtualPlot(plotID);
		if(virtualPlot!=null) {
			return virtualPlot.isValidSchema(schema);
		}
		Station station = getStation(plotID);
		if(station!=null) {
			return station.isValidSchema(schema);
		}
		throw new RuntimeException("plotID not found: "+plotID);
	}

	public boolean isValidSchemaWithVirtualSensors(String plotID, String[] schema) {
		VirtualPlot virtualPlot = getVirtualPlot(plotID);
		if(virtualPlot!=null) {
			return virtualPlot.isValidSchemaWithVirtualSensors(schema);
		}
		Station station = getStation(plotID);
		if(station!=null) {
			return station.isValidSchemaWithVirtualSensors(schema);
		}
		throw new RuntimeException("plotID not found: "+plotID);
	}

	/**
	 * Get an array of reference values of sensors at plotID.
	 * @param plotID
	 * @param schema
	 * @return
	 */
	public float[] getReferenceValues(String plotID, String[] schema) {
		float[] result = new float[schema.length];
		for(int i=0;i<result.length;i++) {
			result[i] = 0f;
		}
		VirtualPlot virtualPlot = getVirtualPlot(plotID);
		if(virtualPlot!=null) {
			for(int i=0;i<schema.length;i++) {
				if(schema[i].equals("Ta_200")) {
					result[i] = virtualPlot.elevationTemperature;
				}
				if(schema[i].equals("Ta_200_min")) {
					result[i] = virtualPlot.elevationTemperature;
				}
				if(schema[i].equals("Ta_200_max")) {
					result[i] = virtualPlot.elevationTemperature;
				}
			}
		}
		//Logger.info("ref " + Arrays.toString(result));
		return result;
	}

	/**
	 * add appropriate virtual sensors to given schema
	 * @param schema (nullable) (if null returns null)
	 * @return expanded schema (nullable)
	 */
	public String[] includeVirtualSensorNames(String[] schema) {
		if(schema==null) {
			return null;
		}
		LinkedHashSet<String> additionalSensorNames = new LinkedHashSet<>(); // no duplicates
		LinkedHashSet<String> allSensorNames = new LinkedHashSet<>(Arrays.asList(schema)); // no duplicates

		int prevAddSize = -1;

		while(prevAddSize != additionalSensorNames.size()) {
			prevAddSize = additionalSensorNames.size();

			for(VirtualCopyList p:raw_copy_lists) { // one source need to be contained
				innerLoop: for(String source:p.sources) {
					if(Util.containsWithRef(allSensorNames, source) && !allSensorNames.contains(p.target)) {
						additionalSensorNames.add(p.target);
						allSensorNames.add(p.target);
						break innerLoop;
					}
				}
			}

			for(VirtualCopyList p:sensor_dependency_lists) { // all sources need to be contained
				boolean satisfied = true;
				innerLoop: for(String source:p.sources) {
					if(!Util.containsWithRef(allSensorNames, source)) {
						satisfied = false;
						break innerLoop;
					}
				}
				if(satisfied && !allSensorNames.contains(p.target)) {
					additionalSensorNames.add(p.target);
					allSensorNames.add(p.target);
				}
			}
		}

		if(additionalSensorNames.isEmpty()) {
			return schema;
		} else {
			return Stream.concat(Arrays.stream(schema), additionalSensorNames.stream()).toArray(String[]::new);			
		}
	}



	/**
	 * Add sensors that are needed for virtual sensor processing in schema
	 * @param schema with virtual sensors
	 * @param availableSchema available sensors
	 * @return schema + additional sensors 
	 */
	public String[] supplementSchema(String[] schema, String[] availableSchema) {
		LinkedHashSet<String> schemaSet = new LinkedHashSet<>(Util.stringArrayToMap(schema).keySet());
		LinkedHashSet<String> availableSchemaSet = new LinkedHashSet<>(Util.stringArrayToMap(availableSchema).keySet());
		LinkedHashSet<String> additionalSensorNames = new LinkedHashSet<>(); // no duplicates

		int prevAddSize = -1;

		while(prevAddSize != additionalSensorNames.size()) {
			prevAddSize = additionalSensorNames.size();

			for(VirtualCopyList list:raw_copy_lists) {
				if(schemaSet.contains(list.target)) {
					boolean found = false;
					sources: for(String sensorName:list.sources) {
						if(schemaSet.contains(sensorName)) {
							found = true;
							break sources;
						} else if(Util.containsWithRef(availableSchemaSet, sensorName)){
							additionalSensorNames.add(sensorName);
							schemaSet.add(sensorName);
							found = true;
							break sources;
						}
					}
					if(!found) {
						Logger.warn("no source for target "+list.target);
					}
				}
			}

			for(VirtualCopyList list:sensor_dependency_lists) {
				if(schemaSet.contains(list.target)) {
					for(String sensorName:list.sources) {
						if(schemaSet.contains(sensorName)) {
							// nothing
						} else if(Util.containsWithRef(availableSchemaSet, sensorName)){
							additionalSensorNames.add(sensorName);
							schemaSet.add(sensorName);
						} else {
							Logger.warn("dependency for "+list.target+" not found "+sensorName+"  in schema "+Arrays.toString(schema)+" of full   "+Arrays.toString(availableSchema));
						}
					}
				}
			}

			if(additionalSensorNames.size()>prevAddSize) {
				Logger.trace("round "+additionalSensorNames.size()+"  "+Arrays.toString(schema)+" -> "+schemaSet);
			}
		}

		if(additionalSensorNames.isEmpty()) {
			return schema;
		} else {
			return Stream.concat(Arrays.stream(schema), additionalSensorNames.stream()).toArray(String[]::new);			
		}
	}

	public String[] supplementSchema(String plotID, String[] schema) {
		schema = supplementSchema(schema, getSensorNamesOfPlotWithVirtual(plotID));
		return schema;
	}

	public Plot getPlot(String plotID) {
		VirtualPlot virtualPlot = getVirtualPlot(plotID);
		if(virtualPlot!=null) {
			return Plot.of(virtualPlot);
		}
		Station station = getStation(plotID);
		if(station!=null) {
			return Plot.of(station);
		}
		return null;
	}

	public void insertLabeledProperty(LabeledProperty property) {
		Station station = getStation(property.station);
		if(station==null) {
			Logger.warn("station not found property not inserted: "+property);
		} else {
			station.labeledProperties.insert(property);
		}
	}

	public String[] getSensorNamesOfPlotWithVirtual(String plotID) {
		if(plotID==null) {
			Logger.warn("plotID null");
			return null;
		}
		int sep = plotID.indexOf(':');
		if(sep>0) {
			String stationID = plotID.substring(sep+1);
			Logger.info("stationID "+stationID);
			Station station = getStation(stationID);
			if(station!=null) {
				return includeVirtualSensorNames(station.getSensorNames());

			}
		}
		VirtualPlot virtualPlot = getVirtualPlot(plotID);
		if(virtualPlot!=null) {
			return includeVirtualSensorNames(virtualPlot.getSensorNames());
		}
		Station station = getStation(plotID);
		if(station!=null) {
			return includeVirtualSensorNames(station.getSensorNames());

		}
		Logger.warn("plotID not found "+plotID);
		return null;
	}

	//**********  sensor dependency management    *********

	public void createSensorDependencies() {

		ArrayList<VirtualCopyList> raw_copy_list_list = new ArrayList<>();
		ArrayList<VirtualCopyList> sensor_dependency_list_list = new ArrayList<>();

		for(Sensor sensor:sensorMap.values()) {
			String[] source = sensor.raw_source;
			if(source != null) {
				if(source.length == 0) {
					Logger.warn("source empty");
				} else {
					//Logger.info("raw_source "+Arrays.toString(sensor.raw_source)+" -> "+sensor.name);
					raw_copy_list_list.add(VirtualCopyList.of(sensor.raw_source, sensor.name));
				}
			}
			String[] dependency = sensor.dependency;
			if(dependency != null) {
				if(dependency.length == 0) {
					Logger.warn("dependency empty");
				} else {
					//Logger.info("dependency "+Arrays.toString(sensor.dependency)+" -> "+sensor.name);
					sensor_dependency_list_list.add(VirtualCopyList.of(sensor.dependency, sensor.name));
				}
			}
		}

		raw_copy_lists = raw_copy_list_list.toArray(new VirtualCopyList[0]);
		sensor_dependency_lists = sensor_dependency_list_list.toArray(new VirtualCopyList[0]);

		ArrayList<String> list = new ArrayList<>();
		for(VirtualCopyList p:raw_copy_lists) {
			list.add(p.target);
		}
		raw_copy_sensor_names = list.toArray(new String[0]);

	}

	/**
	 * copy first found of sources to target 
	 */
	public VirtualCopyList[] raw_copy_lists = {};

	/**
	 * sources that are needed for target
	 */
	public VirtualCopyList[] sensor_dependency_lists = {};

	/**
	 * sensors that have raw source sensor
	 */
	public String[] raw_copy_sensor_names;


	/**
	 * Order sensor names by transitive dependencies, sensors without dependencies first.
	 * 
	 * @param schema
	 * @return
	 */
	public Sensor[] order_by_dependency(String[] schema) {
		Sensor[] sensors = this.getSensors(schema);
		ArrayList<Sensor> waitingSensors = new ArrayList<Sensor>();
		waitingSensors.addAll(Arrays.asList(sensors));
		LinkedHashSet<String> includedSensors = new LinkedHashSet<String>();
		boolean changed = true;
		while(changed) {
			changed = false;
			Iterator<Sensor> it = waitingSensors.iterator();
			while(it.hasNext()) {
				Sensor sensor = it.next();
				boolean valid = true;
				if(sensor.dependency != null) {
					for(String dep:sensor.dependency) {
						if(!dep.equals(sensor.name) && !includedSensors.contains(dep)) {
							valid = false;
						}
					}
				}
				if(valid) {
					it.remove();
					includedSensors.add(sensor.name);
					changed = true;
				}
			}
		}
		if(!waitingSensors.isEmpty()) {
			Logger.warn("dependencies not fulfilled for "+Arrays.toString(waitingSensors.stream().map(s->s.name).toArray())+"  in  "+Arrays.toString(schema));
			for(Sensor s:waitingSensors) {
				includedSensors.add(s.name);
			}
		}
		//Logger.info("sort "+Arrays.toString(schema)+"   ->   "+includedSensors.toString());
		return this.getSensors(includedSensors.toArray(new String[0]));
	}	
}
