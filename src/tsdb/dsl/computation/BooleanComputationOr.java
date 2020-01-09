package tsdb.dsl.computation;

public class BooleanComputationOr extends BooleanComputation {
	public final BooleanComputation a;
	public final BooleanComputation b;
	public BooleanComputationOr(BooleanComputation a, BooleanComputation b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public boolean eval(long timestamp, float[] data) {
		return a.eval(timestamp, data) || b.eval(timestamp, data);
	}
}