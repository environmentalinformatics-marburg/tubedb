package tsdb.usecase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.graph.QueryPlan;
import tsdb.graph.QueryPlanGenerators;
import tsdb.graph.node.Continuous;
import tsdb.graph.node.ContinuousGen;
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
import tsdb.util.iterator.TsIterator;

@SuppressWarnings("unused")
public class EmpiricalDiffAnalysis {
	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) {
		
		/*Properties table = System.getProperties();
		for(Entry<Object, Object> p:table.entrySet()) {
			System.out.println(p);
		}*/

		TsDB tsdb = TsDBFactory.createDefault();

		ContinuousGen continuousGen = QueryPlanGenerators.getContinuousGen(tsdb, DataQuality.STEP);
		
		String sensorName = "Ta_200";
		
		/*String general = "HEW";
		String plotID = general+"03";*/
		String general = "SEG";
		String plotID = general+"29";
		
		Long start = null;
		Long end = null;
		/*Long start = TimeConverter.ofDateStartHour(2012,7);
		Long end = TimeConverter.ofDateEndHour(2012,7);*/
		/*Long start = TimeConverter.ofDateStartHour(2010);
		Long end = TimeConverter.ofDateEndHour(2010);*/
		
		Continuous source = continuousGen.getWithSensorNames(plotID,sensorName);
		//source = TransformLinear.of(source,0.113581f, -0.7146292f);
		
		//Continuous compareSource = GroupAverageSource_NEW.ofPlot(tsdb, plotID, sensorName);
		
		Map<String,float[]> linMap = new HashMap<String,float[]>();
		linMap.put("HEW05", new float[]{0.1128173f, -0.7186393f});
		linMap.put("HEW06", new float[]{0.1142325f, -0.7106393f});
		linMap.put("HEW13", new float[]{0.113581f, -0.7146292f});
		
		List<Continuous> compares = source.getSourceStation().nearestStations.stream()
				.limit(3)
				.map(s->{log.info(s.stationID);
					float[] lin = linMap.get(s.stationID);
					Continuous con = continuousGen.getWithSensorNames(s.stationID, sensorName);
					//con = TransformLinear.of(con,lin[0],lin[1]);
					return con;
					})
				.collect(Collectors.toList());
		
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

		source.writeCSV(start, end, path+"EmpiricalDiffAnalysis_"+plotID+".csv");
		difference.writeCSV(start, end, path+"EmpiricalDiffAnalysis_"+plotID+"_diff.csv");
		differential.writeCSV(start, end, path+"EmpiricalDiffAnalysis_"+plotID+"_derivation.csv");
		//compareSource.writeCSV(start, end, path+"EmpiricalDiffAnalysis_"+general+".csv");
		
		for(Continuous compare:compares) {
			compare.writeCSV(start, end, path+"EmpiricalDiffAnalysis_"+compare.getSourceStation().stationID+"_compare.csv");
		}

	}
}
