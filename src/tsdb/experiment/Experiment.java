package tsdb.experiment;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.DataRow;
import tsdb.util.Table;
import tsdb.util.TimeUtil;
import tsdb.util.Timer;

public class Experiment implements AutoCloseable {
	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) throws Exception {
		if(args.length < 2) {
			throw new RuntimeException("experiment needs (at least) two args: type name   e.g. experiment TubeDB import data/csv");
		}
		String experiment_type = args[0];
		String experiment_name = args[1];

		Experiment experiment = null;
		try {
			switch(experiment_type) {
			case "DryRun": {
				experiment = new Experiment();
				break;
			}
			case "TubeDB": {
				experiment = new Experiment_TubeDB();
				break;
			}
			case "InfluxDB": {
				experiment = new Experiment_InfluxDB_OLD();
				break;
			}
			case "H2": {
				experiment = new Experiment_H2();
				break;
			}
			default: {
				throw new RuntimeException("unknown experiment type");
			}
			}

			switch(experiment_name) {
			case "csv_import": {
				if(args.length < 3) {
					throw new RuntimeException("experiment needs three args: type name csv-folder   e.g. experiment TubeDB import data/csv");
				}
				experiment.readCSV(args[2]);
				break;
			}
			case "full_read": {
				Timer.start("full_read");
				long rows = experiment.full_read();
				log.info(Timer.stop("full_read")+"  rows: " + rows);
				break;
			}
			default: {
				throw new RuntimeException("unknown experiment name");
			}
			}
		} finally {
			if(experiment != null) {
				try {
					experiment.close();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	protected long full_read() {
		log.info("dryRun: no data");
		return 0;
	}

	private void readCSV(String path) {
		Timer.start("clear");
		clear();
		log.info(Timer.stop("clear"));
		Timer.start("import");
		load(path);
		log.info(Timer.stop("import"));
	}

	protected void clear() {		
	}

	public void load(String rootPath) {
		load(Paths.get(rootPath));
	}

	public void load(Path rootPath) {
		loadFiles(rootPath);
		loadSubDirs(rootPath);
	}

	public void loadSubDirs(Path rootPath) {
		try(DirectoryStream<Path> rootStream = Files.newDirectoryStream(rootPath)) {
			for(Path sub:rootStream) {
				if(Files.isDirectory(sub)) {
					load(sub);
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
	}

	public void loadFiles(Path rootPath) {
		try(DirectoryStream<Path> rootStream = Files.newDirectoryStream(rootPath)) {
			for(Path sub:rootStream) {
				if(!Files.isDirectory(sub)) {
					loadFile(sub);
				}
			}
		} catch (Exception e) {
			log.error(e);
		}		
	}

	public void loadFile(Path filePath) {
		try {
			log.info("load file "+filePath);			
			Table table = Table.readCSV(filePath,',');		
			int datetimeIndex = getDatetimeIndex(table);
			if(datetimeIndex!=0) {
				throw new RuntimeException("wrong format");
			}

			String stationName = parseStationName(filePath);
			log.trace("station "+stationName);

			final int sensors = table.names.length-1;

			ArrayList<DataRow> dataRows = new ArrayList<>(table.rows.length);

			int prevTimestamp = -1;
			for(String[] row:table.rows) {				
				int timestamp = parseTimestamp(row[0]);

				if(timestamp==prevTimestamp) {
					log.warn("skip duplicate timestamp "+row[0]+" "+filePath);
					continue;
				}

				float[] data = new float[sensors];
				for(int i=0;i<sensors;i++) {
					String text = row[i+1];
					if(text.isEmpty() || text.equals("NA")) {
						data[i] = Float.NaN;
					} else {
						try {
							float value = Float.parseFloat(text);
							if( Float.isFinite(value) && value!= -9999 ) {
								data[i] = value;
							} else {
								data[i] = Float.NaN;
							}
						} catch(Exception e) {
							data[i] = Float.NaN;
						}
					}
				}
				dataRows.add(new DataRow(data, timestamp));

				prevTimestamp = timestamp;
			}

			if(!dataRows.isEmpty()) {
				String[] sensorNames = Arrays.copyOfRange(table.names, 1, sensors + 1);
				insertTimeseries(stationName, sensorNames, dataRows);				
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e+"   "+filePath);
		}
	}

	protected String parseStationName(Path filePath) {
		String filename = filePath.getFileName().toString();

		int postFixIndex = filename.indexOf('_'); //filename with station name and postfix

		if(postFixIndex<0) {
			postFixIndex = filename.indexOf('.'); //filename with station name and without postfix
		}

		if(postFixIndex<1) {
			throw new RuntimeException("could not get station name from file name: "+filename);
		}

		return filename.substring(0, postFixIndex);		
	}

	protected int parseTimestamp(String timestampText) {
		return TimeUtil.parseStartTimestamp(timestampText);
	}

	protected int getDatetimeIndex(Table table) {
		return table.getColumnIndex("datetime");
	}

	protected void insertTimeseries(String stationName, String[] sensorNames, ArrayList<DataRow> dataRows) {
		log.info("insert timeseries " + stationName);
	}

	@Override
	public void close() throws Exception { }

}
