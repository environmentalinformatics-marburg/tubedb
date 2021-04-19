package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationMulPowVarNumNum extends Computation {
	public final int a;
	public final float exponent;
	public final float factor;
	public ComputationMulPowVarNumNum(int a, float exponent, float factor) {
		this.a = a;
		this.exponent = exponent;
		this.factor = factor;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return  ((float) Math.pow(data[a], exponent)) * factor;
	}
	@Override
	public String toString() {
		return "MulPowVarNumNum([" + a  + "], " + exponent + ", " + factor + ")";
	}
}
