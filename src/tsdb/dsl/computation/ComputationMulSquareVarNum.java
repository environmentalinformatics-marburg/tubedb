package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationMulSquareVarNum extends Computation {
	public final int a;
	public final float factor;
	public ComputationMulSquareVarNum(int a, float factor) {
		this.a = a;
		this.factor = factor;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		float x = data[a];
		return  x * x * factor;
	}
	@Override
	public String toString() {
		return "MulSquareVarNum([" + a  + "], " + factor + ")";
	}
}
