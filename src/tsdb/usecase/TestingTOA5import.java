package tsdb.usecase;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.loader.bale.TOA5Loader;
import tsdb.loader.bale.TOA5Table;
import tsdb.util.DataEntry;
import tsdb.util.Table.ColumnReaderFloat;
import tsdb.util.Table.ColumnReaderInt;
import tsdb.util.Table.ColumnReaderSpaceTimestamp;
import tsdb.util.Table.ColumnReaderString;
import tsdb.util.Timer;
import tsdb.util.TsEntry;
import tsdb.util.iterator.TimestampSeries;

public class TestingTOA5import {
	//


	public static void main(String[] args) throws IOException {
		
		try(TsDB tsdb = TsDBFactory.createDefault()) {
		
		String path = "c:/timeseriesdatabase_source/bale";
		TOA5Loader loader = new TOA5Loader(tsdb);

		int REPEATS = 10;
		int LOOPS = 1000;
		for(int REPEAT=0; REPEAT<REPEATS; REPEAT++) {
			Timer.start("TOA5Loader");
			for(int LOOP=0; LOOP<LOOPS; LOOP++) {
				loader.loadDirectoryRecursive(Paths.get(path));
			}
			Timer.stopAndPrint("TOA5Loader");
		}
		
		}

	}
	public static void testing(String[] args) throws IOException {

		String filename = "c:/timeseriesdatabase_source/bale/BALE001_TableMetHour.dat";

		TOA5Table t = new TOA5Table(filename);
		//System.out.println(t);

		ColumnReaderSpaceTimestamp timestampReader = t.getColumnReader("TIMESTAMP", ColumnReaderSpaceTimestamp::new);
		ColumnReaderInt recordReader = t.createColumnReaderInt("RECORD");
		ColumnReaderString stationNameReader = t.createColumnReader("StationName");

		ArrayList<String> valueNameList = new ArrayList<>();
		ArrayList<ColumnReaderFloat> valueReaderList = new ArrayList<>();
		for(String name:t.names) {
			switch(name) {
			case "TIMESTAMP":
			case "RECORD":
			case "StationName":
				//just exclude
				break;
			default:
				valueNameList.add(name);
				valueReaderList.add(t.createColumnReaderFloat(name));				
			}
		}

		String[] valueNames = valueNameList.toArray(new String[0]);
		ColumnReaderFloat[] valueReaders = valueReaderList.toArray(new ColumnReaderFloat[0]);

		int vLen = valueReaders.length;

		int REPEATS = 10;
		int LOOPS = 1000;
		int DUBS = 1;

		for(int REPEAT=0; REPEAT<REPEATS; REPEAT++) {
			{
				Timer.start("ts");
				for(int LOOP=0; LOOP<LOOPS; LOOP++) {
					ArrayList<TsEntry> tsList = new ArrayList<TsEntry>();
					for (int dub = 0; dub < DUBS; dub++) {					
						for(String[] row:t.rows) {
							//int record = recordReader.get(row);
							long timestamp = timestampReader.get(row);
							//String stationName = stationNameReader.get(row);
							//System.out.println(record+" "+timestamp+" "+stationName);

							float[] vs = new float[vLen];
							for (int i = 0; i < vLen; i++) {
								vs[i] = valueReaders[i].get(row, true);
							}
							tsList.add(new TsEntry(timestamp, vs));			
						}
					}

					TimestampSeries timestampSeries = new TimestampSeries(stationNameReader.get(t.rows[0]), valueNames, tsList);
					//Logger.info(timestampSeries);
					DataEntry[][] dataEntryArrays = new DataEntry[vLen][];
					for (int i = 0; i < vLen; i++) {
						dataEntryArrays[i] = timestampSeries.toDataEntyArray(valueNames[i]);
					}
				}
				Timer.stopAndPrint("ts");
			}


			{
				Timer.start("DataEntry");
				for(int LOOP=0; LOOP<LOOPS; LOOP++) {
					@SuppressWarnings("unchecked")
					ArrayList<DataEntry>[] valueLists = new ArrayList[vLen];
					for (int i = 0; i < vLen; i++) {
						valueLists[i] = new ArrayList<DataEntry>();
					}
					for (int dub = 0; dub < DUBS; dub++) {	
						for(String[] row:t.rows) {
							//int record = recordReader.get(row);
							int timestamp = (int) timestampReader.get(row);
							//String stationName = stationNameReader.get(row);
							for (int i = 0; i < vLen; i++) {
								float v = valueReaders[i].get(row, true);
								if(Float.isFinite(v)) {
									valueLists[i].add(new DataEntry(timestamp, v));
								}
							}
						}
					}
					DataEntry[][] dataEntryArrays = new DataEntry[vLen][];
					for (int i = 0; i < vLen; i++) {
						dataEntryArrays[i] = valueLists[i].toArray(new DataEntry[0]);
					}
				}
				Timer.stopAndPrint("DataEntry");
			}

			{
				Timer.start("DataEntryPreTime");
				for(int LOOP=0; LOOP<LOOPS; LOOP++) {
					@SuppressWarnings("unchecked")
					ArrayList<DataEntry>[] valueLists = new ArrayList[vLen];
					for (int i = 0; i < vLen; i++) {
						valueLists[i] = new ArrayList<DataEntry>();
					}
					for (int dub = 0; dub < DUBS; dub++) {
						String[][] rows = t.rows;
						int rowLen = rows.length;
						int[] timestamps = new int[rowLen];
						for (int rowIndex = 0; rowIndex < rowLen; rowIndex++) {
							timestamps[rowIndex] = (int) timestampReader.get(rows[rowIndex]);
						}
						for (int rowIndex = 0; rowIndex < rowLen; rowIndex++) {
							String[] row = t.rows[rowIndex];	
							for (int i = 0; i < vLen; i++) {
								float v = valueReaders[i].get(row, true);
								if(Float.isFinite(v)) {
									valueLists[i].add(new DataEntry(timestamps[rowIndex], v));
								}
							}
						}
					}
					DataEntry[][] dataEntryArrays = new DataEntry[vLen][];
					for (int i = 0; i < vLen; i++) {
						dataEntryArrays[i] = valueLists[i].toArray(new DataEntry[0]);
					}
				}
				Timer.stopAndPrint("DataEntryPreTime");
			}

			{
				Timer.start("DataEntryPreTime2");
				for(int LOOP=0; LOOP<LOOPS; LOOP++) {
					DataEntry[][] dataEntryArrays = new DataEntry[vLen][];
					ArrayList<DataEntry> vList = new ArrayList<DataEntry>();
					for (int dub = 0; dub < DUBS; dub++) {
						String[][] rows = t.rows;
						int rowLen = rows.length;
						int[] timestamps = new int[rowLen];
						for (int rowIndex = 0; rowIndex < rowLen; rowIndex++) {
							timestamps[rowIndex] = (int) timestampReader.get(rows[rowIndex]);
						}
						for (int i = 0; i < vLen; i++) {
							vList.clear();
							for (int rowIndex = 0; rowIndex < rowLen; rowIndex++) {
								String[] row = t.rows[rowIndex];
								float v = valueReaders[i].get(row, true);
								if(Float.isFinite(v)) {
									vList.add(new DataEntry(timestamps[rowIndex], v));
								}
							}
							dataEntryArrays[i] = vList.toArray(new DataEntry[0]);
						}
					}
				}
				Timer.stopAndPrint("DataEntryPreTime2");
			}

		}


	}

}
