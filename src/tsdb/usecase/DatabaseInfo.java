package tsdb.usecase;

import tsdb.StationProperties;
import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.VirtualPlot;
import tsdb.util.TimeUtil;
import tsdb.util.TimestampInterval;

/**
 * print some info of KiLi meta data
 * @author woellauer
 *
 */
public class DatabaseInfo {

	public static void main(String[] args) {
		System.out.println("start...");
		TsDB timeSeriesDatabase = TsDBFactory.createDefault();
		//timeSeriesDatabase.registerStreams();
		
		
		for(VirtualPlot virtualplot:timeSeriesDatabase.getVirtualPlots()) {
			System.out.println("******\t"+virtualplot.plotID+"   ("+virtualplot.generalStation.name+")\t***");
			for(TimestampInterval<StationProperties> interval:virtualplot.intervalList) {
				String startText = TimeUtil.oleMinutesToText(interval.start);
				String endText = TimeUtil.oleMinutesToText(interval.end);
				String typeText = tsdb.util.Util.ifnull(interval.value.get_logger_type_name(), x->x, ()->"unknown");
				System.out.println(startText+" - "+endText+"\t\tstream name: "+interval.value.get_plotid()+"\t"+typeText);
			}
			System.out.println();
			System.out.println();
			virtualplot.getStationList(null, null, null);
			System.out.println();
			System.out.println();
		}
		
		
	}

}
