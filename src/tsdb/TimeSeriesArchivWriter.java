package tsdb;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.NavigableSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapdb.DataOutput2;

import tsdb.streamdb.StreamIterator;
import tsdb.util.DataEntry;
import tsdb.util.iterator.TimestampSeries;

/**
 * writes ".tsa"-files
 * @author woellauer
 *
 */
public class TimeSeriesArchivWriter {
	private static final Logger log = LogManager.getLogger();

	final static String TOC_HEAD = "Time_Series_Archiv_v_1_0_0";

	final static String TOC_START = "TimeSeriesArchiv:start";
	final static String TOC_END = "TimeSeriesArchiv:end";

	final static String TOC_ENTRY = "Entry";

	final static String TOC_TYPE_TIMESTAMPSERIES = "TimestampSeries";
	final static String TOC_TYPE_DATAENTRYARRAY = "DataEntryArray";

	final static int MAX_ATOMIC_WRITE_SIZE = 100*1024*1024;

	private final OutputStream outputStream;
	private DataOutput2 out;

	private boolean open = false;

	private String filename = null;

	public TimeSeriesArchivWriter(String filename) throws FileNotFoundException {
		this(new BufferedOutputStream(new FileOutputStream(filename)));
		this.filename = filename;
	}

	public TimeSeriesArchivWriter(OutputStream outputStream) {
		this.outputStream = outputStream;
		this.out = new DataOutput2(new byte[MAX_ATOMIC_WRITE_SIZE]);		
	}

	private void writeToStream() throws IOException {
		outputStream.write(out.buf, 0, out.pos);
		out.pos = 0;
	}

	public void open() throws IOException {
		if(out==null) {
			throw new RuntimeException("no data target");
		}
		if(open) {
			out = null;
			throw new RuntimeException("already open");
		}
		out.writeUTF(TOC_HEAD);
		out.writeUTF(TOC_START);
		open = true;
	}

	public void close() throws IOException {
		if(!open) {
			throw new RuntimeException("already closed");
		}
		open = false;
		out.writeUTF(TOC_END);
		writeToStream();
		outputStream.flush();
		if(filename!=null) {
			out.close();
		}
		out = null;
	}

	private void writeEntry(String typeName) throws IOException {
		if(!open) {
			throw new RuntimeException("not open");
		}
		out.writeUTF(TOC_ENTRY);
		out.writeUTF(typeName);
	}

	public void writeTimestampSeries(TimestampSeries timestampSeries) throws IOException {
		if(timestampSeries.sensorNames.length>1) {
			if(!open) {
				throw new RuntimeException("not open");
			}
			writeEntry(TOC_TYPE_TIMESTAMPSERIES);
			TimestampSeries.TIMESERIESARCHIV_SERIALIZER.serialize(out, timestampSeries);
			writeToStream();
		} else if(timestampSeries.sensorNames.length==1) {
			String sensorName = timestampSeries.sensorNames[0];
			DataEntry[] dataEntries = timestampSeries.toDataEntyArray(timestampSeries.sensorNames[0]);
			if(dataEntries!=null) {
				writeDataEntryArray(timestampSeries.name, sensorName, dataEntries);
			}
		} else {
			throw new RuntimeException();
		}
	}

	public void writeDataEntryArray(String stationName, String sensorName, DataEntry[] dataEntries) throws IOException {
		if(!open) {
			throw new RuntimeException("not open");
		}
		writeEntry(TOC_TYPE_DATAENTRYARRAY);
		out.writeUTF(stationName);
		out.writeUTF(sensorName);
		DataEntry.TIMESERIESARCHIV_SERIALIZER.serialize(out, dataEntries);
		writeToStream();
	}

	private void writeStreamIterator(StreamIterator it) throws IOException {
		ArrayList<DataEntry> list = new ArrayList<DataEntry>();
		while(it.hasNext()) {
			list.add(it.next());
		}
		if(!list.isEmpty()) {
			writeDataEntryArray(it.stationName,it.sensorName,list.toArray(new DataEntry[0]));
		}
	}

	public static void writeAllStationsToFile(String filename) {
		TsDB tsdb = TsDBFactory.createDefault();

		NavigableSet<String> stationNames = tsdb.streamStorage.getStationNames();	

		long timeStartImport = System.currentTimeMillis();
		try {
			TimeSeriesArchivWriter tsaWriter = new TimeSeriesArchivWriter(filename);
			tsaWriter.open();
			for(String stationName:stationNames) {
				try {
					String[] sensorNames = tsdb.streamStorage.getSensorNames(stationName);
					for(String sensorName:sensorNames) {
						StreamIterator it = tsdb.streamStorage.getRawSensorIterator(stationName, sensorName, null, null);
						if(it!=null&&it.hasNext()) {
							//log.info(it);
							tsaWriter.writeStreamIterator(it);
						}
					}
				} catch(Exception e) {
					log.error(e);
				}
			}
			tsaWriter.close();
		} catch (Exception e) {
			log.error(e);
		}
		long timeEndImport = System.currentTimeMillis();
		log.info((timeEndImport-timeStartImport)/1000+" s Export");

	}




	public static void main(String[] args) {
		writeAllStationsToFile(TsDBFactory.OUTPUT_PATH+"/full.tsa");
	}
}
