package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationMulCubeNum extends Computation {
	public final Computation a;
	public final float factor;
	public ComputationMulCubeNum(Computation a, float factor) {
		this.a = a;
		this.factor = factor;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		float x = a.eval(timestamp, data);
		return x * x * x * factor;
	}
	@Override
	public String toString() {
		return "MulCubeNum(" + a + ", " + factor + ")";
	}
}
