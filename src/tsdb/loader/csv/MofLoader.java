package tsdb.loader.csv;

import java.nio.file.Path;
import java.time.LocalDateTime;


import org.tinylog.Logger;

import tsdb.TsDB;
import tsdb.util.AbstractTable;
import tsdb.util.TimeUtil;

public class MofLoader extends ImportGenericCSV {
	
	
	public MofLoader(TsDB tsdb) {
		super(tsdb);
	}

	@Override
	protected String parseStationName(Path filePath) {
		String filename = filePath.getFileName().toString();

		int postFixIndex = filename.indexOf('-'); //filename with station name and postfix

		if(postFixIndex<0) {
			postFixIndex = filename.indexOf('.'); //filename with station name and without postfix
		}

		if(postFixIndex<1) {
			throw new RuntimeException("could not get station name from file name: "+filename);
		}
		String stationName = filename.substring(0, postFixIndex);
		Logger.info("parsed station name: |" + stationName + "|");
		return stationName;
	}

	@Override
	protected int parseTimestamp(String timestampText) {		
		LocalDateTime datetime = LocalDateTime.parse(timestampText, TimeUtil.DATE_TIME_FORMATER_MOF);		
		return (int) TimeUtil.dateTimeToOleMinutes(datetime);
	}
	
	@Override
	protected int getDatetimeIndex(AbstractTable table) {
		return table.getColumnIndex("Time (s)");
	}
}
