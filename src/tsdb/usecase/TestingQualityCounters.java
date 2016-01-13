package tsdb.usecase;

import java.rmi.RemoteException;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.graph.QueryPlan;
import tsdb.graph.node.Node;
import tsdb.iterator.ProjectionFillIterator;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.TsEntry;
import tsdb.util.iterator.TsIterator;

public class TestingQualityCounters {

	public static void main(String[] args) throws RemoteException {
		TsDB tsdb = TsDBFactory.createDefault();
		String plotID = "HEG01";
		String[] columnNames = new String[]{"SM_10","Ta_200","rH_200"};
		AggregationInterval aggregationInterval = AggregationInterval.MONTH;
		DataQuality dataQuality = DataQuality.Na;
		boolean interpolated = true;
		Node node = QueryPlan.plot(tsdb, plotID, columnNames, aggregationInterval, dataQuality, interpolated);		
		TsIterator it = node.get(null, null);
		/*NodeGen stationGen = QueryPlan.getStationGen(tsdb, dataQuality);
		TsIterator it = stationGen.get("HEG01", columnNames).get(null, null);*/
		/*ContinuousGen continuousGen = QueryPlan.getContinuousGen(tsdb, dataQuality);
		TsIterator it = Aggregated.of(tsdb, continuousGen.get("HEG01", columnNames), aggregationInterval).get(null, null);*/
		
		it = new ProjectionFillIterator(it,new String[]{"SM_10","parameter","Ta_200","rH_200"});
		while(it.hasNext()) {
			TsEntry e = it.next();
			System.out.println(e);
		}
		
		System.out.println(it.getSchema());
		System.out.println(it.getProcessingChain().getText());
		
		/*ServerTsDB serverTsDB = new ServerTsDB(tsdb);
		TimestampSeries x = serverTsDB.plot(null, plotID, columnNames, aggregationInterval, dataQuality, interpolated, null, null);
		System.out.println(x);*/
		

	}

}
