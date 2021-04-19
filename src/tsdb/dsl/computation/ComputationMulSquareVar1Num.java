package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationMulSquareVar1Num extends Computation {
	public final float factor;
	public ComputationMulSquareVar1Num(float factor) {
		this.factor = factor;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		float x = data[1];
		return  x * x * factor;
	}
	@Override
	public String toString() {
		return "MulSquareVar1Num("+ factor + ")";
	}
}
