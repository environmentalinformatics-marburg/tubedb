package tsdb.run;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.run.command.UpdateMasks;

public class ClearLoadMasks {
	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) {
		log.info("load masks");
		
		TsDB tsdb = TsDBFactory.createDefault();

		for(String stationName:tsdb.streamStorage.getStationNames()) {
			tsdb.streamStorage.clearMaskOfStation(stationName);
		}

		String path = TsDBFactory.CONFIG_PATH;


		if(TsDBFactory.JUST_ONE_REGION==null||TsDBFactory.JUST_ONE_REGION.toUpperCase().equals("BE")) { //*** BE
			String fileName = path+"/be/"+UpdateMasks.MASK_FILENAME;
			UpdateMasks.loadMask(tsdb, fileName);
		}

		if(TsDBFactory.JUST_ONE_REGION==null||TsDBFactory.JUST_ONE_REGION.toUpperCase().equals("KI")) { //*** KI
			String fileName = path+"/ki/"+UpdateMasks.MASK_FILENAME;
			UpdateMasks.loadMask(tsdb, fileName);
		}

		if(TsDBFactory.JUST_ONE_REGION==null||TsDBFactory.JUST_ONE_REGION.toUpperCase().equals("SA")) {  //*** SA
			String fileName = path+"/sa/"+UpdateMasks.MASK_FILENAME;
			UpdateMasks.loadMask(tsdb, fileName);
		}

		if(TsDBFactory.JUST_ONE_REGION==null||TsDBFactory.JUST_ONE_REGION.toUpperCase().equals("SA_OWN")) {  //*** SA_OWN
			String fileName = path+"/sa_own/"+UpdateMasks.MASK_FILENAME;
			UpdateMasks.loadMask(tsdb, fileName);
		}

		if(TsDBFactory.JUST_ONE_REGION==null||TsDBFactory.JUST_ONE_REGION.toUpperCase().equals("MM")) {  //*** MM
			String fileName = path+"/mm/"+UpdateMasks.MASK_FILENAME;
			UpdateMasks.loadMask(tsdb, fileName);
		}
		
		if(TsDBFactory.JUST_ONE_REGION==null||TsDBFactory.JUST_ONE_REGION.toUpperCase().equals("BA")) {  //*** BA
			String fileName = path+"/ba/"+UpdateMasks.MASK_FILENAME;
			UpdateMasks.loadMask(tsdb, fileName);
		}



		//tsdb.streamStorage.setTimeSeriesMask(stationName, sensorName, timeSeriesMask);


		tsdb.close();

	}

	

}
