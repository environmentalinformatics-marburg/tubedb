package tsdb.remote;

import static tsdb.util.AssumptionCheck.throwNull;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


import org.tinylog.Logger;
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
	private final boolean validate_sensors = true; // filter sensors based on selected plots
	private final boolean desc_sensor;
	private final boolean desc_plot;
	private final boolean info = true;
	private final boolean desc_settings;
	private final boolean write_header;
	private final Long startTimestamp;
	private final Long endTimestamp;
	private boolean plots_aggregate = true; // aggregate all plots to one value per time step
	private boolean plots_separate = true; // separate plots
	private boolean casted = false;

	private int processedPlots = 0;

	public ZipExport(RemoteTsDB tsdb, Region region, String[] sensorNames, String[] plotIDs, AggregationInterval aggregationInterval, DataQuality dataQuality, boolean interpolated, boolean allinone, boolean desc_sensor, boolean desc_plot, boolean desc_settings, boolean col_plotid, boolean col_timestamp, boolean col_datetime, boolean write_header, Long startTimestamp, Long endTimestamp, boolean col_qualitycounter, boolean plots_separate, boolean plots_aggregate, boolean col_year, boolean col_month, boolean col_day, boolean col_hour, boolean col_day_of_year, boolean casted) {
		super(col_plotid, col_timestamp, col_datetime, col_qualitycounter, col_year, col_month, col_day, col_hour, col_day_of_year);
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
				Logger.warn(e);
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
		this.plots_separate = plots_separate;
		this.plots_aggregate = plots_aggregate;
		this.casted = casted;
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
			Logger.error(e);
			return false;
		}

	}


	public boolean writeToStream(OutputStream outputstream) {
		printLine("start export...");
		printLine("");
		printLine("climate parameters ("+sensorNames.length+"):");
		for(String sensorName:sensorNames) {
			printLine(sensorName);
		}
		printLine("");
		if(Util.empty(sensorNames)) {
			return false;
		}
		if(Util.empty(plotIDs)) {
			return false;
		}
		printLine("plots: "+plotIDs.length);
		printLine("");

		try {
			ZipOutputStream zipOutputStream = new ZipOutputStream(outputstream);
			zipOutputStream.setComment("TubeDB time series data");
			zipOutputStream.setLevel(9);

			if(validate_sensors) {
				try {
					LinkedHashSet<String> processingSensorNames = new LinkedHashSet<>();
					for(String plotID:plotIDs) {
						String[] schema = tsdb.getValidSchemaWithVirtualSensors(plotID, sensorNames);
						processingSensorNames.addAll(Arrays.asList(schema));
					}
					if(processingSensorNames.size() != sensorNames.length) {
						printLine("Not all selected climate parameters are available on selected plots.");
						printLine("");
						sensorNames = processingSensorNames.toArray(new String[0]);
						printLine("processing climate parameters ("+sensorNames.length+"):");
						for(String sensorName:sensorNames) {
							printLine(sensorName);
						}
						printLine("");
					}
				} catch (Exception e) {
					e.printStackTrace();
					Logger.error(e);
					printLine("ERROR "+e);
				}
			}

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

			if(info) {
				if(region.description != null && !region.description.isEmpty()) {
					zipOutputStream.putNextEntry(new ZipEntry("info.txt"));
					OutputStreamWriter writer = new OutputStreamWriter(zipOutputStream, charset);
					BufferedWriter bufferedWriter = new BufferedWriter(writer);
					write_info(bufferedWriter);
					bufferedWriter.flush();
					writer.flush();
				}
			}

			if(plots_aggregate) {
				printLine("processing plots_aggregate ...");
				zipOutputStream.putNextEntry(new ZipEntry("plots_aggregated.csv"));
				OutputStreamWriter writer = new OutputStreamWriter(zipOutputStream, charset);
				BufferedWriter bufferedWriter = new BufferedWriter(writer);
				if(write_header) {
					writeCSVHeader(bufferedWriter, sensorNames, false);
				}
				try {
					TimestampSeries timeseries = tsdb.plots_aggregate(plotIDs, sensorNames, aggregationInterval, dataQuality, interpolated, startTimestamp, endTimestamp);
					String plots_aggregated_name = "mean";
					if(timeseries!=null) {								
						writeTimeseries(timeseries, plots_aggregated_name, sensorNames, aggregationInterval, bufferedWriter, false);	
					} else {
						printLine("not processed: " + plots_aggregated_name);
					}
				} catch (Exception e) {
					e.printStackTrace();
					Logger.error(e);
					printLine("ERROR "+e);
				}
				bufferedWriter.flush();
				writer.flush();
			}

			if(plots_separate) {
				if(allInOne) {	
					if(casted) {
						printLine("processing plots_casted ...");
						zipOutputStream.putNextEntry(new ZipEntry("plots.csv"));
						OutputStreamWriter writer = new OutputStreamWriter(zipOutputStream, charset);
						BufferedWriter bufferedWriter = new BufferedWriter(writer);
						try {
							TimestampSeries timeseries = tsdb.plots_casted(plotIDs, sensorNames, aggregationInterval, dataQuality, interpolated, startTimestamp, endTimestamp);
							String plots_casted_name = "casted";
							if(timeseries != null) {
								String[] castedSensorNames = timeseries.sensorNames;
								Logger.info("casted " + Arrays.toString(castedSensorNames));
								if(write_header) {
									writeCSVHeader(bufferedWriter, castedSensorNames, false);
								}
								writeTimeseries(timeseries, plots_casted_name, castedSensorNames, aggregationInterval, bufferedWriter, false);	
							} else {
								printLine("not processed: " + plots_casted_name);
							}
						} catch (Exception e) {
							e.printStackTrace();
							Logger.error(e);
							printLine("ERROR "+e);
						}
						bufferedWriter.flush();
						writer.flush();						
					} else {
						zipOutputStream.putNextEntry(new ZipEntry("plots.csv"));
						OutputStreamWriter writer = new OutputStreamWriter(zipOutputStream, charset);
						BufferedWriter bufferedWriter = new BufferedWriter(writer);
						if(write_header) {
							writeCSVHeader(bufferedWriter, sensorNames, col_plotid);
						}
						processedPlots = 0;
						for(String plotID:plotIDs) {
							printLine("processing plot "+plotID+" ...");
							try {
								String[] schema = tsdb.getValidSchemaWithVirtualSensors(plotID, sensorNames);
								if(!Util.empty(schema)) {
									TimestampSeries timeseries = tsdb.plot(null,plotID, schema, aggregationInterval, dataQuality, interpolated, startTimestamp, endTimestamp);
									if(timeseries!=null) {								
										writeTimeseries(timeseries, plotID, sensorNames, aggregationInterval, bufferedWriter, col_plotid);	
									} else {
										printLine("not processed: "+plotID);
									}
								}
							} catch (Exception e) {
								e.printStackTrace();
								Logger.error(e);
								printLine("ERROR "+e);
							}
							processedPlots++;
						}
						bufferedWriter.flush();
						writer.flush();
					}
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
										writeCSVHeader(bufferedWriter, sensorNames, col_plotid);
									}
									writeTimeseries(timeseries, plotID, sensorNames, aggregationInterval, bufferedWriter, col_plotid);
									bufferedWriter.flush();
									writer.flush();
								} else {
									printLine("not processed: "+plotID);
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
							Logger.error(e);
							printLine("ERROR "+e);
						}
						processedPlots++;
					}				
				}
			}
			zipOutputStream.finish();
			printLine("");
			printLine("...finished");
			return true;
		} catch (IOException e) {
			Logger.warn(e);
			printLine("ERROR "+e);
			return false;
		}		
	}	

	private void write_settings_YAML(BufferedWriter bufferedWriter) {		
		try {
			Map<String,Object> map = new LinkedHashMap<String,Object>();
			map.put("creation date", LocalDateTime.now().toString());
			map.put("tubedb version", tsdb.get_tubedb_version());

			Map<String, Object> regionMap = new LinkedHashMap<String,Object>();
			regionMap.put("id", region.name);
			regionMap.put("name", region.longName);
			if(region.time_zone != null) {
				regionMap.put("time_zone", region.time_zone);
			}
			if(region.time_zone_description != null) {
				regionMap.put("time_zone_description", region.time_zone_description);
			}
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
				Logger.error(e);
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
			if(col_year) columnlist.add("year");
			if(col_month) columnlist.add("month");
			if(col_day) columnlist.add("day");
			if(col_hour) columnlist.add("hour");
			if(col_day_of_year) columnlist.add("day_of_year");			
			if(col_qualitycounter) columnlist.add("qualitycounter");
			map.put("data columns", columnlist);
			map.put("data header", write_header);

			if(plots_aggregate) {
				map.put("aggregation over all plots", "plots_aggregated.csv");
			}

			if(plots_separate) {
				if(allInOne) {
					map.put("all plots in one file", "plots.csv");
				} else {
					map.put("all plots in separate files", "[PLOT].csv");
				}
			}

			List<String> filelist = new ArrayList<String>();
			if(desc_sensor) filelist.add("sensor_description.csv");
			if(desc_plot) filelist.add("plot_description.csv");
			if(desc_settings) filelist.add("processing_settings.yaml");

			map.put("additional files", filelist);

			DumperOptions options = new DumperOptions();
			options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
			options.setLineBreak(LINE_BREAK);
			Yaml yaml = new Yaml(options);
			yaml.dump(map, bufferedWriter);
		} catch(Exception e) {
			Logger.error(e);
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
			//CSVWriter csvWriter = new CSVWriter(bufferedWriter, ',', '"', LINE_SEPARATOR);
			CSVWriter csvWriter = new CSVWriter(bufferedWriter, ',', '"', '"', LINE_SEPARATOR);
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
					Logger.error(e);
				}
				csvWriter.writeNext(new String[]{sensorName, sensorDescription, sensorUnit}, false);
			}
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	private void write_info(BufferedWriter bufferedWriter) {		
		try {
			bufferedWriter.write(region.description);
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	private void write_plot_description_CSV(BufferedWriter bufferedWriter) {		
		try {
			//@SuppressWarnings("resource") //don't close stream
			//CSVWriter csvWriter = new CSVWriter(bufferedWriter, ',', '"', LINE_SEPARATOR);
			@SuppressWarnings("resource") //don't close stream
			CSVWriter csvWriter = new CSVWriter(bufferedWriter, ',', '"', '"', LINE_SEPARATOR);
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
			Logger.error(e);
		}
	}

	public int getProcessedPlots() {
		return processedPlots;
	}
}
