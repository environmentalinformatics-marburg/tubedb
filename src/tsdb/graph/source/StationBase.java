package tsdb.graph.source;

import static tsdb.util.AssumptionCheck.throwNull;
import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.graph.node.Base;
import tsdb.graph.node.Node;
import tsdb.graph.node.NodeGen;
import tsdb.iterator.BaseAggregationIterator;
import tsdb.util.iterator.TsIterator;

/**
 * This node creates base aggregated values from one station source.
 * @author woellauer
 *
 */
public class StationBase extends Base.Abstract {

	private final Node source;	

	protected StationBase(TsDB tsdb, Node source) {
		super(tsdb);
		throwNull(source);
		this.source = source;
	}

	public static StationBase of(TsDB tsdb,Station station, String[] querySchema, NodeGen stationGen) {
		if(querySchema==null) {
			//querySchema = tsdb.getBaseSchema(station.loggerType.sensorNames);
			String[] rawSensorNames = tsdb.streamStorage.getSensorNames(station.stationID);
			if(rawSensorNames==null || rawSensorNames.length==0) {
				return null;
			}
			//querySchema = tsdb.getBaseSchema(station.getSchema());
			querySchema = tsdb.getBaseSchema(rawSensorNames);
		}
		if(querySchema==null || querySchema.length==0) {
			return null;
		}
		Node source = stationGen.get(station.stationID, querySchema);
		return new StationBase(tsdb, source);
	}

	@Override
	public TsIterator get(Long start, Long end) {
		TsIterator input_iterator = source.get(start, end);
		if(input_iterator==null||!input_iterator.hasNext()) {
			return null;
		}
		BaseAggregationIterator base_iterator = new BaseAggregationIterator(tsdb, input_iterator);
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
		return tsdb.getBaseSchema(source.getSchema());
	}
	
	@Override
	public VirtualPlot getSourceVirtualPlot() {
		return source.getSourceVirtualPlot();
	}
	
	@Override
	public long[] getTimestampInterval() {
		return source.getTimestampInterval();
	}
}
