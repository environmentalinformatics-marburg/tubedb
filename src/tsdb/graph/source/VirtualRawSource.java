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
 * Node: raw source of virtual plot
 * @author woellauer
 *
 */
public class VirtualRawSource extends RawSource.Abstract {
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger();

	private final VirtualPlot virtualPlot;
	private final String[] schema;

	private VirtualRawSource(TsDB tsdb, VirtualPlot virtualPlot, String[] schema) {
		super(tsdb);
		throwNulls(virtualPlot, schema);
		this.virtualPlot = virtualPlot;
		this.schema = schema;
		if(this.schema.length==0) {
			throw new RuntimeException("no schema");
		}
		if(!virtualPlot.isValidSchema(schema)) { // no virtual sensors in raw !
			throw new RuntimeException("not valid schema: "+Util.arrayToString(schema)+" in "+Util.arrayToString(virtualPlot.getSchema())); 
		}
	}

	public static VirtualRawSource of(TsDB tsdb, VirtualPlot virtualPlot, String[] querySchema) {
		if(querySchema==null) {
			querySchema = virtualPlot.getSchema();
		}
		return new VirtualRawSource(tsdb, virtualPlot, querySchema);
	}

	@Override
	public TsIterator get(Long start, Long end) {		
		List<TimestampInterval<StationProperties>> intervalList = virtualPlot.getStationList(start, end, schema);
		List<StreamIterator> processing_iteratorList = new ArrayList<StreamIterator>();				
		for(TimestampInterval<StationProperties> interval:intervalList) {
			String stationID = interval.value.get_serial();
			String[] stationSchema = tsdb.getValidSchema(stationID, schema);
			TimestampInterval<StationProperties> filteredInterval = interval.filterByInterval(start, end);
			if(filteredInterval!=null) {
				for(String sensorName:stationSchema) {
					StreamIterator it = tsdb.streamStorage.getRawSensorIterator(stationID, sensorName, filteredInterval.start, filteredInterval.end);
					if(it!=null&&it.hasNext()) {
						processing_iteratorList.add(it);
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
		return new RelationalIterator(processing_iteratorList, schema);
	}

	@Override
	public Station getSourceStation() {
		return null;
	}

	@Override
	public String[] getSchema() {
		return schema;
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
