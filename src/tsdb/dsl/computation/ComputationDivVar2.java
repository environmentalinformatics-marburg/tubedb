package tsdb.dsl.computation;

public class ComputationDivVar2 extends Computation {
	public final Computation a;
	public final int b;
	public ComputationDivVar2(Computation a, int b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return a.eval(timestamp, data) / data[b];
	}
}
