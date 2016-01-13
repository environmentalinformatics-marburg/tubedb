package tsdb.graph.node;

import static tsdb.util.AssumptionCheck.throwNull;

import tsdb.TsDB;

/**
 * This node provides base aggregated data.
 * @author woellauer
 *
 */
public interface Base extends Node {
	
	@Override
	public default boolean isConstantTimestep() {
		return true;
	}
	
	public abstract class Abstract implements Base {		
		protected TsDB tsdb; // not null		
		public Abstract(TsDB tsdb) {
			throwNull(tsdb);
			this.tsdb = tsdb;
		}
		
		@Override
		public final boolean isConstantTimestep() {
			return true;
		}
		
		@Override
		public boolean isContinuous() {
			return false;
		}
	}
}
