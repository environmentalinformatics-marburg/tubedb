package tsdb.run.command;


import org.tinylog.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;

public class DataClear {
	
	
	private final TsDB tsdb;

	public static void main(String[] args) {
		try(TsDB tsdb = TsDBFactory.createDefault()){
			new DataClear(tsdb).run();
		}
	}
	
	public DataClear(TsDB tsdb) {
		this.tsdb = tsdb;
	}
	
	public void run() {		
		Logger.info("clear database");
		tsdb.clear();
	}
}
