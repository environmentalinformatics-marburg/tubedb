package tsdb.remote;

import static tsdb.util.AssumptionCheck.throwNull;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import com.opencsv.CSVWriter;

import tsdb.component.Region;
import tsdb.component.Sensor;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.TimeUtil;
import tsdb.util.Util;
import tsdb.util.iterator.TimestampSeries;
import tsdb.util.iterator.TimestampSeriesCSVwriter;

/**
 * Creates Zip-files of sets of time series
 * @author woellauer
 *
 */
public class ZipExport extends TimestampSeriesCSVwriter{
	private static final Logger log = LogManager.getLogger();
	private static final Charset charset = Charset.forName("UTF-8");

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
	private final boolean write_header;
	private final Long startTimestamp;
	private final Long endTimestamp;

	private int processedPlots = 0;

	public ZipExport(RemoteTsDB tsdb, Region region, String[] sensorNames, String[] plotIDs, AggregationInterval aggregationInterval, DataQuality dataQuality, boolean interpolated, boolean allinone, boolean desc_sensor, boolean desc_plot, boolean desc_settings, boolean col_plotid, boolean col_timestamp, boolean col_datetime, boolean write_header, Long startTimestamp, Long endTimestamp, boolean col_qualitycounter) {
		super(col_plotid, col_timestamp, col_datetime, col_qualitycounter);
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
		this.write_header = write_header;
		this.startTimestamp = startTimestamp;
		this.endTimestamp = endTimestamp;
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
			zipOutputStream.setComment("TubeDB time series data");
			zipOutputStream.setLevel(9);

			if(desc_settings) {
				zipOutputStream.putNextEntry(new ZipEntry("processing_settings.yaml"));
				OutputStreamWriter writer = new OutputStreamWriter(zipOutputStream, charset);
				BufferedWriter bufferedWriter = new BufferedWriter(writer);
				write_settings_YAML(bufferedWriter);
				bufferedWriter.flush();
				writer.flush();
			}

			if(desc_sensor) {
				zipOutputStream.putNextEntry(new ZipEntry("sensor_description.csv"));
				OutputStreamWriter writer = new OutputStreamWriter(zipOutputStream, charset);
				BufferedWriter bufferedWriter = new BufferedWriter(writer);
				write_sensor_description_CSV(bufferedWriter);
				bufferedWriter.flush();
				writer.flush();
			}

			if(desc_plot) {
				zipOutputStream.putNextEntry(new ZipEntry("plot_description.csv"));
				OutputStreamWriter writer = new OutputStreamWriter(zipOutputStream, charset);
				BufferedWriter bufferedWriter = new BufferedWriter(writer);
				write_plot_description_CSV(bufferedWriter);
				bufferedWriter.flush();
				writer.flush();
			}

			if(allInOne) {				
				zipOutputStream.putNextEntry(new ZipEntry("plots.csv"));
				OutputStreamWriter writer = new OutputStreamWriter(zipOutputStream, charset);
				BufferedWriter bufferedWriter = new BufferedWriter(writer);
				if(write_header) {
					writeCSVHeader(bufferedWriter, sensorNames);
				}
				processedPlots = 0;
				for(String plotID:plotIDs) {
					printLine("processing plot "+plotID);
					try {
						String[] schema = tsdb.getValidSchemaWithVirtualSensors(plotID, sensorNames);
						if(!Util.empty(schema)) {
							TimestampSeries timeseries = tsdb.plot(null,plotID, schema, aggregationInterval, dataQuality, interpolated, startTimestamp, endTimestamp);
							if(timeseries!=null) {								
								writeTimeseries(timeseries, plotID, sensorNames, aggregationInterval, bufferedWriter);	
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
				bufferedWriter.flush();
				writer.flush();
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
								OutputStreamWriter writer = new OutputStreamWriter(zipOutputStream, charset);
								BufferedWriter bufferedWriter = new BufferedWriter(writer);
								if(write_header) {
									writeCSVHeader(bufferedWriter, sensorNames);
								}
								writeTimeseries(timeseries, plotID, sensorNames, aggregationInterval, bufferedWriter);
								bufferedWriter.flush();
								writer.flush();
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

	private void write_settings_YAML(BufferedWriter bufferedWriter) {		
		try {
			Map<String,Object> map = new LinkedHashMap<String,Object>();
			map.put("creation date", LocalDateTime.now().toString());

			Map<String, Object> regionMap = new LinkedHashMap<String,Object>();
			regionMap.put("id", region.name);
			regionMap.put("name", region.longName);
			map.put("region", regionMap);

			try {
				HashSet<String> plotSet = new HashSet<>(Arrays.asList(plotIDs));			
				TreeSet<GeneralStationInfo> generals = new TreeSet<GeneralStationInfo>();
				PlotInfo[] plotInfos = tsdb.getPlots();
				for(PlotInfo plotInfo:plotInfos) {
					if(plotSet.contains(plotInfo.name) && !generals.contains(plotInfo.generalStationInfo)) {
						generals.add(plotInfo.generalStationInfo);
					}
				}
				ArrayList<Object> generalList = new ArrayList<Object>();
				for(GeneralStationInfo general:generals) {
					Map<String, Object> generalMap = new LinkedHashMap<String,Object>();
					generalMap.put("id", general.name);
					generalMap.put("name", general.longName);
					generalList.add(generalMap);
				}
				map.put("groups", generalList);
			} catch(Exception e) {
				log.error(e);
			}

			map.put("plots", plotIDs);
			map.put("sensors", sensorNames);


			String timeStart = startTimestamp==null?"*":TimeUtil.oleMinutesToText(startTimestamp);
			String timeEnd = endTimestamp==null?"*":TimeUtil.oleMinutesToText(endTimestamp);
			Map<String,Object> timeMap = new LinkedHashMap<String,Object>();
			timeMap.put("start", timeStart);
			timeMap.put("end", timeEnd);
			map.put("time interval", timeMap);

			map.put("aggregation", aggregationInterval.getText());
			map.put("quality check", dataQuality.getText());
			map.put("interpolation", interpolated);

			List<String> columnlist = new ArrayList<String>();
			if(col_plotid) columnlist.add("plotID");
			if(col_timestamp) columnlist.add("timestamp");
			if(col_datetime) columnlist.add("datetime");
			if(col_qualitycounter) columnlist.add("qualitycounter");
			map.put("data columns", columnlist);

			map.put("data header", write_header);			
			map.put("all plots in one file", allInOne);

			List<String> filelist = new ArrayList<String>();
			if(desc_sensor) filelist.add("sensor description");
			if(desc_plot) filelist.add("plot description");
			if(desc_settings) filelist.add("processing settings");

			map.put("additional files", filelist);

			DumperOptions options = new DumperOptions();
			options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			options.setLineBreak(LINE_BREAK);
			Yaml yaml = new Yaml(options);
			yaml.dump(map, bufferedWriter);
		} catch(Exception e) {
			log.error(e);
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

	private void write_sensor_description_CSV(BufferedWriter bufferedWriter) {
		try {
			@SuppressWarnings("resource") //don't close stream
			CSVWriter csvWriter = new CSVWriter(bufferedWriter, ',', '"', LINE_SEPARATOR);
			csvWriter.writeNext(new String[]{"name", "description", "unit"}, false);
			for(String sensorName:sensorNames) {
				String sensorDescription = "";
				String sensorUnit = "";
				try {
					Sensor sensor = tsdb.getSensor(sensorName);
					if(sensor!=null) {
						sensorDescription = sensor.description;
						sensorUnit = sensor.unitDescription;
					}
				} catch (Exception e) {
					log.error(e);
				}
				csvWriter.writeNext(new String[]{sensorName, sensorDescription, sensorUnit}, false);
			}
		} catch (Exception e) {
			log.error(e);
		}
	}

	private void write_plot_description_CSV(BufferedWriter bufferedWriter) {		
		try {
			@SuppressWarnings("resource") //don't close stream
			CSVWriter csvWriter = new CSVWriter(bufferedWriter, ',', '"', LINE_SEPARATOR);
			PlotInfo[] plotInfos = tsdb.getPlots();
			Map<String,PlotInfo> map = new HashMap<String,PlotInfo>();
			for(PlotInfo plotInfo:plotInfos) {
				map.put(plotInfo.name, plotInfo);
			}

			csvWriter.writeNext(new String[]{"plot","general","region","lat","lon","elevation"}, false);

			for(int i=0;i<plotIDs.length;i++) {
				PlotInfo plotInfo = map.get(plotIDs[i]);
				double lat = plotInfo.geoPosLatitude;
				double lon = plotInfo.geoPosLongitude;
				double elevation = plotInfo.elevation;
				csvWriter.writeNext(new String[]{
						plotInfo.name,
						plotInfo.generalStationInfo.name,
						plotInfo.generalStationInfo.region.name,
						Double.isFinite(lat)?Double.toString(lat):"",
								Double.isFinite(lon)?Double.toString(lon):"",
										Double.isFinite(elevation)?Double.toString(elevation):""
				}, false);
			}
		} catch (Exception e) {
			log.error(e);
		}
	}

	public int getProcessedPlots() {
		return processedPlots;
	}
}
