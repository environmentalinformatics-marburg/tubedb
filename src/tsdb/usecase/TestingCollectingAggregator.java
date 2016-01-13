package tsdb.usecase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.graph.QueryPlanGenerators;
import tsdb.graph.node.Node;
import tsdb.iterator.DayCollectingAggregator;
import tsdb.iterator.EvaluatingAggregationIterator;
import tsdb.iterator.MonthCollectingAggregator;
import tsdb.util.DataQuality;
import tsdb.util.iterator.TsIterator;

public class TestingCollectingAggregator {
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) {
		TsDB tsdb = TsDBFactory.createDefault();

		Node node = QueryPlanGenerators.getStationGen(tsdb, DataQuality.STEP).get("HEG01", new String[]{"Ta_200"});
		//node.writeConsole(null, null);

		TsIterator it = node.get(null, null);

		//DayCollectingAggregator mca = new DayCollectingAggregator(it);
		MonthCollectingAggregator mca = new MonthCollectingAggregator(new DayCollectingAggregator(tsdb, it));
		EvaluatingAggregationIterator eai = new EvaluatingAggregationIterator(it.getSchema(),mca);
		
		while(eai.hasNext()) {
			System.out.println(eai.next());
		}

	}
}