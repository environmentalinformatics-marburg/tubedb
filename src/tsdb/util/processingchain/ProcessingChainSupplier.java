package tsdb.util.processingchain;

/**
 * Interface for classes that produce processing chains.
 * @author woellauer
 *
 */
@FunctionalInterface
public interface ProcessingChainSupplier {

	/**
	 * get processing chain for this object.
	 * @return
	 */
	ProcessingChain getProcessingChain();
	
	/**
	 * chain with one node as unknown source
	 * @return
	 */
	public static ProcessingChainSupplier createUnknown() {
		return ()->ProcessingChain.createUnknown();
	}
	
}
