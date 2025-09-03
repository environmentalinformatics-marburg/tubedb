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

public class TreeTalkerTable {

	private static final char DEFAULT_SEPARATOR = ';';
	private static final LocalDateTime UNIX_EPOCH = LocalDateTime.of(1970,1,1,0,0);
	private static final int UNIX_EPOCH_OLE_AUTOMATION_TIME_DIFFERENCE_MINUTES = (int) Duration.between(TimeUtil.OLE_AUTOMATION_TIME_START, UNIX_EPOCH).toMinutes();

	private final int offsetMinutes;
	private final char seperator;

	public TreeTalkerTable(int offsetMinutes) {
		this(offsetMinutes, DEFAULT_SEPARATOR);
	}
	
	public TreeTalkerTable(int offsetMinutes, char seperator) {
		this.offsetMinutes = offsetMinutes;
		this.seperator = seperator;
	}

	public void importFile(TsDB tsdb, File file) throws Exception {
		InputStreamReader in = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8);
		CSVParser csvParser = new CSVParserBuilder().withSeparator(seperator).build();
		try(CSVReader reader = new CSVReaderBuilder(in).withCSVParser(csvParser).build()) {		

			HashMap<String, ArrayList<DataRow>> tt49_dataMap = new HashMap<String, ArrayList<DataRow>>();
			HashMap<String, ArrayList<DataRow>> tt4B_dataMap = new HashMap<String, ArrayList<DataRow>>();	
			HashMap<String, ArrayList<DataRow>> tt4C_dataMap = new HashMap<String, ArrayList<DataRow>>();	
			HashMap<String, ArrayList<DataRow>> tt4D_dataMap = new HashMap<String, ArrayList<DataRow>>();
			HashMap<String, ArrayList<DataRow>> tt53_dataMap = new HashMap<String, ArrayList<DataRow>>();
			HashMap<String, ArrayList<DataRow>> tt54_dataMap = new HashMap<String, ArrayList<DataRow>>();

			TreeSet<String> missingTypeCollector = new TreeSet<String>();

			for(String[] row = reader.readNextSilently(); row != null; row = reader.readNextSilently()) {
				//Logger.info(Arrays.toString(row));
				if(row.length == 0 || row[0].isEmpty()) {
					//Logger.info("skip empty line");
					continue;
				}
				if(row.length < 5) {
					//Logger.info("skip header/footer line with " + row.length + " columns");
					continue;
				}
				String timeID = row[0];
				String tt_ID = "tt_" + timeID.substring(timeID.indexOf(',') + 1); // If ',' is missing start from 0 is correct for raw TreeTalker data with out receive timestamp.
				//Logger.info(tt_ID);

				String tt_Type = row[2];

				switch(tt_Type) {
				case "49": {
					//Logger.info(tt_Type);					
					int timestamp = toTimestamp(row[3]);
					if(row.length < 18) {
						row = fillMissing(row, 18);
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
							parseFloat(row[14]),
							parseFloat(row[15]),
							parseFloat(row[16]),
							parseFloat(row[17])
					};
					ArrayList<DataRow> tt49_data = tt49_dataMap.get(tt_ID);
					if(tt49_data == null) {
						tt49_data = new ArrayList<DataRow>();
						tt49_dataMap.put(tt_ID, tt49_data);
					}
					tt49_data.add(new DataRow(data, timestamp));
					break;
				}
				case "4B": { // status
					//Logger.info(tt_Type);					
					int timestamp = toTimestamp(row[3]);
					if(row.length < 11) {
						row = fillMissing(row, 11);
					}
					float[] data = new float[]{
							parseFloat(row[4]),
							parseFloat(row[5]),
							parseFloat(row[6]),
							parseFloat(row[7]),
							parseFloat(row[8]),
							parseFloat(row[9]),
							parseFloat(row[10])
					};
					//parseFloat(row[11]) // Firmware Version is not a number							
					ArrayList<DataRow> tt4B_data = tt4B_dataMap.get(tt_ID);
					if(tt4B_data == null) {
						tt4B_data = new ArrayList<DataRow>();
						tt4B_dataMap.put(tt_ID, tt4B_data);
					}
					tt4B_data.add(new DataRow(data, timestamp));
					break;
				}
				case "4C": { // status
					//Logger.info(tt_Type);					
					int timestamp = toTimestamp(row[3]);
					if(row.length < 27) {
						row = fillMissing(row, 27);
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
							parseFloat(row[14]),
							parseFloat(row[15]),
							parseFloat(row[16]),
							parseFloat(row[17]),
							parseFloat(row[18]),
							parseFloat(row[19]),
							parseFloat(row[20]),
							parseFloat(row[21]),
							parseFloat(row[22]),
							parseFloat(row[23]),
							parseFloat(row[24]),
							parseFloat(row[25]),
							parseFloat(row[26]),
							//parseFloat(row[27]), // Not connected device 
							//parseFloat(row[28]), // Not connected device
							//parseFloat(row[29]), // Not connected device
					};
					ArrayList<DataRow> tt4C_data = tt4C_dataMap.get(tt_ID);
					if(tt4C_data == null) {
						tt4C_data = new ArrayList<DataRow>();
						tt4C_dataMap.put(tt_ID, tt4C_data);
					}
					tt4C_data.add(new DataRow(data, timestamp));
					break;
				}
				case "4D": {
					//Logger.info(tt_Type);					
					int timestamp = toTimestamp(row[3]);
					if(row.length < 21) {
						row = fillMissing(row, 21);
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
							parseFloat(row[14]),
							parseFloat(row[15]),
							parseFloat(row[16]),
							parseFloat(row[17]),
							parseFloat(row[18]),
							parseFloat(row[19]),
							parseFloat(row[20])
					};
					ArrayList<DataRow> tt4D_data = tt4D_dataMap.get(tt_ID);
					if(tt4D_data == null) {
						tt4D_data = new ArrayList<DataRow>();
						tt4D_dataMap.put(tt_ID, tt4D_data);
					}
					tt4D_data.add(new DataRow(data, timestamp));
					break;
				}
				case "53": { // 53 = three level ground moisture sensor log record identifier
					//Logger.info(tt_Type);					
					int timestamp = toTimestamp(row[3]);
					if(row.length < 21) {
						row = fillMissing(row, 21);
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
							parseFloat(row[14]),
							parseFloat(row[15]),
							parseFloat(row[16]),
							parseFloat(row[17]),
							parseFloat(row[18]),
							parseFloat(row[19]),
							parseFloat(row[20])
					};
					ArrayList<DataRow> tt53_data = tt53_dataMap.get(tt_ID);
					if(tt53_data == null) {
						tt53_data = new ArrayList<DataRow>();
						tt53_dataMap.put(tt_ID, tt53_data);
					}
					tt53_data.add(new DataRow(data, timestamp));
					break;
				}
				case "54": {
					//Logger.info(tt_Type);					
					int timestamp = toTimestamp(row[3]);
					if(row.length < 21) {
						row = fillMissing(row, 21);
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
							parseFloat(row[14]),
							parseFloat(row[15]),
							parseFloat(row[16]),
							parseFloat(row[17]),
							parseFloat(row[18]),
							parseFloat(row[19]),
							parseFloat(row[20])
					};
					ArrayList<DataRow> tt54_data = tt54_dataMap.get(tt_ID);
					if(tt54_data == null) {
						tt54_data = new ArrayList<DataRow>();
						tt54_dataMap.put(tt_ID, tt54_data);
					}
					tt54_data.add(new DataRow(data, timestamp));
					break;
				}
				default: {
					/*long timeStampSeconds = Long.parseLong(row[3]);
					LocalDateTime datetime = TIME_START.plusSeconds(timeStampSeconds);
					Logger.info(row[0] + "   " + timeStampText + "  " + datetime + row[3]);*/
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

			String[] tt49_sensors = new String[] {
					"ttraw_AS7263_610",
					"ttraw_AS7263_680",
					"ttraw_AS7263_730",
					"ttraw_AS7263_760",
					"ttraw_AS7263_810",
					"ttraw_AS7263_860",
					"ttraw_AS7262_450",
					"ttraw_AS7262_500",
					"ttraw_AS7262_550",
					"ttraw_AS7262_570",
					"ttraw_AS7262_600",
					"ttraw_AS7262_650",
					"ttraw_integration_time",
					"ttraw_gain"
			};

			String[] tt4B_sensors = new String[] {
					"ttraw_accumulated_records",
					"ttraw_records_to_send",
					"ttraw_MCC_telephone_operator",
					"ttraw_MNC_telephone_operator",
					"ttraw_GSM_registration",
					"ttraw_GSM_field level",
					"tt_Battery_level",
					// "Firmware Version", // Firmware Version is not a number	
			};			

			String[] tt4C_sensors = new String[] {
					"ttraw_TBL_LOCKED",
					"ttraw_first_sensor",
					"ttraw_TT01",
					"ttraw_TT02",
					"ttraw_TT03",
					"ttraw_TT04",
					"ttraw_TT05",
					"ttraw_TT06",
					"ttraw_TT07",
					"ttraw_TT08",
					"ttraw_TT09",
					"ttraw_TT10",
					"ttraw_TT11",
					"ttraw_TT12",
					"ttraw_TT13",
					"ttraw_TT14",
					"ttraw_TT15",
					"ttraw_TT16",
					"ttraw_TT17",
					"ttraw_TT18",
					"ttraw_TT19",
					"ttraw_TT20",
					"ttraw_TT21",
			};			

			String[] tt4D_sensors = new String[] {
					"ttraw_Tref",
					"ttraw_Theat",
					"ttraw_growth",
					"ttraw_adc_bandgap",
					"ttraw_bits",
					"tt_air_relative_humidity",
					"ttraw_air_temperature",
					"ttraw_g_z",
					"ttraw_g_z_std", 
					"ttraw_g_y", 
					"ttraw_g_y_std", 
					"ttraw_g_x", 
					"ttraw_g_x_std", 
					/*"ttraw_Tref_1",*/ "ttraw_gms_ntc_1", // ntc value of ground moisture sensor #1
					/*"ttraw_Theat_1",*/ "ttraw_not_used", // not used
					/*"ttraw_StWC",*/ "ttraw_gms_fq_1", // frequency of ground moisture sensor #1
					"ttraw_adc_Vbat"
			};

			String[] tt53_sensors = new String[] {
					"ttraw_adc_bandgap",
					"ttraw_bits",
					"tt_air_relative_humidity",
					"ttraw_air_temperature",
					"ttraw_g_z",
					"ttraw_g_z_std", 
					"ttraw_g_y", 
					"ttraw_g_y_std", 
					"ttraw_g_x", 
					"ttraw_g_x_std", 
					"ttraw_gms_ntc_1", // ntc value of ground moisture sensor #1
					"ttraw_gms_fq_1", // frequency of ground moisture sensor #1
					"ttraw_gms_ntc_2", // ntc value of ground moisture sensor #2
					"ttraw_gms_fq_2", // frequency of ground moisture sensor #2
					"ttraw_gms_ntc_3", // ntc value of ground moisture sensor #3
					"ttraw_gms_fq_3", // frequency of ground moisture sensor #3
					"ttraw_adc_Vbat",
			};

			String[] tt54_sensors = new String[] {
					"ttraw_adc_bandgap",
					"ttraw_bits",
					"tt_air_relative_humidity",
					"ttraw_air_temperature",
					"ttraw_g_z",
					"ttraw_g_z_std", 
					"ttraw_g_y", 
					"ttraw_g_y_std", 
					"ttraw_g_x", 
					"ttraw_g_x_std", 
					"ttraw_gms_ntc_1", // ntc value of ground moisture sensor #1
					"ttraw_gms_fq_1", // frequency of ground moisture sensor #1
					"ttraw_gms_ntc_2", // ntc value of ground moisture sensor #2
					"ttraw_gms_fq_2", // frequency of ground moisture sensor #2
					"ttraw_gms_ntc_3", // ntc value of ground moisture sensor #3
					"ttraw_gms_fq_3", // frequency of ground moisture sensor #3
					"ttraw_adc_Vbat",
			};

			TreeSet<String> missingStationsCollector = new TreeSet<String>();

			try {
				insert(tsdb, tt49_dataMap, tt49_sensors, missingStationsCollector, file.toPath());
			}catch(Exception e) {
				Logger.warn("at tt49 " + e.getMessage());
			}
			try {
				insert(tsdb, tt4B_dataMap, tt4B_sensors, missingStationsCollector, file.toPath());
			}catch(Exception e) {
				Logger.warn("at tt4B " + e.getMessage());
			}
			try {
				insert(tsdb, tt4C_dataMap, tt4C_sensors, missingStationsCollector, file.toPath());
			}catch(Exception e) {
				e.printStackTrace();
				Logger.warn("at tt4C " + e.getMessage());
			}
			try {
				insert(tsdb, tt4D_dataMap, tt4D_sensors, missingStationsCollector, file.toPath());
			}catch(Exception e) {
				Logger.warn("at tt4D " + e.getMessage());
			}
			try {
				insert(tsdb, tt53_dataMap, tt53_sensors, missingStationsCollector, file.toPath());
			}catch(Exception e) {
				Logger.warn("at tt53 " + e.getMessage());
			}
			try {				
				insert(tsdb, tt54_dataMap, tt54_sensors, missingStationsCollector, file.toPath());
			}catch(Exception e) {
				Logger.warn("at tt54 " + e.getMessage());
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