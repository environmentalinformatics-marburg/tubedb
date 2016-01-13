package tsdb;

import static tsdb.util.AssumptionCheck.throwNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.AggregationType;
import tsdb.util.BaseAggregationTimeUtil;
import tsdb.util.TimestampInterval;
import tsdb.util.Util;

/**
 * plot with data from a collection of data streams changing in time
 * @author woellauer
 *
 */
public class VirtualPlot {

	private static final Logger log = LogManager.getLogger();

	protected final TsDB tsdb; //not null

	public final String plotID;
	public final GeneralStation generalStation;

	public float geoPosEasting;
	public float geoPosNorthing;
	
	public double geoPosLatitude;
	public double geoPosLongitude;

	public float elevation;
	public float elevationTemperature;

	public final boolean isFocalPlot;

	public final List<TimestampInterval<StationProperties>> intervalList;


	/**
	 * This list is used for interpolation when similar stations are needed.
	 */
	public List<VirtualPlot> nearestVirtualPlots;

	public VirtualPlot(TsDB tsdb, String plotID, GeneralStation generalStation,float geoPosEasting, float geoPosNorthing, boolean isFocalPlot) {
		throwNull(tsdb);
		this.tsdb = tsdb;
		this.plotID = plotID;
		this.generalStation = generalStation;
		this.geoPosEasting = geoPosEasting;
		this.geoPosNorthing = geoPosNorthing;
		this.elevation = Float.NaN;
		this.elevationTemperature = Float.NaN;
		this.isFocalPlot = isFocalPlot;
		this.intervalList = new ArrayList<TimestampInterval<StationProperties>>();
		this.nearestVirtualPlots = new ArrayList<VirtualPlot>(0);
		this.geoPosLatitude = Float.NaN;
		this.geoPosLongitude = Float.NaN;
	}

	/**
	 * Creates schema of this plot that is union of all attributes of stations that are attached to this plot with some time interval.
	 * @return null if there are no intervals
	 */
	public String[] getSchema() {
		if(intervalList.isEmpty()) {
			return new String[0]; //empty schema
		}

		TreeSet<String> sensorNameSet = new TreeSet<String>();
		for(TimestampInterval<StationProperties> interval:intervalList) {
			String stationName = interval.value.get_serial();
			if(stationName==null) {
				log.warn("no station in interval: "+interval);
				continue;
			}
			Station station = tsdb.getStation(stationName);
			if(station==null) {
				log.warn("station not found "+stationName);
				continue;
			}
			String[] sensorNames = station.getSchema();
			sensorNameSet.addAll(Arrays.asList(sensorNames));
		}

		return sensorNameSet.toArray(new String[sensorNameSet.size()]);  

		/*LinkedHashSet<LoggerType> loggerSet = new LinkedHashSet<LoggerType>();
		for(TimestampInterval<StationProperties> interval:intervalList) {
			LoggerType loggerType = tsdb.getLoggerType(interval.value.get_logger_type_name());
			if(loggerType==null) {
				throw new RuntimeException("logger type not found: "+interval.value.get_logger_type_name());
			}
			loggerSet.add(loggerType);
		}
		//LinkedHashSet<String> sensorNameSet = new LinkedHashSet<String>();
		TreeSet<String> sensorNameSet = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
		for(LoggerType loggerType:loggerSet) {
			for(String sensorName:loggerType.sensorNames) {
				sensorNameSet.add(sensorName);
			}
		}

		return sensorNameSet.toArray(new String[0]);*/
	}

	public String[] getValidSchemaEntries(String[] querySchema) {
		return Util.getValidEntries(querySchema, getSchema());
	}
	
	public String[] getValidSchemaEntriesWithVirtualSensors(String[] querySchema) {
		return Util.getValidEntries(querySchema, tsdb.includeVirtualSensorNames(getSchema()));
	}

	/**
	 * Adds one time interval of one station to this plot
	 * @param station
	 * @param properties
	 */
	public void addStationEntry(Station station, StationProperties properties) {
		try {
			intervalList.add(new TimestampInterval<StationProperties>(properties, properties.get_date_start(), properties.get_date_end()));
		} catch(Exception e) {
			log.warn(e+" with "+station.stationID+"   "+properties.getProperty(StationProperties.PROPERTY_START)+"  "+properties.getProperty(StationProperties.PROPERTY_END));
		}
	}

	/**
	 * checks if the given interval overlaps with query interval
	 * @param queryStart may be null if start time is not specified
	 * @param queryEnd may be null if end time is not specified
	 * @param iStart may be null if start time is not specified
	 * @param iEnd may be null if end time is not specified
	 * @return
	 */
	private static boolean overlaps(Long queryStart, Long queryEnd, Long iStart, Long iEnd) {
		if(queryStart==null) {
			queryStart = Long.MIN_VALUE;
		}
		if(queryEnd==null) {
			queryEnd = Long.MAX_VALUE;
		}
		if(iStart==null) {
			iStart = Long.MIN_VALUE;
		}
		if(iEnd==null) {
			iEnd = Long.MAX_VALUE;
		}		
		return queryStart <= iEnd && iStart <= queryEnd;
	}
	
	

