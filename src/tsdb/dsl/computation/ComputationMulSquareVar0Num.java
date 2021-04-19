package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationMulSquareVar0Num extends Computation {
	public final float factor;
	public ComputationMulSquareVar0Num(float factor) {
		this.factor = factor;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		float x = data[0];
		return  x * x * factor;
	}
	@Override
	public String toString() {
		return "MulSquareVar0Num("+ factor + ")";
	}
}
