package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationMulVar0Num extends Computation {
	public final float b;
	public ComputationMulVar0Num(float b) {
		this.b = b;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return data[0] * b;
	}
	
	@Override
	public String toString() {
		return "MulVar0Num(" + b + ")";
	}
}
