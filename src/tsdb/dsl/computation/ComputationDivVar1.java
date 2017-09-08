package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationDivVar1 extends Computation {
	public final int a;
	public final Computation b;
	public ComputationDivVar1(int a, Computation b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return data[a] / b.eval(timestamp, data);
	}
}
