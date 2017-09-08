package tsdb.dsl.computation;

import tsdb.util.Computation;

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
