package tsdb.util.iterator;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.Externalizable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mapdb.DataInput2;
import org.mapdb.DataOutput2;

import tsdb.util.DataEntry;
import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;
import tsdb.util.Util;

/**
 * time series with individual time stamp for each entry
 * @author woellauer
 *
 */
public class TimestampSeries implements TsIterable, Serializable, Externalizable {
	private static final Logger log = LogManager.getLogger();

	private static final long serialVersionUID = 6078067255995220349L;

	public static final TimestampSeries EMPTY_TIMESERIES = new TimestampSeries(new String[0],new ArrayList<TsEntry>(0),null);

	public String name;
	public String[] sensorNames;
	public Integer timeinterval; // null if raw data
	public List<TsEntry> entryList;
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeUTF(name);
		out.writeInt(sensorNames.length);
		for(String sensorName:sensorNames) {
			out.writeUTF(sensorName);
		}
		out.writeObject(timeinterval);
		out.writeInt(entryList.size());
		for(TsEntry entry:entryList) {
			out.writeInt((int) entry.timestamp);
			for(int i=0;i<sensorNames.length;i++) {
				out.writeFloat(entry.data[i]);
			}
		}
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		this.name = in.readUTF();
		final int sensorCount = in.readInt();
		this.sensorNames = new String[sensorCount];
		for(int i=0;i<sensorCount;i++) {
			this.sensorNames[i] = in.readUTF();
		}
		timeinterval = (Integer) in.readObject();
		final int entryCount = in.readInt();
		this.entryList = new ArrayList<TsEntry>(entryCount);
		for(int r=0;r<entryCount;r++) {
			int timestamp = in.readInt();
			float[] data = new float[sensorCount];
			for(int i=0;i<sensorCount;i++) {
				data[i] = in.readFloat();
			}
			this.entryList.add(new TsEntry(timestamp,data));
		}		
	}
	
	
	/**
	 * Included data: name, sensorNames, entries
	 * NOT included data; timeInterval
	 * 
	 *
	 */	
	private static class TimeSeriesArchivSerializer implements org.mapdb.Serializer<TimestampSeries> {
		
		final static String TOC_START = "TimestampSeries:start";
		final static String TOC_END = "TimestampSeries:end";

		@Override
		public void serialize(DataOutput out, TimestampSeries timestampSeries) throws IOException {
			out.writeUTF(TOC_START);
			
			out.writeUTF(timestampSeries.name);
			DataOutput2.packInt(out,timestampSeries.sensorNames.length);
			for(String sensorName:timestampSeries.sensorNames) {
				out.writeUTF(sensorName);
			}
			DataOutput2.packInt(out,timestampSeries.entryList.size());
			int prevTimestamp = -1;
			for(TsEntry entry:timestampSeries.entryList) {
				int timestamp = (int) entry.timestamp;
				if(timestamp<=prevTimestamp) {
					throw new RuntimeException("write timestampseries format error: timestamps not ascending ordered "+prevTimestamp+"  "+timestamp+"   "+TimeUtil.oleMinutesToText((long) prevTimestamp)+"  "+TimeUtil.oleMinutesToText((long) timestamp));
				}
				out.writeInt(timestamp);
				for(int i=0;i<timestampSeries.sensorNames.length;i++) {
					out.writeFloat(entry.data[i]);
				}
				prevTimestamp = timestamp;
			}	
			out.writeUTF(TOC_END);
		}

		@Override
		public TimestampSeries deserialize(DataInput in, int available) throws IOException {
			String start = in.readUTF();
			if(!start.equals(TOC_START)) {
				throw new RuntimeException("file format error");
			}			
			
			String name = in.readUTF(); 
			final int sensorCount = DataInput2.unpackInt(in);
			String[] sensorNames = new String[sensorCount];
			for(int i=0;i<sensorCount;i++) {
				sensorNames[i] = in.readUTF();
			}
			final int entryCount = DataInput2.unpackInt(in);
			ArrayList<TsEntry> entryList = new ArrayList<TsEntry>(entryCount);
			int prevTimestamp = -1;
			for(int r=0;r<entryCount;r++) {
				int timestamp = in.readInt();
				if(timestamp<=prevTimestamp) {
					throw new RuntimeException("file format error: timestamps not ascending ordered");
				}
				float[] data = new float[sensorCount];
				for(int i=0;i<sensorCount;i++) {
					data[i] = in.readFloat();
				}
				entryList.add(new TsEntry(timestamp,data));
				prevTimestamp = timestamp;
			}			
			
			String end = in.readUTF();
			if(!end.equals(TOC_END)) {
				throw new RuntimeException("file format error: \""+end+"\"");
			}
			return new TimestampSeries(name, sensorNames, entryList);
		}

		@Override
		public int fixedSize() {
			return -1;
		}
		
	}
	
	public static final org.mapdb.Serializer<TimestampSeries> TIMESERIESARCHIV_SERIALIZER = new TimeSeriesArchivSerializer();
	
	/**
	 * for Externalizable only!
	 */
	public TimestampSeries() {
		
	}


	public TimestampSeries(String[] sensorNames, List<TsEntry> entryList,Integer timeinterval) {
		this.sensorNames = sensorNames;
		this.entryList = entryList;
		this.timeinterval = timeinterval;
		this.name = null;
	}

	public TimestampSeries(String name, String[] sensorNames, List<TsEntry> entryList) {
		this.sensorNames = sensorNames;
		this.entryList = entryList;
		this.timeinterval = null;
		this.name = name;
	}

	public static TimestampSeries create(TsIterator input_iterator, String name) {
		if(!input_iterator.hasNext()) {
			log.warn("TimestampSeries.create: input_iterator is empty");
			//new Exception().printStackTrace(System.out);
			return null;
		}
		List<TsEntry> entryList = new ArrayList<TsEntry>();
		while(input_iterator.hasNext()) {
			TsEntry next = input_iterator.next();
			entryList.add(next);
		}
		TimestampSeries ts = new TimestampSeries(input_iterator.getNames(), entryList, null);
		ts.name = name;
		return ts;
	}

	/**
	 * Only finite elements are added to result array
	 * @param sensorName
	 * @return
	 */
	public DataEntry[] toDataEntyArray(String sensorName) {
		int index = -1;
		for (int i = 0; i < sensorNames.length; i++) {
			if(sensorNames[i].equals(sensorName)) {
				index = i;
				break;
			}
		}
		if(index<0) {
			log.warn("sensorName not found "+sensorName);
			return null;
		}
		ArrayList<DataEntry> resultList = new ArrayList<DataEntry>(entryList.size());
		for(TsEntry entry:entryList) {
			float value = entry.data[index];
			if(Float.isFinite(value)) {
				resultList.add(new DataEntry((int) entry.timestamp,value));
			}
		}
		if(resultList.isEmpty()) {
			log.warn("list empty "+sensorName);
			return null;
		}
		return resultList.toArray(new DataEntry[0]);
	}

	@Override
	public String toString() {
		int n = entryList.size()>=3?3:entryList.size();
		String s=name+"\t"+TimeUtil.oleMinutesToLocalDateTime(getFirstTimestamp())+" - "+TimeUtil.oleMinutesToLocalDateTime(getLastTimestamp())+"\n";
		s+="("+entryList.size()+")\t\t";
		for(int i=0;i<sensorNames.length;i++) {
			s+=sensorNames[i]+"\t";
		}
		s+='\n';
		for(int i=0;i<n;i++) {			
			TsEntry entry = entryList.get(i);
			/*float[] data = entry.data;
			s+=TimeConverter.oleMinutesToLocalDateTime(entry.timestamp)+"\t";
			for(int c=0;c<data.length;c++) {
				s+=Util.floatToString(data[c])+"\t";
			}*/
			s+=entry.toString();
			s+='\n';
		}

		return s;
	}

	public void removeEmptyColumns() {
		int[] columnEntryCounter = new int[sensorNames.length];
		for(int i=0;i<sensorNames.length;i++) {
			columnEntryCounter[i] = 0;
		}
		for(TsEntry entry:entryList) {
			for(int i=0;i<sensorNames.length;i++) {
				if(!Float.isNaN(entry.data[i])) {
					columnEntryCounter[i]++;
				}
			}
		}
		List<Integer> removColumnsList = new ArrayList<Integer>();
		for(int i=0;i<sensorNames.length;i++) {
			if(columnEntryCounter[i] == 0) {
				removColumnsList.add(i);
			}
		}
		if(removColumnsList.size()==0) {
			return; //not columns to remove;
		}
		int newSize = sensorNames.length-removColumnsList.size();
		int[] newPos = new int[newSize];
		int currPos = 0;
		for(int i=0;i<sensorNames.length;i++) {			
			if(columnEntryCounter[i] != 0) {
				newPos[currPos] = i;
				currPos++;
			}
		}
		String[] newParameterNames = new String[newSize];
		for(int i=0;i<newSize;i++) {
			newParameterNames[i] = sensorNames[newPos[i]];
		}
		List<TsEntry> newEntryList = new ArrayList<TsEntry>(entryList.size());
		for(TsEntry entry:entryList) {
			float[] newData = new float[newSize];
			for(int i=0;i<newSize;i++) {
				newData[i] = entry.data[newPos[i]];
			}
			newEntryList.add(new TsEntry(entry.timestamp,newData));
		}
		sensorNames = newParameterNames;
		entryList = newEntryList;
	}

	public TimestampSeries getTimeInterval(long start, long end) {
		List<TsEntry> resultList = new ArrayList<TsEntry>();
		for(TsEntry entry:entryList) {
			long timestamp = entry.timestamp;
			if( start<=timestamp && timestamp<=end ) {
				resultList.add(entry);
			}
		}
		return new TimestampSeries(sensorNames,resultList,timeinterval);
	}

	public List<Long> getNaNList(String parameterName) {

		int columnID = Util.stringArrayToMap(sensorNames).get(parameterName);

		List<Long> gapList = new ArrayList<Long>();
		long currentTimeStamp = -1;
		for(TsEntry entry:entryList) {
			if(!Float.isNaN(entry.data[columnID])) {
				long nextTimeStamp = entry.timestamp;
				if(currentTimeStamp>-1&&currentTimeStamp+timeinterval<nextTimeStamp) {
					System.out.println("gap: "+(nextTimeStamp-(currentTimeStamp+timeinterval)));
					gapList.add(nextTimeStamp);
				}
				currentTimeStamp = nextTimeStamp;
			}
		}
		return gapList;
	}	

	public long getFirstTimestamp() {
		return this.entryList.get(0).timestamp;
	}

	public long getLastTimestamp() {
		return this.entryList.get(entryList.size()-1).timestamp;
	}

	public int size() {
		return entryList.size();
	}

	@Override
	public TsIterator tsIterator() {
		return new TimeSeriesEntryIterator(entryList.iterator(),sensorNames);
	}

	public static void writeToBinaryFile(TimestampSeries tss, String filename) throws IOException {
		ObjectOutputStream objectOutputStream = null;
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(filename, "rw");			
			FileOutputStream fos = new FileOutputStream(raf.getFD());
			objectOutputStream = new ObjectOutputStream(fos);
			objectOutputStream.writeObject(tss);
		} finally {
			if (objectOutputStream != null) {
				objectOutputStream.close();				
			}
			if(raf!=null) {
				raf.close();
			}
		}
	}
	
	public static TimestampSeries readFromBinaryFile(String filename) throws IOException, ClassNotFoundException {
		ObjectInputStream objectInputStream = null;
		RandomAccessFile raf = null;
		try {
			raf = new RandomAccessFile(filename, "r");
			FileInputStream fos = new FileInputStream(raf.getFD());
			objectInputStream = new ObjectInputStream(fos);
			return (TimestampSeries) objectInputStream.readObject();
		} finally {
			if (objectInputStream != null) {
				objectInputStream.close();
			}
			if(raf!=null) {
				raf.close();
			}
		}
	}



	
}
