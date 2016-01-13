package tsdb.graph.source;

import static tsdb.util.AssumptionCheck.throwNulls;
import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.graph.node.RawSource;
import tsdb.util.Util;
import tsdb.util.iterator.TsIterator;

/**
 * This node is the source of station values.
 * @author woellauer
 *
 */
public class StationRawSource extends RawSource.Abstract {
	public final Station station; // not null
	public final String[] schema; //not null, schema valid

	protected StationRawSource(TsDB tsdb, Station station, String[] schema) {
		super(tsdb);
		throwNulls(station,schema);
		this.station = station;
		this.schema = schema;
		if(this.schema.length==0) {
			throw new RuntimeException("no schema");
		}
		if(!station.isValidSchemaWithVirtualSensors(schema)) {
			throw new RuntimeException("not valid schema: "+Util.arrayToString(schema)+" in "+Util.arrayToString(station.loggerType.sensorNames)); 
		}
	}

	public static StationRawSource of(TsDB tsdb, String stationName, String[] querySchema) {
		Station station = tsdb.getStation(stationName);
		if(station==null) {
			throw new RuntimeException("station not found: "+stationName);
		}
		return of(tsdb, station, querySchema);
	}
	
	public static StationRawSource of(TsDB tsdb, Station station, String[] querySchema) {
		if(querySchema==null) {
			querySchema = station.loggerType.sensorNames;
		}
		return new StationRawSource(tsdb, station, querySchema);
	}
	

	@Override
	public TsIterator get(Long start, Long end) {
		return tsdb.streamStorage.getRawIterator(station.stationID, schema, start, end);
	}

	@Override
	public Station getSourceStation() {
		return this.station;
	}

	@Override
	public String[] getSchema() {
		return schema;
	}
	
	@Override
	public VirtualPlot getSourceVirtualPlot() {
		return null;
	}
	
	@Override
	public long[] getTimestampInterval() {
		return tsdb.getTimeInterval(station.stationID);
	}
}
