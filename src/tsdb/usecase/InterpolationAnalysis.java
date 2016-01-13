package tsdb.usecase;

import java.util.ArrayList;
import java.util.Arrays;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.graph.QueryPlanGenerators;
import tsdb.graph.node.Continuous;
import tsdb.graph.node.ContinuousGen;
import tsdb.graph.processing.Interpolated;
import tsdb.graph.processing.IntervalRemove;
import tsdb.util.DataQuality;
import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;
import tsdb.util.iterator.TsIterator;

public class InterpolationAnalysis {

	public static void main(String[] args) {
		
		//final int source_count = Interpolated.STATION_INTERPOLATION_COUNT;
		final int source_count = 6;
		
		TsDB tsdb = TsDBFactory.createDefault();

		ContinuousGen continuousGen = QueryPlanGenerators.getContinuousGen(tsdb, DataQuality.STEP);

		String[] schema = new String[]{"Ta_200"};
		String targetPlot = "SEG29";
		//String targetPlot = "HEG01";
		long start = TimeUtil.ofDateStartHour(2014,4);
		long end = TimeUtil.ofDateEndHour(2014,6);
		
		long removeStart = TimeUtil.ofDateStartHour(2014,6);
		long removeEnd = TimeUtil.ofDateEndHour(2014,6);


		ArrayList<String> result = new ArrayList<String>();

		Station targetStation = tsdb.getStation(targetPlot);
		
		String[] sourcePlots = targetStation.nearestStations.stream().limit(source_count).map(s->s.stationID).toArray(String[]::new);
		
		Continuous targetNode = continuousGen.get(targetStation.stationID, schema);
		Continuous targetNodeIntervalRemoved = IntervalRemove.of(targetNode, removeStart, removeEnd);
		
		Continuous[] sourceNodes = Arrays.stream(sourcePlots).map(s->continuousGen.get(s, schema)).toArray(Continuous[]::new);
		
		
		for(Continuous source:sourceNodes) {
			TsIterator it = source.get(start, end);
			int count = 0;
			while(it.hasNext()) {
				TsEntry e = it.next();
				if(Float.isFinite(e.data[0])) {
					count++;
				}
			}
			result.add(source.getSourceStation().stationID+"  "+count);
		}

		for(String e:result) {
			System.out.println(e);			
		}
		
		Continuous resultNode = Interpolated.of(tsdb, targetNodeIntervalRemoved, sourceNodes, schema);

		TsIterator it = resultNode.get(start, end);
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
		
		targetNode.writeCSV(removeStart, removeEnd, path+targetPlot+"_real.csv");
		resultNode.writeCSV(removeStart, removeEnd, path+targetPlot+"_interpolated.csv");
		sourceNodes[0].writeCSV(removeStart, removeEnd, path+sourceNodes[0].getSourceStation().stationID+"_real.csv");


	}

}
