package tsdb.usecase;

import java.io.IOException;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.graph.QueryPlan;
import tsdb.graph.node.Node;
import tsdb.graph.processing.Statistics.StatisticsData;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.MiniCSV;
import tsdb.util.Util;

public class CreateStatistics {

	public static void main(String[] args) throws IOException {
		TsDB tsDB = TsDBFactory.createDefault();

		String sensorName = "Ta_200";


		MiniCSV minicsv = new MiniCSV(TsDBFactory.get_CSV_output_directory()+"statistics_"+sensorName+".csv");
		minicsv.writeString("plot");
		minicsv.writeString("avg");
		minicsv.writeString("cnt");
		minicsv.writeString("min");
		minicsv.writeString("max");
		minicsv.finishRow();

		for(String group:tsDB.getGeneralStationGroups()) {
			System.out.println("********** group:              "+group);
			String[] plots = tsDB.getStationAndVirtualPlotNames(group).toArray(String[]::new);
			for(String plotID:plots) {
				String[] schema = Util.toArray("Ta_200");
				if(tsDB.isValidSchema(plotID, schema)) {
					//Base base = Base.create(tsDB, plotID, schema, QueryPlan.getStationGen(tsDB, DataQuality.EMPIRICAL));
					Node node = QueryPlan.plot(tsDB, plotID, schema, AggregationInterval.YEAR, DataQuality.EMPIRICAL, false);
					tsdb.graph.processing.Statistics statistics = tsdb.graph.processing.Statistics.create(node);
					StatisticsData statisticsData = statistics.get(null, null);
					if(statisticsData!=null) {
						if(statisticsData.cnt[0]>0) {
							minicsv.writeString(plotID);
							minicsv.writeDouble(statisticsData.getAverage(0));
							minicsv.writeLong(statisticsData.cnt[0]);
							minicsv.writeDouble(statisticsData.min[0]);
							minicsv.writeDouble(statisticsData.max[0]);
							minicsv.finishRow();
							System.out.println(plotID+": avg="+statisticsData.getAverage(0)+"  std="+statisticsData.getStandardDeviation(0)+"  min="+statisticsData.min[0]+"  max="+statisticsData.max[0]+"  cnt="+statisticsData.cnt[0]);
						}
					}
				}
			}

		}
		minicsv.close();



	}

}
