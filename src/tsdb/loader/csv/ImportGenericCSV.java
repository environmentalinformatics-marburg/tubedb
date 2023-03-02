package tsdb.loader.csv;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.tinylog.Logger;

import ch.randelshofer.fastdoubleparser.FastDoubleParser;
import tsdb.Station;
import tsdb.TsDB;
import tsdb.component.SourceEntry;
import tsdb.component.labeledproperty.LabeledProperty;
import tsdb.component.labeledproperty.PropertyComputation;
import tsdb.util.AbstractTable;
import tsdb.util.AssumptionCheck;
import tsdb.util.DataRow;
import tsdb.util.StreamTable;
import tsdb.util.TimeUtil;
import tsdb.util.Util;

/**
 * import generic CSV data into database.
 * Beginning of filename is station name e.g. mystation_2014_new.csv  ==> station: mystation
 * first column name: datetime   format: ISO_8601  e.g. YYYY-MM-DDThh:mm
 * fllowing columns: database sensor names  
 * @author woellauer
 *
 */
public class ImportGenericCSV {
	

	private final TsDB tsdb;

	public ImportGenericCSV(TsDB tsdb) {
		//Logger.info("ImportGenericCSV");
		AssumptionCheck.throwNull(tsdb);
		this.tsdb = tsdb;
	}

	public void load(String rootPath) {
		load(Paths.get(rootPath));
	}

	public void load(Path rootPath) {
		loadFiles(rootPath);
		loadSubDirs(rootPath);
	}

