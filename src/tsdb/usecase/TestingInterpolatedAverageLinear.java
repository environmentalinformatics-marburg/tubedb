package tsdb.usecase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.graph.QueryPlan;
import tsdb.graph.node.Node;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.iterator.TsIterator;

public class TestingInterpolatedAverageLinear {
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) {
		TsDB tsdb = TsDBFactory.createDefault();
		
		String plotID = "AEW05";
		String[] columnNames = new String[]{"Ta_200","Ts_50"};
		AggregationInterval aggregationInterval = AggregationInterval.HOUR;
		DataQuality dataQuality = DataQuality.PHYSICAL;
		boolean interpolated = true;;
		Node node = QueryPlan.plot(tsdb, plotID, columnNames, aggregationInterval, dataQuality, interpolated);


		TsIterator it = node.get(null, null);

		while(it.hasNext()) {
			System.out.println(it.next());
		}

	}
}
