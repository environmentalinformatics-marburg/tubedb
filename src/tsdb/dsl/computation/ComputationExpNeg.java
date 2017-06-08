package tsdb.dsl.computation;

public class ComputationExpNeg extends Computation {
	public final Computation a;
	public ComputationExpNeg(Computation a) {
		this.a = a;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return (float) - Math.exp(a.eval(timestamp, data));
	}
}
