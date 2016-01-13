package tsdb.usecase;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tsdb.GeneralStation;
import tsdb.Station;
import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.VirtualPlot;
import tsdb.graph.QueryPlanGenerators;
import tsdb.graph.node.Continuous;
import tsdb.graph.node.ContinuousGen;
import tsdb.graph.processing.Differential;
import tsdb.util.DataQuality;
import tsdb.util.TsEntry;
import tsdb.util.iterator.TsIterator;
import tsdb.util.iterator.TsIteratorIterator;

public class CreateSteps {
	
	private static final String CSV_OUTPUT_PATH = "C:/timeseriesdatabase_output/";
	
	public static void main(String[] args) throws FileNotFoundException {
		System.out.println("start...");
		TsDB tsdb = TsDBFactory.createDefault();
		ContinuousGen continuousGen = QueryPlanGenerators.getContinuousGen(tsdb, DataQuality.PHYSICAL);

		for(String sensorName:tsdb.getBaseAggregationSensorNames()) {
			System.out.println("process: "+sensorName);
			String[] schema = new String[]{sensorName};
			List<TsIterator> iterator_list = new ArrayList<TsIterator>();
			
			List<String> stationNames = new ArrayList<String>();
			for(GeneralStation gs:tsdb.getGeneralStations()) {
				
				for(Station station:gs.stationList) {
					if(station.isValidBaseSchema(schema)){
						stationNames.add(station.stationID);
					}
				}
				
				for(VirtualPlot virtualPlot:gs.virtualPlots) {
					if(virtualPlot.isValidBaseSchema(schema)) {
						stationNames.add(virtualPlot.plotID);
					}
				}
				
			}
			
			List<String> insertedNames = new ArrayList<String>();
			
			for(String stationName:stationNames) {
				Continuous source = continuousGen.get(stationName, schema);
				TsIterator it = Differential.of(source).get(null, null);
				if(it!=null&&it.hasNext()) {
					iterator_list.add(it);
					insertedNames.add(stationName);
				}
			}
			System.out.println("included stations("+insertedNames.size()+"): "+insertedNames);
			if(!iterator_list.isEmpty()) {
				TsIteratorIterator result_iterator = new TsIteratorIterator(iterator_list,schema);
				FileOutputStream out = new FileOutputStream(CSV_OUTPUT_PATH+"Steps/"+sensorName);
				PrintStream printStream = new PrintStream(out);
				
				
				ArrayList<Float> result_list = new ArrayList<Float>();
				while(result_iterator.hasNext())  {
					TsEntry element = result_iterator.next();
					float value = element.data[0];
					if(!Float.isNaN(value)) {
						result_list.add(value);
					}
				}
				System.out.println("sort...");
				result_list.sort(null);
				System.out.println("final calc...");
				
				float prevValue = Float.NaN;
				float prevDiff = Float.NaN;
				long startIndex = result_list.size();
				startIndex*=999;
				startIndex/=1000;
				for(Float value:result_list.subList((int) startIndex, result_list.size())) {					
					float diff = value-prevValue;
					float diffdiff = diff-prevDiff;
					if(!Float.isNaN(diff)&&!Float.isNaN(diffdiff)) {
					printStream.format(Locale.ENGLISH,"%3.3f %3.5f %3.5f\n", value, diff, diffdiff);
					}					
					prevValue = value;
					prevDiff = diff;
				}
				printStream.close();
			}
		}


		System.out.println("...end");
	}

}
