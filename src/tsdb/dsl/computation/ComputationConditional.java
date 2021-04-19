package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationConditional extends Computation {
	private final BooleanComputation p;
	public final Computation a;
	public final Computation b;
	public ComputationConditional(BooleanComputation p, Computation a, Computation b) {
		this.p = p;
		this.a = a;
		this.b = b;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return p.eval(timestamp, data) ? a.eval(timestamp, data) : b.eval(timestamp, data);
	}
	@Override
	public String toString() {
		return "IF(" + p.toString() + ", " + a.toString() + ", " + b.toString() + ")";
	}
}
