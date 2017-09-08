package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationSubVarVar extends Computation {
	public final int a;
	public final int b;
	public ComputationSubVarVar(int a, int b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return data[a] - data[b];
	}
}
