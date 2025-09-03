package tsdb.loader.treetalker;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.tinylog.Logger;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import ch.randelshofer.fastdoubleparser.JavaFloatParser;
import tsdb.TsDB;
import tsdb.component.SourceEntry;
import tsdb.util.DataEntry;
import tsdb.util.DataRow;
import tsdb.util.TimeUtil;

public class TreeTalkerCyberTable {

	private static final char DEFAULT_SEPARATOR = ',';
	private static final LocalDateTime UNIX_EPOCH = LocalDateTime.of(1970,1,1,0,0);
	private static final int UNIX_EPOCH_OLE_AUTOMATION_TIME_DIFFERENCE_MINUTES = (int) Duration.between(TimeUtil.OLE_AUTOMATION_TIME_START, UNIX_EPOCH).toMinutes();

	private final int offsetMinutes;
	private final char seperator;

	public TreeTalkerCyberTable(int offsetMinutes) {
		this(offsetMinutes, DEFAULT_SEPARATOR);
	}

	public TreeTalkerCyberTable(int offsetMinutes, char seperator) {
		this.offsetMinutes = offsetMinutes;
		this.seperator = seperator;
	}

	public void importFile(TsDB tsdb, File file) throws Exception {
		InputStreamReader in = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
		CSVParser csvParser = new CSVParserBuilder().withSeparator(seperator).build();
		try(CSVReader reader = new CSVReaderBuilder(in).withCSVParser(csvParser).build()) {		

			HashMap<String, ArrayList<DataRow>> tt3_dataMap = new HashMap<String, ArrayList<DataRow>>();
			HashMap<String, ArrayList<DataRow>> tt7_dataMap = new HashMap<String, ArrayList<DataRow>>();

			TreeSet<String> missingTypeCollector = new TreeSet<String>();

			for(String[] row = reader.readNextSilently(); row != null; row = reader.readNextSilently()) {
				//Logger.info(Arrays.toString(row));
				if(row.length == 0) {
					//Logger.info("skip empty line");
					continue;
				}
				if(row.length < 5) {
					//Logger.info("skip header/footer line with " + row.length + " columns");
					continue;
				}
				String receiveTimeID = row[0];				
				int timestamp = TimeUtil.parseTimestampDayFirstFormat(receiveTimeID);
				Logger.info(TimeUtil.oleMinutesToText(timestamp));

				String station_SN = row[1];
				if(station_SN.isBlank()) {
					//Logger.info("skip empty station SN");
					continue;
				}
				String tt_ID = "tt_" + station_SN;
				//Logger.info(tt_ID);

				String tt_Type = row[2];

				switch(tt_Type) {
				case "3": { // 3 = TTCyber
					//Logger.info(tt_Type);					
					if(row.length < 15) {
						row = fillMissing(row, 15);
					}
					float[] data = new float[]{
							parseFloat(row[4]),
							parseFloat(row[5]),
							parseFloat(row[6]),
							parseFloat(row[7]),
							parseFloat(row[8]),
							parseFloat(row[9]),
							parseFloat(row[10]),
							parseFloat(row[11]),
							parseFloat(row[12]),
							parseFloat(row[13]),
							parseFloat(row[14])							
					};
					ArrayList<DataRow> tt3_data = tt3_dataMap.get(tt_ID);
					if(tt3_data == null) {
						tt3_data = new ArrayList<DataRow>();
						tt3_dataMap.put(tt_ID, tt3_data);
					}
					tt3_data.add(new DataRow(data, timestamp));
					break;
				}
				case "7": { // 7 = TTCyber
					//Logger.info(tt_Type);					
					if(row.length < 15) {
						row = fillMissing(row, 15);
					}
					float[] data = new float[]{
							parseFloat(row[4]),
							parseFloat(row[5]),
							parseFloat(row[6]),
							parseFloat(row[7]),
							parseFloat(row[8]),
							parseFloat(row[9]),
							parseFloat(row[10]),
							parseFloat(row[11]),
							parseFloat(row[12]),
							parseFloat(row[13]),
							parseFloat(row[14])							
					};
					ArrayList<DataRow> tt7_data = tt7_dataMap.get(tt_ID);
					if(tt7_data == null) {
						tt7_data = new ArrayList<DataRow>();
						tt7_dataMap.put(tt_ID, tt7_data);
					}
					tt7_data.add(new DataRow(data, timestamp));
					break;
				}
				default: {
					//Logger.warn("unknown tt_Type " +tt_Type);
					missingTypeCollector.add(tt_Type);
				}
				}
			}

			if(!missingTypeCollector.isEmpty()) {
				String s = "unknown tt_Type:";
				for(String station:missingTypeCollector) {
					s += "\n" + station;
				}
				Logger.warn(s);
			}			

			String[] tt3_sensors = new String[] { // 3 = TTCyber
					"ttraw_g_x",
					"ttraw_g_x_std",
					"ttraw_g_y",
					"ttraw_g_y_std",
					"ttraw_g_z",
					"ttraw_g_z_std", 
					"ttraw_adc_Vbat", 
					"ttraw_air_temperature", 
					"ttraw_air_relative_humidity", 
					"ttraw_soil_temperature", 
					"ttraw_soil_moisture",
			};

			String[] tt7_sensors = new String[] { // 7 = TTCyber AS7341 10-channel spectrometer, approximate fwhm 30 nm
					"ttraw_gain",
					"ttraw_AS7341_F1_415",
					"ttraw_AS7341_F2_445",
					"ttraw_AS7341_F3_480",
					"ttraw_AS7341_F4_515",
					"ttraw_AS7341_F5_555", 
					"ttraw_AS7341_F6_590", 
					"ttraw_AS7341_F7_630", 
					"ttraw_AS7341_F8_680", 
					"ttraw_AS7341_clear", 
					"ttraw_AS7341_nir",
			};

			TreeSet<String> missingStationsCollector = new TreeSet<String>();

			try {
				insert(tsdb, tt3_dataMap, tt3_sensors, missingStationsCollector, file.toPath());
			}catch(Exception e) {
				Logger.warn("at tt3 " + e.getMessage());
			}
			try {
				insert(tsdb, tt7_dataMap, tt7_sensors, missingStationsCollector, file.toPath());
			}catch(Exception e) {
				Logger.warn("at tt7 " + e.getMessage());
			}

			if(!missingStationsCollector.isEmpty()) {
				String s = "missing stations";
				for(String station:missingStationsCollector) {
					s += "\n" + station;
				}
				Logger.warn(s);
			}
		}
	}

