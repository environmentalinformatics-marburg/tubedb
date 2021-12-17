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

public class DataCasted {
	

	public static void main(String[] args) {
		try(TsDB tsdb = TsDBFactory.createDefault()) {			
			//String[] plotIDs = new String[] {"AEG01", "AEG02", "AEG03"};
			String[] plotIDs = tsdb.getGeneralStation("AEG").getStationAndVirtualPlotNames().toArray(String[]::new);
			Logger.info(plotIDs.length + "   " + Arrays.toString(plotIDs));
			String[] schema = new String[] {"SWDR_300", "rH_200", "Ta_200"};
			AggregationInterval aggregationInterval = AggregationInterval.MONTH;
			DataQuality dataQuality = DataQuality.STEP;
			boolean interpolated = true;
			
			Timer.start("aggregated");
			Node casted = QueryPlan.plots_casted(tsdb, plotIDs, schema, aggregationInterval, dataQuality, interpolated);
			Logger.info(Arrays.toString(casted.getSchema()));
			casted.writeConsole();			
			Logger.info(Timer.stop("aggregated"));
			Logger.info(casted.get(null, null).getProcessingChain().getText());
			Logger.info(casted.get(null, null).getSchema());
			Logger.info(Arrays.toString(casted.get(null, null).toTimestampSeries("name").sensorNames));
		}
	}
}
