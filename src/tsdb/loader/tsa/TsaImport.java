package tsdb.loader.tsa;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDB;
import tsdb.util.TimeSeriesArchivReader;

public class TsaImport {
	private static final Logger log = LogManager.getLogger();

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
			TimeSeriesArchivReader.importStationsFromFile(tsdb, filepath.toString());
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}
	}
}