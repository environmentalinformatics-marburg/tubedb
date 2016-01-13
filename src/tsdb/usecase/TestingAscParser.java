package tsdb.usecase;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDBFactory;
import tsdb.loader.ki.AscParser;
import tsdb.loader.ki.TimeSeriesLoaderKiLi;

public class TestingAscParser {

	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) {

		TreeSet<String> excludes = TimeSeriesLoaderKiLi.getExcludes();		
		ArrayList<String> excludePrefixes = TimeSeriesLoaderKiLi.getExcludePrefixes();		
		
		try {
			DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(TsDBFactory.SOURCE_KI_TSM_PATH));
			for(Path subPath:stream) {
				if(Files.isDirectory(subPath)) {
					//loadOneDirectory_structure_kili(subPath, ascCollectorMap);


					DirectoryStream<Path> subStream = Files.newDirectoryStream(subPath);
					//System.out.println("*** load directory: "+subPath+" ***");
					for(Path ascPath:subStream) {
						String filename = ascPath.getName(ascPath.getNameCount()-1).toString();
						boolean excluded = excludes.contains(filename);
						if(!excluded) {
							for(String prefix:excludePrefixes) {
								if(filename.startsWith(prefix)) {
									excluded = true;
									break;
								}
							}
						}
						if(!excluded) {
							int ascIndex = filename.indexOf(".asc");
							if(ascIndex!=-1) {						
								//String fileKey = filename.substring(0, ascIndex);						
								AscParser.parse(ascPath);
							} else {
								log.warn("no asc file: "+filename);
							}
						}
					}
					subStream.close();




				} else {
					log.warn("file in root directory: "+subPath);
				}
			}
			stream.close();
		} catch (IOException e) {
			log.error(e);
		}


	}

}
