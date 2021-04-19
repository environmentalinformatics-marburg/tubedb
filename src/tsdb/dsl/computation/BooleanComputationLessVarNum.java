package tsdb.dsl.computation;

public class BooleanComputationLessVarNum extends BooleanComputation {
	public final int a;
	public final float b;
	public BooleanComputationLessVarNum(int a, float b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public boolean eval(long timestamp, float[] data) {
		return data[a] < b;
	}
	
	@Override
	public String toString() {
		return "LessVarNum([" + a + "], " + b + ")";
	}
}