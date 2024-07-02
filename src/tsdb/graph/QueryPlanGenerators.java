package tsdb.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


import org.tinylog.Logger;

import tsdb.Plot;
import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualCopyList;
import tsdb.component.Sensor;
import tsdb.dsl.Environment;
import tsdb.dsl.FormulaBuilder;
import tsdb.dsl.FormulaCollectUnsafeVarVisitor;
import tsdb.dsl.FormulaCollectVarVisitor;
import tsdb.dsl.FormulaCompileVisitor;
import tsdb.dsl.FormulaJavaVisitor;
import tsdb.dsl.FormulaResolveUnifyVisitor;
import tsdb.dsl.PlotEnvironment;
import tsdb.dsl.formula.Formula;
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
import tsdb.graph.processing.MutatorNode;
import tsdb.graph.processing.NocCheck;
import tsdb.graph.processing.PeakSmoothed;
import tsdb.graph.processing.RangeStepFiltered;
import tsdb.graph.processing.ReferenceSourceMerge;
import tsdb.graph.processing.Sunshine;
import tsdb.graph.processing.SunshineOlivieri;
import tsdb.graph.processing.Virtual_P_RT_NRT;
import tsdb.graph.source.BaseFactory;
import tsdb.graph.source.StationRawSource;
import tsdb.iterator.ElementCopyIterator.Action;
import tsdb.iterator.SunshineIterator;
import tsdb.iterator.SunshineOlivieriIterator;
import tsdb.util.AggregationInterval;
import tsdb.util.Computation;
import tsdb.util.DataQuality;
import tsdb.util.Mutator;
import tsdb.util.Mutators;
import tsdb.util.Util;

public final class QueryPlanGenerators {
		
	private QueryPlanGenerators(){} 

