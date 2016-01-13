package tsdb.run;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDBFactory;
import tsdb.streamdb.StreamDB;
import tsdb.util.DataEntry;

/**
 * Loads database dump from file
 * @author woellauer
 *
 */
@Deprecated
public class DataBaseDumpRead {

	private static final Logger log = LogManager.getLogger();
	
	public static void printHelp() {
		System.out.println("*** database dumb reader ***");
		System.out.println("first parameter is path to dump file to load");
	}

	public static void main(String[] args) throws IOException {
		
		//Path pathToFile = Paths.get(TsDBFactory.OUTPUT_PATH+"/dump/"+"dump.tss");
		Path pathToFile = Paths.get(TsDBFactory.OUTPUT_PATH+"/dump/"+"dump_2015_03_19_10_50.tss");
		
		//Path pathToFile = null;
		/*if(args.length==0) {
			printHelp();
			return;
		} else if(args.length==1) {
			pathToFile = Paths.get(args[0]);
		} else {
			printHelp();
			return;
		}
		
		if(!pathToFile.toFile().exists()) {
			System.out.println("file not found: "+pathToFile);
			printHelp();
			return;
		}*/
		

		System.out.println("open streamDB...");


		StreamDB streamdb = new StreamDB(TsDBFactory.STORAGE_PATH+"/streamdb");
		try {
			
			
			
			BufferedInputStream fileInputStream = new BufferedInputStream(new FileInputStream(pathToFile.toFile()));
			DataInputStream dataInput = new DataInputStream(fileInputStream);

			int time_series_stream_header_marker = dataInput.readInt();
			if(DataBaseDumpWrite.TIME_SERIES_STREAM_HEADER_MARKER!=time_series_stream_header_marker) {
				dataInput.close();
				throw new RuntimeException("unknown file");
			}

			long timeStartImport = System.currentTimeMillis();

			while(dataInput.available()>0) {
				int time_series_stream_entry_marker = dataInput.readInt();
				if(DataBaseDumpWrite.TIME_SERIES_STREAM_ENTRY_MARKER!=time_series_stream_entry_marker) {
					dataInput.close();
					throw new RuntimeException("unknown entry marker");				
				}

				final String stationName = DataInputStream.readUTF(dataInput);
				final String sensorName = DataInputStream.readUTF(dataInput);
				final int valueCount = dataInput.readInt();

				System.out.println(stationName+" "+sensorName+" "+valueCount);

				DataEntry[] data = new DataEntry[valueCount];

				for(int i=0;i<valueCount;i++) {

					int timestamp = dataInput.readInt();
					float value = dataInput.readFloat();

					data[i] = new DataEntry(timestamp, value);
				}				
				streamdb.insertSensorData(stationName, sensorName, data);
			}

			long timeEndImport = System.currentTimeMillis();

			log.info((timeEndImport-timeStartImport)/1000+" s Import");

			dataInput.close();

		} finally {
			streamdb.close();
		}
	}
}
