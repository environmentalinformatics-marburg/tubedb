package tsdb.remote;

import static tsdb.util.AssumptionCheck.throwNull;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.component.Region;
import tsdb.component.Sensor;
import tsdb.iterator.ProjectionFillIterator;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;
import tsdb.util.Util;
import tsdb.util.iterator.TimestampSeries;

/**
 * Creates Zip-files of sets of time series
 * @author woellauer
 *
 */
public class ZipExport {

	private static final Logger log = LogManager.getLogger();
	
	/**
	 * Platform neutral line separator (windows style)
	 */
	private static final String LINE_SEPARATOR = "\r\n";

	private final RemoteTsDB tsdb;

	private Consumer<String> cbPrintLine = null;

	private final Region region;
	private String[] sensorNames;
	private final String[] plotIDs;
	private final AggregationInterval aggregationInterval;
	private final DataQuality dataQuality;
	private final boolean interpolated;
	private final boolean allInOne;
	private final boolean desc_sensor;
	private final boolean desc_plot;
	private final boolean desc_settings;
	private final boolean col_plotid;
	private final boolean col_timestamp;
	private final boolean col_datetime;
	private final boolean col_qualitycounter;
	private final boolean write_header;
	private final Long startTimestamp;
	private final Long endTimestamp;

	private int processedPlots = 0;

	public ZipExport(RemoteTsDB tsdb, Region region, String[] sensorNames, String[] plotIDs,AggregationInterval aggregationInterval,DataQuality dataQuality,boolean interpolated, boolean allinone, boolean desc_sensor, boolean desc_plot, boolean desc_settings, boolean col_plotid, boolean col_timestamp, boolean col_datetime, boolean write_header, Long startTimestamp, Long endTimestamp, boolean col_qualitycounter) {
		throwNull(tsdb);
		this.tsdb = tsdb;

		this.region = region;
		if(aggregationInterval == AggregationInterval.RAW) {
			this.sensorNames = sensorNames;
		} else {
			ArrayList<String> sensorNameList = new ArrayList<String>();
			try {
				Sensor[] allSensors = tsdb.getSensors();
				if(allSensors!=null) {
					Map<String, Sensor> allSensorsMap = Arrays.stream(allSensors).collect(Collectors.toMap(Sensor::getName, Function.identity()));
					for(String sensorName:sensorNames) {
						if(allSensorsMap.containsKey(sensorName)) {
							if(allSensorsMap.get(sensorName).isAggregable()) {
								sensorNameList.add(sensorName);
							}
						}
					}
					this.sensorNames = sensorNameList.toArray(new String[0]);
				} else {
					this.sensorNames = sensorNames;
				}
			} catch (RemoteException e) {
				log.warn(e);
				this.sensorNames = sensorNames;
			}
		}

		this.plotIDs = plotIDs;
		this.aggregationInterval = aggregationInterval;
		this.dataQuality = dataQuality;
		this.interpolated = interpolated;
		this.allInOne = allinone;
		this.desc_sensor = desc_sensor;
		this.desc_plot = desc_plot;
		this.desc_settings = desc_settings;
		this.col_plotid = col_plotid;
		this.col_timestamp = col_timestamp;
		this.col_datetime = col_datetime;
		this.write_header = write_header;
		this.startTimestamp = startTimestamp;
		this.endTimestamp = endTimestamp;
		this.col_qualitycounter = col_qualitycounter;
	}

	public boolean createZipFile(String filename) {
		FileOutputStream fileOutputStream;
		try {
			printLine("create file: "+filename);
			fileOutputStream = new FileOutputStream(filename);
			boolean ret = writeToStream(fileOutputStream);
			fileOutputStream.close();
			printLine("...finished");
			return ret;
		} catch (IOException e) {
			log.error(e);
			return false;
		}

	}


