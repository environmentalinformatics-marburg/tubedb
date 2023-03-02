package tsdb.util.iterator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import tsdb.util.AggregationInterval;
import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;

/**
 * Writes time series data to CSV file with some format options.
 * @author woellauer
 *
 */
public class CSV {	

	private static final String NEW_LINE = "\r\n"; // windows new line

	public static void writeNoHeader(TsIterator it, String filename, char separator, String nanText, CSVTimeType csvTimeType, AggregationInterval datetimeFormat) {
		write(it, false, filename, separator, nanText, csvTimeType, false, false, datetimeFormat);
	}


	public static void write(TsIterable input, String filename, char separator, String nanText, CSVTimeType csvTimeType, AggregationInterval datetimeFormat) {
		write(input, filename, separator, nanText, csvTimeType, false, datetimeFormat);
	}

	public static void write(TsIterable input, String filename, AggregationInterval datetimeFormat) {
		write(input, filename, ',', "NA", CSVTimeType.TIMESTAMP_AND_DATETIME, datetimeFormat);
	}

	public static void write(TsIterable input, String filename, char separator, String nanText, CSVTimeType csvTimeType, boolean qualityFlag, AggregationInterval datetimeFormat) {
		write(input.tsIterator(), filename, separator, nanText, csvTimeType, qualityFlag, false, datetimeFormat);
	}

	public static void write(TsIterator it, String filename, AggregationInterval datetimeFormat) {
		write(it, filename, ',', "NA", CSVTimeType.TIMESTAMP_AND_DATETIME, datetimeFormat);
	}

	public static void write(TsIterator it, String filename, char separator, String nanText, CSVTimeType csvTimeType, AggregationInterval datetimeFormat) {
		write(it, filename, separator, nanText, csvTimeType, false, false, datetimeFormat);
	}

	public static void write(TsIterator it, String filename, char separator, String nanText, CSVTimeType csvTimeType, boolean qualityFlag, boolean qualityCounter, AggregationInterval datetimeFormat) {
		write(it, true, filename, separator, nanText, csvTimeType, qualityFlag, qualityCounter, datetimeFormat);
	}

	public static void write(TsIterator it, boolean header, String filename, char separator, String nanText, CSVTimeType csvTimeType, boolean qualityFlag, boolean qualityCounter, AggregationInterval datetimeFormat) {
		//try(PrintWriter out = new PrintWriter(filename, StandardCharsets.UTF_8)) {
		//FileOutputStream out = new FileOutputStream(filename);		
		try(FileWriter fileWriter = new FileWriter(new File(filename), StandardCharsets.UTF_8)) {
			try(BufferedWriter bufferedWriter = new BufferedWriter(fileWriter)) {
				write(it, header, bufferedWriter, separator, nanText, csvTimeType, qualityFlag, qualityCounter, datetimeFormat, null);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Write iterator to CSV
	 * @param it
	 * @param header
	 * @param out
	 * @param separator
	 * @param nanText
	 * @param csvTimeType
	 * @param qualityFlag
	 * @param qualityCounter
	 * @param datetimeFormat
	 * @param plotLabel nullable, if present write column plot
	 * @throws IOException 
	 */
	public static void write(TsIterator it, boolean header, BufferedWriter out, char separator, String nanText, CSVTimeType csvTimeType, boolean qualityFlag, boolean qualityCounter, AggregationInterval datetimeFormat, String plotLabel) throws IOException {		
		boolean writePlot = plotLabel != null;
		String plotWithSeperator = writePlot ? plotLabel + separator : "";

		boolean time=false;
		if(csvTimeType==CSVTimeType.TIMESTAMP
				||csvTimeType==CSVTimeType.DATETIME
				||csvTimeType==CSVTimeType.TIMESTAMP_AND_DATETIME
				||csvTimeType==CSVTimeType.CUSTOM) {
			time=true;
		}

		if(header) {

			if(writePlot) {
				out.write("plot");
				out.write(separator); // write character
			}

			if(time) {
				switch(csvTimeType) {
				case TIMESTAMP:
					out.write("timestamp");
					break;
				case DATETIME:
					out.write("datetime");
					break;
				case TIMESTAMP_AND_DATETIME:
					out.write("timestamp");
					out.write(separator); // write character
					out.write("datetime");
					break;
				case CUSTOM:
					out.write("custom");
					break;
				default:
					out.write("???");
				}
			}

			String[] sensorNames = it.getNames();

			for(int i = 0; i < sensorNames.length; i++) {
				if(time || i > 0) {
					out.write(separator); // write character
				}
				out.write(sensorNames[i]);				
			}

			if(qualityFlag) {
				out.write(separator); // write character
				out.write("qualityflag");	
			}

			if(qualityCounter) {
				out.write(separator); // write character
				out.write("qualityCounter");	
			}


			out.write(NEW_LINE); // windows new line

		}

		while(it.hasNext()) {

			TsEntry entry = it.next();	
			long timestamp = entry.timestamp;
			float[] data = entry.data;

			if(writePlot) {
				out.write(plotWithSeperator);
			}

			if(time) {
				switch(csvTimeType) {
				case TIMESTAMP:
					out.write(Integer.toString((int) timestamp));
					break;
				case DATETIME:
					//printStream.print(TimeUtil.oleMinutesToLocalDateTime(timestamp));
					out.write(TimeUtil.fastTimestampWrite(timestamp, datetimeFormat));
					break;
				case TIMESTAMP_AND_DATETIME:
					out.write(Integer.toString((int) timestamp));
					out.write(separator); // write character
					//printStream.print(TimeUtil.oleMinutesToLocalDateTime(timestamp));
					out.write(TimeUtil.fastTimestampWrite(timestamp, datetimeFormat));
					break;
				case CUSTOM:
					out.write(TimeUtil.fastTimestampWrite_custom(timestamp, datetimeFormat));
					break;
				default:
					out.write("---");
				}
			}
			for(int columnIndex=0;columnIndex<data.length;columnIndex++) {
				float value = data[columnIndex];
				if(time||columnIndex>0) {
					out.write(separator); // write character
				}
				if(Float.isNaN(value)) {
					out.write(nanText);
				} else {
					//s+=Util.floatToString(entry.data[i]);
					//printStream.format(Locale.ENGLISH,"%3.3f", value);
					out.write(TimestampSeriesCSVwriter.DECIMAL_FORMAT_5.format(value));
				}
			}

			if(qualityFlag) {
				out.write(separator); // write character

				if(entry.qualityFlag==null) {
					out.write(nanText);
				} else {
					for(int qIndex=0; qIndex<entry.qualityFlag.length; qIndex++) {
						switch(entry.qualityFlag[qIndex]) {
						case Na:
							out.write('n'); // write character
							break;
						case NO:
							out.write('0'); // write character
							break;
						case PHYSICAL:
							out.write('1'); // write character
							break;
						case STEP:
							out.write('2'); // write character
							break;
						case EMPIRICAL:
							out.write('3'); // write character
							break;
						default:
							out.write('?'); // write character
						}
					}
				}
			}

			if(qualityCounter) {
				out.write(separator); // write character
				if(entry.qualityCounter==null) {
					out.write(nanText);
				} else {
					int[][] counter = entry.qualityCounter;
					for(int c=0;c<counter.length;c++) {
						if(c>0) {
							out.write('-');  // write character
						}
						for(int q=0;q<counter[c].length;q++) {
							if(q>0) {
								out.write('_');  // write character
							}
							out.write(Integer.toString(counter[c][q]));
						}
					}
				}
			}
			out.write(NEW_LINE); // windows new line
		}
	}
}
