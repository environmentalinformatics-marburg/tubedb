package tsdb.run;

import java.nio.file.Paths;


import org.tinylog.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.loader.ki.TimeSeriesLoaderKiLi_manual_tfi;

/**
 * Load tfi files into database
 * @author woellauer
 *
 */
public class ImportTfi {
	

	
	public static void main(String[] args) {
		Logger.info("begin import tfi");
		

		TsDB tsdb = TsDBFactory.createDefault();
		TimeSeriesLoaderKiLi_manual_tfi TimeSerieaLoaderKiLi_manual_tfi = new TimeSeriesLoaderKiLi_manual_tfi(tsdb);
		
		Logger.info("from "+TsDBFactory.SOURCE_KI_TFI_PATH);
		long timeStartKItfi = System.currentTimeMillis();
		TimeSerieaLoaderKiLi_manual_tfi.loadOneDirectory_structure_kili_tfi(Paths.get(TsDBFactory.SOURCE_KI_TFI_PATH));
		long timeEndKItfi = System.currentTimeMillis();

		Logger.info("end import tfi");

		
		Logger.info((timeEndKItfi-timeStartKItfi)/1000+" s KI tfi import");
		
		tsdb.close();

	}

}
