package tsdb.iterator;

import java.util.Arrays;

import tsdb.util.DataQuality;
import tsdb.util.TsEntry;
import tsdb.util.iterator.TsIterator;

public class RollingMax extends BufferIterator {

    public RollingMax(TsIterator input_iterator, int windowSize) {
        super(input_iterator, windowSize);
    }

    @Override
    protected TsEntry calc(int currentPos) {
        TsEntry entry = window[currentPos];
        float[] max = new float[entry.data.length];
        int[] count = new int[entry.data.length];

        Arrays.fill(max, Float.NEGATIVE_INFINITY);

        for (int i = 0; i < window.length; i++) {
            TsEntry e = window[i];
            if (e != null) {
                float[] data = e.data;
                for (int j = 0; j < data.length; j++) {
                    float v = data[j];
                    if (Float.isFinite(v)) {
                        count[j]++;
                        if (v > max[j]) {
                            max[j] = v;
                        }
                    }
                }
            }
        }

        for (int j = 0; j < count.length; j++) {
            max[j] = count[j] < halfWindow ? Float.NaN : max[j];
        }

        DataQuality[] qf;
        if (entry.qualityFlag != null) {
            qf = Arrays.copyOf(entry.qualityFlag, entry.qualityFlag.length);
        } else {
            qf = null;
        }
        
        return new TsEntry(entry.timestamp, max, qf);
    }
}