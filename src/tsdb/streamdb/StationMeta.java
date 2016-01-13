package tsdb.streamdb;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import org.mapdb.Serializer;

/**
 * Metadat of station
 * immutable (Fields should not be changed.)
 * @author woellauer
 */
public class StationMeta {

	private static final String DB_NAME_SENSOR_MAP_PREFIX = "sensorMap/";
	private static final String DB_NAME_SENSOR_TIME_SERIES_MASK_MAP_PREFIX = "sensorTimeSeriesMaskMap/";
	
	public final String stationName;
	public final String db_name_sensor_map;
	public final String db_name_sensor_time_series_mask_map;
	
	public StationMeta(String stationName) {
		this(stationName, DB_NAME_SENSOR_MAP_PREFIX+stationName,DB_NAME_SENSOR_TIME_SERIES_MASK_MAP_PREFIX+stationName);
	}
	
	private StationMeta(String stationName, String db_name_sensor_map, String db_name_sensor_time_series_mask_map) {
		this.stationName = stationName;
		this.db_name_sensor_map = db_name_sensor_map;
		this.db_name_sensor_time_series_mask_map = db_name_sensor_time_series_mask_map;
	}
	
	private static class StationMetaSerializer implements Serializer<StationMeta>, Serializable {
		private static final long serialVersionUID = 2948674604488108143L;
		@Override
		public void serialize(DataOutput out, StationMeta value)
				throws IOException {
			out.writeUTF(value.stationName);
			out.writeUTF(value.db_name_sensor_map);
			out.writeUTF(value.db_name_sensor_time_series_mask_map);			
		}
		@Override
		public StationMeta deserialize(DataInput in, int available)
				throws IOException {
			String station = in.readUTF();
			String sensorM = in.readUTF();
			String sensorMaskM = in.readUTF();
			return new StationMeta(station,sensorM,sensorMaskM);
		}
		@Override
		public int fixedSize() {
			return -1;
		}		
	};
	
	public static final Serializer<StationMeta> SERIALIZER = new StationMetaSerializer();

}
