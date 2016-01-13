package tsdb.loader.be;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.TimeConverter;
import tsdb.util.TimeUtil;

/**
 * UniversalDataBinFile reads, cleans and structures data of a UDBF-File.
 * @author woellauer
 *
 */
public class UniversalDataBinFile {

	private static final Logger log = LogManager.getLogger();

	final int MAX_VALID_ROW_ID = 30000;

	private Path filename;
	private int fileSize;
	private MappedByteBuffer mappedByteBuffer;
	private FileChannel fileChannel;
	private FileInputStream fileInputStream;
	private short variableCount;
	private TimeConverter timeConverter;
	private int dataSectionStartFilePosition;
	private SensorHeader[] sensorHeaders;
	private boolean empty = false;

	public static class DataRow {

		public static final Comparator<DataRow> COMPARATOR = new Comparator<DataRow>() {
			@Override
			public int compare(DataRow o1, DataRow o2) {
				return Integer.compare(o1.id, o2.id);
			}			
		};

		public final int id;
		public final float[] data; 

		public DataRow(int id, float[] data) {
			this.id = id;
			this.data = data;
		}

	}

	public UniversalDataBinFile(Path fileName) throws IOException {
		this.filename = fileName;
		initFile();
		if(!empty) {
			readHeader();
		}
	}

	public boolean isEmpty() {
		return empty;
	}

	public void close() {
		try {
			fileChannel.close();
			fileInputStream.close();
		} catch (IOException e) {
			log.error(e);
		}
	}

