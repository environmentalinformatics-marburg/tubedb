package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationSqrt extends Computation {
	public final Computation a;
	public ComputationSqrt(Computation a) {
		this.a = a;
	}
	@Override
	public float eval(long timestamp, float[] data) {		
		return (float) Math.sqrt(a.eval(timestamp, data));
	}
}
