package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationSqrtVar extends Computation {
	public final int a;
	public ComputationSqrtVar(int a) {
		this.a = a;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		float x = data[a];
		return (float) Math.sqrt(x);
	}
}