	public boolean writeToStream(OutputStream outputstream) {
		printLine("start export...");
		printLine("");
		printLine("sensorNames       "+Util.arrayToString(sensorNames));
		if(Util.empty(sensorNames)) {
			return false;
		}
		if(Util.empty(plotIDs)) {
			return false;
		}
		printLine("plots "+plotIDs.length);
		printLine("");

		try {
			ZipOutputStream zipOutputStream = new ZipOutputStream(outputstream);
			zipOutputStream.setComment("time series archive file");
			zipOutputStream.setLevel(9);

			if(desc_settings) {
				zipOutputStream.putNextEntry(new ZipEntry("settings.txt"));
				PrintStream printStream = new PrintStream(zipOutputStream, false);
				write_settings(printStream);
				printStream.flush();
			}

			if(desc_sensor) {
				zipOutputStream.putNextEntry(new ZipEntry("sensor_description.txt"));
				PrintStream printStream = new PrintStream(zipOutputStream, false);
				write_sensor_description(printStream);
				printStream.flush();
			}

			if(desc_plot) {
				zipOutputStream.putNextEntry(new ZipEntry("plot_description.txt"));
				PrintStream printStream = new PrintStream(zipOutputStream, false);
				write_plot_description(printStream);
				printStream.flush();
			}

			if(allInOne) {				
				zipOutputStream.putNextEntry(new ZipEntry("plots.csv"));
				PrintStream csvOut = new PrintStream(zipOutputStream,false);
				if(write_header) {
					writeCSVHeader(csvOut);
				}
				processedPlots = 0;
				for(String plotID:plotIDs) {
					printLine("processing plot "+plotID);
					try {
						String[] schema = tsdb.getValidSchemaWithVirtualSensors(plotID, sensorNames);
						if(!Util.empty(schema)) {
							TimestampSeries timeseries = tsdb.plot(null,plotID, schema, aggregationInterval, dataQuality, interpolated, startTimestamp, endTimestamp);
							if(timeseries!=null) {								
								writeTimeseries(timeseries,plotID,csvOut);								
							} else {
								printLine("not processed: "+plotID);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						log.error(e);
						printLine("ERROR "+e);
					}
					processedPlots++;
				}
				csvOut.flush();				
			} else {
				processedPlots = 0;
				for(String plotID:plotIDs) {
					printLine("processing plot "+plotID);
					try {
						String[] schema = tsdb.getValidSchemaWithVirtualSensors(plotID, sensorNames);
						if(!Util.empty(schema)) {
							TimestampSeries timeseries = tsdb.plot(null,plotID, schema, aggregationInterval, dataQuality, interpolated, startTimestamp, endTimestamp);
							if(timeseries!=null) {
								zipOutputStream.putNextEntry(new ZipEntry(plotID+".csv"));
								PrintStream csvOut = new PrintStream(zipOutputStream,false);
								if(write_header) {
									writeCSVHeader(csvOut);
								}
								writeTimeseries(timeseries,plotID,csvOut);
								csvOut.flush();
							} else {
								printLine("not processed: "+plotID);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						log.error(e);
						printLine("ERROR "+e);
					}
					processedPlots++;
				}				
			}
			zipOutputStream.finish();
			printLine("");
			printLine("...finished");
			return true;
		} catch (IOException e) {
			log.warn(e);
			printLine("ERROR "+e);
			return false;
		}		
	}	

	private void write_settings(PrintStream printStream) {
		printStream.print("Settings that were used to create this time series archive file:"+LINE_SEPARATOR);
		printStream.print(LINE_SEPARATOR);
		printStream.print("creation date: "+LocalDateTime.now()+LINE_SEPARATOR);
		printStream.print(LINE_SEPARATOR);
		printStream.print("sensor names ("+sensorNames.length+") : "+Util.arrayToString(sensorNames)+LINE_SEPARATOR);
		printStream.print(LINE_SEPARATOR);
		printStream.print("plot names ("+plotIDs.length+") : "+Util.arrayToString(plotIDs)+LINE_SEPARATOR);
		printStream.print(LINE_SEPARATOR);
		printStream.print("time steps : "+aggregationInterval.getText()+LINE_SEPARATOR);
		printStream.print(LINE_SEPARATOR);
		printStream.print("quality checks : "+dataQuality.getText()+LINE_SEPARATOR);
		printStream.print(LINE_SEPARATOR);
		if(interpolated) {
			printStream.print("interpolate missing data"+LINE_SEPARATOR);
			printStream.print(LINE_SEPARATOR);
		} else {
			printStream.print("no interpolation used"+LINE_SEPARATOR);
			printStream.print(LINE_SEPARATOR);
		}
		if(allInOne) {
			printStream.print("write all plots into one CSV-File"+LINE_SEPARATOR);
			printStream.print(LINE_SEPARATOR);
		} else {
			printStream.print("for each plot write into separate CSV-File"+LINE_SEPARATOR);
			printStream.print(LINE_SEPARATOR);			
		}

	}

	public void setPrintCallback(Consumer<String> callback) {
		this.cbPrintLine = callback;
	}

	private void printLine(String s) {
		if(cbPrintLine!=null) {
			cbPrintLine.accept(s);
		}
	}

	private void writeCSVHeader(PrintStream csvOut) {
		StringBuilder stringbuilder = new StringBuilder();
		boolean isFirst = true;
		if(col_plotid) {
			stringbuilder.append("plotID");
			isFirst = false;
		}

		if(col_timestamp) {
			if(!isFirst) {
				stringbuilder.append(',');				
			}
			stringbuilder.append("timestamp");
			isFirst = false;
		}

		if(col_datetime) {
			if(!isFirst) {
				stringbuilder.append(',');				
			}
			stringbuilder.append("datetime");
			isFirst = false;
		}
		for(String name:sensorNames) {
			if(!isFirst) {
				stringbuilder.append(',');
			}
			stringbuilder.append(name);
			isFirst = false;
		}
		if(col_qualitycounter) {
			if(!isFirst) {
				stringbuilder.append(',');				
			}
			stringbuilder.append("qualitycounter");
			isFirst = false;
		}
		csvOut.print(stringbuilder+LINE_SEPARATOR);
	}

	private void write_sensor_description(PrintStream printStream) {
		printStream.print("sensors:\t"+sensorNames.length+LINE_SEPARATOR);
		printStream.print(LINE_SEPARATOR);
		for(int i=0;i<sensorNames.length;i++) {
			printStream.print((i+1)+". sensor:\t"+sensorNames[i]+LINE_SEPARATOR);
			try {
				Sensor sensor = tsdb.getSensor(sensorNames[i]);
				if(sensor!=null) {
					printStream.print("description:\t"+sensor.description+LINE_SEPARATOR);
					printStream.print("unit:\t\t"+sensor.unitDescription+LINE_SEPARATOR);
				}
			} catch (RemoteException e) {
				log.error(e);
			}
			printStream.print(LINE_SEPARATOR);
		}		
	}

	private void write_plot_description(PrintStream printStream) {
		printStream.print("plots:\t"+plotIDs.length+LINE_SEPARATOR);
		printStream.print("in region:\t"+region.longName+LINE_SEPARATOR);
		printStream.print(LINE_SEPARATOR);

		try {
			PlotInfo[] plotInfos = tsdb.getPlots();
			Map<String,PlotInfo> map = new HashMap<String,PlotInfo>();
			for(PlotInfo plotInfo:plotInfos) {
				map.put(plotInfo.name, plotInfo);
			}

			for(int i=0;i<plotIDs.length;i++) {
				printStream.print((i+1)+". plot:\t"+plotIDs[i]+LINE_SEPARATOR);

				PlotInfo plotInfo = map.get(plotIDs[i]);
				if(plotInfo!=null) {
					printStream.print("category:\t"+plotInfo.generalStationInfo.longName+LINE_SEPARATOR);
					//printStream.print("position:\t"+Util.arrayToString(plotInfo.geoPos)+LINE_SEPARATOR);
					if(Double.isFinite(plotInfo.geoPosLatitude)) {
						printStream.print("Latitude:\t"+plotInfo.geoPosLatitude+LINE_SEPARATOR);
					}
					if(Double.isFinite(plotInfo.geoPosLongitude)) {
						printStream.print("Longitude:\t"+plotInfo.geoPosLongitude+LINE_SEPARATOR);
					}
					if(Float.isFinite(plotInfo.elevation)) {
						printStream.print("Elevation:\t"+plotInfo.elevation+LINE_SEPARATOR);
					}
				}

				printStream.print(LINE_SEPARATOR);
			}
		} catch (RemoteException e) {
			log.error(e);
		}
	}

	private void writeTimeseries(TimestampSeries timeseries, String plotID, PrintStream csvOut) {		
		ProjectionFillIterator it = new ProjectionFillIterator(timeseries.tsIterator(), sensorNames);
		while(it.hasNext()) {
			TsEntry entry = it.next();
			boolean isFirst = true;
			StringBuilder s = new StringBuilder();
			if(col_plotid) {
				s.append(plotID);
				isFirst = false;
			}
			if(col_timestamp) {
				if(!isFirst) {
					s.append(',');
				}
				s.append(entry.timestamp);
				isFirst = false;
			}
			if(col_datetime) {
				if(!isFirst) {
					s.append(',');
				}
				s.append(TimeUtil.oleMinutesToText(entry.timestamp));
				isFirst = false;
			}
			Formatter formater = new Formatter(s,Locale.ENGLISH);
			for(int i=0;i<sensorNames.length;i++) {
				float v = entry.data[i];
				if(Float.isNaN(v)) {
					if(isFirst) {
						formater.format("NA");
						isFirst = false;
					} else {
						formater.format(",NA");
					}	
				} else {
					if(isFirst) {
						//formater.format("%3.3f", v);
						formater.format("%.2f", v);
						isFirst = false;
					} else {
						//formater.format(",%3.3f", v);
						formater.format(",%.2f", v);
					}
				}
			}
			if(col_qualitycounter) {
				if(!isFirst) {
					s.append(',');
				}
				s.append(entry.qualityCountersToString());
				isFirst = false;
			}
			s.append(LINE_SEPARATOR);
			csvOut.print(s);
			formater.close();			
		}		
	}

	public int getProcessedPlots() {
		return processedPlots;
	}
}
