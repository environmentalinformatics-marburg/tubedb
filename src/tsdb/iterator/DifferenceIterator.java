package tsdb.iterator;

import tsdb.util.TsEntry;
import tsdb.util.TsSchema;
import tsdb.util.iterator.TsIterator;
import tsdb.util.processingchain.ProcessingChain;

/**
 * Calculates difference between input_iterator and compare_iterator, corrected by reference values
 * @author woellauer
 *
 */
public class DifferenceIterator extends TsIterator {
	private TsIterator input_iterator;
	private TsIterator compare_iterator;
	private final boolean absoluteDifference;
	private final float[] refValues;

	public static TsSchema createSchema(TsSchema input_schema, TsSchema compare_schema) {
		TsSchema.throwDifferentNames(input_schema, compare_schema);
		TsSchema.throwDifferentAggregation(input_schema, compare_schema);
		TsSchema.throwDifferentTimeStep(input_schema, compare_schema);
		input_schema.throwNotContinuous();
		compare_schema.throwNotContinuous();
		boolean isContinuous = true;
		return new TsSchema(input_schema.names, input_schema.aggregation, input_schema.timeStep, isContinuous);
	}

	public DifferenceIterator(TsIterator input_iterator, TsIterator compare_iterator, boolean absoluteDifference, float[] refValues) {
		super(createSchema(input_iterator.getSchema(),compare_iterator.getSchema()));
		this.input_iterator = input_iterator;
		this.compare_iterator = compare_iterator;
		this.absoluteDifference = absoluteDifference;
		this.refValues = refValues;
	}

	@Override
	public boolean hasNext() {
		return input_iterator.hasNext();
	}

	@Override
	public TsEntry next() {
		TsEntry element = input_iterator.next();
		TsEntry genElement = compare_iterator.next();
		long timestamp = element.timestamp;
		if(timestamp!= genElement.timestamp) {
			throw new RuntimeException("iterator error");
		}

		float[] result = new float[schema.length];
		for(int colIndex=0;colIndex<schema.length;colIndex++) {
			if(absoluteDifference) {
				result[colIndex] = Math.abs(element.data[colIndex]-genElement.data[colIndex]);
			} else {
				result[colIndex] = (element.data[colIndex]-refValues[colIndex])-genElement.data[colIndex];	
			}
		}
		return new TsEntry(timestamp,result);
	}

	@Override
	public ProcessingChain getProcessingChain() {
		return ProcessingChain.of(new TsIterator[]{input_iterator,compare_iterator}, this);
	}
}
