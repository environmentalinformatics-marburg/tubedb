package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationDivNumVar extends Computation {
	public final float a;
	public final int b;
	public ComputationDivNumVar(float a, int b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return a / data[b];
	}
}