	int toTimestamp(String timestampText) {
		long timeStampSeconds = Long.parseLong(timestampText);
		int timestamp = UNIX_EPOCH_OLE_AUTOMATION_TIME_DIFFERENCE_MINUTES + (int)(timeStampSeconds / 60) + offsetMinutes; 
		return timestamp;
	}

	/*static LocalDateTime toDateTime(String timestampText) {
	long timeStampSeconds = Long.parseLong(timestampText);
	LocalDateTime datetime = UNIX_EPOCH.plusSeconds(timeStampSeconds);

	return datetime;
	}*/

	static float parseFloat(String s) {
		return s.isEmpty() ? Float.NaN : JavaFloatParser.parseFloat(s);
	}

	static String[] fillMissing(String[] src, int dstLen) {
		int srcLen = src.length;
		if(srcLen >= dstLen) {
			return src;
		}
		String[] dst = new String[dstLen];
		System.arraycopy(src, 0, dst, 0, srcLen);
		for(int i = srcLen; i < dstLen; i++) {
			dst[i] = "";
		}
		return dst;
	}

	void insert(TsDB tsdb, HashMap<String, ArrayList<DataRow>> ttx_dataMap, String[] ttx_sensors, TreeSet<String> missingStationsCollector, Path filename) {			
		for(Entry<String, ArrayList<DataRow>> e:ttx_dataMap.entrySet()) {
			String tt_ID = e.getKey();
			ArrayList<DataRow> dataRows = e.getValue();
			//Logger.info("insert in " + tt_ID + "  " + dataRows.size());
			//Logger.info(dataRows.toString());
			dataRows.sort(DataRow.TIMESTAMP_COMPARATOR);

			int sensors = ttx_sensors.length;
			ArrayList<DataEntry> dataEntryList = new ArrayList<DataEntry>(dataRows.size());
			for(int i=0;i<sensors;i++) {
				dataEntryList.clear();
				long prevTimestamp = -1;
				for(DataRow dataRow:dataRows) {
					float v = dataRow.data[i];
					if(prevTimestamp < dataRow.timestamp && Float.isFinite(v)) {
						dataEntryList.add(new DataEntry((int)dataRow.timestamp, v));
						prevTimestamp = dataRow.timestamp;
					} else {
						//Logger.info("skip " + v);
					}

				}
				if(tsdb.getStation(tt_ID) == null) {
					//Logger.warn("missing station " + tt_ID);
					missingStationsCollector.add(tt_ID);
				}
				DataEntry[] dataEntries = dataEntryList.toArray(new DataEntry[0]);
				tsdb.streamStorage.insertDataEntryArray(tt_ID, ttx_sensors[i], dataEntries);
			}
			tsdb.sourceCatalog.insert(SourceEntry.of(tt_ID, ttx_sensors, dataRows, filename.resolve(tt_ID)));
		}			
	}

}