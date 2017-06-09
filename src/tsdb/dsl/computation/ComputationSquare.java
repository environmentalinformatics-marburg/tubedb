package tsdb.dsl.computation;

public class ComputationSquare extends Computation {
	public final Computation a;
	public ComputationSquare(Computation a) {
		this.a = a;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		float x = a.eval(timestamp, data);
		return x*x;
	}
}
