package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationDeltaNeg extends Computation {
	private final Computation parameter;

	private float prev = Float.NaN;

	public ComputationDeltaNeg(Computation parameter) {
		this.parameter = parameter;
	}

	@Override
	public float eval(long timestamp, float[] data) {
		float old = prev;
		this.prev = parameter.eval(timestamp, data);
		return old - prev;
	}
}
