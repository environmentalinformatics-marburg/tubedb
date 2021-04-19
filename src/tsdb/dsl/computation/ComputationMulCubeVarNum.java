package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationMulCubeVarNum extends Computation {
	public final int a;
	public final float factor;
	public ComputationMulCubeVarNum(int a, float factor) {
		this.a = a;
		this.factor = factor;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		float x = data[a];
		return  x * x * x * factor;
	}
	@Override
	public String toString() {
		return "MulCubeVarNum([" + a  + "], " + factor + ")";
	}
}
