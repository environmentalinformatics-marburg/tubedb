package tsdb.iterator;

import static tsdb.util.AssumptionCheck.throwEmpty;

import java.util.Arrays;
import java.util.Map;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.TsSchema.Aggregation;
import tsdb.util.Util;
import tsdb.util.iterator.MoveIterator;
import tsdb.util.iterator.TsIterator;
import tsdb.util.processingchain.ProcessingChain;

/**
 * Iterator berechnet den Median pro Sensor aus mehreren Eingangs-Iteratoren.
 * Gibt zusätzlich die Anzahl der validen Werte aus, die für den Median verwendet wurden.
 */
public class MedianCountIterator extends MoveIterator {

    private Map<String, Integer> schemaMap;
    private TsIterator[] input_iterators;
    private int[][] inputIndices;
    private final int minCount;
    private final boolean withQualityMeasures;
    private int numSensors;

    private float[][] valueBuffers;
    private int[] valueCounts;
    private int[][] qualityCounter;

    public static String[] createOutputSensorNames(String[] baseNames) {
        String[] outputNames = new String[baseNames.length * 2];
        for (int i = 0; i < baseNames.length; i++) {
            outputNames[i] = baseNames[i];
            outputNames[i + baseNames.length] = baseNames[i] + "_cnt";
        }
        return outputNames;
    }

    private static TsSchema createOutputSchema(String[] baseNames, TsIterator[] input_iterators) {
        throwEmpty(input_iterators);
        TsSchema[] schemas = TsIterator.toSchemas(input_iterators);
        TsSchema.throwDifferentAggregation(schemas);
        Aggregation aggregation = schemas[0].aggregation;
        TsSchema.throwDifferentTimeStep(schemas);
        int timeStep = schemas[0].timeStep;
        TsSchema.throwDifferentContinuous(schemas);
        boolean isContinuous = schemas[0].isContinuous;

        String[] outputNames = createOutputSensorNames(baseNames);
        return new TsSchema(outputNames, aggregation, timeStep, isContinuous);
    }

    public MedianCountIterator(String[] baseNames, TsIterator[] input_iterators, int minCount, boolean withQualityMeasures) {
        super(createOutputSchema(baseNames, input_iterators));
        this.input_iterators = input_iterators;
        this.schemaMap = Util.stringArrayToMap(this.schema.names);
        this.minCount = minCount;
        this.withQualityMeasures = withQualityMeasures;

        this.numSensors = this.schema.length / 2;
        int numInputs = input_iterators.length;

        this.valueBuffers = new float[numSensors][numInputs];
        this.valueCounts = new int[numSensors];
        this.qualityCounter = new int[numSensors][2];

        inputIndices = new int[input_iterators.length][];
        for (int iteratorIndex = 0; iteratorIndex < input_iterators.length; iteratorIndex++) {
            TsIterator it = input_iterators[iteratorIndex];
            String[] iteratorSchema = it.getNames();
            inputIndices[iteratorIndex] = new int[iteratorSchema.length];
            for (int inputPos = 0; inputPos < iteratorSchema.length; inputPos++) {
                Integer outputPos = schemaMap.get(iteratorSchema[inputPos]);
                if (outputPos == null) {
                    throw new IllegalArgumentException("Sensor '" + iteratorSchema[inputPos] + "' nicht im Output-Schema gefunden.");
                }
                inputIndices[iteratorIndex][inputPos] = outputPos;
            }
        }
    }

    @Override
    protected TsEntry getNext() {
        Arrays.fill(valueCounts, 0);
        if (withQualityMeasures) {
            for (int i = 0; i < numSensors; i++) {
                qualityCounter[i][0] = 0;
                qualityCounter[i][1] = 0;
            }
        }

        long timestamp = -1;

        for (int iteratorIndex = 0; iteratorIndex < input_iterators.length; iteratorIndex++) {
            TsIterator it = input_iterators[iteratorIndex];
            if (!it.hasNext()) {
                return null;
            }
            TsEntry element = it.next();
            if (timestamp == -1) {
                timestamp = element.timestamp;
            } else if (timestamp != element.timestamp) {
                throw new RuntimeException("iterator error: Timestamps stimmen nicht überein");
            }

            int[] inputIteratorIndices = inputIndices[iteratorIndex];
            for (int inputIteratorIndex = 0; inputIteratorIndex < inputIteratorIndices.length; inputIteratorIndex++) {
                float value = element.data[inputIteratorIndex];
                if (!Float.isNaN(value)) {
                    int pos = inputIteratorIndices[inputIteratorIndex];
                    int count = valueCounts[pos];
                    
                    valueBuffers[pos][count] = value;
                    valueCounts[pos] = count + 1;

                    if (withQualityMeasures) {
                        qualityCounter[pos][0]++;
                        if (element.interpolated != null && element.interpolated[inputIteratorIndex]) {
                            qualityCounter[pos][1]++;
                        }
                    }
                }
            }
        }

        float[] outputData = new float[this.schema.length];
        for (int i = 0; i < numSensors; i++) {
            int count = valueCounts[i];
            if (count < minCount) {
                outputData[i] = Float.NaN;
            } else if (count == 1) {
                outputData[i] = valueBuffers[i][0];
            } else {
                Arrays.sort(valueBuffers[i], 0, count);
                if ((count & 1) == 1) {
                    outputData[i] = valueBuffers[i][count >> 1];
                } else {
                    outputData[i] = (valueBuffers[i][count >> 1] + valueBuffers[i][(count >> 1) - 1]) * 0.5f;
                }
            }
            outputData[i + numSensors] = (float) count;
        }

        if (withQualityMeasures) {
            int[][] outputQuality = new int[this.schema.length][2];
            for (int i = 0; i < numSensors; i++) {
                outputQuality[i][0] = qualityCounter[i][0];
                outputQuality[i][1] = qualityCounter[i][1];
                outputQuality[i + numSensors][0] = qualityCounter[i][0];
                outputQuality[i + numSensors][1] = qualityCounter[i][1];
            }
            return new TsEntry(timestamp, outputData, null, outputQuality, null);
        } else {
            return new TsEntry(timestamp, outputData);
        }
    }

    @Override
    public ProcessingChain getProcessingChain() {
        return ProcessingChain.of(input_iterators, this);
    }
}