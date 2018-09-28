package tsdb.testing;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Paths;

import tsdb.loader.be.UDBFTimestampSeries;
import tsdb.loader.be.UniversalDataBinFile;
import tsdb.util.TimeUtil;
import tsdb.util.TsEntry;
import tsdb.util.iterator.TsIterator;

public class ConvertUDBFtoCSV {

	public static void main(String[] args) throws IOException {
		UniversalDataBinFile udbf = new UniversalDataBinFile(Paths.get("AEW20_20110616_^b0_0003.dat"));
		UDBFTimestampSeries s = udbf.getUDBFTimeSeries();
		TsIterator it = s.toTsIterator();
		PrintStream out = System.out;
		out.print("datetime");
		for(int i=0; i<it.getNames().length; i++) {
			out.print(',');
			out.print(it.getNames()[i]);
		}
		out.println("");
		while(it.hasNext()) {
			TsEntry e = it.next();
			out.print(TimeUtil.fastDateTimeWrite(TimeUtil.oleMinutesToLocalDateTime(e.timestamp)));
			float[] data = e.data;
			int len = data.length;
			for(int i=0; i<len; i++) {
				out.print(',');
				out.print(data[i]);
			}
			out.println();

		}
	}

}
