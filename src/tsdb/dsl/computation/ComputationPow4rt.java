package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationPow4rt extends Computation {
	public final Computation a;
	public ComputationPow4rt(Computation a) {
		this.a = a;
	}
	@Override
	public float eval(long timestamp, float[] data) {	
		return (float) Math.pow(a.eval(timestamp, data), (1d/4d));
	}
}
