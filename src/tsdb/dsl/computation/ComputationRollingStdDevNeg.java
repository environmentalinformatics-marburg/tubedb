package tsdb.dsl.computation;

import java.util.Arrays;

import tsdb.util.Computation;

public class ComputationRollingStdDevNeg extends Computation {
	private final Computation parameter;
	private final int windowSize;
	private final int halfWindow;

	private float[] buffer;
	private double sum = 0d;
	private double sumSq = 0d;
	private int count = 0;
	private int pos = 0;

	public ComputationRollingStdDevNeg(Computation parameter, int windowSize) {
		this.parameter = parameter;
		this.windowSize = windowSize;
		this.halfWindow = windowSize / 2;
		this.buffer = new float[windowSize];
		Arrays.fill(buffer, Float.NaN);
	}

	@Override
	public float eval(long timestamp, float[] data) {
		float old = buffer[pos];
		if (Float.isFinite(old)) {
			sum -= old;
			sumSq -= (double) old * old;
			count--;
		}

		float val = parameter.eval(timestamp, data);
		if (Float.isFinite(val)) {
			sum += val;
			sumSq += (double) val * val;
			count++;
		}
		buffer[pos] = val;
		pos = (pos + 1) % windowSize;

		if (count < halfWindow || count == 0) {
			return Float.NaN;
		}

		double mean = sum / count;
		double variance = (sumSq / count) - (mean * mean);
		return variance < 0d ? 0f : (float) -Math.sqrt(variance);
	}
}