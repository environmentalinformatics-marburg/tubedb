package tsdb.util.iterator;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.AggregationInterval;
import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;

/**
 * Writes time series data to CSV file with some format options.
 * @author woellauer
 *
 */
public class CSV {	
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger();
	
	private static final String NEW_LINE = "\r\n"; // windows new line

	public static void writeNoHeader(TsIterator it, String filename, String separator, String nanText, CSVTimeType csvTimeType, AggregationInterval datetimeFormat) {
		write(it, false, filename, separator, nanText, csvTimeType, false, false, datetimeFormat);
	}


	public static void write(TsIterable input, String filename, String separator, String nanText, CSVTimeType csvTimeType, AggregationInterval datetimeFormat) {
		write(input, filename, separator, nanText, csvTimeType, false, datetimeFormat);
	}
	
	public static void write(TsIterable input, String filename, AggregationInterval datetimeFormat) {
		write(input, filename, ",", "NA", CSVTimeType.TIMESTAMP_AND_DATETIME, datetimeFormat);
	}

	public static void write(TsIterable input, String filename, String separator, String nanText, CSVTimeType csvTimeType, boolean qualityFlag, AggregationInterval datetimeFormat) {
		write(input.tsIterator(), filename, separator, nanText, csvTimeType, qualityFlag, false, datetimeFormat);
	}
	
	public static void write(TsIterator it, String filename, AggregationInterval datetimeFormat) {
		write(it, filename, ",", "NA", CSVTimeType.TIMESTAMP_AND_DATETIME, datetimeFormat);
	}

	public static void write(TsIterator it, String filename, String separator, String nanText, CSVTimeType csvTimeType, AggregationInterval datetimeFormat) {
		write(it, filename, separator, nanText, csvTimeType, false, false, datetimeFormat);
	}

	public static void write(TsIterator it, String filename, String separator, String nanText, CSVTimeType csvTimeType, boolean qualityFlag, boolean qualityCounter, AggregationInterval datetimeFormat) {
		write(it, true, filename, separator, nanText, csvTimeType, qualityFlag, qualityCounter, datetimeFormat);
	}
	
	public static void write(TsIterator it, boolean header, String filename, String separator, String nanText, CSVTimeType csvTimeType, boolean qualityFlag, boolean qualityCounter, AggregationInterval datetimeFormat) {
		try {
			FileOutputStream out = new FileOutputStream(filename);
			write(it, header, out, separator, nanText, csvTimeType, qualityFlag, qualityCounter, datetimeFormat, null);
		} catch (FileNotFoundException e) {
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
	 */
	public static void write(TsIterator it, boolean header, OutputStream out, String separator, String nanText, CSVTimeType csvTimeType, boolean qualityFlag, boolean qualityCounter, AggregationInterval datetimeFormat, String plotLabel) {		
		boolean writePlot = plotLabel != null;
		String plotWithSeperator = writePlot ? plotLabel + separator : "";
		
		boolean time=false;
		if(csvTimeType==CSVTimeType.TIMESTAMP||csvTimeType==CSVTimeType.DATETIME||csvTimeType==CSVTimeType.TIMESTAMP_AND_DATETIME) {
			time=true;
		}

			PrintStream printStream = new PrintStream(out,true);

			if(header) {
				
				if(writePlot) {
					printStream.print("plot");
					printStream.print(separator);
				}

				if(time) {
					switch(csvTimeType) {
					case TIMESTAMP:
						printStream.print("timestamp");
						break;
					case DATETIME:
						printStream.print("datetime");
						break;
					case TIMESTAMP_AND_DATETIME:
						printStream.print("timestamp");
						printStream.print(separator);
						printStream.print("datetime");
						break;
					default:
						printStream.print("???");
					}
				}

				String[] sensorNames = it.getNames();

				for(int i=0;i<sensorNames.length;i++) {
					if(time||i>0) {
						printStream.print(separator);
					}
					printStream.print(sensorNames[i]);				
				}

				if(qualityFlag) {
					printStream.print(separator);
					printStream.print("qualityflag");	
				}

				if(qualityCounter) {
					printStream.print(separator);
					printStream.print("qualityCounter");	
				}


				printStream.print(NEW_LINE); // windows new line

			}

			while(it.hasNext()) {

				TsEntry entry = it.next();	
				long timestamp = entry.timestamp;
				float[] data = entry.data;
				
				if(writePlot) {
					printStream.print(plotWithSeperator);
				}

				if(time) {
					switch(csvTimeType) {
					case TIMESTAMP:
						printStream.print(timestamp);
						break;
					case DATETIME:
						//printStream.print(TimeUtil.oleMinutesToLocalDateTime(timestamp));
						printStream.print(TimeUtil.fastTimestampWrite(timestamp, datetimeFormat));
						break;
					case TIMESTAMP_AND_DATETIME:
						printStream.print(timestamp);
						printStream.print(separator);
						//printStream.print(TimeUtil.oleMinutesToLocalDateTime(timestamp));
						printStream.print(TimeUtil.fastTimestampWrite(timestamp, datetimeFormat));
						break;
					default:
						printStream.print("---");
					}
				}
				for(int columnIndex=0;columnIndex<data.length;columnIndex++) {
					float value = data[columnIndex];
					if(time||columnIndex>0) {
						printStream.print(separator);
					}
					if(Float.isNaN(value)) {
						printStream.print(nanText);
					} else {
						//s+=Util.floatToString(entry.data[i]);
						printStream.format(Locale.ENGLISH,"%3.3f", value);
					}
				}

				if(qualityFlag) {
					printStream.print(separator);

					if(entry.qualityFlag==null) {
						printStream.print(nanText);
					} else {
						for(int qIndex=0; qIndex<entry.qualityFlag.length; qIndex++) {
							switch(entry.qualityFlag[qIndex]) {
							case Na:
								printStream.print('n');
								break;
							case NO:
								printStream.print(0);
								break;
							case PHYSICAL:
								printStream.print(1);
								break;
							case STEP:
								printStream.print(2);
								break;
							case EMPIRICAL:
								printStream.print(3);
								break;
							default:
								printStream.print('?');

							}
						}
					}



				}

				if(qualityCounter) {
					printStream.print(separator);
					if(entry.qualityCounter==null) {
						printStream.print(nanText);
					} else {
						int[][] counter = entry.qualityCounter;
						for(int c=0;c<counter.length;c++) {
							if(c>0) {
								printStream.print('-');
							}
							for(int q=0;q<counter[c].length;q++) {
								if(q>0) {
									printStream.print('_');
								}
								printStream.print(counter[c][q]);
							}
						}

					}


				}

				printStream.print(NEW_LINE); // windows new line
			}
			//printStream.close();
	}
}
