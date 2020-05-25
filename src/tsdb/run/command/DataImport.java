package tsdb.run.command;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ini4j.Config;
import org.ini4j.InvalidFileFormatException;
import org.ini4j.Profile.Section;
import org.ini4j.Wini;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.component.Region;
import tsdb.loader.bale.TOA5Loader;
import tsdb.loader.be.TimeSeriesLoaderBE;
import tsdb.loader.burgwald.HoboLoader;
import tsdb.loader.csv.ImportGenericCSV;
import tsdb.loader.csv.MofLoader;
import tsdb.loader.influx.InfluxLoader;
import tsdb.loader.influx.InfluxLoaderConfig;
import tsdb.loader.ki.TimeSeriesLoaderKiLi;
import tsdb.loader.ki.TimeSeriesLoaderKiLi_manual_tfi;
import tsdb.loader.mm.ImportGenericASC;
import tsdb.loader.sa_own.ImportSaOwn;
import tsdb.loader.sa_own.RemoveSouthAfricaStationBeginings;
import tsdb.loader.treetalker.Loader_TreeTalker;
import tsdb.loader.tsa.TsaImport;
import tsdb.util.Interval;
import tsdb.util.TimeUtil;

public class DataImport {
	private static final Logger log = LogManager.getLogger();

	private final TsDB tsdb;

	public static void main(String[] args) {
		try(TsDB tsdb = TsDBFactory.createDefault()) {
			DataImport dataImport = new DataImport(tsdb);
			dataImport.run("import.ini");
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}
	}

	public DataImport(TsDB tsdb) {
		this.tsdb = tsdb;
	}	

	public void run(String configFile) throws InvalidFileFormatException, IOException {
		Wini ini = new Wini();
		Config iniConfig = new Config();
		iniConfig.setMultiOption(true); // multiple keys of same name
		iniConfig.setMultiSection(false); // multiple sections of same name are merged to one section
		ini.setConfig(iniConfig);
		ini.load(new File(configFile));

		for(Section section:ini.values()) {
			//log.info("section "+section);
			String regionName = section.getName();
			Region region = tsdb.getRegion(regionName);
			if(region!=null) {
				log.info("import "+region.name);
				for(String key:section.keySet()) {
					for(String value:section.getAll(key)) {
						importPath(region,key,value);
					}
				}
			} else {
				log.warn("region not found, not imported: "+regionName);
			}
		}
	}

	private void importPath(Region region, String type, String path) {
		Path rootDirectory = Paths.get(path);
		if(rootDirectory.toFile().exists()) {
			switch(type.trim().toLowerCase()) {
			case "udbf_be": {
				Interval range = region.viewTimeRange;
				long minTimestamp = range==null?TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(2008, 01, 01, 00, 00)):range.start;
				TimeSeriesLoaderBE timeseriesloaderBE = new TimeSeriesLoaderBE(tsdb, minTimestamp);
				timeseriesloaderBE.loadDirectory_with_stations_flat(rootDirectory);
				break;
			}
			case "csv": {
				new ImportGenericCSV(tsdb).load(rootDirectory);
				break;
			}
			case "csv_tfi": {
				TimeSeriesLoaderKiLi_manual_tfi TimeSerieaLoaderKiLi_manual_tfi = new TimeSeriesLoaderKiLi_manual_tfi(tsdb);
				TimeSerieaLoaderKiLi_manual_tfi.loadOneDirectory_structure_kili_tfi(rootDirectory);
				break;
			}
			case "csv_hobo": {
				new HoboLoader(tsdb).loadDirectoryRecursive(rootDirectory);
				break;
			}
			case "asc": {
				new ImportGenericASC(tsdb).load(rootDirectory);
				break;
			}
			case "asc_ki": {
				TimeSeriesLoaderKiLi timeseriesloaderKiLi = new TimeSeriesLoaderKiLi(tsdb);
				timeseriesloaderKiLi.loadDirectory_with_stations_recursive(rootDirectory, true, 2*60); // time offset of +2 hours (MEZ to EAT conversation)
				break;
			}
			case "asc_sa_own": {
				new ImportSaOwn(tsdb).load(rootDirectory);
				try {
					log.info("*remove South Africa Own Stations first measure days*");
					RemoveSouthAfricaStationBeginings.run(tsdb);
				} catch (Exception e) {
					log.warn(e);
				}
				break;
			}
			case "tsa": {
				TsaImport.readDirectoryRecursive(tsdb, rootDirectory);
				break;
			}
			case "toa5": {
				new TOA5Loader(tsdb).loadDirectoryRecursive(rootDirectory);
				break;
			}
			case "influx": {
				InfluxLoaderConfig config = new InfluxLoaderConfig(rootDirectory);
				new InfluxLoader(tsdb).load(config);
				break;
			}
			case "mof": {
				new MofLoader(tsdb).load(rootDirectory);
				break;
			}
			case "treetalker": {
				new Loader_TreeTalker(tsdb).loadDirectoryRecursive(rootDirectory);
				break;
			}
			default:
				log.error("unknown import type: "+type+" for "+region.name+" in "+path);
			}
		} else {
			log.warn("path not found, not imported: "+type+" for "+region.name+" in "+path);
		}
	}

}
