package tsdb.graph.source;

import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.graph.node.Base;
import tsdb.graph.node.NodeGen;

public final class BaseFactory {
	
	private BaseFactory(){}
	
	public static Base of(TsDB tsdb, String plotID, String[] querySchema, NodeGen stationGen) {
		VirtualPlot virtualPlot = tsdb.getVirtualPlot(plotID);
		if(virtualPlot!=null) {
			return VirtualBase.of(tsdb, virtualPlot, querySchema, stationGen);
		}
		Station station = tsdb.getStation(plotID);
		if(station!=null) {
			return StationBase.of(tsdb, station, querySchema, stationGen);
		}else {
			throw new RuntimeException("plotID / stationID not found: "+plotID);
		}	
	}

}
