package tsdb.dsl.computation;

public class BooleanComputationLessNumVar extends BooleanComputation {
	public final float a;
	public final int b;
	public BooleanComputationLessNumVar(float a, int b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public boolean eval(long timestamp, float[] data) {
		return a < data[b];
	}
}