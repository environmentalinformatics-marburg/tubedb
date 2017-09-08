package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationMulVarNum extends Computation {
	public final int a;
	public final float b;
	public ComputationMulVarNum(int a, float b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return data[a] * b;
	}
}
