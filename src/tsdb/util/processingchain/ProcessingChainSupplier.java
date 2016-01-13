package tsdb.util.processingchain;

/**
 * Interface for classes that produce processing chains.
 * @author woellauer
 *
 */
@FunctionalInterface
public interface ProcessingChainSupplier {

	ProcessingChain getProcessingChain();
	
	public static ProcessingChainSupplier createUnknown() {
		return ()->ProcessingChain.createUnknown();
	}
	
}
