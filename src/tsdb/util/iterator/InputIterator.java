package tsdb.util.iterator;

import tsdb.util.TsSchema;
import tsdb.util.processingchain.ProcessingChain;

/**
 * Base class for iterators that process input from one TimeSeriesIterator
 * @author woellauer
 *
 */
public abstract class InputIterator extends TsIterator {
	
	protected final TsIterator input_iterator;

	public InputIterator(TsIterator input_iterator, TsSchema output_schema) {
		super(output_schema);
		this.input_iterator = input_iterator;
	}

	@Override
	public ProcessingChain getProcessingChain() {		
		return ProcessingChain.of(input_iterator,this);
	}

	
}
