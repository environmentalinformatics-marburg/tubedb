package tsdb.graph.source;

import static tsdb.util.AssumptionCheck.throwNull;
import static tsdb.util.AssumptionCheck.throwNullArray;
import static tsdb.util.AssumptionCheck.throwNullText;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.GeneralStation;
import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.graph.node.Continuous;
import tsdb.iterator.NanGapIterator;
import tsdb.util.BaseAggregationTimeUtil;
import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.TsSchema.Aggregation;
import tsdb.util.iterator.InputIterator;
import tsdb.util.iterator.TsIterator;

/**
 * Node: source from cache (average of group)
 * @author woellauer
 *
 */
public class GroupAverageSource_NEW extends Continuous.Abstract {
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger();

	private final String group;
	private final String[] targetSchema;

	public GroupAverageSource_NEW(TsDB tsdb, String group, String[] targetSchema) {
		super(tsdb);
		throwNull(group);
		throwNullArray(targetSchema);
		this.group = group;
		this.targetSchema = targetSchema;
	}

	public static GroupAverageSource_NEW ofPlot(TsDB tsdb, String plotID, String... targetSchema) {	
		GeneralStation generalStation = null;
		VirtualPlot virtualPlot = tsdb.getVirtualPlot(plotID);
		if(virtualPlot!=null) {
			generalStation = virtualPlot.generalStation;
		} else {
			Station station = tsdb.getStation(plotID);
			if(station!=null) {
				generalStation = station.generalStation;
			}
		}
		throwNullText(generalStation,"plotID not found: "+plotID);	
		return new GroupAverageSource_NEW(tsdb, generalStation.group, targetSchema);
	}
	
	
	private static class OverrideIterator extends InputIterator {		
		public static TsSchema createSchema(TsSchema tsSchema) {
			String[] names = tsSchema.names;
			Aggregation aggregation = Aggregation.CONSTANT_STEP;
			int timeStep = BaseAggregationTimeUtil.AGGREGATION_TIME_INTERVAL;
			boolean isContinuous = true;
			boolean hasQualityFlags = false;
			boolean hasInterpolatedFlags = false;
			boolean hasQualityCounters = false;
			return new TsSchema(names, aggregation, timeStep, isContinuous, hasQualityFlags, hasInterpolatedFlags, hasQualityCounters);
		}
		public OverrideIterator(TsIterator it) {
			super(it,createSchema(it.getSchema()));
		}
		@Override
		public boolean hasNext() {
			return input_iterator.hasNext();
		}
		@Override
		public TsEntry next() {
			return input_iterator.next();
		}		
	}
 


	@Override
	public TsIterator get(Long start, Long end) {		
		TsIterator it = tsdb.streamCache.getRawIterator(group, targetSchema, start, end);
		if(it==null||!it.hasNext()) {
			return null;
		}
		NanGapIterator cont = new NanGapIterator(new OverrideIterator(it), start, end);
		return cont;
	}

	@Override
	public Station getSourceStation() {
		return null;
	}


	@Override
	public boolean isConstantTimestep() {
		return true;
	}

	@Override
	public String[] getSchema() {
		return targetSchema;
	}

	@Override
	public TsIterator getExactly(long start, long end) {
		return get(start, end);
	}
	
	@Override
	public VirtualPlot getSourceVirtualPlot() {
		return null;
	}
	
	@Override
	public long[] getTimestampInterval() {
		return tsdb.streamCache.getStationTimeInterval(group);
	}

	@Override
	public String getSourceName() {
		return group;
	}
	
	
}
