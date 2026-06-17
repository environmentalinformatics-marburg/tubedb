package tsdb.dsl.computation;

import java.util.Arrays;

import tsdb.util.Computation;

public class ComputationRollingMean extends Computation {
	private final Computation parameter;
	private final int windowSize;
	private final int halfWindow;

	private float[] buffer;
	private double sum = 0d;
	private int count = 0;
	private int pos = 0;

	public ComputationRollingMean(Computation parameter, int windowSize) {
		this.parameter = parameter;
		this.windowSize = windowSize;
		this.halfWindow = windowSize / 2;
		this.buffer = new float[windowSize];
		Arrays.fill(buffer, Float.NaN);
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
		return count < halfWindow ? Float.NaN : (float) (sum / count);
		//return val;
	}
}
