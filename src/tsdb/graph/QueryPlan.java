package tsdb.graph;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.Plot;
import tsdb.Station;
import tsdb.TsDB;
import tsdb.graph.node.Base;
import tsdb.graph.node.Continuous;
import tsdb.graph.node.ContinuousGen;
import tsdb.graph.node.Node;
import tsdb.graph.node.NodeGen;
import tsdb.graph.processing.Aggregated;
import tsdb.graph.processing.Averaged;
import tsdb.graph.processing.DataCasted;
import tsdb.graph.processing.DataCastedRaw;
import tsdb.graph.processing.InterpolatedAverageLinear;
import tsdb.graph.processing.PostHourMutation;
import tsdb.graph.source.RawSource;
import tsdb.graph.source.VirtualPlotStationBase;
import tsdb.graph.source.VirtualPlotStationRawSource;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.Mutator;
import tsdb.util.Util;

/**
 * With QueryPlan query graphs for specific queries a are build
 * @author woellauer
 *
 */
public final class QueryPlan {
	private static final Logger log = LogManager.getLogger();	
	private QueryPlan(){}

	public static Node plots_aggregate(TsDB tsdb, String[] plotIDs, String[] schema, AggregationInterval aggregationInterval, DataQuality dataQuality, boolean interpolated) {
		if(aggregationInterval == AggregationInterval.RAW) {
			throw new RuntimeException("raw data not supported for plots_aggregate");
		} else {
			List<Continuous> sources = new ArrayList<Continuous>();
			for(String plotID : plotIDs) {
				String[] plotSchema = tsdb.getValidSchemaWithVirtualSensors(plotID, schema);
				if(!Util.empty(schema)) {
					Node node = QueryPlan.plot(tsdb, plotID, plotSchema, AggregationInterval.HOUR, dataQuality, interpolated);
					if(node != null) {
						sources.add((Continuous) node);
					}
				}
			}
			Continuous aggregated = Averaged.of(tsdb, sources, 1, true);
			if(aggregationInterval != AggregationInterval.HOUR) {
				Mutator postDayMutator = QueryPlanGenerators.getPostDayMutators(tsdb, null, schema);
				aggregated = Aggregated.of(tsdb, aggregated, aggregationInterval, postDayMutator);
			}
			return aggregated;
		}
	}
	
	public static Node plots_casted(TsDB tsdb, String[] plotIDs, String[] schema, AggregationInterval aggregationInterval, DataQuality dataQuality, boolean interpolated) {
		if(aggregationInterval == AggregationInterval.RAW) {
			List<Node> sources = new ArrayList<Node>();
			for(String plotID : plotIDs) {
				String[] plotSchema = tsdb.getValidSchemaWithVirtualSensors(plotID, schema);				
				if(!Util.empty(plotSchema)) {
					plotSchema = tsdb.supplementSchema(plotID, plotSchema);
					Node node = QueryPlan.plot(tsdb, plotID, plotSchema, aggregationInterval, dataQuality, interpolated);
					if(node != null) {
						sources.add(node);
					}
				}
			}
			DataCastedRaw casted = DataCastedRaw.of(tsdb, sources, schema);
			return casted;
		} else {
			List<Continuous> sources = new ArrayList<Continuous>();
			for(String plotID : plotIDs) {
				String[] plotSchema = tsdb.getValidSchemaWithVirtualSensors(plotID, schema);				
				if(!Util.empty(plotSchema)) {
					plotSchema = tsdb.supplementSchema(plotID, plotSchema);
					Node node = QueryPlan.plot(tsdb, plotID, plotSchema, aggregationInterval, dataQuality, interpolated);
					if(node != null) {
						sources.add((Continuous) node);
					}
				}
			}
			DataCasted casted = DataCasted.of(tsdb, sources, schema);
			return casted;
		}
	}

