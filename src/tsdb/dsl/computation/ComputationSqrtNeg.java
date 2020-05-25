package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationSqrtNeg extends Computation {
	public final Computation a;
	public ComputationSqrtNeg(Computation a) {
		this.a = a;
	}
	@Override
	public float eval(long timestamp, float[] data) {		
		return (float) - Math.sqrt(a.eval(timestamp, data));
	}
}
