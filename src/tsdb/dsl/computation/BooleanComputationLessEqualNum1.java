package tsdb.dsl.computation;

import tsdb.util.Computation;

public class BooleanComputationLessEqualNum1 extends BooleanComputation {
	public final float a;
	public final Computation b;
	public BooleanComputationLessEqualNum1(float a, Computation b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public boolean eval(long timestamp, float[] data) {
		return a <= b.eval(timestamp, data);
	}
}