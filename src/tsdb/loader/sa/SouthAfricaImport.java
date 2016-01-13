package tsdb.loader.sa;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TimeSeriesArchivReader;
import tsdb.TsDB;
import tsdb.TsDBFactory;

/**
 * Reads pre-imported ".tsa"-files and loads content into TsDB
 * @author woellauer
 *
 */
public class SouthAfricaImport {

	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) {

		TsDB tsdb = TsDBFactory.createDefault();

		System.out.println("start...");

		/*try {
			DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get("C:/timeseriesdatabase_source/sa/south_africa_saws_acs"));
			for(Path filepath:ds) {
				if(Files.isDirectory(filepath)) {
					log.info("read directory "+filepath);
				}
				log.info("read "+filepath);
				readOneFile(tsdb, filepath);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}*/

		new SouthAfricaImport(tsdb);

		System.out.println("...end");
		tsdb.close();

	}

	public SouthAfricaImport(TsDB tsdb) {
		try {
			Path root = Paths.get(TsDBFactory.SOURCE_SA_DAT_PATH);
			readDirectoryRecursive(tsdb,root);
		} catch (Exception e) {
			log.error(e);
		}		
	}

	public static void readDirectoryRecursive(TsDB tsdb, Path rootDirectory) {
		try {
			DirectoryStream<Path> ds = Files.newDirectoryStream(rootDirectory);
			for(Path subPath:ds) {
				if(Files.isDirectory(subPath)) {
					log.info("read directory "+subPath);
					readDirectoryRecursive(tsdb,subPath);
				} else {
					log.info("read file "+subPath);
					readOneFile(tsdb, subPath);
				}
			}
		} catch (Exception e) {
			log.error(e);
		}
	}

	public static void readOneFile(TsDB tsdb, Path filepath) {
		try {
			/*TimestampSeries tss = TimestampSeries.readFromBinaryFile(filepath.toString());
			tsdb.streamStorage.insertTimestampSeries(tss);*/
			TimeSeriesArchivReader.importStationsFromFile(tsdb, filepath.toString());
		} catch (Exception e) {
			log.error(e);
		}
	}

}