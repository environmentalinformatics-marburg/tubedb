package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationMul extends Computation {
	public final Computation a;
	public final Computation b;
	public ComputationMul(Computation a, Computation b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return a.eval(timestamp, data) * b.eval(timestamp, data);
	}
}
