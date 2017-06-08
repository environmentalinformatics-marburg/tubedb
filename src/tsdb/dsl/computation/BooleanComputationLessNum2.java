package tsdb.dsl.computation;

public class BooleanComputationLessNum2 extends BooleanComputation {
	public final Computation a;
	public final float b;
	public BooleanComputationLessNum2(Computation a, float b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public boolean eval(long timestamp, float[] data) {
		return a.eval(timestamp, data) < b;
	}
}