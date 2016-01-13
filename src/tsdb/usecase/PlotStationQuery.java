package tsdb.usecase;

import java.util.Arrays;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.VirtualPlot;
import tsdb.graph.QueryPlan;
import tsdb.graph.node.Node;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.iterator.TimestampSeries;
import tsdb.util.iterator.TsIterator;

public class PlotStationQuery {

	public static void main(String[] args) {
		TsDB tsdb = TsDBFactory.createDefault();
		
		
		String plotID = "cof3";
		String stationID = "80091030030";
		String[] querySchema = new String[]{"Ta_200","Ts_10"};
		//String[] querySchema = null;
		
		VirtualPlot virtualPlot = tsdb.getVirtualPlot(plotID);
		System.out.println(Arrays.toString(virtualPlot.getStationIDs()));
		
		//VirtualPlotStationRawSource node = VirtualPlotStationRawSource.of(tsdb, plotID, stationID, querySchema);
		//VirtualPlotStationBase node = VirtualPlotStationBase.of(tsdb, plotID, stationID, querySchema, QueryPlan.getStationGen(tsdb, DataQuality.STEP));
		Node node = QueryPlan.plot(tsdb, plotID+":"+stationID, querySchema, AggregationInterval.DAY, DataQuality.STEP, false);
		TsIterator it = node.get(null, null);
		TimestampSeries tss = it.toTimestampSeries(plotID+":"+stationID);
		System.out.println(tss);
		
		
		
		tsdb.close();

	}

}
