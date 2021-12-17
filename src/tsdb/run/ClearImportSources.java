package tsdb.run;

import java.nio.file.Paths;
import java.time.LocalDateTime;


import org.tinylog.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.loader.be.TimeSeriesLoaderBE;
import tsdb.loader.csv.ImportGenericCSV;
import tsdb.loader.ki.TimeSeriesLoaderKiLi_manual_tfi;
import tsdb.loader.mm.ImportGenericASC;
import tsdb.loader.ki.TimeSeriesLoaderKiLi;
import tsdb.loader.sa.SouthAfricaImport;
import tsdb.loader.sa_own.ImportSaOwn;
import tsdb.loader.sa_own.RemoveSouthAfricaStationBeginings;
import tsdb.util.TimeUtil;
import static tsdb.util.Util.msToText;

/**
 * first creates empty database
 * then loads data files into database
 * @author woellauer
 * 
 * deprecated. use: Terminal clear_import 
 *
 */
@Deprecated
public class ClearImportSources {
	

	public static void main(String[] args) {
		boolean import_BE = true;
		boolean import_KI = true;
		boolean import_KI_tfi = true;
		boolean import_SA = true;
		boolean import_SA_OWN = true;
		boolean import_MM = true;
		boolean import_BA = true;
		if(TsDBFactory.JUST_ONE_REGION==null){
			//all
		} else {
			import_BE = false;
			import_KI = false;
			import_KI_tfi = false;
			import_SA = false;
			import_SA_OWN = false;
			import_MM = false;
			import_BA = false;
			String oneRegion = TsDBFactory.JUST_ONE_REGION.toUpperCase();
			switch(oneRegion) {
			case "BE":
				import_BE = true;
				break;
			case "KI":
				import_KI = true;
				import_KI_tfi = true;
				break;
			case "SA":
				import_SA = true;
				break;
			case "SA_OWN":
				import_SA_OWN = true;
				break;
			case "MM":
				import_MM = true;
				break;
			case "BA":
				import_BA = true;
				break;				
			default:
				Logger.error("unknown region "+oneRegion);
				return;
			}
		}

		long timeStart = System.currentTimeMillis();
		Logger.info("begin import");

		Logger.info("open database");
		long timeStartClear = System.currentTimeMillis();
		TsDB tsdb = TsDBFactory.createDefault();
		Logger.info("clear database");
		tsdb.clear();
		tsdb.close();
		long timeEndClear = System.currentTimeMillis();

		Logger.info("reopen database");
		long timeStartOpen = System.currentTimeMillis();
		tsdb = TsDBFactory.createDefault();
		long timeEndOpen = System.currentTimeMillis();		

		long timeStartBE = 0;
		long timeEndBE = 0;
		long timeStartKI = 0;
		long timeEndKI = 0;
		long timeStartKItfi = 0;
		long timeEndKItfi = 0;
		long timeStartSA = 0;
		long timeEndSA = 0;
		long timeStartSA_OWN = 0;
		long timeEndSA_OWN = 0;
		long timeStartMM = 0;		
		long timeEndMM = 0;
		long timeStartBA = 0;
		long timeEndBA = 0;		

		System.gc();
		if(import_BE) { //*** BE
			Logger.info("import BE tsm");
			Logger.info("from "+TsDBFactory.SOURCE_BE_TSM_PATH);
			timeStartBE = System.currentTimeMillis();
			long minTimestamp = TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(2008, 01, 01, 00, 00));
			TimeSeriesLoaderBE timeseriesloaderBE = new TimeSeriesLoaderBE(tsdb, minTimestamp);
			timeseriesloaderBE.loadDirectory_with_stations_flat(Paths.get(TsDBFactory.SOURCE_BE_TSM_PATH));
			Logger.info("from "+TsDBFactory.SOURCE_BE_GEN_PATH);
			new ImportGenericCSV(tsdb).load(TsDBFactory.SOURCE_BE_GEN_PATH);	
			timeEndBE = System.currentTimeMillis();
			System.gc();
		}
		if(import_KI) { //*** KI
			Logger.info("import KI tsm");
			Logger.info("from "+TsDBFactory.SOURCE_KI_TSM_PATH);
			timeStartKI = System.currentTimeMillis();
			TimeSeriesLoaderKiLi timeseriesloaderKiLi = new TimeSeriesLoaderKiLi(tsdb);
			timeseriesloaderKiLi.loadDirectory_with_stations_recursive(Paths.get(TsDBFactory.SOURCE_KI_TSM_PATH), true, 2*60);
			timeEndKI = System.currentTimeMillis();
			System.gc();
		}
		if(import_KI_tfi) { //*** KI tfi			
			Logger.info("import KI tfi");
			Logger.info("from "+TsDBFactory.SOURCE_KI_TFI_PATH);
			timeStartKItfi = System.currentTimeMillis();
			TimeSeriesLoaderKiLi_manual_tfi TimeSerieaLoaderKiLi_manual_tfi = new TimeSeriesLoaderKiLi_manual_tfi(tsdb);
			TimeSerieaLoaderKiLi_manual_tfi.loadOneDirectory_structure_kili_tfi(Paths.get(TsDBFactory.SOURCE_KI_TFI_PATH));
			timeEndKItfi = System.currentTimeMillis();
			System.gc();
		}
		if(import_SA) { //*** SA
			Logger.info("import SA dat");
			Logger.info("from "+TsDBFactory.SOURCE_SA_DAT_PATH);
			timeStartSA = System.currentTimeMillis();
			new SouthAfricaImport(tsdb);
			timeEndSA = System.currentTimeMillis();
			System.gc();
		}
		if(import_SA_OWN) { //*** SA_OWN
			Logger.info("import SA_OWN");
			Logger.info("from "+TsDBFactory.SOURCE_SA_OWN_PATH);
			timeStartSA_OWN = System.currentTimeMillis();
			new ImportSaOwn(tsdb).load(TsDBFactory.SOURCE_SA_OWN_PATH);
			try {
				Logger.info("*remove South Africa Own Stations first measure days*");
				RemoveSouthAfricaStationBeginings.run(tsdb);
			} catch (Exception e) {
				Logger.error(e);
			}
			timeEndSA_OWN = System.currentTimeMillis();
			System.gc();
		}
		if(import_MM) { //*** MM
			Logger.info("import MM");
			Logger.info("from "+TsDBFactory.SOURCE_MM_PATH);
			timeStartMM = System.currentTimeMillis();
			new ImportGenericASC(tsdb).load(TsDBFactory.SOURCE_MM_PATH);
			timeEndMM = System.currentTimeMillis();
			System.gc();
		}
		if(import_BA) { //*** BA
			Logger.info("import BA");
			Logger.info("from "+TsDBFactory.SOURCE_BA_PATH);
			timeStartBA = System.currentTimeMillis();
			new ImportGenericASC(tsdb).load(TsDBFactory.SOURCE_BA_PATH);
			Logger.info("from "+TsDBFactory.SOURCE_BA_REF_PATH);
			new ImportGenericCSV(tsdb).load(TsDBFactory.SOURCE_BA_REF_PATH);			
			timeEndBA = System.currentTimeMillis();
			System.gc();
		}


