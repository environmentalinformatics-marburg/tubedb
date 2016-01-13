package tsdb.loader.sa;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TimeSeriesArchivWriter;
import tsdb.TsDBFactory;
import tsdb.util.Table;
import tsdb.util.TsEntry;
import tsdb.util.Table.ColumnReaderFloat;
import tsdb.util.Table.ColumnReaderString;
import tsdb.util.Table.ColumnReaderTimestamp;
import tsdb.util.iterator.TimestampSeries;
import tsdb.util.Util;

/**
 * Reads SASSCAL files and writes content into ".tsa"-files
 * @author woellauer
 *
 */
public class SouthAfricaPreImport_sasscal {
	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) {
		System.out.println("start...");

		try {
			String outFile = TsDBFactory.OUTPUT_PATH+"/"+"s_tsa"+"/"+"south_africa_sasscal.tsa";
			Util.createDirectoriesOfFile(outFile);
			TimeSeriesArchivWriter tsaWriter = new TimeSeriesArchivWriter(outFile);
			tsaWriter.open();
			DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get("C:/timeseriesdatabase_source/sa/SASSCAL"));
			for(Path filepath:ds) {
				log.info("read "+filepath);
				readOneFile(filepath,tsaWriter);
			}
			tsaWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		

		System.out.println("...end");
	}

	public static void readOneFile(Path filepath, TimeSeriesArchivWriter tsaWriter) {

		//String prefix = filepath.getName(filepath.getNameCount()-1).toString();

		String filename = filepath.toString();
		System.out.println("read file...");
		Table table = Table.readCSV(filename, ';');
		System.out.println("process...");

		ColumnReaderString cr_station = table.createColumnReader("Station Name");
		//IsoCode ignore
		//Station ID ignore

		ColumnReaderTimestamp cr_timestamp;

		if(table.nameMap.containsKey("Minute")) {
			cr_timestamp = table.createColumnReaderDateHourWrapMinuteTimestamp("Datum","Hour","Minute");
		} else {
			cr_timestamp = table.createColumnReaderDateFullHourTimestamp("Datum","Hour");
		}
		ColumnReaderFloat cr_P_RT_NRT = table.createColumnReaderFloat("Rain"); //?

		String[] sensorNames = new String[]{				
				"P_RT_NRT"
		};		

		if(table.rows.length==0) {
			log.warn("empty");
			return;
		}


		TreeMap<String, ArrayList<TsEntry>> stationMap = new TreeMap<String,ArrayList<TsEntry>>();

		String currentStationID = null;
		ArrayList<TsEntry> currentList = new ArrayList<TsEntry>(table.rows.length);

		long prevTimestamp = -1;
		for(String[] row:table.rows) {
			String stationID = cr_station.get(row);

			if(!stationID.equals(currentStationID)) {
				if(currentStationID!=null&&!currentList.isEmpty()) {
					if(!stationMap.containsKey(currentStationID)) {
						stationMap.put(currentStationID, currentList);
					} else {
						log.error("station already present: "+currentStationID);
					}
				}
				currentStationID = stationID;
				currentList = new ArrayList<TsEntry>(table.rows.length);
			}

			long timestamp = cr_timestamp.get(row);
			if(timestamp<=prevTimestamp) {
				if(timestamp==prevTimestamp) {
					log.warn("duplicate timestamp: ignore second: "+prevTimestamp+" "+timestamp+" in "+stationID+" of "+filepath);
					continue;
				} else {
					//throw new RuntimeException("timestamps not ordered"+prevTimestamp+" "+timestamp+" in "+stationID+" of "+filepath);
				}
			}

			//System.out.println(Arrays.toString(row)+"  "+TimeConverter.oleMinutesToText(timestamp));

			currentList.add(TsEntry.of(timestamp, cr_P_RT_NRT.get(row, false) ));
			prevTimestamp = timestamp;
		}

		if(currentStationID!=null&&!currentList.isEmpty()) {
			if(!stationMap.containsKey(currentStationID)) {
				stationMap.put(currentStationID, currentList);
			} else {
				log.error("station already present: "+currentStationID);
			}
		}

		System.out.println("write...");
		for(Entry<String, ArrayList<TsEntry>> entry:stationMap.entrySet()) {
			entry.getValue().sort((a,b)->Long.compare(a.timestamp, b.timestamp)); // sort rows with timestamps
			TimestampSeries tss = new TimestampSeries(entry.getKey(),sensorNames,entry.getValue());		
			try {
				/*String outFile = TsDBFactory.OUTPUT_PATH+"/"+"south_africa_sasscal"+"/"+tss.name+"_"+prefix+".dat";
				Util.createDirectoriesOfFile(outFile);
				TimestampSeries.writeToBinaryFile(tss, outFile);*/
				/*String outFile = TsDBFactory.OUTPUT_PATH+"/"+"south_africa_sasscal"+"/"+tss.name+".tsa";
				Util.createDirectoriesOfFile(outFile);
				TimeSeriesArchivWriter tsaWriter = new TimeSeriesArchivWriter(outFile);
				tsaWriter.open();
				tsaWriter.writeTimestampSeries(tss);
				tsaWriter.close();*/
				System.out.println(tss);
				tsaWriter.writeTimestampSeries(tss);
			} catch (Exception e) {
				e.printStackTrace();
			}	
		}
	}

}
