package tsdb.dsl.computation;

public class ComputationPowNum extends Computation {
	public final Computation a;
	public final float b;
	public ComputationPowNum(Computation a, float b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return (float) Math.pow(a.eval(timestamp, data), b);
	}
}
