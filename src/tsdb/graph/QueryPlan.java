package tsdb.graph;

import java.util.Arrays;

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
import tsdb.graph.processing.InterpolatedAverageLinear;
import tsdb.graph.source.RawSource;
import tsdb.graph.source.VirtualPlotStationBase;
import tsdb.graph.source.VirtualPlotStationRawSource;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;

/**
 * With QueryPlan query graphs for specific queries a are build
 * @author woellauer
 *
 */
public final class QueryPlan {
	private static final Logger log = LogManager.getLogger();	
	private QueryPlan(){}

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
		if(plotID.indexOf(':')<0) { //plotID
			if(aggregationInterval!=AggregationInterval.RAW) { //plotID aggregated
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
					schema = QueryPlanGenerators.stationSchemaSupplement(tsdb, station, schema);
				}
				Node rawSource = RawSource.of(tsdb, plotID, schema);
				rawSource = QueryPlanGenerators.rawProcessing(tsdb, rawSource, schema, dataQuality);
				return rawSource;
			}			
		} else { // plotID:stationID 
			if(aggregationInterval!=AggregationInterval.RAW) { // plotID:stationID aggregated
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
				if(dataQuality==DataQuality.EMPIRICAL) {
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
		}
		return Aggregated.of(tsdb, continuous, aggregationInterval);
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
		return Aggregated.of(tsdb, continuous, aggregationInterval);
	}


}
