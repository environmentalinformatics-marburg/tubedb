package tsdb.loader.burgwald;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Set;

import org.tinylog.Logger;

import ch.randelshofer.fastdoubleparser.JavaFloatParser;
import tsdb.TsDB;
import tsdb.component.SourceEntry;
import tsdb.util.DataEntry;
import tsdb.util.AbstractTable.ColumnReaderDayFirstAmPmTimestamp;
import tsdb.util.AbstractTable.ColumnReaderFloat;

public class HoboLoader {
	

	private final TsDB tsdb;

	public HoboLoader(TsDB tsdb) {
		this.tsdb = tsdb;
	}

	public void loadDirectoryRecursive(Path path) {
		//Logger.info("import "+path);
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
		//Logger.info("load Hobo File: "+filename);
		HoboTable table = new HoboTable(filename.toString());

		if(table.rows.length==0) {
			Logger.info("empty HoboTable "+filename);
			return;
		}

		ColumnReaderDayFirstAmPmTimestamp timestampReader = table.getColumnReader("Datum Zeit", ColumnReaderDayFirstAmPmTimestamp::new);
		//ColumnReaderInt recordReader = table.createColumnReaderInt("RECORD");
		//ColumnReaderString stationNameReader = table.createColumnReader("StationName");

		//String stationName = stationNameReader.get(table.rows[0]);
		String stationName = table.plotID;

		if(!tsdb.stationExists(stationName)) {
			Logger.error("station not in database "+stationName+"  at  "+filename);
			return;
		}

		ArrayList<String> valueNameList = new ArrayList<>();
		ArrayList<ColumnReaderFloat> valueReaderList = new ArrayList<>();
		ArrayList<String> traceHeaderList = new ArrayList<>();
		ArrayList<String> traceTranslatedList = new ArrayList<>();
		for(String name:table.names) {
			switch(name) {
			case "Datum Zeit":
			case "Anz.":
			case "Koppler verbunden":
			case "Host verbunden":
			case "Koppler abgetrennt":
			case "Angehalten":
			case "Dateiende":
			case "Batterie defekt":
			case "Batterie gut":
				//just exclude
				break;
			default:
				String translation = tsdb.getStation(stationName).translateInputSensorName(name, true);
				if(translation==null || !translation.equals("NaN")) {
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
						if(!text.isEmpty() && !text.equals("Protokolliert")) {
							//float v = Float.parseFloat(text);
							float v = JavaFloatParser.parseFloat(text);
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
		int timeStep = -1;
		//Logger.info("insert: "+Arrays.toString(sensorNames));
		SourceEntry sourceEntry = new SourceEntry(filename, stationName, firstTimestamp, lastTimestamp, rowCount, headerNames, sensorNames, timeStep);
		tsdb.sourceCatalog.insert(sourceEntry);
	}
	
	public void collectPlotsRecursive(Path path, Set<String> plotIDs) {
		//Logger.info("import "+path);
				try(DirectoryStream<Path> rootStream = Files.newDirectoryStream(path)) {
					for(Path sub:rootStream) {
						if(!Files.isDirectory(sub)) {
							try {
								HoboTable table = new HoboTable(sub.toString());
								plotIDs.add(table.plotID);
							} catch (Exception e) {
								e.printStackTrace();
								Logger.error(e+"  in "+sub);
							}
						} else {
							collectPlotsRecursive(sub, plotIDs);
						}

					}
				} catch (Exception e) {
					Logger.error(e);
				}				
	}
}
