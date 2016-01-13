package tsdb.graph.node;

import static tsdb.util.AssumptionCheck.throwNull;

import tsdb.Plot;
import tsdb.Station;
import tsdb.TsDB;
import tsdb.VirtualPlot;
import tsdb.util.BaseAggregationTimeUtil;
import tsdb.util.Util;
import tsdb.util.iterator.TsIterator;

/**
 * The base interface for all Nodes in the Query Graph.
 * @author woellauer
 *
 */
public interface Node {

	public TsIterator get(Long start, Long end);	
	public Station getSourceStation();
	public VirtualPlot getSourceVirtualPlot();

	public default Plot getSourcePlot() {
		Station s = getSourceStation();
		if(s!=null) {
			return Plot.of(s);
		}
		VirtualPlot v = getSourceVirtualPlot();
		if(v!=null) {
			return Plot.of(v);
		}
		return null;
	}

	public default String getSourceName() {
		return getSourcePlot().getPlotID();
	}

	public long[] getTimestampInterval();

	public default long[] getTimestampBaseInterval() {
		long[] interval = getTimestampInterval();
		if(interval==null) {
			return null;
		}
		return new long[]{BaseAggregationTimeUtil.alignQueryTimestampToBaseAggregationTime(interval[0]),BaseAggregationTimeUtil.alignQueryTimestampToBaseAggregationTime(interval[1])};
	}

	/**
	 * true => no time gaps in data stream, time steps do not need to be constant
	 * @return
	 */
	public boolean isContinuous();


	/**
	 * data stream aligned to time step
	 * @return
	 */
	public boolean isConstantTimestep();

	public String[] getSchema();

	public default boolean writeCSV(Long start, Long end, String filename) {
		TsIterator it = get(start,end);
		if(TsIterator.isNotLive(it)) {
			Util.log.error("produced no iterator -> no file written "+filename+"  in "+this.getClass());
			return false;
		}
		it.writeCSV(filename);
		return true;
	}	
	public default void writeConsole(Long start, Long end) {
		get(start,end).writeConsole();
	}

	public abstract class Abstract implements Node {

		protected final TsDB tsdb; //not null

		public Abstract(TsDB tsdb) {
			throwNull(tsdb);
			this.tsdb = tsdb;
		}
	}
}
