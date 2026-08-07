package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationAbs extends Computation {
	public final Computation a;
	public ComputationAbs(Computation a) {
		this.a = a;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		float x = a.eval(timestamp, data);
		return Math.abs(x);
	}
}
