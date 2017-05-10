package tsdb;

import static tsdb.util.AssumptionCheck.throwNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.component.LoggerType;
import tsdb.component.labeledproperty.LabeledProperties;
import tsdb.graph.QueryPlanGenerators;
import tsdb.graph.QueryPlanGenerators.VirtualCopyList;
import tsdb.util.AggregationType;
import tsdb.util.AssumptionCheck;
import tsdb.util.Interval;
import tsdb.util.NamedInterval;
import tsdb.util.TimeUtil;
import tsdb.util.TimestampInterval;
import tsdb.util.Util;

/**
 * This class contains metadata that is associated with a station (plotID).
 * @author woellauer
 *
 */
public class Station {
	private static final Logger log = LogManager.getLogger();

	private final TsDB tsdb; //not null

	/**
	 * Stream name of this station
	 */
	public final String stationID;

	public final LoggerType loggerType;
	
	public LabeledProperties labeledProperties = new LabeledProperties();

	/**
	 * 
	 * list of property map for Kili
	 */
	public List<TimestampInterval<StationProperties>>   propertiesList;

	/**
	 * translation map: input sensor name -> database sensor name
	 * This map contains only entries that are specific for this Station (or plotID)
	 */
	public Map<String,String> sensorNameTranlationMap;

	/**
	 * input sensor name correction map: input sensor name -> corrected sensor name
	 * This map contains only entries that are specific for this Station (or plotID)
	 * This field may be null if no corrections are available.
	 */
	public Map<String,NamedInterval[]> sensorNameCorrectionMap = null;

	//*** start of fields that are used if this station is identical to one plot ***
	public final boolean isPlot;
	public double geoPosLongitude;
	public double geoPosLatitude;
	/**
	 * The general name of this plotID for example HEG03 it is HEG
	 * This name belongs to a GeneralStation Object
	 */
	public final GeneralStation generalStation;

	/**
	 * list of stations of same general station id ordered by position difference to this station
	 */
	public List<Station> nearestStations;

	/**
	 * serial number of station: A19557, A2277, ...
	 * not used currently - station is identified with plotID
	 */
	public String alternativeID = null;
	//*** end of fields that are used if this station is identical to one plot ***

	public Station(TsDB tsdb, GeneralStation generalStation, String stationID, LoggerType loggerType, List<StationProperties> propertyMapList, boolean isPlot) {
		throwNull(tsdb);
		this.tsdb = tsdb;
		this.isPlot = isPlot;
		this.generalStation = generalStation;
		this.stationID = stationID;
		this.propertiesList = StationProperties.createIntervalList(propertyMapList);
		this.geoPosLongitude = Float.NaN;
		this.geoPosLatitude = Float.NaN;
		this.loggerType = loggerType;
		this.sensorNameTranlationMap = new HashMap<String, String>();
		if(isPlot) {
			if(propertiesList.size()!=1) {
				log.warn("station that is plot can only have one StationProperties: "+propertiesList.size());
				if(!stationID.equals(propertyMapList.get(0).get_plotid())) {
					log.warn("stationID is not equal to plotID for station that is plot: "+stationID+"  "+propertyMapList.get(0).get_plotid());
				}
			}
		} else {
			for(StationProperties property:propertyMapList) {
				if(!stationID.equals(property.get_serial())) {
					log.warn("stationID does not equal to serial: "+stationID+"  "+property.get_serial());
				}
				if(!loggerType.typeName.equals(property.get_logger_type_name())) {
					log.warn("station logger does not equal to property logger: "+loggerType.typeName+"  "+property.get_logger_type_name()+" in "+stationID);
				}
			}
		}
	}	

	/**
	 * This method determines the database sensor name out of an input sensor name.
	 * Steps:
	 * <br>1. check if there an entry in the plotID specific sensorNameTranlationMap
	 * <br>2. else check if there is an entry in the general station  sensorNameTranlationMap
	 * <br>3. else check if there is an entry in the logger type sensorNameTranlationMap
	 * <br>4. else return null (either input sensor name and database sensor name are identical or sensor name is unknown / not used)
	 * @param sensorName
	 * @return
	 */
	public String translateInputSensorName(String sensorName, boolean useGeneralstation) {
		String resultName = sensorNameTranlationMap.get(sensorName);
		if(resultName!=null) {
			return resultName;
		}
		if(useGeneralstation) {
			resultName = generalStation.sensorNameTranlationMap.get(sensorName);
			if(resultName!=null) {
				return resultName;
			}
		}
		resultName = loggerType.sensorNameTranlationMap.get(sensorName);
		if(resultName!=null) {
			return resultName;
		}
		String[] schemaSensorNames = loggerType.sensorNames;
		for(String schemaSensorName:schemaSensorNames) {
			if(schemaSensorName.equals(sensorName)) {
				return sensorName;
			}
		}
		return null;
	}

	/**
	 * Applys sensor name corrections.
	 * Should be used before translateInputSensorName.
	 * @param rawName
	 * @param fileTimeInterval
	 * @return rawName or corrected name
	 */
	public @NotNull String correctRawSensorName(@NotNull String rawName, @NotNull Interval fileTimeInterval) {
		AssumptionCheck.throwNulls(rawName, fileTimeInterval);
		if(sensorNameCorrectionMap==null) {
			return rawName;
		}
		NamedInterval[] corrected = sensorNameCorrectionMap.get(rawName);
		if(corrected==null) {
			return rawName;
		}
		for(NamedInterval namedInterval:corrected) {
			if(namedInterval.covers(fileTimeInterval)) {
				log.info("sensor name corrected in "+stationID+"    "+rawName+" -> "+namedInterval.name);
				return namedInterval.name;
			}
		}
		return rawName;
	}

