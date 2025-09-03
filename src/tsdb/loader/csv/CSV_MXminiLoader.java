package tsdb.loader.csv;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

import org.tinylog.Logger;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import ch.randelshofer.fastdoubleparser.JavaFloatParser;
import tsdb.Station;
import tsdb.TsDB;
import tsdb.component.SourceEntry;
import tsdb.util.AssumptionCheck;
import tsdb.util.DataRow;
import tsdb.util.Interval;
import tsdb.util.TimeUtil;

public class CSV_MXminiLoader {	

	private final TsDB tsdb;

	public CSV_MXminiLoader(TsDB tsdb) {
		AssumptionCheck.throwNull(tsdb);
		this.tsdb = tsdb;
	}

	public void load(String rootPath) {
		load(Paths.get(rootPath));
	}

	public void load(Path rootPath) {
		Logger.info("load directory with MXminiLoader:        "+rootPath);
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
			Logger.error(e);
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
			Logger.error(e);
		}		
	}

	private static final Charset ASCII = Charset.forName("ASCII");

	public void loadFile(Path filePath) {
		try {
			Logger.info("load file "+filePath);			
			InputStreamReader in = new InputStreamReader(new FileInputStream(filePath.toFile()),ASCII);			
			CSVParser parser = new CSVParserBuilder().withSeparator(';').withIgnoreQuotations(true).build();			
			CSVReader csvReader = new CSVReaderBuilder(in).withCSVParser(parser).withMultilineLimit(1).build();

			String[] row1 = csvReader.readNext();
			String[] row2 = csvReader.readNext();
			String[] row3 = csvReader.readNext();
			String[] row4 = csvReader.readNext();
			String[] row5 = csvReader.readNext();
			String[] row6 = csvReader.readNext();
			String[] row7 = csvReader.readNext();

			if(row1 == null || row1.length != 2 || !row1[0].equals("Station Name") || !row1[1].equals("MXmini")) {
				throw new RuntimeException("wrong format in line 1");
			}
			if(row2 == null || row2.length != 2 || !row2[0].equals("Station ID")) {
				throw new RuntimeException("wrong format in line 2");
			}
			String stationID = row2[1];
			if(row2 == null || row2.length != 2) {
				throw new RuntimeException("wrong format in line 2");
			}
			if(row3 == null || row3.length != 2) {
				throw new RuntimeException("wrong format in line 3");
			}
			if(row4 == null || row4.length != 2) {
				throw new RuntimeException("wrong format in line 4");
			}
			if(row5 == null || row5.length != 2 || !row5[0].isEmpty() || !row5[1].isEmpty()) {
				throw new RuntimeException("wrong format in line 5");
			}
			if(row6 == null || row6.length < 2) {
				throw new RuntimeException("wrong format in line 6");
			}
			if(row7 == null || row6.length != row7.length) {
				throw new RuntimeException("wrong format in line 7");
			}

			int sensorNamesLen = row6.length - 1;
			String[] sensorNames = new String[sensorNamesLen];
			for(int i = 0; i < sensorNames.length; i++) {
				sensorNames[i] = row6[i + 1];
			}

			long timestampMin = Integer.MAX_VALUE;
			long timestampMax = Integer.MIN_VALUE;
			ArrayList<DataRow> dataRows = new ArrayList<>();
			String[] row = csvReader.readNext();
			while(row != null) {
				if(row.length - 1 == sensorNamesLen) {
				long timestamp = TimeUtil.parseTimestampSpaceFormat(row[0]);
				float[] data = new float[sensorNamesLen];
				for(int i = 0; i < sensorNamesLen; i++) {
					String v = row[i + 1];
					if(v.isBlank() || v.equals("nan")) {
						data[i] = Float.NaN;
					} else {
						//data[i] = Float.parseFloat(v.replace(',','.'));
						data[i] = JavaFloatParser.parseFloat(v.replace(',','.'));
					}
				}
				DataRow dataRow = new DataRow(data, timestamp);
				//Logger.info(dataRow);
				dataRows.add(dataRow);
				if(timestamp < timestampMin) {
					timestampMin = timestamp;
				}
				if(timestamp > timestampMax) {
					timestampMax = timestamp;
				}
				} else {
					Logger.warn("skip invalid line sensor columns: " + (row.length - 1) + " should be: " + sensorNamesLen + "   in " + filePath + "  line: " + Arrays.toString(row));
				}
				row = csvReader.readNext();
			}
			if(!dataRows.isEmpty()) {
				String[] correctedSensorNames = sensorNames;
				Station station = tsdb.getStation(stationID);
				if(station != null) {
					Logger.info(Arrays.toString(sensorNames));
					Interval interval = Interval.of((int) timestampMin, (int) timestampMax);
					correctedSensorNames = station.correctRawSensorNames(sensorNames, interval);
				}				
				tsdb.streamStorage.insertDataRows(stationID, correctedSensorNames, dataRows);
				tsdb.sourceCatalog.insert(SourceEntry.of(stationID, sensorNames, correctedSensorNames, dataRows, filePath));
			}
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error(e+"   "+filePath);
		}
	}
}
