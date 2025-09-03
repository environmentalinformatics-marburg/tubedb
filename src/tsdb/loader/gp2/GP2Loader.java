package tsdb.loader.gp2;

import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONObject;
import org.tinylog.Logger;

import ch.randelshofer.fastdoubleparser.JavaFloatParser;
import tsdb.Station;
import tsdb.TsDB;
import tsdb.component.SourceEntry;
import tsdb.util.AssumptionCheck;
import tsdb.util.DataEntry;
import tsdb.util.Table;
import tsdb.util.TimeUtil;
import tsdb.util.TsSchema;

public class GP2Loader {

	private final TsDB tsdb;

	public GP2Loader(TsDB tsdb, JSONObject jsonObject) {
		AssumptionCheck.throwNull(tsdb);
		this.tsdb = tsdb;

		/*if(jsonObject != null) {
			JSONObject jsonSensors = jsonObject.optJSONObject("sensors", null);
			if(jsonSensors != null) {

			} else {
				throw new RuntimeException("missing GP2 loader parameter sensors");
			}
		} else {
			throw new RuntimeException("missing GP2 loader parameters");
		}*/
	}

	public void loadDirectoryRecursive(Path path) {		
		Logger.info("GP2 import Directory "+path);
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

	private static final DateTimeFormatter DATE_TIME_FORMATER_DAY_FIRST = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

	public void loadFile(Path path) {
		Logger.info("GP2 import File "+ path);
		try {
			String filename = path.getFileName().toString();
			int stationNameBorderIndex = filename.length();
			int pointIndex = filename.indexOf('.');
			if(pointIndex > 0 && pointIndex < stationNameBorderIndex) {
				stationNameBorderIndex = pointIndex;
			}
			int underscoreIndex = filename.indexOf('_');
			if(underscoreIndex > 0 && underscoreIndex < stationNameBorderIndex) {
				stationNameBorderIndex = underscoreIndex;
			}
			String stationName = filename.substring(0, stationNameBorderIndex);
			Station station = tsdb.getStation(stationName); 
			if(station == null) {
				Logger.warn("missing station " + stationName + "  in  " + path);
				return;
			}

			Table table = Table.readCSV(path, '\t');
			if(table.names != null && table.names.length > 1 && (table.names[0].isEmpty() || table.names[0].equals("Label"))) {
				//Logger.info(Arrays.toString(table.names));
				int colLen = table.names.length;
				String[][] rows = table.rows;
				@SuppressWarnings("unchecked")
				ArrayList<DataEntry>[] ts = new ArrayList[colLen];
				for (int colIndex = 1; colIndex < colLen; colIndex++) {
					ts[colIndex] = new ArrayList<DataEntry>();
				}
				int rowInsertedCount = 0;
				int prevTimestamp = Integer.MIN_VALUE;
				int firstTimestamp = Integer.MAX_VALUE;
				int lastTimestamp = Integer.MIN_VALUE;
				boolean needsSorting = false;
				rowLoop: for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
					String[] row = rows[rowIndex];
					if(row.length != colLen) {
						Logger.info("skip row " + (rowIndex+1) + "  " + Arrays.toString(row));
						continue rowLoop;
					}
					String label = row[0];
					if(label.isEmpty() || label.equals("Units")) {
						//Logger.info("skip units in row " + (rowIndex+1) + "  " + Arrays.toString(row));
						continue rowLoop;
					}
					/*for(String col : row) {
						if(col.isBlank()) {
							Logger.info("skip empty column in row " + (i+1) + "  " + Arrays.toString(row));
							continue rowLoop;
						}
					}*/
					int timestamp;
					try {
						LocalDateTime dt = LocalDateTime.parse(label, DATE_TIME_FORMATER_DAY_FIRST);
						timestamp = (int) TimeUtil.dateTimeToOleMinutes(dt);
						//Logger.info(label + "  -->  " + timestamp);						
					} catch(Exception e) {
						Logger.warn(e.getMessage());
						continue rowLoop;
					}
					boolean rowInserted = false;
					colLoop: for (int colIndex = 1; colIndex < colLen; colIndex++) {
						String col = row[colIndex];
						if(col.isEmpty() || col.charAt(0) == '#' || (col.length() == 8 && col.charAt(2) == ':') || col.equals("open") || col.equals("closed")) {
							continue colLoop;
						}
						//Logger.info(col);
						try {	
							byte[] colBytes = col.getBytes(StandardCharsets.ISO_8859_1);
							replaceLoop: for (int i = 0; i < colBytes.length; i++) {
								if(colBytes[i] == ',') {
									colBytes[i] = '.';
									break replaceLoop;
								}
							}
							//float v = JavaFloatParser.parseFloat(col.replace(",", "."));
							float v = JavaFloatParser.parseFloat(colBytes);
							//Logger.info(v);
							ts[colIndex].add(new DataEntry(timestamp, v));
							rowInserted = true;
						} catch(Exception e) {
							Logger.warn("|" + col + "|   " + e.getMessage());
						}
					}
					if(rowInserted) {
						rowInsertedCount++;
						if(firstTimestamp > timestamp) {
							firstTimestamp = timestamp;
						}
						if(lastTimestamp < timestamp) {
							lastTimestamp = timestamp;
						}
						if(timestamp <= prevTimestamp) {
							Logger.warn(prevTimestamp + "  " + timestamp);
							needsSorting = true;
							Logger.warn("sort timestamps " + stationName + "  in  " + path + "   row " + rowIndex + "  " + Arrays.toString(row));
						}
						prevTimestamp = timestamp;
					}
				}
				ArrayList<String> sensorNames = new ArrayList<String>();
				ArrayList<String> translatedSensorNames = new ArrayList<String>();
				for (int colIndex = 1; colIndex < colLen; colIndex++) {
					ArrayList<DataEntry> vs = ts[colIndex];
					if(!vs.isEmpty()) {
						String sensorName = table.names[colIndex];
						String translatedSensorName = sensorName;
						String translation = station.translateInputSensorName(translatedSensorName, true);
						if(translation != null) {
							translatedSensorName = translation.equals("NaN") ? null : translation;
						}
						if(translatedSensorName != null) {
							//Logger.info(colIndex + "  " + stationName + " / " + sensorName + "   " + vs.size());
							DataEntry[] dataEntries = vs.toArray(new DataEntry[0]);
							if(needsSorting) {
								Logger.warn("sort timestamps " + stationName + " / " + sensorName + "  in  " + path);
								Arrays.sort(dataEntries);
								boolean hasDuplicates = false;
								dupCheck: for (int i = 1; i < dataEntries.length; i++) {
									if(dataEntries[i-1] == dataEntries[i]) {
										hasDuplicates = true;
										break dupCheck;
									}
								}
								if(hasDuplicates) {
									Logger.warn("remove duplicate timestamps " + stationName + " / " + sensorName + "  in  " + path);
									int currTimestamp = -1;
									ArrayList<DataEntry> de = new ArrayList<DataEntry>(dataEntries.length);
									for(DataEntry dataEntry : dataEntries) {
										if(dataEntry.timestamp == currTimestamp) {
											de.set(de.size() - 1, dataEntry);
										} else {
											de.add(dataEntry);
											currTimestamp = dataEntry.timestamp;
										}
									}
									dataEntries = de.toArray(new DataEntry[0]);
								}
							}
							tsdb.streamStorage.insertDataEntryArray(stationName, translatedSensorName, dataEntries);
						}
						sensorNames.add(sensorName);
						translatedSensorNames.add(translatedSensorName);
					}
				}
				if(!sensorNames.isEmpty()) {
					String[] sensorNamesArray = sensorNames.toArray(String[]::new);
					String[] translatedSensorNamesArray = translatedSensorNames.toArray(String[]::new);
					SourceEntry sourceEntry = new SourceEntry(path, stationName, firstTimestamp, lastTimestamp, rowInsertedCount, sensorNamesArray, translatedSensorNamesArray, TsSchema.NO_CONSTANT_TIMESTEP);
					tsdb.sourceCatalog.insert(sourceEntry);
				}
			} else {
				Logger.info("no GP2 text file " + path);
			}			
		} catch(Exception e) {
			e.printStackTrace();
			Logger.error(e);
		}
	}
}
