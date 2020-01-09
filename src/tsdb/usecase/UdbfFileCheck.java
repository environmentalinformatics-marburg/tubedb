package tsdb.usecase;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.loader.be.UDBFTimestampSeries;
import tsdb.loader.be.UniversalDataBinFile;
import tsdb.util.iterator.TimestampSeries;

public class UdbfFileCheck {
	private static final Logger log = LogManager.getLogger();

	public static void main(String[] args) throws IOException {
		String filename = "c:/temp2/jig302/20140212_^b0_0225.dat";
		UniversalDataBinFile udbf = new UniversalDataBinFile(Paths.get(filename));
		
		log.info(udbf);
		
		UDBFTimestampSeries udbfts = udbf.getUDBFTimeSeries();
		
		log.info(udbfts);
		
		TimestampSeries ts = udbfts.toTimestampSeries();
		
		log.info(ts);

	}

}
