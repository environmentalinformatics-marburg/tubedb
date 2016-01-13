package tsdb.loader.sa;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.TreeMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import au.com.bytecode.opencsv.CSVWriter;
import tsdb.TsDBFactory;
import tsdb.util.Table;
import tsdb.util.Table.ColumnReaderString;

/**
 * creates station inventroy from data files of external Southafrica stations
 * @author woellauer
 *
 */
public class SouthAfricaCreateStationInventory {
	private static final Logger log = LogManager.getLogger();
	public static void main(String[] args) throws IOException {
		new SouthAfricaCreateStationInventory().run();
	}

	private static class Info {
		public String stationID;
		public String generalStation;
		public String lat;
		public String lon;

		public Info(String stationID, String generalStation, String lat, String lon) {
			this.stationID = stationID;
			this.generalStation = generalStation;
			this.lat = lat;
			this.lon = lon;
		}
	}

	private TreeMap<String,Info> stationMap = new TreeMap<String,Info>();

	private void run() throws IOException {

		System.out.println("read ACS");

		try {
			DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get("C:/timeseriesdatabase_source/sa/SAWS/ACS"));
			for(Path filepath:ds) {
				readOneFileACS(filepath);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("read ARS");

		try {
			DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get("C:/timeseriesdatabase_source/sa/SAWS/ARS"));
			for(Path filepath:ds) {
				readOneFileARS(filepath);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("read SASSCAL");

		try {
			DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get("C:/timeseriesdatabase_source/sa/SASSCAL"));
			for(Path filepath:ds) {
				readOneFileSASSCAL(filepath);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("read SASSCAL type 2");

		try {
			DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get("C:/timeseriesdatabase_source/sa/SASSCAL_type_2"));
			for(Path filepath:ds) {
				readOneFileSASSCAL_type_2(filepath);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("create inventory");

		CSVWriter csvwriter = new CSVWriter(new FileWriter(TsDBFactory.OUTPUT_PATH+"/"+"sa_station_inventory.csv"));

		csvwriter.writeNext(new String[]{"station","general","lat","lon"});
		for(Info info:stationMap.values()) {
			//System.out.println(info.stationID);
			csvwriter.writeNext(new String[]{info.stationID,info.generalStation,info.lat,info.lon});
		}

		csvwriter.close();
		System.out.println("end");
	}

	private void readOneFileACS(Path filepath) {
		String filename = filepath.toString();
		Table table = Table.readCSVFirstDataRow(filename, ',');
		String stationID = table.createColumnReader("title").get(table.rows[0]);

		String lat;
		String lon;
		try {
			lat = table.createColumnReader("lat").get(table.rows[0]);
			lon  = table.createColumnReader("lon").get(table.rows[0]);
		} catch(Exception e) {
			lat = "NA";
			lon = "NA";	
		}

		//System.out.println(stationID+"  "+lat+"  "+lon);
		if(stationMap.containsKey(stationID)) {
			log.warn("station already inserted: "+stationID);
		} else {
			stationMap.put(stationID, new Info(stationID, "SAWS", lat, lon));
		}
	}

	private void readOneFileARS(Path filepath) {
		String filename = filepath.toString();
		Table table = Table.readCSVFirstDataRow(filename, ',');
		String stationID = table.createColumnReader("StasName").get(table.rows[0]);

		String lat;
		String lon;
		try {
			lat = table.createColumnReader("Latitude").get(table.rows[0]);
			lon  = table.createColumnReader("Longitude").get(table.rows[0]);
		} catch(Exception e) {
			lat = "NA";
			lon = "NA";	
		}

		//System.out.println(stationID+"  "+lat+"  "+lon);
		if(stationMap.containsKey(stationID)) {
			log.warn("station already inserted: "+stationID);
		} else {
			stationMap.put(stationID, new Info(stationID, "SAWS", lat, lon));
		}
	}

	private void readOneFileSASSCAL(Path filepath) {
		String filename = filepath.toString();
		Table table = Table.readCSV(filename, ';');

		ColumnReaderString cr_stationID = table.createColumnReader("Station Name");

		String lat = "NA";
		String lon = "NA";

		for(String[] row:table.rows) {
			String stationID = cr_stationID.get(row);
			if(!stationMap.containsKey(stationID)) {
				stationMap.put(stationID, new Info(stationID, "SASSCAL", lat, lon));
			}			
		}		
	}

	private void readOneFileSASSCAL_type_2(Path filepath) {

		String prefix = filepath.getName(filepath.getNameCount()-1).toString();

		if(!prefix.endsWith(".csv")) {
			throw new RuntimeException("no csv: "+prefix);
		}

		String stationID = prefix.substring(0,prefix.length()-4);

		String lat = "NA";
		String lon = "NA";
		
		if(!stationMap.containsKey(stationID)) {
			stationMap.put(stationID, new Info(stationID, "SASSCAL", lat, lon));
		}		
	}

}