	@Override
	public String toString() {
		return stationID+"("+loggerType.typeName+")";
	}

	/*public String[] getValidSchemaEntries(String[] querySchema) {
		Map<String, Integer> schemaMap = Util.stringArrayToMap(loggerType.sensorNames);
		ArrayList<String> resultList = new ArrayList<String>();
		for(String sensorName:querySchema) {
			if(schemaMap.containsKey(sensorName)) {
				resultList.add(sensorName);
			}
		}
		if(resultList.size()==0) {
			return null;
		}
		return resultList.toArray(new String[0]);
	}*/

	public String[] getValidSchemaEntries(String[] querySchema) {		
		//return Util.getValidEntries(querySchema, loggerType.sensorNames);
		return Util.getValidEntries(querySchema, getSensorNames());
	}

	public String[] getValidSchemaEntriesWithVirtualSensors(String[] querySchema) {		
		//return Util.getValidEntries(querySchema, loggerType.sensorNames);
		return Util.getValidEntries(querySchema, tsdb.includeVirtualSensorNames(getSensorNames()));
	}

	public boolean isValidSchema(String[] querySchema) {
		//return !(querySchema==null||querySchema.length==0||!Util.isContained(querySchema, loggerType.sensorNames));
		return !(querySchema==null||querySchema.length==0||!Util.isContained(querySchema, getSensorNames()));
	}

	public boolean isValidSchemaWithVirtualSensors(String[] querySchema) {
		//return !(querySchema==null||querySchema.length==0||!Util.isContained(querySchema, loggerType.sensorNames));
		return !(querySchema==null||querySchema.length==0||!Util.isContained(querySchema, tsdb.includeVirtualSensorNames(getSensorNames())));
	}

	public boolean isValidBaseSchema(String[] querySchema) {
		if(!isValidSchema(querySchema)) {
			return false;
		}
		for(String name:querySchema) {
			if(!tsdb.getSensor(name).isAggregable()) {
				return false;
			}
		}
		return true;
	}

	public List<Station> getNearestStationsWithSensor(String sensorName) {
		ArrayList<Station> result = new ArrayList<Station>();
		for(Station station:nearestStations) {
			for(String name:station.loggerType.sensorNames) {
				if(sensorName.equals(name)) {
					result.add(station);
					break;
				}				
			}
		}
		return result;
	}

	public StationProperties getProperties(long intervalStart, long intervalEnd) {
		StationProperties properties = null;
		for(TimestampInterval<StationProperties> interval:propertiesList) {
			if((interval.start==null || interval.start<=intervalStart) && (interval.end==null || intervalEnd<=interval.end)) {
				if(properties!=null) {
					log.warn("multiple properties for one time interval in station   "+stationID+"  of  "+TimeUtil.oleMinutesToText(intervalStart)+" "+TimeUtil.oleMinutesToText(intervalEnd));
				}
				properties = interval.value;
			}
		}
		return properties;
	}

	/**
	 * Get sensor names of station.
	 * @return array of all sensor names that contain time series data. Empty array if there is no data.
	 */
	public @NotNull String[] getSensorNames() {
		/*String[] sensorSet = null;
		if(tsdb.streamStorage.existStation(stationID)) {
			sensorSet = tsdb.streamStorage.getSensorNames(stationID);
		}
		if(sensorSet!=null) {
			return sensorSet;
		} else {
			return loggerType.sensorNames;
		}*/
		if(tsdb.streamStorage.existStation(stationID)) {
			return tsdb.streamStorage.getSensorNames(stationID);
		} else {
			return new String[0];
		}		
	}

	public boolean isVIP() {
		if(!isPlot) {
			return false;
		}
		return propertiesList.get(0).value.isVIP();
	}

	public boolean existData() {
		return tsdb.streamStorage.existStation(stationID);
	}

	public boolean existData(String sensorName) {
		if( !existData()) {
			return false;
		}
		String[] sensorNames = tsdb.streamStorage.getSensorNames(stationID);
		if(sensorName==null) {
			return false;
		}
		for(String s:sensorNames) {
			if(sensorName.equals(s)) {
				return true;
			}
		}
		return false;
	}
	
	public String[] stationSchemaSupplement(String[] schema) {
		String[] stationSensorNames = getSensorNames();
		for(VirtualCopyList p:QueryPlanGenerators.VIRTUAL_COPY_LISTS) {
			if(Util.containsString(schema, p.target)) {				
				innerLoop: for(String source:p.sources) {
					if(Util.containsString(schema, source)) {
						break innerLoop;
					}
					if(Util.containsString(stationSensorNames, source)) {
						schema = Util.concat(schema, source);
						break innerLoop;
					}
				}
			}
		}	

		if(generalStation!=null && generalStation.region.name.equals("BE") && Util.containsString(schema, "P_RT_NRT")) { // add virtual P_RT_NRT of P_container_RT for stations in BE
			if(!Util.containsString(schema, "P_container_RT")) {
				return Util.concat(schema,"P_container_RT");
			}
		}
		return schema;		
	}
}