package tsdb.usecase;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.graph.QueryPlanGenerators;
import tsdb.graph.node.Continuous;
import tsdb.graph.processing.Subtraction;
import tsdb.util.DataQuality;

public class TestingAddition {
	
	public static void main(String[] args) {
		TsDB tsdb = TsDBFactory.createDefault();
		
		String plotID = "foc1";
		
		Continuous source = QueryPlanGenerators.getContinuousGen(tsdb, DataQuality.STEP).get(plotID, new String[]{"Ta_200"});
		source = Subtraction.createWithElevationTemperature(tsdb, source, plotID);
		source.writeConsole(null, null);
	}

}
