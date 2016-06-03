package tsdb.util.processingchain;

/**
 * Node of processing chain is a processing chain entry and processing chain supplier.
 * @author woellauer
 *
 */
public interface ProcessingChainNode extends ProcessingChainEntry, ProcessingChainSupplier {
	
	/**
	 * Default implementation: Create title for processing chain entry from class name.
	 */
	default String getProcessingTitle() {
		String simpleName = this.getClass().getSimpleName();
		if(simpleName.isEmpty()) {
			return this.getClass().getName();
		}
		return simpleName;
	}
	
	/**
	 * Default implementation: Get processing chain with this as source entry.
	 */
	public default ProcessingChain getProcessingChain() {
		return ProcessingChain.of(this);
	}
}
