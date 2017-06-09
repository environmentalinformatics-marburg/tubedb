package tsdb.dsl.computation;

public class ComputationDivVarNum extends Computation {
	public final int a;
	public final float b;
	public ComputationDivVarNum(int a, float b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return data[a] / b;
	}
}
