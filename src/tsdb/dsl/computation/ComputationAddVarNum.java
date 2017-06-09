package tsdb.dsl.computation;

public class ComputationAddVarNum extends Computation {
	public final int a;
	public final float b;
	public ComputationAddVarNum(int a, float b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return data[a] + b;
	}
}
