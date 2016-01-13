package tsdb.usecase;

import tsdb.TsDBFactory;
import tsdb.streamdb.ChunkMeta;
import tsdb.streamdb.SensorMeta;
import tsdb.streamdb.StreamDB;

public class CountTsDBValues {

	public static void main(String[] args) {
		
		System.out.println("open streamDB...");
		
		StreamDB streamdb = new StreamDB(TsDBFactory.STORAGE_PATH+"/streamdb");
		
		System.out.println("collect data...");
		
		long dbValues = 0;
		long dbChunkCount = 0;
		long dbSensorCount = 0;
		long dbStationCount = 0;
		long dbClassicRows = 0; 
		for(String stationName:streamdb.getStationNames()) {
			long stationValues = 0;
			long stationChunkCount = 0;
			long stationSensorCount = 0;
			long stationClassicRows = 0; 
			for(SensorMeta sensorMeta:streamdb.getSensorMap(stationName).values()) {
				long sensorValues = 0;
				long sensorChunkCount = 0;
				for(ChunkMeta chunkMeta:streamdb.getSensorChunkMetaMap(sensorMeta).values()) {
					sensorValues += chunkMeta.entryCount;
					sensorChunkCount++;
				}
				System.out.println(stationName+"  "+sensorMeta.sensorName + "  "+sensorValues+ " values in "+sensorChunkCount+" chunks");
				stationValues += sensorValues;
				stationChunkCount += sensorChunkCount;
				stationSensorCount++;
				if(stationClassicRows<sensorValues) {
					stationClassicRows = sensorValues;
				}
			}
			System.out.println(stationName+"  "+stationValues+ " values in "+stationSensorCount+" sensors and "+stationChunkCount+" chunks and  "+stationClassicRows+" 'table'-rows");
			dbValues += stationValues;
			dbChunkCount += stationChunkCount;
			dbSensorCount += stationSensorCount;
			dbStationCount++;
			dbClassicRows += stationClassicRows;
		}
		System.out.println();
		System.out.println("db  "+dbValues+ " values in "+dbStationCount+" stations and "+dbSensorCount+" sensors and "+dbChunkCount+" chunks and  "+dbClassicRows+" 'table'-rows");
		
		
		/*SensorMeta sensorMeta = streamdb.getSensorMeta("51021020159", "Ta_200");
		//streamdb.getSensorMeta(stationMeta, sensorName, createIfNotExists)
		BTreeMap<Integer, ChunkMeta> chunkMetaMap = streamdb.getSensorChunkMetaMap(sensorMeta);
		for(ChunkMeta chunkMeta:chunkMetaMap.values()) {
			System.out.println(chunkMeta);
		}
		
		
		StreamIterator it = streamdb.getSensorIterator(sensorMeta, Integer.MIN_VALUE, Integer.MAX_VALUE);
		
		while(it.hasNext()) {
			DataEntry e = it.next();
			System.out.println(e);
		}*/
		
		
		
		streamdb.close();
	}

}
