package tsdb.testing;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;


import org.tinylog.Logger;

import tsdb.loader.be.UDBFTimestampSeries;
import tsdb.loader.be.UniversalDataBinFile;
import tsdb.loader.be.UniversalDataBinFile.DataRow;

public class Testing_UDB_read {
	

	public static void main(String[] args) throws IOException {
		Path fileName1 = Paths.get("C:/temp2/HW10/HEW10_20110610_^b0_0015.dat");
		Path fileName2 = Paths.get("C:/temp2/HW10/HW10^b0_0058_udb.dat");
		Path[] filenames = new Path[]{fileName1, fileName2};

		for(Path filename:filenames) {
			System.out.println(" *** ");
			System.out.println(filename);
			UniversalDataBinFile udbf = new UniversalDataBinFile(filename);
			System.out.println(udbf.toString());

			DataRow[] datarows = udbf.readDataRows();
			
			System.out.println("len "+datarows.length);
			Logger.info(datarows[0]);
			Logger.info(datarows[1]);
			Logger.info(datarows[2]);

			UDBFTimestampSeries series = udbf.getUDBFTimeSeries();
			System.out.println(series);
			
			series.toTsIterator().writeConsole(10);


		}

	}

}
