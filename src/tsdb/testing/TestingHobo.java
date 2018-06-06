package tsdb.testing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.loader.burgwald.HoboLoader;

public class TestingHobo {
	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) throws FileNotFoundException, IOException {
		//String filename = "c:/timeseriesdatabase_source/burgwald/20160722_Daten_csv/TB_340.csv";
		String root = "c:/timeseriesdatabase_source/burgwald";
		
		//HoboTable hobotable = new HoboTable(filename);
		
		try(TsDB tsdb = TsDBFactory.createDefault()) {
			HoboLoader hoboLoader = new HoboLoader(tsdb);
			
			Set<String> plotIDs = new TreeSet<String>();
			hoboLoader.collectPlotsRecursive(Paths.get(root), plotIDs);
			System.out.println("plot,general,lat,lon,elevation,focal,is_station,logger");
			for(String plotID:plotIDs) {
				System.out.println(plotID+",BURGWALD,,,,N,Y,Hobo");
			}
			
			
			//hoboLoader.loadFile(Paths.get(filename));
			//hoboLoader.loadDirectoryRecursive(Paths.get(root));
		}
		

	}

}
