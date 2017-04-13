package tsdb.testing;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.TsDBFactory;

public class TestingMask {
	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) {
		try(TsDB tsdb = TsDBFactory.createDefault()) {

			Station station = tsdb.getStation("SEG03");
			

		}
	}

}
