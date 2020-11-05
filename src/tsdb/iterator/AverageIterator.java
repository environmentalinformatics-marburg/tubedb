package tsdb.iterator;

import static tsdb.util.AssumptionCheck.throwEmpty;

import java.util.Arrays;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.TsSchema.Aggregation;
import tsdb.util.Util;
import tsdb.util.iterator.MoveIterator;
import tsdb.util.iterator.TsIterator;
import tsdb.util.processingchain.ProcessingChain;

/**
 * This iterator outputs elements of average values of input_iterator values.
 * input_iterators need to be in same timestamp per element order.
 * @author woellauer
 *
 */
public class AverageIterator extends MoveIterator {
	@SuppressWarnings("unused")
	private static final Logger log = LogManager.getLogger();

	private Map<String, Integer> schemaMap;
	private TsIterator[] input_iterators;
	private int[][] inputIndices;
	private final int minCount;
	private final boolean withQualityMeasures;

	private static TsSchema createSchema(String[] names, TsIterator[] input_iterators) {
		throwEmpty(input_iterators);
		TsSchema[] schemas = TsIterator.toSchemas(input_iterators);
		TsSchema.throwDifferentAggregation(schemas);
		Aggregation aggregation = schemas[0].aggregation;
		TsSchema.throwDifferentTimeStep(schemas);
		int timeStep = schemas[0].timeStep;
		TsSchema.throwDifferentContinuous(schemas);
		boolean isContinuous = schemas[0].isContinuous;
		return new TsSchema(names, aggregation,timeStep ,isContinuous);
	}

	public AverageIterator(String[] schema, TsIterator[] input_iterators, int minCount, boolean withQualityMeasures) {
		super(createSchema(schema, input_iterators));
		this.input_iterators = input_iterators;
		this.schemaMap = Util.stringArrayToMap(schema);
		this.minCount = minCount;
		//this.withQualityMeasures = false;
		this.withQualityMeasures = withQualityMeasures;

		inputIndices = new int[input_iterators.length][];
		for (int iteratorIndex = 0; iteratorIndex < input_iterators.length; iteratorIndex++) {
			TsIterator it = input_iterators[iteratorIndex];
			String[] iteratorSchema = it.getNames();
			inputIndices[iteratorIndex] = new int[iteratorSchema.length];
			for(int inputPos = 0; inputPos < iteratorSchema.length; inputPos++) {
				int outputPos = schemaMap.get(iteratorSchema[inputPos]);
				inputIndices[iteratorIndex][inputPos] = outputPos;
			}

		}
	}

	@Override
	protected TsEntry getNext() {
		if(withQualityMeasures) {
			long timestamp = -1;
			int[] value_cnt = new int[this.schema.length];
			float[] value_sum = new float[this.schema.length];
			int[][] value_qualityCounter = new int[schema.length][AbstractAggregationIterator.QUALITY_COUNTERS];
			for (int iteratorIndex = 0; iteratorIndex < input_iterators.length; iteratorIndex++) {
				TsIterator it = input_iterators[iteratorIndex];
				if(!it.hasNext()) {
					return null;
				}
				TsEntry element = it.next();
				if(timestamp==-1) {
					timestamp = element.timestamp;
				} else {
					if(timestamp != element.timestamp) {
						throw new RuntimeException("iterator error");
					}
				}

				int[] inputIteratorIndices = inputIndices[iteratorIndex];
				for (int inputIteratorIndex = 0; inputIteratorIndex < inputIteratorIndices.length; inputIteratorIndex++) {
					float value = element.data[inputIteratorIndex];
					if(!Float.isNaN(value)) {
						int pos = inputIteratorIndices[inputIteratorIndex];
						value_cnt[pos]++;
						value_sum[pos] += value;
						value_qualityCounter[inputIteratorIndex][0]++;
						if(element.interpolated != null && element.interpolated[inputIteratorIndex]) {
							value_qualityCounter[inputIteratorIndex][1]++;
						}						
					}				
				}
			}

			float[] value_avg = new float[this.schema.length];
			for(int i=0;i<this.schema.length;i++) {
				if(value_cnt[i]>=minCount) {
					value_avg[i] = value_sum[i]/value_cnt[i];
				} else {
					value_avg[i] = Float.NaN;
				}
			}
			//log.info(Arrays.deepToString(value_qualityCounter));
			return new TsEntry(timestamp, value_avg, null, value_qualityCounter, null);
		} else {
			long timestamp = -1;
			int[] value_cnt = new int[this.schema.length];
			float[] value_sum = new float[this.schema.length];
			for (int iteratorIndex = 0; iteratorIndex < input_iterators.length; iteratorIndex++) {
				TsIterator it = input_iterators[iteratorIndex];
				if(!it.hasNext()) {
					return null;
				}
				TsEntry element = it.next();
				if(timestamp==-1) {
					timestamp = element.timestamp;
				} else {
					if(timestamp != element.timestamp) {
						throw new RuntimeException("iterator error");
					}
				}

				int[] inputIteratorIndices = inputIndices[iteratorIndex];
				for (int inputIteratorIndex = 0; inputIteratorIndex < inputIteratorIndices.length; inputIteratorIndex++) {
					float value = element.data[inputIteratorIndex];
					if(!Float.isNaN(value)) {
						int pos = inputIteratorIndices[inputIteratorIndex];
						value_cnt[pos]++;
						value_sum[pos] += value;
					}				
				}

				/*String[] schema = it.getNames();
			for(int i=0;i<schema.length;i++) {
				int pos = schemaMap.get(schema[i]);
				float value = element.data[i];
				if(!Float.isNaN(value)) {
					value_cnt[pos]++;
					value_sum[pos] += value;
				}
			}*/

			}

			float[] value_avg = new float[this.schema.length];
			for(int i=0;i<this.schema.length;i++) {
				if(value_cnt[i]>=minCount) {
					value_avg[i] = value_sum[i]/value_cnt[i];
					//System.out.println("cnt: "+value_cnt[i]);
				} else {
					value_avg[i] = Float.NaN;
				}
			}
			return new TsEntry(timestamp, value_avg);
		}
	}

	@Override
	public ProcessingChain getProcessingChain() {
		return ProcessingChain.of(input_iterators, this);
	}
}
