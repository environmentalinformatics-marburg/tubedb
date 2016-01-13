package tsdb.run;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDBFactory;
import tsdb.streamdb.ChunkMeta;
import tsdb.streamdb.SensorMeta;
import tsdb.streamdb.StreamDB;
import tsdb.streamdb.StreamIterator;
import tsdb.util.DataEntry;

/**
 * Writes database dumb to file
 * @author woellauer
 *
 */
@Deprecated
public class DataBaseDumpWrite {

	public final static int TIME_SERIES_STREAM_HEADER_MARKER = 0x54535348; //TSSH
	public final static int TIME_SERIES_STREAM_ENTRY_MARKER = 0x54535345;  //TSSE

	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) throws IOException {

		LocalDateTime ldt = LocalDateTime.now();
		
		DateTimeFormatter.ISO_DATE_TIME.format(ldt);
		String dateText = DateTimeFormatter.ofPattern("yyyy_MM_dd_HH_mm").format(ldt);
		
		Path pathToFile = Paths.get(TsDBFactory.OUTPUT_PATH+"/dump/"+"dump_"+dateText+".tss");
		Files.createDirectories(pathToFile.getParent());
		//Files.createFile(pathToFile);
		FileOutputStream fileOutputStream = new FileOutputStream(pathToFile.toFile());
		@SuppressWarnings("resource")
		DataOutputStream dataOutput = new DataOutputStream(new BufferedOutputStream(fileOutputStream));

		dataOutput.writeInt(TIME_SERIES_STREAM_HEADER_MARKER);		

		System.out.println("open streamDB...");

		StreamDB streamdb = new StreamDB(TsDBFactory.STORAGE_PATH+"/streamdb");

		try {

			System.out.println("collect data...");

			long timeStartExport = System.currentTimeMillis();

			long dbValues = 0;
			long dbChunkCount = 0;
			long dbSensorCount = 0;
			long dbStationCount = 0;
			for(String stationName:streamdb.getStationNames()) {
				//System.out.println("station: "+stationName);
				long stationValues = 0;
				long stationChunkCount = 0;
				long stationSensorCount = 0;			
				for(SensorMeta sensorMeta:streamdb.getSensorMap(stationName).values()) {
					int sensorValues = 0;
					int sensorValuesChunkSum = 0;
					int sensorChunkCount = 0;
					for(ChunkMeta chunkMeta:streamdb.getSensorChunkMetaMap(sensorMeta).values()) {
						sensorValuesChunkSum += chunkMeta.entryCount;
						sensorChunkCount++;

					}
					StreamIterator it = streamdb.getSensorIterator(sensorMeta, Integer.MIN_VALUE, Integer.MAX_VALUE);
					if(it.hasNext()){

						//DataOutputStream dataoutput = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(pathToFile.toFile())));
						//zipOutputStream.putNextEntry(new ZipEntry(stationName+"/"+sensorMeta.sensorName+".ts"));
						dataOutput.writeInt(TIME_SERIES_STREAM_ENTRY_MARKER);
						dataOutput.writeUTF(stationName);
						dataOutput.writeUTF(sensorMeta.sensorName);
						dataOutput.writeInt(sensorValuesChunkSum);

						while(it.hasNext()) {
							DataEntry e = it.next();
							sensorValues++;
							dataOutput.writeInt(e.timestamp);
							dataOutput.writeFloat(e.value);
						}
						if(sensorValues!=sensorValuesChunkSum) {
							throw new RuntimeException("internal error");
						}
					}




					//System.out.println(stationName+"  "+sensorMeta.sensorName + "  "+sensorValues+ " values in "+sensorChunkCount+" chunks");
					stationValues += sensorValues;
					stationChunkCount += sensorChunkCount;
					stationSensorCount++;
				}
				System.out.println(stationName+"  "+stationValues+ " values in "+stationSensorCount+" sensors and "+stationChunkCount+" chunks");
				dbValues += stationValues;
				dbChunkCount += stationChunkCount;
				dbSensorCount += stationSensorCount;
				dbStationCount++;
			}

			long timeEndExport = System.currentTimeMillis();
			log.info((timeEndExport-timeStartExport)/1000+" s Export");

			System.out.println("db  "+dbValues+ " values in "+dbStationCount+" stations and "+dbSensorCount+" sensors and "+dbChunkCount+" chunks");

		} catch (Exception e) {
			log.error(e);
		}

		try {
			streamdb.close();
		} catch(Exception e) {
			log.error(e);
		}		

		try {
			dataOutput.close();
		} catch (Exception e) {
			log.error(e);
		}

		try {
			fileOutputStream.close();
		} catch (Exception e) {
			log.error(e);
		}

	}

}