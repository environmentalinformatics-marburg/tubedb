package tsdb.dsl.computation;

public class ComputationNum extends Computation {
	private final float v;
	public ComputationNum(float v) {
		this.v = v;
	}
	@Override
	public float eval(long timestamp, float[] data) {
		return v;
	}
}
