package tsdb.usecase;

import java.io.IOException;
import java.nio.file.Paths;


import org.tinylog.Logger;

import tsdb.loader.be.UDBFTimestampSeries;
import tsdb.loader.be.UniversalDataBinFile;
import tsdb.util.iterator.TimestampSeries;

public class UdbfFileCheck {
	

	public static void main(String[] args) throws IOException {
		String filename = "c:/temp2/jig302/20140212_^b0_0225.dat";
		UniversalDataBinFile udbf = new UniversalDataBinFile(Paths.get(filename));
		
		Logger.info(udbf);
		
		UDBFTimestampSeries udbfts = udbf.getUDBFTimeSeries();
		
		Logger.info(udbfts);
		
		TimestampSeries ts = udbfts.toTimestampSeries();
		
		Logger.info(ts);

	}

}
