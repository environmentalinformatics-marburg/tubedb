package tsdb.testing;

import java.nio.file.Paths;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.loader.ki.TimeSeriesLoaderKiLi;

public class Testing_Init_tsm_Database {

	public static void main(String[] args) {
		System.out.println("begin...");
		
		// *** workaround for not created database files ... 
		TsDB timeSeriesDatabase = TsDBFactory.createDefault();
		timeSeriesDatabase.clear();
		timeSeriesDatabase.close();
		
		timeSeriesDatabase = TsDBFactory.createDefault();
		TimeSeriesLoaderKiLi timeseriesloaderKiLi = new TimeSeriesLoaderKiLi(timeSeriesDatabase);
		//long minTimestamp = TimeConverter.DateTimeToOleMinutes(LocalDateTime.of(2008, 01, 01, 00, 00));
		//TimeSeriesLoaderBE timeseriesloaderBE = new TimeSeriesLoaderBE(timeSeriesDatabase, minTimestamp);
		
		//String kili_basepath = "c:/timeseriesdatabase_data_source_structure_kili_asc_variant/";
		/*String kili_basepath_short = "c:/timeseriesdatabase_data_source_structure_kili_SHORT/";
		timeSeriesDatabase.loadDirectoryOfAllExploratories_structure_kili(Paths.get(kili_basepath_short));*/
		
		System.gc();
		//timeseriesloaderBE.loadDirectory_with_stations_flat(Paths.get("c:/timeseriesdatabase_data_source_be_tsm"));
		System.gc();
		timeseriesloaderKiLi.loadDirectory_with_stations_flat(Paths.get("c:/timeseriesdatabase_data_source_ki_tsm"),true);
		System.gc();
		
		
		
		


		timeSeriesDatabase.close();
		System.out.println("...end");
		System.exit(0);
	}

}
