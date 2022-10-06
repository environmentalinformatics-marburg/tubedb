package tsdb.loader.bale;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;


import org.tinylog.Logger;

import ch.randelshofer.fastdoubleparser.FastDoubleParser;
import tsdb.Station;
import tsdb.TsDB;
import tsdb.component.SourceEntry;
import tsdb.util.DataEntry;
import tsdb.util.AbstractTable.ColumnReaderFloat;
import tsdb.util.AbstractTable.ColumnReaderSpaceTimestamp;

public class TOA5Loader {
	

	private final TsDB tsdb;

	public TOA5Loader(TsDB tsdb) {
		this.tsdb = tsdb;
	}

	public void loadDirectoryRecursive(Path path) {
		Logger.info("TOA5 import "+path);
		try(DirectoryStream<Path> rootStream = Files.newDirectoryStream(path)) {
			for(Path sub:rootStream) {
				if(!Files.isDirectory(sub)) {
					try {
						loadFile(sub);
					} catch (Exception e) {
						e.printStackTrace();
						Logger.error(e+"  in "+sub);
					}
				} else {
					loadDirectoryRecursive(sub);
				}

			}
		} catch (Exception e) {
			Logger.error(e);
		}		
	}

	public void loadFile(Path filename) throws Exception {
		Logger.info("TOA5 load " + filename);
		TOA5Table table = new TOA5Table(filename.toString());

		if(table.rows.length==0) {
			Logger.info("empty TOA5Table "+filename);
			return;
		}

		//Logger.info(Arrays.toString(table.names));
		ColumnReaderSpaceTimestamp timestampReader = table.getColumnReader("TIMESTAMP", ColumnReaderSpaceTimestamp::new);
		//ColumnReaderInt recordReader = table.createColumnReaderInt("RECORD");
		
		String stationNameText = table.recordingName;
		if(tsdb.stationExistsWithAlias(stationNameText)) {
			// nothing
		} else if(table.containsColumn("StationName")) {
			stationNameText = table.createColumnReader("StationName").get(table.rows[0]);
			if(tsdb.stationExistsWithAlias(stationNameText)) {
				// nothing
			} else {
				Logger.error("station not in database "+stationNameText+"  at  "+filename);
				return;
			}
		} else {
			Logger.error("station not in database "+stationNameText+"  at  "+filename);
			return;
		}
		
		Station station = tsdb.getStationWithAlias(stationNameText);


		ArrayList<String> valueNameList = new ArrayList<>();
		ArrayList<ColumnReaderFloat> valueReaderList = new ArrayList<>();
		ArrayList<String> traceHeaderList = new ArrayList<>();
		ArrayList<String> traceTranslatedList = new ArrayList<>();
		for(String name:table.names) {
			switch(name) {
			case "TIMESTAMP":
			case "RECORD":
			case "StationName":
			case "Latitude":
			case "Longitude":
			case "Altitude":
				//just exclude
				break;
			default:
				String translation = station.translateInputSensorName(name, true);
				if(translation == null || !translation.equals("NaN")) {
					valueNameList.add(name);
					valueReaderList.add(table.createColumnReaderFloat(name));
					traceTranslatedList.add(translation == null ? name : translation);
				} else {
					traceTranslatedList.add(null);
				}
				traceHeaderList.add(name);
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
							//float v = Float.parseFloat(text);
							float v = (float) FastDoubleParser.parseDouble(text);
							if(Float.isFinite(v)) {
								vList.add(new DataEntry(timestamps[rowIndex], v));
							}
						}
					}
				}
				DataEntry[] dataEntries = vList.toArray(new DataEntry[0]);
				String translation = station.translateInputSensorName(valueNames[i], true);
				String sensorName = translation == null ? valueNames[i] : translation;
				tsdb.streamStorage.insertDataEntryArray(station.stationID, sensorName, dataEntries);				
			} catch(Exception e) {
				//e.printStackTrace();
				Logger.error(e+" with name "+valueNames[i]+"  in "+filename);
			}
		}
		long firstTimestamp = -1;
		int rowIndex = 0;
		while( rowIndex < rowLen && firstTimestamp == -1) {
			firstTimestamp = timestamps[rowIndex++];
		}
		long lastTimestamp = -1;
		rowIndex = rowLen-1;
		while( rowIndex >= 0 && lastTimestamp == -1) {
			lastTimestamp = timestamps[rowIndex--];
		}
		int rowCount = rowLen;
		String[] headerNames = traceHeaderList.toArray(new String[0]);
		String[] sensorNames = traceTranslatedList.toArray(new String[0]);
		int timeStep = table.metaHeaderContains("TableMetHour") ? 60 : -1;
		SourceEntry sourceEntry = new SourceEntry(filename, station.stationID, firstTimestamp, lastTimestamp, rowCount, headerNames, sensorNames, timeStep);
		tsdb.sourceCatalog.insert(sourceEntry);
	}
}
