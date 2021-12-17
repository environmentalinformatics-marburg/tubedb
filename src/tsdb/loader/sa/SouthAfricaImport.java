package tsdb.loader.sa;

import java.nio.file.Path;
import java.nio.file.Paths;


import org.tinylog.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.loader.tsa.TsaImport;

/**
 * Reads pre-imported ".tsa"-files and loads content into TsDB
 * @author woellauer
 *
 */
public class SouthAfricaImport {
	

	public static void main(String[] args) {

		TsDB tsdb = TsDBFactory.createDefault();

		System.out.println("start...");

		/*try {
			DirectoryStream<Path> ds = Files.newDirectoryStream(Paths.get("C:/timeseriesdatabase_source/sa/south_africa_saws_acs"));
			for(Path filepath:ds) {
				if(Files.isDirectory(filepath)) {
					Logger.info("read directory "+filepath);
				}
				Logger.info("read "+filepath);
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
			TsaImport.readDirectoryRecursive(tsdb,root);
		} catch (Exception e) {
			Logger.error(e);
		}		
	}
}