		long timeStartClose = System.currentTimeMillis();
		tsdb.close();
		tsdb = null;
		System.gc();
		long timeEndClose = System.currentTimeMillis();
		long timeEnd = System.currentTimeMillis();

		Logger.info("end import");
		
		ClearLoadMasks.main(new String[0]);

		long timeStartAvg = System.currentTimeMillis();
		CreateStationGroupAverageCache.main(new String[0]);
		long timeEndAvg = System.currentTimeMillis();
		System.gc();
		/*long timeStartCompact = System.currentTimeMillis();
		RunCompact.main(new String[0]); // 'compact' not usable because of bug in MapDB.
		long timeEndCompact = System.currentTimeMillis();*/

		Logger.info(msToText(timeStartClear,timeEndClear)+" Clear");
		Logger.info(msToText(timeStartOpen,timeEndOpen)+" Open");
		Logger.info(msToText(timeStartBE,timeEndBE)+" BE import");
		Logger.info(msToText(timeStartKI,timeEndKI)+" KI import");
		Logger.info(msToText(timeStartKItfi,timeEndKItfi)+" KI tfi import");
		Logger.info(msToText(timeStartSA,timeEndSA)+" SA import");
		Logger.info(msToText(timeStartSA_OWN,timeEndSA_OWN)+" SA_OWN import");
		Logger.info(msToText(timeStartMM,timeEndMM)+" MM import");
		Logger.info(msToText(timeStartBA,timeEndBA)+" BA import");		
		Logger.info(msToText(timeStartClose,timeEndClose)+" Close");
		Logger.info(msToText(timeStart,timeEnd)+" total import");
		Logger.info("");
		Logger.info(msToText(timeStartAvg,timeEndAvg)+" create averages");
		//Logger.info(msToText(timeStartCompact,timeEndCompact)+" compact streamDB");
	}


}
