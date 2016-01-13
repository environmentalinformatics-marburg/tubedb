package tsdb.run;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.xml.bind.DatatypeConverter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.Station;
import tsdb.StationProperties;
import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.loader.ki.AscParser;
import tsdb.loader.ki.TimeSeriesLoaderKiLi;
import tsdb.util.TimeUtil;
import tsdb.util.Util;
import tsdb.util.iterator.TimestampSeries;

/**
 * This class preprocesses new KiLi-data files.
 * 1. read all old data files
 * 2. read all files to exclude
 * 3. read all new data files
 * 4. remove excluded files in new data files
 * 5. compare old to new
 * 6. copy files to processed directories
 * @author woellauer
 *
 */
public class KiLiCollector {
	private static final Logger log = LogManager.getLogger();

	//private static final  String PREPROCESS_PATH = "c:/timeseriesdatabase_preprocess_temp";
	private static final  String PREPROCESS_PATH = "c:/timeseriesdatabase_preprocess";
	
	private static final String LOG_PATH = PREPROCESS_PATH+"/preprocessed_log";
	private static final String BASE_PATH = PREPROCESS_PATH+"/ki_tsm";
	private static final String EXCLUDED_PATH = PREPROCESS_PATH+"/ki_excluded";
	private static final String SOURCE_PATH = PREPROCESS_PATH+"/source";
	//private static final String EMPTY_PATH = PREPROCESS_PATH+"/empty";

	private static final String PREPROCESSED_PATH = PREPROCESS_PATH+"/preprocessed";
	private static final String PREPROCESSED_EMPTY_PATH = PREPROCESS_PATH+"/preprocessed_empty";
	private static final String PREPROCESSED_ERROR_PATH = PREPROCESS_PATH+"/preprocessed_error";
	private static final String PREPROCESSED_NO_PLOT_PATH = PREPROCESS_PATH+"/preprocessed_no_plot";
	private static final String PREPROCESSED_REMOVED_PATH = PREPROCESS_PATH+"/preprocessed_removed";

	private static final String pathLogAllFiles = LOG_PATH+"/"+"all_files.csv";
	private static final String pathLogNewFiles = LOG_PATH+"/"+"new_files.csv";
	private static final String pathLogNewDuplicateFiles = LOG_PATH+"/"+"new_duplicate_files.csv";
	private static final String pathLogNewEmptyFiles = LOG_PATH+"/"+"new_empty_files.csv";

	private final TsDB tsdb;	
	private final TimeSeriesLoaderKiLi timeseriesloaderKiLi;

	private static class CollectorEntry {
		public final String filename;
		public String status;
		public String station;		
		public int firstTimestamp;
		public int lastTimestamp;
		public String plot;
		public String logger;
		public Long filesize;
		public String md5;

		public CollectorEntry(String filename) {
			this.filename = filename;
			this.status = "init";
			this.station = null;
			this.firstTimestamp = -1;
			this.lastTimestamp = -1;
			this.plot = null;
			this.logger = null;
			this.filesize = null;
			this.md5 = null;
		}
		
		public static String CSV_HEADER = "plot"+","+"logger"+","+"station"+","+"firstTimestamp"+","+"lastTimestamp"+","+"status"+","+"md5"+","+"filesize"+","+"filename";

		public String toCSVRow() {
			return plot+","+logger+","+station+","+TimeUtil.oleMinutesToText((long) firstTimestamp)+","+TimeUtil.oleMinutesToText((long) lastTimestamp)+","+status+","+md5+","+filesize+","+filename;
		}

		public String createNewFilename() {
			String s = plot==null?"xxxx":plot;
			s += "_";
			s += logger==null?"xxx":logger;
			s += "_";
			s += station==null?"xxxx":station;
			s += "___";
			s += TimeUtil.oleMinutesToDateTimeFileText(this.firstTimestamp);
			s += "___";
			s += TimeUtil.oleMinutesToDateTimeFileText(this.lastTimestamp);
			s += ".asc";
			return s;
		}
	}

	public KiLiCollector(TsDB tsdb) {
		this.tsdb = tsdb;
		this.timeseriesloaderKiLi = new TimeSeriesLoaderKiLi(tsdb);		
	}

