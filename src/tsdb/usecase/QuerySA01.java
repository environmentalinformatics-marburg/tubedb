package tsdb.usecase;

import java.time.LocalDateTime;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.graph.QueryPlan;
import tsdb.graph.node.Node;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.TimeUtil;
import tsdb.util.iterator.TsIterator;

public class QuerySA01 {

	public static void main(String[] args) {
		TsDB tsdb = TsDBFactory.createDefault();
		
		Node node = QueryPlan.plot(tsdb,"SA01",null,AggregationInterval.RAW,DataQuality.Na,false);
		TsIterator it = node.get(null,TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(2014, 3,30, 0, 0)));
		//TsIterator it = node.get(null,null);
		it.writeConsole();

	}

}
