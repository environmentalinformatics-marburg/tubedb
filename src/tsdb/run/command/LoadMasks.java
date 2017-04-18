package tsdb.run.command;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.ConfigLoader;
import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.component.Region;
import tsdb.util.Interval;
import tsdb.util.Table;
import tsdb.util.TimeSeriesMask;
import tsdb.util.TimeUtil;
import tsdb.util.Table.ColumnReaderIntFunc;
import tsdb.util.Table.ColumnReaderString;

public class LoadMasks {
	private static final Logger log = LogManager.getLogger();

	public static final String MASK_FILENAME = "mask.csv";

	private final TsDB tsdb;

	public static void main(String[] args) {
		try(TsDB tsdb = TsDBFactory.createDefault()) {
			LoadMasks updateMasks = new LoadMasks(tsdb);
			updateMasks.run(tsdb.configDirectory);
		} catch (Exception e) {
			log.error(e);
		}		
	}

	public LoadMasks(TsDB tsdb) {
		this.tsdb = tsdb;
	}	

	public void run(String configDirectory) {

		try {

			ConfigLoader configLoader = new ConfigLoader(tsdb);

			//*** region config start
			for(Path path : Files.newDirectoryStream(Paths.get(configDirectory), path->path.toFile().isDirectory())) {
				String dir = path.toString();
				//log.info("dir  "+path+"  "+path.getFileName());
				try {
					Region region = configLoader.readRegion(dir+"/region.ini", TsDBFactory.JUST_ONE_REGION);
					if(region!=null) {
						String fileName = dir+"/"+LoadMasks.MASK_FILENAME;
						LoadMasks.loadMask(tsdb, fileName);
					}
				} catch(Exception e) {
					log.info("could not load meta data of  "+path+"  "+e);
				}
			}
			//*** region config end

		} catch(Exception e) {
			log.error(e);
		}
	}


	public static void loadMask(TsDB tsdb, String filename) {
		try {
			if(!Files.exists(Paths.get(filename))) {
				log.trace("mask file not found: "+filename);
				return;
			}
			Table maskTable = Table.readCSV(filename, ',');

			ColumnReaderString colStation = maskTable.createColumnReader("station");
			ColumnReaderString colSensor = maskTable.createColumnReader("sensor");
			ColumnReaderIntFunc colStart = maskTable.createColumnReaderInt("start",TimeUtil::parseStartTimestamp);
			ColumnReaderIntFunc colEnd = maskTable.createColumnReaderInt("end",TimeUtil::parseEndTimestamp);

			for(String[] row:maskTable.rows) {
				if(Table.isNoComment(row)) {
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
			}

			//log.info("\n"+maskTable);
		} catch(Exception e) {
			log.error(e);
		}
	}

}
