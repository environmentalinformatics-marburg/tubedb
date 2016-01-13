package tsdb;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ini4j.Profile.Section;
import org.ini4j.Wini;

import tsdb.util.Util;

/**
 * Factory for TimeSeriesDatabase.
 * Creates tsdb object and loads tsdb config data from config files.
 * @author woellauer
 */
public final class TsDBFactory {
	private static final Logger log = LogManager.getLogger();

	private static final String PATH_CONFIG_FILENAME = "tsdb_paths.ini";
	private static final String TSDB_PATH_SECTION = "tsdb_paths"; 


	public static String CONFIG_PATH = "config";
	public static String STORAGE_PATH = "storage";

	public static String SOURCE_BE_TSM_PATH = "source/be_tsm";
	public static String SOURCE_BE_GEN_PATH = "source/be_gen";
	public static String SOURCE_KI_TSM_PATH = "source/ki_tsm";
	public static String SOURCE_KI_TFI_PATH = "source/ki_tfi";
	public static String SOURCE_SA_DAT_PATH = "source/sa_dat";
	public static String SOURCE_SA_OWN_PATH = "source/sa_own";
	public static String SOURCE_MM_PATH = "source/mm";
	public static String SOURCE_BA_PATH = "source/ba";
	public static String SOURCE_BA_REF_PATH = "source/ba_ref";	

	public static String WEBCONTENT_PATH = "webcontent";
	public static String WEBDOWNLOAD_PATH = "webDownload";
	public static String WEBFILES_PATH = "webFiles";

	public static String OUTPUT_PATH = "output";

	public static String WEB_SERVER_PREFIX_BASE_URL = ""; //no prefix
	//private static final String WEB_SERVER_PREFIX_BASE_URL = "/0123456789abcdef"; //example prefix
	public static int WEB_SERVER_PORT = 8080;
	public static boolean WEB_SERVER_LOGIN = false;
	public static boolean WEB_SERVER_HTTPS = false;
	public static String WEB_SERVER_HTTPS_KEY_STORE_PASSWORD = "password";

	public static String JUST_ONE_REGION = null;
	//public static String JUST_ONE_REGION = "BE";
	//public static String JUST_ONE_REGION = "KI";
	//public static String JUST_ONE_REGION = "SA";

	public static boolean HIDE_INTENAL_SENSORS = true;
	
	private TsDBFactory(){}

	static {
		initPaths();
	}

