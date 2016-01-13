package tsdb.run;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.util.Interval;
import tsdb.util.Table;
import tsdb.util.Table.ColumnReaderIntFunc;
import tsdb.util.Table.ColumnReaderString;
import tsdb.util.TimeSeriesMask;
import tsdb.util.TimeUtil;

public class ClearLoadMasks {
	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) {
		log.info("load masks");
		
		TsDB tsdb = TsDBFactory.createDefault();

		for(String stationName:tsdb.streamStorage.getStationNames()) {
			tsdb.streamStorage.clearMaskOfStation(stationName);
		}

		String path = TsDBFactory.CONFIG_PATH;
		String cf = "mask.csv";


		if(TsDBFactory.JUST_ONE_REGION==null||TsDBFactory.JUST_ONE_REGION.toUpperCase().equals("BE")) { //*** BE
			String fileName = path+"/be/"+cf;
			loadMask(tsdb, fileName);
		}

		if(TsDBFactory.JUST_ONE_REGION==null||TsDBFactory.JUST_ONE_REGION.toUpperCase().equals("KI")) { //*** KI
			String fileName = path+"/ki/"+cf;
			loadMask(tsdb, fileName);
		}

		if(TsDBFactory.JUST_ONE_REGION==null||TsDBFactory.JUST_ONE_REGION.toUpperCase().equals("SA")) {  //*** SA
			String fileName = path+"/sa/"+cf;
			loadMask(tsdb, fileName);
		}

		if(TsDBFactory.JUST_ONE_REGION==null||TsDBFactory.JUST_ONE_REGION.toUpperCase().equals("SA_OWN")) {  //*** SA_OWN
			String fileName = path+"/sa_own/"+cf;
			loadMask(tsdb, fileName);
		}

		if(TsDBFactory.JUST_ONE_REGION==null||TsDBFactory.JUST_ONE_REGION.toUpperCase().equals("MM")) {  //*** MM
			String fileName = path+"/mm/"+cf;
			loadMask(tsdb, fileName);
		}
		
		if(TsDBFactory.JUST_ONE_REGION==null||TsDBFactory.JUST_ONE_REGION.toUpperCase().equals("BA")) {  //*** BA
			String fileName = path+"/ba/"+cf;
			loadMask(tsdb, fileName);
		}



		//tsdb.streamStorage.setTimeSeriesMask(stationName, sensorName, timeSeriesMask);


		tsdb.close();

	}

	public static void loadMask(TsDB tsdb, String filename) {
		try {
			if(!Files.exists(Paths.get(filename))) {
				log.warn("mask file not found: "+filename);
				return;
			}
			Table maskTable = Table.readCSV(filename, ',');

			ColumnReaderString colStation = maskTable.createColumnReader("station");
			ColumnReaderString colSensor = maskTable.createColumnReader("sensor");
			ColumnReaderIntFunc colStart = maskTable.createColumnReaderInt("start",TimeUtil::parseStartTimestamp);
			ColumnReaderIntFunc colEnd = maskTable.createColumnReaderInt("end",TimeUtil::parseEndTimestamp);

			for(String[] row:maskTable.rows) {
				try {
					String stationName = colStation.get(row);					
					if(tsdb.getStation(stationName)==null) {
						log.warn("mask: station not found "+stationName+"  at "+filename+"   in "+Arrays.toString(row));
					}					
					String sensorName = colSensor.get(row);
					if(!tsdb.sensorExists(sensorName)) {
						log.warn("mask: sensor not found "+sensorName+"  at "+filename+"   in "+Arrays.toString(row));
					}
					int start = colStart.get(row);
					int end = colEnd.get(row);				
					//log.info(TimeUtil.oleMinutesToText(start, end));				
					TimeSeriesMask mask = tsdb.streamStorage.getTimeSeriesMask(stationName, sensorName);
					if(mask==null) {
						mask = new TimeSeriesMask();
					}
					mask.addInterval(Interval.of(start, end));				
					tsdb.streamStorage.setTimeSeriesMask(stationName, sensorName, mask);
				} catch(Exception e) {
					log.error(e+" in "+Arrays.toString(row));
				}
			}

			log.info("\n"+maskTable);
		} catch(Exception e) {
			log.error(e);
		}
	}

}