	private void initFile() throws IOException {
		fileInputStream = new FileInputStream(filename.toString());
		fileChannel = fileInputStream.getChannel();
		if(fileChannel.size()>Integer.MAX_VALUE) {
			throw new RuntimeException("File > Integer.MAX_VALUE: "+fileChannel.size());
		}
		fileSize = (int) fileChannel.size();
		empty = fileSize==0;
		mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileSize);
	}

	private void readHeader() throws IOException {
		fileChannel.position(0);

		byte isBigEndian = mappedByteBuffer.get();
		//System.out.println(isBigEndian+"\tisBigEndian");
		if(isBigEndian!=1) {
			throw new RuntimeException("no readable Universal-Data-Bin-File header: not big endian: "+isBigEndian);
		}		
		short version = mappedByteBuffer.getShort();
		//System.out.println(version+"\tversion");
		if(version!=107) {
			throw new RuntimeException("just Universal-Data-Bin-File version 1.07 implemented: "+version);
		}
		short typeVendorLen  = mappedByteBuffer.getShort();
		//System.out.println(typeVendorLen+"\ttypeVendorLen");
		byte[] typeVendorBytes = new byte[typeVendorLen]; 
		mappedByteBuffer.get(typeVendorBytes);
		///*String typeVendor =*/ new String(typeVendorBytes);
		//System.out.println(typeVendor+"\ttypeVendor");
		/*byte withCheckSum =*/ mappedByteBuffer.get();
		//System.out.println(withCheckSum+"\twithCheckSum");
		short moduleAdditionalDataLen = mappedByteBuffer.getShort();
		//System.out.println(moduleAdditionalDataLen+"\tmoduleAdditionalDataLen");
		if(moduleAdditionalDataLen>0) {
			throw new RuntimeException("reading of additional optional data in header not implemented: "+moduleAdditionalDataLen);
		}
		double startTimeToDayFactor = mappedByteBuffer.getDouble();
		//System.out.println(startTimeToDayFactor+"\tstartTimeToDayFactor");
		/*short dActTimeDataType =*/ mappedByteBuffer.getShort();
		//System.out.println(dActTimeDataType+"\tdActTimeDataType");
		double dActTimeToSecondFactor = mappedByteBuffer.getDouble();
		//System.out.println(dActTimeToSecondFactor+"\tdActTimeToSecondFactor");
		double startTime = mappedByteBuffer.getDouble();
		//System.out.println(startTime+"\tstartTime");
		double sampleRate = mappedByteBuffer.getDouble();
		//System.out.println(sampleRate+"\tsampleRate");
		variableCount = mappedByteBuffer.getShort();
		//System.out.println(variableCount+" variableCount");

		timeConverter = new TimeConverter(startTimeToDayFactor, dActTimeToSecondFactor, startTime, sampleRate);

		readSensorHeaders();

		int headerEndPosition = mappedByteBuffer.position();

		//System.out.println(headerEndPosition+"\theaderEndPosition");

		final int MIN_SEPARATION_CHARACTERS = 8;

		int minDataStartPosition = headerEndPosition + MIN_SEPARATION_CHARACTERS;

		final int ALIGN_GRID_SIZE = 16;

		int offset = (minDataStartPosition % ALIGN_GRID_SIZE)==0?0:ALIGN_GRID_SIZE - (minDataStartPosition % ALIGN_GRID_SIZE);

		//System.out.println(offset+" offset");

		dataSectionStartFilePosition = minDataStartPosition + offset;

		//System.out.println(dataSectionStartFilePosition+"\tdataSectionStartFilePosition");


	}

	private void readSensorHeaders() {

		sensorHeaders = new SensorHeader[variableCount];

		for(int i=0;i<variableCount;i++) {
			short nameLen = mappedByteBuffer.getShort();
			//System.out.println(nameLen+"\tnameLen");
			byte[] nameBytes = new byte[nameLen-1];
			mappedByteBuffer.get(nameBytes);
			mappedByteBuffer.get();
			String name = new String(nameBytes);
			//System.out.println(name+"\tname");
			/*short dataDirection =*/ mappedByteBuffer.getShort();
			//System.out.println(dataDirection+"\tdataDirection");
			short dataType = mappedByteBuffer.getShort();
			//System.out.println(dataType+"\tdataType");
			/*short fieldLen =*/ mappedByteBuffer.getShort();
			//System.out.println(fieldLen+"\tfieldLen");
			/*short precision =*/ mappedByteBuffer.getShort();
			//System.out.println(precision+"\tprecision");
			short unitLen = mappedByteBuffer.getShort();
			//System.out.println(unitLen+"\tunitLen");
			byte[] unitBytes = new byte[unitLen-1];
			mappedByteBuffer.get(unitBytes);
			String unit = new String(unitBytes);
			//System.out.println('"'+unit+"\"\tunit");
			short additionalDataLen = mappedByteBuffer.getShort();
			//System.out.println(additionalDataLen+"\tadditionalDataLen");
			if(additionalDataLen!=0) {
				throw new RuntimeException("reading of additional optional data in element header not implemented");
			}
			/*byte b =*/ mappedByteBuffer.get();
			//System.out.println(b+"\t?");

			sensorHeaders[i] = new SensorHeader(name,unit,dataType);			
		}

		int nullCount=0;
		for(int i=0;i<sensorHeaders.length;i++) {
			if(sensorHeaders[i].dataType==0) {
				nullCount++;
				//System.out.println("warning: header entry with no data: "+sensorHeaders[i].name+"\t"+sensorHeaders[i].unit);
			}
		}

		if(nullCount>0) {
			SensorHeader[] temp = new SensorHeader[sensorHeaders.length-nullCount];
			int c=0;
			for(int i=0;i<sensorHeaders.length;i++) {
				if(sensorHeaders[i].dataType!=0) {
					temp[c] = sensorHeaders[i];
					c++;
				}
			}
			sensorHeaders = temp;
			variableCount = (short) sensorHeaders.length;
		}		
	}

	/**
	 * Reads all data rows from file without further processing.
	 * @return Array of Datarows
	 */
	public DataRow[] readDataRows() {
		mappedByteBuffer.position(dataSectionStartFilePosition);
		//int dataRowByteSize = (variableCount+1)*4;
		int dataRowByteSize = 4;
		for(int sensorID=0;sensorID<variableCount;sensorID++) {
			switch(sensorHeaders[sensorID].dataType) {
			case 1:
				dataRowByteSize += 1; // ~ 1 byte boolean
				break;
			case 8:
				dataRowByteSize += 4; // ~ 4 byte float
				break;
			case 7:
				dataRowByteSize += 4; // ~ 4 byte int
				break;
			default:
				throw new RuntimeException("type not implemented:\t"+sensorHeaders[sensorID].dataType);
			}			
		}

		if((fileSize-dataSectionStartFilePosition)%dataRowByteSize!=0){
			log.warn("file end not at row boundary: "+filename+"\t"+fileSize+"\t"+dataSectionStartFilePosition+"\t"+dataRowByteSize+"\t"+(fileSize-dataSectionStartFilePosition)%dataRowByteSize+"\t"+timeConverter.getStartDateTime());
			//return null;
		}

		int dataEntryCount = (fileSize-dataSectionStartFilePosition)/dataRowByteSize;

		DataRow[] datarows = new DataRow[dataEntryCount];

		for(int i=0;i<dataEntryCount;i++) {
			float[] data = new float[variableCount];
			int rowID = mappedByteBuffer.getInt();
			for(int sensorID=0;sensorID<variableCount;sensorID++) {
				switch(sensorHeaders[sensorID].dataType) {
				case 1:
					data[sensorID] = mappedByteBuffer.get(); // ~ 1 byte boolean
					break;
				case 8:
					data[sensorID] = mappedByteBuffer.getFloat(); 
					break;
				case 7:
					data[sensorID] = mappedByteBuffer.getInt();
					break;
				default:
					throw new RuntimeException("type not implemented:\t"+sensorHeaders[sensorID].dataType);
				}

			}
			datarows[i] = new DataRow(rowID, data);
		}
		return datarows;
	}

	public UDBFTimestampSeries getUDBFTimeSeries() {
		DataRow[] dataRows = readDataRows();
		if(dataRows.length==0) {
			return null;
		}
		//Arrays.sort(dataRows, DataRow.COMPARATOR); // don't sort first!

		ArrayList<DataRow> tempRowList = new ArrayList<DataRow>(dataRows.length);

		if(dataRows.length==0) {
			//nothing
		} else if (dataRows.length==1) {
			tempRowList.add(dataRows[0]);
		} else {
			if(dataRows[0].id+1==dataRows[1].id) {
				tempRowList.add(dataRows[0]);
			}
			for(int i=1;i<dataRows.length-1;i++) {
				if(dataRows[i-1].id+1==dataRows[i].id || dataRows[i].id+1==dataRows[i+1].id) {
					tempRowList.add(dataRows[i]);
				} else {
					//log.warn("no "+dataRows[i].id);
				}
			}
			if(dataRows[dataRows.length-2].id+1==dataRows[dataRows.length-1].id) {
				tempRowList.add(dataRows[dataRows.length-1]);
			}
		}

		if(tempRowList.isEmpty()) {
			return null;
		}

		tempRowList.sort(DataRow.COMPARATOR);

		dataRows = tempRowList.toArray(new DataRow[tempRowList.size()]);

		tempRowList.clear();

		int prevCheckID = -1;
		for(int i=0;i<dataRows.length;i++) {
			if(dataRows[i].id<0) {
				continue;
			}
			if(dataRows[i].id>10000000) { //invalid id at AEW40: 134220377 and 134220378
				log.warn("invalid id "+dataRows[i].id);
				continue;
			}
			if(dataRows[i].id==prevCheckID) {
				DataRow prevRow = tempRowList.get(tempRowList.size()-1);
				if(prevRow.id==prevCheckID) {
					if(!Arrays.equals(prevRow.data, dataRows[i].data)) {
						tempRowList.remove(tempRowList.size()-1);
						//log.info("duplicate id row different "+dataRows[i].id+"  in "+filename+"   prev "+Arrays.toString(prevRow.data)+" curr "+Arrays.toString(dataRows[i].data));
					} else {
						//log.info("duplicate id row same "+dataRows[i].id+"  in "+filename);
					}
				}
			} else {
				tempRowList.add(dataRows[i]);
				prevCheckID = dataRows[i].id; 
			}
		}

		if(tempRowList.isEmpty()) {
			return null;
		}

		long[] time = new long[tempRowList.size()];
		float[][] data = new float[tempRowList.size()][];
		for(int rowIndex=0; rowIndex<tempRowList.size(); rowIndex++) {
			data[rowIndex] = new float[sensorHeaders.length];
		}

		final long time_offset = timeConverter.getStartTimeOleMinutes();
		final long time_step = timeConverter.getTimeStepMinutes();

		Integer prevID = null;
		int rowIndex = 0;
		for(DataRow row:tempRowList) {
			time[rowIndex] = time_offset + (row.id*time_step);
			for(int sensorIndex=0;sensorIndex<sensorHeaders.length;sensorIndex++) {
				data[rowIndex][sensorIndex] = row.data[sensorIndex];
			}
			if(prevID!=null&&prevID==row.id) {
				log.error("duplicate timestamps: "+row.id+"      "+time[rowIndex]+"     "+ TimeUtil.oleMinutesToText(time[rowIndex]));
				return null;
			}
			if(prevID!=null&&prevID>row.id) {
				log.error("invalid timestamps: "+row.id+"      "+time[rowIndex]+"     "+ TimeUtil.oleMinutesToText(time[rowIndex]));
				return null;
			}
			rowIndex++;
			prevID = row.id;
		}

		return new UDBFTimestampSeries(filename, sensorHeaders, timeConverter, time, data);
	}

	@Deprecated
	public UDBFTimestampSeries getUDBFTimeSeries_OLD() {
		DataRow[] dataRows = readDataRows();

		int maxRowID = -1;

		for(int i=0;i<dataRows.length;i++) {
			int id = dataRows[i].id;
			if(id>=0&&id<=MAX_VALID_ROW_ID) {
				if(maxRowID<id) {
					maxRowID = id; 
				}
			}
		}		

		DataRow[] tempRows = new DataRow[maxRowID+1];

		//int badRowCounter = 0;
		//int idPos = -1;

		for(int r=0;r<dataRows.length;r++) {
			int id = dataRows[r].id;
			if(id>=0&&id<=MAX_VALID_ROW_ID) {
				tempRows[id] = dataRows[r];
			} else {
				//badRowCounter++;
			}
		}

		int rowCount=0;
		//int gapCount=0;
		for(int r=0;r<tempRows.length;r++) {
			if(tempRows[r]==null) {
				//gapCount++;
			} else {
				rowCount++;
			}
		}

		long[] time = new long[rowCount];
		float[][] data = new float[rowCount][];
		for(int rowIndex=0; rowIndex<rowCount; rowIndex++) {
			data[rowIndex] = new float[sensorHeaders.length];
		}
		int dataRowIndex = 0;
		for(int tempRowsIndex=0; tempRowsIndex<tempRows.length; tempRowsIndex++) {
			if(tempRows[tempRowsIndex]!=null) {
				for(int sensorIndex=0;sensorIndex<sensorHeaders.length;sensorIndex++) {
					data[dataRowIndex][sensorIndex] = tempRows[tempRowsIndex].data[sensorIndex];

				}
				time[dataRowIndex] =  timeConverter.getStartTimeOleMinutes()+(tempRows[tempRowsIndex].id*timeConverter.getTimeStepMinutes());
				dataRowIndex++;
			}
		}

		return new UDBFTimestampSeries(filename, sensorHeaders, timeConverter, time, data);
	}
}
