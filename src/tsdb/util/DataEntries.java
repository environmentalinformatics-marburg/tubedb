package tsdb.util;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.luben.zstd.Zstd;

public class DataEntries {
	private static final Logger log = LogManager.getLogger();
	
	public static int encodeZigZag(int v) {
		return (v << 1) ^ (v >> 31);
	}
	
	public static int decodeZigZag(int v) {
		return (v >>> 1) ^ ((v << 31) >> 31);
	}
	
	
	public static void getTimestampDeltas(DataEntry[] data, int[] ret) {
		int len = data.length;
		int prev = 0;
		for (int i = 0; i < len; i++) {
			int curr = data[i].timestamp;
			ret[i] =  curr - prev;
			prev = curr;
		}
	}
	
	public static void getValueDeltaZigZags(DataEntry[] data, int[] ret, int pos) {
		int len = data.length;
		int prev = 0;		
		for (int i = 0; i < len; i++) {
			int curr = Float.floatToIntBits(data[i].value);
			ret[pos + i] =  encodeZigZag(curr - prev);
			prev = curr;
		}
	}	
	
	public static byte[] intToByteArray(int[] data) {
		int SIZE_INTS = data.length;
		byte[] result = new byte[SIZE_INTS*4];
		int pos=0;
		for(int i=0; i<SIZE_INTS; i++) {
			int v = data[i];
			result[pos] = (byte) (v);
			result[pos+1] = (byte) (v >> 8);
			result[pos+2] = (byte) (v >> 16);
			result[pos+3] = (byte) (v >> 24);
			pos+=4;
		}
		return result;
	}
	
	public static int[] byteToIntArray(byte[] data) {
		int SIZE_INTS = data.length/4;
		int[] result = new int[SIZE_INTS];
		int pos=0;
		for(int i=0; i<SIZE_INTS; i++) {
			result[i] = (data[pos] & 0xFF) | ((data[pos+1] & 0xFF)<<8) | ((data[pos+2] & 0xFF)<<16) | (data[pos+3]<<24);
			pos+=4;
		}		
		return result;
	}
	
	public static void deocdeTimestampDeltas(int[] data, int len) {
		int curr = 0;
		for (int i = 0; i < len; i++) {
			curr += data[i];
			data[i] = curr;
		}
	}
	
	public static void deocdeValueDeltaZigZags(int[] data, int pos, int len) {
		int curr = 0;
		int end = pos + len;
		for (int i = pos; i < end; i++) {
			curr += decodeZigZag(data[i]);
			data[i] = curr;
		}
	}
	
	public static DataEntry[] getDataEntries(int[] data, int pos, int len) {
		DataEntry[] ret = new DataEntry[len];
		for (int i = 0; i < len; i++) {
			ret[i] = new DataEntry(data[i], Float.intBitsToFloat(data[pos + i]));
		}
		return ret;
	}
	
	public static void serialize(DataEntry[] data, DataOutput out) throws IOException {
		int len = data.length;
		int[] ints = new int[len * 2];
		getTimestampDeltas(data, ints);
		getValueDeltaZigZags(data, ints, len);
		byte[] movedInts = Zstd.compress(intToByteArray(ints), 19);
		out.writeInt(len);
		out.writeInt(movedInts.length);
		out.write(movedInts);
	}
	
	public static DataEntry[] deserialize(DataInput in) throws IOException {
		int len = in.readInt();
		int bytelen = in.readInt();
		byte[] movedInts = new byte[bytelen];
		in.readFully(movedInts);
		byte[] dd = new byte[len*8];
		long size = Zstd.decompressedSize(movedInts);
		if(dd.length != size) {
			throw new RuntimeException("wrong size " + dd.length + "  "  + size);
		}
		long ret = Zstd.decompress(dd, movedInts);
		if(ret != len*8) {
			throw new RuntimeException("read error ret " + ret + " len " + len + " bytelen " + bytelen + " size "+ size + "   text: " + Zstd.getErrorName(ret));
		}
		int[] ints = byteToIntArray(dd);
		deocdeTimestampDeltas(ints, len);
		deocdeValueDeltaZigZags(ints, len, len);
		return getDataEntries(ints, len, len);
	}

}
