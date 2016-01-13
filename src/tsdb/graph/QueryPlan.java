package tsdb.graph;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDB;
import tsdb.graph.node.Base;
import tsdb.graph.node.Continuous;
import tsdb.graph.node.ContinuousGen;
import tsdb.graph.node.Node;
import tsdb.graph.node.NodeGen;
import tsdb.graph.node.RawSource;
import tsdb.graph.processing.Aggregated;
import tsdb.graph.processing.InterpolatedAverageLinear;
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
	public static Node plot(TsDB tsdb, String plotID, String[] columnNames, AggregationInterval aggregationInterval, DataQuality dataQuality, boolean interpolated) {
		String[] schema = columnNames;

		if(plotID.indexOf(':')<0) { //plotID without sub station
			if(aggregationInterval!=AggregationInterval.RAW) { // aggregated
				return plotWithoutSubStation(tsdb, plotID, columnNames, aggregationInterval, dataQuality, interpolated);
			} else { // raw
				if(dataQuality!=DataQuality.NO&&dataQuality!=DataQuality.Na) {
					dataQuality = DataQuality.NO;
					log.warn("raw query quality check not supported");
				}
				if(interpolated) {
					interpolated = false;
					log.warn("raw query interpolation not supported");
				}
				return RawSource.of(tsdb, plotID, schema);
			}			
		} else { // plotID of structure plotID:stationID
			if(aggregationInterval!=AggregationInterval.RAW) { // aggregated
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
			} else { // raw
				if(dataQuality!=DataQuality.NO&&dataQuality!=DataQuality.Na) {
					dataQuality = DataQuality.NO;
					log.warn("raw query quality check not supported");
				}
				if(interpolated) {
					interpolated = false;
					log.warn("raw query interpolation not supported");
				}
				String[] parts = plotID.split(":");
				if(parts.length!=2) {
					log.error("not valid name: "+plotID);
					return null;
				}
				return VirtualPlotStationRawSource.of(tsdb, parts[0], parts[1], schema);
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
		/*if(aggregationInterval.isDay()) {
			ContinuousGen dayGen = QueryPlanGenerators.getDayAggregationGen(tsdb, dataQuality);
			if(interpolated) {
				//continuous = Interpolated.of(tsdb, plotID, schema, dayGen);
				Continuous interpolatedNode = InterpolatedAverageLinear.of(tsdb, plotID, schema, dayGen, AggregationInterval.DAY);
				return QueryPlanGenerators.elementCopy(interpolatedNode, schema);
			} else {
				Continuous dayNode = dayGen.get(plotID, schema);
				return QueryPlanGenerators.elementCopy(dayNode, schema);
			}
		} else {*/

		ContinuousGen continuousGen = QueryPlanGenerators.getContinuousGen(tsdb, dataQuality);
		Continuous continuous;
		if(interpolated) {
			//continuous = Interpolated.of(tsdb, plotID, schema, continuousGen);
			continuous = InterpolatedAverageLinear.of(tsdb, plotID, schema, continuousGen, AggregationInterval.HOUR);
			continuous = QueryPlanGenerators.elementCopy(continuous);
		} else {
			continuous = continuousGen.get(plotID, schema);
			continuous = QueryPlanGenerators.elementCopy(continuous);
		}
		return Aggregated.of(tsdb, continuous, aggregationInterval);
		//}
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
		continuous = QueryPlanGenerators.elementCopy(continuous);
		return Aggregated.of(tsdb, continuous, aggregationInterval);
	}


}
