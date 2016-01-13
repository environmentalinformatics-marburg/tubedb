package tsdb.graph.node;

import static tsdb.util.AssumptionCheck.throwNull;
import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.graph.source.StationRawSource;
import tsdb.graph.source.VirtualRawSource;

/**
 * Interface Node: raw source
 * @author woellauer
 *
 */
public interface RawSource extends Node {	
	
	public static RawSource of(TsDB tsdb, String plotID, String[] querySchema) {		
		VirtualPlot virtualPlot = tsdb.getVirtualPlot(plotID);
		if(virtualPlot!=null) {
			return VirtualRawSource.of(tsdb, virtualPlot, querySchema);
		}
		Station station = tsdb.getStation(plotID);
		if(station!=null) {
			return StationRawSource.of(tsdb, station, querySchema);
		}else {
			throw new RuntimeException("plotID / stationID not found: "+plotID);
		}	
		
	}
	
	public abstract class Abstract implements RawSource {		
		protected TsDB tsdb; // not null		
		protected Abstract(TsDB tsdb) {
			throwNull(tsdb);
			this.tsdb = tsdb;
		}
		
		@Override
		public final boolean isContinuous() {
			return false;
		}

		@Override
		public final boolean isConstantTimestep() {
			return false;
		}
	}

}
