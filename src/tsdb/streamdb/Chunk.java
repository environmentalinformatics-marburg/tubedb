package tsdb.streamdb;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import org.mapdb.Serializer;
import org.xerial.snappy.Snappy;

import tsdb.util.AssumptionCheck;
import tsdb.util.DataEntry;

/**
 * Chunk of time series data
 * immutable (Entries of data should not be changed.)
 * @author woellauer
 */
public class Chunk {
	public final DataEntry[] data;
	
	public static Chunk of(ArrayList<DataEntry> list) {
		AssumptionCheck.throwNull(list);
		DataEntry[] array = list.toArray(new DataEntry[0]);
		AssumptionCheck.throwEmpty(array);
		return new Chunk(array);
	}
	
	public static Chunk of(DataEntry[] data) {
		AssumptionCheck.throwEmpty(data);
		return new Chunk(data);
	}

	Chunk(DataEntry[] data) {
		this.data = data;
	}

	private static class DeltaTimeDeltaValueIntQuantizedSerializer implements Serializer<Chunk>, Serializable {
		private static final long serialVersionUID = -5944450269165056735L;
		
		private static final float QUANTIZATION_FACTOR = 100f;
				
		@Override
		public void serialize(DataOutput out, Chunk chunk) throws IOException {
			DataEntry[] data = chunk.data;
			out.writeInt(data.length);
			int prev = 0;
			for(DataEntry entry:data) {
				out.writeInt(entry.timestamp-prev);
				prev = entry.timestamp;
			}
			int prevValue = 0;
			for(DataEntry entry:data) {
				if(Float.isNaN(entry.value)) {
					throw new RuntimeException("NaN Value in stream");
				}
				int curr = (int) (entry.value*QUANTIZATION_FACTOR);
				out.writeInt(curr-prevValue);
				prevValue = curr;
			}
		}
		@Override
		public Chunk deserialize(DataInput in, int available) throws IOException {
			int count = in.readInt();
			int[] timestamps = new int[count];
			int curr = 0;
			for(int i=0;i<timestamps.length;i++) {
				curr += in.readInt();
				timestamps[i] = curr;
			}
			DataEntry[] data = new DataEntry[count];
			int currValue = 0; 
			for(int i=0;i<data.length;i++) {
				currValue += in.readInt();
				data[i] = new DataEntry(timestamps[i],currValue/QUANTIZATION_FACTOR); // no mul for better rounding!!!
			}
			return new Chunk(data);
		}
		@Override
		public int fixedSize() {
			return -1;
		}		
	};
	
	public static final Serializer<Chunk> DELTA_TIME_DELTA_DELTA_VALUE_INT_QUANTIZED_SERIALIZER = new DeltaTimeDeltaValueIntQuantizedSerializer();
	
	
	private static class SnappyDeltaTimeDeltaValueIntQuantizedSerializer implements Serializer<Chunk>, Serializable {
		private static final long serialVersionUID = -5944450269165056735L;
		
		private static final float QUANTIZATION_FACTOR = 100f;
				
		@Override
		public void serialize(DataOutput out, Chunk chunk) throws IOException {
			DataEntry[] data = chunk.data;
			int[] deltaT = new int[data.length];
			int prev = 0;
			for(int i=0;i<data.length;i++) {
				deltaT[i] = data[i].timestamp-prev;
				prev = data[i].timestamp;
			}

			int[] deltaV = new int[data.length];
			int prevValue = 0;
			for(int i=0;i<data.length;i++) {
				if(Float.isNaN(data[i].value)) {
					throw new RuntimeException("NaN Value in stream");
				}
				int curr = (int) (data[i].value*QUANTIZATION_FACTOR);
				deltaV[i] = curr-prevValue;
				prevValue = curr;
			}
			byte[] compT = Snappy.compress(deltaT);
			int tsize = compT.length;
			out.writeInt(tsize);
			out.write(compT);
			
			byte[] compV = Snappy.compress(deltaV);
			int vsize = compV.length;
			out.writeInt(vsize);
			out.write(compV);
			
		}
		@Override
		public Chunk deserialize(DataInput in, int available) throws IOException {
			
			int tsize = in.readInt();
			byte[] tbuffer = new byte[tsize];
			in.readFully(tbuffer);
			int[] deltaT = Snappy.uncompressIntArray(tbuffer);
			int[] timestamps = new int[deltaT.length];
			
			int curr = 0;
			for(int i=0;i<timestamps.length;i++) {
				curr += deltaT[i];
				timestamps[i] = curr;
			}
			
			int vsize = in.readInt();
			byte[] vbuffer = new byte[vsize];
			in.readFully(vbuffer);
			int[] deltaV = Snappy.uncompressIntArray(vbuffer);
			
			DataEntry[] data = new DataEntry[deltaV.length];
			int currValue = 0; 
			for(int i=0;i<data.length;i++) {
				currValue += deltaV[i];
				data[i] = new DataEntry(timestamps[i],currValue/QUANTIZATION_FACTOR); // no mul for better rounding!!!
			}
			return new Chunk(data);
		}
		@Override
		public int fixedSize() {
			return -1;
		}		
	};
	
	public static final Serializer<Chunk> SNAPPY_DELTA_TIME_DELTA_DELTA_VALUE_INT_QUANTIZED_SERIALIZER = new SnappyDeltaTimeDeltaValueIntQuantizedSerializer();
	
}
