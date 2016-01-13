package tsdb.testing;

import java.io.IOException;

import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;
import org.mapdb.Serializer;
import org.xerial.snappy.Snappy;

import me.lemire.integercompression.FastPFOR;
import me.lemire.integercompression.IntCompressor;
import me.lemire.integercompression.SkippableComposition;
import me.lemire.integercompression.VariableByte;
import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.streamdb.Chunk;
import tsdb.streamdb.ChunkSerializer;
import tsdb.streamdb.StreamIterator;
import tsdb.util.DataEntry;
import tsdb.util.TimeUtil;
import tsdb.util.Util;

public class TestingCompression {
	private static final Logger log = LogManager.getLogger();

	private static final float QUANTIZATION_FACTOR = 100f;

	public static int encodeZigZag(int v) {
		return (v << 1) ^ (v >> 31);
	}

	public static int decodeZigZag(int v) {
		return (v >>> 1) ^ ((v << 31) >> 31);
	}

	private static final int ROUNDS = 1;
	private static final int INNER_ROUNDS = 1000;


	public static void main(String[] args) throws IOException {



		TsDB tsdb = TsDBFactory.createDefault();

		StreamIterator it = tsdb.streamStorage.getRawSensorIterator("51021020218", "Ta_200", TimeUtil.ofDateStartHour(2014), TimeUtil.ofDateEndHour(2014));

		DataEntry[] data = it.remainingToArray();

		long timeStart = System.currentTimeMillis();

		compressOld(data);
		//compressNew(data);

		long timeEnd = System.currentTimeMillis();

		log.info(Util.msToText(timeStart,timeEnd)+" compress");



	}

	private static void compressOld(DataEntry[] data) throws IOException {
		for(int round=0;round<ROUNDS;round++) {
			Chunk chunk = Chunk.of(data);
			DataOutput2 out = new DataOutput2();
			Chunk.SNAPPY_DELTA_TIME_DELTA_DELTA_VALUE_INT_QUANTIZED_SERIALIZER.serialize(out, chunk);
			byte[] raw = out.copyBytes();
			//log.info(raw.length);
			for(int innerRound=0;innerRound<INNER_ROUNDS;innerRound++) {
				DataInput2 in = new DataInput2(raw);
				Chunk chunkNew = Chunk.SNAPPY_DELTA_TIME_DELTA_DELTA_VALUE_INT_QUANTIZED_SERIALIZER.deserialize(in, -1);
			}
		}
	}


	private static void compressNew(DataEntry[] data) throws IOException {
		for(int round=0;round<ROUNDS;round++) {
			Chunk chunk = Chunk.of(data);
			DataOutput2 out = new DataOutput2();
			ChunkSerializer.DEFAULT.serialize(out, chunk);
			byte[] raw = out.copyBytes();
			//log.info(raw.length);
			for(int innerRound=0;innerRound<INNER_ROUNDS;innerRound++) {
				DataInput2 in = new DataInput2(raw);
				Chunk chunkNew = ChunkSerializer.DEFAULT.deserialize(in, -1);
			}
		}
	}


	private static void compressNewPrev(DataEntry[] data) throws IOException {
		final int SIZE = data.length;		

		for(int round=0;round<ROUNDS;round++) {

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
				if(Float.isFinite(value)) {
					throw new RuntimeException("NaN Value in stream");
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

			SkippableComposition codec = new SkippableComposition(new FastPFOR(), new VariableByte());
			IntCompressor ic = new IntCompressor(codec);

			int[] resultTimestamp = ic.compress(deltaTimestamps);
			int[] result = ic.compress(deltas);

			DataOutput2 out = new DataOutput2();
			//Serializer.INT_ARRAY.serialize(out, resultTimestamp);
			//Serializer.INT_ARRAY.serialize(out, result);
			Serializer.BYTE_ARRAY.serialize(out, Snappy.compress(resultTimestamp));
			Serializer.BYTE_ARRAY.serialize(out, Snappy.compress(result));
			byte[] raw = out.copyBytes();
			log.info(raw.length);

			/*
			System.out.println("size\t"+SIZE*4);
			System.out.println("result\t"+resultTimestamp.length*4);

			System.out.println("size\t"+SIZE*4);
			System.out.println("result\t"+result.length*4);*/

			for (int innterLoop = 0; innterLoop < 1; innterLoop++) {


				DataInput2 in = new DataInput2(raw);

				//int[] deltaTimestampsNew = ic.uncompress(Serializer.INT_ARRAY.deserialize(in, -1));
				//int[] deltasNew = ic.uncompress(Serializer.INT_ARRAY.deserialize(in, -1));
				int[] deltaTimestampsNew = ic.uncompress(Snappy.uncompressIntArray(Serializer.BYTE_ARRAY.deserialize(in, -1)));
				int[] deltasNew = ic.uncompress(Snappy.uncompressIntArray(Serializer.BYTE_ARRAY.deserialize(in, -1)));

				DataEntry[] dataNew = new DataEntry[SIZE];
				int currTimestamp = 0;
				int currDeltaTimestamp = 0;
				int curr = 0;
				int currDelta = 0;
				for (int i = 0; i < SIZE; i++) {
					currDeltaTimestamp += decodeZigZag(deltaTimestampsNew[i]);
					currTimestamp += currDeltaTimestamp;
					currDelta += decodeZigZag(deltasNew[i]);
					curr += currDelta;
					dataNew[i] = new DataEntry(currTimestamp, curr/QUANTIZATION_FACTOR);
				}

				/*if(!DataEntry.equals(data, dataNew)) {
					log.error("wrong data");
				}*/

			}
		}
	}

}
