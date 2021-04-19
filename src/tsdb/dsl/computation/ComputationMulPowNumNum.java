package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationMulPowNumNum extends Computation {
	public final Computation a;
	public final float exponent;
	public final float factor;
	public ComputationMulPowNumNum(Computation a, float exponent, float factor) {
		this.a = a;
		this.exponent = exponent;
		this.factor = factor;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return  ((float) (Math.pow(a.eval(timestamp, data), exponent))) * factor;
	}
	@Override
	public String toString() {
		return "MulPowNumNum(" + a  + ", " + exponent + ", " + factor + ")";
	}
}
