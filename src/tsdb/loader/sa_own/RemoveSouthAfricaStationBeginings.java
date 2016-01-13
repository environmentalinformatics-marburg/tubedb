package tsdb.loader.sa_own;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.StationProperties;
import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.VirtualPlot;
import tsdb.util.TimeUtil;
import tsdb.util.TimestampInterval;

public class RemoveSouthAfricaStationBeginings {
	private static final Logger log = LogManager.getLogger();

	/**
Station 1: bis einschließlich 4.3. löschen
Station 2: bis einschließlich 5.3. löschen
Station 3: bis einschließlich 6.3. löschen
Station 4: bis einschließlich 7.3. löschen
Station 5: bis einschließlich 10.3. löschen
Station 6: bis einschließlich 11.3. löschen
Station 7: bis einschließlich 12.3. löschen
Station 8: bis einschließlich 13.3. löschen
Station 9: bis einschließlich 15.3. löschen
Station 10: bis einschließlich 20.3. löschen
Station 11: bis einschließlich 21.3. löschen
Station 12: bis einschließlich 22.3. löschen
Station 13: bis einschließlich 24.3. löschen
Station 14: bis einschließlich 25.3. löschen
Station 15: bis einschließlich 26.3. löschen
	 */
	public static void run(TsDB tsdb) {
		final int startTime = 0;

		Object[][] days = new Object[][]{
				{"SA01",4},
				{"SA02",5},
				{"SA03",6},
				{"SA04",7},
				{"SA05",10},
				{"SA06",11},
				{"SA07",12},
				{"SA08",13},
				{"SA09",15},
				{"SA10",20},
				{"SA11",21},
				{"SA12",22},
				{"SA13",24},
				{"SA14",25},
				{"SA15",26},
		};

		for(Object[] day:days) {
			String plotID = (String) day[0];
			int endTime = (int) TimeUtil.dateTimeToOleMinutes(LocalDateTime.of(2014, 3, ((Number)day[1]).intValue(), 23, 59));
			log.info("remove  "+plotID+"  up to  "+endTime+"  "+TimeUtil.oleMinutesToText(endTime));
			VirtualPlot virtualPlot = tsdb.getVirtualPlot(plotID);
			for(TimestampInterval<StationProperties> entry:virtualPlot.intervalList) {
				tsdb.streamStorage.removeInterval(entry.value.get_serial(), startTime, endTime);
			}
		}
		
		
	}

	public static void main(String[] args) {
		TsDB tsdb = TsDBFactory.createDefault();
		run(tsdb);
		tsdb.close();
	}

}
