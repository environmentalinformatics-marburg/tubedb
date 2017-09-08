package tsdb.graph.processing;

import static tsdb.util.AssumptionCheck.throwNull;

import tsdb.Station;
import tsdb.VirtualPlot;
import tsdb.graph.node.Continuous;
import tsdb.util.Mutator;
import tsdb.util.iterator.MutatorIterator;
import tsdb.util.iterator.TsIterator;

public class PostHourMutation implements Continuous {
	private final Continuous source;
	private final Mutator postHourMutator;
	
	public PostHourMutation(Continuous source, Mutator postHourMutator) {
		throwNull(source);
		this.source = source;
		this.postHourMutator = postHourMutator;
	}		

	@Override
	public TsIterator get(Long start, Long end) {
		TsIterator it = source.get(start, end);
		return MutatorIterator.appendMutator(it, postHourMutator);
	}

	@Override
	public TsIterator getExactly(long start, long end) {		
		return get(start,end);
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
	public boolean isConstantTimestep() {
		return source.isConstantTimestep();
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
