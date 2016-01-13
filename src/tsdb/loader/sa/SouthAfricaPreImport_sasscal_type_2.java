package tsdb.loader.sa;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TimeSeriesArchivWriter;
import tsdb.TsDBFactory;
import tsdb.util.Table;
import tsdb.util.Table.ColumnReaderFloat;
import tsdb.util.Table.ColumnReaderTimestamp;
import tsdb.util.TsEntry;
import tsdb.util.Util;
import tsdb.util.iterator.TimestampSeries;

/**
 * Reads SASSCAL_Type_2 files and writes content into ".tsa"-files
 * @author woellauer
 *
 */
public class SouthAfricaPreImport_sasscal_type_2 {
	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) {
		System.out.println("start...");

		try {
			
			String outFile = TsDBFactory.OUTPUT_PATH+"/"+"sa_tsa"+"/"+"south_africa_sasscal_type_2.tsa";
			Util.createDirectoriesOfFile(outFile);
			TimeSeriesArchivWriter tsaWriter = new TimeSeriesArchivWriter(outFile);
			tsaWriter.open();
			DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get("C:/timeseriesdatabase_source/sa/SASSCAL_Type_2"));
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

		String prefix = filepath.getName(filepath.getNameCount()-1).toString();
		
		if(!prefix.endsWith(".csv")) {
			throw new RuntimeException("no csv: "+prefix);
		}
		
		String stationID = prefix.substring(0,prefix.length()-4);

		String filename = filepath.toString();
		System.out.println("read file...");
		Table table = Table.readCSV(filename, ';');
		System.out.println("process...");

		ColumnReaderTimestamp cr_timestamp;

		if(table.nameMap.containsKey("MINUTE")) {
			cr_timestamp = table.createColumnReaderDateHourWrapMinuteTimestamp("DATUM","HOUR","MINUTE");
		} else {
			cr_timestamp = table.createColumnReaderDateFullHourTimestamp("DATUM","HOUR");
		}
		ColumnReaderFloat cr_P_RT_NRT = table.createColumnReaderFloat("RAIN"); //?

		String[] sensorNames = new String[]{				
				"P_RT_NRT"
		};		

		if(table.rows.length==0) {
			log.warn("empty");
			return;
		}

		ArrayList<TsEntry> resultList = new ArrayList<TsEntry>(table.rows.length);

		for(String[] row:table.rows) {
			long timestamp = cr_timestamp.get(row);
			//System.out.println(Arrays.toString(row)+"  "+TimeConverter.oleMinutesToText(timestamp));
			resultList.add(TsEntry.of(timestamp, cr_P_RT_NRT.get(row, false) ));
		}

		if(!resultList.isEmpty()) {
			resultList.sort((a,b)->Long.compare(a.timestamp, b.timestamp)); // sort rows with timestamps
			TimestampSeries tss = new TimestampSeries(stationID,sensorNames,resultList);		
			try {
				/*String outFile = TsDBFactory.OUTPUT_PATH+"/"+"south_africa_sasscal_type_2"+"/"+tss.name+".dat";
				Util.createDirectoriesOfFile(outFile);
				TimestampSeries.writeToBinaryFile(tss, outFile);*/
				/*String outFile = TsDBFactory.OUTPUT_PATH+"/"+"south_africa_sasscal_type_2"+"/"+tss.name+".tsa";
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
