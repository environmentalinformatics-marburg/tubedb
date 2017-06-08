package tsdb.dsl.computation;

public class ComputationAddNum extends Computation {
	public final Computation a;
	public final float b;
	public ComputationAddNum(Computation a, float b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return a.eval(timestamp, data) + b;
	}
}
