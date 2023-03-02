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
	public static final String LINE_SEPARATOR = "\r\n";
	protected static final LineBreak LINE_BREAK = LineBreak.WIN; //"\r\n"
	
	protected final boolean col_plotid;
	protected final boolean col_timestamp;
	protected final boolean col_datetime;
	protected final boolean col_year;
	protected final boolean col_month;
	protected final boolean col_day;
	protected final boolean col_hour;
	protected final boolean col_day_of_year;
	protected final boolean col_qualitycounter;

	
	private final boolean short_datetime = true;
	private final String missing_value_placeholder = "NA";
	
	//private static final DecimalFormat decimalFormat2 = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.ENGLISH));
	public static final DecimalFormat DECIMAL_FORMAT_5 = new DecimalFormat("0.00000", new DecimalFormatSymbols(Locale.ENGLISH));
	
	public TimestampSeriesCSVwriter(boolean col_plotid, boolean col_timestamp, boolean col_datetime, boolean col_qualitycounter, boolean col_year, boolean col_month, boolean col_day, boolean col_hour, boolean col_day_of_year) {
		this.col_plotid = col_plotid;
		this.col_timestamp = col_timestamp;
		this.col_datetime = col_datetime;
		this.col_qualitycounter = col_qualitycounter;
		this.col_year = col_year;
		this.col_month = col_month;
		this.col_day = col_day;
		this.col_hour = col_hour;
		this.col_day_of_year = col_day_of_year;
	}
	
	protected void writeCSVHeader(BufferedWriter bufferedWriter, String[] sensorNames, boolean withPlotID) throws IOException {
		boolean isFirst = true;
		if(withPlotID) {
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
		
		if(col_year) {
			if(!isFirst) {
				bufferedWriter.write(',');			
			}
			bufferedWriter.write("year");
			isFirst = false;
		}
		
		if(col_month) {
			if(!isFirst) {
				bufferedWriter.write(',');			
			}
			bufferedWriter.write("month");
			isFirst = false;
		}
		
		if(col_day) {
			if(!isFirst) {
				bufferedWriter.write(',');			
			}
			bufferedWriter.write("day");
			isFirst = false;
		}
		
		if(col_hour) {
			if(!isFirst) {
				bufferedWriter.write(',');			
			}
			bufferedWriter.write("hour");
			isFirst = false;
		}
		
		if(col_day_of_year) {
			if(!isFirst) {
				bufferedWriter.write(',');			
			}
			bufferedWriter.write("day_of_year");
			isFirst = false;
		}
		
		for(String name : sensorNames) {
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
	
	protected void writeTimeseries(TimestampSeries timeseries, String plotID, String[] sensorNames, AggregationInterval aggregationInterval, BufferedWriter bufferedWriter, boolean withPlotID) throws IOException {
		
		AggregationInterval datetimeFormat = short_datetime?aggregationInterval:AggregationInterval.RAW;
		
		char[] missingValueChars = missing_value_placeholder.toCharArray();
		
		//@SuppressWarnings("resource") //don't close stream
		//Formatter formatter = new Formatter(bufferedWriter,Locale.ENGLISH); 
		ProjectionFillIterator it = new ProjectionFillIterator(timeseries.tsIterator(), sensorNames);
		while(it.hasNext()) {
			TsEntry entry = it.next();
			boolean isFirst = true;
			if(withPlotID) {
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
			LocalDateTime datetime = TimeUtil.oleMinutesToLocalDateTime(entry.timestamp);
			if(col_datetime) {
				if(!isFirst) {
					bufferedWriter.write(',');
				}				
				bufferedWriter.write(TimeUtil.fastTimestampWrite(datetime, datetimeFormat));
				isFirst = false;
			}
			if(col_year) {
				if(!isFirst) {
					bufferedWriter.write(',');
				}				
				bufferedWriter.write(Integer.toString(datetime.getYear()));
				isFirst = false;
			}
			if(col_month) {
				if(!isFirst) {
					bufferedWriter.write(',');
				}				
				bufferedWriter.write(Integer.toString(datetime.getMonthValue()));
				isFirst = false;
			}
			if(col_day) {
				if(!isFirst) {
					bufferedWriter.write(',');
				}				
				bufferedWriter.write(Integer.toString(datetime.getDayOfMonth()));
				isFirst = false;
			}
			if(col_hour) {
				if(!isFirst) {
					bufferedWriter.write(',');
				}				
				bufferedWriter.write(Integer.toString(datetime.getHour()));
				isFirst = false;
			}
			if(col_day_of_year) {
				if(!isFirst) {
					bufferedWriter.write(',');
				}				
				bufferedWriter.write(Integer.toString(datetime.getDayOfYear()));
				isFirst = false;
			}			
			for(int i = 0; i < sensorNames.length; i++) {
				float v = entry.data[i];
				if(!isFirst) {
					bufferedWriter.write(',');
				}
				isFirst = false;				
				if(Float.isNaN(v)) {
					bufferedWriter.write(missingValueChars);
				} else {
					//formatter.format(Locale.ENGLISH, "%.2f", v);
					//bufferedWriter.write(Util.fastWriteFloat(v));
					//bufferedWriter.write(decimalFormat2.format(v));
					bufferedWriter.write(DECIMAL_FORMAT_5.format(v));
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
