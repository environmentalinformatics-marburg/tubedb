package tsdb.usecase;

import java.util.Arrays;

import tsdb.StationProperties;
import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.VirtualPlot;
import tsdb.util.TimestampInterval;

public class QueryPlot {

	public static void main(String[] args) {
		System.out.println("begin...");

		TsDB tsdb = TsDBFactory.createDefault();


		VirtualPlot virtualPLot = tsdb.getVirtualPlot("cof5");

		for(TimestampInterval<StationProperties> entry:virtualPLot.intervalList) {
			StationProperties stationProperies = entry.value;
			if(stationProperies.get_logger_type_name().equals("pu1")) {
				System.out.println(stationProperies);
				String stationID = stationProperies.get_serial();
				System.out.println("stationID "+stationID);
				//Station station = tsdb.getStation(stationID);
				String[] it = tsdb.streamStorage.getSensorNames(stationID);
				System.out.println(Arrays.toString(it));
			}
		}





		tsdb.close();


		System.out.println("...end");

	}

}
