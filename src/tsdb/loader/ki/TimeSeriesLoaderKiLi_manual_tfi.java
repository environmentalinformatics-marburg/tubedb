package tsdb.loader.ki;

import static tsdb.util.AssumptionCheck.throwNull;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.StationProperties;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.component.SourceEntry;
import tsdb.util.DataRow;
import tsdb.util.Table;
import tsdb.util.Table.ColumnReaderFloat;
import tsdb.util.Table.ColumnReaderString;
import tsdb.util.Table.ColumnReaderTimestampTwoCols;
import tsdb.util.TimestampInterval;
import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.iterator.CSVIterator;
import tsdb.util.iterator.TimestampSeries;

/**
 * loads tfi data from directory
 * @author woellauer
 *
 */
public class TimeSeriesLoaderKiLi_manual_tfi {

	private static final Logger log = LogManager.getLogger();

	private final TsDB tsdb; //not null

	public TimeSeriesLoaderKiLi_manual_tfi(TsDB tsdb) {
		throwNull(tsdb);
		this.tsdb = tsdb;
	}

	/**
	 * load one directory with files of tfi manual measurement files
	 * @param kiliTfiPath
	 */
	public void loadOneDirectory_structure_kili_tfi(Path kiliTfiPath) {		
		try {
			if(Files.exists(kiliTfiPath)) {
				DirectoryStream<Path> stream = Files.newDirectoryStream(kiliTfiPath);
				log.info("load directory of manual tfi files    "+kiliTfiPath);
				for(Path path:stream) {
					try {
						if(!path.toFile().isDirectory()) {
							String filename = path.getName(path.getNameCount()-1).toString();
							if(filename.endsWith(".csv")) {
								if(filename.startsWith("iso_tfi")) {
									log.info("load iso tfi  "+path);
									loadOneFile_structure_kili_iso_tfi(path);
								} else if(filename.indexOf("_tfi")==4) {
									String plotID = filename.substring(0, 4);
									log.info("load plot tfi  "+plotID+"   from  "+path.getFileName());
									VirtualPlot virtualPlot = tsdb.getVirtualPlot(plotID);
									if(virtualPlot!=null) {
										loadOneFile_structure_kili_tfi(virtualPlot,path);
									} else {
										log.warn("unknown plotID: "+plotID);
									}
								} else {
									log.warn("no csv tfi file: "+filename);
								}
								//ascCollectorMap.putIfAbsent(fileKey, path);
							} else {
								log.warn("no csv file: "+filename);
							}
						}
					} catch (Exception e) {
						log.error("error in load tfi file: "+e+"  "+path);
					}
				}

			} else {
				log.warn("directory not found: "+kiliTfiPath);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	private String get_tfi_serial(VirtualPlot virtualPlot, long start, long end) {
		String serial = null;
		for(TimestampInterval<StationProperties> interval:virtualPlot.intervalList) {
			if(interval.contains(start, end)) {
				String loggerTypeName = interval.value.get_logger_type_name();
				if(loggerTypeName.equals("tfi")) {
					if(interval.value.get_plotid().equals(virtualPlot.plotID)) {
						if(serial==null) {
							serial = interval.value.get_serial();
						} else {
							log.warn("multiple entries");
						}
					} else {
						log.error("plotIDs not consistent");
					}						
				}
			}
		}
		return serial;
	}

	/**
	 * load one tfi manual measurement file
	 * @param kiliTfiPath
	 */
	private void loadOneFile_structure_kili_tfi(VirtualPlot virtualPlot, Path path) {		
		CSVIterator input_iterator = CSVIterator.create(path, true);
		if(input_iterator!=null&&input_iterator.hasNext()) {
			TimestampSeries timestampSeries = input_iterator.toTimestampSeries(virtualPlot.plotID);
			long start = timestampSeries.getFirstTimestamp();
			long end = timestampSeries.getLastTimestamp();
			final String serial = get_tfi_serial(virtualPlot, start, end);			
			if(serial!=null) {
				String[] targetSchema = tsdb.getLoggerType("tfi").sensorNames;
				Loader_manual_tfi loader = new Loader_manual_tfi(timestampSeries);
				loader.load(targetSchema);
				List<DataRow> events = loader.toDataRows();
				if(events!=null) {
					tsdb.streamStorage.insertDataRows(serial, events, start, end, targetSchema);
					tsdb.sourceCatalog.insert(new SourceEntry(path,serial,start,end,events.size(),timestampSeries.sensorNames, targetSchema, TsSchema.NO_CONSTANT_TIMESTEP));
				} else {
					log.warn("no events inserted: "+path);
				}
			} else {
				log.warn("no serial found for tfi: "+virtualPlot.plotID+"   "+path);
			}
		} else {
			log.warn("empty file: "+path);
		}
	}

	private void loadOneFile_structure_kili_iso_tfi(Path filePath) {
		Table table = Table.readCSV(filePath.toString(), ',');
		ColumnReaderTimestampTwoCols cr_timestamp = table.createColumnReaderTimestamp("date_sample","time");
		ColumnReaderString cr_plotID = table.createColumnReader("plot_id_sp1");
		ColumnReaderString cr_type = table.createColumnReader("type");
		ColumnReaderFloat cr_iso_d18_16 = table.createColumnReaderFloat("d18_16");
		ColumnReaderFloat cr_iso_dD_H = table.createColumnReaderFloat("dD_H");
		ColumnReaderFloat cr_iso_d_excess = table.createColumnReaderFloat("d-excess");

		HashMap<String,List<TsEntry>> fogMap = new HashMap<String,List<TsEntry>>();
		HashMap<String,List<TsEntry>> rainMap = new HashMap<String,List<TsEntry>>();
		HashMap<String,List<TsEntry>> tfMap = new HashMap<String,List<TsEntry>>();

		for(String[] row:table.rows) {
			long timestamp = cr_timestamp.get(row);
			String plotID = cr_plotID.get(row);
			String type = cr_type.get(row);
			float iso_d18_16 = cr_iso_d18_16.get(row,false);
			float iso_dD_H = cr_iso_dD_H.get(row,false);
			float iso_d_excess = cr_iso_d_excess.get(row,false);

			if(!Float.isNaN(iso_d18_16) || !Float.isNaN(iso_dD_H) || !Float.isNaN(iso_d_excess)) {

				//System.out.println(TimeConverter.oleMinutesToLocalDateTime(timestamp)+"  "+plotID+"  "+type+" "+iso_d18_16+" "+iso_dD_H+" "+iso_d_excess);


				HashMap<String,List<TsEntry>> map = null;

				switch(type) {
				case "fog":
					map = fogMap;
					break;
				case "rain":
					map = rainMap;
					break;
				case "tf":
					map = tfMap;
					break;
				default:
					log.warn("unknown type "+type);			
				}

				if(map!=null) {
					List<TsEntry> tsList = map.get(plotID);
					if(tsList==null) {
						tsList = new ArrayList<TsEntry>();
						map.put(plotID, tsList);
					}
					if(!tsList.isEmpty()) {
						TsEntry top = tsList.get(tsList.size()-1);
						if(top.timestamp<=timestamp) {
							throwNull(new RuntimeException("timestamps are not ordered"));
						}
					}
					tsList.add(new TsEntry(timestamp, new float[]{iso_d18_16,iso_dD_H,iso_d_excess}));
				}
			}
		}


		String[] isoNames = new String[]{"d18_16","dD_H","d_excess"};

		String[] types = new String[]{"fog","rain","tf"};
		@SuppressWarnings("unchecked")
		HashMap<String, List<TsEntry>>[] maps = new HashMap[]{fogMap,rainMap,tfMap};

		for(int typeIndex=0; typeIndex<types.length; typeIndex++) {			
			final String type = types[typeIndex];
			final HashMap<String, List<TsEntry>> map = maps[typeIndex];
			String[] sensorNames = Arrays.stream(isoNames).map(name->type+"_"+name).toArray(String[]::new);
			System.out.println(Arrays.toString(sensorNames));
			for(Entry<String, List<TsEntry>> mapEntry:map.entrySet()) {
				String plotID = mapEntry.getKey();
				VirtualPlot virtualPlot = tsdb.getVirtualPlot(plotID);
				if(virtualPlot!=null) {
					String stationID = get_tfi_serial(virtualPlot, mapEntry.getValue().get(0).timestamp, mapEntry.getValue().get(mapEntry.getValue().size()-1).timestamp);
					if(stationID!=null) {
						TimestampSeries timestampSeries = new TimestampSeries(stationID,sensorNames ,mapEntry.getValue());
						tsdb.streamStorage.insertTimestampSeries(timestampSeries);
					} else {
						log.warn("stationID not found of plot "+plotID);	
					}
				} else {
					log.warn("unknown plotID: "+plotID+"   in  "+filePath);
				}
			}
		}
	}
}
