package tsdb.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualCopyList;
import tsdb.component.Sensor;
import tsdb.graph.node.Base;
import tsdb.graph.node.Continuous;
import tsdb.graph.node.ContinuousGen;
import tsdb.graph.node.Node;
import tsdb.graph.node.NodeGen;
import tsdb.graph.processing.Aggregated;
import tsdb.graph.processing.ElementRawCopy;
import tsdb.graph.processing.EmpiricalFiltered_NEW;
import tsdb.graph.processing.Evaporation;
import tsdb.graph.processing.Mask;
import tsdb.graph.processing.PeakSmoothed;
import tsdb.graph.processing.RangeStepFiltered;
import tsdb.graph.processing.Sunshine;
import tsdb.graph.processing.SunshineOlivieri;
import tsdb.graph.processing.Virtual_P_RT_NRT;
import tsdb.graph.source.BaseFactory;
import tsdb.graph.source.StationRawSource;
import tsdb.iterator.ElementCopyIterator.Action;
import tsdb.iterator.SunshineIterator;
import tsdb.iterator.SunshineOlivieriIterator;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.TsEntry;
import tsdb.util.TsEntryMutator;
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
			//log.info("gen "+stationID+"  "+Arrays.toString(schema));
			Station station = tsdb.getStation(stationID);
			if(station==null) {
				throw new RuntimeException("station not found: "+stationID);
			}
			schema = station.stationSchemaSupplement(schema);
			Node rawSource = StationRawSource.of(tsdb, stationID, schema);									
			rawSource = rawProcessing(tsdb, rawSource, schema, dataQuality);			
			if(station.loggerType.typeName.equals("tfi")) {
				rawSource = PeakSmoothed.of(rawSource);
			}
			return rawSource;
		};
	}

	public static Node rawProcessing(TsDB tsdb, Node rawSource, String[] schema, DataQuality dataQuality) {
		if(DataQuality.Na!=dataQuality) {
			if(DataQuality.NO!=dataQuality) {
				rawSource = Mask.of(tsdb, rawSource);
			}
			rawSource = RangeStepFiltered.of(tsdb, rawSource, dataQuality);
		}
		rawSource = elementRawCopy(tsdb, rawSource);
		if(Util.containsString(schema, SunshineIterator.SUNSHINE_SENSOR_NAME)) {
			rawSource = Sunshine.of(tsdb, rawSource);
		}
		if(Util.containsString(schema, SunshineOlivieriIterator.SUNSHINE_SENSOR_NAME)) {
			rawSource = SunshineOlivieri.of(tsdb, rawSource);
		}
		if(Util.containsString(schema, Evaporation.SENSOR_NAME)) {
			rawSource = Evaporation.of(tsdb, rawSource);
		}
		if(Util.containsString(schema, "P_RT_NRT") && Util.containsString(schema, "P_container_RT")) {
			rawSource = Virtual_P_RT_NRT.of(tsdb, rawSource);
		}
		return rawSource;
	}

	/**
	 * Copy elements for virtual sensors.
	 * @param tsdb 
	 * @param schema 
	 * @param source 
	 * @return 
	 */
	public static Node elementRawCopy(TsDB tsdb, Node source) {
		String[] schema = source.getSchema();
		log.info("schema "+Arrays.toString(schema));
		if(Util.containsOneString(schema, tsdb.raw_copy_sensor_names)) {
			List<Action> actions = new ArrayList<>();
			for(VirtualCopyList p:tsdb.raw_copy_lists) { 
				if(Util.containsString(schema, p.target)) {
					actions.add(Action.of(schema, p.sources, p.target));
				}
			}
			if(!actions.isEmpty()) {
				List<Action> finalActions = new ArrayList<>();
				int counter = 0;
				while(!actions.isEmpty()) {
					HashSet<String> targetSet = new HashSet<String>();
					for(Action action:actions) {
						targetSet.add(schema[action.targetIndex]);
					}
					Iterator<Action> it = actions.iterator();
					while(it.hasNext()) {
						Action action = it.next();
						if(!targetSet.contains(schema[action.sourceIndex])) {
							finalActions.add(action);
							it.remove();
						}
					}
					counter++;
					if(counter>100) {
						log.error("elementRawCopy: could not reorder copy actions");
						break;
					}
				}
				source = ElementRawCopy.of(source, finalActions.toArray(new Action[0]));
			}
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
			TsEntryMutator[] dayMutators = getDayMutators(tsdb, schema);
			return Aggregated.of(tsdb, continuous, AggregationInterval.DAY, dayMutators);
		};
	}


	public static TsEntryMutator[] getDayMutators(TsDB tsdb, String[] schema) {
		ArrayList<TsEntryMutator> mutators = null;
		for(String sensorName:schema) {
			Sensor sensor = tsdb.getSensor(sensorName);
			if(sensor != null && sensor.post_day_func != null) {
				TsEntryMutator mutator = null;
				try {
					switch(sensor.post_day_func) {
					case "dependency2_minus_dependency1": {
						String dependency1 = sensor.dependency[0];
						String dependency2 = sensor.dependency[1];
						int iDependency1 = Util.getIndexInArray(dependency1, schema);
						int iDependency2 = Util.getIndexInArray(dependency2, schema);
						int iTarget = Util.getIndexInArray(sensorName, schema);
						mutator = new TsEntryMutator() {						
							@Override
							public void apply(TsEntry entry) {
								float[] data = entry.data;
								data[iTarget] = data[iDependency2] - data[iDependency1];							
							}
						};
						break;
					}
					case "dependency1_less_0": {
						String dependency1 = sensor.dependency[0];
						int iDependency1 = Util.getIndexInArray(dependency1, schema);
						int iTarget = Util.getIndexInArray(sensorName, schema);
						mutator = new TsEntryMutator() {						
							@Override
							public void apply(TsEntry entry) {
								float[] data = entry.data;
								float v = data[iDependency1];
								data[iTarget] = Float.isNaN(v) ? Float.NaN : v < 0f ? 1 : 0;							
							}
						};
						break;
					}
					case "dependency1_less_10": {
						String dependency1 = sensor.dependency[0];
						int iDependency1 = Util.getIndexInArray(dependency1, schema);
						int iTarget = Util.getIndexInArray(sensorName, schema);
						mutator = new TsEntryMutator() {						
							@Override
							public void apply(TsEntry entry) {
								float[] data = entry.data;
								float v = data[iDependency1];
								data[iTarget] = Float.isNaN(v) ? Float.NaN : v < 10f ? 1 : 0;							
							}
						};
						break;
					}
					case "dependency1_greater_equal_20": {
						String dependency1 = sensor.dependency[0];
						int iDependency1 = Util.getIndexInArray(dependency1, schema);
						int iTarget = Util.getIndexInArray(sensorName, schema);
						mutator = new TsEntryMutator() {						
							@Override
							public void apply(TsEntry entry) {
								float[] data = entry.data;
								float v = data[iDependency1];
								data[iTarget] = Float.isNaN(v) ? Float.NaN : v >= 20f ? 1 : 0;							
							}
						};
						break;
					}
					case "dependency1_greater_equal_30": {
						String dependency1 = sensor.dependency[0];
						int iDependency1 = Util.getIndexInArray(dependency1, schema);
						int iTarget = Util.getIndexInArray(sensorName, schema);
						mutator = new TsEntryMutator() {						
							@Override
							public void apply(TsEntry entry) {
								float[] data = entry.data;
								float v = data[iDependency1];
								data[iTarget] = Float.isNaN(v) ? Float.NaN : v >= 30f ? 1 : 0;							
							}
						};
						break;
					}
					default:
						log.warn("unknown day mutator: "+sensor.post_day_func);
					}
				} catch(Exception e) {
					log.error(e);
				}
				if(mutator != null) {
					if(mutators == null) {
						mutators = new ArrayList<TsEntryMutator>();
					}
					mutators.add(mutator);
				}
			}
		}
		return mutators == null ? null : mutators.toArray(new TsEntryMutator[0]);
	}
}
