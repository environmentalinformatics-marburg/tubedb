package tsdb.iterator;

import static tsdb.util.AssumptionCheck.throwEmpty;

import java.util.Map;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.TsSchema.Aggregation;
import tsdb.util.Util;
import tsdb.util.iterator.MoveIterator;
import tsdb.util.iterator.TsIterator;
import tsdb.util.processingchain.ProcessingChain;

/**
 * This iterator outputs elements of average values of input_iterator values.
 * Additionally, it outputs the count of values used for each average.
 * input_iterators need to be in same timestamp per element order.
 * @author woellauer
 */
public class AverageCountIterator extends MoveIterator {

	private Map<String, Integer> schemaMap;
	private TsIterator[] input_iterators;
	private int[][] inputIndices;
	private final int minCount;
	private final boolean withQualityMeasures;
	
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

		// Output-Schema: [sensor1, sensor2, sensor1_cnt, sensor2_cnt]
		String[] outputNames = createOutputSensorNames(baseNames);
		return new TsSchema(outputNames, aggregation, timeStep, isContinuous);
	}

	public AverageCountIterator(String[] baseNames, TsIterator[] input_iterators, int minCount, boolean withQualityMeasures) {
		super(createOutputSchema(baseNames, input_iterators));
		this.input_iterators = input_iterators;
		this.schemaMap = Util.stringArrayToMap(this.schema.names); // TODO check if this.schema is already by super set
		this.minCount = minCount;
		this.withQualityMeasures = withQualityMeasures;

		int numSensors = this.schema.length / 2;

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
		int numSensors = this.schema.length / 2;
		int[] value_cnt = new int[numSensors];
		float[] value_sum = new float[numSensors];
		long timestamp = -1;

		if (withQualityMeasures) {
			// Quality-Counter für ALLE Ausgabespalten (Avg + Cnt)
			int[][] value_qualityCounter = new int[this.schema.length][AbstractAggregationIterator.QUALITY_COUNTERS];

			for (int iteratorIndex = 0; iteratorIndex < input_iterators.length; iteratorIndex++) {
				TsIterator it = input_iterators[iteratorIndex];
				if (!it.hasNext()) {
					return null;
				}
				TsEntry element = it.next();
				if (timestamp == -1) {
					timestamp = element.timestamp;
				} else {
					if (timestamp != element.timestamp) {
						throw new RuntimeException("iterator error");
					}
				}

				int[] inputIteratorIndices = inputIndices[iteratorIndex];
				for (int inputIteratorIndex = 0; inputIteratorIndex < inputIteratorIndices.length; inputIteratorIndex++) {
					float value = element.data[inputIteratorIndex];
					if (!Float.isNaN(value)) {
						int pos = inputIteratorIndices[inputIteratorIndex]; // Index im Avg-Teil (0..numSensors-1)
						
						value_cnt[pos]++;
						value_sum[pos] += value;
						
						// Qualität für Avg UND Cnt identisch setzen
						value_qualityCounter[pos][0]++;
						value_qualityCounter[pos + numSensors][0]++;
						
						if (element.interpolated != null && element.interpolated[inputIteratorIndex]) {
							value_qualityCounter[pos][1]++;
							value_qualityCounter[pos + numSensors][1]++;
						}
					}
				}
			}

			float[] outputData = new float[this.schema.length];
			for (int i = 0; i < numSensors; i++) {
				if (value_cnt[i] >= minCount) {
					outputData[i] = value_sum[i] / value_cnt[i];
				} else {
					outputData[i] = Float.NaN;
				}
				// Count-Wert immer speichern
				outputData[i + numSensors] = (float) value_cnt[i];
			}
			return new TsEntry(timestamp, outputData, null, value_qualityCounter, null);
		} else {
			for (int iteratorIndex = 0; iteratorIndex < input_iterators.length; iteratorIndex++) {
				TsIterator it = input_iterators[iteratorIndex];
				if (!it.hasNext()) {
					return null;
				}
				TsEntry element = it.next();
				if (timestamp == -1) {
					timestamp = element.timestamp;
				} else {
					if (timestamp != element.timestamp) {
						throw new RuntimeException("iterator error");
					}
				}

				int[] inputIteratorIndices = inputIndices[iteratorIndex];
				for (int inputIteratorIndex = 0; inputIteratorIndex < inputIteratorIndices.length; inputIteratorIndex++) {
					float value = element.data[inputIteratorIndex];
					if (!Float.isNaN(value)) {
						int pos = inputIteratorIndices[inputIteratorIndex];
						value_cnt[pos]++;
						value_sum[pos] += value;
					}
				}
			}

			float[] outputData = new float[this.schema.length];
			for (int i = 0; i < numSensors; i++) {
				if (value_cnt[i] >= minCount) {
					outputData[i] = value_sum[i] / value_cnt[i];
				} else {
					outputData[i] = Float.NaN;
				}
				outputData[i + numSensors] = (float) value_cnt[i];
			}
			return new TsEntry(timestamp, outputData);
		}
	}

	@Override
	public ProcessingChain getProcessingChain() {
		return ProcessingChain.of(input_iterators, this);
	}
}