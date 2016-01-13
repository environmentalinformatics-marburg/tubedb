package tsdb.loader.ki;

import static tsdb.util.AssumptionCheck.throwNull;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.Station;
import tsdb.StationProperties;
import tsdb.TsDB;
import tsdb.component.SourceEntry;
import tsdb.loader.ki.type.AbstractLoader;
import tsdb.loader.ki.type.LoaderFactory;
import tsdb.util.DataRow;
import tsdb.util.TimeUtil;
import tsdb.util.iterator.TimestampSeries;

/**
 * This class contains methods to read time series from input files in "KiLi"-Format and stores data into database.
 * @author woellauer
 *
 */
public class TimeSeriesLoaderKiLi {

	private static final Logger log = LogManager.getLogger();

	private final TsDB tsdb; //not null

	private final TreeSet<String> excludes;
	private final ArrayList<String> excludePrefixes;

	public TimeSeriesLoaderKiLi(TsDB tsdb) {
		throwNull(tsdb);
		this.tsdb = tsdb;
		this.excludes = getExcludes();
		this.excludePrefixes = getExcludePrefixes();
	}

	/**
	 * load load directory with directories of files: structure of tsm
	 * @param root
	 */
	public void loadDirectory_with_stations_flat(Path root, boolean checkExcluded) {
		log.info("load directory with directories of files:      "+root);		
		TreeMap<String,Path> ascCollectorMap = new TreeMap<String,Path>();		
		try {
			DirectoryStream<Path> stream = Files.newDirectoryStream(root);
			for(Path subPath:stream) {
				if(Files.isDirectory(subPath)) {
					readOneDirectory_structure_kili(subPath, ascCollectorMap, checkExcluded);
				} else {
					log.warn("file in root directory: "+subPath+"   of   "+root);
				}
			}
			stream.close();
		} catch (IOException e) {
			log.error(e);
		}
		//loadWithAscCollectorMap(ascCollectorMap);
		loadWithAscCollectorMapNewParser(ascCollectorMap);
	}

