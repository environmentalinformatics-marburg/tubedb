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
import tsdb.graph.node.Base;
import tsdb.graph.node.Node;
import tsdb.graph.node.NodeGen;
import tsdb.iterator.BaseAggregationIterator;
import tsdb.iterator.MergeIterator;
import tsdb.util.TimestampInterval;
import tsdb.util.Util;
import tsdb.util.iterator.TsIterator;

/**
 * This node creates base aggregated values from one station source filtered by a virtual plot interval.
 * @author woellauer
 *
 */
public class VirtualPlotStationBase extends Base.Abstract  {
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger();

	private final VirtualPlot virtualPlot; // not null
	private final Node source;  // not null

	protected VirtualPlotStationBase(TsDB tsdb, VirtualPlot virtualPlot, Node source) {
		super(tsdb);
		throwNulls(virtualPlot, source);
		this.virtualPlot = virtualPlot;
		this.source = source;
	}

	public static VirtualPlotStationBase of(TsDB tsdb, String plotID, String stationID, String[] querySchema, NodeGen stationGen) {
		VirtualPlot virtualPlot = tsdb.getVirtualPlot(plotID);
		if(virtualPlot==null) {
			throw new RuntimeException("virtual plot not found: "+plotID);
		}
		return of(tsdb,virtualPlot,stationID,querySchema,stationGen);
	}

	public static VirtualPlotStationBase of(TsDB tsdb, VirtualPlot virtualPlot, String stationID, String[] querySchema, NodeGen stationGen) {
		Station station = tsdb.getStation(stationID);
		if(station==null) {
			throw new RuntimeException("station not found: "+stationID);
		}
		return of(tsdb,virtualPlot,station,querySchema,stationGen);
	}


	public static VirtualPlotStationBase of(TsDB tsdb, VirtualPlot virtualPlot, Station station, String[] querySchema, NodeGen stationGen) {	
		String[] rawSensorNames = tsdb.streamStorage.getSensorNames(station.stationID);
		rawSensorNames = tsdb.includeVirtualSensorNames(rawSensorNames);
		if(rawSensorNames==null || rawSensorNames.length==0) {
			return null;
		}		
		if(querySchema==null) {			
			querySchema = tsdb.getBaseSchema(rawSensorNames);
			if(querySchema==null || querySchema.length==0) {
				return null;
			}
		} else {
			if(querySchema.length==0) {
				return null;
			}
			if(!tsdb.isBaseSchema(querySchema)) {
				throw new RuntimeException("no base schema");
			}
			if(!Util.isContained(querySchema, rawSensorNames)) {
				throw new RuntimeException("not valid schema");
			}
		}	

		Node source = stationGen.get(station.stationID, querySchema);
		return new VirtualPlotStationBase(tsdb, virtualPlot, source);
	}

	@Override
	public TsIterator get(Long start, Long end) {
		String stationID = source.getSourceStation().stationID;
		List<TsIterator> processing_iteratorList = new ArrayList<TsIterator>();	
		for(TimestampInterval<StationProperties> i:virtualPlot.intervalList) {
			if(stationID.equals(i.value.get_serial())) {
				TimestampInterval<StationProperties> interval = i.filterByInterval(start, end);
				if(interval!=null) {
					TsIterator input_iterator = source.get(interval.start, interval.end);
					if(input_iterator!=null&&input_iterator.hasNext()) {
						processing_iteratorList.add(input_iterator);
					}
				}

			}
		}

		if(processing_iteratorList.isEmpty()) {
			return null;
		}
		if(processing_iteratorList.size()==1) {
			BaseAggregationIterator base_iterator = new BaseAggregationIterator(tsdb, processing_iteratorList.get(0));
			if(!base_iterator.hasNext()) {
				return null;
			}
			return base_iterator;
		}
		
		MergeIterator mergeIterator = new MergeIterator(source.getSchema(), processing_iteratorList, "VirtualPlotStationBase:"+stationID);
		BaseAggregationIterator base_iterator = new BaseAggregationIterator(tsdb, mergeIterator);
		if(!base_iterator.hasNext()) {
			return null;
		}
		return base_iterator;
	}

	@Override
	public Station getSourceStation() {
		return source.getSourceStation();
	}

	@Override
	public String[] getSchema() {
		return source.getSchema();
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
