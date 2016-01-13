package tsdb.usecase;

import java.io.IOException;
import java.nio.file.Paths;

import tsdb.TsDBFactory;
import tsdb.loader.be.UDBFTimestampSeries;
import tsdb.loader.be.UniversalDataBinFile;
import tsdb.util.TimeUtil;

public class DatFileReading {

	public static void main(String[] args) throws IOException {
		//UniversalDataBinFile udbf = new UniversalDataBinFile(Paths.get(TsDBFactory.SOURCE_BE_TSM_PATH,"AEW32","20140709_^b0_0007.dat"));
		UniversalDataBinFile udbf = new UniversalDataBinFile(Paths.get(TsDBFactory.SOURCE_BE_TSM_PATH,"AEW33","20140903_^b1_0008.dat"));
		
		UDBFTimestampSeries ts = udbf.getUDBFTimeSeries();
		
		for (int i = 0; i < ts.time.length; i++) {
			System.out.println(TimeUtil.oleMinutesToText(ts.time[i])+"   "+ts.data[i][0]);
		}
	
		System.out.println("size: "+ts.time.length);
		System.out.println(TimeUtil.oleMinutesToText(ts.time[0]));
		System.out.println(TimeUtil.oleMinutesToText(ts.time[ts.time.length-1]));

		/*DataRow[] rows = udbf.readDataRows();

		Integer prev=null;
		for(DataRow row:rows) {
			int cur = row.id;
			if(prev!=null&&prev+1!=cur) {
				System.out.println(row.id+"    "+Arrays.toString(row.data));
			}
			prev = cur;
		}*/

	}

}
