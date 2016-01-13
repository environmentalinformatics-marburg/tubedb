package tsdb.streamdb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapdb.BTreeMap;

import tsdb.util.DataEntry;
import tsdb.util.processingchain.ProcessingChain;
import tsdb.util.processingchain.ProcessingChainEntry;
import tsdb.util.processingchain.ProcessingChainSupplier;

/**
 * Iterator over one time series
 * @author woellauer
 *
 */
public class StreamIterator implements Iterator<DataEntry>, ProcessingChainEntry, ProcessingChainSupplier {	
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger();

	private final BTreeMap<Integer, Chunk> sensorChunkMap;
	public final int minQueryTimestamp;
	public final int maxQueryTimestamp;
	
	public final String stationName;
	public final String sensorName;

	private final Iterator<ChunkMeta> chunkMetaIterator;
	private Iterator<DataEntry> dataEntryIterator;
	
	private static int getDataMinTimestamp(BTreeMap<Integer, ChunkMeta> chunkMetaMap, int minTimestamp) {
		Entry<Integer, ChunkMeta> entry = chunkMetaMap.firstEntry();
		if(entry==null) {
			return minTimestamp;
		}
		if(minTimestamp<entry.getValue().firstTimestamp) {
			return entry.getValue().firstTimestamp;
		}
		return minTimestamp;
	}
	
	private static int getDataMaxTimestamp(BTreeMap<Integer, ChunkMeta> chunkMetaMap, int maxTimestamp) {
		Entry<Integer, ChunkMeta> entry = chunkMetaMap.lastEntry();
		if(entry==null) {
			return maxTimestamp;
		}
		if(maxTimestamp>entry.getValue().lastTimestamp) {
			return entry.getValue().lastTimestamp;
		}
		return maxTimestamp;
	}

	public StreamIterator(SensorMeta sensorMeta, BTreeMap<Integer, ChunkMeta> chunkMetaMap, BTreeMap<Integer, Chunk> sensorChunkMap, int minQueryTimestamp, int maxQueryTimestamp) {
		this.sensorChunkMap = sensorChunkMap;
		this.stationName = sensorMeta.stationName;
		this.sensorName = sensorMeta.sensorName;
		
		int min = getDataMinTimestamp(chunkMetaMap,minQueryTimestamp);
		int max = getDataMaxTimestamp(chunkMetaMap,maxQueryTimestamp);
		
		if(min<=max) {
			this.minQueryTimestamp = min;
			this.maxQueryTimestamp = max;
		} else {
			this.minQueryTimestamp = -1;
			this.maxQueryTimestamp = -1;
		}		
		
		this.chunkMetaIterator = ChunkMeta.createIterator(chunkMetaMap, minQueryTimestamp, maxQueryTimestamp);
		if(chunkMetaIterator.hasNext()) {
			nextChunk();
		} else {
			this.dataEntryIterator = Collections.emptyIterator();
		}
	}

	private void nextChunk() {
		ChunkMeta chunkMeta = chunkMetaIterator.next();
		//log.info("chunk "+TimeConverter.oleMinutesToText(chunkMeta.firstTimestamp,chunkMeta.lastTimestamp));
		Chunk chunk = sensorChunkMap.get(chunkMeta.firstTimestamp);
		if(minQueryTimestamp<=chunkMeta.firstTimestamp) {
			if(chunkMeta.lastTimestamp<=maxQueryTimestamp) {
				dataEntryIterator = new SimpleIterator(chunk.data);
			} else {
				dataEntryIterator = new ClipIterator(chunk.data,minQueryTimestamp,maxQueryTimestamp);
			}
		} else {
			if(chunkMeta.lastTimestamp<=maxQueryTimestamp) {
				dataEntryIterator = new SimpleIterator(chunk.data,minQueryTimestamp);
			} else {
				dataEntryIterator = new ClipIterator(chunk.data,minQueryTimestamp,maxQueryTimestamp);
			}
		}
	}

	@Override
	public boolean hasNext() {
		while(!dataEntryIterator.hasNext()) {
			if(!chunkMetaIterator.hasNext()) {
				return false;
			}
			nextChunk();
		}
		return true;
	}

	@Override
	public DataEntry next() {
		return dataEntryIterator.next();
	}
	
	private static class SimpleIterator implements Iterator<DataEntry> {		
		private final DataEntry[] chunk;
		private int currentPos;		
		public SimpleIterator(DataEntry[] chunk) {
			this.chunk = chunk;
			this.currentPos = 0;
		}
		public SimpleIterator(DataEntry[] chunk, int minTimestamp) {
			this.chunk = chunk;
			this.currentPos = 0;
			while(currentPos!=chunk.length&&chunk[currentPos].timestamp<minTimestamp) {
				currentPos++;
			}
		}
		@Override
		public boolean hasNext() {
			return currentPos!=chunk.length;
		}
		@Override
		public DataEntry next() {
			return chunk[currentPos++];
		}		
	}
	
	private static class ClipIterator implements Iterator<DataEntry> {		
		private final DataEntry[] chunk;
		private final int maxTimestamp;
		private int currentPos;		
		public ClipIterator(DataEntry[] chunk, int minTimestamp, int maxTimestamp) {
			this.chunk = chunk;
			this.maxTimestamp = maxTimestamp;
			this.currentPos = 0;
			while(currentPos!=chunk.length&&chunk[currentPos].timestamp<minTimestamp) {
				currentPos++;
			}
		}
		@Override
		public boolean hasNext() {
			return currentPos!=chunk.length&&chunk[currentPos].timestamp<=maxTimestamp;
		}
		@Override
		public DataEntry next() {
			return chunk[currentPos++];
		}		
	}

	@Override
	public String toString() {
		return "streamIterator("+stationName+"/"+sensorName+"["+minQueryTimestamp+","+maxQueryTimestamp+"])";
	}

	@Override
	public String getProcessingTitle() {
		return "stream("+stationName+"/"+sensorName+")";
	}

	@Override
	public ProcessingChain getProcessingChain() {
		return ProcessingChain.of(this);
	}
	
	public DataEntry[] remainingToArray() {
		ArrayList<DataEntry> data = new ArrayList<DataEntry>();
		while(hasNext()) {
			DataEntry e = next();
			data.add(e);
		}
		return data.toArray(new DataEntry[0]);
	}
	
	
}
