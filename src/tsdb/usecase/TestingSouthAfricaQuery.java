package tsdb.usecase;


import org.tinylog.Logger;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.streamdb.StreamIterator;
import tsdb.util.DataEntry;

public class TestingSouthAfricaQuery {
	@SuppressWarnings("unused")
	

	public static void main(String[] args) {

		TsDB tsdb = TsDBFactory.createDefault();

		System.out.println("start...");
		
		StreamIterator it = tsdb.streamStorage.getRawSensorIterator("ALIWAL-NORTH PLAATKOP", "Ta_200", null, null);
		while(it.hasNext()) {
			DataEntry e = it.next();
			System.out.println(e);
		}


		System.out.println("...end");
		
		tsdb.close();
	}

}
