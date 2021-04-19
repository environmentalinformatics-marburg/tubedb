package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationMulCubeVar0Num extends Computation {
	public final float factor;
	public ComputationMulCubeVar0Num(float factor) {
		this.factor = factor;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		float x = data[0];
		return  x * x * x * factor;
	}
	@Override
	public String toString() {
		return "MulCubeVar0Num(" + factor + ")";
	}
}
