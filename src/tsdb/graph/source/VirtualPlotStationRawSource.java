package tsdb.graph.source;

import static tsdb.util.AssumptionCheck.throwNulls;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.Station;
import tsdb.StationProperties;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.graph.node.RawSource;
import tsdb.streamdb.RelationalIterator;
import tsdb.streamdb.StreamIterator;
import tsdb.streamdb.StreamTsIterator;
import tsdb.util.TimestampInterval;
import tsdb.util.Util;
import tsdb.util.iterator.TsIterator;

/**
 * This node creates raw values from one station source filtered by a virtual plot interval.
 * @author woellauer
 *
 */
public class VirtualPlotStationRawSource extends RawSource.Abstract {
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger();

	private final VirtualPlot virtualPlot; // not null
	private final Station station; // not null
	private final String[] sensorNames; //not null, schema valid

	VirtualPlotStationRawSource(TsDB tsdb, VirtualPlot virtualPlot, Station station, String[] sensorNames) {
		super(tsdb);
		throwNulls(virtualPlot, station,sensorNames);
		this.virtualPlot = virtualPlot;
		this.station = station;
		this.sensorNames = sensorNames;
		if(this.sensorNames.length==0) {
			throw new RuntimeException("no schema");
		}
		if(!station.isValidSchema(sensorNames)) { // no virtual sensors in raw !
			throw new RuntimeException("not valid schema: "+Util.arrayToString(sensorNames)+" in "+Util.arrayToString(station.getSchema())); 
		}
	}

	public static VirtualPlotStationRawSource of(TsDB tsdb, String plotID, String stationID, String[] querySchema) {
		VirtualPlot virtualPlot = tsdb.getVirtualPlot(plotID);
		if(virtualPlot==null) {
			throw new RuntimeException("virtual plot not found: "+plotID);
		}
		return of(tsdb,virtualPlot,stationID,querySchema);
	}

	public static VirtualPlotStationRawSource of(TsDB tsdb, VirtualPlot virtualPlot, String stationID, String[] querySchema) {
		Station station = tsdb.getStation(stationID);
		if(station==null) {
			throw new RuntimeException("station not found: "+stationID);
		}
		return of(tsdb,virtualPlot,station,querySchema);
	}


	public static VirtualPlotStationRawSource of(TsDB tsdb, VirtualPlot virtualPlot, Station station, String[] querySchema) {	
		if(querySchema==null) {
			querySchema = station.getSchema();
		}
		return new VirtualPlotStationRawSource(tsdb, virtualPlot, station, querySchema);
	}

	@Override
	public TsIterator get(Long start, Long end) {
		List<StreamIterator> processing_iteratorList = new ArrayList<StreamIterator>();	
		for(TimestampInterval<StationProperties> interval:virtualPlot.intervalList) {
			if(station.stationID.equals(interval.value.get_serial())) {				
				String[] stationSchema = tsdb.getValidSchema(station.stationID, sensorNames);
				TimestampInterval<StationProperties> filteredInterval = interval.filterByInterval(start, end);
				if(filteredInterval!=null) {
					for(String sensorName:stationSchema) {
						StreamIterator it = tsdb.streamStorage.getRawSensorIterator(station.stationID, sensorName, filteredInterval.start, filteredInterval.end);
						if(it!=null&&it.hasNext()) {
							processing_iteratorList.add(it);
						}
					}
				}
			}
		}

		if(processing_iteratorList.isEmpty()) {
			return null;
		}
		if(processing_iteratorList.size()==1) {
			return new StreamTsIterator(processing_iteratorList.get(0));
		}
		return new RelationalIterator(processing_iteratorList, sensorNames);
	}

	@Override
	public Station getSourceStation() {
		return station;
	}

	@Override
	public String[] getSchema() {
		return sensorNames;
	}

	@Override
	public VirtualPlot getSourceVirtualPlot() {
		return virtualPlot;
	}
	
	@Override
	public long[] getTimestampInterval() {
		return virtualPlot.getTimestampInterval();
	}
}
