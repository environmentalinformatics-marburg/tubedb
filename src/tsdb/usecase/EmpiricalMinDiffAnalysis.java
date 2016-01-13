package tsdb.usecase;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.graph.QueryPlan;
import tsdb.graph.QueryPlanGenerators;
import tsdb.graph.node.Continuous;
import tsdb.graph.node.ContinuousGen;
import tsdb.graph.processing.Addition;
import tsdb.graph.processing.Averaged;
import tsdb.graph.processing.Difference;
import tsdb.graph.processing.Differential;
import tsdb.graph.processing.Middle;
import tsdb.graph.processing.MinDiff;
import tsdb.graph.processing.TransformLinear;
import tsdb.graph.source.GroupAverageSource_NEW;
import tsdb.util.DataQuality;
import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;
import tsdb.util.Util;
import tsdb.util.iterator.TsIterator;

@SuppressWarnings("unused")
public class EmpiricalMinDiffAnalysis {
	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) {
		TsDB tsdb = TsDBFactory.createDefault();

		ContinuousGen continuousGen = QueryPlanGenerators.getContinuousGen(tsdb, DataQuality.STEP);

		String sensorName = "Ta_200";

		//String plotID = "HEW03";
		//String plotID = "SEG29";
		//String plotID = "AEG11";
		String plotID = "cof3";

		Long start = null;
		Long end = null;
		/*Long start = TimeConverter.ofDateStartHour(2012,7);
		Long end = TimeConverter.ofDateEndHour(2012,7);*/
		/*Long start = TimeConverter.ofDateStartHour(2010);
		Long end = TimeConverter.ofDateEndHour(2010);*/

		Continuous source = continuousGen.getWithSensorNames(plotID,sensorName);
		Addition addSource = Addition.createWithElevationTemperature(tsdb, source, plotID);
		if(addSource!=null) {
			source = addSource;
		}
		//source = TransformLinear.of(source,0.113581f, -0.7146292f);

		//Continuous compareSource = GroupAverageSource_NEW.ofPlot(tsdb, plotID, sensorName);

		/*Map<String,float[]> linMap = new HashMap<String,float[]>();
		linMap.put("HEW05", new float[]{0.1128173f, -0.7186393f});
		linMap.put("HEW06", new float[]{0.1142325f, -0.7106393f});
		linMap.put("HEW13", new float[]{0.113581f, -0.7146292f});*/

		//Stream<String> nearestPlots = source.getSourceStation().nearestStations.stream().map(s->s.stationID);
		//Stream<String> nearestPlots = source.getSourceVirtualPlot().nearestVirtualPlots.stream().map(s->s.stationID);

		List<Continuous> compares = source.getSourcePlot().getNearestPlots()
				.limit(6)
				.map(p->{
					log.info(p.getPlotID());				
					String[] validSensorNames = p.getValidSensorNames(new String[]{sensorName});
					if(validSensorNames.length==0) {
						return null;
					}
					//float[] lin = linMap.get(p.getPlotID());
					Continuous con = continuousGen.getWithSensorNames(p.getPlotID(), validSensorNames);
					//con = TransformLinear.of(con,lin[0],lin[1]);
					if(con==null) {
						return null;
					}
					Addition add = Addition.createWithElevationTemperature(tsdb, con, p.getPlotID());
					if(add!=null) {
						return add;
					}
					return con;
				})
				.filter(Util::notNull)
				.collect(Collectors.toList());
		
		//compares.clear();
		//compares.add(GroupAverageSource_NEW.ofPlot(tsdb, plotID, "Ta_200"));

		//Continuous compareSource = Averaged.of(tsdb, compares, 3);
		/*Continuous compareSource = Middle.of(tsdb, compares);

		Continuous difference = Difference.of(tsdb, source, compareSource, plotID, false);*/

		Continuous difference = MinDiff.of(source, compares);

		Continuous differential = Differential.of(difference);

		Continuous resultNode = difference;

		TsIterator it = resultNode.get(null, null);
		int count = 0;
		while(it.hasNext()) {
			TsEntry e = it.next();
			if(Float.isFinite(e.data[0])) {
				count++;
			}
			//System.out.println(e);			
		}
		System.out.println(""+count);

		String path = "C:/timeseriesdatabase_R/";

		source.writeCSV(start, end, path+"EmpiricalMinDiffAnalysis_plot.csv");
		difference.writeCSV(start, end, path+"EmpiricalMinDiffAnalysis_diff.csv");
		differential.writeCSV(start, end, path+"EmpiricalMinDiffAnalysis_derivation.csv");

		for (int i = 0; i < compares.size(); i++) {
			compares.get(i).writeCSV(start, end, path+"EmpiricalMinDiffAnalysis_"+i+"_compare.csv");
		}
	}
}
