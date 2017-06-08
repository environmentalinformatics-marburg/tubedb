package tsdb.dsl.computation;

public class ComputationSubNum extends Computation {
	public final float a;
	public final Computation b;
	public ComputationSubNum(float a, Computation b) {
		this.a = a;
		this.b = b;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return a - b.eval(timestamp, data);
	}
}
