package tsdb.graph;

import java.util.ArrayList;
import java.util.Arrays;
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
import tsdb.graph.processing.ElementCopy;
import tsdb.graph.processing.EmpiricalFiltered_NEW;
import tsdb.graph.processing.Mask;
import tsdb.graph.processing.PeakSmoothed;
import tsdb.graph.processing.RangeStepFiltered;
import tsdb.graph.processing.Sunshine;
import tsdb.graph.processing.Virtual_P_RT_NRT;
import tsdb.graph.source.BaseFactory;
import tsdb.graph.source.StationRawSource;
import tsdb.iterator.ElementCopyIterator.Action;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.Util;

public final class QueryPlanGenerators {
	private static final Logger log = LogManager.getLogger();	
	private QueryPlanGenerators(){} 

	/**
	 * creates a generator of a station raw data with quality check
	 * @param tsdb
	 * @param dataQuality
	 * @return
	 */
	public static NodeGen getStationGen(TsDB tsdb, DataQuality dataQuality) {
		return (String stationID, String[] schema)->{
			Station station = tsdb.getStation(stationID);
			if(station==null) {
				throw new RuntimeException("station not found");
			}
			boolean virtual_P_RT_NRT = false;
			if(station.generalStation!=null && station.generalStation.region.name.equals("BE") && Util.containsString(schema, "P_RT_NRT")) {
				virtual_P_RT_NRT = true;				
				if(!Util.containsString(schema, "P_container_RT")) {
					schema = Util.concat(schema,"P_container_RT");
				}
			}
			Node rawSource = StationRawSource.of(tsdb, stationID, schema);
			log.info("get raw source "+Arrays.toString(schema));
			if(DataQuality.Na!=dataQuality && DataQuality.NO!=dataQuality) {
				rawSource = Mask.of(tsdb, rawSource);
			}
			if(virtual_P_RT_NRT) {
				rawSource = Virtual_P_RT_NRT.of(tsdb, rawSource);
			}
			if(station.loggerType.typeName.equals("tfi")) {
				rawSource = PeakSmoothed.of(rawSource);
			}			
			if(DataQuality.Na!=dataQuality) {
				rawSource = RangeStepFiltered.of(tsdb, rawSource, dataQuality);
			}
			if(Util.containsString(schema, "sunshine")) {
				rawSource = Sunshine.of(tsdb, rawSource);
			}
			//rawSource = elementCopy(rawSource, schema);
			return rawSource;
		};
	}

	/**
	 * Copy elements for virtual sensors.
	 * @param schema 
	 * @param source 
	 * @return 
	 */
	public static Continuous elementCopy(Continuous source) {
		String[] schema = source.getSchema();
		if(Util.containsString(schema, "Ta_200_min") 
				|| Util.containsString(schema, "Ta_200_max")
				|| Util.containsString(schema, "rH_200_min") 
				|| Util.containsString(schema, "rH_200_max")) {
			List<Action> actions = new ArrayList<>();
			if(Util.containsString(schema, "Ta_200_min")) {
				actions.add(Action.of(schema, "Ta_200", "Ta_200_min"));
			}
			if(Util.containsString(schema, "Ta_200_max")) {
				actions.add(Action.of(schema, "Ta_200", "Ta_200_max"));
			}
			if(Util.containsString(schema, "rH_200_min")) {
				actions.add(Action.of(schema, "rH_200", "rH_200_min"));
			}
			if(Util.containsString(schema, "rH_200_max")) {
				actions.add(Action.of(schema, "rH_200", "rH_200_max"));
			}				
			source = ElementCopy.of(source, actions.toArray(new Action[0]));
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
			Base base = BaseFactory.of(tsdb, plotID, schema, stationGen);
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
