package tsdb.usecase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.graph.QueryPlan;
import tsdb.graph.node.Node;
import tsdb.graph.source.GroupAverageSource_NEW;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.TsEntry;
import tsdb.util.iterator.TsIterator;

@SuppressWarnings("unused")
public class TestingGroupAverageSource_NEW {
	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) {


		TsDB tsdb = TsDBFactory.createDefault();

		//GroupAverageSource_NEW source = GroupAverageSource_NEW.ofPlot(tsdb, "EXCELSIOR CERES",);
		String plotID = "EXCELSIOR CERES";
		String[] columnNames = new String[]{"rH_200"};
		AggregationInterval aggregationInterval = AggregationInterval.HOUR;
		DataQuality dataQuality = DataQuality.EMPIRICAL;
		boolean interpolated = false;
		Node source = QueryPlan.plot(tsdb, plotID, columnNames, aggregationInterval, dataQuality, interpolated);

		TsIterator it = source.get(null, null);
		if(it!=null) {
			while(it.hasNext()) {
				TsEntry e = it.next();
				System.out.println(e);
			}
			log.info(it.getProcessingChain().getText());
		}

	}

}