	private static void writeLogFile(String filename, List<String> logs) {
		try {
			if(!logs.isEmpty()) {
				Util.createDirectoriesOfFile(filename);
				PrintStream out = new PrintStream(new FileOutputStream(filename));
				out.println(CollectorEntry.CSV_HEADER);
				for(String v:logs) {
					out.println(v);
				}
				out.close();
			}
		} catch (FileNotFoundException e) {
			log.error(e);
		}
	}

	public static void main(String[] args) throws FileNotFoundException {

		System.out.println("start...");

		TsDB tsdb = TsDBFactory.createDefault();
		KiLiCollector kiliCollector = new KiLiCollector(tsdb);

		//Map<String, CollectorEntry> collectorMapBasis = kiliCollector.readDirectory_with_asc_recursive(Paths.get(EMPTY_PATH));
		Map<String, CollectorEntry> collectorMapBasis = kiliCollector.readDirectory_with_asc_recursive(Paths.get(BASE_PATH));
		TreeMap<String, List<CollectorEntry>> md5MapBasis = createMD5Map(collectorMapBasis);

		Map<String, CollectorEntry> collectorMapExcluded = kiliCollector.readDirectory_with_asc_recursive(Paths.get(EXCLUDED_PATH));
		TreeMap<String, List<CollectorEntry>> md5MapExcluded = createMD5Map(collectorMapExcluded);		

		Map<String, CollectorEntry> collectorMapToAdd = kiliCollector.readDirectory_with_asc_recursive(Paths.get(SOURCE_PATH));
		TreeMap<String, List<CollectorEntry>> md5MapToAdd = createMD5Map(collectorMapToAdd);

		for(CollectorEntry entry:collectorMapToAdd.values()) {
			if(md5MapExcluded.containsKey(entry.md5)) {
				//log.info("exclude "+entry.filename);
				entry.status = "removed";
			}
		}

		List<String> allFilesLog = collectorMapToAdd.values().stream().map(CollectorEntry::toCSVRow).collect(Collectors.toList());
		writeLogFile(pathLogAllFiles, allFilesLog);

		ArrayList<String> newFilesLog = new ArrayList<String>();
		ArrayList<String> newDublicateFilesLog = new ArrayList<String>();

		Map<String, CollectorEntry> collectorMapProcessed = new TreeMap<String, CollectorEntry>();
		for(Entry<String, List<CollectorEntry>> entry:md5MapToAdd.entrySet()) {
			String md5Key = entry.getKey();
			if(!md5MapBasis.containsKey(md5Key)) {
				CollectorEntry toAddEntry = entry.getValue().get(0);
				collectorMapProcessed.put(toAddEntry.filename,toAddEntry);
				if(!toAddEntry.status.equals("removed")) {
					newFilesLog.add(toAddEntry.toCSVRow());
				}

				if(entry.getValue().size()>1) {
					for(CollectorEntry e:entry.getValue()) {
						newDublicateFilesLog.add(e.toCSVRow());
					}
					newDublicateFilesLog.add("");
				}

			} else {
				for(CollectorEntry e:entry.getValue()) {
					e.status = "base duplicate"; 
				}
			}
		}

		writeLogFile(pathLogNewFiles, newFilesLog);
		writeLogFile(pathLogNewDuplicateFiles, newDublicateFilesLog);



		//Util.createDirectoriesOfFile(pathLogNewEmptyFiles);
		//PrintStream outNewEmptyFiles = new PrintStream(new FileOutputStream(pathLogNewEmptyFiles));
		ArrayList<String> NewEmptyFilesLog = new ArrayList<String>();

		for(CollectorEntry collectorEntry:collectorMapProcessed.values()) {			
			try {
				Path source = Paths.get(collectorEntry.filename);
				String filename = source.toFile().getName();

				String targetRoot = PREPROCESSED_ERROR_PATH;
				switch(collectorEntry.status) {
				case "ok":
					targetRoot = Paths.get(PREPROCESSED_PATH).toString();
					filename = collectorEntry.createNewFilename();
					break;			
				case "empty":
					targetRoot = PREPROCESSED_EMPTY_PATH;
					NewEmptyFilesLog.add(collectorEntry.toCSVRow());
					break;
				case "no properties found in station":
					targetRoot = PREPROCESSED_NO_PLOT_PATH;
					break;
				case "removed":
					targetRoot = PREPROCESSED_REMOVED_PATH;
					break;
				default:
					//nothing: targetRoot --> PREPROCESSED_ERROR_PATH
				}

				Path target = Paths.get(targetRoot, filename);
				CopyOption options = StandardCopyOption.COPY_ATTRIBUTES;
				Util.createDirectoriesOfFile(target.toString());
				Files.copy(source, target, options);
			} catch(Exception e) {
				log.error(e+"  "+collectorEntry.filename);
			}
		}

		writeLogFile(pathLogNewEmptyFiles, NewEmptyFilesLog);

		System.out.println("collectorMapBasis "+collectorMapBasis.size());
		System.out.println("md5MapBasis "+md5MapBasis.size());
		System.out.println("collectorMapToAdd1 "+collectorMapToAdd.size());
		System.out.println("md5MapToAdd "+md5MapToAdd.size());
		System.out.println("collectorMapProcessed "+collectorMapProcessed.size());

		System.out.println("...finished");
	}

