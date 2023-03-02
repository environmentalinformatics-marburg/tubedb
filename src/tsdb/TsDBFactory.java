package tsdb;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;


import org.tinylog.Logger;
import org.ini4j.Profile.Section;
import org.ini4j.Wini;

import tsdb.component.Region;
import tsdb.util.Util;

/**
 * Factory for TimeSeriesDatabase.
 * Creates tsdb object and loads tsdb config data from config files.
 * @author woellauer
 */
public final class TsDBFactory {


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
	public static int WEB_SERVER_HTTPS_PORT = 0;
	public static int WEB_SERVER_JWS_PORT = 0;
	public static String WEB_SERVER_JWS_CONFIG_FILENAME = "jws.yaml";

	public static String JUST_ONE_REGION = null;
	//public static String JUST_ONE_REGION = "BE";
	//public static String JUST_ONE_REGION = "KI";
	//public static String JUST_ONE_REGION = "SA";

	public static boolean HIDE_INTENAL_SENSORS = true;

	public static boolean IOT_API = false;
	public static String IOT_API_KEY = ""; //no key

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
				Logger.trace("read from root: "+PATH_CONFIG_FILENAME);
				ini = new Wini(new File(PATH_CONFIG_FILENAME));
			} else if(Files.exists(Paths.get(CONFIG_PATH,PATH_CONFIG_FILENAME))) {
				Logger.trace("read from config: "+PATH_CONFIG_FILENAME);
				ini = new Wini(new File(CONFIG_PATH,PATH_CONFIG_FILENAME));
			} else {
				Logger.trace("no "+PATH_CONFIG_FILENAME);
				return;
			}
			Section section = ini.get(TSDB_PATH_SECTION);
			if(section==null) {
				Logger.warn("no "+TSDB_PATH_SECTION+" section in "+ini.getFile());
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
			WEB_SERVER_HTTPS_PORT = getInt(pathMap, "WEB_SERVER_HTTPS_PORT", WEB_SERVER_HTTPS_PORT);
			WEB_SERVER_JWS_PORT = getInt(pathMap, "WEB_SERVER_JWS_PORT", WEB_SERVER_JWS_PORT);

			IOT_API = getBoolean(pathMap, "IOT_API", IOT_API);
			IOT_API_KEY = getString(pathMap, "IOT_API_KEY", IOT_API_KEY);
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
		Logger.warn("tsdb ini config value for "+key+" unknown: "+valueText);		
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
			Logger.warn("tsdb ini config value for "+key+" empty: ");
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
			Logger.warn("tsdb ini config value for "+key+" empty: ");
			return defaultValue;
		}
		try{
			return Integer.parseInt(valueText);
		} catch(Exception e) {
			Logger.error("int not read for "+key+": "+valueText+" || "+e);
			return defaultValue;
		}	
	}

	public static TsDB createDefault() {
		return createDefault(STORAGE_PATH+"/",CONFIG_PATH,STORAGE_PATH+"/",STORAGE_PATH+"/streamdb");
	}

	public static TsDB createDefault(String databaseDirectory,String configPath, String cacheDirectory, String streamdbPathPrefix) {
		String configDirectory = configPath + "/";

		try {
			TsDB tsdb = new TsDB(databaseDirectory, cacheDirectory, streamdbPathPrefix, configDirectory);
			ConfigLoader configLoader = new ConfigLoader(tsdb);			

			//*** global config start
			configLoader.readSensorMetaData(configDirectory + "sensors.yaml"); // read sensor meta data
			tsdb.createSensorDependencies();
			configLoader.readIgnoreSensorName(configDirectory + "sensor_ignore.ini"); // read and insert sensor names that should be not inserted in database
			//*** global config end			

			//*** region config start
			try(DirectoryStream<Path> paths = Files.newDirectoryStream(Paths.get(configDirectory), path->path.toFile().isDirectory())) {
				for(Path path : paths) {
					String dir = path.toString();
					//Logger.info("dir  "+path+"  "+path.getFileName());
					try {
						String regionFilePath = dir + "/region.ini";
						File regionFile = new File(regionFilePath);
						if(regionFile.exists()) {
							Region region = configLoader.readRegion(regionFilePath, JUST_ONE_REGION);
							if(region!=null) {
								configLoader.readGeneralStation(dir + "/general_stations.ini");
								configLoader.readLoggerTypeSchema(dir + "/logger_type_schema.ini");
								configLoader.readPlotInventory(dir + "/plot_inventory.csv");
								configLoader.readOptionalStationInventory(dir + "/station_inventory.csv"); // If all plots are stations then this file is not required.
								configLoader.readOptinalSensorTranslation(dir + "/sensor_translation.ini");
								configLoader.readOptionalSensorNameCorrection(dir + "/sensor_name_correction.json");  // read sensor translation and insert it into existing stations
								configLoader.readOptionalStationProperties(dir + "/station_properties.yaml");
							}
						} else {
							Logger.warn("no region config at  " + path + "    missing config file  " + regionFilePath);
						}
					} catch(Exception e) {
						e.printStackTrace();
						Logger.info("could not load meta data of  " + path + "  " + e);
					}
				}
			}
			//*** region config end			

			//*** calc additional data start
			tsdb.refresStationAliasMap();
			tsdb.updateGeneralStations();
			configLoader.calcNearestStations();
			configLoader.calcNearestVirtualPlots();
			//*** calc additional data end

			return tsdb;		
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error("create TsDB"+e);
			return null;
		}		
	}

	public static String get_CSV_output_directory() {		
		return OUTPUT_PATH+"/";
	}
}
