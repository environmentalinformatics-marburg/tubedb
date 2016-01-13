package tsdb;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapdb.DataInput2;

import tsdb.util.AssumptionCheck;
import tsdb.util.DataEntry;
import tsdb.util.iterator.TimestampSeries;

/**
 * Reads ".tsa"-files
 * @author woellauer
 *
 */
public class TimeSeriesArchivReader {
	private static final Logger log = LogManager.getLogger();

	public static interface TimeSeriesArchivVisitor {
		public default void readDataEntries(String stationName, String sensorName, DataEntry[] dataEntries) {}
		public default void readTimestampSeries(TimestampSeries timestampSeries) {}
	}

	public static enum EntryType {
		TIMESTAMPSERIES,
		DATAENTRYARRAY,
		END 
	}

	private DataInput2 in;

	private boolean open = false;

	private EntryType currentEntryType = null;

	private FileChannel filechannel;
	private final long filechannelSize;
	private long byteBufferPos;
	private long byteBufferMaxSize = 200*1024*1024;//Integer.MAX_VALUE;
	private long minReadCapacity = 100*1024*1024; 

	private ByteBuffer mappedByteBuffer;

	private long byteBufferSize;

	public TimeSeriesArchivReader(String filename) throws IOException {
		this.filechannel = FileChannel.open(Paths.get(filename), StandardOpenOption.READ);
		this.filechannelSize = filechannel.size();
		this.byteBufferMaxSize = 200*1024*1024;//Integer.MAX_VALUE;
		this.minReadCapacity = 100*1024*1024;
		if(filechannelSize<byteBufferMaxSize) {
			byteBufferMaxSize = filechannelSize;
			minReadCapacity = byteBufferMaxSize;
		}
		this.byteBufferPos = 0;
		mappedByteBuffer = ByteBuffer.allocateDirect((int) byteBufferMaxSize);
		mapBuffer();		
	}

	private void mapBuffer() throws IOException {
		byteBufferSize = filechannelSize-byteBufferPos;
		if(byteBufferSize>byteBufferMaxSize) {
			byteBufferSize = byteBufferMaxSize;
		}
		//log.info("remap buffer "+byteBufferPos+" size "+byteBufferSize);
		//mappedByteBuffer = filechannel.map(MapMode.READ_ONLY, byteBufferPos, byteBufferSize);
		mappedByteBuffer.clear();
		int readBytes = filechannel.read(mappedByteBuffer, byteBufferPos);
		if(readBytes!=byteBufferSize) {
			throw new RuntimeException(readBytes+"  "+byteBufferSize);
		}


		byteBufferPos += byteBufferSize;		
		this.in = new DataInput2(mappedByteBuffer,0);
		//System.gc();
	}

	private void ensureBufferCapacity() throws IOException {
		if(byteBufferPos<filechannelSize) {
			final int localPos = in.pos;
			if(byteBufferSize-localPos<minReadCapacity) {
				byteBufferPos = byteBufferPos-byteBufferSize+localPos;
				mapBuffer();
			}
		}
	}

	public void open() throws IOException {
		if(in==null) {
			throw new RuntimeException("no data source");
		}
		if(open) {
			in = null;
			throw new RuntimeException("already open");
		}

		String toc_head = in.readUTF();
		if(!toc_head.equals(TimeSeriesArchivWriter.TOC_HEAD)) {
			in = null;
			throw new RuntimeException("file format error: found not "+TimeSeriesArchivWriter.TOC_HEAD+" but \""+toc_head+"\"");
		}

		String toc_start = in.readUTF();
		if(!toc_start.equals(TimeSeriesArchivWriter.TOC_START)) {
			in = null;
			throw new RuntimeException("file format error: found not "+TimeSeriesArchivWriter.TOC_START+" but \""+toc_start+"\"");
		}

		open = true;
	}

	private EntryType readNextEntryType() throws IOException {
		if(!open) {
			in = null;
			throw new RuntimeException("not open");
		}
		ensureBufferCapacity();
		String entry = in.readUTF();
		if(entry.equals(TimeSeriesArchivWriter.TOC_END)) {
			return EntryType.END;
		}
		if(!entry.equals(TimeSeriesArchivWriter.TOC_ENTRY)) {
			in = null;
			open = false;
			throw new RuntimeException("file format error");
		}
		String type = in.readUTF();
		switch(type) {
		case TimeSeriesArchivWriter.TOC_TYPE_TIMESTAMPSERIES:
			return EntryType.TIMESTAMPSERIES;
		case TimeSeriesArchivWriter.TOC_TYPE_DATAENTRYARRAY:
			return EntryType.DATAENTRYARRAY;
		default:
			in = null;
			open = false;
			throw new RuntimeException("file format error");
		}
	}

