package tsdb.loader.sa;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;


import org.tinylog.Logger;

import tsdb.TsDBFactory;
import tsdb.util.Table;
import tsdb.util.TimeSeriesArchivWriter;
import tsdb.util.AbstractTable.ColumnReaderFloat;
import tsdb.util.AbstractTable.ColumnReaderString;
import tsdb.util.AbstractTable.ColumnReaderTimestamp;
import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;
import tsdb.util.Util;
import tsdb.util.iterator.TimestampSeries;

public class SouthAfricaPreImport_sasscal_2016_11_13 {
	

	private static long too_small_timestamp = TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(2000, 01, 01, 0, 0));

	public static void main(String[] args) {
		Logger.info("start...");

		try {
			String outFile = TsDBFactory.OUTPUT_PATH+"/"+"sa_tsa"+"/"+"south_africa_sasscal_2016_11_13.tsa";
			Util.createDirectoriesOfFile(outFile);
			TimeSeriesArchivWriter tsaWriter = new TimeSeriesArchivWriter(outFile);
			tsaWriter.open();
			DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get("C:/timeseriesdatabase_source/sa/SASSCAL_2016_11_13"));
			for(Path filepath:ds) {
				try {
					readOneFile(filepath,tsaWriter);
				} catch(Exception e) {
					e.printStackTrace();
					Logger.error("error reading "+filepath+"  "+e);
				}
			}			
			tsaWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		

		Logger.info("...end");
	}

	private static void readOneFile(Path filepath, TimeSeriesArchivWriter tsaWriter) throws IOException {

		TreeMap<String,String> sensorTranslation = new TreeMap<String, String>();
		sensorTranslation.put("WindSpeed", "WV");
		sensorTranslation.put("WindDirection", "WD");
		sensorTranslation.put("MaxWindSpeed", "WV_gust");
		sensorTranslation.put("TempAmbiet", "Ta_200");
		sensorTranslation.put("SoilTemp", "Ts_10");
		sensorTranslation.put("Humidity", "rH_200");
		sensorTranslation.put("SolarIrra", "Rn_300"); // W/m²
		sensorTranslation.put("SolarRadiation", "Rn_300"); // MJ/m² will be converted
		sensorTranslation.put("Rain", "P_RT_NRT");


		TreeSet<String> sensorIgnore = new TreeSet<String>();
		sensorIgnore.add("Station Name");
		sensorIgnore.add("Station ID");
		sensorIgnore.add("Datum");
		sensorIgnore.add("Hour");
		sensorIgnore.add("SolarIrra2"); //MJ/m²

		Logger.info("read "+filepath);
		Table table = Table.readCSV(filepath, ';');

		if(table.rows.length==0) {
			Logger.warn("empty");
			return;
		}

		final ColumnReaderString cr_station = table.createColumnReader("Station Name");

		final ColumnReaderTimestamp cr_timestamp;		
		if(table.nameMap.containsKey("Minute")) {
			cr_timestamp = table.createColumnReaderDateHourWrapMinuteTimestamp("Datum","Hour","Minute");
		} else {
			cr_timestamp = table.createColumnReaderDateFullHourTimestamp("Datum","Hour");
		}

		ArrayList<String> sensorNameList = new ArrayList<String>();
		ArrayList<ColumnReaderFloat> sensorNameReaderList = new ArrayList<ColumnReaderFloat>();

		for(String name:table.names) {
			if(!sensorIgnore.contains(name)) {
				if(sensorTranslation.containsKey(name)) {
					sensorNameList.add(sensorTranslation.get(name));
					ColumnReaderFloat cr_value = table.createColumnReaderFloat(name);
					if(name.equals("SolarRadiation")) {
						if(table.containsColumn("SolarIrra")) {
							continue; // ignore column
						} else {
							cr_value = cr_value.then(v->v*277.78f); // convert MJoule to watt							
						}
					}
					sensorNameReaderList.add(cr_value);
				} else {
					Logger.warn("ignored unknown column "+name);
				}
			}
		}

		int sensorLen = sensorNameReaderList.size();
		ColumnReaderFloat[] cr_sensors = sensorNameReaderList.toArray(new ColumnReaderFloat[sensorLen]);

		TreeMap<String, ArrayList<TsEntry>> stationMap = new TreeMap<String, ArrayList<TsEntry>>();
		String currentStationID = null;
		ArrayList<TsEntry> currentStationTimeSeries = null;

		int errorRowCount = 0;
		for(String[] row:table.rows) {
			try {
				String stationID = cr_station.get(row);
				if(!stationID.equals(currentStationID)) {
					currentStationTimeSeries = stationMap.get(stationID);
					currentStationID = stationID;
					if(currentStationTimeSeries==null) {
						currentStationTimeSeries = new ArrayList<TsEntry>();
						stationMap.put(stationID, currentStationTimeSeries);
					}
				}
				long timestamp = cr_timestamp.get(row);
				float[] values = new float[sensorLen];
				for(int i=0;i<sensorLen;i++) {
					values[i] = cr_sensors[i].get(row, false);
				}
				currentStationTimeSeries.add(TsEntry.of(timestamp, values));
			}catch(Exception e) {
				errorRowCount++;
				Logger.warn("row not read "+Arrays.toString(row)+"    "+e);
				if(errorRowCount>=20) {
					Logger.warn("abort reading data rows: too much errors "+filepath);
					break;
				}
			}
		}



		for(Entry<String, ArrayList<TsEntry>> entry:stationMap.entrySet()) {
			String stationID = entry.getKey();
			Logger.info("process "+stationID);
			ArrayList<TsEntry> tsList = entry.getValue();
			Iterator<TsEntry> it = tsList.iterator();
			if(it.hasNext()) {
				ArrayList<TsEntry> tsListResult = new ArrayList<TsEntry>(tsList.size());
				TsEntry currentTsEntry = it.next();
				//Logger.info(currentTsEntry);
				while(it.hasNext()) {
					TsEntry nextTsEntry = it.next();
					if(currentTsEntry.timestamp<too_small_timestamp) {
						Logger.warn("ignore too small timestamp "+TimeUtil.oleMinutesToText(currentTsEntry.timestamp)+"  "+stationID+"  "+filepath);
						currentTsEntry = nextTsEntry;
					} else if(currentTsEntry.timestamp < nextTsEntry.timestamp) { // timestamp ascending ==> add
						tsListResult.add(currentTsEntry);
						currentTsEntry = nextTsEntry;
					} else if(currentTsEntry.timestamp == nextTsEntry.timestamp) { // same timestamp ==> overwrite
						currentTsEntry = nextTsEntry;
					} else { // timestamp descending ==> warning and ignore entry
						Logger.warn("timestamps not in order "+currentTsEntry+" "+nextTsEntry+"  "+filepath);
					}
				}
				if(currentTsEntry.timestamp<too_small_timestamp) {
					Logger.warn("ignore too small timestamp "+TimeUtil.oleMinutesToText(currentTsEntry.timestamp)+"  "+stationID+"  "+filepath);
				} else {
					tsListResult.add(currentTsEntry);
				}
				TimestampSeries tss = new TimestampSeries(stationID, sensorNameList.toArray(new String[0]), tsListResult);
				tsaWriter.writeTimestampSeries(tss);
			}


		}

	}

}
