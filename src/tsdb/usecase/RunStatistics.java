package tsdb.usecase;

import tsdb.TsDBFactory;
import tsdb.streamdb.StreamDB;

public class RunStatistics {

	public static void main(String[] args) {
		StreamDB streamDB = new StreamDB(TsDBFactory.STORAGE_PATH+"/"+"streamdb");
		streamDB.printStatistics();
		streamDB.close();

	}

}
