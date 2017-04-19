package tsdb.graph;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.graph.node.Base;
import tsdb.graph.node.Continuous;
import tsdb.graph.node.ContinuousGen;
import tsdb.graph.node.Node;
import tsdb.graph.node.NodeGen;
import tsdb.graph.processing.Aggregated;
import tsdb.graph.processing.ElementRawCopy;
import tsdb.graph.processing.EmpiricalFiltered_NEW;
import tsdb.graph.processing.Mask;
import tsdb.graph.processing.PeakSmoothed;
import tsdb.graph.processing.RangeStepFiltered;
import tsdb.graph.processing.Sunshine;
import tsdb.graph.processing.Virtual_P_RT_NRT;
import tsdb.graph.source.BaseFactory;
import tsdb.graph.source.StationRawSource;
import tsdb.iterator.ElementCopyIterator.Action;
import tsdb.iterator.SunshineIterator;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.Util;

public final class QueryPlanGenerators {
	private static final Logger log = LogManager.getLogger();	
	private QueryPlanGenerators(){} 

	/**
	 * creates a generator of a station raw data with raw processing (quality check) and processing on not aggregated data (tfi)
	 * @param tsdb
	 * @param dataQuality
	 * @return
	 */
	public static NodeGen getStationGen(TsDB tsdb, DataQuality dataQuality) {
		return (String stationID, String[] schema)->{
			Station station = tsdb.getStation(stationID);
			if(station==null) {
				throw new RuntimeException("station not found: "+stationID);
			}
			schema = stationSchemaSupplement(tsdb, station, schema);
			Node rawSource = StationRawSource.of(tsdb, stationID, schema);									
			rawSource = rawProcessing(tsdb, rawSource, schema, dataQuality);			
			if(station.loggerType.typeName.equals("tfi")) {
				rawSource = PeakSmoothed.of(rawSource);
			}
			return rawSource;
		};
	}

	public static String[] stationSchemaSupplement(TsDB tsdb, Station station, String[] schema) {
		String[] stationSensorNames = station.getSensorNames();
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

		if(station.generalStation!=null && station.generalStation.region.name.equals("BE") && Util.containsString(schema, "P_RT_NRT")) { // add virtual P_RT_NRT of P_container_RT for stations in BE
			if(!Util.containsString(schema, "P_container_RT")) {
				return Util.concat(schema,"P_container_RT");
			}
		}
		return schema;		
	}

	public static Node rawProcessing(TsDB tsdb, Node rawSource, String[] schema, DataQuality dataQuality) {
		if(DataQuality.Na!=dataQuality) {
			if(DataQuality.NO!=dataQuality) {
				rawSource = Mask.of(tsdb, rawSource);
			}
			rawSource = RangeStepFiltered.of(tsdb, rawSource, dataQuality);
		}
		rawSource = elementRawCopy(rawSource);
		if(Util.containsString(schema, SunshineIterator.SUNSHINE_SENSOR_NAME)) {
			rawSource = Sunshine.of(tsdb, rawSource);
		}
		if(Util.containsString(schema, "P_RT_NRT") && Util.containsString(schema, "P_container_RT")) {
			rawSource = Virtual_P_RT_NRT.of(tsdb, rawSource);
		}
		return rawSource;
	}

	public static class VirtualCopyPair {
		public final String source;
		public final String target;
		public VirtualCopyPair(String source, String target) {
			this.source = source;
			this.target = target;
		}
		public static VirtualCopyPair of(String source, String target) {
			return new VirtualCopyPair(source, target);
		}
	}

	public static class VirtualCopyList {
		public final String[] sources;
		public final String target;
		public VirtualCopyList(String[] sources, String target) {
			this.sources = sources;
			this.target = target;
		}
		public static VirtualCopyList of(String[] sources, String target) {
			return new VirtualCopyList(sources, target);
		}
	}

	public static final VirtualCopyPair[] VIRTUAL_COPY_PAIRS = new VirtualCopyPair[]{
			VirtualCopyPair.of("Ta_200", "Ta_200_min"),			
			VirtualCopyPair.of("Ta_200", "Ta_200_max"),
			VirtualCopyPair.of("rH_200", "rH_200_min"),
			VirtualCopyPair.of("rH_200", "rH_200_max"),			
	};

	public static final VirtualCopyList[] VIRTUAL_COPY_LISTS = new VirtualCopyList[]{
			VirtualCopyList.of(new String[] {"SWDR_300", "SWDR_3700", "SWDR_4400"}, "SWDR"),
			VirtualCopyList.of(new String[] {"SWUR_300", "SWUR_3700", "SWUR_4400"}, "SWUR"),
			VirtualCopyList.of(new String[] {"LWDR_300", "LWDR_3700", "LWDR_4400"}, "LWDR"),
			VirtualCopyList.of(new String[] {"LWUR_300", "LWUR_3700", "LWUR_4400"}, "LWUR"),
	};

	private static final String[] VIRTUAL_COPY_SENSORS;

	static {		
		ArrayList<String> list = new ArrayList<>();
		for(VirtualCopyPair p:VIRTUAL_COPY_PAIRS) {
			list.add(p.target);
		}
		for(VirtualCopyList p:VIRTUAL_COPY_LISTS) {
			list.add(p.target);
		}
		VIRTUAL_COPY_SENSORS = list.toArray(new String[0]);
	}


	/**
	 * Copy elements for virtual sensors.
	 * @param schema 
	 * @param source 
	 * @return 
	 */
	public static Node elementRawCopy(Node source) {
		String[] schema = source.getSchema();
		if(Util.containsOneString(schema, VIRTUAL_COPY_SENSORS)) {
			List<Action> actions = new ArrayList<>();
			for(VirtualCopyPair pair:VIRTUAL_COPY_PAIRS) {
				if(Util.containsString(schema, pair.target)) {
					actions.add(Action.of(schema, pair.source, pair.target));
				}
			}
			for(VirtualCopyList p:VIRTUAL_COPY_LISTS) { 
				if(Util.containsString(schema, p.target)) {
					actions.add(Action.of(schema, p.sources, p.target));
				}
			}
			source = ElementRawCopy.of(source, actions.toArray(new Action[0]));
		}
		return source;
	}

	/**
	 * Creates a generator of a continuous source.
	 * @param tsdb
	 * @param dataQuality
	 * @return
	 */
	public static ContinuousGen getContinuousGen(TsDB tsdb, DataQuality dataQuality) {
		return (String plotID, String[] schema)->{
			NodeGen stationGen = getStationGen(tsdb, dataQuality);		
			Base base = null;
			try {
				base = BaseFactory.of(tsdb, plotID, schema, stationGen);
			} catch(Exception e) {
				log.warn(e);
				return null;
			}
			if(base==null) {
				return null;
			}
			Continuous continuous = Continuous.of(base);
			if(DataQuality.EMPIRICAL==dataQuality) {
				continuous = EmpiricalFiltered_NEW.of(tsdb, continuous, plotID);
			}
			return continuous;
		};
	}

	/**
	 * Creates a generator of a continuous source with day aggregated values.
	 * not interpolated
	 * @param tsdb
	 * @param dataQuality
	 * @return
	 */
	public static ContinuousGen getDayAggregationGen(TsDB tsdb, DataQuality dataQuality) {
		return (String plotID, String[] schema)->{
			Continuous continuous = getContinuousGen(tsdb, dataQuality).get(plotID, schema);
			return Aggregated.of(tsdb, continuous, AggregationInterval.DAY);
		};
	}
}