	/**
	 * creates a generator of a station raw data with raw processing (quality check) and processing on not aggregated data (tfi)
	 * @param tsdb
	 * @param dataQuality
	 * @return
	 */
	public static NodeGen getStationGen(TsDB tsdb, DataQuality dataQuality) {
		return (String stationID, String[] schema)->{
			//Logger.info("gen "+stationID+"  "+Arrays.toString(schema));
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
		Mutator rawMutators = QueryPlanGenerators.getRawMutators(tsdb, rawSource.getSourcePlot(), schema);
		rawSource = elementRawCopy(tsdb, rawSource); // First copy raw source then apply mutators. So, mutators are not applied to copied raw source!
		if(rawMutators != null) {
			rawSource = MutatorNode.of(tsdb, rawSource, rawMutators);
		}		
		if(DataQuality.Na != dataQuality) {
			if(DataQuality.NO != dataQuality) {
				rawSource = Mask.of(tsdb, rawSource);
			}
			rawSource = RangeStepFiltered.of(tsdb, rawSource, dataQuality);
		}
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
		//Logger.info("elementRawCopy schema " + Arrays.toString(schema));
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
						Logger.error("elementRawCopy: could not reorder copy actions");
						break;
					}
				}
				//Logger.info(finalActions);
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
				String[] realSchema = Util.getSensorNamesWithoutRefs(schema);
				base = BaseFactory.of(tsdb, plotID, realSchema, stationGen);
			} catch(Exception e) {				
				Logger.warn(e);
				e.printStackTrace();
				return null;
			}
			if(base == null) {
				return null;
			}
			Continuous continuous = Continuous.of(base);
			if(dataQuality.isStep()) {
				//Logger.info("NocCheck");
				continuous = NocCheck.of(tsdb, continuous);
			}
			if(DataQuality.EMPIRICAL==dataQuality) {
				continuous = EmpiricalFiltered_NEW.of(tsdb, continuous, plotID);
			}
			String[] refRenameSchema = Util.getSensorNamesRefs(schema);
			if(refRenameSchema.length > 0) {
				String[] refSourceSchema = Util.convertSensorNamesRefsToSensorNames(refRenameSchema);
				continuous = ReferenceSourceMerge.of(tsdb, continuous, plotID, refSourceSchema, refRenameSchema, schema);
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
	/*public static ContinuousGen getDayAggregationGen(TsDB tsdb, DataQuality dataQuality) {
		return (String plotID, String[] schema)->{
			Continuous continuous = getContinuousGen(tsdb, dataQuality).get(plotID, schema);
			Mutator dayMutators = getPostDayMutators(tsdb, tsdb.getPlot(plotID), schema);
			return Aggregated.of(tsdb, continuous, AggregationInterval.DAY, dayMutators);
		};
	}*/

	public static Mutator getMutator(Sensor sensor, String func, Plot plot, String[] schema) {
		try {
			int iTarget = Util.getIndexInArray(sensor.name, schema);

			//Logger.info("parse formula: "+func);
			Formula formula_org = FormulaBuilder.parseFormula(func);
			HashMap<String, Integer> sensorMap = new HashMap<String, Integer>();
			String[] dependencies = sensor.dependency;
			if(dependencies != null) {
				for (int i = 0; i < dependencies.length; i++) {
					String dep = dependencies[i];
					int pos = Util.getIndexInArray(dep, schema);
					if(pos<0) {
						Logger.warn("dependency not found: "+dep);
					} else {
						sensorMap.put(dep, pos);
						String dependencyName = "dependency"+(i+1);
						sensorMap.put(dependencyName, pos);
					}
				}
			}
			//Logger.info(sensorMap);
			Environment env = plot == null ? new Environment(sensorMap) : new PlotEnvironment(plot, sensorMap);
			Formula formula = formula_org.accept(new FormulaResolveUnifyVisitor(env));
			try {
				FormulaJavaVisitor v = new FormulaJavaVisitor(env);
				//Logger.info("formula: "+formula.accept(v));
			} catch(Exception e) {
				Logger.warn(e);
			}
			int[] varIndices = formula.accept(new FormulaCollectVarVisitor()).getDataVarIndices(env);
			int[] unsafeVarIndices = formula.accept(new FormulaCollectUnsafeVarVisitor()).getDataVarIndices(env);
			//Logger.info("----");
			//Logger.info(Arrays.toString(varIndices)+"    "+Arrays.toString(unsafeVarIndices));
			Computation computation = formula.accept(new FormulaCompileVisitor(env));
			//Logger.info(computation.toString());
			return Mutators.getMutator(computation, iTarget, unsafeVarIndices);
		} catch(Exception e) {
			e.printStackTrace();
			Logger.error("could not create mutator: "+func+"    "+e);
			return null;
		}
	}
	
	public static Mutator getRawMutators(TsDB tsdb, Plot plot, String[] schema) {		
		ArrayList<Sensor> sensors = new ArrayList<Sensor>();
		ArrayList<String> funcs = new ArrayList<String>();
		for(Sensor sensor : tsdb.order_by_dependency(schema)) {
			String func = sensor.raw_func;
			if(sensor != null &&  func != null) {
				sensors.add(sensor);
				funcs.add(func);
			}
		}
		if(sensors.isEmpty()) {
			return null;
		}
		return getMutators(sensors, funcs, plot, schema);
	}

	public static Mutator getPostHourMutators(TsDB tsdb, Plot plot, String[] schema) {		
		ArrayList<Sensor> sensors = new ArrayList<Sensor>();
		ArrayList<String> funcs = new ArrayList<String>();
		for(Sensor sensor : tsdb.order_by_dependency(schema)) {
			String func = sensor.post_hour_func;
			if(sensor != null &&  func != null) {
				sensors.add(sensor);
				funcs.add(func);
			}
		}
		if(sensors.isEmpty()) {
			return null;
		}
		return getMutators(sensors, funcs, plot, schema);
	}

	public static Mutator getPostDayMutators(TsDB tsdb, Plot plot, String[] schema) {
		ArrayList<Sensor> sensors = new ArrayList<Sensor>();
		ArrayList<String> funcs = new ArrayList<String>();
		for(Sensor sensor : tsdb.order_by_dependency(schema)) {
			String func = sensor.post_day_func;
			if(sensor != null &&  func != null) {
				sensors.add(sensor);
				funcs.add(func);
			}
		}
		if(sensors.isEmpty()) {
			return null;
		}
		return getMutators(sensors, funcs, plot, schema);
	}
	
	public static Mutator getPostWeekMutators(TsDB tsdb, Plot plot, String[] schema) {
		ArrayList<Sensor> sensors = new ArrayList<Sensor>();
		ArrayList<String> funcs = new ArrayList<String>();
		for(Sensor sensor : tsdb.order_by_dependency(schema)) {
			String func = sensor.post_week_func;
			if(sensor != null &&  func != null) {
				sensors.add(sensor);
				funcs.add(func);
			}
		}
		if(sensors.isEmpty()) {
			return null;
		}
		return getMutators(sensors, funcs, plot, schema);
	}
	
	public static Mutator getPostMonthMutators(TsDB tsdb, Plot plot, String[] schema) {
		ArrayList<Sensor> sensors = new ArrayList<Sensor>();
		ArrayList<String> funcs = new ArrayList<String>();
		for(Sensor sensor : tsdb.order_by_dependency(schema)) {
			String func = sensor.post_month_func;
			if(sensor != null &&  func != null) {
				sensors.add(sensor);
				funcs.add(func);
			}
		}
		if(sensors.isEmpty()) {
			return null;
		}
		return getMutators(sensors, funcs, plot, schema);
	}
	
	public static Mutator getPostYearMutators(TsDB tsdb, Plot plot, String[] schema) {
		ArrayList<Sensor> sensors = new ArrayList<Sensor>();
		ArrayList<String> funcs = new ArrayList<String>();
		for(Sensor sensor : tsdb.order_by_dependency(schema)) {
			String func = sensor.post_year_func;
			if(sensor != null &&  func != null) {
				sensors.add(sensor);
				funcs.add(func);
			}
		}
		if(sensors.isEmpty()) {
			return null;
		}
		return getMutators(sensors, funcs, plot, schema);
	}
	
	public static Mutator getMutators(ArrayList<Sensor> sensors, ArrayList<String> funcs, Plot plot, String[] schema) {
		ArrayList<Mutator> mutators = new ArrayList<Mutator>();
		int len = sensors.size();
		for (int i = 0; i < len; i++) {
			Sensor sensor = sensors.get(i);
			String func = funcs.get(i);
			Mutator mutator = getMutator(sensor, func, plot, schema);
			if(mutator != null) {
				mutators.add(mutator);
			}
		}
		//Logger.info("funcs "+mutators.size()+"     "+funcs.toString());
		return Mutators.bundle(mutators);
	}
}
