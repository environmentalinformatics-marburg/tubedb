package tsdb.streamdb;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;

import org.mapdb.Serializer;

import me.lemire.integercompression.FastPFOR128;
import me.lemire.integercompression.IntCompressor;
import me.lemire.integercompression.SkippableComposition;
import me.lemire.integercompression.VariableByte;
import tsdb.util.AssumptionCheck;
import tsdb.util.DataEntry;
import tsdb.util.TimeUtil;

/**
 * Serializer for Chunk.
 * serializable
 * thread-safe
 * @author woellauer
 *
 */
public final class ChunkSerializer implements Serializer<Chunk>, Serializable {
	//private static final Logger log = LogManager.getLogger();
	private static final long serialVersionUID = 0;
	
	public static final Serializer<Chunk> DEFAULT = new ChunkSerializer();
	
	private static final float QUANTIZATION_FACTOR = 100f;
	
	private static ThreadLocal<IntCompressor> threadLocal_ic = new ThreadLocal<IntCompressor>() {
		@Override
		protected IntCompressor initialValue() {
			SkippableComposition codec = new SkippableComposition(new FastPFOR128(), new VariableByte());
			IntCompressor ic = new IntCompressor(codec);
			return ic;
		}		
	};
	
	private ChunkSerializer(){}

	@Override
	public void serialize(DataOutput out, Chunk chunk) throws IOException {
		
		IntCompressor ic = threadLocal_ic.get();
		
		final DataEntry[] data = chunk.data;
		final int SIZE = data.length;
		
		int[] deltaTimestamps = new int[SIZE];
		int prevTimestamp = 0;
		int prevDeltaTimestamp = 0;
		int[] deltas = new int[SIZE];
		int prev = 0;
		int prevDelta = 0;
		for (int i = 0; i < SIZE; i++) {		
			DataEntry entry = data[i];
			int timestamp = entry.timestamp;
			float value = entry.value;
			if(!Float.isFinite(value)) {
				throw new RuntimeException("Not finite Value in stream "+value+"  "+Float.floatToRawIntBits(value)+" "+TimeUtil.oleMinutesToText(timestamp));
			}
			int curr = (int) (value*QUANTIZATION_FACTOR);
			int deltaTimestamp = timestamp-prevTimestamp;
			deltaTimestamps[i] = encodeZigZag(deltaTimestamp-prevDeltaTimestamp);
			prevTimestamp = timestamp;
			prevDeltaTimestamp = deltaTimestamp;
			int delta = curr-prev;
			deltas[i] = encodeZigZag(delta-prevDelta);
			prev = curr;
			prevDelta = delta;
		}

		int[] resultTimestamp = ic.compress(deltaTimestamps);
		int[] result = ic.compress(deltas);

		//writeByteArray(out, Snappy.compress(resultTimestamp));
		//writeByteArray(out, Snappy.compress(result));
		serializeIntArray(out, resultTimestamp);
		serializeIntArray(out, result);
	}

	@Override
	public Chunk deserialize(DataInput in, int available) throws IOException {
		IntCompressor ic = threadLocal_ic.get();
		
		//int[] deltaTimestamps = ic.uncompress(Snappy.uncompressIntArray(readByteArray(in)));
		//int[] deltas = ic.uncompress(Snappy.uncompressIntArray(readByteArray(in)));
		int[] deltaTimestamps = ic.uncompress(deserializeIntArray(in));
		int[] deltas = ic.uncompress(deserializeIntArray(in));
		
		final int SIZE = deltaTimestamps.length;
		AssumptionCheck.throwFalse(deltas.length==SIZE);

		DataEntry[] data = new DataEntry[SIZE];
		int currTimestamp = 0;
		int currDeltaTimestamp = 0;
		int curr = 0;
		int currDelta = 0;
		for (int i = 0; i < SIZE; i++) {
			currDeltaTimestamp += decodeZigZag(deltaTimestamps[i]);
			currTimestamp += currDeltaTimestamp;
			currDelta += decodeZigZag(deltas[i]);
			curr += currDelta;
			data[i] = new DataEntry(currTimestamp, curr/QUANTIZATION_FACTOR);
		}
		return new Chunk(data);
	}

	@Override
	public int fixedSize() {
		return -1;
	}
	
	static int encodeZigZag(int v) {
		return (v << 1) ^ (v >> 31);
	}

	static int decodeZigZag(int v) {
		return (v >>> 1) ^ ((v << 31) >> 31);
	}
	
	static void writeByteArray(DataOutput out, byte[] array) throws IOException {
		out.writeInt(array.length);
		out.write(array);		
	}
	
	static byte[] readByteArray(DataInput in) throws IOException {
		int SIZE = in.readInt();
		byte[] array = new byte[SIZE];
		in.readFully(array);
		return array;
	}
	
	static byte[] intToByteArray(int[] data) {
		int SIZE_INTS = data.length;
		byte[] result = new byte[SIZE_INTS*4];
		int pos=0;
		for(int i=0; i<SIZE_INTS; i++) {
			int v = data[i];
			result[pos++] = (byte) (0xff & v);
			result[pos++] = (byte) (0xff & (v >> 8));
			result[pos++] = (byte) (0xff & (v >> 16));
			result[pos++] = (byte) (0xff & (v >> 24));
		}
		return result;
	}

	static int[] byteToIntArray(byte[] data) {
		int SIZE_INTS = data.length/4;
		int[] result = new int[SIZE_INTS];
		int pos=0;
		for(int i=0; i<SIZE_INTS; i++) {
			int a = data[pos++] & 0xFF;
			int b = data[pos++] & 0xFF;
			int c = data[pos++] & 0xFF;
			int d = data[pos++];
			result[i] = a | (b<<8) | (c<<16) | (d<<24);
		}
		return result;
	}

	static void serializeIntArray(DataOutput out, int[] value) throws IOException {
		out.writeInt(value.length);
		out.write(intToByteArray(value));
	}

	static int[] deserializeIntArray(DataInput in) throws IOException {		
		final int size = in.readInt();
		byte[] byteArray = new byte[size*4];
		in.readFully(byteArray);
		return byteToIntArray(byteArray);
	}
}
