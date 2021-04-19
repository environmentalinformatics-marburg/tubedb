package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationMulCubeVar1Num extends Computation {
	public final float factor;
	public ComputationMulCubeVar1Num(float factor) {
		this.factor = factor;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		float x = data[1];
		return  x * x * x * factor;
	}
	@Override
	public String toString() {
		return "MulCubeVar1Num(" + factor + ")";
	}
}