	/**
	 * If entry is in path config file read it else set to default.
	 */
	private static void initPaths() {
		try {
			Wini ini;
			if(Files.exists(Paths.get(PATH_CONFIG_FILENAME))) {
				log.trace("read from root: "+PATH_CONFIG_FILENAME);
				ini = new Wini(new File(PATH_CONFIG_FILENAME));
			} else if(Files.exists(Paths.get(CONFIG_PATH,PATH_CONFIG_FILENAME))) {
				log.trace("read from config: "+PATH_CONFIG_FILENAME);
				ini = new Wini(new File(CONFIG_PATH,PATH_CONFIG_FILENAME));
			} else {
				log.trace("no "+PATH_CONFIG_FILENAME);
				return;
			}
			Section section = ini.get(TSDB_PATH_SECTION);
			if(section==null) {
				log.warn("no "+TSDB_PATH_SECTION+" section in "+ini.getFile());
				return;
			}
			Map<String, String> pathMap = Util.readIniSectionMap(section);
			CONFIG_PATH = getString(pathMap, "CONFIG_PATH", CONFIG_PATH);
			STORAGE_PATH = getString(pathMap, "STORAGE_PATH", STORAGE_PATH);
			SOURCE_BE_TSM_PATH = getString(pathMap, "SOURCE_BE_TSM_PATH", SOURCE_BE_TSM_PATH);
			SOURCE_BE_GEN_PATH = getString(pathMap, "SOURCE_BE_GEN_PATH", SOURCE_BE_GEN_PATH);
			SOURCE_KI_TSM_PATH = getString(pathMap, "SOURCE_KI_TSM_PATH", SOURCE_KI_TSM_PATH);
			SOURCE_KI_TFI_PATH = getString(pathMap, "SOURCE_KI_TFI_PATH", SOURCE_KI_TFI_PATH);
			SOURCE_SA_DAT_PATH = getString(pathMap, "SOURCE_SA_DAT_PATH", SOURCE_SA_DAT_PATH);
			SOURCE_SA_OWN_PATH = getString(pathMap, "SOURCE_SA_OWN_PATH", SOURCE_SA_OWN_PATH);
			SOURCE_MM_PATH = getString(pathMap, "SOURCE_MM_PATH", SOURCE_MM_PATH);
			SOURCE_BA_PATH = getString(pathMap, "SOURCE_BA_PATH", SOURCE_BA_PATH);
			SOURCE_BA_REF_PATH = getString(pathMap, "SOURCE_BA_REF_PATH", SOURCE_BA_REF_PATH);			
			WEBCONTENT_PATH = getString(pathMap, "WEBCONTENT_PATH", WEBCONTENT_PATH);
			WEBDOWNLOAD_PATH = getString(pathMap, "WEBDOWNLOAD_PATH", WEBDOWNLOAD_PATH);
			WEBFILES_PATH = getString(pathMap, "WEBFILES_PATH", WEBFILES_PATH);
			OUTPUT_PATH = getString(pathMap, "OUTPUT_PATH", OUTPUT_PATH);

			WEB_SERVER_PREFIX_BASE_URL = getString(pathMap, "WEB_SERVER_PREFIX_BASE_URL", WEB_SERVER_PREFIX_BASE_URL);
			WEB_SERVER_PORT = getInt(pathMap, "WEB_SERVER_PORT", WEB_SERVER_PORT);
			WEB_SERVER_LOGIN = getBoolean(pathMap,"WEB_SERVER_LOGIN", WEB_SERVER_LOGIN);
			HIDE_INTENAL_SENSORS = getBoolean(pathMap,"HIDE_INTENAL_SENSORS",HIDE_INTENAL_SENSORS);
			JUST_ONE_REGION = getString(pathMap, "JUST_ONE_REGION", JUST_ONE_REGION);			
			
			WEB_SERVER_HTTPS = getBoolean(pathMap,"WEB_SERVER_HTTPS",WEB_SERVER_HTTPS);
			WEB_SERVER_HTTPS_KEY_STORE_PASSWORD = getString(pathMap, "WEB_SERVER_HTTPS_KEY_STORE_PASSWORD", WEB_SERVER_HTTPS_KEY_STORE_PASSWORD);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}

	/**
	 * Read boolean parameter from ini file. If it not exists set to default.
	 * @param map map of ini-file-section
	 * @param key parameter
	 * @param defaultValue default
	 * @return resulting value
	 */
	private static boolean getBoolean(Map<String, String> map, String key, boolean defaultValue) {
		String valueText = map.get(key);
		if(valueText==null) {
			return defaultValue;
		}
		if(valueText.toLowerCase().trim().equals("true")) {
			return true;
		}
		if(valueText.toLowerCase().trim().equals("false")) {
			return false;
		}
		log.warn("tsdb ini config value for "+key+" unknown: "+valueText);		
		return defaultValue;
	}

	/**
	 * Read String parameter from ini file. If it not exists set to default.
	 * @param map map of ini-file-section
	 * @param key parameter
	 * @param defaultValue default
	 * @return resulting value
	 */
	private static String getString(Map<String, String> map, String key, String defaultValue) {
		String valueText = map.get(key);
		if(valueText==null) {
			return defaultValue;
		}
		if(valueText.trim().isEmpty()) {
			log.warn("tsdb ini config value for "+key+" empty: ");
			return defaultValue;
		}
		return valueText;	
	}

	private static int getInt(Map<String, String> map, String key, int defaultValue) {
		String valueText = map.get(key);
		if(valueText==null) {
			return defaultValue;
		}
		if(valueText.trim().isEmpty()) {
			log.warn("tsdb ini config value for "+key+" empty: ");
			return defaultValue;
		}
		try{
			return Integer.parseInt(valueText);
		} catch(Exception e) {
			log.error("int not read for "+key+": "+valueText+" || "+e);
			return defaultValue;
		}	
	}

	public static TsDB createDefault() {
		return createDefault(STORAGE_PATH+"/",CONFIG_PATH,STORAGE_PATH+"/",STORAGE_PATH+"/streamdb");
	}

	public static TsDB createDefault(String databaseDirectory,String configPath, String cacheDirectory, String streamdbPathPrefix) {
		String configDirectory = configPath+"/";
		try {
			TsDB tsdb = new TsDB(databaseDirectory, cacheDirectory, streamdbPathPrefix);
			ConfigLoader configLoader = new ConfigLoader(tsdb);

			if(JUST_ONE_REGION==null||JUST_ONE_REGION.toUpperCase().equals("BE")) { //*** BE
				configLoader.readRegion(configDirectory+"be/be_region.ini");
				configLoader.readLoggerTypeSchema(configDirectory+"be/be_logger_type_schema.ini"); // [create LOGGER_TYPE] read schema of logger types and create: logger type objects, sensor objects (if new)
				configLoader.readGeneralStation(configDirectory+"be/be_general_station.ini"); // [create GENERAL_STATION]
				configLoader.readStation(configDirectory+"be/be_station_inventory.csv"); // [create STATION] read station list, generate general station name and properties and create station objects
				configLoader.readStationGeoPosition(configDirectory+"be/be_station_master.csv"); // read geo position of stations and insert into existing stations
				configLoader.readSensorTranslation(configDirectory+"be/be_sensor_translation.ini"); // read sensor translation and insert it into existing logger types, general stations and stations
			}

			if(JUST_ONE_REGION==null||JUST_ONE_REGION.toUpperCase().equals("KI")) { //*** KI
				configLoader.readRegion(configDirectory+"ki/ki_region.ini");
				configLoader.readLoggerTypeSchema(configDirectory+"ki/ki_logger_type_schema.ini"); // [create LOGGER_TYPE] read schema of logger types and create: logger type objects, sensor objects (if new)
				configLoader.readGeneralStation(configDirectory+"ki/ki_general_stations.ini"); // [create GENERAL_STATION]
				configLoader.readVirtualPlot(configDirectory+"ki/ki_station_master.csv"); // [create VIRTUAL_PLOT]
				configLoader.readVirtualPlotGeoPosition(configDirectory+"ki/ki_plot_position.csv"); // read geo position of virtual plots and insert into existing virtual plots
				configLoader.readVirtualPlotElevation(configDirectory+"ki/ki_plot_elevation.csv"); // read elevation of virtual plots and insert into existing virtual plots
				configLoader.readKiStation(configDirectory+"ki/ki_station_inventory.csv"); // [create STATION] read time interval of stations and insert it in existing virtual plots			
				configLoader.readSensorTranslation(configDirectory+"ki/ki_sensor_translation.ini"); // read sensor translation and insert it into existing logger types, general stations and stations
			}

			if(JUST_ONE_REGION==null||JUST_ONE_REGION.toUpperCase().equals("SA")) {  //*** SA
				configLoader.readRegion(configDirectory+"sa/sa_region.ini");
				configLoader.readLoggerTypeSchema(configDirectory+"sa/sa_logger_type_schema.ini"); // [create LOGGER_TYPE] read schema of logger types and create: logger type objects, sensor objects (if new)
				configLoader.readGeneralStation(configDirectory+"sa/sa_general_stations.ini"); // [create GENERAL_STATION]
				configLoader.readSaStation(configDirectory+"sa/sa_station_inventory.csv"); //[create STATION] read station with geo position
			}

			if(JUST_ONE_REGION==null||JUST_ONE_REGION.toUpperCase().equals("SA_OWN")) {  //*** SA_OWN
				String prefix = configDirectory+"sa_own/";

				configLoader.readRegion(prefix+"sa_own_region.ini");
				configLoader.readLoggerTypeSchema(prefix+"sa_own_logger_type_schema.ini"); // [create LOGGER_TYPE] read schema of logger types and create: logger type objects, sensor objects (if new)
				configLoader.readGeneralStation(prefix+"sa_own_general_stations.ini"); // [create GENERAL_STATION]
				configLoader.readSaOwnPlotInventory(prefix+"sa_own_plot_inventory.csv");
				configLoader.readGenericStationInventory(prefix+"sa_own_station_inventory.csv");
				configLoader.readSensorTranslation(prefix+"sa_own_sensor_translation.ini"); // read sensor translation and insert it into existing logger types, general stations and stations				
			}
			
			if(JUST_ONE_REGION==null||JUST_ONE_REGION.toUpperCase().equals("MM")) {  //*** MM
				String prefix = configDirectory+"mm/";

				configLoader.readRegion(prefix+"mm_region.ini");
				configLoader.readLoggerTypeSchema(prefix+"mm_logger_type_schema.ini"); // [create LOGGER_TYPE] read schema of logger types and create: logger type objects, sensor objects (if new)
				configLoader.readGeneralStation(prefix+"mm_general_stations.ini"); // [create GENERAL_STATION]
				configLoader.readSaOwnPlotInventory(prefix+"mm_plot_inventory.csv");
				configLoader.readGenericStationInventory(prefix+"mm_station_inventory.csv");
				configLoader.readSensorTranslation(prefix+"mm_sensor_translation.ini"); // read sensor translation and insert it into existing logger types, general stations and stations				
			}
			
			if(JUST_ONE_REGION==null||JUST_ONE_REGION.toUpperCase().equals("BA")) {  //*** BA
				String prefix = configDirectory+"ba/";

				configLoader.readRegion(prefix+"ba_region.ini");
				configLoader.readLoggerTypeSchema(prefix+"ba_logger_type_schema.ini"); // [create LOGGER_TYPE] read schema of logger types and create: logger type objects, sensor objects (if new)
				configLoader.readGeneralStation(prefix+"ba_general_stations.ini"); // [create GENERAL_STATION]
				configLoader.readBaPlotInventory(prefix+"ba_plot_inventory.csv");
				configLoader.readGenericStationInventory(prefix+"ba_station_inventory.csv");
				configLoader.readSensorTranslation(prefix+"ba_sensor_translation.ini"); // read sensor translation and insert it into existing logger types, general stations and stations				
			}

			//*** global config start
			configLoader.readBaseAggregationConfig(configDirectory+"global_sensor_aggregation.ini"); // read and insert type of aggregation to sensor objects
			configLoader.readIgnoreSensorName(configDirectory+"global_sensor_ignore.ini"); // read and insert sensor names that should be not inserted in database
			configLoader.readSensorPhysicalRangeConfig(configDirectory+"global_sensor_physical_range.ini"); // read and insert physical range to sensor objects
			configLoader.readSensorStepRangeConfig(configDirectory+"global_sensor_step_range.ini"); // read and insert step range to sensor objects			
			configLoader.readInterpolationSensorNameConfig(configDirectory+"global_sensor_interpolation.ini"); // read list of sensor names for interpolation and mark sensor objects
			configLoader.readEmpiricalDiffConfig(configDirectory+"global_sensor_empirical_diff.ini"); // read empirical max diff and insert it in sensor objects
			configLoader.readSensorDescriptionConfig(configDirectory+"global_sensor_description.ini");
			configLoader.readSensorUnitConfig(configDirectory+"global_sensor_unit.ini");
			configLoader.readSensorCategoryConfig(configDirectory+"global_sensor_category.ini");
			configLoader.readSensorInternalConfig(configDirectory+"global_sensor_internal.ini");
			//*** global config end

			//*** calc additional data start
			tsdb.updateGeneralStations();
			configLoader.calcNearestStations();
			configLoader.calcNearestVirtualPlots();
			//*** calc additional data end

			return tsdb;		
		} catch (Exception e) {
			e.printStackTrace();
			log.error("create TimeSeriesDatabase"+e);
			return null;
		}		
	}

	public static String get_CSV_output_directory() {		
		return OUTPUT_PATH+"/";
	}
}
