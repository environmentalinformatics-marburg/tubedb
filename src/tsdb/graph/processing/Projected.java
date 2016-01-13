package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNull;

import tsdb.Station;
import tsdb.VirtualPlot;
import tsdb.graph.node.Continuous;
import tsdb.iterator.ProjectionFillIterator;
import tsdb.util.iterator.TsIterator;

/**
 * Project schema of source to target schema (fill with NaNs if sensor not in source)
 * @author woellauer
 *
 */
public class Projected implements Continuous {
	
	private final Continuous source;
	private final String[] targetSchema;

	
	protected Projected(Continuous source, String[] targetSchema) {
		throwNull(source);
		this.source = source;
		this.targetSchema = targetSchema;
	}
	
	public static Projected of(Continuous source, String[] targetSchema) {
		return new Projected(source, targetSchema);
	}
	
	
	@Override
	public TsIterator get(Long start, Long end) {
		TsIterator input_iterator = source.get(start, end);
		if(TsIterator.isNotLive(input_iterator)) {
			return null;
		}		
		ProjectionFillIterator it = new ProjectionFillIterator(input_iterator,targetSchema);		
		if(TsIterator.isNotLive(it)) {
			return null;
		}
		return it;
	}

	@Override
	public Station getSourceStation() {
		return source.getSourceStation();
	}

	@Override
	public boolean isConstantTimestep() {
		return source.isConstantTimestep();
	}

	@Override
	public String[] getSchema() {
		return targetSchema;
	}

	@Override
	public TsIterator getExactly(long start, long end) {
		return get(start,end);
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