package tsdb.dsl.computation;

public class ComputationPow extends Computation {
	public final Computation a;
	public final Computation b;
	public ComputationPow(Computation a, Computation b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return (float) Math.pow(a.eval(timestamp, data), b.eval(timestamp, data));
	}
}
