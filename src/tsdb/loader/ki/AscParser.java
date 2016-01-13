package tsdb.loader.ki;

import java.io.BufferedReader;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;
import tsdb.util.iterator.TimestampSeries;

/**
 * parses ".asc"-files
 * @author woellauer
 *
 */
public class AscParser {

	private static final Logger log = LogManager.getLogger();

	private static final DateTimeFormatter dateFormate = new DateTimeFormatterBuilder().append(DateTimeFormatter.ofPattern("dd.MM.yy")).toFormatter();
	private static final DateTimeFormatter timeFormater = new DateTimeFormatterBuilder().append(DateTimeFormatter.ofPattern("HH:mm:00")).toFormatter();

	public static TimestampSeries parse(Path filename) throws IOException {
		return parse(filename,false);
	}
	
	public static TimestampSeries parse(Path filename, boolean ignoreSeconds) throws IOException {
		BufferedReader bufferedReader = Files.newBufferedReader(filename,Charset.forName("windows-1252"));
		final String[] lines = bufferedReader.lines().toArray(String[]::new);
		bufferedReader.close();

		String serial = null;
		int currentLineIndex = 0;		
		while(currentLineIndex<lines.length) {  // search serial
			String currentLine = lines[currentLineIndex++];
			if(currentLine.startsWith("Serialnumber")||currentLine.startsWith("Logger Seriennummer")) {
				int valueStartIndex = currentLine.indexOf(":")+1;
				if(valueStartIndex<1&&valueStartIndex<currentLine.length()) {
					log.warn("could not get serialnumber: "+currentLine);
					break;
				}
				serial = currentLine.substring(valueStartIndex).trim();
				try {
					serial = Long.toString(Long.parseUnsignedLong(serial));
				} catch (Exception e) {
					log.warn("could not long parse serialnumber: "+serial);
				}
				break;
			}
		}

		if(serial==null) {
			log.warn("no serial found: "+filename);
			return null;
		}

		String[] header = null;
		while(currentLineIndex<lines.length) {  // search header
			String currentLine = lines[currentLineIndex++];
			if(currentLine.startsWith("Date	Time")||currentLine.startsWith("Datum	Zeit")||currentLine.startsWith("Date;Time;")||currentLine.startsWith("Date  Time")) {
				header = currentLine.split("(\\s|;)+");
				break;
			}
		}

		if(header==null) {
			log.warn("no header found: "+filename);
			return null;
		}


		ArrayList<String> tempHeader = new ArrayList<String>();
		String prev = null;
		for(int i=2;i<header.length;i++) {
			if(header[i].startsWith("[")) {
				if(prev!=null) {
					tempHeader.add(prev);
					prev=null;
				}
			} else {
				if(prev==null) {
					prev = header[i];
				} else {
					prev += ' '+header[i];
				}
			}
		}
		String[] sensorNames = tempHeader.toArray(new String[0]);

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
					//log.info("empty line : "+currentLineIndex+" in "+filename);
					continue;
				}

				if(columns.length!=sensorNames.length+2) {					
					String endoffile = currentLineIndex==lines.length?"at end of file":"at line within file";					
					log.warn("different column count "+endoffile+" : "+currentLine+"  in "+filename);
					break rowLoop;
				}

				LocalDate date = LocalDate.parse(columns[0], dateFormate);
				String timeText = columns[1];
				if(ignoreSeconds) {
					timeText = timeText.substring(0, timeText.length()-2)+"00";
				}				
				LocalTime time = LocalTime.parse(timeText, timeFormater);

				LocalDateTime datetime = LocalDateTime.of(date, time);

				float[] data = new float[sensorNames.length];
				for(int i=0;i<sensorNames.length;i++) {
					try {
						data[i] = Float.parseFloat(columns[i+2]);
					} catch (Exception e) {
						log.warn(e+" in "+filename);
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
							log.warn("skip row: timestamp==prevTimestamp  "+filename+"  "+TimeUtil.oleMinutesToText(prevTimestamp, timestamp));
						}
					} else {
						log.warn("skip row: timestamp==prevTimestamp  "+filename+"  "+TimeUtil.oleMinutesToText(prevTimestamp, timestamp));
					}
				} else {
					log.warn("skip row: timestamp<prevTimestamp  "+filename+"  "+TimeUtil.oleMinutesToText(prevTimestamp, timestamp));
				}
			}
			return new TimestampSeries(serial, sensorNames, resultList);
		} catch (Exception e) {
			log.error(e+"  "+filename);
			return null;
		}
	}
}