	public EntryType getNextEntryType() throws IOException {
		if(currentEntryType!=null) {
			in = null;
			open = false;
			throw new RuntimeException("file format error");
		}
		EntryType e = readNextEntryType();
		currentEntryType = e;
		return e;
	}

	public TimestampSeries getTimestampSeries() throws IOException {
		if(!open) {
			in = null;
			throw new RuntimeException("not open");
		}
		if(currentEntryType!=EntryType.TIMESTAMPSERIES) {
			throw new RuntimeException("wrong type");
		}
		TimestampSeries tss = TimestampSeries.TIMESERIESARCHIV_SERIALIZER.deserialize(in, -1);
		currentEntryType = null;
		return tss;
	}

	public static class DataEntriesTriple {
		public final String stationName;
		public final String sensorName;
		public final DataEntry[] dataEntries;
		public DataEntriesTriple(String stationName, String sensorName, DataEntry[] dataEntries) {
			this.stationName = stationName;
			this.sensorName = sensorName;
			this.dataEntries = dataEntries;
		}
	}

	public DataEntriesTriple getDataEntryArray() throws IOException {
		if(!open) {
			in = null;
			throw new RuntimeException("not open");
		}
		if(currentEntryType!=EntryType.DATAENTRYARRAY) {
			throw new RuntimeException("wrong type");
		}
		String stationName = in.readUTF();
		String sensorName = in.readUTF();
		DataEntry[] dataEntries = DataEntry.TIMESERIESARCHIV_SERIALIZER.deserialize(in, -1);
		currentEntryType = null;
		return new DataEntriesTriple(stationName,sensorName,dataEntries); 
	}

	public void close() throws IOException {
		if(filechannel!=null) {
			filechannel.close();
			filechannel = null;
		}
		in = null;
		open = false;
	}

	public void readFully(TimeSeriesArchivVisitor timeSeriesArchivVisitor) throws IOException {
		AssumptionCheck.throwNull(timeSeriesArchivVisitor);
		this.open();
		boolean loop = true;
		while(loop) {
			EntryType entryType = this.getNextEntryType();
			//log.info("entry: "+entryType);
			switch(entryType) {
			case DATAENTRYARRAY:
				DataEntriesTriple triple = this.getDataEntryArray();
				timeSeriesArchivVisitor.readDataEntries(triple.stationName,triple.sensorName,triple.dataEntries);
				break;
			case TIMESTAMPSERIES:
				TimestampSeries timestampSeries = this.getTimestampSeries();
				timeSeriesArchivVisitor.readTimestampSeries(timestampSeries);
				break;
			case END:
				loop = false;
				break;
			default:
				log.error("unknown "+entryType);
				loop = false;
			}
		}
		this.close();		
	}

	public static void importStationsFromFile(TsDB tsdb, String filename) {
		try {
			TimeSeriesArchivReader tsaReader = new TimeSeriesArchivReader(filename);
			tsaReader.readFully(new TimeSeriesArchivVisitor(){
				@Override
				public void readDataEntries(String stationName, String sensorName, DataEntry[] dataEntries) {
					//log.info(stationName+" "+sensorName+" "+dataEntries.length);
					tsdb.streamStorage.insertDataEntryArray(stationName, sensorName, dataEntries);
				}
				@Override
				public void readTimestampSeries(TimestampSeries timestampSeries) {
					//log.info(timestampSeries.name);
					tsdb.streamStorage.insertTimestampSeries(timestampSeries);
				}});
		} catch (IOException e) {
			log.error(e);
		}		
	}

	public static void main(String[] args) {
		TsDB tsdb = TsDBFactory.createDefault();
		long timeStartImport = System.currentTimeMillis();
		importStationsFromFile(tsdb, TsDBFactory.OUTPUT_PATH+"/full.tsa");
		tsdb.close();
		long timeEndImport = System.currentTimeMillis();
		log.info((timeEndImport-timeStartImport)/1000+" s Import");
	}

}
