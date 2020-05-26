package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationArctanDegNeg extends Computation {
	public final Computation a;
	public ComputationArctanDegNeg(Computation a) {
		this.a = a;
	}
	@Override
	public float eval(long timestamp, float[] data) {		
		return (float) - Math.toDegrees(Math.atan(a.eval(timestamp, data)));
	}
}
