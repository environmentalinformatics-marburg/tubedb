package tsdb.loader.tsa;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;


import org.tinylog.Logger;

import tsdb.TsDB;
import tsdb.util.TimeSeriesArchivReader;

public class TsaImport {
	

	public static void readDirectoryRecursive(TsDB tsdb, Path rootDirectory) {
		try {
			DirectoryStream<Path> ds = Files.newDirectoryStream(rootDirectory);
			for(Path subPath:ds) {
				if(Files.isDirectory(subPath)) {
					Logger.info("read directory "+subPath);
					readDirectoryRecursive(tsdb,subPath);
				} else {
					Logger.info("read file "+subPath);
					readOneFile(tsdb, subPath);
				}
			}
		} catch (Exception e) {
			Logger.error(e);
		}
	}

	public static void readOneFile(TsDB tsdb, Path filepath) {
		try {
			TimeSeriesArchivReader.importStationsFromFile(tsdb, filepath.toString());
		} catch (Exception e) {
			e.printStackTrace();
			Logger.error(e);
		}
	}
}