	/**
	 * Creates a general purpose graph for queries over one plot
	 * @param tsdb
	 * @param plotID
	 * @param columnName
	 * @param aggregationInterval
	 * @param dataQuality
	 * @param interpolated
	 * @return
	 */
	public static Node plot(TsDB tsdb, String plotID, String[] schema, AggregationInterval aggregationInterval, DataQuality dataQuality, boolean interpolated) {
		//log.info("schema "+Arrays.toString(schema));
		if(plotID.indexOf(':') < 0) { //plotID
			if(aggregationInterval != AggregationInterval.RAW) { //plotID aggregated
				return plotWithoutSubStation(tsdb, plotID, schema, aggregationInterval, dataQuality, interpolated);
			} else { //plotID raw
				if(dataQuality==DataQuality.EMPIRICAL) {
					log.warn("raw query empirical quality check not supported");
				}
				if(interpolated) {
					log.warn("raw query interpolation not supported");
				}
				Station station = tsdb.getStation(plotID);
				if(station!=null) {
					schema = station.stationSchemaSupplement(schema);
				}
				Node rawSource = RawSource.of(tsdb, plotID, schema);
				rawSource = QueryPlanGenerators.rawProcessing(tsdb, rawSource, schema, dataQuality);
				return rawSource;
			}			
		} else { // plotID:stationID 
			if(aggregationInterval != AggregationInterval.RAW) { // plotID:stationID aggregated
				if(dataQuality==DataQuality.EMPIRICAL) {
					dataQuality = DataQuality.STEP;
					log.warn("query of plotID:stationID: DataQuality.EMPIRICAL not supported");
				}
				if(interpolated) {
					interpolated = false;
					log.warn("query of plotID:stationID: interpolation not supported");
				}
				String[] parts = plotID.split(":");
				if(parts.length!=2) {
					log.error("not valid name: "+plotID);
					return null;
				}
				return plotWithSubStation(tsdb, parts[0], parts[1], schema, aggregationInterval, dataQuality);
			} else { // plotID:stationID raw
				if(dataQuality == DataQuality.EMPIRICAL) {
					log.warn("raw query empirical quality check not supported");
				}
				if(interpolated) {
					log.warn("raw query interpolation not supported");
				}
				String[] parts = plotID.split(":");
				if(parts.length!=2) {
					log.error("not valid name: "+plotID);
					return null;
				}
				Node rawSource =  VirtualPlotStationRawSource.of(tsdb, parts[0], parts[1], schema);
				rawSource = QueryPlanGenerators.rawProcessing(tsdb, rawSource, schema, dataQuality);
				return rawSource;
			}	
		}
	}

	/**
	 * Default processing for plot as station of virtual plot with station merge.
	 * @param tsdb
	 * @param plotID
	 * @param schema
	 * @param aggregationInterval
	 * @param dataQuality
	 * @param interpolated
	 * @return
	 */
	private static Node plotWithoutSubStation(TsDB tsdb, String plotID, String[] schema, AggregationInterval aggregationInterval, DataQuality dataQuality, boolean interpolated) {
		ContinuousGen continuousGen = QueryPlanGenerators.getContinuousGen(tsdb, dataQuality);
		Continuous continuous;
		if(interpolated) {
			continuous = InterpolatedAverageLinear.of(tsdb, plotID, schema, continuousGen, AggregationInterval.HOUR);
		} else {
			continuous = continuousGen.get(plotID, schema);
			//log.info("continuous "+continuous);
		}
		if(continuous == null) {
			return null;
		}
		Plot plot = tsdb.getPlot(plotID);
		Mutator postHourMutator = QueryPlanGenerators.getPostHourMutators(tsdb, plot, schema);
		continuous = new PostHourMutation(continuous, postHourMutator);
		Mutator postDayMutator = QueryPlanGenerators.getPostDayMutators(tsdb, plot, schema);
		return Aggregated.of(tsdb, continuous, aggregationInterval, postDayMutator);
	}

	/**
	 * Processing for virtual plot with one specific station.
	 * @param tsdb
	 * @param plotID
	 * @param stationID
	 * @param schema
	 * @param aggregationInterval
	 * @param dataQuality
	 * @return
	 */
	private static Node plotWithSubStation(TsDB tsdb, String plotID, String stationID, String[] schema, AggregationInterval aggregationInterval, DataQuality dataQuality) {
		NodeGen stationGen = QueryPlanGenerators.getStationGen(tsdb, dataQuality);
		Base base = VirtualPlotStationBase.of(tsdb, plotID, stationID, schema, stationGen);
		if(base==null) {
			return null;
		}
		Continuous continuous = Continuous.of(base);
		Plot plot = tsdb.getPlot(plotID);
		Mutator dayMutators = QueryPlanGenerators.getPostDayMutators(tsdb, plot, schema);
		return Aggregated.of(tsdb, continuous, aggregationInterval, dayMutators);
	}


}
