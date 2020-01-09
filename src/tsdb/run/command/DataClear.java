package tsdb.run.command;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;

public class DataClear {
	private static final Logger log = LogManager.getLogger();
	
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
		log.info("clear database");
		tsdb.clear();
	}
}
