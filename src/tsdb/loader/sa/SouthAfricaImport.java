package tsdb.loader.sa;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.loader.tsa.TsaImport;

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
			TsaImport.readDirectoryRecursive(tsdb,root);
		} catch (Exception e) {
			log.error(e);
		}		
	}
}