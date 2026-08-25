package tsdb.run;


import org.tinylog.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.run.command.LoadMasks;
import tsdb.run.command.LoadMasks.MASK_TYPE;

public class ClearLoadMasks {
	

	public static void main(String[] args) {
		Logger.info("load masks");
		
		TsDB tsdb = TsDBFactory.createDefault();

		for(String stationName:tsdb.streamStorage.getStationNames()) {
			tsdb.streamStorage.clearMaskOfStation(stationName);
			tsdb.streamStorage.clearSuspectMaskOfStation(stationName);
		}

		String path = TsDBFactory.CONFIG_PATH;


		if(TsDBFactory.JUST_ONE_REGION==null||TsDBFactory.JUST_ONE_REGION.toUpperCase().equals("BE")) { //*** BE
		    String fileName = path+"/be/"+LoadMasks.MASK_FILENAME;
		    LoadMasks.loadMask(tsdb, fileName, MASK_TYPE.BASIC);
		    String suspectFileName = path+"/be/"+LoadMasks.SUSPECT_MASK_FILENAME;
		    LoadMasks.loadMask(tsdb, suspectFileName, MASK_TYPE.SUSPECT);
		}

		if(TsDBFactory.JUST_ONE_REGION==null||TsDBFactory.JUST_ONE_REGION.toUpperCase().equals("KI")) { //*** KI
		    String fileName = path+"/ki/"+LoadMasks.MASK_FILENAME;
		    LoadMasks.loadMask(tsdb, fileName, MASK_TYPE.BASIC);
		    String suspectFileName = path+"/ki/"+LoadMasks.SUSPECT_MASK_FILENAME;
		    LoadMasks.loadMask(tsdb, suspectFileName, MASK_TYPE.SUSPECT);
		}

		if(TsDBFactory.JUST_ONE_REGION==null||TsDBFactory.JUST_ONE_REGION.toUpperCase().equals("SA")) {  //*** SA
		    String fileName = path+"/sa/"+LoadMasks.MASK_FILENAME;
		    LoadMasks.loadMask(tsdb, fileName, MASK_TYPE.BASIC);
		    String suspectFileName = path+"/sa/"+LoadMasks.SUSPECT_MASK_FILENAME;
		    LoadMasks.loadMask(tsdb, suspectFileName, MASK_TYPE.SUSPECT);
		}

		if(TsDBFactory.JUST_ONE_REGION==null||TsDBFactory.JUST_ONE_REGION.toUpperCase().equals("SA_OWN")) {  //*** SA_OWN
		    String fileName = path+"/sa_own/"+LoadMasks.MASK_FILENAME;
		    LoadMasks.loadMask(tsdb, fileName, MASK_TYPE.BASIC);
		    String suspectFileName = path+"/sa_own/"+LoadMasks.SUSPECT_MASK_FILENAME;
		    LoadMasks.loadMask(tsdb, suspectFileName, MASK_TYPE.SUSPECT);
		}

		if(TsDBFactory.JUST_ONE_REGION==null||TsDBFactory.JUST_ONE_REGION.toUpperCase().equals("MM")) {  //*** MM
		    String fileName = path+"/mm/"+LoadMasks.MASK_FILENAME;
		    LoadMasks.loadMask(tsdb, fileName, MASK_TYPE.BASIC);
		    String suspectFileName = path+"/mm/"+LoadMasks.SUSPECT_MASK_FILENAME;
		    LoadMasks.loadMask(tsdb, suspectFileName, MASK_TYPE.SUSPECT);
		}

		if(TsDBFactory.JUST_ONE_REGION==null||TsDBFactory.JUST_ONE_REGION.toUpperCase().equals("BA")) {  //*** BA
		    String fileName = path+"/ba/"+LoadMasks.MASK_FILENAME;
		    LoadMasks.loadMask(tsdb, fileName, MASK_TYPE.BASIC);
		    String suspectFileName = path+"/ba/"+LoadMasks.SUSPECT_MASK_FILENAME;
		    LoadMasks.loadMask(tsdb, suspectFileName, MASK_TYPE.SUSPECT);
		}



		//tsdb.streamStorage.setTimeSeriesMask(stationName, sensorName, timeSeriesMask);


		tsdb.close();

	}

	

}
