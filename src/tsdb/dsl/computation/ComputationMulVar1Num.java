package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationMulVar1Num extends Computation {
	public final float b;
	public ComputationMulVar1Num(float b) {
		this.b = b;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return data[1] * b;
	}
	
	@Override
	public String toString() {
		return "MulVar1Num(" + b + ")";
	}
}
