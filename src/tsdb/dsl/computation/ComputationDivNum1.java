package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationDivNum1 extends Computation {
	public final float a;
	public final Computation b;
	public ComputationDivNum1(float a, Computation b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return a / b.eval(timestamp, data);
	}
}
