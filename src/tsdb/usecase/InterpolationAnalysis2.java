package tsdb.usecase;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.Plot;
import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.graph.QueryPlanGenerators;
import tsdb.graph.node.Continuous;
import tsdb.graph.node.ContinuousGen;
import tsdb.graph.processing.Aggregated;
import tsdb.graph.processing.Averaged;
import tsdb.graph.processing.InterpolatedAverageLinear;
import tsdb.graph.processing.IntervalRemove;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.TimeUtil;

public class InterpolationAnalysis2 {
	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) {
		try(TsDB tsdb = TsDBFactory.createDefault()) {
			run(tsdb);
		}		
	}

	public static void run(TsDB tsdb) {
		String[] schema = new String[]{"Ta_200"};
		//String targetPlot = "SEG29";
		//String targetPlot = "AEG02";
		String targetPlot = "AEG35";
		//String targetPlot = "sav5";
		//String targetPlot = "foc2"; // <----
		DataQuality dataQuality = DataQuality.EMPIRICAL;
		//long gapStart = TimeUtil.ofDateStartHour(2014,6);
		//long gapEnd = TimeUtil.ofDateEndHour(2014,6);
		long gapStart = TimeUtil.ofDateStartHour(2014);
		long gapEnd = TimeUtil.ofDateEndHour(2014);
		String path = "C:/timeseriesdatabase_R/interpolation_analysis/";
		int meanPlotCount = 5;
		AggregationInterval aggregationInterval = AggregationInterval.HOUR;

		ContinuousGen continuousGen = QueryPlanGenerators.getContinuousGen(tsdb, dataQuality);
		ContinuousGen gapGen = (String stationName, String[] genSchema) -> {
			Continuous node = continuousGen.get(stationName, schema);
			if(stationName.equals(targetPlot)) {
				return IntervalRemove.of(node, gapStart, gapEnd);
			} else {
				return node;
			}
		};
		
		List<Plot> nearPlots = tsdb.getPlot(targetPlot).getNearestPlots().collect(Collectors.toList());	
		log.info("near plots "+nearPlots.size()+":  "+nearPlots);
		
		Continuous realNode = continuousGen.get(targetPlot, schema);
		Continuous interpolatedNode = InterpolatedAverageLinear.of(tsdb, targetPlot, schema, gapGen, AggregationInterval.HOUR);
		
		List<Continuous> sources = nearPlots.stream().limit(meanPlotCount).map(plot->continuousGen.get(plot.getPlotID(), schema)).filter(p->p!=null).collect(Collectors.toList());
		Continuous nearNode = sources.get(0);
		int minCount = 1;
		Continuous meanNode = Averaged.of(tsdb, sources, minCount); 
		

		

		/*realNode.writeCSV(gapStart, gapEnd, path+targetPlot+"_real.csv");
		interpolatedNode.writeCSV(gapStart, gapEnd, path+targetPlot+"_interpolated.csv");
		meanNode.writeCSV(gapStart, gapEnd, path+targetPlot+"_mean.csv");
		nearNode.writeCSV(gapStart, gapEnd, path+nearPlot+"_real.csv");*/
		
		Aggregated.of(tsdb, realNode, aggregationInterval).writeCSV(gapStart, gapEnd, path+"real.csv");
		Aggregated.of(tsdb, interpolatedNode, aggregationInterval).writeCSV(gapStart, gapEnd, path+"interpolated.csv");
		Aggregated.of(tsdb, meanNode, aggregationInterval).writeCSV(gapStart, gapEnd, path+"mean.csv");
		Aggregated.of(tsdb, nearNode, aggregationInterval).writeCSV(gapStart, gapEnd, path+"near.csv");
		
		Aggregated.of(tsdb, realNode, aggregationInterval).writeCSV(null, null, path+"real_full.csv");
		Aggregated.of(tsdb, nearNode, aggregationInterval).writeCSV(null, null, path+"near_full.csv");
		

	}

}
