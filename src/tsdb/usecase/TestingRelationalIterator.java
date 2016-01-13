package tsdb.usecase;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDBFactory;
import tsdb.streamdb.StreamDB;
import tsdb.streamdb.StreamIterator;
import tsdb.util.DataEntry;

public class TestingRelationalIterator {
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) {
		long time_start = System.currentTimeMillis();
		StreamDB streamDB = new StreamDB(TsDBFactory.STORAGE_PATH+"/"+"streamdb");
		{
			String stationName = "HEG01";
			String sensorName = "Ta_200";

			StreamIterator sit = streamDB.getSensorIterator(stationName, sensorName, Integer.MIN_VALUE, Integer.MAX_VALUE);
			
			while(sit.hasNext()) {
				DataEntry entry = sit.next();
				System.out.println(entry);
			}
			
			
			/*StreamIterator sit2 = streamDB.getSensorIterator(stationName, "rH_200", Integer.MIN_VALUE, Integer.MAX_VALUE);

			//RelationalIterator it = new RelationalIterator(new StreamIterator[]{sit,sit2},new String[]{"1","x","Ta_200","y","rH_200","f"});
			String[] sensorNames = new String[]{"Ta_200","rH_200","x"};
			int minTimestamp = Integer.MIN_VALUE;
			int maxTimestamp = Integer.MAX_VALUE;
			TsIterator it = streamDB.getTsIterator(stationName, sensorNames, minTimestamp, maxTimestamp);

			while(it.hasNext()) {
				TsEntry entry = it.next();
				System.out.println(entry);
			}*/
		}
		
		/*for(String stationName:streamDB.getStationNames()) {
			System.out.println(streamDB.getSensorNames(stationName));
		}*/


		/*

		String sensorName = "Ta_200";


		int count = 0;
		for(int i=10;i<25;i++) {
			String stationName = "HEG"+i;
			ArrayList<DataEntry> data = streamDB.readData(stationName, sensorName);
			for(DataEntry entry:data) {
				count++;
			}
		}

		long time_start_intern = System.currentTimeMillis();
		for(int i=10;i<25;i++) {
			String stationName = "HEG"+i;
			ArrayList<DataEntry> data = streamDB.readData(stationName, sensorName);
			for(DataEntry entry:data) {
				count++;
			}
		}
		long time_end_intern = System.currentTimeMillis();

		System.out.println("count "+count);
		 */

		streamDB.close();
		long time_end = System.currentTimeMillis();
		System.out.println(((time_end-time_start)/1000)+" s");
		//System.out.println(((time_end_intern-time_start_intern))+" ms (intern)");

	}

}
