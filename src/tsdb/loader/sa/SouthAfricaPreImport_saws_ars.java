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
import tsdb.util.TsEntry;
import tsdb.util.Table.ColumnReaderFloat;
import tsdb.util.Table.ColumnReaderMonthNameTimestamp;
import tsdb.util.Table.ColumnReaderString;
import tsdb.util.iterator.TimestampSeries;
import tsdb.util.Util;

/**
 * Reads SAWS ARS files and writes content into ".tsa"-files
 * @author woellauer
 *
 */
public class SouthAfricaPreImport_saws_ars {

	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) {
		System.out.println("start...");
		
		try {
			String outFile = TsDBFactory.OUTPUT_PATH+"/"+"sa_tsa"+"/"+"south_africa_saws_ars.tsa";
			Util.createDirectoriesOfFile(outFile);
			TimeSeriesArchivWriter tsaWriter = new TimeSeriesArchivWriter(outFile);
			tsaWriter.open();
			//DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get("C:/timeseriesdatabase_source/sa/TESTING"));
			DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get("C:/timeseriesdatabase_source/sa/SAWS/ARS"));
			for(Path filepath:ds) {
				log.info("read "+filepath);
				readOneFile(filepath, tsaWriter);
			}
			tsaWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	public static void readOneFile(Path filepath, TimeSeriesArchivWriter tsaWriter) {
		//String filename = "C:/timeseriesdatabase_source/sa/SAWS/ACS/ALIWAL-NORTH PLAATKOP.csv";
		String filename = filepath.toString();
		System.out.println("read file...");
		Table table = Table.readCSV(filename, ',');
		System.out.println("process...");
		
		//ClimNo ignore
		ColumnReaderString cr_title = table.createColumnReader("StasName");
		//Latitude ignore
		//Longitude ignore
		ColumnReaderMonthNameTimestamp cr_timestamp = table.createColumnReaderMonthNameTimestamp("DateT");
		ColumnReaderFloat cr_P_RT_NRT = table.createColumnReaderFloat("Rain"); //?
		
		ArrayList<TsEntry> list = new ArrayList<TsEntry>(table.rows.length);
		
		String[] sensorNames = new String[]{				
				"P_RT_NRT"
		};		
		
		if(table.rows.length==0) {
			log.warn("empty");
			return;
		}
		
		String stationID = cr_title.get(table.rows[0]);	
		
		for(String[] row:table.rows) {			
			list.add(TsEntry.of(cr_timestamp.get(row),
					cr_P_RT_NRT.get(row, false)
					));
		}		
		
		TimestampSeries tss = new TimestampSeries(stationID,sensorNames,list);
		
		System.out.println("write...");
		try {
			/*String outFile = TsDBFactory.OUTPUT_PATH+"/"+"south_africa_saws_ars"+"/"+tss.name+".dat";
			Util.createDirectoriesOfFile(outFile);
			TimestampSeries.writeToBinaryFile(tss, outFile);*/
			/*String outFile = TsDBFactory.OUTPUT_PATH+"/"+"south_africa_saws_ars"+"/"+tss.name+".tsa";
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
