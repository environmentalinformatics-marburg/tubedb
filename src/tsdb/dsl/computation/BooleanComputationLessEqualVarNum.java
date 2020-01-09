package tsdb.dsl.computation;

public class BooleanComputationLessEqualVarNum extends BooleanComputation {
	public final int a;
	public final float b;
	public BooleanComputationLessEqualVarNum(int a, float b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public boolean eval(long timestamp, float[] data) {
		return data[a] <= b;
	}
}