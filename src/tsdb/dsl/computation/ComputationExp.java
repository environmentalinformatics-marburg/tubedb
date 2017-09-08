package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationExp extends Computation {
	public final Computation a;
	public ComputationExp(Computation a) {
		this.a = a;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return (float) Math.exp(a.eval(timestamp, data));
	}
}
