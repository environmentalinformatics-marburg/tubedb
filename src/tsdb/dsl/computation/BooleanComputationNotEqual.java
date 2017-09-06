package tsdb.dsl.computation;

public class BooleanComputationNotEqual extends BooleanComputation {
	public final Computation a;
	public final Computation b;
	public BooleanComputationNotEqual(Computation a, Computation b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public boolean eval(long timestamp, float[] data) {
		return a.eval(timestamp, data) != b.eval(timestamp, data);
	}
}