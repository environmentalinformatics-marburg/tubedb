package tsdb.usecase;

import java.util.ArrayList;

import tsdb.StationProperties;
import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.VirtualPlot;
import tsdb.streamdb.StreamTsIterator;
import tsdb.util.TimeUtil;
import tsdb.util.TimestampInterval;
import tsdb.util.iterator.TimestampSeries;
import tsdb.util.iterator.TsIterator;

public class TestingStationsOfPlot {
	
	/*
	 
	 view:
	 
	 plot/logger_type/station 
	 
	 
	 query_logger_types( plot )  ->  logger_types at plot
	 
	 query_stations( plot , logger_type ) ->  stations of logger_type at plot
	 
	 query_time_series( plot , station ) ->  time series of station at plot
	 
	 
	 
	 */
	
	
	
	
	public static void main(String[] args) {
		System.out.println("start...");
		TsDB tsdb = TsDBFactory.createDefault();
		//String plotID = "cof3";
		//String plotID = "nkw1";
		String plotID = "sav0";
		//String[] querySchema = new String[]{"Ta_200"};
		
		VirtualPlot virtualPlot = tsdb.getVirtualPlot(plotID);
		
		ArrayList<TimestampInterval<StationProperties>> infoList = new ArrayList<TimestampInterval<StationProperties>>();
		
		for(TimestampInterval<StationProperties> i:virtualPlot.intervalList) {
			
			//String stationID = i.value.get_serial();
			TsIterator it = StreamTsIterator.of(tsdb.streamStorage.getRawSensorIterator(i.value.get_serial(), "Ta_200", i.start, i.end));
			if(it!=null&&it.hasNext()) {
				TimestampSeries ts = it.toTimestampSeries("ts");
				infoList.add(new TimestampInterval<StationProperties>(i.value,ts.getFirstTimestamp(),ts.getLastTimestamp()));
				//infoList.add(TimeConverter.oleMinutesToText(ts.getFirstTimestamp(),ts.getLastTimestamp())+"  "+i.value.get_logger_type_name());
			}
			
			
			
			//System.out.println(i.value.get_serial()+"  "+i.value.get_logger_type_name()+"  "+TimeConverter.oleMinutesToText(i.start,i.end));
			//System.out.println(Arrays.toString(tsdb.streamStorage.getSensorNames(i.value.get_serial())));
			
		}
		
		
		tsdb.close();
		
		infoList.sort(TimestampInterval.START_END_COMPARATOR);
		
		System.out.println();
		for(TimestampInterval<StationProperties> i:infoList) {
			System.out.println(TimeUtil.oleMinutesToText(i.start,i.end)+"  "+i.value.get_logger_type_name()+"  "+i.value.get_serial());
		}
		System.out.println();
		

		System.out.println("...end");
	}

}
