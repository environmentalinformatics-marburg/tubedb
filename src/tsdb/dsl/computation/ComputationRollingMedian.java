package tsdb.dsl.computation;

import java.util.Arrays;

import tsdb.util.Computation;

public class ComputationRollingMedian extends Computation {
    private final Computation parameter;
    private final int windowSize;
    private final int halfWindow;

    private float[] buffer;
    private float[] sortedWindow;
    private int count = 0;
    private int pos = 0;

    public ComputationRollingMedian(Computation parameter, int windowSize) {
        this.parameter = parameter;
        this.windowSize = windowSize;
        this.halfWindow = windowSize / 2;
        this.buffer = new float[windowSize];
        Arrays.fill(buffer, Float.NaN);
        this.sortedWindow = new float[windowSize];
    }

    @Override
    public float eval(long timestamp, float[] data) {
        float old = buffer[pos];
        if (Float.isFinite(old)) {
            removeValue(old);
            count--;
        }

        float val = parameter.eval(timestamp, data);
        if (Float.isFinite(val)) {
            insertValue(val);
            count++;
        }
        buffer[pos] = val;
        pos = (pos + 1) % windowSize;

        if (count == 0) {
            return Float.NaN;
        }
        
        if (count < halfWindow) {
            return Float.NaN;
        }

        if (count % 2 == 1) {
            return sortedWindow[count / 2];
        } else {
            return (sortedWindow[count / 2 - 1] + sortedWindow[count / 2]) / 2.0f;
        }
    }

    private void removeValue(float val) {
        int idx = Arrays.binarySearch(sortedWindow, 0, count, val);
        if (idx >= 0) {
            System.arraycopy(sortedWindow, idx + 1, sortedWindow, idx, count - 1 - idx);
        }
    }

    private void insertValue(float val) {
        int idx = Arrays.binarySearch(sortedWindow, 0, count, val);
        if (idx < 0) {
            idx = -(idx + 1);
        }
        System.arraycopy(sortedWindow, idx, sortedWindow, idx + 1, count - idx);
        sortedWindow[idx] = val;
    }
}