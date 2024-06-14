package tsdb.run.command;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import javax.validation.constraints.NotNull;

import org.tinylog.Logger;

import tsdb.ConfigLoader;
import tsdb.Station;
import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.component.Region;
import tsdb.util.Interval;
import tsdb.util.Table;
import tsdb.util.TimeSeriesMask;
import tsdb.util.TimeUtil;
import tsdb.util.AbstractTable.ColumnReaderIntFunc;
import tsdb.util.AbstractTable.ColumnReaderString;

public class LoadMasks {


	public static final String MASK_FILENAME = "mask.csv";

	private final TsDB tsdb;

	public static void main(String[] args) {
		try(TsDB tsdb = TsDBFactory.createDefault()) {
			LoadMasks updateMasks = new LoadMasks(tsdb);
			updateMasks.run(tsdb.configDirectory);
		} catch (Exception e) {
			Logger.error(e);
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
				//Logger.info("dir  "+path+"  "+path.getFileName());
				try {
					Region region = configLoader.readRegion(dir+"/region.ini", TsDBFactory.JUST_ONE_REGION);
					if(region!=null) {
						String fileName = dir+"/"+LoadMasks.MASK_FILENAME;
						LoadMasks.loadMask(tsdb, fileName);
					}
				} catch(Exception e) {
					Logger.info("could not load meta data of  "+path+"  "+e);
				}
			}
			//*** region config end

		} catch(Exception e) {
			Logger.error(e);
		}
	}


	public static void loadMask(TsDB tsdb, String filename) {
		try {
			if(!Files.exists(Paths.get(filename))) {
				Logger.trace("mask file not found: "+filename);
				return;
			}
			Table maskTable = Table.readCSV(filename, ',');

			ColumnReaderString colStation = maskTable.createColumnReader("station");
			ColumnReaderString colSensor = maskTable.createColumnReader("sensor");
			ColumnReaderIntFunc colStart = maskTable.createColumnReaderInt("start",TimeUtil::parseStartTimestamp);
			ColumnReaderIntFunc colEnd = maskTable.createColumnReaderInt("end",TimeUtil::parseEndTimestamp);

			for(String[] row:maskTable.rows) {
				if(Table.isNoComment(row) && row.length > 1) {
					try {
						String stationName = colStation.get(row);
						Station station = tsdb.getStation(stationName);
						if(station == null) {
							Logger.warn("mask: station not found " + stationName + "  at " + filename + "   in " + Arrays.toString(row));
						} else {
							int start = colStart.get(row);
							int end = colEnd.get(row);
							String sensorName = colSensor.get(row);
							if("*".equals(sensorName)) {
								String[] sensorNames = station.getSensorNames();
								for(String sn : sensorNames) {
									insertMask(tsdb, filename, row, stationName, sn, start, end);	
								}
							} else {
								insertMask(tsdb, filename, row, stationName, sensorName, start, end);
							}
						}
					} catch(Exception e) {
						Logger.error(e+" in "+Arrays.toString(row));
					}
				}
			}
			tsdb.streamStorage.commit();
			//Logger.info("\n"+maskTable);
		} catch(Exception e) {
			Logger.error(e);
		}
	}

	private static void insertMask(TsDB tsdb, String filename, String[] row, String stationName, String sensorName, int start, int end) {
		if(tsdb.streamStorage.existSensor(stationName, sensorName)) { 
			//Logger.info(TimeUtil.oleMinutesToText(start, end));				
			TimeSeriesMask mask = tsdb.streamStorage.getTimeSeriesMask(stationName, sensorName);
			if(mask==null) {
				mask = new TimeSeriesMask();
			}
			mask.addInterval(Interval.of(start, end));				
			tsdb.streamStorage.setTimeSeriesMask(stationName, sensorName, mask, false);			
		} else {
			Logger.warn("mask: sensor not found " + sensorName + "  at " + filename +"   in " + Arrays.toString(row));
		}
	}
}
