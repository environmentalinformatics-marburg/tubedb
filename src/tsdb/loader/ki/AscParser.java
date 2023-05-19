package tsdb.loader.ki;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashSet;


import org.tinylog.Logger;

import ch.randelshofer.fastdoubleparser.JavaFloatParser;
import tsdb.util.Pair;
import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;
import tsdb.util.iterator.TimestampSeries;

/**
 * parses ".asc"-files
 * @author woellauer
 *
 */
public class AscParser {
	
	
	private static final Charset ASC_CHARSET = Charset.forName("windows-1252");

	private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().append(DateTimeFormatter.ofPattern("dd.MM.yy")).toFormatter();
	private static final DateTimeFormatter TIME_FORMATTER_FULL_MINUTES = new DateTimeFormatterBuilder().append(DateTimeFormatter.ofPattern("HH:mm:00")).toFormatter();
	private static final DateTimeFormatter TIME_FORMATTER_SKIP_SECONDS = new DateTimeFormatterBuilder().append(DateTimeFormatter.ofPattern("HH:mm:ss")).toFormatter();

	public static TimestampSeries parse(Path filename) throws IOException {
		return parse(filename,false);
	}

	public static TimestampSeries parse(Path filename, boolean ignoreSeconds) throws IOException {
		DateTimeFormatter timeFormatter = ignoreSeconds ? TIME_FORMATTER_SKIP_SECONDS : TIME_FORMATTER_FULL_MINUTES;
		
		final String[] lines = Files.readAllLines(filename, ASC_CHARSET).toArray(new String[0]);

		String serial = null;
		int currentLineIndex = 0;		
		while(currentLineIndex<lines.length) {  // search serial
			String currentLine = lines[currentLineIndex++];
			if(currentLine.startsWith("Serialnumber")||currentLine.startsWith("Logger Seriennummer")) {
				int valueStartIndex = currentLine.indexOf(":")+1;
				if(valueStartIndex<1&&valueStartIndex<currentLine.length()) {
					Logger.warn("could not get serialnumber: "+currentLine);
					break;
				}
				serial = currentLine.substring(valueStartIndex).trim();
				try {
					serial = Long.toString(Long.parseUnsignedLong(serial));
				} catch (Exception e) {
					Logger.warn("could not long parse serialnumber: "+serial);
				}
				break;
			}
		}

		if(serial==null) {
			Logger.warn("no serial found: "+filename);
			return null;
		}

		String[] sensorNames = null;
		String[] sensorUnits = null;
		while(currentLineIndex<lines.length) {  // search header
			String currentLine = lines[currentLineIndex++];
			if(currentLine.startsWith("Date")||currentLine.startsWith("Datum")) {
				Pair<String[], String[]> pair = parseSensornames(currentLine);
				sensorNames = pair.a;
				sensorUnits = pair.b;				
				break;
			}
		}
		if(sensorNames==null) {
			Logger.warn("no sensornames found: "+filename);
			return null;
		}

		sensorNames = correctSensorNames(sensorNames, sensorUnits);
		//Logger.info(Arrays.toString(sensorNames));
		//Logger.info(Arrays.toString(sensorUnits));

		try {
			ArrayList<TsEntry> resultList = new ArrayList<TsEntry>(lines.length-currentLineIndex);
			long prevTimestamp = 0;
			boolean entrySkipped = false;
			rowLoop: while(currentLineIndex<lines.length) {
				String currentLine = lines[currentLineIndex++];

				while(currentLine.startsWith("RUN")) {
					if(!(currentLineIndex<lines.length)) {
						break rowLoop;
					}
					currentLine = lines[currentLineIndex++];
					while(!currentLine.startsWith("---")) {
						if(!(currentLineIndex<lines.length)) {
							break rowLoop;
						}
						currentLine = lines[currentLineIndex++];
					}
					if(!(currentLineIndex<lines.length)) {
						break rowLoop;
					}
					currentLine = lines[currentLineIndex++];
				}

				while(currentLine.startsWith("...Start")) {
					if(!(currentLineIndex<lines.length)) {
						break rowLoop;
					}
					currentLine = lines[currentLineIndex++];
					while(!currentLine.startsWith("...End")) {
						if(!(currentLineIndex<lines.length)) {
							break rowLoop;
						}
						currentLine = lines[currentLineIndex++];
					}
					if(!(currentLineIndex<lines.length)) {
						break rowLoop;
					}
					currentLine = lines[currentLineIndex++];
				}

				while(currentLine.isEmpty()) {
					if(!(currentLineIndex<lines.length)) {
						break rowLoop;
					}
					currentLine = lines[currentLineIndex++];	
				}

				String[] columns = currentLine.split("(\\s|;)+");

				if(columns.length==0) {
					//Logger.info("empty line : "+currentLineIndex+" in "+filename);
					continue;
				}

				if(columns.length!=sensorNames.length+2) {					
					String endoffile = currentLineIndex==lines.length?"at end of file":"at line within file";					
					Logger.warn("different column count "+endoffile+" : "+currentLine+"  in "+filename+"   header columns: "+(sensorNames.length+2)+"  row colunmns:"+columns.length);
					break rowLoop;
				}

				LocalDate date = LocalDate.parse(columns[0], DATE_FORMATTER);
				
				String timeText = columns[1];
				/*if(ignoreSeconds) {
					timeText = timeText.substring(0, timeText.length()-2)+"00";
				}				
				LocalTime time = LocalTime.parse(timeText, TIME_FORMATTER_FULL_MINUTES);*/
				LocalTime time = LocalTime.parse(timeText, timeFormatter);

				LocalDateTime datetime = LocalDateTime.of(date, time);

				float[] data = new float[sensorNames.length];
				for(int i=0;i<sensorNames.length;i++) {
					try {
						//data[i] = Float.parseFloat(columns[i+2]);
						data[i] = JavaFloatParser.parseFloat(columns[i+2]);
					} catch (Exception e) {
						Logger.warn(e+" in "+filename);
						data[i] = Float.NaN;
					}
				}

				long timestamp = TimeUtil.dateTimeToOleMinutes(datetime);
				if(prevTimestamp<timestamp) {
					resultList.add(new TsEntry(timestamp, data));
					prevTimestamp = timestamp;
				} else if(prevTimestamp==timestamp){
					if(ignoreSeconds) {
						if(!entrySkipped) {
							entrySkipped = true;
							Logger.warn("skip row: timestamp==prevTimestamp  "+filename+"  "+TimeUtil.oleMinutesToText(prevTimestamp, timestamp));
						}
					} else {
						Logger.warn("skip row: timestamp==prevTimestamp  "+filename+"  "+TimeUtil.oleMinutesToText(prevTimestamp, timestamp));
					}
				} else {
					Logger.warn("skip row: timestamp<prevTimestamp  "+filename+"  "+TimeUtil.oleMinutesToText(prevTimestamp, timestamp));
				}
			}
			return new TimestampSeries(serial, sensorNames, resultList);
		} catch (Exception e) {
			Logger.error(e+"  "+filename);
			return null;
		}
	}

