package tsdb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.component.LoggerType;
import tsdb.component.Region;
import tsdb.component.Sensor;
import tsdb.component.SourceCatalog;
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
	private static final Logger log = LogManager.getLogger();

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


	//*** end persistent information ***



	/**
	 * create a new TimeSeriesDatabase object and connects to stored database files
	 * @param databasePath
	 * @param evenstoreConfigFile
	 * @param streamdbPathPrefix 
	 */
	public TsDB(String databasePath, String cachePath, String streamdbPathPrefix) {		
		log.info("open tsdb...");		

		this.regionMap = new TreeMap<String,Region>();

		this.streamStorage = new StreamStorageStreamDB(streamdbPathPrefix);
		loggerTypeMap = new TreeMap<String, LoggerType>();
		stationMap = new TreeMap<String,Station>();
		generalStationMap = new TreeMap<String, GeneralStation>();
		sensorMap = new TreeMap<String,Sensor>();
		ignoreSensorNameSet = new TreeSet<String>();
		baseAggregationSensorNameSet = new TreeSet<String>();

		this.streamCache = new StreamStorageStreamDB(streamdbPathPrefix+"__cache");

		this.virtualplotMap = new TreeMap<String, VirtualPlot>();

		this.sourceCatalog = new SourceCatalog(databasePath);
	}	

	/**
	 * clears all stream data in EventStore; deletes all database files
	 */
	public void clear() {
		streamCache.clear();
		sourceCatalog.clear();		
		streamStorage.clear();
	}

	/**
	 * close EventStore, all pending stream data is written to disk
	 */
	@Override
	public void close() {
		try {
			streamStorage.close();
		} catch(Exception e) {
			log.error("error in streamStorage.close: "+e);
		}
		try {
			streamCache.close();
		}  catch(Exception e) {
			log.error("error in streamCache.close: "+e);
		}
		try {
			sourceCatalog.close();
		} catch(Exception e) {
			log.error("error in sourceCatalog.close: "+e);
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
	 * get array of Sensor objects with given sensor names
	 * @param outputTimeSeriesSchema
	 * @return
	 */
	public Sensor[] getSensors(String[] names) {
		Sensor[] sensors = new Sensor[names.length];
		for(int i=0;i<names.length;i++) {
			Sensor sensor = sensorMap.get(names[i]);
			sensors[i] = sensor;
			if(sensor==null) {
				log.warn("sensor "+names[i]+" not found");
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
			if(station.generalStation!=null) {
				GeneralStation generalStation = station.generalStation;
				if(generalStation!=null) {
					generalStation.stationList.add(station);
				} else {
					log.warn("no general station in "+station.stationID);
				}
			}
		}

		for(VirtualPlot virtualplot:virtualplotMap.values()) {
			if(virtualplot.generalStation!=null) {
				virtualplot.generalStation.virtualPlots.add(virtualplot);
			} else {
				log.warn("no general station in "+virtualplot.plotID);
			}

		}
	}

	//*********************************************** begin Station *************************************************

	public Station getStation(String stationName) {
		return stationMap.get(stationName);		
	}

	public Collection<Station> getStations() {
		return stationMap.values();
	}

	public Set<String> getStationNames() {
		return stationMap.keySet();
	}

	public boolean stationExists(String stationName) {
		return stationMap.containsKey(stationName);
	}

	public void insertStation(Station station) {
		if(stationMap.containsKey(station.stationID)) {
			log.warn("override station (already exists): "+station.stationID);
		}
		stationMap.put(station.stationID, station);
	}

	/**
	 * gets first and last timestamp of virtualplot or station
	 * @param stationName
	 * @return null if empty
	 */
	public long[] getTimeInterval(String stationName) {
		VirtualPlot virtualPlot = getVirtualPlot(stationName);
		if(virtualPlot!=null) {
			return virtualPlot.getTimestampInterval();
		}
		/*Iterator<Event> it = streamStorage.queryRawEvents(stationName, null, null);
		if(it==null || !it.hasNext()) {
			return null;
		}
		long start = it.next().getTimestamp();
		long end = start;
		while (it.hasNext()) {
			end = it.next().getTimestamp();
		}
		return new long[]{start,end};*/
		return streamStorage.getStationTimeInterval(stationName);
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

	public void insertGeneralStation(GeneralStation generalStation) {
		if(generalStationExists(generalStation.name)) {
			log.warn("override general station (already exists): "+generalStation.name);
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

	public Stream<GeneralStation> getGeneralStations(String regionName) {
		return generalStationMap.values().stream().filter(x->regionName.equals(x.region.name));
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
			log.warn("override sensor (already exists): "+sensor.name);
		}
		sensorMap.put(sensor.name, sensor);
	}

	public Sensor getSensor(String sensorName) {
		return sensorMap.get(sensorName);
	}
	
	public Sensor getOrCreateSensor(String sensorName) {
		Sensor sensor = sensorMap.get(sensorName);
		if(sensor==null) {
			sensor = new Sensor(sensorName);
			insertSensor(sensor);
			return sensor;
		} else {
			return sensor;
		}
	}

	public Collection<Sensor> getSensors() {
		return sensorMap.values();
	}

	//*********************************************** end Sensor **********************************************************

	//*********************************************** begin LoggerType *******************************************************

	public boolean loggerTypeExists(String loggerTypeName) {
		return loggerTypeMap.containsKey(loggerTypeName);
	}

	public void insertLoggerType(LoggerType loggertype) {
		if(loggerTypeExists(loggertype.typeName)) {
			log.warn("override logger type (already exists): "+loggertype.typeName);
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

	public void insertVirtualPlot(VirtualPlot virtualPlot) {
		if(virtualPlotExists(virtualPlot.plotID)) {
			log.warn("overwrite virtual plot (already exists): "+virtualPlot.plotID);
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
			log.warn("sensor name already ignored: "+sensorName);
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
			log.warn("overwrite region (already exists): "+region.name);
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
		return getGeneralStations(regionName).map(x -> x.longName).sorted().toArray(String[]::new);
	}

	//*********************************************** end Region *************************************************************************


	//*********************************************** begin base aggregation *************************************************************************

	public boolean baseAggregationExists(String sensorName) {
		return baseAggregationSensorNameSet.contains(sensorName);
	}

	public void insertBaseAggregation(String sensorName, AggregationType aggregateType) {
		Sensor sensor = getSensor(sensorName);
		if(sensor==null) {
			log.trace("created new sensor "+sensorName);
			sensor = new Sensor(sensorName);
			insertSensor(sensor);
		}			
		if(baseAggregationExists(sensorName)) {
			log.warn("base aggregation already exists: "+sensorName);
		} else {
			baseAggregationSensorNameSet.add(sensorName);
		}
		sensor.baseAggregationType = aggregateType;
	}
	
	public void insertRawSensor(String sensorName) {
		Sensor sensor = getSensor(sensorName);
		if(sensor==null) {
			log.trace("created new sensor "+sensorName);
			sensor = new Sensor(sensorName);
			insertSensor(sensor);
		}			
		if(baseAggregationExists(sensorName)) {
			log.error("base aggregation for raw exists: "+sensorName);
		}
		sensor.baseAggregationType = AggregationType.NONE;		
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
			return virtualPlot.getSchema();
		}
		Station station = getStation(plotID);
		if(station!=null) {
			return station.getSchema();
		}		
		String[] parts = plotID.split(":"); // structure plotID:stationID
		if(parts.length!=2) {
			throw new RuntimeException("plotID not found: "+plotID);
		}
		station = getStation(parts[1]);
		if(station!=null) {
			return station.getSchema();
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
			}
		}
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
		ArrayList<String> additionalSensorNames = null;
		for(String name:schema) {
			if(name.equals("Rn_300")) {
				if(additionalSensorNames==null) {
					additionalSensorNames = new ArrayList<String>();
				}
				additionalSensorNames.add("sunshine");
			}
			if(name.equals("Ta_200")) {
				if(additionalSensorNames==null) {
					additionalSensorNames = new ArrayList<String>();
				}
				additionalSensorNames.add("Ta_200_min");
				additionalSensorNames.add("Ta_200_max");
			}
			if(name.equals("rH_200")) {
				if(additionalSensorNames==null) {
					additionalSensorNames = new ArrayList<String>();
				}
				additionalSensorNames.add("rH_200_min");
				additionalSensorNames.add("rH_200_max");
			}
		}
		if(additionalSensorNames==null) {
			return schema;
		} else {
			return Stream.concat(Arrays.stream(schema), additionalSensorNames.stream()).toArray(String[]::new);			
		}
	}
	
	public String[] supplementSchema(String... schema) {
		if(schema==null) {
			return null;
		}
		
		Map<String, Integer> schemaMap = Util.stringArrayToMap(schema);
		//ArrayList<String> additionalSensorNames = new ArrayList<String>();
		LinkedHashSet<String> additionalSensorNames = new LinkedHashSet<>(); // no duplicates
		if(schemaMap.containsKey("WD")&&!schemaMap.containsKey("WV")) {
			additionalSensorNames.add("WV");
		}
		if(schemaMap.containsKey("sunshine")&&!schemaMap.containsKey("Rn_300")) {
			additionalSensorNames.add("Rn_300");
		}
		
		if(schemaMap.containsKey("Ta_200_min")&&!schemaMap.containsKey("Ta_200")) {
			additionalSensorNames.add("Ta_200");
		}
		
		if(schemaMap.containsKey("Ta_200_max")&&!schemaMap.containsKey("Ta_200")) {
			additionalSensorNames.add("Ta_200");
		}
		
		if(schemaMap.containsKey("rH_200_min")&&!schemaMap.containsKey("rH_200")) {
			additionalSensorNames.add("rH_200");
		}
		
		if(schemaMap.containsKey("rH_200_max")&&!schemaMap.containsKey("rH_200")) {
			additionalSensorNames.add("rH_200");
		}

		if(additionalSensorNames.isEmpty()) {
			return schema;
		} else {
			return Stream.concat(Arrays.stream(schema), additionalSensorNames.stream()).toArray(String[]::new);			
		}		
	}
}
