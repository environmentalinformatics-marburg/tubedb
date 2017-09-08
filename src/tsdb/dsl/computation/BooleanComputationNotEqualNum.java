package tsdb.dsl.computation;

import tsdb.util.Computation;

public class BooleanComputationNotEqualNum extends BooleanComputation {
	public final Computation a;
	public final float b;
	public BooleanComputationNotEqualNum(Computation a, float b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public boolean eval(long timestamp, float[] data) {
		return a.eval(timestamp, data) != b;
	}
}