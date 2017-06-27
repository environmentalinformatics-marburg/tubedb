package tsdb.run;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.component.SourceEntry;
import tsdb.component.labeledproperty.LabeledProperty;
import tsdb.component.labeledproperty.PropertyComputation;
import tsdb.util.AssumptionCheck;
import tsdb.util.DataRow;
import tsdb.util.Table;
import tsdb.util.TimeUtil;
import tsdb.util.Util;

/**
 * import generic CSV data into database.
 * Beginning of filename is station name e.g. mystation_2014_new.csv  ==> station: mystation
 * first column name: datetime   format: ISO_8601  e.g. YYYY-MM-DDThh:mm
 * fllowing columns: database sensor names  
 * @author woellauer
 *
 */
public class ImportGenericCSV {
	private static final Logger log = LogManager.getLogger();

	private final TsDB tsdb;

	public ImportGenericCSV(TsDB tsdb) {
		log.info("ImportGenericCSV");
		AssumptionCheck.throwNull(tsdb);
		this.tsdb = tsdb;
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
			int datetimeIndex = table.getColumnIndex("datetime");
			if(datetimeIndex!=0) {
				throw new RuntimeException("wrong format");
			}

			String filename = filePath.getFileName().toString();

			int postFixIndex = filename.indexOf('_'); //filename with station name and postfix

			if(postFixIndex<0) {
				postFixIndex = filename.indexOf('.'); //filename with station name and without postfix
			}

			if(postFixIndex<1) {
				throw new RuntimeException("could not get station name from file name: "+filename);
			}

			String stationName = filename.substring(0, postFixIndex);
			log.trace("station "+stationName);
			Station station = tsdb.getStation(stationName);
			if(station==null) {
				throw new RuntimeException("station not found: "+stationName+"   in "+filePath);
			}

			final int sensors = table.names.length-1;

			ArrayList<DataRow> dataRows = new ArrayList<>(table.rows.length);

			int prevTimestamp = -1;
			for(String[] row:table.rows) {				
				int timestamp = (int) TimeUtil.dateTimeToOleMinutes(LocalDateTime.parse(row[0]));

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
				long firstTimestamp = dataRows.get(0).timestamp;
				long lastTimestamp = dataRows.get(dataRows.size()-1).timestamp;

				List<LabeledProperty> computationList = station.labeledProperties.query("computation", (int)firstTimestamp, (int)lastTimestamp);
				if(computationList.size()>0) {
					log.trace("LabeledProperty computations");
					for(LabeledProperty prop:computationList) {					
						try {
							PropertyComputation computation = ((PropertyComputation)prop.content);
							if(Util.containsString(sensorNames, computation.target)) {
								log.trace("LabeledProperty computation "+computation.target);
								computation.calculate(dataRows, sensorNames, firstTimestamp, lastTimestamp);
							}
						} catch(Exception e) {
							e.printStackTrace();
							log.warn(e);
						}
					}
				}

				tsdb.streamStorage.insertDataRows(stationName, sensorNames, dataRows);
				tsdb.sourceCatalog.insert(SourceEntry.of(stationName, sensorNames, dataRows, filePath));
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e+"   "+filePath);
		}
	}
}
