package tsdb.usecase;

import java.util.Arrays;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.streamdb.StreamIterator;
import tsdb.util.DataEntry;

public class QueryStationSensorNames {

	public static void main(String[] args) {
		TsDB tsdb = TsDBFactory.createDefault();
		
		String stationName = "8"; //fer0 -> station 8
		String[] sensorNames = tsdb.streamStorage.getSensorNames(stationName);
		System.out.println(Arrays.toString(sensorNames));
		
		StreamIterator it = tsdb.streamStorage.getRawSensorIterator(stationName, "tf_d18_16", null, null);
		while(it.hasNext()) {
			DataEntry e = it.next();
			System.out.println(e);
		}
		
		tsdb.close();

	}

}
