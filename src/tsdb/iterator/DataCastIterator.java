package tsdb.iterator;

import static tsdb.util.AssumptionCheck.throwEmpty;


import org.tinylog.Logger;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.TsSchema.Aggregation;
import tsdb.util.iterator.MoveIterator;
import tsdb.util.iterator.TsIterator;
import tsdb.util.processingchain.ProcessingChain;

public class DataCastIterator extends MoveIterator {
	@SuppressWarnings("unused")
	

	private TsIterator[] input_iterators;
	private int[][] inputIndices;

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

	public DataCastIterator(String[] schema, TsIterator[] input_iterators, int[][] inputIndices) {
		super(createSchema(schema, input_iterators));
		this.input_iterators = input_iterators;
		this.inputIndices = inputIndices;
	}

	@Override
	protected TsEntry getNext() {
		long timestamp = -1;
		float[] values = TsEntry.createNanData(this.schema.length);
		for (int iteratorIndex = 0; iteratorIndex < input_iterators.length; iteratorIndex++) {
			TsIterator it = input_iterators[iteratorIndex];
			if(!it.hasNext()) {
				return null;
			}
			TsEntry element = it.next();
			if(timestamp == -1) {
				timestamp = element.timestamp;
			} else {
				if(timestamp != element.timestamp) {
					throw new RuntimeException("iterator error");
				}
			}
			int[] inputIteratorIndices = inputIndices[iteratorIndex];
			for (int inputIteratorIndex = 0; inputIteratorIndex < inputIteratorIndices.length; inputIteratorIndex++) {
				float value = element.data[inputIteratorIndex];
				//Logger.info("value " + value + "  it " + iteratorIndex + " " + inputIteratorIndex + " -> " + inputIteratorIndices[inputIteratorIndex]);
				if(!Float.isNaN(value)) {
					int outputPos = inputIteratorIndices[inputIteratorIndex];
					if(outputPos >= 0) {
						values[outputPos] = value;
					}
				}				
			}
		}
		return new TsEntry(timestamp, values);
	}

	@Override
	public ProcessingChain getProcessingChain() {
		return ProcessingChain.of(input_iterators, this);
	}
}
