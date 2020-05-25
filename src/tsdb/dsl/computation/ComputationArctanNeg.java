package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationArctanNeg extends Computation {
	public final Computation a;
	public ComputationArctanNeg(Computation a) {
		this.a = a;
	}
	@Override
	public float eval(long timestamp, float[] data) {		
		return (float) - Math.atan(a.eval(timestamp, data));
	}
}
