package tsdb.dsl.computation;

public class ComputationDivNum2 extends Computation {
	public final Computation a;
	public final float b;
	public ComputationDivNum2(Computation a, float b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return a.eval(timestamp, data) / b;
	}
}
