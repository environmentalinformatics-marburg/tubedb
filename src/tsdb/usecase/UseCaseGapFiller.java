package tsdb.usecase;

import java.time.LocalDateTime;

import tsdb.util.TimeUtil;
import tsdb.util.iterator.CSV;
import tsdb.util.iterator.CSVTimeType;
import tsdb.util.iterator.Interpolator;
import tsdb.util.iterator.TimeSeries;

/**
 * use case of gap filler with synthetic data
 * @author woellauer
 *
 */
public class UseCaseGapFiller {
	
	static float getSyntheticValue(double stationIndex, int valueIndex) {
		return (float) (Math.random()*300d+stationIndex+valueIndex+500d*Math.sin(valueIndex*0.01d+stationIndex));
	}

	public static void main(String[] args) {
		System.out.println("start...");
		
		System.out.println("generate synthetic data...");
		
		final int INTERPOLATION_STATION_COUNT = 15;
		final int SOURCE_VALUE_COUNT = 24*7*30;
		final int TARGET_VALUE_COUNT = 24*7*20;
		
		
		long sourceStartTimestamp = TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(2013,01,01,0,0));
		
		float[][] source = new float[INTERPOLATION_STATION_COUNT][SOURCE_VALUE_COUNT];
		long targetStartTimestamp = TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(2013,03,01,0,0));
		float[] target = new float[TARGET_VALUE_COUNT];
		final int TIMEINTERVAL = 60;
		
		for(int stationIndex=0;stationIndex<INTERPOLATION_STATION_COUNT;stationIndex++) {
			for(int valueIndex=0;valueIndex<SOURCE_VALUE_COUNT;valueIndex++) {
				source[stationIndex][valueIndex] = getSyntheticValue(stationIndex,valueIndex);
			}
		}
		
		for(int valueIndex=0;valueIndex<TARGET_VALUE_COUNT;valueIndex++) {
			target[valueIndex] =  (float) getSyntheticValue(1.5,valueIndex);
		}
		
		//*** gaps ***
		
		int gapPos = 10*7*24+42;
		for(int i=0;i<100;i++) {
			gapPos++;
			System.out.println("create gap at "+gapPos+" value: "+target[gapPos]);		
			target[gapPos] = Float.NaN;
		}
		
		//***
		
		TimeSeries[] sourceBaseTimeSeries = new TimeSeries[INTERPOLATION_STATION_COUNT];
		for(int i=0;i<INTERPOLATION_STATION_COUNT;i++) {
			sourceBaseTimeSeries[i] = new TimeSeries(null, new String[]{"synthetic"}, sourceStartTimestamp, TIMEINTERVAL, new float[][]{source[i]}, null, null);
		}
		TimeSeries targetBaseTimeSeries = new TimeSeries(null, new String[]{"synthetic"}, targetStartTimestamp, TIMEINTERVAL, new float[][]{target}, null, null);
		
		
		//targetBaseTimeSeries.writeToCSV("c:/timeseriesdatabase_output/result.csv", " ", "NaN", CSVTimeType.TIMESTAMP_AND_DATETIME);
		
		
		
		
		
		System.out.println("start processing gab filling...");
		
    	//GapFiller.process(sourceStartTimestamp, source, targetStartTimestamp, target, TIMEINTERVAL);
		Interpolator.processMultiLinear(sourceBaseTimeSeries, targetBaseTimeSeries, "synthetic");
    	
    	System.out.println("write to file...");
    	
    	CSV.write(targetBaseTimeSeries,"c:/timeseriesdatabase_output/result.csv", " ", "NaN", CSVTimeType.TIMESTAMP_AND_DATETIME);

    	
    	
    	
		System.out.println("...end");
	}

}
