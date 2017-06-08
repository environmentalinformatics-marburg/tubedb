package tsdb.dsl.computation;

public class BooleanComputationAnd extends BooleanComputation {
	public final BooleanComputation a;
	public final BooleanComputation b;
	public BooleanComputationAnd(BooleanComputation a, BooleanComputation b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public boolean eval(long timestamp, float[] data) {
		return a.eval(timestamp, data) && b.eval(timestamp, data);
	}
}