	public Map<String, CollectorEntry> readDirectory_with_stations_flat(Path root) {
		log.info("load directory with directories of files:      "+root);		
		TreeMap<String,Path> ascCollectorMap = new TreeMap<String,Path>();		
		try {
			DirectoryStream<Path> stream = Files.newDirectoryStream(root);
			for(Path subPath:stream) {
				if(Files.isDirectory(subPath)) {
					timeseriesloaderKiLi.readOneDirectory_structure_kili(subPath, ascCollectorMap,false);
				} else {
					log.warn("file in root directory: "+subPath+"   of   "+root);
				}
			}
			stream.close();
		} catch (IOException e) {
			log.error(e);
		}
		return readWithAscCollectorMap(ascCollectorMap);
	}


	public Map<String, CollectorEntry> readDirectory_with_asc_recursive(Path root) {
		log.info("load directory of files recursive:      "+root);		
		TreeMap<String,Path> ascCollectorMap = new TreeMap<String,Path>();
		readDirectory_with_asc_recursive_internal(root,ascCollectorMap);
		return readWithAscCollectorMap(ascCollectorMap);
	}

	private void readDirectory_with_asc_recursive_internal(Path root, TreeMap<String,Path> ascCollectorMap) {
		try {
			readDirectory_with_asc_files(root, ascCollectorMap);
		} catch (Exception e) {
			log.error(e);
		}

		try {
			DirectoryStream<Path> stream = Files.newDirectoryStream(root);
			for(Path subPath:stream) {
				if(Files.isDirectory(subPath)) {
					readDirectory_with_asc_recursive_internal(subPath,ascCollectorMap);
				} 
			}
			stream.close();
		} catch (IOException e) {
			log.error(e);
		}		
	}

	private void readDirectory_with_asc_files(Path directory_path, TreeMap<String,Path> ascCollectorMap) {
		try {
			if(Files.exists(directory_path)) {
				DirectoryStream<Path> stream = Files.newDirectoryStream(directory_path);
				//log.info("read directory of files:    "+directory_path);
				for(Path path:stream) {
					if(!Files.isDirectory(path)) {
						String filename = path.getName(path.getNameCount()-1).toString();
						int ascIndex = filename.toLowerCase().indexOf(".asc");
						if(ascIndex!=-1) {	
							String fileKey = filename.substring(0, ascIndex);						
							if(!ascCollectorMap.containsKey(fileKey)) {
								ascCollectorMap.put(fileKey, path);		
							} else {
								log.error("file key already present: "+fileKey+"     "+ascCollectorMap.get(fileKey)+"   "+path);
							}										
						} else {
							int binIndex = filename.toLowerCase().indexOf(".bin");
							if(binIndex<0) {
								log.warn("no asc file: "+filename);
							}
						}
					}
				}
				stream.close();
			} else {
				log.warn("directory not found: "+directory_path);
			}
		} catch (IOException e) {
			log.error(e);
		}		
	}

