package tsdb.usecase;


import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.graph.QueryPlan;
import tsdb.graph.node.Node;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.iterator.TsIterator;

public class TestingInterpolation {
	
	public static void main(String[] args) {
		
	//	logger.error("_...__----_-----");
		
		
		TsDB tsdb = TsDBFactory.createDefault();
		String plotID = "cof1";
		//String plotID = "gra1";
		//String plotID = "sun1";
		//String plotID = "cof3";
		String[] columnNames = new String[]{"Ta_200"};
		AggregationInterval aggregationInterval = AggregationInterval.HOUR;
		DataQuality dataQuality = DataQuality.EMPIRICAL;
		boolean interpolated = true;
		Node ts = QueryPlan.plot(tsdb, plotID, columnNames, aggregationInterval, dataQuality, interpolated);
		TsIterator it = ts.get(null, null);
		
		while(it.hasNext()) {
			System.out.println(it.next());
		}
		
		
		
		System.out.println(it.getSchema());
		
		

	}

}