	public void loadSubDirs(Path rootPath) {
		try(DirectoryStream<Path> rootStream = Files.newDirectoryStream(rootPath)) {
			for(Path sub:rootStream) {
				if(Files.isDirectory(sub)) {
					load(sub);
				}
			}
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	public void loadFiles(Path rootPath) {
		try(DirectoryStream<Path> rootStream = Files.newDirectoryStream(rootPath)) {
			for(Path sub:rootStream) {
				if(!Files.isDirectory(sub)) {
					loadFile(sub);
				}
			}
		} catch (Exception e) {
			Logger.error(e);
		}		
	}

	public void loadFile(Path filePath) {
		try {
			Logger.info("load file "+filePath);			
			StreamTable table = StreamTable.openCSV(filePath,',');

			int stationIndex = getStationIndex(table);
			if(stationIndex > 0) {
				throw new RuntimeException("wrong format");
			}
			if(stationIndex == 0) {
				int datetimeIndex = getDatetimeIndex(table);
				if(datetimeIndex != 1) {
					throw new RuntimeException("wrong format");
				}
				loadMultiStationFile(table, filePath);
			} else {
				int datetimeIndex = getDatetimeIndex(table);
				if(datetimeIndex != 0) {
					throw new RuntimeException("wrong format");
				}
				loadSingleStationFile(table, filePath);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error(e+"   "+filePath);
		}
	}

	protected String parseStationName(Path filePath) {
		String filename = filePath.getFileName().toString();

		int postFixIndex = filename.indexOf('_'); //filename with station name and postfix

		if(postFixIndex<0) {
			postFixIndex = filename.indexOf('.'); //filename with station name and without postfix
		}

		if(postFixIndex<1) {
			throw new RuntimeException("could not get station name from file name: "+filename);
		}

		return filename.substring(0, postFixIndex);		
	}

	protected int parseTimestamp(String timestampText) {
		//return TimeUtil.parseStartTimestamp(timestampText);
		return TimeUtil.parseNormalDatetime(timestampText);
	}

	protected int getDatetimeIndex(AbstractTable table) {
		return table.getColumnIndex("datetime");
	}

	protected int getStationIndex(AbstractTable table) {
		return table.getColumnIndex("plotID", false);
	}

	private void loadSingleStationFile(StreamTable table, Path filePath) throws IOException {
		String stationName = parseStationName(filePath);
		Logger.trace("station "+stationName);
		Station station = tsdb.getStation(stationName);
		if(station==null) {
			throw new RuntimeException("station not found: "+stationName+"   in "+filePath);
		}

		final int sensors = table.names.length-1;

		//ArrayList<DataRow> dataRows = new ArrayList<>(table.rows.length);
		ArrayList<DataRow> dataRows = new ArrayList<>();

		long numberParseErrorCount = 0;
		String numberParseErrorLast = null;
		long skipRowMissingTimestampCount = 0;
		int prevTimestamp = -1;
		long processingTime = System.currentTimeMillis();
		//for(String[] row:table.rows) {
		for(String[] row = table.readNext(); row != null; row = table.readNext()) {
			long currentTime = System.currentTimeMillis();
			if(processingTime + 1000 <= currentTime) {
				processingTime = currentTime;
				Logger.info(Arrays.toString(row));
			}
			if(row[0].isEmpty() || row[0].equals("NA")) {
				skipRowMissingTimestampCount++;
				continue;
			}
			int timestamp = parseTimestamp(row[0]);

			if(timestamp==prevTimestamp) {
				Logger.warn("skip duplicate timestamp "+row[0]+" "+filePath);
				continue;
			}
			
			float[] data = new float[sensors];
			for(int i=0;i<sensors;i++) {
				String text = row[i+1];
				if(text.isEmpty() || text.equals("NA")) {
					data[i] = Float.NaN;
				} else {
					try {
						//float value = Float.parseFloat(text);
						float value = (float) FastDoubleParser.parseDouble(text);
						if( Float.isFinite(value) && value!= -9999 ) {
							data[i] = value;
						} else {
							data[i] = Float.NaN;
						}
					} catch(Exception e) {
						data[i] = Float.NaN;
						numberParseErrorCount++;
						numberParseErrorLast = text;
						//Logger.warn("parse error: |" + text + "|" + "   at " + filePath);
					}
				}
			}

			DataRow dataRow = new DataRow(data, timestamp);
			//Logger.info(dataRow);
			dataRows.add(dataRow);

			prevTimestamp = timestamp;
		}
		if(skipRowMissingTimestampCount > 0) {
			Logger.warn(skipRowMissingTimestampCount + " skipped rows with missing timestamps "+filePath);			
		}
		if(numberParseErrorCount > 0) {
			Logger.warn(numberParseErrorCount + " number parse errors in " + filePath + "  last error : |" + numberParseErrorLast + "|");
		}

		//Logger.info("read done.");
		
		dataRows = sortAndRemoveDuplicates(dataRows);

		if(!dataRows.isEmpty()) {
			String[] sensorNames = Arrays.copyOfRange(table.names, 1, sensors + 1);
			long firstTimestamp = dataRows.get(0).timestamp;
			long lastTimestamp = dataRows.get(dataRows.size()-1).timestamp;

			List<LabeledProperty> computationList = station.labeledProperties.query("computation", (int)firstTimestamp, (int)lastTimestamp);
			if(computationList.size()>0) {
				Logger.trace("LabeledProperty computations");
				for(LabeledProperty prop:computationList) {					
					try {
						PropertyComputation computation = ((PropertyComputation)prop.content);
						if(Util.containsString(sensorNames, computation.target)) {
							Logger.trace("LabeledProperty computation "+computation.target);
							computation.calculate(dataRows, sensorNames, firstTimestamp, lastTimestamp);
						}
					} catch(Exception e) {
						e.printStackTrace();
						Logger.warn(e);
					}
				}
			}

			tsdb.streamStorage.insertDataRows(stationName, sensorNames, dataRows);
			tsdb.sourceCatalog.insert(SourceEntry.of(stationName, sensorNames, dataRows, filePath));
		}
	}

	private void loadMultiStationFile(StreamTable table, Path filePath) throws IOException {
		final int sensors = table.names.length - 2;

		HashMap<String, ArrayList<DataRow>> dataRowsMap = new HashMap<String, ArrayList<DataRow>>();
		//ArrayList<DataRow> dataRows = new ArrayList<>(table.rows.length);

		String cacheStationName = "";
		ArrayList<DataRow> cacheDataRows = null;

		//for(String[] row:table.rows) {
		for(String[] row = table.readNext(); row != null; row = table.readNext()) {
			if(row.length < 3) {
				Logger.warn("skip row with missing columns  " + filePath);
				continue;
			}
			String stationName = row[0];
			if(stationName.isEmpty() || stationName.equals("NA")) {
				Logger.warn("skip row with missing plotID " + stationName + " " + filePath);
				continue;
			}
			String timestampText = row[1];
			if(timestampText.isEmpty() || timestampText.equals("NA")) {
				Logger.warn("skip row with missing timestamp " + timestampText + " " + filePath);
				continue;
			}

			int timestamp = parseTimestamp(timestampText);

			float[] data = new float[sensors];
			int sensorsLen = Math.min(row.length - 2, sensors);
			for(int i=0; i < sensorsLen; i++) {
				String text = row[i + 2];
				if(text.isEmpty() || text.equals("NA")) {
					data[i] = Float.NaN;
				} else {
					try {
						//float value = Float.parseFloat(text);
						float value = (float) FastDoubleParser.parseDouble(text);
						if( Float.isFinite(value) && value!= -9999 ) {
							data[i] = value;
						} else {
							data[i] = Float.NaN;
						}
					} catch(Exception e) {
						data[i] = Float.NaN;
					}
				}
			}

			DataRow dataRow = new DataRow(data, timestamp);

			if(!cacheStationName.equals(stationName)) {
				cacheDataRows = dataRowsMap.get(stationName);
				if(cacheDataRows == null) {
					cacheDataRows = new ArrayList<>();
					dataRowsMap.put(stationName, cacheDataRows);
				}
			}
			cacheDataRows.add(dataRow);
		}

		for(Entry<String, ArrayList<DataRow>> entry:dataRowsMap.entrySet()) {
			String stationName = entry.getKey();
			Logger.trace("station "+stationName);
			Station station = tsdb.getStation(stationName);
			if(station==null) {
				Logger.warn("station not found: "+stationName+"   in "+filePath);
				continue;
			}
			ArrayList<DataRow> dataRows = entry.getValue();
			dataRows = sortAndRemoveDuplicates(dataRows);

			if(!dataRows.isEmpty()) {

				String[] sensorNames = Arrays.copyOfRange(table.names, 2, sensors + 2);
				long firstTimestamp = dataRows.get(0).timestamp;
				long lastTimestamp = dataRows.get(dataRows.size() - 1).timestamp;

				List<LabeledProperty> computationList = station.labeledProperties.query("computation", (int)firstTimestamp, (int)lastTimestamp);
				if(computationList.size() > 0) {
					Logger.trace("LabeledProperty computations");
					for(LabeledProperty prop:computationList) {					
						try {
							PropertyComputation computation = ((PropertyComputation)prop.content);
							if(Util.containsString(sensorNames, computation.target)) {
								Logger.trace("LabeledProperty computation "+computation.target);
								computation.calculate(dataRows, sensorNames, firstTimestamp, lastTimestamp);
							}
						} catch(Exception e) {
							e.printStackTrace();
							Logger.warn(e);
						}
					}
				}

				Logger.info("insert " + stationName + "  with rows: " + dataRows.size() + "  " + TimeUtil.oleMinutesToText(firstTimestamp) + "  " + TimeUtil.oleMinutesToText(lastTimestamp) + "   " + dataRows.get(0));
				tsdb.streamStorage.insertDataRows(stationName, sensorNames, dataRows);
				tsdb.sourceCatalog.insert(SourceEntry.of(stationName, sensorNames, dataRows, filePath));
			}
		}
	}

	private ArrayList<DataRow> sortAndRemoveDuplicates(ArrayList<DataRow> dataRows) {
		boolean duplicates = false;
		boolean unsorted = false;
		{
			long prevTimestamp = -1;
			for(DataRow dataRow : dataRows) {
				long timestamp = dataRow.timestamp;
				if(prevTimestamp == timestamp) {
					duplicates = true;
				} else if(prevTimestamp > timestamp) {
					unsorted = true;
					break;
				}
				prevTimestamp = timestamp;
			}			
		}
		if(unsorted) {
			dataRows.sort(DataRow.TIMESTAMP_COMPARATOR);
			duplicates = false;
			long prevTimestamp = -1;
			for(DataRow dataRow : dataRows) {
				long timestamp = dataRow.timestamp;
				if(prevTimestamp == timestamp) {
					duplicates = true;
					break;
				}
				prevTimestamp = timestamp;
			}	
		}
		if(duplicates) {
			int duplicatesCnt = 0;
			ArrayList<DataRow> cleanDataRows = new ArrayList<DataRow>(dataRows.size());
			long prevTimestamp = -1;
			for(DataRow dataRow : dataRows) {
				long timestamp = dataRow.timestamp;
				if(prevTimestamp == timestamp) {
					duplicatesCnt++;
				} else {
					prevTimestamp = timestamp;
					cleanDataRows.add(dataRow);
				}
			}
			Logger.warn("duplicates skipped " + duplicatesCnt);
			return cleanDataRows;
		} else {
			return dataRows;
		}
	}
}