	private static String getMD5(Path filename) {
		try {
			MessageDigest md5 = MessageDigest.getInstance("md5");
			byte[] bytes = Files.readAllBytes(filename);
			byte[] checksum = md5.digest(bytes);
			return DatatypeConverter.printHexBinary(checksum);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public Map<String, CollectorEntry> readWithAscCollectorMap(TreeMap<String,Path> ascCollectorMap) {
		Map<String, CollectorEntry> collectorMap = new TreeMap<String,CollectorEntry>();
		String currentInfoPrefix = "";

		for(Entry<String, Path> ascMapEntry:ascCollectorMap.entrySet()) {

			String infoFilename = ascMapEntry.getKey();
			Path ascPath = ascMapEntry.getValue();

			CollectorEntry collectorEntry = new CollectorEntry(ascPath.toString());
			if(!collectorMap.containsKey(ascPath.toString())) {
				collectorMap.put(ascPath.toString(),collectorEntry);
			} else {
				log.error("error duplicate filename");
			}

			collectorEntry.md5 = getMD5(ascPath);

			try {
				collectorEntry.filesize = ascPath.toFile().length();
			} catch(Exception e) {
				log.error(e);
			}


			try {
				String infoKeyPrefix = infoFilename;
				if(infoKeyPrefix.length()>18) {
					infoKeyPrefix = infoKeyPrefix.substring(0, 18);
				}
				if(!currentInfoPrefix.equals(infoKeyPrefix)) {
					log.info("read files of prefix   "+infoKeyPrefix);
					currentInfoPrefix = infoKeyPrefix;
				}
			} catch(Exception e) {
				log.warn(e);
			}			

			try {
				TimestampSeries timestampseries = AscParser.parse(ascPath);
				if(timestampseries==null) {
					log.error("read error in "+infoFilename);
					collectorEntry.status = "read error";
					continue;
				}

				collectorEntry.station = timestampseries.name;

				if(timestampseries.entryList.isEmpty()) {
					log.info("empty timestampseries in  "+infoFilename);
					collectorEntry.status = "empty";
					continue;
				}

				collectorEntry.firstTimestamp = (int) timestampseries.getFirstTimestamp();
				collectorEntry.lastTimestamp =  (int) timestampseries.getLastTimestamp();

				Station station = tsdb.getStation(timestampseries.name);
				if(station==null) {
					log.error("station not found "+timestampseries.name+"   in  "+ascPath);
					collectorEntry.status = "station not found";
					continue;
				}

				String[] translatedInputSchema = new String[timestampseries.sensorNames.length];
				for(int i=0;i<timestampseries.sensorNames.length;i++) {
					translatedInputSchema[i] = station.translateInputSensorName(timestampseries.sensorNames[i], false);
				}

				StationProperties properties = station.getProperties(timestampseries.getFirstTimestamp(), timestampseries.getLastTimestamp());

				if(properties==null) {
					log.error("no properties found in station "+timestampseries.name+"  of  "+TimeUtil.oleMinutesToText(timestampseries.getFirstTimestamp())+" - "+TimeUtil.oleMinutesToText(timestampseries.getLastTimestamp())+"  in  "+ascPath);
					collectorEntry.status = "no properties found in station";
					continue;
				}

				collectorEntry.plot = properties.get_plotid();
				collectorEntry.logger = properties.get_logger_type_name();

				collectorEntry.status = "ok";

			} catch (Exception e) {
				collectorEntry.status = "error";
				log.error(e+"  in  "+infoFilename);
			}
		}

		return collectorMap;
	}

	private static TreeMap<String, List<CollectorEntry>> createMD5Map(Map<String, CollectorEntry> collectorMap) {
		TreeMap<String, List<CollectorEntry>> md5Map = new TreeMap<String,List<CollectorEntry>>();
		for(CollectorEntry collectorEntry : collectorMap.values()) {
			String key = collectorEntry.md5;
			if(key==null) {
				key = "unknown";
			}
			List<CollectorEntry> entryList = md5Map.get(key);
			if(entryList==null) {
				entryList = new ArrayList<CollectorEntry>();
				md5Map.put(key,entryList);
			}
			entryList.add(collectorEntry);
		}
		return md5Map;
	}

}
