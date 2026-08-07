package tsdb.dsl.computation;

import java.util.Arrays;

import tsdb.util.Computation;

public class ComputationRollingMax extends Computation {
    private final Computation parameter;
    private final int windowSize;
    private final int halfWindow;

    private float[] buffer;
    private int pos = 0;

    public ComputationRollingMax(Computation parameter, int windowSize) {
        this.parameter = parameter;
        this.windowSize = windowSize;
        this.halfWindow = windowSize / 2;
        this.buffer = new float[windowSize];
        Arrays.fill(buffer, Float.NaN);
    }

    @Override
    public float eval(long timestamp, float[] data) {
        float val = parameter.eval(timestamp, data);
        buffer[pos] = val;
        pos = (pos + 1) % windowSize;

        float max = Float.NEGATIVE_INFINITY;
        int count = 0;

        for (float v : buffer) {
            if (Float.isFinite(v)) {
                count++;
                if (v > max) {
                    max = v;
                }
            }
        }

        return count < halfWindow ? Float.NaN : max;
    }
}