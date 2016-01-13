package tsdb.usecase;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.graph.QueryPlanGenerators;
import tsdb.iterator.DayAggregationIterator;
import tsdb.iterator.MonthAggregationIterator;
import tsdb.iterator.YearAggregationIterator;
import tsdb.util.DataQuality;
import tsdb.util.iterator.TsIterator;

public class TestingNewAggregation {
	
	public static void main(String[] args) {
		System.out.println("begin...");
		
		TsDB tsdb = TsDBFactory.createDefault();
		
		TsIterator it = QueryPlanGenerators.getContinuousGen(tsdb, DataQuality.NO).get("HEG01", new String[]{"Ta_200"}).get(null, null);
		it = new DayAggregationIterator(tsdb, it);
		//it = new WeekAggregationIterator(tsdb, it);
		it = new MonthAggregationIterator(tsdb, it);
		it = new YearAggregationIterator(tsdb, it);
		it.writeConsole();
		System.out.println(it.getProcessingChain().getText());
		
		System.out.println("...end");
	}

}