	private static HashSet<String> dateColumnNames; 
	private static HashSet<String> timeColumnNames;
	static {
		dateColumnNames =  new HashSet<>();
		dateColumnNames.add("Date");
		dateColumnNames.add("Datum");

		timeColumnNames =  new HashSet<>();
		timeColumnNames.add("Time");
		timeColumnNames.add("Zeit");
	}

	private static Pair<String[],String[]> parseSensornames(String headerText) {
		String[] header = headerText.split("\t|;");
		String[] headerUnit = new String[header.length];
		for (int i=0; i<header.length; i++) {
			String sensorName = header[i];
			int unitIndexStart = sensorName.indexOf('[');
			int unitIndexEnd = sensorName.indexOf(']');
			if(unitIndexStart>=0) {
				if(unitIndexEnd>=0) {
					headerUnit[i] = sensorName.substring(unitIndexStart+1, unitIndexEnd).trim();
				}
				sensorName = sensorName.substring(0, unitIndexStart);
			}
			header[i] = sensorName.trim();			
		}

		String[] sensorNames = new String[header.length-2];
		String[] sensorUnits = new String[header.length-2];

		if( !(dateColumnNames.contains(header[0]) && timeColumnNames.contains(header[1])) ) {
			throw new RuntimeException("date time columns not found: "+header[0]+" | "+header[1]);
		}


		for (int i=2; i<header.length; i++) {
			sensorUnits[i-2] = headerUnit[i];
			sensorNames[i-2] = header[i];
		}

		return Pair.of(sensorNames, sensorUnits);
	}

	/**
	 * Add unit name to sensorname that may contain more than one unit. eg. Temperature �C and K
	 * @param sensorNames
	 * @param sensorUnits
	 * @return
	 */
	private static String[] correctSensorNames(String[] sensorNames, String[] sensorUnits) {
		String[] resultNames = new String[sensorNames.length];
		for (int i = 0; i < sensorNames.length; i++) {
			if(sensorNames[i].equals("Temperature") && sensorUnits[i].equals("K")) {
				resultNames[i] = sensorNames[i]+"["+sensorUnits[i]+"]";
			} else {
				resultNames[i] = sensorNames[i];
			}
		}
		return resultNames;
	}
}
