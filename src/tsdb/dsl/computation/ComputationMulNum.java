package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationMulNum extends Computation {
	public final Computation a;
	public final float b;
	public ComputationMulNum(Computation a, float b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return a.eval(timestamp, data) * b;
	}
	@Override
	public String toString() {
		return "MulNum(" + a + ", " + b + ")";
	}
}