	/**
	 * Collects all kili-Files from one directory and adds them to ascCollectorMap.
	 * @param kiliPath
	 * @param ascCollectorMap
	 */
	public void readOneDirectory_structure_kili(Path kiliPath, TreeMap<String, Path> ascCollectorMap, boolean checkExcluded) {
		try {
			if(Files.exists(kiliPath)) {
				DirectoryStream<Path> stream = Files.newDirectoryStream(kiliPath);
				//log.info("read directory of files:    "+kiliPath);
				for(Path path:stream) {
					String filename = path.getName(path.getNameCount()-1).toString();
					int ascIndex = filename.toLowerCase().indexOf(".asc");
					if(ascIndex!=-1) {
						boolean excluded = false;
						if(checkExcluded) {
							excluded = excludes.contains(filename);
							if(!excluded) {
								for(String prefix:excludePrefixes) {
									if(filename.startsWith(prefix)) {
										excluded = true;
										break;
									}
								}
							}
						}
						if(!excluded) {
							String fileKey = filename.substring(0, ascIndex);
							if(ascCollectorMap.containsKey(fileKey)) {
								log.warn("file already inserted in map "+fileKey+"   "+ascCollectorMap.get(fileKey)+"   "+path);
							}
							ascCollectorMap.putIfAbsent(fileKey, path);
						}
					} else {
						log.warn("no asc file: "+filename);
					}
				}
				stream.close();
			} else {
				log.warn("directory not found: "+kiliPath);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void loadWithAscCollectorMapNewParser(TreeMap<String,Path> ascCollectorMap) {
		String currentInfoPrefix = "";

		for(Entry<String, Path> ascMapEntry:ascCollectorMap.entrySet()) {
			String infoFilename = ascMapEntry.getKey();
			Path ascPath = ascMapEntry.getValue();

			try {
				String infoKeyPrefix = infoFilename;
				if(infoKeyPrefix.length()>18) {
					infoKeyPrefix = infoKeyPrefix.substring(0, 18);
				}
				if(!currentInfoPrefix.equals(infoKeyPrefix)) {
					log.info("load files of prefix   "+infoKeyPrefix);
					currentInfoPrefix = infoKeyPrefix;
				}
			} catch(Exception e) {
				log.warn(e);
			}			

			try {
				TimestampSeries timestampseries = AscParser.parse(ascPath);
				if(timestampseries==null) {
					log.error("read error in "+infoFilename);
					continue;
				}
				if(timestampseries.entryList.isEmpty()) {
					log.info("empty timestampseries in  "+infoFilename);
					continue;
				}				

				Station station = tsdb.getStation(timestampseries.name);
				if(station==null) {
					log.error("station not found "+timestampseries.name+"   in  "+ascPath);
					continue;
				}

				String[] translatedInputSchema = new String[timestampseries.sensorNames.length];
				for(int i=0;i<timestampseries.sensorNames.length;i++) {
					translatedInputSchema[i] = station.translateInputSensorName(timestampseries.sensorNames[i], false);
				}

				StationProperties properties = station.getProperties(timestampseries.getFirstTimestamp(), timestampseries.getLastTimestamp());

				if(properties==null) {
					log.error("no properties found in station "+timestampseries.name+"  of  "+TimeUtil.oleMinutesToText(timestampseries.getFirstTimestamp())+" - "+TimeUtil.oleMinutesToText(timestampseries.getLastTimestamp())+"  in  "+ascPath);
					continue;
				}

				insertTimestampseries(station, properties, timestampseries, translatedInputSchema, ascPath);
			} catch (Exception e) {
				e.printStackTrace();
				log.error(e+"  in  "+infoFilename);
			}
		}
	}

	public void insertTimestampseries(Station station, StationProperties properties, TimestampSeries timestampSeries, String[] translatedInputSchema, Path ascPath) {
		AbstractLoader loader = LoaderFactory.createLoader(station.loggerType.typeName, translatedInputSchema, properties, ascPath.toString());
		if(loader==null) {
			log.error("no loader found for  "+station.stationID);
			return;
		}
		List<DataRow> eventList = loader.load(station, station.loggerType.sensorNames, timestampSeries);
		if(eventList==null) {
			log.error("no entries for "+station.stationID+"   in   "+ascPath);
			return;
		}
		tsdb.streamStorage.insertDataRows(timestampSeries.name, eventList, timestampSeries.getFirstTimestamp(), timestampSeries.getLastTimestamp(), station.loggerType.sensorNames);
		String[] resultSchema = loader.getResultSchema();

		tsdb.sourceCatalog.insert(SourceEntry.of(timestampSeries, ascPath, resultSchema));
	}

	public static TreeSet<String> getExcludes() {
		TreeSet<String> excludes = new TreeSet<String>();

		excludes.add("ki_0000cof1_rug_201012280800_201012291118_mez_ra01_nas02_0000.asc"); // Messung in 2 Sekunden Intervallen
		excludes.add("ki_0000hom1_rug_201012220000_201012230318_mez_ra01_nas02_0000.asc"); // Messung in 2 Sekunden Intervallen
		excludes.add("ki_0000hom2_rug_201012270800_201012281118_mez_ra01_nas02_0000.asc"); // Messung in 2 Sekunden Intervallen
		excludes.add("ki_0000mai2_rug_201203031500_201203061115_mez_ra01_nas05_0000.asc"); // Messung in 2 Sekunden Intervallen
		excludes.add("ki_0000sav1_rug_201101040800_201101051118_mez_ra01_nas02_0000.asc"); // Messung in 2 Sekunden Intervallen
		excludes.add("ki_000gra1b_rug_201012200000_201012210318_mez_ra01_nas02_0000.asc"); // Messung in 2 Sekunden Intervallen
		excludes.add("ki_xx000000_xxx_201210151540_201210151541_mez_ra01_nas02_0000.asc"); // Messung in 2 Sekunden Intervallen
		excludes.add("ki_xx000000_xxx_201210151543_201210151544_mez_ra01_nas02_0000.asc"); // Messung in 2 Sekunden Intervallen
		excludes.add("ki_xx000000_xxx_201301251456_201301251503_mez_ra01_nas02_0000.asc"); // Messung in 2 Sekunden Intervallen
		excludes.add("ki_xx000000_xxx_201301301442_201301301501_mez_ra01_nas02_0000.asc"); // Messung in 2 Sekunden Intervallen
		excludes.add("ki_xx000000_xxx_201301301442_201301301508_mez_ra01_nas02_0000.asc"); // Messung in 2 Sekunden Intervallen
		excludes.add("ki_xx00hom4_xxx_201101040800_201101051118_mez_ra01_nas02_0000.asc"); // Messung in 2 Sekunden Intervallen
		excludes.add("rug_cofeins_201102171212.asc"); // Messung in 2 Sekunden Intervallen
		excludes.add("rug_graeins_201102101010.asc"); // Messung in 2 Sekunden Intervallen
		excludes.add("rug_hom4_201103231902.asc"); // Messung in 2 Sekunden Intervallen
		excludes.add("rug_homeins_201102111504.asc");  // Messung in 2 Sekunden Intervallen
		excludes.add("rug_homzwei_201102221010.asc");  // Messung in 2 Sekunden Intervallen
		excludes.add("rug_saveins_201103031017.asc");  // Messung in 2 Sekunden Intervallen
		excludes.add("DK320-20456_5-5-2012_13-02-43.asc");  // Messung in 2 Sekunden Intervallen

		excludes.add("DK341-25287_5-5-2012_13-16-40.asc");  // Messung in 3 Sekunden Intervallen

		excludes.add("ki_0000sav0_000wxt_201301311200_201302011406_mez_ra01_nas05_0000.asc");  // Messung in 5 Sekunden Intervallen
		excludes.add("gra0_rug_DK320-20219_19-9-2012_14-21-00.asc"); // Messung in 5 Sekunden Intervallen
		excludes.add("ki_xx000000_rug_201004011330_201004011612_mez_ra01_nas05_0000.asc"); // Messung in 5 Sekunden Intervallen
		excludes.add("ki_xx00hom5_xxx_201104181205_201104210820_mez_ra01_nas05_0000.asc"); // Messung in 5 Sekunden Intervallen
		excludes.add("ki_xx00mai2_xxx_201101062212_201102191836_mez_ra01_nai12_0000.asc"); // Messung in 5 Sekunden Intervallen
		excludes.add("rug_hom5_201107021531.asc"); // Messung in 5 Sekunden Intervallen

		excludes.add("gra0_rug_DK320-20219_19-9-2012_14-22-24.asc");  // Messung in 10 Sekunden Intervallen
		excludes.add("DK390-25302_30_09_2012_17-41-45.asc");  // Messung in 10 Sekunden Intervallen

		excludes.add("ki_xx000000_rug_201003301400_201004010746_mez_ra01_nas15_0000.asc");  // Messung in 15 Sekunden Intervallen

		excludes.add("ki_xx00hom5_xxx_201101062212_201102191836_mez_ra01_nai12_0000.asc"); // Messung in 12 Minuten Intervallen
		excludes.add("ki_xx00mai2_xxx_201101062212_201102191836_mez_ra01_nai12_0000.asc"); // Messung in 12 Minuten Intervallen
		excludes.add("rug_hom5_201102221452.asc"); // Messung in 12 Minuten Intervallen
		excludes.add("rug_mai2_201102171200.asc"); // Messung in 12 Minuten Intervallen

		excludes.add("DK320-20192_5-5-2012_12-41-51.asc"); //timestamp<=prevTimestamp 2011-11-06T01:00
		excludes.add("DK320-20193_5-5-2012_12-31-56.asc"); //timestamp<=prevTimestamp 2011-11-06T01:00

		excludes.add("DK320-20184_5-5-2012_13-13-35.asc"); //no data
		excludes.add("DK320-20185_01_10_2012_09-01-25.asc"); //no data
		excludes.add("fpd5_rug______________dk320-20197_20.09.2012_09-31-53.asc"); //no data
		excludes.add("gra2_rug_____dk320-20185_04_09_2012_09-48-43.asc"); //no data
		excludes.add("gra2_rug_____dk320-20185_11.07.2012_09-52-51.asc"); //no data
		excludes.add("gra2_rug_____dk320-20185_15_08_2012_09-21-00.asc"); //no data
		excludes.add("gra2_rug_____dk320-20185_28.05.2012_12-22-07.asc"); //no data
		excludes.add("hom3_rug___________________dk320-20165_30.05.2012_12-05-06.asc"); //no data

		excludes.add("DK320-20165_26_09_2012_14-56-47.asc"); //no station 51021038
		excludes.add("ki_xx000000_xxx_201108220620_201111080055_mez_ra01_nai05_0000.asc"); //no station 51021038
		excludes.add("ki_xx000000_xxx_201110191030_201201161825_mez_ra01_nai05_0000.asc"); //no station 51021038
		excludes.add("ki_xx000000_xxx_201111081110_201201112145_mez_ra01_nai05_0000.asc"); //no station 51021038

		excludes.add("DK320-20208_26_09_2012_10-10-54.asc"); //no station 51021056

		excludes.add("DK320-20231_5-5-2012_13-03-56.asc"); // no station 510210309

		excludes.add("DK320-20472_26_09_2012_11-34-00.asc");// no station 51071138

		excludes.add("DK320-20474_24_09_2012_09-48-43.asc");// no station 51071154

		excludes.add("DK341-25290_01_10_2012_11-23-35.asc");// no station 80081055

		excludes.add("DK390-25299_24_09_2012_09-39-12.asc");// no station 80091054
		excludes.add("DK390-25301_24_09_2012_09-32-41.asc");// no station 80091054
		excludes.add("DK390-25303_24_09_2012_09-29-10.asc");// no station 80091054
		excludes.add("DK390-25304_24_09_2012_09-23-45.asc");// no station 80091054
		excludes.add("DK390-25305_01_10_2012_10-29-05.asc");// no station 80091054
		excludes.add("DK390-25309_24_09_2012_09-19-51.asc");// no station 80091054

		excludes.add("DK390-25300_01_10_2012_11-08-11.asc");// no station 80091055

		excludes.add("DK390-25306_01_10_2012_16-17-50.asc");// no station 80091038

		excludes.add("ki_xx000000_xxx_201108171000_201111241245_mez_ra01_nai05_0000.asc"); // no station 51021020161

		excludes.add("ki_xx000000_xxx_201108220620_201111080055_mez_ra01_nai05_0000.asc"); // no station 51021038
		excludes.add("ki_xx000000_xxx_201108220620_201111080055_mez_ra01_nai05_0000.asc"); // no station 51021038
		excludes.add("ki_xx000000_xxx_201110191030_201201161825_mez_ra01_nai05_0000.asc"); // no station 51021038
		excludes.add("ki_xx000000_xxx_201111081110_201201112145_mez_ra01_nai05_0000.asc"); // no station 51021038

		excludes.add("ki_xx00cof4_xxx_201106210930_201107050405_mez_ra01_nai05_0000.asc"); // no station 51021020230
		excludes.add("ki_xx00hom5_xxx_201102230000_201103072115_mez_ra01_nai05_0000.asc"); // no station 51021020230
		excludes.add("rug_cof4_201107051257.asc"); // no station 51021020230
		excludes.add("rug_hom5_201103081200.asc"); // no station 51021020230

		return excludes;
	}

	public static ArrayList<String> getExcludePrefixes() {
		ArrayList<String> excludePrefixes = new ArrayList<String>();

		excludePrefixes.add("ki_0000flm1_000tfi"); // "wrong" tfi
		excludePrefixes.add("ki_0000foc6_000tfi"); // "wrong" tfi
		excludePrefixes.add("ki_0000hom4_000tfi"); // "wrong" tfi
		excludePrefixes.add("ki_0000nkw1_000tfi"); // "wrong" tfi
		excludePrefixes.add("ki_0000sav5_000tfi"); // "wrong" tfi
		excludePrefixes.add("ki_0000sav0_000tfi"); // "wrong" tfi

		return excludePrefixes;
	}
}
