package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationPow4Var extends Computation {
	public final int a;
	public ComputationPow4Var(int a) {
		this.a = a;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		float x = data[a];
		return x*x*x*x;
	}
}
