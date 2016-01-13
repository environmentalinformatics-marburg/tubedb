package tsdb.run;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDBFactory;
import tsdb.streamdb.StreamDB;

/**
 * Compacts the database.
 * @author woellauer
 *
 */
public class RunCompact {
	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) {
		System.out.println("start compacting streamDB...");
		
		StreamDB streamDB = new StreamDB(TsDBFactory.STORAGE_PATH+"/"+"streamdb");

		try {		
			streamDB.compact();		
		} catch(Exception e) {
			log.error(e);
		}		

		streamDB.close();
		
		System.out.println("...finished compacting streamDB");
	}
}
