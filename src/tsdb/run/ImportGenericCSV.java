package tsdb.run;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDB;
import tsdb.util.AssumptionCheck;
import tsdb.util.DataEntry;
import tsdb.util.Table;
import tsdb.util.TimeUtil;

/**
 * import generic CSV data into database.
 * Beginning of filename is station name e.g. mystation_2014_new.csv  ==> station: mystation
 * first column name: datetime   format: ISO_8601  e.g. YYYY-MM-DDThh:mm
 * fllowing columns: database sensor names  
 * @author woellauer
 *
 */
public class ImportGenericCSV {
	private static final Logger log = LogManager.getLogger();

	private final TsDB tsdb;

	public ImportGenericCSV(TsDB tsdb) {
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
			log.error(e);
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
			log.error(e);
		}		
	}

	public void loadFile(Path filePath) {
		try {
			log.info("load file "+filePath);			
			Table table = Table.readCSV(filePath,',');		
			int datetimeIndex = table.getColumnIndex("datetime");
			if(datetimeIndex!=0) {
				throw new RuntimeException("wrong format");
			}

			String filename = filePath.getFileName().toString();

			String stationName = filename.substring(0, filename.indexOf('_'));

			log.info("station "+stationName);

			final int sensors = table.names.length-1;

			@SuppressWarnings("unchecked")
			ArrayList<DataEntry>[] data = new ArrayList[sensors];
			for(int i=0;i<sensors;i++) {
				data[i] = new ArrayList<DataEntry>(table.rows.length);
			}

			int prevTimestamp = -1;
			for(String[] row:table.rows) {				
				//log.info(Arrays.toString(row));
				int timestamp = (int) TimeUtil.dateTimeToOleMinutes(LocalDateTime.parse(row[0]));

				if(timestamp==prevTimestamp) {
					log.warn("skip duplicate timestamp "+row[0]+" "+filePath);
					continue;
				}

				for(int i=0;i<sensors;i++) {
					if(row[i+1].isEmpty() || row[i+1].equals("NA")) {
						continue;
					}
					try {
						float value = Float.parseFloat(row[i+1]);
						if( Float.isFinite(value) && value!= -9999 ) {
							DataEntry entry = new DataEntry(timestamp, value);
							data[i].add(entry);
						}
					} catch(Exception e) {
						log.error(e+"   "+filePath);
					}
				}

				prevTimestamp = timestamp;
			}

			for(int i=0;i<sensors;i++) {
				String sensorName = table.names[i+1];
				DataEntry[] dataEntries = data[i].toArray(new DataEntry[0]);
				tsdb.streamStorage.insertDataEntryArray(stationName, sensorName, dataEntries);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e+"   "+filePath);
		}
	}
}
