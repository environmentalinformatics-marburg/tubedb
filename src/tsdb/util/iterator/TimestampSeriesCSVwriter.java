package tsdb.util.iterator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.util.Locale;

import org.yaml.snakeyaml.DumperOptions.LineBreak;

import tsdb.iterator.ProjectionFillIterator;
import tsdb.util.AggregationInterval;
import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;

public class TimestampSeriesCSVwriter {
	
	/**
	 * Platform neutral line separator (windows style)
	 */
	protected static final String LINE_SEPARATOR = "\r\n";
	protected static final LineBreak LINE_BREAK = LineBreak.WIN; //"\r\n"
	
	protected final boolean col_plotid;
	protected final boolean col_timestamp;
	protected final boolean col_datetime;
	protected final boolean col_qualitycounter;

	
	private final boolean short_datetime = true;
	private final String missing_value_placeholder = "NA";
	
	private static final DecimalFormat decimalFormat = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.ENGLISH));
	
	public TimestampSeriesCSVwriter(boolean col_plotid, boolean col_timestamp, boolean col_datetime, boolean col_qualitycounter) {
		this.col_plotid = col_plotid;
		this.col_timestamp = col_timestamp;
		this.col_datetime = col_datetime;
		this.col_qualitycounter = col_qualitycounter;
	}
	
	protected void writeCSVHeader(BufferedWriter bufferedWriter, String[] sensorNames) throws IOException {
		boolean isFirst = true;
		if(col_plotid) {
			bufferedWriter.write("plotID");
			isFirst = false;
		}

		if(col_timestamp) {
			if(!isFirst) {
				bufferedWriter.write(',');				
			}
			bufferedWriter.write("timestamp");
			isFirst = false;
		}

		if(col_datetime) {
			if(!isFirst) {
				bufferedWriter.write(',');			
			}
			bufferedWriter.write("datetime");
			isFirst = false;
		}
		for(String name:sensorNames) {
			if(!isFirst) {
				bufferedWriter.write(',');
			}
			bufferedWriter.write(name);
			isFirst = false;
		}
		if(col_qualitycounter) {
			if(!isFirst) {
				bufferedWriter.write(',');				
			}
			bufferedWriter.write("qualitycounter");
			isFirst = false;
		}
		bufferedWriter.write(LINE_SEPARATOR);
	}
	
	protected void writeTimeseries(TimestampSeries timeseries, String plotID, String[] sensorNames, AggregationInterval aggregationInterval, BufferedWriter bufferedWriter) throws IOException {
		
		AggregationInterval datetimeFormat = short_datetime?aggregationInterval:AggregationInterval.RAW;
		
		char[] missingValueChars = missing_value_placeholder.toCharArray();
		
		//@SuppressWarnings("resource") //don't close stream
		//Formatter formatter = new Formatter(bufferedWriter,Locale.ENGLISH); 
		ProjectionFillIterator it = new ProjectionFillIterator(timeseries.tsIterator(), sensorNames);
		while(it.hasNext()) {
			TsEntry entry = it.next();
			boolean isFirst = true;
			if(col_plotid) {
				bufferedWriter.write(plotID);
				isFirst = false;
			}
			if(col_timestamp) {
				if(!isFirst) {
					bufferedWriter.write(',');
				}
				bufferedWriter.write(Integer.toString((int) entry.timestamp));
				isFirst = false;
			}
			if(col_datetime) {
				if(!isFirst) {
					bufferedWriter.write(',');
				}
				LocalDateTime datetime = TimeUtil.oleMinutesToLocalDateTime(entry.timestamp);				
				switch(datetimeFormat) {
				case YEAR:
					bufferedWriter.write(TimeUtil.fastDateWriteYears(datetime.toLocalDate()));
					break;
				case MONTH:
					bufferedWriter.write(TimeUtil.fastDateWriteMonths(datetime.toLocalDate()));
					break;
				case WEEK:
					bufferedWriter.write(TimeUtil.fastDateWriteWeeks(datetime.toLocalDate()));
					break;
				case DAY:
					bufferedWriter.write(TimeUtil.fastDateWrite(datetime.toLocalDate()));
					break;
				case HOUR:
					bufferedWriter.write(TimeUtil.fastDateTimeWriteHours(datetime));
					break;
				default:
					//bufferedWriter.write(datetime.toString());
					bufferedWriter.write(TimeUtil.fastDateTimeWrite(datetime));	
				}				
				isFirst = false;
			}
			for(int i=0;i<sensorNames.length;i++) {
				float v = entry.data[i];
				if(!isFirst) {
					bufferedWriter.write(',');
				}
				isFirst = false;				
				if(Float.isNaN(v)) {
					bufferedWriter.write(missingValueChars);
				} else {
					//formatter.format(Locale.ENGLISH, "%.2f", v);
					bufferedWriter.write(decimalFormat.format(v));
					//bufferedWriter.write(Util.fastWriteFloat(v));
				}
			}
			if(col_qualitycounter) {
				if(!isFirst) {
					bufferedWriter.write(',');
				}
				bufferedWriter.write(entry.qualityCountersToString());
				isFirst = false;
			}
			bufferedWriter.write(LINE_SEPARATOR);	
		}		
	}
}
