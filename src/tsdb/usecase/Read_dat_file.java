package tsdb.usecase;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.loader.be.TimeSeriesLoaderBE;
import tsdb.loader.be.UDBFTimestampSeries;
import tsdb.util.TimeUtil;

public class Read_dat_file {

	public static void main(String[] args) throws IOException {
		TsDB tsdb = TsDBFactory.createDefault();
		
		
		Path x = Paths.get("C:/timeseriesdatabase_data_source_be_tsm/AET06", "20130628_^b0_0009.dat");
		//Path x = Paths.get("C:/timeseriesdatabase_data_source_be_tsm/SET39", "20130917_^b0_0021.dat");
		//Path x = Paths.get("C:/timeseriesdatabase_data_source_be_tsm/JIG301", "20130110_^b0_0014.dat");
		
		System.out.println("read "+x);
		
		UDBFTimestampSeries ts = TimeSeriesLoaderBE.readUDBFTimeSeries("?", x );
		
		for(String sensorName:ts.getHeaderNames()) {
			System.out.println(sensorName);
		}
		
		long minTimestamp = TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(2008, 01, 01, 00, 00));
		TimeSeriesLoaderBE timeSeriesLoaderBE = new TimeSeriesLoaderBE(tsdb, minTimestamp);
		timeSeriesLoaderBE.loadDirectoryOfOneStation(tsdb.getStation("AET06"), Paths.get("C:/timeseriesdatabase_data_source_be_tsm/AET06"));
		timeSeriesLoaderBE.loadDirectoryOfOneStation(tsdb.getStation("SET39"), Paths.get("C:/timeseriesdatabase_data_source_be_tsm/SET39"));
		timeSeriesLoaderBE.loadDirectoryOfOneStation(tsdb.getStation("JIG301"), Paths.get("C:/timeseriesdatabase_data_source_be_tsm/JIG301"));

		tsdb.close();
	}

}
