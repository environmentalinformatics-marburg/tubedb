package tsdb.loader.bale;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDB;
import tsdb.util.DataEntry;
import tsdb.util.TOA5Table;
import tsdb.util.Table.ColumnReaderFloat;
import tsdb.util.Table.ColumnReaderSpaceTimestamp;

public class TOA5Loader {
	private static final Logger log = LogManager.getLogger();

	private final TsDB tsdb;

	public TOA5Loader(TsDB tsdb) {
		this.tsdb = tsdb;
	}

	public void loadDirectoryRecursive(Path path) {
		try(DirectoryStream<Path> rootStream = Files.newDirectoryStream(path)) {
			for(Path sub:rootStream) {
				if(!Files.isDirectory(sub)) {
					try {
						loadFile(sub);
					} catch (Exception e) {
						e.printStackTrace();
						log.error(e+"  in "+sub);
					}
				} else {
					loadDirectoryRecursive(sub);
				}

			}
		} catch (Exception e) {
			log.error(e);
		}		
	}

	public void loadFile(Path filename) throws FileNotFoundException, IOException {
		TOA5Table table = new TOA5Table(filename.toString());

		if(table.rows.length==0) {
			log.info("empty TOA5Table "+filename);
			return;
		}

		ColumnReaderSpaceTimestamp timestampReader = table.getColumnReader("TIMESTAMP", ColumnReaderSpaceTimestamp::new);
		//ColumnReaderInt recordReader = table.createColumnReaderInt("RECORD");
		//ColumnReaderString stationNameReader = table.createColumnReader("StationName");

		//String stationName = stationNameReader.get(table.rows[0]);
		String stationName = table.recordingName;

		if(!tsdb.stationExists(stationName)) {
			log.error("station not in database "+stationName+"  at  "+filename);
			return;
		}

		ArrayList<String> valueNameList = new ArrayList<>();
		ArrayList<ColumnReaderFloat> valueReaderList = new ArrayList<>();
		for(String name:table.names) {
			switch(name) {
			case "TIMESTAMP":
			case "RECORD":
			case "StationName":
				//just exclude
				break;
			default:
				String translation = tsdb.getStation(stationName).translateInputSensorName(name, true);
				if(translation==null || !translation.equals("NaN")) {
					valueNameList.add(name);
					valueReaderList.add(table.createColumnReaderFloat(name));
				}
			}
		}
		String[] valueNames = valueNameList.toArray(new String[0]);
		int vLen = valueNames.length;

		String[][] rows = table.rows;
		int rowLen = rows.length;
		int[] timestamps = new int[rowLen];
		for (int rowIndex = 0; rowIndex < rowLen; rowIndex++) {
			timestamps[rowIndex] = (int) timestampReader.get(rows[rowIndex]);
		}
		int safeTimestamp = Integer.MAX_VALUE;
		for (int rowIndex = rowLen-1; rowIndex>=0; rowIndex--) { // remove error rows
			if(timestamps[rowIndex]<safeTimestamp) {
				safeTimestamp = timestamps[rowIndex];
			} else {
				timestamps[rowIndex] = -1; // mark row as removed
			}
		}
		ArrayList<DataEntry> vList = new ArrayList<DataEntry>();
		for (int i = 0; i < vLen; i++) {
			try {
				vList.clear();
				int colIndex = table.nameMap.get(valueNames[i]);
				for (int rowIndex = 0; rowIndex < rowLen; rowIndex++) {
					if(timestamps[rowIndex]!=-1) {
						String text = rows[rowIndex][colIndex];
						if(!text.equals("NAN")) {
							float v = Float.parseFloat(text);
							if(Float.isFinite(v)) {
								vList.add(new DataEntry(timestamps[rowIndex], v));
							}
						}
					}
				}
				DataEntry[] dataEntries = vList.toArray(new DataEntry[0]);
				String translation = tsdb.getStation(stationName).translateInputSensorName(valueNames[i], true);
				String sensorName = translation == null ? valueNames[i] : translation;
				tsdb.streamStorage.insertDataEntryArray(stationName, sensorName, dataEntries);
			} catch(Exception e) {
				e.printStackTrace();
				log.error(e+" with name "+valueNames[i]+"  in "+filename);
			}
		}
	}
}
