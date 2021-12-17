package tsdb.testing;

import java.util.Arrays;


import org.tinylog.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.graph.QueryPlan;
import tsdb.graph.node.Node;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.Timer;

public class PlotAggregates {
	

	public static void main(String[] args) {
		try(TsDB tsdb = TsDBFactory.createDefault()) {			
			//String[] plotIDs = new String[] {"AEG01", "AEG02", "AEG03"};
			String[] plotIDs = tsdb.getGeneralStation("AEG").getStationAndVirtualPlotNames().toArray(String[]::new);
			Logger.info(plotIDs.length + "   " + Arrays.toString(plotIDs));
			String[] schema = new String[] {"SWDR_300", "Ta_200", "rH_200", "SM_10", "SM_20", "Ta_10", "Ts_05", "Ts_10", "Ts_20", "Ts_50"};
			AggregationInterval aggregationInterval = AggregationInterval.YEAR;
			DataQuality dataQuality = DataQuality.STEP;
			boolean interpolated = true;
			
			Timer.start("aggregated");
			Node aggregated = QueryPlan.plots_aggregate(tsdb, plotIDs, schema, aggregationInterval, dataQuality, interpolated);
			aggregated.writeConsole();
			Logger.info(Timer.stop("aggregated"));
		}
	}
}
