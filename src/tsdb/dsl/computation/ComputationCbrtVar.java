package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationCbrtVar extends Computation {
	public final int a;
	public ComputationCbrtVar(int a) {
		this.a = a;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		float x = data[a];
		return (float) Math.cbrt(x);
	}
}
