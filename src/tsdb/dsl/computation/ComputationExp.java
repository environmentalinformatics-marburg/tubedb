package tsdb.dsl.computation;

public class ComputationExp extends Computation {
	public final Computation a;
	public ComputationExp(Computation a) {
		this.a = a;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return (float) Math.exp(a.eval(timestamp, data));
	}
}
