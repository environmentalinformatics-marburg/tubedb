package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationNeg extends Computation {
	public final Computation a;
	public ComputationNeg(Computation a) {
		this.a = a;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return - a.eval(timestamp, data);
	}
	
	public static Computation wrap(Computation a, boolean positive) {
		return positive ? a : new ComputationNeg(a);
	}
}
