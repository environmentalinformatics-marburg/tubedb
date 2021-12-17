package tsdb.run;


import org.tinylog.Logger;

import tsdb.TsDBFactory;
import tsdb.streamdb.StreamDB;

/**
 * Compacts the database.
 * @author woellauer
 *
 */
public class RunCompact {
	

	public static void main(String[] args) {
		System.out.println("start compacting streamDB...");
		
		StreamDB streamDB = new StreamDB(TsDBFactory.STORAGE_PATH+"/"+"streamdb");

		try {
			streamDB.compact();		
		} catch(Exception e) {
			Logger.error(e);
		}		

		streamDB.close();
		
		System.out.println("...finished compacting streamDB");
	}
}
