package tsdb.dsl.computation;

import tsdb.util.Computation;

public class BooleanComputationLess extends BooleanComputation {
	public final Computation a;
	public final Computation b;
	public BooleanComputationLess(Computation a, Computation b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public boolean eval(long timestamp, float[] data) {
		return a.eval(timestamp, data) < b.eval(timestamp, data);
	}
}