package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNull;

import tsdb.graph.node.Continuous;
import tsdb.graph.source.DelegateContinuous;
import tsdb.iterator.ProjectionFillIterator;
import tsdb.util.iterator.TsIterator;

/**
 * Project schema of source to target schema (fill with NaNs if sensor not in source)
 * @author woellauer
 *
 */
public class Projected extends DelegateContinuous {
	
	private final String[] targetSchema;
	
	private Projected(Continuous source, String[] targetSchema) {
		super(source);
		throwNull(targetSchema);
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
	public String[] getSchema() {
		return targetSchema;
	}	
}