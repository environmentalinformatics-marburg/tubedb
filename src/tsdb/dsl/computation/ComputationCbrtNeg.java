package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationCbrtNeg extends Computation {
	public final Computation a;
	public ComputationCbrtNeg(Computation a) {
		this.a = a;
	}
	@Override
	public float eval(long timestamp, float[] data) {		
		return (float) - Math.cbrt(a.eval(timestamp, data));
	}
}
