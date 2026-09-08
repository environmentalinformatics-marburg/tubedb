package tsdb.run.command;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.tinylog.Logger;

import tsdb.ConfigLoader;
import tsdb.Station;
import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.component.Region;
import tsdb.run.command.LoadMasks.MaskType;
import tsdb.util.AbstractTable.ColumnReaderIntFunc;
import tsdb.util.AbstractTable.ColumnReaderString;
import tsdb.util.Interval;
import tsdb.util.Table;
import tsdb.util.TimeSeriesMask;
import tsdb.util.TimeUtil;

public class LoadMasks {

	public static final String MASK_FILENAME = "mask.csv";
	public static final String SUSPECT_MASK_FILENAME = "suspect_mask.csv";

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
			
			if(true) { // root masks
				String fileName = configDirectory+LoadMasks.MASK_FILENAME;
			    LoadMasks.loadMask(tsdb, fileName, MaskType.INVALID);
			    String suspectFileName = configDirectory+LoadMasks.SUSPECT_MASK_FILENAME;
			    LoadMasks.loadMask(tsdb, suspectFileName, MaskType.SUSPECT);
			}

			//*** region config start
			for(Path path : Files.newDirectoryStream(Paths.get(configDirectory), path->path.toFile().isDirectory())) {
				String dir = path.toString();
				//Logger.info("dir  "+path+"  "+path.getFileName());
				try {
					Region region = configLoader.readRegion(dir+"/region.ini", TsDBFactory.JUST_ONE_REGION);
					if(region != null) {
						String fileName = dir+"/"+LoadMasks.MASK_FILENAME;
						LoadMasks.loadMask(tsdb, fileName, MaskType.INVALID);
						String suspectFileName = dir+"/"+LoadMasks.SUSPECT_MASK_FILENAME;
						LoadMasks.loadMask(tsdb, suspectFileName, MaskType.SUSPECT);
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

	public static enum MaskType {
	    INVALID("invalid"),
	    SUSPECT("suspect");

	    private final String typeText;

	    MaskType(String typeString) {
	        this.typeText = typeString;
	    }

	    public String getTypeText() {
	        return typeText;
	    }

	    public static MaskType fromText(String text) {
	        if (text != null) {
	        	 for (MaskType type : MaskType.values()) {
	 	            if (type.typeText.equalsIgnoreCase(text)) {
	 	                return type;
	 	            }
	 	        }
	        }
	      throw new IllegalArgumentException("Unknown mask type: " + text);
	    }
	}


	public static void loadMask(TsDB tsdb, String filename, MaskType maskType) {
		try {
			if(!Files.exists(Paths.get(filename))) {
				Logger.info("mask file not found: "+filename);
				return;
			}

			Logger.info("load mask " + maskType + " from " + filename);

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
							Logger.info("insert " + Arrays.toString(row));
							if("*".equals(sensorName)) {
								String[] sensorNames = station.getSensorNames();
								for(String sn : sensorNames) {
									insertMask(tsdb, filename, row, stationName, sn, start, end, maskType, false);	
								}
							} else {
								insertMask(tsdb, filename, row, stationName, sensorName, start, end, maskType, false);
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

	public static void insertMask(TsDB tsdb, String filename, String[] row, String stationName, String sensorName, int start, int end, MaskType maskType, boolean commit) {
		//Logger.info(TimeUtil.oleMinutesToText(start, end));
		switch(maskType) {
		case INVALID: {
			TimeSeriesMask mask = tsdb.streamStorage.getTimeSeriesMask(stationName, sensorName);
			if(mask==null) {
				mask = new TimeSeriesMask();
			}
			mask.addInterval(Interval.of(start, end));
			tsdb.streamStorage.setTimeSeriesMask(stationName, sensorName, mask, commit);	
			break;
		}
		case SUSPECT: {
			TimeSeriesMask suspectMask = tsdb.streamStorage.getTimeSeriesSuspectMask(stationName, sensorName);
			if(suspectMask==null) {
				suspectMask = new TimeSeriesMask();
			}
			suspectMask.addInterval(Interval.of(start, end));
			tsdb.streamStorage.setTimeSeriesSuspectMask(stationName, sensorName, suspectMask, commit);		
			break;
		}
		default:
			throw new RuntimeException("unknown mask type: " + maskType);
		}

		if(!tsdb.streamStorage.existSensor(stationName, sensorName)) {
			Logger.warn("mask " + maskType + ": sensor not found, but inserted " + sensorName + "  at " + filename +"   in " + Arrays.toString(row));
		}
	}
}
