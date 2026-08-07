package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationAbsNeg extends Computation {
	public final Computation a;
	public ComputationAbsNeg(Computation a) {
		this.a = a;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		float x = a.eval(timestamp, data);
		return -Math.abs(x);
	}
}
