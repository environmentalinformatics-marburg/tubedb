package tsdb.testing;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.graph.QueryPlan;
import tsdb.graph.node.Node;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;

public class Testing_P_RT_NRT {

	public static void main(String[] args) {
		TsDB tsdb = TsDBFactory.createDefault();
		String plotID = "AEG10";
		//String[] schema = new String[]{"P_RT_NRT"};
		String[] schema = new String[]{"P_RT_NRT"};
		//String[] schema = new String[]{"P_container_RT"};
		AggregationInterval aggregationInterval = AggregationInterval.DAY;
		DataQuality dataQuality = DataQuality.EMPIRICAL;
		boolean interpolated = false;
		Node node = QueryPlan.plot(tsdb, plotID, schema, aggregationInterval, dataQuality, interpolated);
		node.writeConsole();
	}

}
