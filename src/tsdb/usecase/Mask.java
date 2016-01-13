package tsdb.usecase;

import tsdb.TsDB;
import tsdb.TsDBFactory;
import tsdb.graph.QueryPlan;
import tsdb.graph.node.Node;
import tsdb.util.AggregationInterval;
import tsdb.util.DataQuality;
import tsdb.util.TimeSeriesMask;

public class Mask {

	public static void main(String[] args) {
		TsDB tsdb = TsDBFactory.createDefault();
		
		/*String plotid = "AEG01";
		String sensorName = "Albedo";
		
		TimeSeriesMask mask = tsdb.streamStorage.getTimeSeriesMask(plotid, sensorName);
		if(mask==null) {
			mask = new TimeSeriesMask();
		}
		
		System.out.println("mask "+mask);
		
		int start = (int) TimeUtil.ofDateStartHour(2013, 3);
		int end = (int) TimeUtil.ofDateEndHour(2013, 3);
		mask.addInterval(Interval.of(start, end));
		
		tsdb.streamStorage.setTimeSeriesMask(plotid, sensorName, mask);*/
		
		String plotid = "HEG19";
		String sensorName = "P_container_NRT";
		TimeSeriesMask mask = tsdb.streamStorage.getTimeSeriesMask(plotid, sensorName);
		System.out.println(mask);
		
		Node node = QueryPlan.plot(tsdb, plotid, new String[]{sensorName}, AggregationInterval.DAY, DataQuality.EMPIRICAL, false);
		node.writeConsole(null, null);
	}

}