	/**
	 * Get list of stations with overlapping entries in time interval start - end
	 * @param queryStart
	 * @param queryEnd
	 * @param schema
	 * @return
	 */
	public List<TimestampInterval<StationProperties>> getStationList(Long queryStart, Long queryEnd, String[] schema) {		
		if(schema==null) {
			schema = getSchema();
		}

		ArrayList<TimestampInterval<StationProperties>> tempList = new ArrayList<TimestampInterval<StationProperties>>(intervalList); // because ConcurrentModificationException

		tempList.sort( (a,b) -> {
			if(a.start==null) {
				if(b.start==null) {
					return 0;
				} else {
					return -1; // start1==null start2!=null
				}
			} else {
				if(b.start==null) {
					return 1; // start1!=null start2==null
				} else {
					return (a.start < b.start) ? -1 : ((a.start == b.start) ? 0 : 1);
				}
			}
		});

		Iterator<TimestampInterval<StationProperties>> it = tempList.iterator();


		List<TimestampInterval<StationProperties>> resultIntervalList = new ArrayList<TimestampInterval<StationProperties>>();
		while(it.hasNext()) {
			TimestampInterval<StationProperties> interval = it.next();
			/*if(schemaOverlaps(tsdb.getLoggerType(interval.value.get_logger_type_name()).sensorNames,schema)) {
				if(overlaps(queryStart, queryEnd, interval.start, interval.end)) {
					resultIntervalList.add(interval);
				}
			}*/
			String stationID = interval.value.get_serial();
			if(stationID!=null) {
				Station station = tsdb.getStation(stationID);
				if(station!=null) {
					String[] stationSchema = station.getSchema();
					if(schemaOverlaps(stationSchema,schema)) {
						if(overlaps(queryStart, queryEnd, interval.start, interval.end)) {
							resultIntervalList.add(interval);
						}
					}
				} else {
					log.warn("station not found "+stationID);
				}
			} else {
				log.warn("no stationID");
			}

		}
		return resultIntervalList;
	}

	/**
	 * Checks if there are some attributes that are in both schema
	 * @param schema
	 * @param schema2
	 * @return
	 */
	private boolean schemaOverlaps(String[] schema, String[] schema2) {
		for(String name:schema) {
			for(String name2:schema2) {
				if(name.equals(name2)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return plotID;
	}

	public long[] getTimestampInterval() {
		long[] result = null;
		for(TimestampInterval<StationProperties> entry:intervalList) {
			long[] interval = tsdb.getTimeInterval(entry.value.get_serial());

			if(interval!=null) {
				Long partStart = entry.value.get_date_start();
				if(partStart!=null) {
					if(interval[1]<partStart) {
						interval = null;
					} else if(interval[0]<partStart) {
						interval[0] = partStart;				
					}
				}
			}
			if(interval!=null) {
				Long partEnd = entry.value.get_date_end();
				if(partEnd!=null) {
					if(partEnd<interval[0]) {
						interval = null;
					} else if(partEnd<interval[1]) {
						interval[1] = partEnd;
					}
				}
			}
			if(interval!=null) {				
				Long partStart = entry.value.get_date_start();
				if(partStart!=null&&interval[0]<partStart) {
					interval[0] = partStart;
				}
				Long partEnd = entry.value.get_date_end();
				if(partEnd!=null&&partEnd<interval[1]) {
					interval[1] = partEnd;
				}
				if(interval[0]>interval[1]) {
					log.info("interval[0]>interval[1]"+interval[0]+"  "+interval[1]+"    "+entry.value.get_date_start()+"  "+entry.value.get_date_end());
				}
				if(result==null) {
					result = interval;
				} else {
					if(interval[0]<result[0]) {
						result[0] = interval[0];
					}
					if(result[1]<interval[1]) {
						result[1] = interval[1];
					}
				}
			}
		}
		return result;
	}

	public long[] getTimestampBaseInterval() {
		long[] interval = getTimestampInterval();
		if(interval==null) {
			return null;
		}
		return new long[]{BaseAggregationTimeUtil.alignQueryTimestampToBaseAggregationTime(interval[0]),BaseAggregationTimeUtil.alignQueryTimestampToBaseAggregationTime(interval[1])};
	}

	public boolean isValidSchema(String[] querySchema) {
		throwNull((Object)querySchema);
		String[] schema = getSchema();
		if(schema==null) {
			return false;
		}
		return !(querySchema==null||querySchema.length==0||!Util.isContained(querySchema, schema));
	}
	
	public boolean isValidSchemaWithVirtualSensors(String[] querySchema) {
		throwNull((Object)querySchema);
		String[] schema = getSchema();
		if(schema==null) {
			return false;
		}
		return !(querySchema==null||querySchema.length==0||!Util.isContained(querySchema, tsdb.includeVirtualSensorNames(schema)));		
	}

	public boolean isValidBaseSchema(String[] querySchema) {
		if(!isValidSchema(querySchema)) {
			return false;
		}
		for(String name:querySchema) {
			if(tsdb.getSensor(name).baseAggregationType==AggregationType.NONE) {
				return false;
			}
		}
		return true;
	}

	public void setElevation(float elevation) {
		if(Float.isNaN(elevation)) {
			log.warn("elevation not set: nan");
			return;
		}
		if(!Float.isNaN(this.elevation)) {
			log.warn("elevation already set, overwriting");
		}

		this.elevation = elevation;

		if(!Float.isNaN(this.elevation)) {
			if(elevation<=2321.501) {
				elevationTemperature = elevation*-0.008443f+31.560182f;
			} else {
				elevationTemperature = elevation*-0.004174f+21.648931f;	
			}
		} else {
			elevationTemperature = Float.NaN;
		}
	}
	
	public String[] getStationIDs() {
		TreeSet<String> set = new TreeSet<String>();
		for(TimestampInterval<StationProperties> interval:intervalList) {
			set.add(interval.value.get_serial());
		}
		return set.toArray(new String[0]);
	}
}
