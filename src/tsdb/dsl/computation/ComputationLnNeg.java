package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationLnNeg extends Computation {
	public final Computation a;
	public ComputationLnNeg(Computation a) {
		this.a = a;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return (float) - Math.log(a.eval(timestamp, data));
	}
}
