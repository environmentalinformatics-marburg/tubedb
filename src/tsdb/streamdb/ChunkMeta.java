package tsdb.streamdb;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.Serializable;
import java.util.Iterator;

import org.mapdb.BTreeMap;
import org.mapdb.Serializer;

/**
 * Metadata of chunk
 * immutable
 * @author woellauer
 */
public class ChunkMeta {
	public final int firstTimestamp;
	public final int lastTimestamp;
	public final int entryCount;

	public ChunkMeta(int firstTimestamp, int lastTimestamp, int entryCount) {
		this.firstTimestamp = firstTimestamp;
		this.lastTimestamp = lastTimestamp;
		this.entryCount = entryCount;
	}

	@Override
	public String toString() {
		return "["+firstTimestamp+", "+lastTimestamp+"]: "+entryCount;
	}

	private static class ChunkMetaSerializer implements Serializer<ChunkMeta>, Serializable {
		private static final long serialVersionUID = -4064482638307132258L;
		@Override
		public void serialize(DataOutput out, ChunkMeta s)
				throws IOException {
			out.writeInt(s.firstTimestamp);
			out.writeInt(s.lastTimestamp);
			out.writeInt(s.entryCount);
		}
		@Override
		public ChunkMeta deserialize(DataInput in, int available) throws IOException {			
			int firstTimestamp = in.readInt();
			int lastTimestamp = in.readInt();
			int entryCount = in.readInt();
			return new ChunkMeta(firstTimestamp,lastTimestamp,entryCount);
		}
		@Override
		public int fixedSize() {
			return -1;
		}		
	};

	public static final Serializer<ChunkMeta> SERIALIZER = new ChunkMetaSerializer();

	public static Iterator<ChunkMeta> createIterator(BTreeMap<Integer, ChunkMeta> chunkMetaMap,int minTimestamp, int maxTimestamp) {
		return new ChunkMetaIterator(chunkMetaMap,minTimestamp,maxTimestamp);
	}

	private static class ChunkMetaIterator implements Iterator<ChunkMeta> {

		private ChunkMeta current;
		private Iterator<ChunkMeta> it;
		private final int maxTimestamp;

		public ChunkMetaIterator(BTreeMap<Integer, ChunkMeta> chunkMetaMap, int minTimestamp, int maxTimestamp) {
			this.maxTimestamp = maxTimestamp;
			it = chunkMetaMap.values().iterator();
			while(it.hasNext()) {
				current = it.next();
				if(minTimestamp<=current.lastTimestamp) {
					if(current.firstTimestamp<=maxTimestamp) {
						return;
					} else {
						it = null;
						current = null;
						return;
					}
				}
			}
			it = null;
			current = null;
		}

		@Override
		public boolean hasNext() {
			return current!=null;
		}

		@Override
		public ChunkMeta next() {
			ChunkMeta r = current;
			if(it.hasNext()) {
				current = it.next();
				if(maxTimestamp<current.firstTimestamp) {
					current = null;
				}
			} else {
				current = null;
			}
			return r;
		}
	}
}
