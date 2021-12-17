package tsdb.iterator;

import static tsdb.util.AssumptionCheck.throwEmpty;

import java.util.Arrays;


import org.tinylog.Logger;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.TsSchema.Aggregation;
import tsdb.util.iterator.TsIterator;
import tsdb.util.processingchain.ProcessingChain;

public class DataCastRawIterator extends TsIterator {
	@SuppressWarnings("unused")
	
	
	private final float[] NAN_DATA;
	private TsEntry curr;
	private TsEntry[] next;

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

	public DataCastRawIterator(String[] schema, TsIterator[] input_iterators, int[][] inputIndices) {
		super(createSchema(schema, input_iterators));
		this.input_iterators = input_iterators;
		this.inputIndices = inputIndices;		
		NAN_DATA = new float[schema.length];
		for(int i = 0; i < schema.length; i++) {
			NAN_DATA[i] = Float.NaN;
		}
		next = new TsEntry[input_iterators.length];		
		for(int i = 0; i < input_iterators.length; i++) {
			if(input_iterators[i].hasNext()) {
				next[i] = input_iterators[i].next(); 
			} else {
				next[i] = null;
			}				
		}
		calcNext();
	}

	@Override
	public ProcessingChain getProcessingChain() {
		return ProcessingChain.of(input_iterators, this);
	}
	
	private void calcNext() {
		long timestamp = Integer.MAX_VALUE;
		for(int i = 0; i < input_iterators.length; i++) {
			if(next[i] != null && next[i].timestamp < timestamp) {
				timestamp = next[i].timestamp;
			}
		}
		if(timestamp == Integer.MAX_VALUE) {
			curr = null;
			return;
		}
		float[] values = Arrays.copyOf(NAN_DATA, NAN_DATA.length);
		for(int iteratorIndex = 0; iteratorIndex < input_iterators.length; iteratorIndex++) {
			TsEntry element = next[iteratorIndex];
			if(element != null && element.timestamp == timestamp) {				
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
				if(input_iterators[iteratorIndex].hasNext()) {
					next[iteratorIndex] = input_iterators[iteratorIndex].next();
				} else {
					next[iteratorIndex] = null;
				}
			}
		}
		curr = new TsEntry(timestamp, values);		
	}

	@Override
	public boolean hasNext() {
		return curr != null;
	}

	@Override
	public TsEntry next() {
		TsEntry r = curr;
		calcNext();
		return r;
	}
}
