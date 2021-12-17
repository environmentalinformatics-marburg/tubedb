package tsdb.testing;


import org.tinylog.Logger;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.TsDBFactory;

public class TestingMask {
	

	public static void main(String[] args) {
		try(TsDB tsdb = TsDBFactory.createDefault()) {

			Station station = tsdb.getStation("SEG03");
			

		}
	}

}
