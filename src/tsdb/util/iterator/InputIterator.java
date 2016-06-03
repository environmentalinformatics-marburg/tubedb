package tsdb.util.iterator;

import tsdb.util.TsSchema;
import tsdb.util.processingchain.ProcessingChain;

/**
 * Base class for iterators that process input from one time series iterator.
 * @author woellauer
 *
 */
public abstract class InputIterator extends TsIterator {
	
	/**
	 * Input iterator used as source.
	 */
	protected final TsIterator input_iterator;

	public InputIterator(TsIterator input_iterator, TsSchema output_schema) {
		super(output_schema);
		this.input_iterator = input_iterator;
	}
	
	/**
	 * Default implementation: delegate to input_iterator.
	 */
	@Override
	public boolean hasNext() {
		return input_iterator.hasNext();
	}

	/**
	 * Default implementation: Get processing chain with this iterator and input_iterator as source(-chain).
	 */
	@Override
	public ProcessingChain getProcessingChain() {		
		return ProcessingChain.of(input_iterator,this);
	}	
}
