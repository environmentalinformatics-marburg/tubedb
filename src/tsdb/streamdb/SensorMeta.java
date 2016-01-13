package tsdb.streamdb;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import org.mapdb.Serializer;

/**
 * Metadata of sensor
 * immutable
 * @author woellauer
 */
public class SensorMeta {
	private static final String DB_NAME_SENSOR_CHUNK_MAP_PREFIX = "sensorChunkMap/";
	private static final String DB_NAME_SENSOR_CHUNKMETA_MAP_PREFIX = "sensorChunkMetaMap/";
	
	public final String db_name_sensor_chunk_map;
	public final String db_name_sensor_chunkmeta_map;
	
	public final String stationName;
	public final String sensorName;

	public SensorMeta(String stationName, String sensorName) {
		this.stationName = stationName;
		this.sensorName = sensorName;
		this.db_name_sensor_chunk_map = DB_NAME_SENSOR_CHUNK_MAP_PREFIX+stationName+"/"+sensorName;
		this.db_name_sensor_chunkmeta_map = DB_NAME_SENSOR_CHUNKMETA_MAP_PREFIX+stationName+"/"+sensorName;
	}
	
	private SensorMeta(String stationName, String sensorName, String db_name_sensor_chunk_map, String db_name_sensor_chunkmeta_map) {
		this.stationName = stationName;
		this.sensorName = sensorName;
		this.db_name_sensor_chunk_map = db_name_sensor_chunk_map;
		this.db_name_sensor_chunkmeta_map = db_name_sensor_chunkmeta_map;
	}
	
	/*public int[] getInterval() {
		if(list.isEmpty()) {
			return null;
		}
		int minTimestamp = Integer.MAX_VALUE;
		int maxTimestamp = Integer.MIN_VALUE;
		for(ChunkMetaEntry entry:list) {
			if(entry.firstTimestamp<minTimestamp) {
				minTimestamp = entry.firstTimestamp;
			}
			if(maxTimestamp<entry.lastTimestamp) {
				maxTimestamp = entry.lastTimestamp;
			}
		}
		if(minTimestamp == Integer.MAX_VALUE || maxTimestamp == Integer.MIN_VALUE) {
			return null;
		}
		return new int[]{minTimestamp,maxTimestamp};
	}*/
	
	private static class SensorMetaSerializer implements Serializer<SensorMeta>, Serializable {
		private static final long serialVersionUID = -4064482638307132258L;
		@Override
		public void serialize(DataOutput out, SensorMeta value)
				throws IOException {
			out.writeUTF(value.stationName);
			out.writeUTF(value.sensorName);
			out.writeUTF(value.db_name_sensor_chunk_map);
			out.writeUTF(value.db_name_sensor_chunkmeta_map);
		}
		@Override
		public SensorMeta deserialize(DataInput in, int available) throws IOException {			
			String stationName = in.readUTF();
			String sensorName = in.readUTF();
			String db_name_sensor_chunk_map = in.readUTF();
			String db_name_sensor_chunkmeta_map = in.readUTF();
			return new SensorMeta(stationName,sensorName,db_name_sensor_chunk_map,db_name_sensor_chunkmeta_map);
		}
		@Override
		public int fixedSize() {
			return -1;
		}		
	};
	
	public static final Serializer<SensorMeta> SERIALIZER = new SensorMetaSerializer();

}
