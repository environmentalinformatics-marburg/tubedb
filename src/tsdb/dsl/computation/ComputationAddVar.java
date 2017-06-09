package tsdb.dsl.computation;

public class ComputationAddVar extends Computation {
	public final Computation a;
	public final int b;
	public ComputationAddVar(Computation a, int b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return a.eval(timestamp, data) + data[b];
	}
}
