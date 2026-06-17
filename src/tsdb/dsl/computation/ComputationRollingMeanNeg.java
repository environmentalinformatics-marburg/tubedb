package tsdb.dsl.computation;

import tsdb.util.Computation;

public class ComputationRollingMeanNeg extends Computation {
	private final Computation parameter;
	private final int windowSize;
	private final int halfWindow;

	private float[] buffer;
	private double sum = 0f;
	private int count = 0;
	private int pos = 0;

	public ComputationRollingMeanNeg(Computation parameter, int windowSize) {
		this.parameter = parameter;
		this.windowSize = windowSize;
		this.halfWindow = windowSize / 2;
		this.buffer = new float[windowSize];
	}

	@Override
	public float eval(long timestamp, float[] data) {
		float old = buffer[pos];
		if(Float.isFinite(old)) {
			sum -= old;
			count--;
		}

		float val = parameter.eval(timestamp, data);
		if (Float.isFinite(val)) {
			sum += val;
			count++;
		}
		buffer[pos] = val;
		pos = (pos + 1) % windowSize;
		return count < halfWindow ? Float.NaN : (float) (-(sum / count));
	}
}
