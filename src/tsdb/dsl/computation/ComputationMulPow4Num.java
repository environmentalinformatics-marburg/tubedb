package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationMulPow4Num extends Computation {
	public final Computation a;
	public final float factor;
	public ComputationMulPow4Num(Computation a, float factor) {
		this.a = a;
		this.factor = factor;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		float x = a.eval(timestamp, data);
		return x * x * x *  x * factor;
	}
	@Override
	public String toString() {
		return "MulPow4Num(" + a + ", " + factor + ")";
	}
}
