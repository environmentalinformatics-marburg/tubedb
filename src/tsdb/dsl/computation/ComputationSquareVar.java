package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationSquareVar extends Computation {
	public final int a;
	public ComputationSquareVar(int a) {
		this.a = a;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		float x = data[a];
		return x*x;
	}
}
